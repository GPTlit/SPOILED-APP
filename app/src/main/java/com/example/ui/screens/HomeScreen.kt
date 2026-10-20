package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
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
import com.example.R
import com.example.data.models.AppItem
import com.example.data.models.NewsItem
import com.example.ui.components.AppStoreSectionHeader
import com.example.ui.components.CompactAppItem
import com.example.ui.components.LiquidGlassSearchBar
import com.example.ui.components.SpoiledStoreWordmark
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
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    installedAppIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    onSeeAllAppsClick: () -> Unit,
    onSeeAllGamesClick: () -> Unit,
    onGoToAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topApps = apps.filter { it.isTopApp }
    val topGames = apps.filter { it.isTopGame }
    val otherApps = apps.filter { !it.isTopApp && !it.isTopGame }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Header Wordmark ("Spoiled Store")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                SpoiledStoreWordmark()
                Spacer(modifier = Modifier.height(14.dp))
                LiquidGlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange
                )
            }
        }

        // 2. Featured Card (Clean Monochrome)
        item {
            TodayFeaturedCard(
                onCardClick = onSeeAllAppsClick,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        // If NO apps exist yet in the store:
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Authentic Store Catalog",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "All fake placeholder apps have been removed. Every application in Spoiled Store is real, with genuine uploaded APK files and verified internal storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isAdmin) {
                            Button(
                                onClick = onGoToAdminPanel,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("home_go_admin_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload App in Admin Panel")
                            }
                        } else {
                            Text(
                                text = "Admin can log in via Profile with salemmoustapha15@gmail.com to upload apps.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
        } else {
            // 3. Top Apps Section (if any)
            if (topApps.isNotEmpty()) {
                item {
                    AppStoreSectionHeader(
                        title = "Top Apps",
                        onSeeAllClick = onSeeAllAppsClick
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(topApps, key = { it.id }) { app ->
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

            // 4. Top Games Section (if any)
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

            // 5. All Uploaded Store Apps
            val allOther = if (topApps.isEmpty() && topGames.isEmpty()) apps else otherApps
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
                        items(allOther, key = { it.id }) { app ->
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

        // 6. News Stories on Homepage
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
            Image(
                painter = painterResource(id = R.drawable.featured_card_phone),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(180.dp)
                    .height(175.dp)
                    .align(Alignment.CenterEnd)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CharcoalCardStart,
                                CharcoalCardStart.copy(alpha = 0.88f),
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
                        text = "FEATURED ECOSYSTEM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color(0xFFB0B7C3)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real APK Downloads\n& Cloud Storage",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp,
                            lineHeight = 26.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Explore Catalog",
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
