package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

data class SupabaseConfig(
    val projectUrl: String,
    val anonKey: String,
    val isEnabled: Boolean = true
)

class SupabaseStorageManager(private val context: Context) {

    companion object {
        private const val TAG = "SupabaseStorage"
        private const val PREFS_NAME = "spoiled_supabase_config"
        private const val KEY_PROJECT_URL = "supabase_project_url"
        private const val KEY_ANON_KEY = "supabase_anon_key"
        private const val KEY_ENABLED = "supabase_enabled"

        // Official buckets defined in schema.sql
        const val BUCKET_ICONS = "app-icons"
        const val BUCKET_SCREENSHOTS = "app-screenshots"
        const val BUCKET_APKS = "app-apks"

        // Default project configuration (can be updated dynamically in UI or via Secrets)
        const val DEFAULT_PROJECT_URL = "https://xwhqeyvcvxljomwepnre.supabase.co"
        const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.placeholder"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getConfig(): SupabaseConfig {
        val url = prefs.getString(KEY_PROJECT_URL, DEFAULT_PROJECT_URL)?.trim().orEmpty()
            .ifBlank { DEFAULT_PROJECT_URL }
        val key = prefs.getString(KEY_ANON_KEY, DEFAULT_ANON_KEY)?.trim().orEmpty()
            .ifBlank { DEFAULT_ANON_KEY }
        val enabled = prefs.getBoolean(KEY_ENABLED, true)
        return SupabaseConfig(url.removeSuffix("/"), key, enabled)
    }

    fun saveConfig(projectUrl: String, anonKey: String, isEnabled: Boolean = true) {
        prefs.edit()
            .putString(KEY_PROJECT_URL, projectUrl.trim().removeSuffix("/"))
            .putString(KEY_ANON_KEY, anonKey.trim())
            .putBoolean(KEY_ENABLED, isEnabled)
            .apply()
    }

    fun isConfigured(): Boolean {
        val config = getConfig()
        return config.isEnabled &&
                config.projectUrl.startsWith("https://", ignoreCase = true) &&
                config.anonKey.isNotBlank() &&
                !config.anonKey.contains("placeholder")
    }

    fun getPublicUrl(bucket: String, fileName: String): String {
        val config = getConfig()
        val cleanName = fileName.removePrefix("/")
        return "${config.projectUrl}/storage/v1/object/public/$bucket/$cleanName"
    }

    /**
     * Uploads binary image/file bytes to Supabase Storage REST endpoint.
     * Endpoint: POST /storage/v1/object/<bucket>/<fileName>
     * Returns the direct public HTTPS URL.
     */
    suspend fun uploadFile(
        bytes: ByteArray,
        bucket: String,
        fileName: String,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (config.projectUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Supabase Project URL is not configured."))
        }

        val cleanName = fileName.replace("\\s+".toRegex(), "_")
        val uploadUrl = "${config.projectUrl}/storage/v1/object/$bucket/$cleanName"
        val publicUrl = getPublicUrl(bucket, cleanName)

        try {
            val mediaType = mimeType.toMediaTypeOrNull()
            val body = bytes.toRequestBody(mediaType)

            val requestBuilder = Request.Builder()
                .url(uploadUrl)
                .post(body)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .header("x-upsert", "true")

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful || response.code == 200 || response.code == 201) {
                Log.d(TAG, "Uploaded $cleanName to Supabase bucket '$bucket'. Public URL: $publicUrl")
                Result.success(publicUrl)
            } else {
                val errorBody = response.body?.string() ?: "Empty response"
                Log.w(TAG, "Supabase upload returned code ${response.code}: $errorBody")
                // Even if custom bucket write failed with permissions, if it's a known bucket, return public URL if configured
                Result.failure(Exception("Supabase HTTP ${response.code}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase upload error for $cleanName", e)
            Result.failure(e)
        }
    }

    /**
     * Upload an APK file from device disk to Supabase Storage.
     */
    suspend fun uploadApkFile(
        file: File,
        customName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val fileName = customName ?: "apk_${UUID.randomUUID().toString().take(8)}_${file.name}"
        if (!file.exists()) {
            return@withContext Result.failure(IllegalArgumentException("File does not exist: ${file.absolutePath}"))
        }
        val bytes = file.readBytes()
        uploadFile(
            bytes = bytes,
            bucket = BUCKET_APKS,
            fileName = fileName,
            mimeType = "application/vnd.android.package-archive"
        )
    }
}
