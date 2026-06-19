package com.qadam.lastpuff.ui.screens.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import kotlinx.coroutines.delay

private enum class IntroTextPhase {
    None,
    First,
    Second
}

@Composable
fun FirstLaunchIntroScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textPhase by remember { mutableIntStateOf(IntroTextPhase.None.ordinal) }

    LaunchedEffect(Unit) {
        delay(3400)
        textPhase = IntroTextPhase.First.ordinal
        delay(1000)
        textPhase = IntroTextPhase.Second.ordinal
        delay(1200)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            SmokeQLogo(size = 220.dp)

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedContent(
                targetState = textPhase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "introText"
            ) { phase ->
                when (IntroTextPhase.entries[phase]) {
                    IntroTextPhase.None -> Spacer(modifier = Modifier.height(72.dp))
                    IntroTextPhase.First -> IntroText(
                        lines = listOf(
                            "Сегодня ты сделал",
                            "самый важный шаг."
                        )
                    )
                    IntroTextPhase.Second -> IntroText(
                        lines = listOf(
                            "Добро пожаловать",
                            "в жизнь без сигарет."
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroText(lines: List<String>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        lines.forEach { line ->
            Text(
                text = line,
                color = Color(0xFFE8F5EC),
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
        }
    }
}
