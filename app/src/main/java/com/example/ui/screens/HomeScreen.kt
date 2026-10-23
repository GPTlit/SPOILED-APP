package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.models.AppItem
import com.example.data.models.FeaturedBannerConfig
import com.example.data.models.NewsItem
import com.example.ui.components.AppStoreSectionHeader
import com.example.ui.components.CompactAppItem
import com.example.ui.components.LiquidGlassSearchBar
import com.example.ui.components.SpoiledStoreWordmark
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

@Composable
fun HomeScreen(
    apps: List<AppItem>,
    newsList: List<NewsItem>,
    isAdmin: Boolean,
    featuredBanner: FeaturedBannerConfig = FeaturedBannerConfig(),
    isHighContrast: Boolean = false,
    onToggleTheme: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String = "All",
    onCategorySelect: (String) -> Unit = {},
    installedAppIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    onSeeAllAppsClick: () -> Unit,
    onSeeAllGamesClick: () -> Unit,
    onGoToAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "All", "Puzzles", "Trivia", "Indie Kids", "Racing", "Productivity", "Finance", "Photo & Video", "Utilities"
    )

    val displayedApps = if (selectedCategory == "All") {
        apps
    } else {
        apps.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val topApps = displayedApps.filter { it.isTopApp }
    val topGames = displayedApps.filter { it.isTopGame }
    val heavyRotationApps = displayedApps.sortedByDescending { it.downloadsCount }
    val otherApps = displayedApps.filter { !it.isTopApp && !it.isTopGame }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Header Wordmark ("Spoiled Store") & Search
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpoiledStoreWordmark()

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
                                    modifier = Modifier.size(13.dp)
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
                }

                Spacer(modifier = Modifier.height(12.dp))
                LiquidGlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange
                )
            }
        }

        // 2. Category Filter Pills (Horizontal swipe)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp).testTag("home_category_pills")
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) Color.Black else SurfaceSecondary,
                        border = BorderStroke(1.dp, if (isSelected) Color.Black else BorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onCategorySelect(category) }
                            .testTag("home_pill_${category.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3. Featured Card (Admin Customizable)
        item {
            TodayFeaturedCard(
                banner = featuredBanner,
                onCardClick = onSeeAllAppsClick,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Empty state when catalog is empty
        if (apps.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.05f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Catalog Awaiting Uploads",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload real Android APK packages directly from the Admin Panel to populate the store for all users.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onGoToAdminPanel,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload App Now")
                            }
                        }
                    }
                }
            }
        } else {
            // 4. APPS IN HEAVY ROTATION
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 4.dp)) {
                    Text(
                        text = "Apps in Heavy Rotation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        ),
                        color = TextPrimary
                    )
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
                    items(heavyRotationApps.take(6), key = { "rot_${it.id}" }) { app ->
                        CompactAppItem(
                            app = app,
                            isInstalled = installedAppIds.contains(app.id),
                            downloadProgress = downloadProgress[app.id],
                            onAppClick = { onAppClick(app.slug) },
                            onInstallClick = { onInstallClick(app.id) }
                        )
                    }
                }
            }

            // 5. Top Games Section (if any)
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
                        items(topGames, key = { it.id }) { game ->
                            CompactAppItem(
                                app = game,
                                isInstalled = installedAppIds.contains(game.id),
                                downloadProgress = downloadProgress[game.id],
                                onAppClick = { onAppClick(game.slug) },
                                onInstallClick = { onInstallClick(game.id) }
                            )
                        }
                    }
                }
            }

            // 6. Top Downloaded Apps Section
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
                        items(topApps, key = { "top_${it.id}" }) { app ->
                            CompactAppItem(
                                app = app,
                                isInstalled = installedAppIds.contains(app.id),
                                downloadProgress = downloadProgress[app.id],
                                onAppClick = { onAppClick(app.slug) },
                                onInstallClick = { onInstallClick(app.id) }
                            )
                        }
                    }
                }
            }

            // 7. Recently Uploaded Apps
            val allOther = if (topApps.isEmpty() && topGames.isEmpty()) displayedApps else otherApps
            if (allOther.isNotEmpty()) {
                item {
                    AppStoreSectionHeader(
                        title = "Recently Uploaded",
                        onSeeAllClick = onSeeAllAppsClick
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(allOther, key = { "rec_${it.id}" }) { app ->
                            CompactAppItem(
                                app = app,
                                isInstalled = installedAppIds.contains(app.id),
                                downloadProgress = downloadProgress[app.id],
                                onAppClick = { onAppClick(app.slug) },
                                onInstallClick = { onInstallClick(app.id) }
                            )
                        }
                    }
                }
            }
        }

        // 8. Latest Store News Stories
        if (newsList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AppStoreSectionHeader(
                    title = "Latest Store News",
                    onSeeAllClick = {}
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(newsList, key = { it.id }) { news ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                            border = BorderStroke(1.dp, BorderLight),
                            modifier = Modifier
                                .width(220.dp)
                                .height(120.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(
                                        text = news.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = news.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 2
                                    )
                                }
                                Text(
                                    text = "${news.date} • ${news.author}",
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
}

@Composable
private fun TodayFeaturedCard(
    banner: FeaturedBannerConfig,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCardMid),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp)
            .clickable { onCardClick() }
            .testTag("today_featured_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CharcoalCardStart,
                            CharcoalCardMid,
                            CharcoalCardEnd
                        )
                    )
                )
        ) {
            if (banner.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.featured_card_phone),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(180.dp)
                        .height(175.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CharcoalCardStart.copy(alpha = 0.95f),
                                CharcoalCardStart.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = banner.tagline.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color(0xFFB0B7C3)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp,
                            lineHeight = 26.sp
                        ),
                        color = Color.White,
                        maxLines = 2
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = banner.description.ifBlank { "Explore Catalog" },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
