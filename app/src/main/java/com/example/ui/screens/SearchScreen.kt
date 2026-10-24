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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import com.example.ui.components.motionEntrance
import com.example.ui.components.pressBounce
import com.example.ui.components.liquidShimmer
import com.example.ui.components.pulseGlow
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

data class BrowseCategoryItem(
    val title: String,
    val queryTag: String,
    val gradientColors: List<Color>
)

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
    userAvatarInitial: String = "AS",
    modifier: Modifier = Modifier
) {
    val browseCards = listOf(
        BrowseCategoryItem("Top Apps", "Utilities", listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
        BrowseCategoryItem("Top Games", "Games", listOf(Color(0xFFF97316), Color(0xFFC2410C))),
        BrowseCategoryItem("Photo & Video", "Photo & Video", listOf(Color(0xFFF59E0B), Color(0xFFB45309))),
        BrowseCategoryItem("Productivity", "Productivity", listOf(Color(0xFF10B981), Color(0xFF047857))),
        BrowseCategoryItem("Top Download Apps", "Top", listOf(Color(0xFF6366F1), Color(0xFF4338CA))),
        BrowseCategoryItem("Top Download Games", "Racing", listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
        BrowseCategoryItem("Top Selling Apps", "Developer Tools", listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
        BrowseCategoryItem("Top Selling Games", "Action Games", listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
        BrowseCategoryItem("Utilities", "Utilities", listOf(Color(0xFF06B6D4), Color(0xFF0E7490))),
        BrowseCategoryItem("Finance", "Finance", listOf(Color(0xFF14B8A6), Color(0xFF0F766E)))
    )

    val filteredApps = if (searchQuery.isBlank()) {
        emptyList()
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

    // Default suggestions from available apps or fallback
    val suggestedApps = if (apps.isNotEmpty()) apps.take(3) else emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Search Bar Top Header (matching Image 1)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.8).sp,
                            fontSize = 32.sp
                        ),
                        color = TextPrimary
                    )

                    // User avatar indicator circle (matching "AS" badge on Image 1)
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userAvatarInitial,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Input Field matching iOS App Store style
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceSecondary,
                    border = BorderStroke(0.5.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "Games, Apps, Stories and More",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextTertiary,
                                    fontSize = 15.sp
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
                                    color = TextTertiary.copy(alpha = 0.25f),
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

        // 2. SUGGESTED SECTION (when query is empty, matching Image 1)
        if (searchQuery.isBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Suggested",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(0.5.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // If catalog has apps, display them as suggestions, else render realistic default suggested apps
                            val displaySuggestions = if (suggestedApps.isNotEmpty()) {
                                suggestedApps
                            } else {
                                listOf(
                                    AppItem(
                                        id = "sug-1",
                                        slug = "range-finder",
                                        name = "Golf Score Range Finder",
                                        category = "Utilities",
                                        developer = "Sports Engine",
                                        tagline = "Range Finder Handicap Score",
                                        iconPath = ""
                                    ),
                                    AppItem(
                                        id = "sug-2",
                                        slug = "vending-sorting",
                                        name = "Match 3: Vending Game",
                                        category = "Casual Games",
                                        developer = "Play Fun Studio",
                                        tagline = "Match 3: Vending Sorting Game",
                                        iconPath = ""
                                    ),
                                    AppItem(
                                        id = "sug-3",
                                        slug = "doll-house-fun",
                                        name = "Girl Game Doll House",
                                        category = "Kids",
                                        developer = "Creative Kids",
                                        tagline = "Girl Game Doll House Fun",
                                        iconPath = ""
                                    )
                                )
                            }

                            val colors = listOf(Color(0xFF3B82F6), Color(0xFFFACC15), Color(0xFFDB2777))

                            displaySuggestions.forEachIndexed { index, app ->
                                SuggestedAppRow(
                                    app = app,
                                    tintColor = colors[index % colors.size],
                                    hasAdBadge = index == 0,
                                    isInstalled = installedAppIds.contains(app.id),
                                    onAppClick = { onAppClick(app.slug) },
                                    onInstallClick = { onInstallClick(app.id) }
                                )
                                if (index < displaySuggestions.size - 1) {
                                    HorizontalDivider(
                                        color = BorderLight.copy(alpha = 0.6f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 68.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. BROWSE SECTION (matching Image 1 2-column colorful gradient cards)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Browse",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 2-Column Grid
                    val rows = browseCards.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        rows.forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { category ->
                                    BrowseCategoryCard(
                                        item = category,
                                        onClick = { onSearchQueryChange(category.queryTag) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 4. LIVE SEARCH RESULTS (matching Image 2)
            if (filteredApps.isEmpty()) {
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
            } else {
                itemsIndexed(filteredApps, key = { _, it -> it.id }) { index, app ->
                    SearchAppResultCard(
                        app = app,
                        isInstalled = installedAppIds.contains(app.id),
                        downloadProgress = downloadProgress[app.id],
                        onAppClick = { onAppClick(app.slug) },
                        onInstallClick = { onInstallClick(app.id) },
                        modifier = Modifier
                            .motionEntrance(index, delayPerIndexMs = 35)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Suggested App Row matching Image 1:
 * Squircle Icon, Title, Subtitle, "Ad" badge, "In-App Purchases" badge, "Get" button.
 */
@Composable
private fun SuggestedAppRow(
    app: AppItem,
    tintColor: Color,
    hasAdBadge: Boolean,
    isInstalled: Boolean,
    onAppClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressBounce(interactionSource, scaleDown = 0.98f)
            .clickable(interactionSource = interactionSource, indication = null) { onAppClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.iconPath.isNotBlank()) {
            AppIconImage(
                iconPath = app.iconPath,
                appName = app.name,
                size = 46.dp,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tintColor,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = app.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

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
                text = app.tagline.ifBlank { "${app.category} • ${app.developer}" },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (hasAdBadge) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Ad",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "In-App Purchases",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onInstallClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEEF2F6),
                contentColor = Color(0xFF2563EB)
            ),
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(32.dp).testTag("suggested_get_button_${app.slug}")
        ) {
            Text(
                text = if (isInstalled) "OPEN" else "Get",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                color = Color(0xFF2563EB)
            )
        }
    }
}

/**
 * Browse Category Card matching Image 1:
 * 2-column cards with smooth gradient background and bottom-left bold white text.
 */
@Composable
private fun BrowseCategoryCard(
    item: BrowseCategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = item.gradientColors.first()),
        modifier = modifier
            .height(76.dp)
            .pressBounce(interactionSource, scaleDown = 0.95f)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .testTag("browse_card_${item.title.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .liquidShimmer(durationMs = 2800)
                .background(Brush.linearGradient(item.gradientColors))
                .padding(14.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.2).sp
                )
            )
        }
    }
}

/**
 * App Result Card matching user reference image image_a8c2138e.png (Image 2):
 * Top Row: Squircle Icon, Title, Subtitle, Star Rating + count, GET button.
 * Middle: Summary description text.
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
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(0.5.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .pressBounce(interactionSource, scaleDown = 0.98f)
            .clickable(interactionSource = interactionSource, indication = null) { onAppClick() }
            .testTag("search_app_card_${app.slug}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Title, Subtitle, Star Rating, Action Pill
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
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Ad",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color(0xFF2563EB),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${app.rating} (${if (app.reviewsCount > 0) "${app.reviewsCount / 100}K" else "12K"})",
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

            Spacer(modifier = Modifier.height(10.dp))

            // Summary description snippet (matching image 2)
            Text(
                text = app.description.ifBlank { app.overview },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 3 Side-by-Side Portrait Screenshots (matching image 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shotsToDisplay = if (app.screenshots.isNotEmpty()) app.screenshots.take(3) else emptyList()

                for (i in 0 until 3) {
                    val shotUrl = if (i < shotsToDisplay.size) shotsToDisplay[i] else ""
                    RealisticScreenshotCard(
                        app = app,
                        shotUrl = shotUrl,
                        variant = i,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Realistic Screenshot Preview Card:
 * Displays real image if provided and loaded, with a rich smartphone UI layout fallback
 * so that screenshots are ALWAYS visible and never empty!
 */
@Composable
fun RealisticScreenshotCard(
    app: AppItem,
    shotUrl: String = "",
    variant: Int = 0,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (variant) {
        0 -> listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        1 -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
        else -> listOf(Color(0xFF064E3B), Color(0xFF065F46))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, BorderLight),
        modifier = modifier.height(175.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // High-fidelity phone UI screen rendered as background & fallback
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
                    // 1. Phone Status Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "9:41",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(5.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                            Box(modifier = Modifier.size(5.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                        }
                    }

                    // 2. Realistic In-App Interface Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = app.name.take(2).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (variant) {
                                0 -> "Home & Feed"
                                1 -> "Live Controls"
                                else -> "Verified Engine"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Interface widget preview
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(4.dp)
                                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(4.dp)
                                        .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    // 3. Phone Home bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    )
                }
            }

            // Real screenshot image loaded over the mockup if valid
            if (shotUrl.isNotBlank()) {
                val model = com.example.util.ImageHelper.buildImageModel(shotUrl) ?: shotUrl
                AsyncImage(
                    model = model,
                    contentDescription = "Screenshot Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
