package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppItem
import com.example.ui.components.AppActionPill
import com.example.ui.components.AppIconImage
import com.example.ui.components.AppStoreSectionHeader
import com.example.ui.components.CompactAppItem
import com.example.ui.components.liquidShimmer
import com.example.ui.components.motionEntrance
import com.example.ui.components.pressBounce
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ArcadeScreen(
    apps: List<AppItem>,
    installedAppIds: Set<String>,
    downloadProgress: Map<String, Float>,
    onAppClick: (String) -> Unit,
    onInstallClick: (String) -> Unit,
    userAvatarInitial: String = "AS",
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val arcadeGames = apps.filter {
        it.isTopGame || it.category.contains("Game", ignoreCase = true) || it.category.contains("Racing", ignoreCase = true)
    }.ifEmpty { apps }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("arcade_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Arcade Header
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
                        text = "Arcade",
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
                            .testTag("arcade_profile_avatar_button")
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

        // 2. Apple Arcade Hero Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .motionEntrance(0)
                    .liquidShimmer(durationMs = 2800)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AppleBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "APPLE ARCADE PASSPORT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 11.sp
                                ),
                                color = AppleBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Unlimited Play.\nOver 200+ Games.",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Zero ads. No in-app purchases. All games included with full controller & haptics support.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        val buttonInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = {
                                if (arcadeGames.isNotEmpty()) {
                                    onAppClick(arcadeGames.first().slug)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppleBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .pressBounce(buttonInteraction)
                                .testTag("arcade_start_trial_button")
                        ) {
                            Text(
                                text = "Try It Free — 1 Month",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Featured Arcade Games
        item {
            AppStoreSectionHeader(
                title = "Featured Arcade Titles",
                onSeeAllClick = {}
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                itemsIndexed(arcadeGames.take(6), key = { _, it -> "arc_${it.id}" }) { index, game ->
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

        // 4. All Arcade Games List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp, bottom = 6.dp)
            ) {
                Text(
                    text = "All Included with Arcade",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Download and play instantly with zero interruptions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        itemsIndexed(arcadeGames, key = { _, it -> "all_arc_${it.id}" }) { index, game ->
            val interaction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressBounce(interaction, scaleDown = 0.98f)
                    .clickable(interactionSource = interaction, indication = null) { onAppClick(game.slug) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconImage(
                    iconPath = game.iconPath,
                    appName = game.name,
                    size = 54.dp,
                    shape = RoundedCornerShape(13.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = game.tagline.ifBlank { "${game.category} • ${game.developer}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${game.rating} (${game.reviewsCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                AppActionPill(
                    isInstalled = installedAppIds.contains(game.id),
                    downloadProgress = downloadProgress[game.id],
                    price = game.price,
                    isSubscription = false,
                    onClick = { onInstallClick(game.id) }
                )
            }
        }
    }
}
