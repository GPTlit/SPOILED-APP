package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * CSS-inspired motion graphics and entrance animations for cards, modals, and list items.
 */

/**
 * Staggered entrance animation that glides and scales up smoothly when the item mounts.
 * Equivalent to CSS:
 * animation: cardEntrance 380ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
 */
@Composable
fun Modifier.motionEntrance(
    index: Int = 0,
    delayPerIndexMs: Int = 30,
    offsetY: Dp = 22.dp,
    initialScale: Float = 0.96f
): Modifier {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val delayTime = (index * delayPerIndexMs).toLong().coerceIn(0L, 350L)
        if (delayTime > 0L) {
            delay(delayTime)
        }
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "entranceAlpha"
    )

    val density = LocalDensity.current
    val offsetYPx = with(density) { offsetY.toPx() }
    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else offsetYPx,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 320f
        ),
        label = "entranceTranslationY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else initialScale,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 350f
        ),
        label = "entranceScale"
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        this.scaleX = scale
        this.scaleY = scale
    }
}

/**
 * Interactive press bounce feedback with spring physics (Apple App Store tactile bounce).
 * Scales down slightly on press, then springs back with damping.
 */
@Composable
fun Modifier.pressBounce(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.96f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressBounceScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * CSS-like moving light shimmer effect for cards, badges, and headers.
 * Equivalent to CSS linear-gradient sweep keyframe.
 */
@Composable
fun Modifier.liquidShimmer(
    shimmerColor: Color = Color.White.copy(alpha = 0.14f),
    durationMs: Int = 2400
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidShimmerTransition")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = -350f,
        targetValue = 950f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquidShimmerTranslate"
    )

    return this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                shimmerColor,
                Color.Transparent
            ),
            start = Offset(translateAnim, 0f),
            end = Offset(translateAnim + 180f, size.height)
        )
        drawRect(brush = brush)
    }
}

/**
 * Dynamic breathing pulse for live indicators, notifications, and badges.
 */
@Composable
fun Modifier.pulseGlow(
    minScale: Float = 0.96f,
    maxScale: Float = 1.04f,
    durationMs: Int = 1300
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlowTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Animated container for Modal Dialogs and sheets.
 * Provides a fluid scale-up and slide-in entrance with Apple-like spring physics.
 */
@Composable
fun AnimatedModalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = 380f
        ),
        label = "modalScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "modalAlpha"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 48f,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 360f
        ),
        label = "modalTranslationY"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        content()
    }
}
