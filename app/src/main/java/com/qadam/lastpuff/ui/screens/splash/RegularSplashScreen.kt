package com.qadam.lastpuff.ui.screens.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qadam.lastpuff.domain.model.RecoveryIndex
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

object SplashQuotes {
    val phrases = listOf(
        "Ты уже на пути к свободе.",
        "Каждый день имеет значение.",
        "Сегодня организм станет ещё сильнее.",
        "Не сдавайся.",
        "Ты уже начал новую жизнь.",
        "Свобода начинается с одного решения.",
        "Каждая победа делает тебя сильнее.",
        "Твоё тело благодарит тебя."
    )
}

@Composable
fun RegularSplashScreen(
    recoveryIndex: RecoveryIndex,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quote = remember { SplashQuotes.phrases.random() }
    var syncPhase by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(800)
        syncPhase = 1
        delay(700)
        onFinished()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlowingQLogo(size = 88.dp)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = quote,
            color = Color(0xFFD8F0E2),
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        RecoveryStatusBlock(recoveryIndex = recoveryIndex)

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedContent(
            targetState = syncPhase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "syncText"
        ) { phase ->
            Text(
                text = if (phase == 0) "Синхронизация прогресса..." else "✓ Организм восстанавливается",
                color = Color(0xFF8FD4AA),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecoveryStatusBlock(recoveryIndex: RecoveryIndex) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Организм восстанавливается",
            color = Color(0xFFB8F5D4),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        RecoveryRow("❤️", "Сердце", recoveryIndex.heart)
        RecoveryRow("🫁", "Лёгкие", recoveryIndex.lungs)
        RecoveryRow("🩸", "Кровь", recoveryIndex.blood)
        RecoveryRow("🧠", "Контроль", recoveryIndex.willpower)
    }
}

@Composable
private fun RecoveryRow(emoji: String, label: String, value: Float) {
    Text(
        text = "$emoji $label: ${value.coerceIn(0f, 100f).roundToInt()}%",
        color = Color(0xFF9FD4B8),
        fontSize = 14.sp
    )
}
