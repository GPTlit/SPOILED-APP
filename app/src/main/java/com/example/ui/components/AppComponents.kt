package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.AppItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleBlueSoft
import com.example.ui.theme.BorderLight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

// ----------------------------------------------------
// 1. BRAND WORDMARK ("Spoiled Store")
// ----------------------------------------------------
@Composable
fun SpoiledStoreWordmark(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("spoiled_store_wordmark"),
        verticalAlignment = Alignment.Bottom
    ) {
        // "Spoiled" with subtle liquid drop detail
        Box {
            Text(
                text = "Spoiled",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                ),
                color = TextPrimary
            )
            // Tiny liquid highlight over the "Spo"
            Box(
                modifier = Modifier
                    .padding(start = 22.dp, top = 2.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(AppleBlue.copy(alpha = 0.6f))
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // "Store" in lighter weight
        Text(
            text = "Store",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.5).sp
            ),
            color = TextPrimary
        )
    }
}

// ----------------------------------------------------
// 2. LIQUID GLASS SEARCH BAR
// ----------------------------------------------------
@Composable
fun LiquidGlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search apps, games, stories and more...",
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = GlassWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("liquid_glass_search_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextTertiary,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input"),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 3. APP ICON WITH SHADOW AND FALLBACK
// ----------------------------------------------------
@Composable
fun AppIconImage(
    iconUrl: String = "",
    fallbackText: String = "",
    accentColor: Long = 0xFF6366F1,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    cornerRadius: Dp = 16.dp,
    iconPath: String = "",
    appName: String = "",
    shape: androidx.compose.ui.graphics.Shape? = null
) {
    val effectiveUrl = iconPath.ifBlank { iconUrl }
    val effectiveText = fallbackText.ifBlank { appName.take(2).uppercase().ifBlank { "SS" } }
    val effectiveShape = shape ?: RoundedCornerShape(cornerRadius)
    val imageModel = com.example.util.ImageHelper.buildImageModel(effectiveUrl)
    Surface(
        shape = effectiveShape,
        color = Color(accentColor).copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderLight),
        shadowElevation = 1.dp,
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(effectiveShape)
                )
            } else {
                Text(
                    text = effectiveText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(accentColor)
                    )
                )
            }
        }
    }
}

// ----------------------------------------------------
// 4. ACTION PILL (GET / OPEN / DOWNLOADING)
// ----------------------------------------------------
@Composable
fun AppActionPill(
    isInstalled: Boolean,
    downloadProgress: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    when {
        downloadProgress != null -> {
            val progressAnim by animateFloatAsState(targetValue = downloadProgress, label = "p")
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppleBlueSoft,
                modifier = modifier
                    .height(28.dp)
                    .width(62.dp)
                    .pressBounce(interactionSource, scaleDown = 0.94f)
                    .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier.size(16.dp),
                        color = AppleBlue,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        isInstalled -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppleBlueSoft,
                modifier = modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .pressBounce(interactionSource, scaleDown = 0.94f)
                    .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OPEN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = AppleBlue
                    )
                }
            }
        }
        else -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppleBlueSoft,
                modifier = modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .pressBounce(interactionSource, scaleDown = 0.94f)
                    .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GET",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = AppleBlue
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. COMPACT HORIZONTAL APP CARD (Top Apps & Games)
// ----------------------------------------------------
@Composable
fun CompactAppItem(
    app: AppItem,
    isInstalled: Boolean,
    downloadProgress: Float?,
    onAppClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Column(
        modifier = modifier
            .width(80.dp)
            .pressBounce(interactionSource, scaleDown = 0.95f)
            .clickable(interactionSource = interactionSource, indication = null) { onAppClick() }
            .testTag("compact_app_${app.slug}"),
        horizontalAlignment = Alignment.Start
    ) {
        AppIconImage(
            iconUrl = app.iconUrl,
            fallbackText = app.iconFallbackText,
            accentColor = app.iconColorHex,
            size = 68.dp,
            cornerRadius = 16.dp
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = app.category,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format("%.1f", app.rating),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
            }

            Text(
                text = if (isInstalled) "Open" else "Get",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AppleBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onInstallClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ----------------------------------------------------
// 6. SECTION HEADER WITH "SEE ALL"
// ----------------------------------------------------
@Composable
fun AppStoreSectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = TextPrimary
        )

        Text(
            text = "See All",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                color = AppleBlue
            ),
            modifier = Modifier
                .clickable { onSeeAllClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp)
        )
    }
}

// ----------------------------------------------------
// 7. PROFILE LIST ROW (Purchased, Subscriptions, etc.)
// ----------------------------------------------------
@Composable
fun ProfileNavigationRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingBadge: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                color = TextPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailingBadge != null) {
                Text(
                    text = trailingBadge,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000_000 -> String.format("%.1fB", count / 1_000_000_000f)
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format("%.1fK", count / 1_000f)
        else -> count.toString()
    }
}

