package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface

@Composable
fun HudGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = HudBorder,
    glowColor: Color = ArcCyanPrimary.copy(alpha = 0.12f),
    shape: Shape = RoundedCornerShape(14.dp),
    cornerRadiusDp: Dp = 14.dp,
    showTechCorners: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                if (showTechCorners) {
                    val lineLen = 14.dp.toPx()
                    val strokeW = 2.dp.toPx()
                    val c = borderColor.copy(alpha = 0.85f)

                    // Top Left Corner Bracket
                    drawLine(c, Offset(0f, 0f), Offset(lineLen, 0f), strokeW)
                    drawLine(c, Offset(0f, 0f), Offset(0f, lineLen), strokeW)

                    // Top Right Corner Bracket
                    drawLine(c, Offset(size.width, 0f), Offset(size.width - lineLen, 0f), strokeW)
                    drawLine(c, Offset(size.width, 0f), Offset(size.width, lineLen), strokeW)

                    // Bottom Left Corner Bracket
                    drawLine(c, Offset(0f, size.height), Offset(lineLen, size.height), strokeW)
                    drawLine(c, Offset(0f, size.height), Offset(0f, size.height - lineLen), strokeW)

                    // Bottom Right Corner Bracket
                    drawLine(c, Offset(size.width, size.height), Offset(size.width - lineLen, size.height), strokeW)
                    drawLine(c, Offset(size.width, size.height), Offset(size.width, size.height - lineLen), strokeW)
                }
            },
        shape = shape,
        color = HudSurface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glowColor,
                            Color.Transparent,
                            HudBackground.copy(alpha = 0.35f)
                        )
                    )
                )
                .padding(14.dp),
            content = content
        )
    }
}
