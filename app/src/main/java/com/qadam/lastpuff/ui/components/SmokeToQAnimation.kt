package com.qadam.lastpuff.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val SmokeBright = Color(0xFFB8F5D4)
private val SmokeMid = Color(0xFF6BCF9A)
private val SmokeDim = Color(0xFF3A8F62)
private val CigaretteBody = Color(0xFF8FD4AA)
private val CigaretteFilter = Color(0xFF2E6B48)
private val SplashBg = Color(0xFF0A120E)

private data class SmokeParticle(
    val start: Offset,
    val target: Offset,
    val delay: Float,
    val sizeFactor: Float,
    val drift: Float
)

@Composable
fun SmokeToQAnimation(
    modifier: Modifier = Modifier,
    loopIdle: Boolean = true,
    showCigarette: Boolean = true,
    onFormed: () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }
    val particles = remember { buildSmokeParticles(28) }
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "smokeIdle")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    LaunchedEffect(loopIdle) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2800, easing = FastOutSlowInEasing)
        )
        onFormed()
        if (loopIdle) {
            while (true) {
                progress.animateTo(0.88f, tween(1400, easing = FastOutSlowInEasing))
                progress.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.42f
        val form = progress.value

        if (showCigarette) {
            drawCigarette(w, h, (1f - form).coerceIn(0f, 1f))
        }

        drawQGuide(cx, cy, w * 0.24f, form)

        particles.forEach { particle ->
            drawSmokeParticle(particle, form, w, h, idlePulse, loopIdle && form >= 0.95f)
        }

        if (form > 0.55f) {
            val qAlpha = ((form - 0.55f) / 0.45f).coerceIn(0f, 1f)
            drawQLetter(cx, cy, w * 0.24f, qAlpha)
        }
    }
}

@Composable
fun SmokeToQLogo(
    size: Dp = 120.dp,
    modifier: Modifier = Modifier,
    loopIdle: Boolean = true
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        SmokeToQAnimation(
            modifier = Modifier.fillMaxSize(),
            loopIdle = loopIdle,
            showCigarette = true
        )
    }
}

@Composable
fun SmokeToQSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        SmokeToQAnimation(
            modifier = Modifier.size(280.dp),
            loopIdle = false,
            showCigarette = true,
            onFormed = onFinished
        )
    }
}

private fun buildSmokeParticles(count: Int): List<SmokeParticle> {
    val random = Random(42)
    val targets = buildQTargets(count)
    return targets.mapIndexed { index, target ->
        val scatterAngle = random.nextFloat() * 2f * PI.toFloat()
        val scatterDist = 0.08f + random.nextFloat() * 0.18f
        val start = Offset(
            x = 0.18f + cos(scatterAngle) * scatterDist + random.nextFloat() * 0.06f,
            y = 0.78f + sin(scatterAngle) * scatterDist * 0.6f + random.nextFloat() * 0.05f
        )
        SmokeParticle(
            start = start,
            target = target,
            delay = (index % 5) * 0.04f + random.nextFloat() * 0.08f,
            sizeFactor = 0.75f + random.nextFloat() * 0.55f,
            drift = random.nextFloat() * 2f * PI.toFloat()
        )
    }
}

private fun buildQTargets(count: Int): List<Offset> {
    val cx = 0.5f
    val cy = 0.42f
    val r = 0.24f
    val ringCount = (count * 0.72f).toInt().coerceAtLeast(1)
    val tailCount = count - ringCount
    val points = mutableListOf<Offset>()

    for (i in 0 until ringCount) {
        val t = i / (ringCount - 1).coerceAtLeast(1).toFloat()
        val angle = lerp(2.35f, -0.25f, t)
        points.add(Offset(cx + cos(angle) * r, cy + sin(angle) * r))
    }
    for (i in 0 until tailCount) {
        val t = if (tailCount <= 1) 1f else i / (tailCount - 1).toFloat()
        points.add(Offset(lerp(0.58f, 0.76f, t), lerp(0.56f, 0.84f, t)))
    }
    return points
}

