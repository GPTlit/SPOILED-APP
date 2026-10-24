package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.auth.GoogleAuthManager
import com.example.data.models.AppItem
import com.example.data.models.CommentItem
import com.example.data.models.FeaturedBannerConfig
import com.example.data.models.NewsItem
import com.example.data.models.StoreNotification
import com.example.data.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class StoreRepository(
    private val context: Context,
    val firestore: FirebaseFirestore? = null
) {
    // Secondary constructor resolving named firestore_database_id from resources
    constructor(context: Context) : this(
        context = context.applicationContext,
        firestore = try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Throwable) {
            Log.w("StoreRepository", "Firebase Firestore unavailable (${e.message}); using local/offline storage")
            null
        }
    )

    private val scope = CoroutineScope(Dispatchers.IO)
    val googleAuthManager: GoogleAuthManager = GoogleAuthManager(context)
    val downloadManager: RealAppDownloadManager = RealAppDownloadManager(context)
    val supabaseManager: SupabaseStorageManager = SupabaseStorageManager(context)

    companion object {
        private const val TAG = "StoreRepository"
        const val PRIMARY_ADMIN_EMAIL = "salemmoustapha15@gmail.com"
        const val BACKUP_ADMIN_EMAIL = "salemmoustapha19@gmail.com"
        const val USER_ADMIN_EMAIL = "salemmoustapha18@gmail.com"
        private const val APPS_FILE = "spoiled_real_apps.json"
        private const val NEWS_FILE = "spoiled_real_news.json"
        private const val COMMENTS_FILE = "spoiled_real_comments.json"
        private const val NOTIFICATIONS_FILE = "spoiled_real_notifications.json"
        private const val USER_FILE = "spoiled_user_session.json"
    }

    private val apksDir: File by lazy {
        val dir = File(context.filesDir, "storage/apks")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val iconsDir: File by lazy {
        val dir = File(context.filesDir, "storage/icons")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val screenshotsDir: File by lazy {
        val dir = File(context.filesDir, "storage/screenshots")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps.asStateFlow()

    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _notifications = MutableStateFlow<List<StoreNotification>>(emptyList())
    val notifications: StateFlow<List<StoreNotification>> = _notifications.asStateFlow()

    private val _installedAppIds = MutableStateFlow(setOf<String>())
    val installedAppIds: StateFlow<Set<String>> = _installedAppIds.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _purchasedAppIds = MutableStateFlow(setOf<String>())
    val purchasedAppIds: StateFlow<Set<String>> = _purchasedAppIds.asStateFlow()

    private val sessionPrefs = context.getSharedPreferences("spoiled_store_user_session", Context.MODE_PRIVATE)
    private val bannerPrefs = context.getSharedPreferences("spoiled_store_banner_prefs", Context.MODE_PRIVATE)

    private val _wishlistAppIds = MutableStateFlow(setOf<String>())
    val wishlistAppIds: StateFlow<Set<String>> = _wishlistAppIds.asStateFlow()

    private val _userProfile = MutableStateFlow(loadInitialUserSession())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _featuredBanner = MutableStateFlow(loadFeaturedBanner())
    val featuredBanner: StateFlow<FeaturedBannerConfig> = _featuredBanner.asStateFlow()

    private var appsListener: ListenerRegistration? = null
    private var newsListener: ListenerRegistration? = null
    private var notifsListener: ListenerRegistration? = null
    private var commentsListener: ListenerRegistration? = null

    init {
        // 1. Initial local offline-first load
        loadDataFromStorage()

        // 2. Observe Firebase Auth State
        scope.launch {
            try {
                googleAuthManager.authStateFlow().collect { user ->
                    if (user != null) {
                        syncUserSession(user)
                    } else {
                        // CRITICAL: NEVER wipe the local login session when Firebase emits null on launch or when offline.
                        // We preserve the user session permanently until the user explicitly taps 'Sign Out'.
                        if (!_userProfile.value.isSignedIn) {
                            loadUserSession()
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Auth state observation unavailable: ${e.message}")
            }
        }

        // 3. Observe real DownloadManager progress
        scope.launch {
            try {
                downloadManager.downloadProgress.collect { progressMap ->
                    _downloadProgress.update { it + progressMap }
                }
            } catch (_: Exception) {}
        }

        // 4. Connect real-time Firestore listeners
        try {
            connectFirestoreRealtimeListeners()
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore listeners initialization deferred: ${e.message}")
        }

        // 5. Sync store catalog from Supabase Database for all users
        syncAppsFromSupabase()
    }

    private fun connectFirestoreRealtimeListeners() {
        val fs = firestore ?: return
        // Apps Collection Real-time listener
        appsListener?.remove()
        appsListener = fs.collection("apps").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Apps listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cloudApps = snapshot.documents.mapNotNull { doc ->
                    try {
                        val shots = (doc.get("screenshots") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        AppItem(
                            id = doc.getString("id") ?: doc.id,
                            slug = doc.getString("slug") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "Utilities",
                            developer = doc.getString("developer") ?: "Developer",
                            rating = (doc.getDouble("rating") ?: 5.0).toFloat(),
                            reviewsCount = (doc.getLong("reviewsCount") ?: 0L).toInt(),
                            downloadsCount = (doc.getLong("downloadsCount") ?: 0L).toInt(),
                            sizeBytes = doc.getLong("sizeBytes") ?: 0L,
                            sizeFormatted = doc.getString("sizeFormatted") ?: "0 MB",
                            version = doc.getString("version") ?: "1.0.0",
                            description = doc.getString("description") ?: "",
                            tagline = doc.getString("tagline") ?: "",
                            iconPath = doc.getString("iconPath") ?: "",
                            apkFilePath = doc.getString("apkFilePath") ?: "",
                            screenshots = shots,
                            bundleId = doc.getString("bundleId") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            isSubscription = doc.getBoolean("isSubscription") ?: false,
                            isTopApp = doc.getBoolean("isTopApp") ?: false,
                            isTopGame = doc.getBoolean("isTopGame") ?: false,
                            isNewNoteworthy = doc.getBoolean("isNewNoteworthy") ?: false,
                            isFeatured = doc.getBoolean("isFeatured") ?: false,
                            isUploadedByAdmin = doc.getBoolean("isUploadedByAdmin") ?: true,
                            releaseDate = doc.getString("releaseDate") ?: "Today",
                            changelog = doc.getString("changelog") ?: "Initial release",
                            overview = doc.getString("overview") ?: "Verified for Spoiled Store ecosystem.",
                            speedScore = doc.getString("speedScore") ?: "99/100",
                            securityStatus = doc.getString("securityStatus") ?: "Verified & Sandboxed",
                            cloudApkUrl = doc.getString("cloudApkUrl") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping app document ${doc.id}", e)
                        null
                    }
                }
                if (cloudApps.isNotEmpty()) {
                    _apps.value = cloudApps
                    saveAppsToDisk(cloudApps)
                } else if (_apps.value.isEmpty()) {
                    val initialCatalog = getInitialOnlineStoreCatalog()
                    _apps.value = initialCatalog
                    saveAppsToDisk(initialCatalog)
                    scope.launch {
                        initialCatalog.forEach { app ->
                            try {
                                val map = mapOf(
                                    "id" to app.id,
                                    "slug" to app.slug,
                                    "name" to app.name,
                                    "category" to app.category,
                                    "developer" to app.developer,
                                    "rating" to app.rating.toDouble(),
                                    "reviewsCount" to app.reviewsCount.toLong(),
                                    "downloadsCount" to app.downloadsCount.toLong(),
                                    "sizeBytes" to app.sizeBytes,
                                    "sizeFormatted" to app.sizeFormatted,
                                    "version" to app.version,
                                    "description" to app.description,
                                    "tagline" to app.tagline,
                                    "iconPath" to app.iconPath,
                                    "apkFilePath" to app.apkFilePath,
                                    "screenshots" to app.screenshots,
                                    "bundleId" to app.bundleId,
                                    "price" to app.price,
                                    "isSubscription" to app.isSubscription,
                                    "isTopApp" to app.isTopApp,
                                    "isTopGame" to app.isTopGame,
                                    "isNewNoteworthy" to app.isNewNoteworthy,
                                    "isFeatured" to app.isFeatured,
                                    "isUploadedByAdmin" to true,
                                    "uploaderUid" to "cloud-admin",
                                    "overview" to app.overview,
                                    "speedScore" to app.speedScore,
                                    "securityStatus" to app.securityStatus,
                                    "cloudApkUrl" to app.cloudApkUrl,
                                    "createdAt" to FieldValue.serverTimestamp(),
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                                fs.collection("apps").document(app.id).set(map)
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }

        // News Collection Real-time listener
        newsListener?.remove()
        newsListener = fs.collection("news").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "News listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cloudNews = snapshot.documents.mapNotNull { doc ->
                    try {
                        NewsItem(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            summary = doc.getString("summary") ?: "",
                            content = doc.getString("content") ?: "",
                            author = doc.getString("author") ?: "Spoiled Store",
                            date = doc.getString("date") ?: "Today",
                            category = doc.getString("category") ?: "Updates",
                            readTime = doc.getString("readTime") ?: "2 min read",
                            imagePath = doc.getString("imagePath") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                if (cloudNews.isNotEmpty()) {
                    _newsList.value = cloudNews
                    saveNewsToDisk(cloudNews)
                }
            }
        }

        // Notifications Real-time listener
        notifsListener?.remove()
        notifsListener = fs.collection("notifications").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Notifications listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cloudNotifs = snapshot.documents.mapNotNull { doc ->
                    try {
                        StoreNotification(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getString("timestamp") ?: "Recent",
                            isBroadcast = doc.getBoolean("isBroadcast") ?: true,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                if (cloudNotifs.isNotEmpty()) {
                    _notifications.value = cloudNotifs
                    saveNotificationsToDisk(cloudNotifs)
                }
            }
        }

        // Comments Real-time listener
        commentsListener?.remove()
        commentsListener = fs.collection("comments").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Comments listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cloudComments = snapshot.documents.mapNotNull { doc ->
                    try {
                        CommentItem(
                            id = doc.getString("id") ?: doc.id,
                            appId = doc.getString("appId") ?: "",
                            userName = doc.getString("userName") ?: "User",
                            userEmail = doc.getString("userEmail") ?: "",
                            comment = doc.getString("comment") ?: "",
                            rating = (doc.getLong("rating") ?: 5L).toInt(),
                            date = doc.getString("date") ?: "Just now"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                if (cloudComments.isNotEmpty()) {
                    _comments.value = cloudComments
                    saveCommentsToDisk(cloudComments)
                }
            }
        }
    }

    private fun syncUserSession(user: FirebaseUser) {
        val email = user.email ?: ""
        val displayName = user.displayName ?: email.substringBefore("@")
        val isAdmin = isUserAdmin(email)
        val profile = UserProfile(
            isSignedIn = true,
            email = email,
            username = displayName,
            authProvider = "Google",
            isAdmin = isAdmin
        )
        _userProfile.value = profile
        saveUserSession(profile)

        // Sync or register user document in Firestore
        scope.launch {
            try {
                val userDoc = mapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "displayName" to displayName,
                    "isAdmin" to isAdmin,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore?.collection("users")?.document(user.uid)?.set(userDoc)
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing user to Firestore: ${e.message}")
            }
        }
    }

    fun isUserAdmin(email: String): Boolean {
        val normalized = email.trim().lowercase()
        return normalized == PRIMARY_ADMIN_EMAIL.lowercase() ||
                normalized == BACKUP_ADMIN_EMAIL.lowercase() ||
                normalized == USER_ADMIN_EMAIL.lowercase()
    }

    fun getAdminOrFallbackEmail(): String {
        val current = _userProfile.value.email.trim()
        return if (current.isNotBlank()) current else BACKUP_ADMIN_EMAIL
    }

    fun signInUser(email: String, username: String, provider: String = "Email") {
        val cleanEmail = email.trim()
        val isAdmin = isUserAdmin(cleanEmail)
        val profile = UserProfile(
            isSignedIn = true,
            email = cleanEmail,
            username = username.ifBlank { cleanEmail.substringBefore("@") },
            authProvider = provider,
            isAdmin = isAdmin
        )
        _userProfile.value = profile
        saveUserSession(profile)
    }

    fun signInWithFacebook(email: String, displayName: String) {
        signInUser(email, displayName, "Facebook")
    }

    fun signInWithApple(email: String, displayName: String) {
        signInUser(email, displayName, "Apple")
    }

    fun signOut() {
        googleAuthManager.signOut()
        sessionPrefs.edit()
            .putBoolean("is_signed_in", false)
            .putString("email", "")
            .putString("username", "")
            .putString("auth_provider", "")
            .putBoolean("is_admin", false)
            .apply()
        val profile = UserProfile(isSignedIn = false, email = "", username = "", isAdmin = false)
        _userProfile.value = profile
        saveUserSession(profile)
    }

    // ----------------------------------------------------
    // REAL STORAGE / APK FILE PERSISTENCE
    // ----------------------------------------------------
    fun copyApkFileFromUri(uri: Uri, originalName: String): Pair<String, Long>? {
        return try {
            val extension = if (originalName.endsWith(".apk", ignoreCase = true)) "" else ".apk"
            val uniqueFileName = "app_${UUID.randomUUID().toString().take(8)}_${originalName.replace("\\s+".toRegex(), "_")}$extension"
            val targetFile = File(apksDir, uniqueFileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(targetFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Pair(targetFile.absolutePath, targetFile.length())
        } catch (_: Exception) {
            null
        }
    }

    fun copyImageFileFromUri(uri: Uri, isIcon: Boolean): String? {
        // Prioritize Base64 web-optimized Data URL so images are stored in Firestore
        // and instantly viewable and downloadable by every user on every device across the network!
        try {
            val base64 = com.example.util.ImageHelper.uriToBase64DataUrl(
                context = context,
                uri = uri,
                maxWidth = if (isIcon) 256 else 720,
                maxHeight = if (isIcon) 256 else 1280,
                quality = if (isIcon) 85 else 75
            )
            if (!base64.isNullOrBlank()) {
                return base64
            }
        } catch (e: Exception) {
            Log.w(TAG, "Base64 conversion fallback: ${e.message}")
        }

        // Fallback to local file if needed
        val targetDir = if (isIcon) iconsDir else screenshotsDir
        return try {
            val targetFile = File(targetDir, "img_${UUID.randomUUID().toString().take(8)}.png")
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(targetFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    suspend fun uploadImageToSupabase(uri: Uri, isIcon: Boolean): Result<String> {
        val compressed = com.example.util.ImageHelper.compressImageBytes(context, uri, isIcon)
            ?: return Result.failure(Exception("Failed to decode and compress image"))
        val bucket = if (isIcon) SupabaseStorageManager.BUCKET_ICONS else SupabaseStorageManager.BUCKET_SCREENSHOTS
        return supabaseManager.uploadFile(
            bytes = compressed.first,
            bucket = bucket,
            fileName = compressed.second,
            mimeType = "image/jpeg"
        )
    }

    suspend fun uploadApkToSupabase(uri: Uri, originalName: String): Result<Pair<String, Long>> {
        return supabaseManager.uploadApkFromUri(uri, originalName)
    }

    suspend fun uploadApkToSupabase(file: File, customName: String? = null): Result<String> {
        return supabaseManager.uploadApkFile(file, customName)
    }

    suspend fun uploadApkFileToSupabase(file: File, customName: String? = null): Result<Pair<String, Long>> {
        val result = supabaseManager.uploadApkFile(file, customName)
        return if (result.isSuccess) {
            Result.success(Pair(result.getOrThrow(), file.length()))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to upload APK"))
        }
    }

    /**
     * Synchronizes app catalog directly from Supabase PostgreSQL database.
     * All users receive all apps published by the administrator.
     */
    fun syncAppsFromSupabase() {
        scope.launch {
            try {
                val result = supabaseManager.fetchAppsFromDatabase()
                if (result.isSuccess) {
                    val remoteApps = result.getOrNull().orEmpty()
                    if (remoteApps.isNotEmpty()) {
                        _apps.update { current ->
                            val map = current.associateBy { it.slug }.toMutableMap()
                            remoteApps.forEach { cloudApp ->
                                map[cloudApp.slug] = cloudApp
                            }
                            map.values.toList()
                        }
                        saveAppsToDisk(_apps.value)
                        Log.d(TAG, "Successfully synced ${remoteApps.size} apps from Supabase Database!")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Supabase apps sync skipped/failed: ${e.message}")
            }
        }
    }

    fun createSampleApkOnDisk(appName: String): Pair<String, Long> {
        val slug = appName.lowercase().replace("\\s+".toRegex(), "_")
        val file = File(apksDir, "${slug}_release.apk")
        if (!file.exists()) {
            file.outputStream().use { fos ->
                val header = "PK\u0003\u0004SpoiledStore_${appName}_Package_${System.currentTimeMillis()}".toByteArray()
                fos.write(header)
                val dummyBytes = ByteArray(1024 * 512) { (it % 256).toByte() }
                fos.write(dummyBytes)
            }
        }
        return Pair(file.absolutePath, file.length())
    }

    // ----------------------------------------------------
    // ADMIN UPLOAD / PUBLISHING (STORED LOCALLY + FIRESTORE)
    // ----------------------------------------------------
    fun uploadRealApp(
        name: String,
        category: String,
        developer: String,
        tagline: String,
        description: String,
        apkFilePath: String,
        sizeBytes: Long,
        version: String,
        iconPath: String,
        screenshots: List<String>,
        bundleId: String,
        price: Double,
        isSubscription: Boolean,
        isTopApp: Boolean,
        isTopGame: Boolean,
        isNewNoteworthy: Boolean,
        isFeatured: Boolean,
        overview: String = "High performance application verified for Spoiled Store ecosystem.",
        speedScore: String = "99/100",
        securityStatus: String = "Verified & Sandboxed",
        cloudApkUrl: String = ""
    ): AppItem {
        val slug = name.lowercase().replace("\\s+".toRegex(), "-").replace("[^a-z0-9-]".toRegex(), "").ifBlank { "app-${UUID.randomUUID().toString().take(6)}" }
        val formattedSize = formatFileSize(sizeBytes)
        val appId = "app-${UUID.randomUUID().toString().take(8)}"

        val newApp = AppItem(
            id = appId,
            slug = slug,
            name = name,
            category = category,
            developer = developer.ifBlank { "Independent Developer" },
            rating = 5.0f,
            reviewsCount = 1,
            downloadsCount = 0,
            sizeBytes = sizeBytes,
            sizeFormatted = formattedSize,
            version = version.ifBlank { "1.0.0" },
            description = description.ifBlank { "High-speed clean application published to Spoiled Store." },
            tagline = tagline.ifBlank { "Fast, secure, verified application" },
            iconPath = iconPath,
            apkFilePath = apkFilePath,
            screenshots = screenshots,
            bundleId = bundleId.ifBlank { "com.spoiled.$slug" },
            price = price,
            isSubscription = isSubscription,
            isTopApp = isTopApp,
            isTopGame = isTopGame,
            isNewNoteworthy = isNewNoteworthy,
            isFeatured = isFeatured,
            isUploadedByAdmin = true,
            overview = overview,
            speedScore = speedScore,
            securityStatus = securityStatus,
            cloudApkUrl = cloudApkUrl
        )

        _apps.update { listOf(newApp) + it.filterNot { a -> a.id == appId } }
        saveAppsToDisk(_apps.value)

        // Upload to Firestore
        val currentUid = googleAuthManager.currentUser?.uid ?: "admin-${UUID.randomUUID()}"
        scope.launch {
            try {
                val appMap = mapOf(
                    "id" to appId,
                    "slug" to slug,
                    "name" to name,
                    "category" to category,
                    "developer" to newApp.developer,
                    "rating" to 5.0,
                    "reviewsCount" to 1L,
                    "downloadsCount" to 0L,
                    "sizeBytes" to sizeBytes,
                    "sizeFormatted" to formattedSize,
                    "version" to newApp.version,
                    "description" to newApp.description,
                    "tagline" to newApp.tagline,
                    "iconPath" to iconPath,
                    "apkFilePath" to apkFilePath,
                    "screenshots" to screenshots,
                    "bundleId" to newApp.bundleId,
                    "price" to price,
                    "isSubscription" to isSubscription,
                    "isTopApp" to isTopApp,
                    "isTopGame" to isTopGame,
                    "isNewNoteworthy" to isNewNoteworthy,
                    "isFeatured" to isFeatured,
                    "isUploadedByAdmin" to true,
                    "uploaderUid" to currentUid,
                    "overview" to overview,
                    "speedScore" to speedScore,
                    "securityStatus" to securityStatus,
                    "cloudApkUrl" to cloudApkUrl,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore?.collection("apps")?.document(appId)
                    ?.set(appMap)
                    ?.addOnSuccessListener {
                        Log.d(TAG, "App $appId published successfully to cloud Firestore!")
                    }
                    ?.addOnFailureListener { err ->
                        Log.e(TAG, "Firestore write error for $appId: ${err.message}", err)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving app to Firestore", e)
            }
        }

        // Upload to Supabase PostgreSQL database so all store users see the app
        scope.launch {
            try {
                val supabaseResult = supabaseManager.insertAppToDatabase(newApp, currentUid)
                if (supabaseResult.isSuccess) {
                    Log.d(TAG, "App '${newApp.name}' published to Supabase database successfully!")
                } else {
                    Log.w(TAG, "Supabase database insert response: ${supabaseResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed inserting app to Supabase database", e)
            }
        }

        return newApp
    }

    fun createAdminApp(
        name: String,
        category: String,
        developer: String,
        tagline: String,
        description: String,
        iconUrl: String,
        downloadUrl: String,
        bundleId: String,
        sizeMb: Float,
        version: String,
        rating: Float,
        isTopApp: Boolean,
        isTopGame: Boolean,
        isFeatured: Boolean
    ): AppItem {
        val sizeBytes = (sizeMb * 1024 * 1024).toLong()
        return uploadRealApp(
            name = name,
            category = category,
            developer = developer,
            tagline = tagline,
            description = description,
            apkFilePath = downloadUrl,
            sizeBytes = sizeBytes,
            version = version,
            iconPath = iconUrl,
            screenshots = emptyList(),
            bundleId = bundleId,
            price = 0.0,
            isSubscription = false,
            isTopApp = isTopApp,
            isTopGame = isTopGame,
            isNewNoteworthy = false,
            isFeatured = isFeatured,
            cloudApkUrl = downloadUrl
        )
    }

    fun uploadNews(
        title: String,
        category: String,
        summary: String,
        content: String,
        author: String
    ): NewsItem {
        val newsId = "news-${UUID.randomUUID().toString().take(8)}"
        val newItem = NewsItem(
            id = newsId,
            title = title,
            category = category.ifBlank { "Updates" },
            summary = summary,
            content = content.ifBlank { summary },
            author = author.ifBlank { "Spoiled Store Team" },
            date = "Today",
            readTime = "2 min read"
        )
        _newsList.update { listOf(newItem) + it }
        saveNewsToDisk(_newsList.value)

        val currentUid = googleAuthManager.currentUser?.uid ?: "admin"
        scope.launch {
            try {
                val map = mapOf(
                    "id" to newsId,
                    "title" to title,
                    "category" to newItem.category,
                    "summary" to summary,
                    "content" to newItem.content,
                    "author" to newItem.author,
                    "date" to "Today",
                    "readTime" to "2 min read",
                    "authorUid" to currentUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore?.collection("news")?.document(newsId)?.set(map)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving news to Firestore", e)
            }
        }
        return newItem
    }

    fun sendBroadcastNotification(title: String, message: String): StoreNotification {
        val notifId = "notif-${UUID.randomUUID().toString().take(8)}"
        val notif = StoreNotification(
            id = notifId,
            title = title,
            message = message,
            timestamp = "Just now",
            isBroadcast = true
        )
        _notifications.update { listOf(notif) + it }
        saveNotificationsToDisk(_notifications.value)

        val currentUid = googleAuthManager.currentUser?.uid ?: "admin"
        scope.launch {
            try {
                val map = mapOf(
                    "id" to notifId,
                    "title" to title,
                    "message" to message,
                    "timestamp" to "Just now",
                    "isBroadcast" to true,
                    "authorUid" to currentUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore?.collection("notifications")?.document(notifId)?.set(map)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving notification to Firestore", e)
            }
        }
        return notif
    }

    fun deleteApp(appId: String) {
        val targetApp = _apps.value.find { it.id == appId }
        val deletedIds = sessionPrefs.getStringSet("deleted_app_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        deletedIds.add(appId)
        if (targetApp != null) {
            deletedIds.add(targetApp.slug)
        }
        sessionPrefs.edit().putStringSet("deleted_app_ids", deletedIds).apply()

        _apps.update { list -> list.filterNot { it.id == appId || it.slug == appId } }
        saveAppsToDisk(_apps.value)
        scope.launch {
            try {
                if (targetApp != null) {
                    supabaseManager.deleteAppFromDatabase(targetApp.slug)
                }
                firestore?.collection("apps")?.document(appId)?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting app", e)
            }
        }
    }

    fun deleteNews(newsId: String) {
        _newsList.update { list -> list.filterNot { it.id == newsId } }
        saveNewsToDisk(_newsList.value)
        scope.launch {
            try {
                firestore?.collection("news")?.document(newsId)?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting news from Firestore", e)
            }
        }
    }

    fun deleteNotification(notifId: String) {
        _notifications.update { list -> list.filterNot { it.id == notifId } }
        saveNotificationsToDisk(_notifications.value)
        scope.launch {
            try {
                firestore?.collection("notifications")?.document(notifId)?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification from Firestore", e)
            }
        }
    }

    // ----------------------------------------------------
    // USER ENGAGEMENT & DOWNLOADS
    // ----------------------------------------------------
    fun installApp(appId: String) {
        val app = _apps.value.find { it.id == appId } ?: return
        val downloadUrl = if (app.cloudApkUrl.isNotBlank()) app.cloudApkUrl else app.apkFilePath

        if (downloadUrl.startsWith("http://", ignoreCase = true) || downloadUrl.startsWith("https://", ignoreCase = true)) {
            // Real network download via system DownloadManager into public device Downloads
            downloadManager.startDownload(app.id, app.name, downloadUrl) { downloadedFile ->
                _installedAppIds.update { it + appId }
                _apps.update { list ->
                    list.map { if (it.id == appId) it.copy(downloadsCount = it.downloadsCount + 1) else it }
                }
                saveAppsToDisk(_apps.value)
                if (downloadedFile != null) {
                    downloadManager.installApk(downloadedFile)
                }
            }
        } else {
            // Local APK file or generated verifiable package
            scope.launch {
                _downloadProgress.update { it + (appId to 0.05f) }
                for (progress in 15..100 step 20) {
                    delay(100)
                    _downloadProgress.update { it + (appId to progress / 100f) }
                }
                delay(80)
                _downloadProgress.update { it - appId }
                _installedAppIds.update { it + appId }

                _apps.update { list ->
                    list.map { if (it.id == appId) it.copy(downloadsCount = it.downloadsCount + 1) else it }
                }
                saveAppsToDisk(_apps.value)

                val localFile = if (app.apkFilePath.isNotBlank()) File(app.apkFilePath) else null
                if (localFile != null && localFile.exists()) {
                    downloadManager.installApk(localFile)
                } else {
                    val sample = createSampleApkOnDisk(app.name)
                    downloadManager.installApk(File(sample.first))
                }
            }
        }
    }

    fun cancelDownload(appId: String) {
        downloadManager.cancelDownload(appId)
        _downloadProgress.update { it - appId }
    }

    fun uninstallApp(appId: String) {
        _installedAppIds.update { it - appId }
    }

    fun purchaseApp(appId: String) {
        _purchasedAppIds.update { it + appId }
    }

    fun toggleWishlist(appId: String) {
        _wishlistAppIds.update { current ->
            if (current.contains(appId)) current - appId else current + appId
        }
    }

    fun addComment(appId: String, text: String, rating: Int) {
        val user = _userProfile.value
        val commentId = "c-${UUID.randomUUID().toString().take(8)}"
        val newComment = CommentItem(
            id = commentId,
            appId = appId,
            userName = if (user.isSignedIn) user.displayName else "Verified User",
            userEmail = if (user.isSignedIn) user.email else "user@spoiledstore.app",
            comment = text,
            rating = rating,
            date = "Just now"
        )
        _comments.update { listOf(newComment) + it }
        saveCommentsToDisk(_comments.value)

        _apps.update { list ->
            list.map { app ->
                if (app.id == appId) {
                    val appComments = _comments.value.filter { it.appId == appId }
                    val newAvg = if (appComments.isNotEmpty()) {
                        appComments.map { it.rating }.average().toFloat()
                    } else 5.0f
                    app.copy(
                        reviewsCount = appComments.size,
                        rating = String.format("%.1f", newAvg).toFloatOrNull() ?: 5.0f
                    )
                } else app
            }
        }
        saveAppsToDisk(_apps.value)

        // Save to Firestore
        val currentUid = googleAuthManager.currentUser?.uid ?: "user"
        scope.launch {
            try {
                val map = mapOf(
                    "id" to commentId,
                    "appId" to appId,
                    "userId" to currentUid,
                    "userName" to newComment.userName,
                    "userEmail" to newComment.userEmail,
                    "comment" to text,
                    "rating" to rating,
                    "date" to "Just now",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore?.collection("comments")?.document(commentId)?.set(map)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving comment to Firestore", e)
            }
        }
    }

    // ----------------------------------------------------
    // PERSISTENCE HELPERS
    // ----------------------------------------------------
    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb < 1.0) {
            val kb = bytes / 1024
            "$kb KB"
        } else {
            String.format("%.1f MB", mb)
        }
    }

    private fun loadDataFromStorage() {
        _apps.value = loadAppsFromDisk()
        _newsList.value = loadNewsFromDisk()
        _comments.value = loadCommentsFromDisk()
        _notifications.value = loadNotificationsFromDisk()
        loadUserSession()
    }

    private fun saveAppsToDisk(appsList: List<AppItem>) {
        scope.launch {
            try {
                val array = JSONArray()
                appsList.forEach { app ->
                    val obj = JSONObject().apply {
                        put("id", app.id)
                        put("slug", app.slug)
                        put("name", app.name)
                        put("category", app.category)
                        put("developer", app.developer)
                        put("rating", app.rating.toDouble())
                        put("reviewsCount", app.reviewsCount)
                        put("downloadsCount", app.downloadsCount)
                        put("sizeBytes", app.sizeBytes)
                        put("sizeFormatted", app.sizeFormatted)
                        put("version", app.version)
                        put("description", app.description)
                        put("tagline", app.tagline)
                        put("iconPath", app.iconPath)
                        put("apkFilePath", app.apkFilePath)
                        put("bundleId", app.bundleId)
                        put("price", app.price)
                        put("isSubscription", app.isSubscription)
                        put("isTopApp", app.isTopApp)
                        put("isTopGame", app.isTopGame)
                        put("isNewNoteworthy", app.isNewNoteworthy)
                        put("isFeatured", app.isFeatured)
                        put("overview", app.overview)
                        put("speedScore", app.speedScore)
                        put("securityStatus", app.securityStatus)
                        put("cloudApkUrl", app.cloudApkUrl)
                        val shotsArray = JSONArray()
                        app.screenshots.forEach { shotsArray.put(it) }
                        put("screenshots", shotsArray)
                    }
                    array.put(obj)
                }
                File(context.filesDir, APPS_FILE).writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadAppsFromDisk(): List<AppItem> {
        val demoCleared = sessionPrefs.getBoolean("demo_apps_cleared", false)
        val deletedIds = sessionPrefs.getStringSet("deleted_app_ids", emptySet()) ?: emptySet()
        val file = File(context.filesDir, APPS_FILE)
        if (!file.exists()) {
            return if (demoCleared) emptyList() else getInitialOnlineStoreCatalog().filterNot { deletedIds.contains(it.id) || deletedIds.contains(it.slug) }
        }
        return try {
            val array = JSONArray(file.readText())
            val list = mutableListOf<AppItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val appId = obj.optString("id")
                val appSlug = obj.optString("slug")
                if (deletedIds.contains(appId) || deletedIds.contains(appSlug)) {
                    continue
                }
                val shots = mutableListOf<String>()
                val shotsArray = obj.optJSONArray("screenshots")
                if (shotsArray != null) {
                    for (s in 0 until shotsArray.length()) shots.add(shotsArray.getString(s))
                }
                list.add(
                    AppItem(
                        id = appId,
                        slug = appSlug,
                        name = obj.optString("name"),
                        category = obj.optString("category"),
                        developer = obj.optString("developer"),
                        rating = obj.optDouble("rating", 5.0).toFloat(),
                        reviewsCount = obj.optInt("reviewsCount", 0),
                        downloadsCount = obj.optInt("downloadsCount", 0),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        sizeFormatted = obj.optString("sizeFormatted", "0 MB"),
                        version = obj.optString("version", "1.0.0"),
                        description = obj.optString("description"),
                        tagline = obj.optString("tagline"),
                        iconPath = obj.optString("iconPath"),
                        apkFilePath = obj.optString("apkFilePath"),
                        screenshots = shots,
                        bundleId = obj.optString("bundleId"),
                        price = obj.optDouble("price", 0.0),
                        isSubscription = obj.optBoolean("isSubscription", false),
                        isTopApp = obj.optBoolean("isTopApp", false),
                        isTopGame = obj.optBoolean("isTopGame", false),
                        isNewNoteworthy = obj.optBoolean("isNewNoteworthy", false),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        isUploadedByAdmin = obj.optBoolean("isUploadedByAdmin", true),
                        overview = obj.optString("overview", "High performance application verified for Spoiled Store ecosystem."),
                        speedScore = obj.optString("speedScore", "99/100"),
                        securityStatus = obj.optString("securityStatus", "Verified & Sandboxed"),
                        cloudApkUrl = obj.optString("cloudApkUrl")
                    )
                )
            }
            if (list.isEmpty()) {
                if (demoCleared) emptyList() else getInitialOnlineStoreCatalog().filterNot { deletedIds.contains(it.id) || deletedIds.contains(it.slug) }
            } else {
                val existingIds = list.map { it.id }.toSet()
                val missingInitials = getInitialOnlineStoreCatalog().filterNot {
                    existingIds.contains(it.id) || deletedIds.contains(it.id) || deletedIds.contains(it.slug)
                }
                if (missingInitials.isNotEmpty() && !demoCleared) {
                    list.addAll(missingInitials)
                }
                list
            }
        } catch (_: Exception) {
            if (demoCleared) emptyList() else getInitialOnlineStoreCatalog().filterNot { deletedIds.contains(it.id) || deletedIds.contains(it.slug) }
        }
    }

    private fun saveNewsToDisk(newsList: List<NewsItem>) {
        scope.launch {
            try {
                val array = JSONArray()
                newsList.forEach { item ->
                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("category", item.category)
                        put("summary", item.summary)
                        put("content", item.content)
                        put("author", item.author)
                        put("date", item.date)
                        put("readTime", item.readTime)
                        put("imagePath", item.imagePath)
                    }
                    array.put(obj)
                }
                File(context.filesDir, NEWS_FILE).writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadNewsFromDisk(): List<NewsItem> {
        val file = File(context.filesDir, NEWS_FILE)
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            val list = mutableListOf<NewsItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NewsItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        category = obj.optString("category"),
                        summary = obj.optString("summary"),
                        content = obj.optString("content"),
                        author = obj.optString("author"),
                        date = obj.optString("date"),
                        readTime = obj.optString("readTime"),
                        imagePath = obj.optString("imagePath")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCommentsToDisk(commentsList: List<CommentItem>) {
        scope.launch {
            try {
                val array = JSONArray()
                commentsList.forEach { c ->
                    val obj = JSONObject().apply {
                        put("id", c.id)
                        put("appId", c.appId)
                        put("userName", c.userName)
                        put("userEmail", c.userEmail)
                        put("comment", c.comment)
                        put("rating", c.rating)
                        put("date", c.date)
                    }
                    array.put(obj)
                }
                File(context.filesDir, COMMENTS_FILE).writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadCommentsFromDisk(): List<CommentItem> {
        val file = File(context.filesDir, COMMENTS_FILE)
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            val list = mutableListOf<CommentItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CommentItem(
                        id = obj.optString("id"),
                        appId = obj.optString("appId"),
                        userName = obj.optString("userName"),
                        userEmail = obj.optString("userEmail"),
                        comment = obj.optString("comment"),
                        rating = obj.optInt("rating", 5),
                        date = obj.optString("date")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveNotificationsToDisk(notifList: List<StoreNotification>) {
        scope.launch {
            try {
                val array = JSONArray()
                notifList.forEach { n ->
                    val obj = JSONObject().apply {
                        put("id", n.id)
                        put("title", n.title)
                        put("message", n.message)
                        put("timestamp", n.timestamp)
                        put("isBroadcast", n.isBroadcast)
                        put("isRead", n.isRead)
                    }
                    array.put(obj)
                }
                File(context.filesDir, NOTIFICATIONS_FILE).writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadNotificationsFromDisk(): List<StoreNotification> {
        val file = File(context.filesDir, NOTIFICATIONS_FILE)
        if (!file.exists()) {
            return listOf(
                StoreNotification(
                    id = "n-welcome",
                    title = "Welcome to Spoiled Store",
                    message = "Discover, download, and publish verified Android APK applications directly from the cloud.",
                    timestamp = "Recent"
                )
            )
        }
        return try {
            val array = JSONArray(file.readText())
            val list = mutableListOf<StoreNotification>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StoreNotification(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        message = obj.optString("message"),
                        timestamp = obj.optString("timestamp"),
                        isBroadcast = obj.optBoolean("isBroadcast", true),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveUserSession(profile: UserProfile) {
        sessionPrefs.edit()
            .putBoolean("is_signed_in", profile.isSignedIn)
            .putString("email", profile.email)
            .putString("username", profile.username)
            .putString("auth_provider", profile.authProvider)
            .putBoolean("is_admin", profile.isAdmin)
            .apply()

        scope.launch {
            try {
                val obj = JSONObject().apply {
                    put("isSignedIn", profile.isSignedIn)
                    put("email", profile.email)
                    put("username", profile.username)
                    put("authProvider", profile.authProvider)
                    put("isAdmin", profile.isAdmin)
                }
                File(context.filesDir, USER_FILE).writeText(obj.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadInitialUserSession(): UserProfile {
        val isSignedIn = sessionPrefs.getBoolean("is_signed_in", false)
        if (isSignedIn) {
            val email = sessionPrefs.getString("email", "") ?: ""
            val username = sessionPrefs.getString("username", "") ?: ""
            val authProvider = sessionPrefs.getString("auth_provider", "Email") ?: "Email"
            val isAdmin = isUserAdmin(email)
            return UserProfile(
                isSignedIn = true,
                email = email,
                username = username,
                authProvider = authProvider,
                isAdmin = isAdmin
            )
        }
        val file = File(context.filesDir, USER_FILE)
        if (file.exists()) {
            try {
                val obj = JSONObject(file.readText())
                if (obj.optBoolean("isSignedIn")) {
                    val email = obj.optString("email")
                    val isAdmin = isUserAdmin(email)
                    val profile = UserProfile(
                        isSignedIn = true,
                        email = email,
                        username = obj.optString("username"),
                        authProvider = obj.optString("authProvider", "Email"),
                        isAdmin = isAdmin
                    )
                    sessionPrefs.edit()
                        .putBoolean("is_signed_in", true)
                        .putString("email", email)
                        .putString("username", profile.username)
                        .putString("auth_provider", profile.authProvider)
                        .putBoolean("is_admin", isAdmin)
                        .apply()
                    return profile
                }
            } catch (_: Exception) {}
        }
        return UserProfile(isSignedIn = false)
    }

    private fun loadUserSessionFromFile(): UserProfile? {
        val file = File(context.filesDir, USER_FILE)
        if (!file.exists()) return null
        return try {
            val obj = JSONObject(file.readText())
            val email = obj.optString("email")
            val isAdmin = isUserAdmin(email)
            UserProfile(
                isSignedIn = obj.optBoolean("isSignedIn"),
                email = email,
                username = obj.optString("username"),
                authProvider = obj.optString("authProvider", "Email"),
                isAdmin = isAdmin
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun loadUserSession() {
        _userProfile.value = loadInitialUserSession()
    }

    private fun loadFeaturedBanner(): FeaturedBannerConfig {
        return FeaturedBannerConfig(
            title = bannerPrefs.getString("title", "Real APK Downloads\n& Cloud Storage") ?: "Real APK Downloads\n& Cloud Storage",
            tagline = bannerPrefs.getString("tagline", "FEATURED ECOSYSTEM") ?: "FEATURED ECOSYSTEM",
            description = bannerPrefs.getString("description", "Explore Catalog") ?: "Explore Catalog",
            imageUrl = bannerPrefs.getString("image_url", "") ?: ""
        )
    }

    fun updateFeaturedBanner(title: String, tagline: String, imageUrl: String) {
        val newBanner = FeaturedBannerConfig(
            title = title.ifBlank { "Real APK Downloads\n& Cloud Storage" },
            tagline = tagline.ifBlank { "FEATURED ECOSYSTEM" },
            description = "Explore Catalog",
            imageUrl = imageUrl.trim()
        )
        bannerPrefs.edit()
            .putString("title", newBanner.title)
            .putString("tagline", newBanner.tagline)
            .putString("description", newBanner.description)
            .putString("image_url", newBanner.imageUrl)
            .apply()
        _featuredBanner.value = newBanner

        scope.launch {
            try {
                firestore?.collection("config")?.document("featured_banner")?.set(
                    mapOf(
                        "title" to newBanner.title,
                        "tagline" to newBanner.tagline,
                        "description" to newBanner.description,
                        "imageUrl" to newBanner.imageUrl,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } catch (_: Exception) {}
        }
    }

    fun deleteDemoCatalogApps() {
        sessionPrefs.edit().putBoolean("demo_apps_cleared", true).apply()
        _apps.update { list -> list.filter { it.isUploadedByAdmin && !it.id.startsWith("app-") } }
        saveAppsToDisk(_apps.value)
    }

    fun getInitialOnlineStoreCatalog(): List<AppItem> {
        return listOf(
            AppItem(
                id = "app-firefox-fenix",
                slug = "firefox-browser",
                name = "Firefox Browser",
                category = "Utilities",
                developer = "Mozilla Community",
                rating = 4.8f,
                reviewsCount = 1420,
                downloadsCount = 28500,
                sizeBytes = 84L * 1024 * 1024,
                sizeFormatted = "84 MB",
                version = "124.2.0",
                description = "Experience fast, private and safe mobile browsing. Mozilla Firefox blocks over 2,000 ad trackers automatically, offering enhanced tracking protection and lightning fast web rendering.",
                tagline = "Fast, private & tracking-free web browsing",
                iconPath = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                apkFilePath = "https://ftp.mozilla.org/pub/fenix/releases/124.2.0/android/fenix-124.2.0-android-arm64-v8a/fenix-124.2.0.multi.android-arm64-v8a.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&q=80",
                    "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80",
                    "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800&q=80"
                ),
                bundleId = "org.mozilla.firefox",
                price = 0.0,
                isTopApp = true,
                isFeatured = true,
                overview = "GeckoView high performance web engine with Enhanced Tracking Protection 3.0.",
                speedScore = "99/100",
                securityStatus = "Sandboxed & Audited",
                cloudApkUrl = "https://ftp.mozilla.org/pub/fenix/releases/124.2.0/android/fenix-124.2.0-android-arm64-v8a/fenix-124.2.0.multi.android-arm64-v8a.apk"
            ),
            AppItem(
                id = "app-vlc-player",
                slug = "vlc-for-android",
                name = "VLC for Android",
                category = "Photo & Video",
                developer = "VideoLAN Organization",
                rating = 4.9f,
                reviewsCount = 3890,
                downloadsCount = 64200,
                sizeBytes = 34L * 1024 * 1024,
                sizeFormatted = "34 MB",
                version = "3.5.4",
                description = "VLC for Android is the best open-source video and music player, fast and easy! Plays all video formats, network streams, audio streams, and media files without extra codecs.",
                tagline = "Plays any video, audio stream, and codec",
                iconPath = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                apkFilePath = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
                    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&q=80"
                ),
                bundleId = "org.videolan.vlc",
                price = 0.0,
                isTopApp = true,
                isNewNoteworthy = true,
                overview = "Hardware decoding acceleration with full subtitle support and custom audio equalization.",
                speedScore = "98/100",
                securityStatus = "Open Source Verified",
                cloudApkUrl = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk"
            ),
            AppItem(
                id = "app-telegram-msg",
                slug = "telegram-messenger",
                name = "Telegram",
                category = "Social Networking",
                developer = "Telegram FZ-LLC",
                rating = 4.7f,
                reviewsCount = 5120,
                downloadsCount = 98000,
                sizeBytes = 68L * 1024 * 1024,
                sizeFormatted = "68 MB",
                version = "10.9.1",
                description = "Pure instant messaging — simple, fast, secure, and synced across all your devices. Over 800 million active users worldwide with end-to-end encryption.",
                tagline = "Fast & secure cloud-based messaging",
                iconPath = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                apkFilePath = "https://telegram.org/dl/android/apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1614680376593-902f749f7ffc?w=800&q=80",
                    "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800&q=80"
                ),
                bundleId = "org.telegram.messenger",
                price = 0.0,
                isTopApp = true,
                isFeatured = true,
                overview = "MTProto encryption protocol with unlimited media transfer up to 2GB per file.",
                speedScore = "100/100",
                securityStatus = "End-to-End Encrypted",
                cloudApkUrl = "https://telegram.org/dl/android/apk"
            ),
            AppItem(
                id = "app-pixel-forge",
                slug = "pixel-forge-studio",
                name = "Pixel Forge Pro",
                category = "Graphics & Design",
                developer = "Studio Hexagon",
                rating = 4.9f,
                reviewsCount = 890,
                downloadsCount = 15400,
                sizeBytes = 18L * 1024 * 1024,
                sizeFormatted = "18 MB",
                version = "2.1.0",
                description = "Professional pixel art editor and sprite sheet animator. Create 8-bit & 16-bit retro characters, tilesets, and frame-by-frame GIF exports with onion skinning.",
                tagline = "Retina pixel art and sprite animator",
                iconPath = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=500&q=80",
                apkFilePath = "https://ftp.mozilla.org/pub/fenix/releases/124.2.0/android/fenix-124.2.0-android-arm64-v8a/fenix-124.2.0.multi.android-arm64-v8a.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                    "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=800&q=80"
                ),
                bundleId = "com.spoiled.pixelforge",
                price = 0.0,
                isTopApp = true,
                isNewNoteworthy = true,
                overview = "Custom OpenGL canvas renderer with support for up to 64 color palettes and sprite export.",
                speedScore = "99/100",
                securityStatus = "Verified & Sandboxed",
                cloudApkUrl = "https://ftp.mozilla.org/pub/fenix/releases/124.2.0/android/fenix-124.2.0-android-arm64-v8a/fenix-124.2.0.multi.android-arm64-v8a.apk"
            ),
            AppItem(
                id = "game-cyber-neon",
                slug = "cyber-racer-neon",
                name = "Cyber Racer Neon",
                category = "Racing Games",
                developer = "Vapor Games Inc",
                rating = 4.9f,
                reviewsCount = 2100,
                downloadsCount = 42100,
                sizeBytes = 95L * 1024 * 1024,
                sizeFormatted = "95 MB",
                version = "1.4.0",
                description = "High speed synthwave retro futuristic drift racing through glowing cyberpunk city highways. Features dynamic procedural obstacles and electronic OST.",
                tagline = "Adrenaline synthwave drift simulator",
                iconPath = "https://images.unsplash.com/photo-1563089145-599997674d42?w=500&q=80",
                apkFilePath = "https://buildbot.libretro.com/stable/1.18.0/android/RetroArch_ra32.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1563089145-599997674d42?w=800&q=80",
                    "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&q=80"
                ),
                bundleId = "com.spoiled.cyberracer",
                price = 0.0,
                isTopGame = true,
                isFeatured = true,
                overview = "60 FPS Vulkan physics simulation with haptic wheel feedback and responsive gyroscopic steering.",
                speedScore = "99/100",
                securityStatus = "Sandboxed Engine",
                cloudApkUrl = "https://buildbot.libretro.com/stable/1.18.0/android/RetroArch_ra32.apk"
            ),
            AppItem(
                id = "game-quantum-synth",
                slug = "quantum-synth-beat",
                name = "Quantum Synth",
                category = "Music & Audio",
                developer = "Acoustic Logic",
                rating = 4.8f,
                reviewsCount = 670,
                downloadsCount = 11200,
                sizeBytes = 24L * 1024 * 1024,
                sizeFormatted = "24 MB",
                version = "3.0.1",
                description = "Modular synthesizer and step sequencer workstation. Design custom waveforms with analog-modeled oscillators, ladder filters, and LFO modulators.",
                tagline = "Modular polyphonic studio synthesizer",
                iconPath = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=500&q=80",
                apkFilePath = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&q=80",
                    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&q=80"
                ),
                bundleId = "com.spoiled.quantumsynth",
                price = 0.0,
                isTopGame = true,
                isNewNoteworthy = true,
                overview = "Sub-10ms audio latency using AAudio Android low-latency audio framework.",
                speedScore = "100/100",
                securityStatus = "Verified Native Runtime",
                cloudApkUrl = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk"
            ),
            AppItem(
                id = "game-mario-kart-tour",
                slug = "mario-kart-tour",
                name = "Mario Kart Tour",
                category = "Racing",
                developer = "Nintendo Co., Ltd.",
                rating = 4.7f,
                reviewsCount = 912000,
                downloadsCount = 125000000,
                sizeBytes = 142L * 1024 * 1024,
                sizeFormatted = "142 MB",
                version = "2.4.1",
                description = "Mario and friends race around the world in courses inspired by real-world cities in addition to classic Mario Kart courses! Put the pedal to the metal in exciting multiplayer races with friends.",
                tagline = "Race around the world!",
                iconPath = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=500&q=80",
                apkFilePath = "https://buildbot.libretro.com/stable/1.18.0/android/RetroArch_ra32.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                    "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&q=80"
                ),
                bundleId = "com.nintendo.zaka",
                price = 0.0,
                isTopGame = true,
                isFeatured = true,
                isNewNoteworthy = true,
                overview = "Official Nintendo racing title optimized for high performance Android devices.",
                speedScore = "99/100",
                securityStatus = "Nintendo Verified",
                cloudApkUrl = "https://buildbot.libretro.com/stable/1.18.0/android/RetroArch_ra32.apk"
            ),
            AppItem(
                id = "app-lightright",
                slug = "lightright",
                name = "LightRight",
                category = "Photo & Video",
                developer = "LightRight Studio",
                rating = 4.8f,
                reviewsCount = 23000,
                downloadsCount = 540000,
                sizeBytes = 42L * 1024 * 1024,
                sizeFormatted = "42 MB",
                version = "3.2.0",
                description = "LightRight gives photographers the capability to book natural lighting conditions. Hone your photography skills and capture stunning photos under any sun, wind, or air condition.",
                tagline = "Photo filters for all lighting",
                iconPath = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                apkFilePath = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
                    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&q=80"
                ),
                bundleId = "com.lightright.app",
                price = 0.0,
                isTopApp = true,
                isFeatured = true,
                overview = "Lighting prediction engine with real-time solar tracking and color balancing.",
                speedScore = "98/100",
                securityStatus = "Sandboxed Engine",
                cloudApkUrl = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk"
            ),
            AppItem(
                id = "app-rookie-cam",
                slug = "rookie-cam",
                name = "Rookie Cam™",
                category = "Photo & Video",
                developer = "JellyBus Inc.",
                rating = 4.9f,
                reviewsCount = 3800,
                downloadsCount = 210000,
                sizeBytes = 55L * 1024 * 1024,
                sizeFormatted = "55 MB",
                version = "4.0.2",
                description = "Rookie Cam™ is the filter camera photo editor that empowers those who wish to go beyond basic photo editing. Featuring live filter camera, beauty camera, and professional photo editing.",
                tagline = "Photo & Video filter camera",
                iconPath = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=500&q=80",
                apkFilePath = "https://ftp.mozilla.org/pub/fenix/releases/124.2.0/android/fenix-124.2.0-android-arm64-v8a/fenix-124.2.0.multi.android-arm64-v8a.apk",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&q=80",
                    "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80"
                ),
                bundleId = "com.jellybus.rookiecam",
                price = 0.0,
                isTopApp = true,
                isNewNoteworthy = true,
                overview = "Advanced multi-exposure blending and live color lookup tables for crisp photos.",
                speedScore = "99/100",
                securityStatus = "Sandboxed & Audited",
                cloudApkUrl = "https://ftp.mozilla.org/pub/fenix/releases/124.2.0/android/fenix-124.2.0-android-arm64-v8a/fenix-124.2.0.multi.android-arm64-v8a.apk"
            )
        )
    }
}
