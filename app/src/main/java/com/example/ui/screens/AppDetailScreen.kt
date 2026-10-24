package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.components.motionEntrance
import com.example.ui.components.pressBounce
import com.example.ui.components.liquidShimmer
import com.example.ui.components.pulseGlow
import com.example.ui.components.AnimatedModalCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.models.AppItem
import com.example.data.models.CommentItem
import com.example.ui.components.AppActionPill
import com.example.ui.components.AppIconImage
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AppDetailScreen(
    app: AppItem,
    isInstalled: Boolean,
    downloadProgress: Float?,
    isWishlisted: Boolean,
    isPurchased: Boolean,
    comments: List<CommentItem>,
    onBackClick: () -> Unit,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onPurchaseClick: () -> Unit,
    onAddComment: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var descExpanded by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    // Full-screen screenshot zoom modal state
    var selectedZoomScreenshot by remember { mutableStateOf<String?>(null) }

    BackHandler {
        onBackClick()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_detail_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Navigation Top Bar (matching user reference photo with < Games / < Apps back label)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backCategoryName = if (app.category.contains("Game", ignoreCase = true)) "Games" else "Apps"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppleBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = backCategoryName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = AppleBlue,
                            fontSize = 17.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onWishlistClick) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) Color(0xFFE1306C) else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out ${app.name} on Spoiled Store! ${app.tagline}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share ${app.name}"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = AppleBlue
                        )
                    }
                }
            }
        }

        // 2. App Hero Row (Icon, Name, Developer, GET / BUY button)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionEntrance(0, delayPerIndexMs = 25)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                AppIconImage(
                    iconUrl = app.iconUrl,
                    fallbackText = app.iconFallbackText,
                    accentColor = app.iconColorHex,
                    size = 96.dp,
                    cornerRadius = 22.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = app.tagline.ifBlank { app.developer },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = app.developer,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (app.isSubscription && !isPurchased) {
                            Button(
                                onClick = onPurchaseClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.height(34.dp).testTag("app_subscribe_button")
                            ) {
                                Text(
                                    text = if (app.price > 0) "Buy $${app.price}" else "Subscribe",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            AppActionPill(
                                isInstalled = isInstalled,
                                downloadProgress = downloadProgress,
                                onClick = onInstallClick
                            )
                        }

                        if (isInstalled) {
                            Text(
                                text = "Uninstall",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clickable { onUninstallClick() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Stats Strip (Matching User Reference Image 2: Ratings, Age Rating, Chart, Developer)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .motionEntrance(1, delayPerIndexMs = 30)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppStatItem(
                    label = "${formatCount(app.reviewsCount)} RATINGS",
                    value = "${app.rating}",
                    hasStarRow = true
                )
                AppStatDivider()
                AppStatItem(
                    label = "AGE RATING",
                    value = "4+",
                    subtext = "Years Old"
                )
                AppStatDivider()
                AppStatItem(
                    label = "CHART",
                    value = "#7",
                    subtext = app.category.take(8)
                )
                AppStatDivider()
                AppStatItem(
                    label = "DEVELOPER",
                    value = app.developer.take(9),
                    icon = Icons.Default.AccountCircle
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 20.dp))
        }

        // 4. OVERVIEW, SPEED, AND SECURITY
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Overview, Speed & Security",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Speed Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Speed Rating",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.speedScore,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Native load speed",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }

                    // Security Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF248A4B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Security",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.securityStatus,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = TextPrimary
                            )
                            Text(
                                text = "Verified sandbox runtime",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Overview highlights banner
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Architectural Overview",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.overview,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 4b. IN-APP EVENT (Matching Reference Image 4)
        item {
            var eventReminderSet by remember { mutableStateOf(false) }
            val eventCardInteraction = remember { MutableInteractionSource() }

            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .motionEntrance(2, delayPerIndexMs = 30)
            ) {
                Text(
                    text = "Events",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp,
                        fontSize = 22.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "TODAY 12:00 PM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 11.sp
                    ),
                    color = AppleBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    border = BorderStroke(0.5.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressBounce(eventCardInteraction, scaleDown = 0.98f)
                        .testTag("app_event_card")
                ) {
                    Column {
                        // Event Banner with gradient, event info, and bell reminder button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .liquidShimmer(durationMs = 3600)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF334155))
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top row: Notification Bell on top right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable { eventReminderSet = !eventReminderSet }
                                            .pulseGlow(minScale = 0.95f, maxScale = 1.05f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (eventReminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                                contentDescription = "Event Notification Reminder",
                                                tint = if (eventReminderSet) AppleBlue else Color.White,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = "COMPETITION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${app.name} Championship Tour",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.3).sp,
                                            fontSize = 20.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Celebrate seasonal competition and unlock exclusive cloud drops!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Event docked bottom bar matching Reference Image 4
                        Surface(
                            color = SurfaceLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AppIconImage(
                                        iconPath = app.iconPath,
                                        appName = app.name,
                                        size = 40.dp,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = app.tagline.ifBlank { "Race around the world!" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                AppActionPill(
                                    isInstalled = isInstalled,
                                    downloadProgress = downloadProgress,
                                    onClick = onInstallClick,
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4c. WHAT'S NEW (Matching Reference Image 2)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .motionEntrance(3, delayPerIndexMs = 30)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "What's New",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp,
                            fontSize = 22.sp
                        ),
                        color = TextPrimary
                    )

                    Text(
                        text = "Version History",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = AppleBlue
                        ),
                        modifier = Modifier
                            .clickable { /* Version History */ }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Version ${app.version}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Text(
                        text = "2w ago",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Enhanced rendering performance and native touch responsiveness\n• Added support for seasonal in-app event leaderboards\n• Bug fixes, memory optimizations, and stability improvements",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        // 5. PICTURES FROM INSIDE THE APP (Screenshots Gallery with Zoom & Click-to-Enlarge)
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inside App Screenshots",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Tap to zoom",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = AppleBlue
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val screenGradients = listOf(
                listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
                listOf(Color(0xFF1E1B4B), Color(0xFF312E81)),
                listOf(Color(0xFF064E3B), Color(0xFF065F46))
            )

            val displayScreenshots: List<String> = if (app.screenshots.isNotEmpty()) {
                app.screenshots
            } else {
                listOf("mock://0", "mock://1", "mock://2")
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayScreenshots.size) { index ->
                    val shotUri = displayScreenshots[index]
                    val isMock = shotUri.startsWith("mock://")
                    val grad = screenGradients[index % screenGradients.size]

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .width(210.dp)
                            .height(350.dp)
                            .clickable { selectedZoomScreenshot = shotUri }
                            .testTag("screenshot_card_$index")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 1. High-Fidelity Rendered Phone Screen (Always visible base and offline fallback)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(grad))
                                    .padding(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Status bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "9:41",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Box(modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
                                            Box(modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
                                        }
                                    }

                                    // App Interface Content Mockup
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        AppIconImage(
                                            iconPath = app.iconPath,
                                            appName = app.name,
                                            size = 52.dp,
                                            shape = RoundedCornerShape(14.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = when (index) {
                                                0 -> "Home & Overview"
                                                1 -> "Live Interface"
                                                else -> "Specifications"
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (index) {
                                                0 -> "Verified runtime & instant access"
                                                1 -> "Fast performance & low latency"
                                                else -> "Sandboxed & security audited"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.75f),
                                            lineHeight = 14.sp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Mock UI card inside device
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.14f),
                                            modifier = Modifier.fillMaxWidth().height(80.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.Center) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(16.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(3.dp)))
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(modifier = Modifier.fillMaxWidth(0.9f).height(5.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp)))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Box(modifier = Modifier.fillMaxWidth(0.5f).height(5.dp).background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp)))
                                            }
                                        }
                                    }

                                    // Home bar
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .width(46.dp)
                                            .height(3.dp)
                                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                                    )
                                }
                            }

                            // 2. Real Image (if valid, loaded on top)
                            if (!isMock && shotUri.isNotBlank()) {
                                val model = com.example.util.ImageHelper.buildImageModel(shotUri) ?: shotUri
                                AsyncImage(
                                    model = model,
                                    contentDescription = "App Screenshot",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Zoom overlay icon
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. DESCRIPTION
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = app.description.ifBlank { "Full real Android app verified and hosted on Spoiled Store." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = if (descExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (descExpanded) "less" else "more",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppleBlue,
                    modifier = Modifier.clickable { descExpanded = !descExpanded }
                )
            }
        }

        // 7. USER COMMENTS & RATINGS
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ratings & Comments",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${comments.size} user reviews",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { showReviewDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("write_review_button")
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Write Review", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (comments.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No reviews yet. Be the first to download and write a comment!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        comments.forEach { commentItem ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = commentItem.userName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = commentItem.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row {
                                        repeat(commentItem.rating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = commentItem.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. APP METADATA & SPECS
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Information",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                InfoRow("Developer", app.developer)
                InfoRow("Package ID", app.bundleId)
                InfoRow("Size", app.sizeFormatted)
                InfoRow("Version", app.version)
                InfoRow("Category", app.category)
                InfoRow("Compatibility", "Android 7.0 and up")
                InfoRow("Pricing", if (app.price > 0) "$${app.price}" else "Free")
                InfoRow("Distribution", "Cloud Storage / Direct APK")
            }
        }
    }

    // FULL-SCREEN SCREENSHOT ZOOM VIEWER DIALOG
    selectedZoomScreenshot?.let { screenshotUri ->
        Dialog(
            onDismissRequest = { selectedZoomScreenshot = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 4f)
                offset += offsetChange
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .testTag("screenshot_zoom_viewer")
            ) {
                // Image with pinch-to-zoom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(state = transformState),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = com.example.util.ImageHelper.buildImageModel(screenshotUri) ?: screenshotUri,
                        contentDescription = "Zoomed screenshot",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }

                // Top Close Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Screenshot Viewer (Pinch to zoom)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    IconButton(
                        onClick = { selectedZoomScreenshot = null },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    // REVIEW SUBMISSION DIALOG
    if (showReviewDialog) {
        var reviewText by remember { mutableStateOf("") }
        var reviewRating by remember { mutableIntStateOf(5) }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Rate ${app.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Share your real experience with this app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= reviewRating) Color.Black else TextTertiary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { reviewRating = star }
                                    .padding(2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("Write your review or feedback...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth().testTag("review_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewText.isNotBlank()) {
                            onAddComment(reviewText.trim(), reviewRating)
                            showReviewDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("submit_review_button")
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showReviewDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AppStatItem(
    label: String,
    value: String,
    subtext: String? = null,
    hasStar: Boolean = false,
    hasStarRow: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            ),
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            if (hasStar) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        if (hasStarRow) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        } else if (subtext != null) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun AppStatDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(BorderLight)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
            color = TextPrimary
        )
    }
    HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> "$count"
    }
}