private fun DrawScope.drawSmokeParticle(
    particle: SmokeParticle,
    form: Float,
    w: Float,
    h: Float,
    idlePulse: Float,
    idleActive: Boolean
) {
    val localT = ((form - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
    val eased = FastOutSlowInEasing.transform(localT)

    val start = Offset(particle.start.x * w, particle.start.y * h)
    val target = Offset(particle.target.x * w, particle.target.y * h)
    var pos = Offset(
        x = lerp(start.x, target.x, eased),
        y = lerp(start.y, target.y, eased)
    )

    if (idleActive) {
        val swirl = sin(idlePulse * PI.toFloat() * 2f + particle.drift) * w * 0.012f
        pos = Offset(pos.x + swirl, pos.y + swirl * 0.6f)
    }

    val spreadScale = lerp(1.9f, 0.75f, eased)
    val alpha = lerp(0.12f, 0.82f, eased) * lerp(0.5f, 1f, form)
    val baseRadius = w * 0.045f * particle.sizeFactor * spreadScale

    drawSmokePuff(pos, baseRadius * 1.8f, alpha * 0.35f, SmokeDim)
    drawSmokePuff(pos, baseRadius * 1.2f, alpha * 0.55f, SmokeMid)
    drawSmokePuff(pos, baseRadius * 0.65f, alpha * 0.85f, SmokeBright)
}

private fun DrawScope.drawSmokePuff(center: Offset, radius: Float, alpha: Float, color: Color) {
    if (alpha <= 0.01f) return
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.45f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawQGuide(cx: Float, cy: Float, radius: Float, form: Float) {
    if (form < 0.15f) return
    val alpha = (form * 0.25f).coerceIn(0f, 0.22f)
    val path = Path().apply {
        addQArc(cx, cy, radius)
    }
    drawPath(
        path,
        SmokeMid.copy(alpha = alpha),
        style = Stroke(width = radius * 0.35f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawQLetter(cx: Float, cy: Float, radius: Float, alpha: Float) {
    val path = Path().apply {
        addQArc(cx, cy, radius)
    }
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
        style = Stroke(width = radius * 0.28f, cap = StrokeCap.Round)
    )
    drawPath(
        path,
        SmokeBright.copy(alpha = alpha * 0.35f),
        style = Stroke(width = radius * 0.55f, cap = StrokeCap.Round)
    )

    val tailStart = Offset(cx + radius * 0.55f, cy + radius * 0.45f)
    val tailEnd = Offset(cx + radius * 1.35f, cy + radius * 1.55f)
    drawLine(
        color = SmokeBright.copy(alpha = alpha * 0.9f),
        start = tailStart,
        end = tailEnd,
        strokeWidth = radius * 0.24f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = SmokeMid.copy(alpha = alpha * 0.35f),
        start = tailStart,
        end = tailEnd,
        strokeWidth = radius * 0.48f,
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

private fun DrawScope.drawCigarette(w: Float, h: Float, alpha: Float) {
    if (alpha <= 0.05f) return
    val y = h * 0.79f
    val bodyLeft = w * 0.08f
    val bodyRight = w * 0.52f
    val bodyH = h * 0.035f

    drawRoundRect(
        color = CigaretteBody.copy(alpha = alpha * 0.9f),
        topLeft = Offset(bodyLeft, y - bodyH / 2f),
        size = androidx.compose.ui.geometry.Size(bodyRight - bodyLeft, bodyH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyH / 2f)
    )
    drawRoundRect(
        color = CigaretteFilter.copy(alpha = alpha),
        topLeft = Offset(bodyRight - w * 0.08f, y - bodyH / 2f),
        size = androidx.compose.ui.geometry.Size(w * 0.08f, bodyH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyH / 2f)
    )

    val ember = Offset(bodyLeft - w * 0.012f, y)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                SmokeBright.copy(alpha = alpha * 0.9f),
                SmokeMid.copy(alpha = alpha * 0.4f),
                Color.Transparent
            ),
            center = ember,
            radius = w * 0.028f
        ),
        radius = w * 0.028f,
        center = ember
    )

    if (alpha > 0.3f) {
        scale(scaleX = 1f, scaleY = 1f, pivot = ember) {
            drawSmokePuff(
                Offset(ember.x - w * 0.02f, ember.y - h * 0.05f),
                w * 0.04f,
                alpha * 0.35f,
                SmokeMid
            )
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
