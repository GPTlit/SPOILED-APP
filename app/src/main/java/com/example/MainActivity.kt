package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.StoreNavigationTab
import com.example.ui.StoreViewModel
import com.example.ui.components.AboutSpoiledStoreDialog
import com.example.ui.components.AdminBroadcastNotificationSheet
import com.example.ui.components.AdminUploadAppSheet
import com.example.ui.components.AdminUploadNewsSheet
import com.example.ui.components.HelpAndSupportDialog
import com.example.ui.components.NotificationsCenterDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SignInDialog
import com.example.ui.components.AppStoreBottomBar
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.ArcadeScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PurchasedScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.screens.RedeemGiftCardScreen
import com.example.ui.theme.SpoiledStoreTheme
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextTertiary

class MainActivity : ComponentActivity() {
    private val viewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SpoiledStoreTheme(
                isHighContrastLight = uiState.isHighContrastLightMode,
                themePreference = uiState.themePreference
            ) {
                SpoiledStoreApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SpoiledStoreApp(
    viewModel: StoreViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allApps by viewModel.apps.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val installedAppIds by viewModel.installedAppIds.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val wishlistAppIds by viewModel.wishlistAppIds.collectAsStateWithLifecycle()
    val purchasedAppIds by viewModel.purchasedAppIds.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val newsList by viewModel.newsList.collectAsStateWithLifecycle()
    val allComments by viewModel.comments.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val featuredBanner by viewModel.featuredBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val selectedApp = uiState.selectedAppSlug?.let { slug ->
        allApps.find { it.slug == slug }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            val isSubPageActive = selectedApp != null ||
                uiState.showAdminPanelSheet ||
                uiState.showPurchasedScreen ||
                uiState.showSubscriptionsScreen ||
                uiState.showWishlistScreen ||
                uiState.showRedeemScreen

            if (!isSubPageActive) {
                AppStoreBottomBar(
                    currentTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                selectedApp != null -> {
                    AppDetailScreen(
                        app = selectedApp,
                        isInstalled = installedAppIds.contains(selectedApp.id),
                        downloadProgress = downloadProgress[selectedApp.id],
                        isWishlisted = wishlistAppIds.contains(selectedApp.id),
                        isPurchased = purchasedAppIds.contains(selectedApp.id),
                        comments = allComments.filter { it.appId == selectedApp.id },
                        onBackClick = { viewModel.closeAppDetail() },
                        onInstallClick = { viewModel.installApp(selectedApp.id) },
                        onUninstallClick = { viewModel.uninstallApp(selectedApp.id) },
                        onWishlistClick = { viewModel.toggleWishlist(selectedApp.id) },
                        onPurchaseClick = { viewModel.purchaseApp(selectedApp.id) },
                        onAddComment = { text, rating -> viewModel.addComment(selectedApp.id, text, rating) }
                    )
                }

                uiState.showAdminPanelSheet -> {
                    AdminPanelScreen(
                        apps = allApps,
                        newsList = newsList,
                        adminEmail = userProfile.email.ifBlank { viewModel.adminEmail },
                        featuredBanner = featuredBanner,
                        onUpdateFeaturedBanner = { title, tag, img -> viewModel.updateFeaturedBanner(title, tag, img) },
                        onDeleteDemoApps = { viewModel.deleteDemoCatalogApps() },
                        onBackClick = { viewModel.toggleAdminPanelSheet(false) },
                        onOpenUploadDialog = { viewModel.toggleAdminUploadSheet(true) },
                        onOpenUploadNewsDialog = { viewModel.toggleAdminUploadNewsSheet(true) },
                        onOpenBroadcastNotificationDialog = { viewModel.toggleAdminNotificationSheet(true) },
                        onDeleteApp = { viewModel.deleteApp(it) },
                        onDeleteNews = { viewModel.deleteNews(it) },
                        onAppClick = { slug ->
                            viewModel.toggleAdminPanelSheet(false)
                            viewModel.openAppDetail(slug)
                        }
                    )
                }

                uiState.showPurchasedScreen -> {
                    val purchasedList = allApps.filter { purchasedAppIds.contains(it.id) }
                    PurchasedScreen(
                        purchasedApps = purchasedList,
                        installedAppIds = installedAppIds,
                        downloadProgress = downloadProgress,
                        onBackClick = { viewModel.togglePurchasedScreen(false) },
                        onAppClick = { slug ->
                            viewModel.togglePurchasedScreen(false)
                            viewModel.openAppDetail(slug)
                        },
                        onInstallClick = { viewModel.installApp(it) },
                        onExploreStoreClick = {
                            viewModel.togglePurchasedScreen(false)
                            viewModel.selectTab(StoreNavigationTab.APPS)
                        }
                    )
                }

                uiState.showSubscriptionsScreen -> {
                    val subscribedList = allApps.filter { it.isSubscription && purchasedAppIds.contains(it.id) }
                    SubscriptionsScreen(
                        subscribedApps = subscribedList,
                        installedAppIds = installedAppIds,
                        downloadProgress = downloadProgress,
                        onBackClick = { viewModel.toggleSubscriptionsScreen(false) },
                        onAppClick = { slug ->
                            viewModel.toggleSubscriptionsScreen(false)
                            viewModel.openAppDetail(slug)
                        },
                        onInstallClick = { viewModel.installApp(it) },
                        onExploreStoreClick = {
                            viewModel.toggleSubscriptionsScreen(false)
                            viewModel.selectTab(StoreNavigationTab.APPS)
                        }
                    )
                }

                uiState.showWishlistScreen -> {
                    val wishlistList = allApps.filter { wishlistAppIds.contains(it.id) }
                    WishlistScreen(
                        wishlistApps = wishlistList,
                        installedAppIds = installedAppIds,
                        downloadProgress = downloadProgress,
                        onBackClick = { viewModel.toggleWishlistScreen(false) },
                        onAppClick = { slug ->
                            viewModel.toggleWishlistScreen(false)
                            viewModel.openAppDetail(slug)
                        },
                        onInstallClick = { viewModel.installApp(it) },
                        onExploreStoreClick = {
                            viewModel.toggleWishlistScreen(false)
                            viewModel.selectTab(StoreNavigationTab.APPS)
                        }
                    )
                }

                uiState.showRedeemScreen -> {
                    RedeemGiftCardScreen(
                        onBackClick = { viewModel.toggleRedeemScreen(false) },
                        onRedeemCode = { code ->
                            viewModel.showSnackbar("Code '$code' redeemed successfully! $25.00 added.")
                        }
                    )
                }

                else -> {
                    AnimatedContent(
                        targetState = uiState.currentTab,
                        transitionSpec = {
                            (fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) + androidx.compose.animation.scaleIn(initialScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(240)))
                                .togetherWith(fadeOut(animationSpec = androidx.compose.animation.core.tween(180)))
                        },
                        label = "MainTabsAnimation"
                    ) { currentTab ->
                        when (currentTab) {
                            StoreNavigationTab.HOME -> {
                                HomeScreen(
                                    apps = allApps,
                                    newsList = newsList,
                                    isAdmin = userProfile.isAdmin,
                                    featuredBanner = featuredBanner,
                                    userAvatarInitial = userProfile.displayName.take(1).uppercase().ifBlank { "AS" },
                                    onProfileClick = { viewModel.toggleSettingsDialog(true) },
                                    installedAppIds = installedAppIds,
                                    downloadProgress = downloadProgress,
                                    onAppClick = { viewModel.openAppDetail(it) },
                                    onInstallClick = { viewModel.installApp(it) },
                                    onSeeAllAppsClick = { viewModel.selectTab(StoreNavigationTab.APPS) },
                                    onSeeAllGamesClick = { viewModel.selectTab(StoreNavigationTab.GAMES) },
                                    onGoToAdminPanel = { viewModel.toggleAdminPanelSheet(true) }
                                )
                            }

                            StoreNavigationTab.APPS -> {
                                AppsScreen(
                                    apps = searchResults,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    selectedCategory = uiState.selectedCategory,
                                    onCategorySelect = { viewModel.setCategory(it) },
                                    installedAppIds = installedAppIds,
                                    downloadProgress = downloadProgress,
                                    onAppClick = { viewModel.openAppDetail(it) },
                                    onInstallClick = { viewModel.installApp(it) },
                                    userAvatarInitial = userProfile.displayName.take(1).uppercase().ifBlank { "AS" },
                                    onProfileClick = { viewModel.toggleSettingsDialog(true) }
                                )
                            }

                            StoreNavigationTab.GAMES -> {
                                GamesScreen(
                                    apps = searchResults,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    installedAppIds = installedAppIds,
                                    downloadProgress = downloadProgress,
                                    onAppClick = { viewModel.openAppDetail(it) },
                                    onInstallClick = { viewModel.installApp(it) },
                                    userAvatarInitial = userProfile.displayName.take(1).uppercase().ifBlank { "AS" },
                                    onProfileClick = { viewModel.toggleSettingsDialog(true) }
                                )
                            }

                            StoreNavigationTab.ARCADE -> {
                                ArcadeScreen(
                                    apps = allApps,
                                    installedAppIds = installedAppIds,
                                    downloadProgress = downloadProgress,
                                    onAppClick = { viewModel.openAppDetail(it) },
                                    onInstallClick = { viewModel.installApp(it) },
                                    userAvatarInitial = userProfile.displayName.take(1).uppercase().ifBlank { "AS" },
                                    onProfileClick = { viewModel.toggleSettingsDialog(true) }
                                )
                            }

                            StoreNavigationTab.SEARCH -> {
                                SearchScreen(
                                    apps = allApps,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    installedAppIds = installedAppIds,
                                    downloadProgress = downloadProgress,
                                    onAppClick = { viewModel.openAppDetail(it) },
                                    onInstallClick = { viewModel.installApp(it) },
                                    userAvatarInitial = userProfile.displayName.take(1).uppercase().ifBlank { "AS" }
                                )
                            }

                            StoreNavigationTab.NEWS -> {
                                NewsScreen(newsList = newsList)
                            }

                            StoreNavigationTab.PROFILE -> {
                                ProfileScreen(
                                    userProfile = userProfile,
                                    purchasedCount = purchasedAppIds.size,
                                    wishlistCount = wishlistAppIds.size,
                                    installedCount = installedAppIds.size,
                                    unreadNotificationsCount = notifications.size,
                                    themePreference = uiState.themePreference,
                                    onSetThemePreference = { viewModel.setThemePreference(it) },
                                    onSignInClick = { viewModel.toggleSignInDialog(true) },
                                    onSignOutClick = { viewModel.signOut() },
                                    onAdminPanelClick = { viewModel.toggleAdminPanelSheet(true) },
                                    onSettingsClick = { viewModel.toggleSettingsDialog(true) },
                                    onPurchasedClick = { viewModel.togglePurchasedScreen(true) },
                                    onSubscriptionsClick = { viewModel.toggleSubscriptionsScreen(true) },
                                    onWishlistClick = { viewModel.toggleWishlistScreen(true) },
                                    onRedeemGiftCardClick = { viewModel.toggleRedeemScreen(true) },
                                    onNotificationsClick = { viewModel.toggleNotificationsDialog(true) },
                                    onHelpClick = { viewModel.toggleHelpDialog(true) },
                                    onAboutClick = { viewModel.toggleAboutDialog(true) },
                                    onShowMessage = { viewModel.showSnackbar(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs & Sheets
    if (uiState.showSignInDialog) {
        SignInDialog(
            onDismiss = { viewModel.toggleSignInDialog(false) },
            onLaunchRealGoogleSignIn = { viewModel.launchRealGoogleSignIn() },
            onSignInWithEmail = { email, password, username -> viewModel.signInUser(email, username, "Email") }
        )
    }

    if (uiState.showSettingsDialog) {
        SettingsDialog(
            currentAdminEmail = viewModel.adminEmail,
            themePreference = uiState.themePreference,
            onSetThemePreference = { viewModel.setThemePreference(it) },
            onDismiss = { viewModel.toggleSettingsDialog(false) },
            onSaveAdminEmail = { viewModel.showSnackbar("Admin access verified for: $it") }
        )
    }

    if (uiState.showHelpDialog) {
        HelpAndSupportDialog(onDismiss = { viewModel.toggleHelpDialog(false) })
    }

    if (uiState.showAboutDialog) {
        AboutSpoiledStoreDialog(onDismiss = { viewModel.toggleAboutDialog(false) })
    }

    if (uiState.showNotificationsDialog) {
        NotificationsCenterDialog(
            notifications = notifications,
            onDismiss = { viewModel.toggleNotificationsDialog(false) },
            onDeleteNotification = { viewModel.deleteNotification(it) }
        )
    }

    if (uiState.showAdminNotificationSheet) {
        AdminBroadcastNotificationSheet(
            onDismiss = { viewModel.toggleAdminNotificationSheet(false) },
            onSendNotification = { title, msg -> viewModel.sendBroadcastNotification(title, msg) }
        )
    }

    if (uiState.showAdminUploadSheet) {
        AdminUploadAppSheet(
            onDismiss = { viewModel.toggleAdminUploadSheet(false) },
            onCopyApkFromUri = { uri, name -> viewModel.copyApkFileFromUri(uri, name) },
            onCopyImageFromUri = { uri, isIcon -> viewModel.copyImageFileFromUri(uri, isIcon) },
            onCreateSampleApk = { name -> viewModel.createSampleApkOnDisk(name) },
            onUploadImageToCloud = { uri, isIcon, onResult -> viewModel.uploadImageToSupabase(uri, isIcon, onResult) },
            onUploadApkToCloud = { uri, name, onResult -> viewModel.uploadApkToSupabase(uri, name, onResult) },
            onUploadSampleApkToCloud = { name, onResult -> viewModel.uploadSampleApkToSupabase(name, onResult) },
            supabaseConfig = viewModel.getSupabaseConfig(),
            onSaveSupabaseConfig = { url, key -> viewModel.saveSupabaseConfig(url, key) },
            onUpload = { name, cat, dev, tag, desc, apk, size, ver, icon, shots, bundle, price, sub, topApp, topGame, newNote, feat, overview, speed, sec, cloudApkUrl ->
                viewModel.uploadRealApp(
                    name = name,
                    category = cat,
                    developer = dev,
                    tagline = tag,
                    description = desc,
                    apkFilePath = apk,
                    sizeBytes = size,
                    version = ver,
                    iconPath = icon,
                    screenshots = shots,
                    bundleId = bundle,
                    price = price,
                    isSubscription = sub,
                    isTopApp = topApp,
                    isTopGame = topGame,
                    isNewNoteworthy = newNote,
                    isFeatured = feat,
                    overview = overview,
                    speedScore = speed,
                    securityStatus = sec,
                    cloudApkUrl = cloudApkUrl
                )
            }
        )
    }

    if (uiState.showAdminUploadNewsSheet) {
        AdminUploadNewsSheet(
            onDismiss = { viewModel.toggleAdminUploadNewsSheet(false) },
            onPublish = { title, category, summary, content, author ->
                viewModel.uploadNews(title, category, summary, content, author)
            }
        )
    }
}
