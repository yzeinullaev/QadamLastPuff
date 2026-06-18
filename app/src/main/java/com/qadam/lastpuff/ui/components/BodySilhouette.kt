package com.qadam.lastpuff.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.qadam.lastpuff.domain.model.OrganInfo

private val BodyFill = Color(0xFF1E3A5F)
private val BodyFillLight = Color(0xFF2A5080)
private val BodyOutline = Color(0xFF4A7AB5)

data class OrganGlow(
    val id: String,
    val centerFraction: Offset,
    val color: Color,
    val radiusFraction: Float = 0.06f
)

private val organPositions = listOf(
    OrganGlow("brain", Offset(0.50f, 0.10f), Color(0xFFBA68C8), 0.055f),
    OrganGlow("smell", Offset(0.50f, 0.145f), Color(0xFFFFB74D), 0.04f),
    OrganGlow("lungs", Offset(0.50f, 0.30f), Color(0xFF64B5F6), 0.09f),
    OrganGlow("heart", Offset(0.44f, 0.28f), Color(0xFFE57373), 0.06f),
    OrganGlow("blood", Offset(0.54f, 0.36f), Color(0xFFEF5350), 0.05f)
)

@Composable
fun BodySilhouette(
    organs: List<OrganInfo>,
    modifier: Modifier = Modifier,
    waveProgress: Float? = null,
    highlightHeartLungs: Boolean = false,
    glowColor: Color = Color(0xFF4CAF50)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bodyPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        drawHumanSilhouette(cx, w, h)

        waveProgress?.let { progress ->
            drawRectangularWave(cx, w, h, h * progress, glowColor)
        }

        organs.forEach { organ ->
            val pos = organPositions.find { it.id == organ.id } ?: return@forEach
            val center = Offset(w * pos.centerFraction.x, h * pos.centerFraction.y)
            val intensity = (organ.value / 100f).coerceIn(0.2f, 1f)
            val extraGlow = when {
                highlightHeartLungs && organ.id in listOf("heart", "lungs") -> 1.5f
                else -> 1f
            }
            val radius = w * pos.radiusFraction * intensity * pulse * extraGlow
            val color = Color(organ.glowColor)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.75f * intensity),
                        color.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 3f
                ),
                radius = radius * 3f,
                center = center
            )
            drawCircle(
                color = color.copy(alpha = 0.95f),
                radius = radius * 0.45f,
                center = center
            )
        }

        if (highlightHeartLungs) {
            listOf("heart", "lungs").forEach { id ->
                val pos = organPositions.find { it.id == id } ?: return@forEach
                val center = Offset(w * pos.centerFraction.x, h * pos.centerFraction.y)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.55f),
                            glowColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = w * 0.14f
                    ),
                    radius = w * 0.14f,
                    center = center
                )
            }
        }
    }
}

private fun DrawScope.drawHumanSilhouette(cx: Float, w: Float, h: Float) {
    val bw = w * 0.50f
    val bh = h * 0.90f
    val top = h * 0.02f
    val sx = bw / 100f
    val sy = bh / 380f

    fun px(x: Float) = cx + (x - 50f) * sx
    fun py(y: Float) = top + y * sy

    val body = Path().apply {
        // Start top of head, go clockwise
        moveTo(px(50f), py(0f))
        cubicTo(px(66f), py(0f), px(74f), py(10f), px(74f), py(24f))
        cubicTo(px(74f), py(38f), px(66f), py(46f), px(50f), py(46f))
        cubicTo(px(34f), py(46f), px(26f), py(38f), px(26f), py(24f))
        cubicTo(px(26f), py(10f), px(34f), py(0f), px(50f), py(0f))

        // Right shoulder & arm
        moveTo(px(54f), py(46f))
        lineTo(px(54f), py(52f))
        cubicTo(px(68f), py(54f), px(82f), py(62f), px(88f), py(78f))
        cubicTo(px(92f), py(88f), px(90f), py(98f), px(84f), py(104f))
        cubicTo(px(78f), py(108f), px(72f), py(102f), px(70f), py(92f))
        cubicTo(px(68f), py(82f), px(66f), py(70f), px(64f), py(58f))
        lineTo(px(62f), py(98f))
        cubicTo(px(60f), py(140f), px(58f), py(182f), px(57f), py(224f))
        lineTo(px(57f), py(290f))
        cubicTo(px(57f), py(320f), px(59f), py(348f), px(63f), py(368f))
        lineTo(px(54f), py(372f))
        cubicTo(px(48f), py(374f), px(46f), py(368f), px(46f), py(358f))
        lineTo(px(46f), py(290f))
        lineTo(px(46f), py(224f))
        lineTo(px(50f), py(200f))
        lineTo(px(54f), py(224f))
        lineTo(px(54f), py(290f))
        lineTo(px(54f), py(358f))
        cubicTo(px(54f), py(368f), px(52f), py(374f), px(46f), py(372f))
        lineTo(px(37f), py(368f))
        cubicTo(px(41f), py(348f), px(43f), py(320f), px(43f), py(290f))
        lineTo(px(43f), py(224f))
        cubicTo(px(42f), py(182f), px(40f), py(140f), px(38f), py(98f))
        lineTo(px(36f), py(58f))
        cubicTo(px(34f), py(70f), px(32f), py(82f), px(30f), py(92f))
        cubicTo(px(28f), py(102f), px(22f), py(108f), px(16f), py(104f))
        cubicTo(px(10f), py(98f), px(8f), py(88f), px(12f), py(78f))
        cubicTo(px(18f), py(62f), px(32f), py(54f), px(46f), py(52f))
        lineTo(px(46f), py(46f))
        close()
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                BodyFillLight.copy(alpha = 0.35f),
                BodyFill.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = Offset(cx, py(185f)),
            radius = bw * 0.95f
        ),
        radius = bw * 0.95f,
        center = Offset(cx, py(185f))
    )

    drawPath(
        body,
        brush = Brush.verticalGradient(
            colors = listOf(BodyFillLight, BodyFill, BodyFill.copy(alpha = 0.92f)),
            startY = py(0f),
            endY = py(380f)
        )
    )
    drawPath(body, BodyOutline.copy(alpha = 0.5f), style = Stroke(width = 1.5f, cap = StrokeCap.Round))

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF3D6A9E).copy(alpha = 0.35f), Color.Transparent),
            center = Offset(cx, py(120f)),
            radius = bw * 0.3f
        ),
        topLeft = Offset(cx - bw * 0.32f, py(88f)),
        size = Size(bw * 0.64f, bh * 0.2f)
    )
}

private fun DrawScope.drawRectangularWave(cx: Float, w: Float, h: Float, waveY: Float, color: Color) {
    if (waveY < 0f || waveY > h) return
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.18f),
                Color.Transparent
            ),
            startY = waveY - h * 0.14f,
            endY = waveY + h * 0.05f
        ),
        topLeft = Offset(0f, waveY - h * 0.14f),
        size = Size(w, h * 0.19f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
            center = Offset(cx, waveY),
            radius = w * 0.45f
        ),
        radius = w * 0.45f,
        center = Offset(cx, waveY)
    )
}
