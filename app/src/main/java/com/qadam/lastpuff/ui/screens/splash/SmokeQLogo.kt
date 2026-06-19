package com.qadam.lastpuff.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val SplashBackground = Color(0xFF0A120E)
private val SmokeBright = Color(0xFFB8F5D4)
private val SmokeMid = Color(0xFF6BCF9A)
private val SmokeDim = Color(0xFF3A8F62)

private data class SmokeParticle(
    val start: Offset,
    val target: Offset,
    val delay: Float,
    val sizeFactor: Float,
    val drift: Float
)

@Composable
fun SmokeQLogo(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    animated: Boolean = true,
    showSmoke: Boolean = true,
    glowPulse: Boolean = false,
    onAnimationProgress: (Float) -> Unit = {}
) {
    val progress = remember { Animatable(0f) }
    val smokeAlpha = remember { Animatable(1f) }
    val particles = remember { buildSmokeParticles(24) }
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(animated) {
        if (animated) {
            progress.snapTo(0f)
            smokeAlpha.snapTo(1f)
            progress.animateTo(1f, tween(2500, easing = FastOutSlowInEasing))
            smokeAlpha.animateTo(0f, tween(900, easing = FastOutSlowInEasing))
        } else {
            progress.snapTo(1f)
            smokeAlpha.snapTo(0f)
        }
    }

    LaunchedEffect(progress.value, smokeAlpha.value) {
        onAnimationProgress(progress.value)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.5f
            val radius = w * 0.22f
            val form = progress.value
            val glowScale = if (glowPulse) pulse else 1f

            if (showSmoke && smokeAlpha.value > 0.01f) {
                particles.forEach { particle ->
                    drawSmokeParticle(particle, form, w, h, smokeAlpha.value)
                }
            }

            if (form > 0.45f) {
                val qAlpha = ((form - 0.45f) / 0.55f).coerceIn(0f, 1f)
                drawQLetter(cx, cy, radius * glowScale, qAlpha)
            }
        }
    }
}

@Composable
fun GlowingQLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    SmokeQLogo(
        modifier = modifier,
        size = size,
        animated = false,
        showSmoke = false,
        glowPulse = true
    )
}

private fun buildSmokeParticles(count: Int): List<SmokeParticle> {
    val random = Random(17)
    val targets = buildQTargets(count)
    return targets.mapIndexed { index, target ->
        val scatterAngle = random.nextFloat() * 2f * PI.toFloat()
        val scatterDist = 0.1f + random.nextFloat() * 0.2f
        val start = Offset(
            x = 0.5f + cos(scatterAngle) * scatterDist,
            y = 0.72f + sin(scatterAngle) * scatterDist * 0.5f
        )
        SmokeParticle(
            start = start,
            target = target,
            delay = (index % 4) * 0.05f + random.nextFloat() * 0.1f,
            sizeFactor = 0.7f + random.nextFloat() * 0.6f,
            drift = random.nextFloat() * 2f * PI.toFloat()
        )
    }
}

private fun buildQTargets(count: Int): List<Offset> {
    val cx = 0.5f
    val cy = 0.5f
    val r = 0.22f
    val ringCount = (count * 0.75f).toInt().coerceAtLeast(1)
    val tailCount = count - ringCount
    val points = mutableListOf<Offset>()

    for (i in 0 until ringCount) {
        val t = i / (ringCount - 1).coerceAtLeast(1).toFloat()
        val angle = lerp(2.35f, -0.25f, t)
        points.add(Offset(cx + cos(angle) * r, cy + sin(angle) * r))
    }
    for (i in 0 until tailCount) {
        val t = if (tailCount <= 1) 1f else i / (tailCount - 1).toFloat()
        points.add(Offset(lerp(0.58f, 0.74f, t), lerp(0.58f, 0.82f, t)))
    }
    return points
}

private fun DrawScope.drawSmokeParticle(
    particle: SmokeParticle,
    form: Float,
    w: Float,
    h: Float,
    smokeAlpha: Float
) {
    val localT = ((form - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
    val eased = FastOutSlowInEasing.transform(localT)

    val start = Offset(particle.start.x * w, particle.start.y * h)
    val target = Offset(particle.target.x * w, particle.target.y * h)
    val pos = Offset(
        x = lerp(start.x, target.x, eased),
        y = lerp(start.y, target.y, eased)
    )

    val spreadScale = lerp(2f, 0.7f, eased)
    val alpha = lerp(0.1f, 0.75f, eased) * smokeAlpha
    val baseRadius = w * 0.04f * particle.sizeFactor * spreadScale

    drawSmokePuff(pos, baseRadius * 1.6f, alpha * 0.3f, SmokeDim)
    drawSmokePuff(pos, baseRadius, alpha * 0.55f, SmokeMid)
    drawSmokePuff(pos, baseRadius * 0.55f, alpha * 0.85f, SmokeBright)
}

private fun DrawScope.drawSmokePuff(center: Offset, radius: Float, alpha: Float, color: Color) {
    if (alpha <= 0.01f) return
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawQLetter(cx: Float, cy: Float, radius: Float, alpha: Float) {
    val path = Path().apply { addQArc(cx, cy, radius) }
    drawPath(
        path,
        Brush.linearGradient(
            colors = listOf(
                SmokeBright.copy(alpha = alpha * 0.95f),
                SmokeMid.copy(alpha = alpha * 0.75f)
            ),
            start = Offset(cx - radius, cy - radius),
            end = Offset(cx + radius, cy + radius)
        ),
        style = Stroke(width = radius * 0.3f, cap = StrokeCap.Round)
    )
    drawPath(
        path,
        SmokeBright.copy(alpha = alpha * 0.3f),
        style = Stroke(width = radius * 0.58f, cap = StrokeCap.Round)
    )

    val tailStart = Offset(cx + radius * 0.55f, cy + radius * 0.45f)
    val tailEnd = Offset(cx + radius * 1.3f, cy + radius * 1.45f)
    drawLine(
        color = SmokeBright.copy(alpha = alpha * 0.9f),
        start = tailStart,
        end = tailEnd,
        strokeWidth = radius * 0.22f,
        cap = StrokeCap.Round
    )
}

private fun Path.addQArc(cx: Float, cy: Float, radius: Float) {
    val rect = androidx.compose.ui.geometry.Rect(
        left = cx - radius,
        top = cy - radius,
        right = cx + radius,
        bottom = cy + radius
    )
    arcTo(rect, 125f, 290f, false)
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
