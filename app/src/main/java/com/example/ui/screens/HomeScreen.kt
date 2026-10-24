package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.models.AppItem
import com.example.data.models.FeaturedBannerConfig
import com.example.data.models.NewsItem
import com.example.ui.components.AppActionPill
import com.example.ui.components.AppIconImage
import com.example.ui.components.AppStoreSectionHeader
import com.example.ui.components.CompactAppItem
import com.example.ui.components.liquidShimmer
import com.example.ui.components.motionEntrance
import com.example.ui.components.pressBounce
import com.example.ui.components.pulseGlow
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CharcoalCardEnd
import com.example.ui.theme.CharcoalCardMid
import com.example.ui.theme.CharcoalCardStart
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic Apple App Store "Today" Homepage matching user reference photos.
 * Features:
 * - Date line & bold "Today" header with Profile avatar button
 * - Large full-bleed Today hero card with docked app action row
 * - In-App Events card (matching Mario Kart Wedding Tour from photo 2 & 4)
 * - Apps in Heavy Rotation carousel
 * - Today editorial stories
 * - Top Games & Top Downloaded Apps carousels
 */
@Composable
fun HomeScreen(
    apps: List<AppItem>,
    newsList: List<NewsItem>,
    isAdmin: Boolean,
    featuredBanner: FeaturedBannerConfig = FeaturedBannerConfig(),
    userAvatarInitial: String = "AS",
    onProfileClick: () -> Unit = {},
    installedAppIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    onSeeAllAppsClick: () -> Unit,
    onSeeAllGamesClick: () -> Unit,
    onGoToAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val marioKart = apps.find { it.slug == "mario-kart-tour" || it.name.contains("Mario", ignoreCase = true) }
        ?: apps.find { it.isTopGame }
        ?: apps.firstOrNull()

    val topApps = apps.filter { it.isTopApp }
    val topGames = apps.filter { it.isTopGame || it.category.contains("Game", ignoreCase = true) }
    val heavyRotationApps = apps.sortedByDescending { it.downloadsCount }

    val formattedDate = remember {
        try {
            val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.US)
            sdf.format(Date()).uppercase()
        } catch (_: Exception) {
            "TODAY"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. TODAY HEADER (Date, "Today", and Profile Avatar Button)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp, bottom = 12.dp)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1.0).sp,
                            fontSize = 34.sp
                        ),
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAdmin) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Black,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable { onGoToAdminPanel() }
                                    .testTag("home_admin_quick_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Admin Panel",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Admin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Circular Profile Avatar (Tapping opens Account & Settings)
                        val avatarInteraction = remember { MutableInteractionSource() }
                        Surface(
                            shape = CircleShape,
                            color = AppleBlue.copy(alpha = 0.12f),
                            border = BorderStroke(1.5.dp, AppleBlue.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .pressBounce(avatarInteraction, scaleDown = 0.92f)
                                .clickable(
                                    interactionSource = avatarInteraction,
                                    indication = null
                                ) { onProfileClick() }
                                .testTag("home_profile_avatar_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userAvatarInitial,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppleBlue
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. LARGE HERO TODAY CARD (Game / App of the Day with docked bottom bar)
        item {
            TodayAppStoreHeroCard(
                banner = featuredBanner,
                featuredApp = marioKart ?: apps.firstOrNull(),
                isInstalled = marioKart?.let { installedAppIds.contains(it.id) } ?: false,
                downloadProgress = marioKart?.let { downloadProgress[it.id] },
                onCardClick = {
                    if (marioKart != null) {
                        onAppClick(marioKart.slug)
                    } else {
                        onSeeAllAppsClick()
                    }
                },
                onInstallClick = {
                    marioKart?.let { onInstallClick(it.id) }
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // 3. IN-APP EVENTS HERO CARD (Exact parity with Reference Photo 2 & 4)
        if (marioKart != null) {
            item {
                TodayInAppEventsCard(
                    app = marioKart,
                    isInstalled = installedAppIds.contains(marioKart.id),
                    downloadProgress = downloadProgress[marioKart.id],
                    onAppClick = { onAppClick(marioKart.slug) },
                    onInstallClick = { onInstallClick(marioKart.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        // 4. APPS IN HEAVY ROTATION / NOW TRENDING
        if (heavyRotationApps.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Apps in Heavy Rotation",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp,
                                fontSize = 22.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppleBlue
                            ),
                            modifier = Modifier
                                .clickable { onSeeAllAppsClick() }
                                .padding(vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Keep up with the trends in culture and technology",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    itemsIndexed(heavyRotationApps.take(6), key = { _, it -> "rot_${it.id}" }) { index, app ->
                        CompactAppItem(
                            app = app,
                            isInstalled = installedAppIds.contains(app.id),
                            downloadProgress = downloadProgress[app.id],
                            onAppClick = { onAppClick(app.slug) },
                            onInstallClick = { onInstallClick(app.id) },
                            modifier = Modifier.motionEntrance(index, delayPerIndexMs = 28)
                        )
                    }
                }
            }
        }

        // 5. DAILY EDITORIAL STORY CARD
        item {
            TodayEditorialCard(
                category = "THE BASICS",
                headline = "Essential Tools for Android Creators",
                subtitle = "Take your mobile workflows and multimedia editing to the next level.",
                onClick = onSeeAllAppsClick,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        // 6. TOP GAMES CAROUSEL
        if (topGames.isNotEmpty()) {
            item {
                AppStoreSectionHeader(
                    title = "Top Games",
                    onSeeAllClick = onSeeAllGamesClick
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    itemsIndexed(topGames, key = { _, it -> "tg_${it.id}" }) { index, game ->
                        CompactAppItem(
                            app = game,
                            isInstalled = installedAppIds.contains(game.id),
                            downloadProgress = downloadProgress[game.id],
                            onAppClick = { onAppClick(game.slug) },
                            onInstallClick = { onInstallClick(game.id) },
                            modifier = Modifier.motionEntrance(index, delayPerIndexMs = 28)
                        )
                    }
                }
            }
        }

        // 7. TOP DOWNLOADED APPS CAROUSEL
        if (topApps.isNotEmpty()) {
            item {
                AppStoreSectionHeader(
                    title = "Top Downloaded Apps",
                    onSeeAllClick = onSeeAllAppsClick
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    itemsIndexed(topApps, key = { _, it -> "ta_${it.id}" }) { index, app ->
                        CompactAppItem(
                            app = app,
                            isInstalled = installedAppIds.contains(app.id),
                            downloadProgress = downloadProgress[app.id],
                            onAppClick = { onAppClick(app.slug) },
                            onInstallClick = { onInstallClick(app.id) },
                            modifier = Modifier.motionEntrance(index, delayPerIndexMs = 28)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Large Full-Bleed Today Hero Card with docked bottom app row matching Apple App Store.
 */
@Composable
private fun TodayAppStoreHeroCard(
    banner: FeaturedBannerConfig,
    featuredApp: AppItem?,
    isInstalled: Boolean,
    downloadProgress: Float?,
    onCardClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCardStart),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .motionEntrance(0, offsetY = 16.dp)
            .pressBounce(interactionSource, scaleDown = 0.98f)
            .clickable(interactionSource = interactionSource, indication = null) { onCardClick() }
            .testTag("today_featured_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork
            if (banner.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    CharcoalCardStart,
                                    CharcoalCardMid,
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.featured_card_phone),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(280.dp)
                        .align(Alignment.Center)
                )
            }

            // Top gradient for text legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top Headline & Eyebrow
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = banner.tagline.ifBlank { "GAME OF THE DAY" }.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 12.sp
                    ),
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = banner.title.ifBlank { featuredApp?.name ?: "Mario Kart Tour" },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                        color = Color.White,
                        lineHeight = 28.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Docked App Row (Apple App Store standard)
            Surface(
                color = SurfaceLight.copy(alpha = 0.92f),
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (featuredApp != null) {
                        AppIconImage(
                            iconPath = featuredApp.iconPath,
                            appName = featuredApp.name,
                            size = 46.dp,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = featuredApp.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = featuredApp.tagline.ifBlank { "${featuredApp.category} • ${featuredApp.developer}" },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        AppActionPill(
                            isInstalled = isInstalled,
                            downloadProgress = downloadProgress,
                            price = featuredApp.price,
                            isSubscription = featuredApp.isSubscription,
                            onClick = onInstallClick
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Explore Verified Apps",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Browse the catalog for top picks",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * In-App Events Hero Card matching Reference Photo 2 & 4:
 * "Events" header, "TODAY 12:00 PM" blue text, live bell reminder button,
 * "COMPETITION" tag, and docked bottom row.
 */
@Composable
private fun TodayInAppEventsCard(
    app: AppItem,
    isInstalled: Boolean,
    downloadProgress: Float?,
    onAppClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasReminderSet by remember { mutableStateOf(false) }
    val cardInteraction = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "TODAY 12:00 PM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        fontSize = 12.sp
                    ),
                    color = AppleBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Events",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 26.sp
                    ),
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .motionEntrance(1, offsetY = 16.dp)
                .pressBounce(cardInteraction, scaleDown = 0.98f)
                .clickable(interactionSource = cardInteraction, indication = null) { onAppClick() }
                .testTag("today_in_app_event_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Event Artwork Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = if (app.screenshots.isNotEmpty()) app.screenshots.first() else app.iconPath,
                        contentDescription = "Event banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Scrim for text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )

                    // Bell Notification Button (Top Right)
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(38.dp)
                            .pulseGlow(minScale = 0.96f, maxScale = 1.04f, durationMs = 1800)
                    ) {
                        IconButton(onClick = { hasReminderSet = !hasReminderSet }) {
                            Icon(
                                imageVector = if (hasReminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Event Reminder",
                                tint = if (hasReminderSet) AppleBlue else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Bottom Event Details on image
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "COMPETITION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Wedding Tour",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.3).sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Mario and Peach celebrate wedding season in style!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Docked Bottom Mini App Row
                Surface(
                    color = SurfaceLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconImage(
                            iconPath = app.iconPath,
                            appName = app.name,
                            size = 46.dp,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = app.tagline.ifBlank { "Race around the world!" },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        AppActionPill(
                            isInstalled = isInstalled,
                            downloadProgress = downloadProgress,
                            price = app.price,
                            isSubscription = app.isSubscription,
                            onClick = onInstallClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Editorial Story Card
 */
@Composable
private fun TodayEditorialCard(
    category: String,
    headline: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
        border = BorderStroke(1.dp, BorderLight),
        modifier = modifier
            .fillMaxWidth()
            .motionEntrance(2, offsetY = 16.dp)
            .pressBounce(interactionSource, scaleDown = 0.98f)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = category.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
