package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.IronGold
import com.example.ui.theme.IronGoldGlow
import com.example.ui.theme.IronRedGlow
import com.example.viewmodel.JarvisCoreState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorView(
    coreState: JarvisCoreState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (coreState == JarvisCoreState.PROCESSING) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRing"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (coreState == JarvisCoreState.PROCESSING) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRing"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (coreState) {
                    JarvisCoreState.LISTENING -> 600
                    JarvisCoreState.SPEAKING -> 800
                    JarvisCoreState.PROCESSING -> 400
                    else -> 2000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    val primaryColor = when (coreState) {
        JarvisCoreState.LISTENING -> IronRedGlow
        JarvisCoreState.SPEAKING -> IronGoldGlow
        JarvisCoreState.PROCESSING -> IronGold
        JarvisCoreState.EXECUTING -> Color(0xFF10B981)
        JarvisCoreState.STANDBY -> ArcCyanPrimary
    }

    val glowColor = when (coreState) {
        JarvisCoreState.LISTENING -> Color(0xFFFF2A4B)
        JarvisCoreState.SPEAKING -> IronGold
        JarvisCoreState.PROCESSING -> IronGoldGlow
        JarvisCoreState.EXECUTING -> Color(0xFF34D399)
        JarvisCoreState.STANDBY -> ArcCyanGlow
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 120.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f - 10f

            // 1. Ambient Glow Field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.28f * pulseScale), Color.Transparent),
                    center = center,
                    radius = maxRadius * 1.1f
                ),
                radius = maxRadius * 1.1f,
                center = center
            )

            // 2. Outermost Segmented Ring
            rotate(outerRotation, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.35f),
                    radius = maxRadius,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 15f, 10f, 15f), 0f)
                    )
                )

                // Outer telemetry notches (12 tick marks)
                for (i in 0 until 12) {
                    val angle = (i * 30.0 * Math.PI / 180.0)
                    val startX = center.x + ((maxRadius - 12f) * cos(angle)).toFloat()
                    val startY = center.y + ((maxRadius - 12f) * sin(angle)).toFloat()
                    val endX = center.x + (maxRadius * cos(angle)).toFloat()
                    val endY = center.y + (maxRadius * sin(angle)).toFloat()
                    drawLine(
                        color = glowColor.copy(alpha = 0.7f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Middle Core Ring (Opposite Rotation)
            rotate(innerRotation, pivot = center) {
                val midRadius = maxRadius * 0.76f
                drawCircle(
                    color = glowColor.copy(alpha = 0.45f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f), 0f)
                    )
                )

                // 8 Arc segments (Iron Man reactor coils)
                for (i in 0 until 8) {
                    val angle = (i * 45.0 * Math.PI / 180.0)
                    val r1 = midRadius - 16f
                    val r2 = midRadius + 8f
                    val p1 = Offset(center.x + (r1 * cos(angle)).toFloat(), center.y + (r1 * sin(angle)).toFloat())
                    val p2 = Offset(center.x + (r2 * cos(angle)).toFloat(), center.y + (r2 * sin(angle)).toFloat())
                    drawLine(
                        color = primaryColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Square
                    )
                }
            }

            // 4. Inner Glowing Power Core
            val coreRadius = maxRadius * 0.45f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        glowColor,
                        primaryColor,
                        primaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.25f
                ),
                radius = coreRadius,
                center = center
            )

            // Inner Metallic Core Ring
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = coreRadius * 0.55f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Triangular / Tri-point Core Emissary (Mark 6 style center)
            val triangleRadius = coreRadius * 0.4f
            for (i in 0 until 3) {
                val angle = ((i * 120.0 - 90.0) * Math.PI / 180.0)
                val x = center.x + (triangleRadius * cos(angle)).toFloat()
                val y = center.y + (triangleRadius * sin(angle)).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
