package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.StoreNavigationTab
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight

/**
 * Authentic Apple App Store Bottom Navigation Bar matching user reference photos.
 * 5 Tabs: Today, Games, Apps, Arcade, Search.
 * Translucent frosted background, hairline top border, Apple Blue active tint, 0 pill containers.
 */
@Composable
fun AppStoreBottomBar(
    currentTab: StoreNavigationTab,
    onTabSelected: (StoreNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        StoreNavigationTab.HOME to "Today",
        StoreNavigationTab.GAMES to "Games",
        StoreNavigationTab.APPS to "Apps",
        StoreNavigationTab.ARCADE to "Arcade",
        StoreNavigationTab.SEARCH to "Search"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("spoiled_bottom_navigation")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = BorderLight
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEach { (tab, label) ->
                    val isSelected = currentTab == tab
                    val tabInteraction = remember { MutableInteractionSource() }
                    val activeColor = AppleBlue
                    val inactiveColor = Color(0xFF8E8E93)
                    val iconColor = if (isSelected) activeColor else inactiveColor

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .pressBounce(tabInteraction, scaleDown = 0.92f)
                            .clickable(
                                interactionSource = tabInteraction,
                                indication = null
                            ) {
                                onTabSelected(tab)
                            }
                            .testTag("tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (tab) {
                                StoreNavigationTab.HOME -> TodayTabIcon(selected = isSelected, color = iconColor)
                                StoreNavigationTab.GAMES -> GamesRocketIcon(selected = isSelected, color = iconColor)
                                StoreNavigationTab.APPS -> AppsLayersIcon(selected = isSelected, color = iconColor)
                                StoreNavigationTab.ARCADE -> ArcadeJoystickIcon(selected = isSelected, color = iconColor)
                                StoreNavigationTab.SEARCH -> SearchMagnifierIcon(selected = isSelected, color = iconColor)
                                else -> TodayTabIcon(selected = isSelected, color = iconColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = iconColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. Today Tab Icon: Daily document / article card with top header and content lines
 */
@Composable
fun TodayTabIcon(selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        val cardLeft = w * 0.16f
        val cardTop = h * 0.12f
        val cardWidth = w * 0.68f
        val cardHeight = h * 0.76f
        val corner = 3.dp.toPx()

        // Outer rounded card
        drawRoundRect(
            color = color,
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = if (selected) Stroke(width = strokeWidth) else Stroke(width = strokeWidth)
        )

        // Top header strip
        val headerH = cardHeight * 0.28f
        if (selected) {
            drawRoundRect(
                color = color,
                topLeft = Offset(cardLeft, cardTop),
                size = Size(cardWidth, headerH),
                cornerRadius = CornerRadius(corner, corner),
                style = Fill
            )
        } else {
            drawLine(
                color = color,
                start = Offset(cardLeft, cardTop + headerH),
                end = Offset(cardLeft + cardWidth, cardTop + headerH),
                strokeWidth = strokeWidth
            )
        }

        // Two horizontal body lines
        val line1Y = cardTop + headerH + (cardHeight - headerH) * 0.35f
        val line2Y = cardTop + headerH + (cardHeight - headerH) * 0.68f
        val linePadding = cardWidth * 0.2f

        drawLine(
            color = color,
            start = Offset(cardLeft + linePadding, line1Y),
            end = Offset(cardLeft + cardWidth - linePadding, line1Y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(cardLeft + linePadding, line2Y),
            end = Offset(cardLeft + cardWidth * 0.65f, line2Y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 2. Games Tab Icon: Angled Rocket (matching reference photo 2)
 */
@Composable
fun GamesRocketIcon(selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Rocket body pointing diagonally up-right
        val path = Path().apply {
            // Nose tip
            moveTo(w * 0.82f, h * 0.18f)
            // Right curve to fin
            cubicTo(w * 0.82f, h * 0.40f, w * 0.68f, h * 0.65f, w * 0.48f, h * 0.74f)
            // Right fin flare
            lineTo(w * 0.44f, h * 0.86f)
            lineTo(w * 0.34f, h * 0.82f)
            // Rocket nozzle base
            lineTo(w * 0.28f, h * 0.72f)
            // Left fin flare
            lineTo(w * 0.18f, h * 0.66f)
            lineTo(w * 0.14f, h * 0.56f)
            lineTo(w * 0.26f, h * 0.52f)
            // Left curve back to nose
            cubicTo(w * 0.35f, h * 0.32f, w * 0.60f, h * 0.18f, w * 0.82f, h * 0.18f)
            close()
        }

        drawPath(
            path = path,
            color = color,
            style = if (selected) Fill else Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )

        // Portal window
        val portalColor = if (selected) Color.White else color
        drawCircle(
            color = portalColor,
            radius = 2.4.dp.toPx(),
            center = Offset(w * 0.58f, h * 0.42f),
            style = if (selected) Fill else Stroke(width = strokeWidth)
        )
    }
}

/**
 * 3. Apps Tab Icon: 3 Stacked Isometric Layers (matching reference photo 2)
 */
@Composable
fun AppsLayersIcon(selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Top rhombus
        val topRhombus = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.84f, h * 0.32f)
            lineTo(w * 0.50f, h * 0.48f)
            lineTo(w * 0.16f, h * 0.32f)
            close()
        }

        drawPath(
            path = topRhombus,
            color = color,
            style = if (selected) Fill else Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )

        // Middle layer chevron
        val midChevron = Path().apply {
            moveTo(w * 0.16f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.64f)
            lineTo(w * 0.84f, h * 0.48f)
        }
        drawPath(
            path = midChevron,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Bottom layer chevron
        val botChevron = Path().apply {
            moveTo(w * 0.16f, h * 0.64f)
            lineTo(w * 0.50f, h * 0.80f)
            lineTo(w * 0.84f, h * 0.64f)
        }
        drawPath(
            path = botChevron,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * 4. Arcade Tab Icon: Arcade Joystick Controller (matching reference photo 2)
 */
@Composable
fun ArcadeJoystickIcon(selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Console base
        val baseLeft = w * 0.14f
        val baseTop = h * 0.62f
        val baseWidth = w * 0.72f
        val baseHeight = h * 0.24f
        val baseCorner = 3.dp.toPx()

        drawRoundRect(
            color = color,
            topLeft = Offset(baseLeft, baseTop),
            size = Size(baseWidth, baseHeight),
            cornerRadius = CornerRadius(baseCorner, baseCorner),
            style = if (selected) Fill else Stroke(width = strokeWidth)
        )

        // 2 buttons on right side of console
        val buttonColor = if (selected) Color.White else color
        drawCircle(
            color = buttonColor,
            radius = 1.8.dp.toPx(),
            center = Offset(w * 0.62f, h * 0.74f),
            style = Fill
        )
        drawCircle(
            color = buttonColor,
            radius = 1.8.dp.toPx(),
            center = Offset(w * 0.74f, h * 0.74f),
            style = Fill
        )

        // Joystick shaft
        drawLine(
            color = color,
            start = Offset(w * 0.36f, baseTop),
            end = Offset(w * 0.36f, h * 0.30f),
            strokeWidth = strokeWidth * 1.2f,
            cap = StrokeCap.Round
        )

        // Joystick ball knob
        drawCircle(
            color = color,
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.36f, h * 0.24f),
            style = Fill
        )
    }
}

/**
 * 5. Search Tab Icon: Classic Magnifying Glass
 */
@Composable
fun SearchMagnifierIcon(selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        val glassCenter = Offset(w * 0.44f, h * 0.44f)
        val glassRadius = w * 0.28f

        drawCircle(
            color = color,
            radius = glassRadius,
            center = glassCenter,
            style = if (selected) Stroke(width = strokeWidth * 1.2f) else Stroke(width = strokeWidth)
        )

        // Handle
        val handleStart = Offset(
            x = glassCenter.x + glassRadius * 0.707f,
            y = glassCenter.y + glassRadius * 0.707f
        )
        val handleEnd = Offset(w * 0.84f, h * 0.84f)

        drawLine(
            color = color,
            start = handleStart,
            end = handleEnd,
            strokeWidth = strokeWidth * 1.3f,
            cap = StrokeCap.Round
        )
    }
}
