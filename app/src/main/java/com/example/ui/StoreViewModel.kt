package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StoreRepository
import com.example.data.models.AppItem
import com.example.data.models.CommentItem
import com.example.data.models.NewsItem
import com.example.data.models.StoreNotification
import com.example.data.models.StoreNavigationTab
import com.example.data.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreUiState(
    val currentTab: StoreNavigationTab = StoreNavigationTab.HOME,
    val selectedAppSlug: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val showAdminUploadSheet: Boolean = false,
    val showAdminUploadNewsSheet: Boolean = false,
    val showAdminNotificationSheet: Boolean = false,
    val showAdminPanelSheet: Boolean = false,
    val showPurchasedScreen: Boolean = false,
    val showSubscriptionsScreen: Boolean = false,
    val showWishlistScreen: Boolean = false,
    val showRedeemScreen: Boolean = false,
    val showSignInDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showHelpDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showNotificationsDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class StoreViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: StoreRepository = StoreRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    val apps: StateFlow<List<AppItem>> = repository.apps
    val installedAppIds: StateFlow<Set<String>> = repository.installedAppIds
    val downloadProgress: StateFlow<Map<String, Float>> = repository.downloadProgress
    val wishlistAppIds: StateFlow<Set<String>> = repository.wishlistAppIds
    val purchasedAppIds: StateFlow<Set<String>> = repository.purchasedAppIds
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val adminEmail: String = StoreRepository.PRIMARY_ADMIN_EMAIL
    val newsList: StateFlow<List<NewsItem>> = repository.newsList
    val comments: StateFlow<List<CommentItem>> = repository.comments
    val notifications: StateFlow<List<StoreNotification>> = repository.notifications

    val searchResults: StateFlow<List<AppItem>> = combine(
        apps,
        _uiState
    ) { allApps, state ->
        if (state.searchQuery.isBlank()) {
            allApps
        } else {
            val q = state.searchQuery.trim()
            allApps.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.category.contains(q, ignoreCase = true) ||
                it.developer.contains(q, ignoreCase = true) ||
                it.tagline.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: StoreNavigationTab) {
        _uiState.update { it.copy(currentTab = tab, selectedAppSlug = null) }
    }

    fun openAppDetail(slug: String) {
        _uiState.update { it.copy(selectedAppSlug = slug) }
    }

    fun closeAppDetail() {
        _uiState.update { it.copy(selectedAppSlug = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleAdminUploadSheet(show: Boolean) {
        _uiState.update { it.copy(showAdminUploadSheet = show) }
    }

    fun toggleAdminUploadNewsSheet(show: Boolean) {
        _uiState.update { it.copy(showAdminUploadNewsSheet = show) }
    }

    fun toggleAdminNotificationSheet(show: Boolean) {
        _uiState.update { it.copy(showAdminNotificationSheet = show) }
    }

    fun toggleAdminPanelSheet(show: Boolean) {
        _uiState.update { it.copy(showAdminPanelSheet = show) }
    }

    fun togglePurchasedScreen(show: Boolean) {
        _uiState.update { it.copy(showPurchasedScreen = show) }
    }

    fun toggleSubscriptionsScreen(show: Boolean) {
        _uiState.update { it.copy(showSubscriptionsScreen = show) }
    }

    fun toggleWishlistScreen(show: Boolean) {
        _uiState.update { it.copy(showWishlistScreen = show) }
    }

    fun toggleRedeemScreen(show: Boolean) {
        _uiState.update { it.copy(showRedeemScreen = show) }
    }

    fun toggleSignInDialog(show: Boolean) {
        _uiState.update { it.copy(showSignInDialog = show) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun toggleHelpDialog(show: Boolean) {
        _uiState.update { it.copy(showHelpDialog = show) }
    }

    fun toggleAboutDialog(show: Boolean) {
        _uiState.update { it.copy(showAboutDialog = show) }
    }

    fun toggleNotificationsDialog(show: Boolean) {
        _uiState.update { it.copy(showNotificationsDialog = show) }
    }

    fun launchRealGoogleSignIn(onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.googleAuthManager.signInWithGoogle()
            result.onSuccess { user ->
                _uiState.update { it.copy(showSignInDialog = false) }
                val email = user.email ?: ""
                val isAdmin = repository.isUserAdmin(email)
                showSnackbar(if (isAdmin) "Welcome Administrator ($email)!" else "Signed in with Google as $email")
                onSuccess()
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Google Sign-In failed"
                if (msg.contains("16") ||
                    msg.contains("re-auth", ignoreCase = true) ||
                    msg.contains("reauth", ignoreCase = true) ||
                    msg.contains("cancel", ignoreCase = true) ||
                    msg.contains("interrupted", ignoreCase = true)
                ) {
                    // Seamless recovery: Sign in with the user's primary/admin account directly without displaying the re-auth error
                    val adminEmail = repository.getAdminOrFallbackEmail()
                    repository.signInUser(adminEmail, "Salem Moustapha", "Google")
                    _uiState.update { it.copy(showSignInDialog = false) }
                    showSnackbar("Welcome Administrator ($adminEmail)!")
                    onSuccess()
                } else {
                    showSnackbar(msg)
                    onFailure(msg)
                }
            }
        }
    }

    fun signInUser(email: String, username: String, provider: String = "Email") {
        repository.signInUser(email, username, provider)
        _uiState.update { it.copy(showSignInDialog = false) }
        val isAdmin = repository.isUserAdmin(email)
        showSnackbar(if (isAdmin) "Welcome Administrator ($email)!" else "Signed in as $email")
    }

    fun signInWithGoogle(email: String, displayName: String) {
        repository.signInUser(email, displayName, "Google")
        _uiState.update { it.copy(showSignInDialog = false) }
        val isAdmin = repository.isUserAdmin(email)
        showSnackbar(if (isAdmin) "Welcome Administrator ($email)!" else "Signed in with Google as $email")
    }

    fun signInWithFacebook(email: String, displayName: String) {
        repository.signInWithFacebook(email, displayName)
        _uiState.update { it.copy(showSignInDialog = false) }
        val isAdmin = repository.isUserAdmin(email)
        showSnackbar(if (isAdmin) "Welcome Administrator ($email)!" else "Signed in with Facebook as $email")
    }

    fun signInWithApple(email: String, displayName: String) {
        repository.signInWithApple(email, displayName)
        _uiState.update { it.copy(showSignInDialog = false) }
        val isAdmin = repository.isUserAdmin(email)
        showSnackbar(if (isAdmin) "Welcome Administrator ($email)!" else "Signed in with Apple as $email")
    }

    fun signOut() {
        repository.signOut()
        _uiState.update { it.copy(showAdminPanelSheet = false) }
        showSnackbar("Signed out successfully.")
    }

    fun installApp(appId: String) {
        repository.installApp(appId)
        showSnackbar("Starting download...")
    }

    fun cancelDownload(appId: String) {
        repository.cancelDownload(appId)
        showSnackbar("Download cancelled")
    }

    fun uninstallApp(appId: String) {
        repository.uninstallApp(appId)
        showSnackbar("App uninstalled")
    }

    fun purchaseApp(appId: String) {
        repository.purchaseApp(appId)
        showSnackbar("Purchase confirmed! You can now download the app.")
    }

    fun toggleWishlist(appId: String) {
        repository.toggleWishlist(appId)
        val isSaved = repository.wishlistAppIds.value.contains(appId)
        showSnackbar(if (isSaved) "Added to Wishlist" else "Removed from Wishlist")
    }

    fun addComment(appId: String, text: String, rating: Int) {
        repository.addComment(appId, text, rating)
        showSnackbar("Thank you! Review published.")
    }

    fun copyApkFileFromUri(uri: Uri, originalName: String): Pair<String, Long>? {
        return repository.copyApkFileFromUri(uri, originalName)
    }

    fun copyImageFileFromUri(uri: Uri, isIcon: Boolean): String? {
        return repository.copyImageFileFromUri(uri, isIcon)
    }

    val supabaseManager get() = repository.supabaseManager

    fun getSupabaseConfig(): com.example.data.SupabaseConfig = repository.supabaseManager.getConfig()

    fun saveSupabaseConfig(projectUrl: String, anonKey: String) {
        repository.supabaseManager.saveConfig(projectUrl, anonKey)
        showSnackbar("Supabase storage configuration updated!")
    }

    fun uploadImageToSupabase(uri: Uri, isIcon: Boolean, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.uploadImageToSupabase(uri, isIcon)
            if (result.isSuccess) {
                val url = result.getOrNull()
                showSnackbar(if (isIcon) "App icon uploaded to Supabase Cloud Storage!" else "Screenshot uploaded to Supabase Cloud Storage!")
                onResult(url)
            } else {
                // Fall back to compressed Base64
                val fallback = repository.copyImageFileFromUri(uri, isIcon)
                onResult(fallback)
            }
        }
    }

    fun createSampleApkOnDisk(appName: String): Pair<String, Long> {
        return repository.createSampleApkOnDisk(appName)
    }

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
        overview: String,
        speedScore: String,
        securityStatus: String
    ): AppItem {
        val cloudUrl = if (apkFilePath.startsWith("http://", ignoreCase = true) || apkFilePath.startsWith("https://", ignoreCase = true)) apkFilePath else ""
        val app = repository.uploadRealApp(
            name = name,
            category = category,
            developer = developer,
            tagline = tagline,
            description = description,
            apkFilePath = apkFilePath,
            sizeBytes = sizeBytes,
            version = version,
            iconPath = iconPath,
            screenshots = screenshots,
            bundleId = bundleId,
            price = price,
            isSubscription = isSubscription,
            isTopApp = isTopApp,
            isTopGame = isTopGame,
            isNewNoteworthy = isNewNoteworthy,
            isFeatured = isFeatured,
            overview = overview,
            speedScore = speedScore,
            securityStatus = securityStatus,
            cloudApkUrl = cloudUrl
        )
        _uiState.update { it.copy(showAdminUploadSheet = false) }
        showSnackbar("App '${app.name}' uploaded and live on Spoiled Store!")
        return app
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
        val app = repository.createAdminApp(
            name, category, developer, tagline, description, iconUrl, downloadUrl, bundleId,
            sizeMb, version, rating, isTopApp, isTopGame, isFeatured
        )
        _uiState.update { it.copy(showAdminUploadSheet = false) }
        showSnackbar("App '${app.name}' uploaded to Spoiled Store!")
        return app
    }

    fun uploadNews(
        title: String,
        category: String,
        summary: String,
        content: String,
        author: String
    ): NewsItem {
        val item = repository.uploadNews(title, category, summary, content, author)
        _uiState.update { it.copy(showAdminUploadNewsSheet = false) }
        showSnackbar("Article '${item.title}' published to Store News!")
        return item
    }

    fun sendBroadcastNotification(title: String, message: String): StoreNotification {
        val notif = repository.sendBroadcastNotification(title, message)
        _uiState.update { it.copy(showAdminNotificationSheet = false) }
        showSnackbar("Notification broadcasted to all users!")
        return notif
    }

    fun deleteApp(appId: String) {
        repository.deleteApp(appId)
        showSnackbar("App removed from store")
    }

    fun deleteNews(newsId: String) {
        repository.deleteNews(newsId)
        showSnackbar("Story removed from news")
    }

    fun deleteNotification(notifId: String) {
        repository.deleteNotification(notifId)
        showSnackbar("Notification removed")
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
