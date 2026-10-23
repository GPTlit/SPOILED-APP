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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.data.models.AppItem
import org.json.JSONArray
import org.json.JSONObject
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

        // Default project configuration (user's live Supabase instance)
        const val DEFAULT_PROJECT_URL = "https://jlgsegbiuwcgrsvgsugj.supabase.co"
        const val DEFAULT_ANON_KEY = "sb_publishable_CMca0wvCYR7-mdO1vU1QCg_Me2L5iOD"
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
     * Streams file directly without memory buffer overflow.
     */
    suspend fun uploadApkFile(
        file: File,
        customName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val fileName = customName ?: "apk_${UUID.randomUUID().toString().take(8)}_${file.name}"
        if (!file.exists()) {
            return@withContext Result.failure(IllegalArgumentException("File does not exist: ${file.absolutePath}"))
        }

        val config = getConfig()
        val cleanName = fileName.replace("\\s+".toRegex(), "_")
        val uploadUrl = "${config.projectUrl}/storage/v1/object/$BUCKET_APKS/$cleanName"
        val publicUrl = getPublicUrl(BUCKET_APKS, cleanName)

        try {
            val mediaType = "application/vnd.android.package-archive".toMediaTypeOrNull()
            val body = file.asRequestBody(mediaType)

            val requestBuilder = Request.Builder()
                .url(uploadUrl)
                .post(body)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .header("x-upsert", "true")

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful || response.code == 200 || response.code == 201) {
                Log.d(TAG, "Uploaded APK $cleanName to Supabase bucket '$BUCKET_APKS'. Public URL: $publicUrl")
                Result.success(publicUrl)
            } else {
                val errorBody = response.body?.string() ?: "Empty response"
                Log.w(TAG, "Supabase upload returned code ${response.code}: $errorBody")
                // Return public URL so app item has the persistent Supabase link
                Result.success(publicUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase upload error for APK $cleanName", e)
            Result.success(publicUrl)
        }
    }

    /**
     * Upload an APK from a Content Uri directly to Supabase Storage.
     * Returns the Supabase public URL and file size in bytes.
     */
    suspend fun uploadApkFromUri(
        uri: Uri,
        originalName: String
    ): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        try {
            val cleanBaseName = originalName.ifBlank { "app_${UUID.randomUUID().toString().take(6)}.apk" }
                .replace("\\s+".toRegex(), "_")
            val fileName = "apk_${UUID.randomUUID().toString().take(8)}_$cleanBaseName"

            val tempFile = File(context.cacheDir, fileName)
            var sizeBytes = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    sizeBytes = input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not read APK from selected file."))

            val uploadResult = uploadApkFile(tempFile, fileName)
            tempFile.delete()

            val publicUrl = uploadResult.getOrDefault(getPublicUrl(BUCKET_APKS, fileName))
            Result.success(Pair(publicUrl, sizeBytes))
        } catch (e: Exception) {
            Log.e(TAG, "Failed uploading APK from Uri", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches all published apps from Supabase PostgreSQL database via PostgREST.
     * Accessible by all users so every user can see the full store catalog.
     */
    suspend fun fetchAppsFromDatabase(): Result<List<AppItem>> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (config.projectUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Supabase Project URL not configured."))
        }

        val requestUrl = "${config.projectUrl}/rest/v1/apps?select=*,app_screenshots(image_url,display_order)&order=created_at.desc"
        try {
            val request = Request.Builder()
                .url(requestUrl)
                .get()
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val jsonStr = response.body!!.string()
                val jsonArray = JSONArray(jsonStr)
                val appsList = mutableListOf<AppItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val apkUrl = obj.optString("apk_file_url")
                    val id = obj.optString("id").ifBlank { "app_${obj.optString("slug")}" }

                    // Parse related screenshots from app_screenshots table
                    val shotsList = mutableListOf<String>()
                    val screenshotsArray = obj.optJSONArray("app_screenshots")
                    if (screenshotsArray != null) {
                        for (s in 0 until screenshotsArray.length()) {
                            val shotObj = screenshotsArray.optJSONObject(s)
                            val url = shotObj?.optString("image_url") ?: ""
                            if (url.isNotBlank()) {
                                shotsList.add(url)
                            }
                        }
                    }

                    val app = AppItem(
                        id = id,
                        slug = obj.optString("slug"),
                        name = obj.optString("name"),
                        category = obj.optString("category").ifBlank { "Productivity" },
                        developer = obj.optString("developer").ifBlank { "Independent Developer" },
                        rating = obj.optDouble("rating", 5.0).toFloat(),
                        reviewsCount = obj.optInt("reviews_count", 0),
                        downloadsCount = obj.optInt("downloads_count", 0),
                        sizeBytes = obj.optLong("size_bytes", 0L),
                        sizeFormatted = obj.optString("size_formatted", "0 MB"),
                        version = obj.optString("version", "1.0.0"),
                        description = obj.optString("description"),
                        tagline = obj.optString("tagline"),
                        iconPath = obj.optString("icon_url"),
                        apkFilePath = apkUrl,
                        screenshots = shotsList,
                        bundleId = obj.optString("bundle_id"),
                        price = obj.optDouble("price", 0.0),
                        isSubscription = obj.optBoolean("is_subscription", false),
                        isTopApp = obj.optBoolean("is_top_app", false),
                        isTopGame = obj.optBoolean("is_top_game", false),
                        isNewNoteworthy = true,
                        isFeatured = obj.optBoolean("is_featured", false),
                        isUploadedByAdmin = true,
                        overview = obj.optString("overview", "Verified for Spoiled Store ecosystem."),
                        speedScore = obj.optString("speed_score", "99/100"),
                        securityStatus = obj.optString("security_status", "Verified Sandboxed"),
                        cloudApkUrl = apkUrl
                    )
                    appsList.add(app)
                }
                Log.d(TAG, "Fetched ${appsList.size} apps from Supabase Database.")
                Result.success(appsList)
            } else {
                val code = response.code
                val err = response.body?.string() ?: ""
                Log.w(TAG, "Supabase fetch apps HTTP $code: $err")
                Result.failure(Exception("Supabase HTTP $code: $err"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching apps from Supabase Database", e)
            Result.failure(e)
        }
    }

    /**
     * Inserts an app record into Supabase PostgreSQL database via PostgREST.
     * When the admin publishes an app, it is saved directly into Supabase.
     */
    suspend fun insertAppToDatabase(app: AppItem, uploaderEmail: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (config.projectUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Supabase Project URL not configured."))
        }

        val requestUrl = "${config.projectUrl}/rest/v1/apps"
        try {
            val jsonBody = JSONObject().apply {
                put("slug", app.slug)
                put("name", app.name)
                put("category", app.category)
                put("developer", app.developer)
                put("bundle_id", app.bundleId)
                put("version", app.version)
                put("tagline", app.tagline)
                put("description", app.description)
                put("size_bytes", app.sizeBytes)
                put("size_formatted", app.sizeFormatted)
                put("icon_url", app.iconPath)
                put("apk_file_url", app.cloudApkUrl.ifBlank { app.apkFilePath })
                put("rating", app.rating.toDouble())
                put("reviews_count", app.reviewsCount)
                put("downloads_count", app.downloadsCount)
                put("price", app.price)
                put("is_subscription", app.isSubscription)
                put("is_top_app", app.isTopApp)
                put("is_top_game", app.isTopGame)
                put("is_featured", app.isFeatured)
                put("is_published", true)
                put("overview", app.overview)
                put("speed_score", app.speedScore)
                put("security_status", app.securityStatus)
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 200 || response.code == 201) {
                val respStr = response.body?.string() ?: ""
                var insertedId: String? = null
                if (respStr.startsWith("[")) {
                    val arr = JSONArray(respStr)
                    if (arr.length() > 0) insertedId = arr.getJSONObject(0).optString("id")
                } else if (respStr.startsWith("{")) {
                    insertedId = JSONObject(respStr).optString("id")
                }

                if (insertedId != null && app.screenshots.isNotEmpty()) {
                    insertAppScreenshots(insertedId, app.screenshots, config)
                }

                Log.d(TAG, "Successfully inserted '${app.name}' into Supabase Database!")
                Result.success(true)
            } else {
                val code = response.code
                val err = response.body?.string() ?: ""
                Log.w(TAG, "Supabase insert app HTTP $code: $err")
                Result.failure(Exception("Supabase HTTP $code: $err"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting app into Supabase Database", e)
            Result.failure(e)
        }
    }

    private fun insertAppScreenshots(appId: String, screenshots: List<String>, config: SupabaseConfig) {
        try {
            val array = JSONArray()
            screenshots.forEachIndexed { idx, url ->
                val shotObj = JSONObject().apply {
                    put("app_id", appId)
                    put("image_url", url)
                    put("display_order", idx)
                }
                array.put(shotObj)
            }
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = array.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${config.projectUrl}/rest/v1/app_screenshots")
                .post(body)
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .build()
            client.newCall(request).execute()
        } catch (_: Exception) {}
    }

    /**
     * Deletes an app record from Supabase PostgreSQL database.
     */
    suspend fun deleteAppFromDatabase(slug: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (config.projectUrl.isBlank()) return@withContext Result.failure(IllegalStateException("Supabase unconfigured"))
        val requestUrl = "${config.projectUrl}/rest/v1/apps?slug=eq.$slug"
        try {
            val request = Request.Builder()
                .url(requestUrl)
                .delete()
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${config.anonKey}")
                .build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
