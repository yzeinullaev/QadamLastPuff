package com.qadam.lastpuff.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qadam.lastpuff.util.AppConstants
import kotlinx.coroutines.delay

private val SosRed = Color(0xFFE53935)
private val SosRedDark = Color(0xFFB71C1C)

@Composable
fun HugeSosButton(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(AppConstants.LONG_PRESS_MS)
            if (isPressed) {
                longPressTriggered = true
                isPressed = false
                onLongPress()
            }
        }
    }

    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isPressed) 0.96f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .scale(scale)
            .clip(RoundedCornerShape(32.dp))
            .background(SosRed)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    longPressTriggered = false
                    isPressed = true
                    waitForUpOrCancellation()
                    if (!longPressTriggered) onClick()
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔥", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Я сейчас\nхочу курить",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Нажми сюда",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Удержи 2 сек — критическая тяга",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        if (isPressed) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun BreathingCircle(phase: Int, modifier: Modifier = Modifier) {
    val cycle = phase % 8
    val (instruction, targetScale) = when {
        cycle < 3 -> "Вдох..." to 1.4f
        cycle < 5 -> "Задержка..." to 1.4f
        else -> "Выдох..." to 0.7f
    }
    val animScale = remember { Animatable(1f) }

    LaunchedEffect(phase) {
        animScale.animateTo(targetScale, tween(3000, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(animScale.value)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.4f))
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = instruction,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = "Следуй за кругом",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun CoinDropAnimation(
    visible: Boolean,
    totalCoins: Int,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            scale.snapTo(0f)
            scale.animateTo(1.2f, tween(400))
            scale.animateTo(1f, tween(200))
        }
    }

    if (visible) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💰",
                fontSize = (64 * scale.value).sp,
                modifier = Modifier.scale(scale.value)
            )
            Text(
                text = "+1 монетка",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "В копилке: $totalCoins",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SosFullscreenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SosRedDark),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
