package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppItem
import com.example.data.models.StoreCategories
import com.example.ui.components.AppActionPill
import com.example.ui.components.AppIconImage
import com.example.ui.components.LiquidGlassSearchBar
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import com.example.ui.components.motionEntrance
import com.example.ui.components.pressBounce
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import com.example.ui.theme.AppleBlue

@Composable
fun AppsScreen(
    apps: List<AppItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    installedAppIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    userAvatarInitial: String = "AS",
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories = StoreCategories.APP_CATEGORIES

    val filteredApps = apps.filter {
        !it.isTopGame && (selectedCategory == "All" || it.category.contains(selectedCategory, ignoreCase = true) || selectedCategory.contains(it.category, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("apps_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
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
                    Text(
                        text = "Apps",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp,
                            fontSize = 34.sp
                        ),
                        color = TextPrimary
                    )

                    Surface(
                        shape = CircleShape,
                        color = AppleBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, AppleBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .pressBounce(remember { MutableInteractionSource() })
                            .clickable { onProfileClick() }
                            .testTag("apps_profile_avatar_button")
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

                Spacer(modifier = Modifier.height(12.dp))

                LiquidGlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Search apps & utilities..."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips (Swipeable / Scrollable horizontally across categories)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color.Black else SurfaceSecondary,
                            border = BorderStroke(1.dp, if (isSelected) Color.Black else BorderLight),
                            modifier = Modifier
                                .clickable { onCategorySelect(category) }
                                .testTag("category_chip_$category")
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (filteredApps.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "No applications available" else "No apps in '$selectedCategory'",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin can upload real APK files directly in the Admin Panel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            itemsIndexed(filteredApps, key = { _, it -> it.id }) { index, app ->
                AppRowItem(
                    app = app,
                    isInstalled = installedAppIds.contains(app.id),
                    downloadProgress = downloadProgress[app.id],
                    onAppClick = { onAppClick(app.slug) },
                    onInstallClick = { onInstallClick(app.id) },
                    modifier = Modifier
                        .motionEntrance(index, delayPerIndexMs = 28)
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AppRowItem(
    app: AppItem,
    isInstalled: Boolean,
    downloadProgress: Float?,
    onAppClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight),
        modifier = modifier
            .fillMaxWidth()
            .pressBounce(interactionSource, scaleDown = 0.98f)
            .clickable(interactionSource = interactionSource, indication = null) { onAppClick() }
            .testTag("app_row_${app.slug}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconImage(
                iconUrl = app.iconUrl,
                fallbackText = app.iconFallbackText,
                accentColor = app.iconColorHex,
                size = 56.dp,
                cornerRadius = 14.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = app.tagline.ifBlank { app.developer },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${app.rating}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.sizeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    }
}
