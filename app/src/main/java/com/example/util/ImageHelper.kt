package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID

object ImageHelper {

    private const val TAG = "ImageHelper"

    /**
     * Universal Image Model Resolver for Coil:
     * - If HTTPS/HTTP URL (e.g. Supabase, Web, CDN): returns the String URL directly.
     * - If Base64 Data URL (data:image/...;base64,...): decodes into a ByteArray so Coil can render it natively!
     * - If Content URI: returns parsed Uri.
     * - If Local File: checks existence; if absent (e.g. from another device), returns null so placeholder shows gracefully.
     */
    fun buildImageModel(model: Any?): Any? {
        if (model == null) return null
        if (model is ByteArray || model is Bitmap || model is Uri || model is File) {
            return model
        }
        val str = model.toString().trim()
        if (str.isBlank()) return null

        // 1. Web & Supabase Cloud URLs
        if (str.startsWith("http://", ignoreCase = true) || str.startsWith("https://", ignoreCase = true)) {
            return str
        }

        // 2. Base64 Data URLs (data:image/jpeg;base64,....)
        if (str.startsWith("data:", ignoreCase = true) && str.contains("base64,", ignoreCase = true)) {
            return try {
                val base64Data = str.substringAfter("base64,")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                decodedBytes
            } catch (e: Exception) {
                Log.w(TAG, "Failed decoding base64 data URL: ${e.message}")
                null
            }
        }

        // 3. Android Content and Resource Uris
        if (str.startsWith("content://", ignoreCase = true) || str.startsWith("android.resource://", ignoreCase = true)) {
            return try {
                Uri.parse(str)
            } catch (_: Exception) {
                null
            }
        }

        // 4. File system paths
        if (str.startsWith("/") || str.startsWith("file://", ignoreCase = true)) {
            val path = str.removePrefix("file://")
            val file = File(path)
            return if (file.exists() && file.canRead()) file else null
        }

        return str
    }

    /**
     * Reads an image Uri from the gallery and compresses it into an optimized byte array
     * suitable for immediate Supabase Storage upload.
     */
    fun compressImageBytes(
        context: Context,
        uri: Uri,
        isIcon: Boolean
    ): Pair<ByteArray, String>? {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(input) ?: return null
            input?.close()

            val maxWidth = if (isIcon) 256 else 720
            val maxHeight = if (isIcon) 256 else 1280
            val quality = if (isIcon) 85 else 75

            val widthRatio = maxWidth.toFloat() / originalBitmap.width
            val heightRatio = maxHeight.toFloat() / originalBitmap.height
            val scale = minOf(widthRatio, heightRatio, 1.0f)

            val targetWidth = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (originalBitmap.height * scale).toInt().coerceAtLeast(1)

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            val fileName = "img_${if (isIcon) "icon" else "shot"}_${UUID.randomUUID().toString().take(8)}.jpg"
            Pair(bytes, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image bytes from Uri", e)
            null
        }
    }

    /**
     * Converts an image Uri from device into a compact Base64 Data URL
     * for instant Firestore persistence as a backup if offline.
     */
    fun uriToBase64DataUrl(
        context: Context,
        uri: Uri,
        maxWidth: Int = 512,
        maxHeight: Int = 512,
        quality: Int = 70
    ): String? {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(input) ?: return null
            input?.close()

            val widthRatio = maxWidth.toFloat() / originalBitmap.width
            val heightRatio = maxHeight.toFloat() / originalBitmap.height
            val scale = minOf(widthRatio, heightRatio, 1.0f)

            val targetWidth = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (originalBitmap.height * scale).toInt().coerceAtLeast(1)

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } catch (e: Exception) {
            Log.w(TAG, "uriToBase64DataUrl failed: ${e.message}")
            null
        }
    }
}
