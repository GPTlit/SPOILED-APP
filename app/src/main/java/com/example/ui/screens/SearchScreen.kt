package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.AppItem
import com.example.ui.components.AppActionPill
import com.example.ui.components.AppIconImage
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    apps: List<AppItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    installedAppIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val browseCategories = listOf(
        "Top Downloaded Apps",
        "Top Downloaded Games",
        "Top Selling Apps",
        "Top Selling Games",
        "Photo & Video",
        "Utilities",
        "Productivity",
        "Finance",
        "Racing",
        "Puzzles",
        "Trivia",
        "Indie Kids"
    )

    val trendingSuggestions = listOf(
        "photo filters",
        "cloud storage",
        "open source browser",
        "media player",
        "racing games",
        "productivity tools",
        "offline utility"
    )

    val filteredApps = if (searchQuery.isBlank()) {
        apps
    } else {
        val q = searchQuery.trim().lowercase()
        apps.filter {
            it.name.lowercase().contains(q) ||
            it.category.lowercase().contains(q) ||
            it.developer.lowercase().contains(q) ||
            it.tagline.lowercase().contains(q) ||
            it.description.lowercase().contains(q)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Search Bar Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Input Field matching iOS style
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceSecondary,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "Apps, Games, Tools & Verified APKs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextTertiary
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input_field")
                        )
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TextTertiary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Suggestions & Browse Categories (when query is empty)
        if (searchQuery.isBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "Discover & Browse",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        browseCategories.forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SurfaceLight,
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        val filter = when (category) {
                                            "Top Downloaded Apps" -> "Utilities"
                                            "Top Downloaded Games" -> "Racing"
                                            "Top Selling Apps" -> "Productivity"
                                            "Top Selling Games" -> "Games"
                                            else -> category
                                        }
                                        onSearchQueryChange(filter)
                                    }
                                    .testTag("browse_chip_${category.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Trending Searches",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        trendingSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchQueryChange(suggestion) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = AppleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                            }
                            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Suggested Applications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }

        // 3. Search Result Cards matching image_a8c2138e.png (App Header + 3 Side-by-Side Screenshots)
        items(filteredApps, key = { it.id }) { app ->
            SearchAppResultCard(
                app = app,
                isInstalled = installedAppIds.contains(app.id),
                downloadProgress = downloadProgress[app.id],
                onAppClick = { onAppClick(app.slug) },
                onInstallClick = { onInstallClick(app.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        if (filteredApps.isEmpty() && searchQuery.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try searching for another keyword, app name, or category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * App Result Card matching user reference image image_a8c2138e.png:
 * Top Row: Squircle Icon, Title, Subtitle, Star Rating + count, GET button.
 * Bottom: 3 portrait screenshot previews side by side with rounded corners.
 */
@Composable
fun SearchAppResultCard(
    app: AppItem,
    isInstalled: Boolean,
    downloadProgress: Float?,
    onAppClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAppClick() }
            .testTag("search_app_card_${app.slug}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Title, Rating, Action Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconImage(
                    iconPath = app.iconPath,
                    appName = app.name,
                    size = 56.dp,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.tagline.ifBlank { "${app.category} • ${app.developer}" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${app.rating} (${app.reviewsCount} reviews)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                AppActionPill(
                    isInstalled = isInstalled,
                    downloadProgress = downloadProgress,
                    onClick = onInstallClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Side-by-Side Portrait Screenshots (matching image_a8c2138e.png)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shotsToDisplay = if (app.screenshots.isNotEmpty()) {
                    app.screenshots.take(3)
                } else {
                    emptyList()
                }

                if (shotsToDisplay.isNotEmpty()) {
                    shotsToDisplay.forEach { shotUrl ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, BorderLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                        ) {
                            AsyncImage(
                                model = com.example.util.ImageHelper.buildImageModel(shotUrl) ?: shotUrl,
                                contentDescription = "${app.name} preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Pad up to 3 cards if fewer screenshots exist
                    repeat(3 - shotsToDisplay.size) {
                        MockScreenshotCard(app = app, modifier = Modifier.weight(1f))
                    }
                } else {
                    // 3 rich mock previews with app branding and category elements
                    MockScreenshotCard(app = app, variant = 0, modifier = Modifier.weight(1f))
                    MockScreenshotCard(app = app, variant = 1, modifier = Modifier.weight(1f))
                    MockScreenshotCard(app = app, variant = 2, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MockScreenshotCard(
    app: AppItem,
    variant: Int = 0,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (variant) {
        0 -> listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        1 -> listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
        else -> listOf(Color(0xFF1E1B4B), Color(0xFF020617))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, BorderLight),
        modifier = modifier.height(160.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mock phone status bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "9:41", fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                        Box(modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                    }
                }

                // Mock app interface preview
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = app.name.take(2).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (variant) {
                            0 -> "Dashboard"
                            1 -> "Real Engine"
                            else -> "Verified"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.category,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Mock bottom navigation bar indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
