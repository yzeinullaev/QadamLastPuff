package com.qadam.lastpuff.ui.screens.body

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qadam.lastpuff.domain.model.RecoveryBoosts
import com.qadam.lastpuff.domain.model.RecoveryIndex
import com.qadam.lastpuff.ui.components.BodySilhouette
import com.qadam.lastpuff.ui.components.CoinDropAnimation
import com.qadam.lastpuff.util.RecoveryCalculator
import kotlinx.coroutines.delay

private val VictoryGreen = Color(0xFF4CAF50)
private val VictoryBg = Color(0xFF0A0A0A)
private val CardDark = Color(0xFF1A1F1C)

data class VictoryBoostItem(
    val emoji: String,
    val label: String,
    val boost: Float
)

private val victoryBoosts = listOf(
    VictoryBoostItem("❤️", "Сердце", RecoveryBoosts.SOS_WIN.heart),
    VictoryBoostItem("🫁", "Лёгкие", RecoveryBoosts.SOS_WIN.lungs),
    VictoryBoostItem("🩸", "Кровь", RecoveryBoosts.SOS_WIN.blood),
    VictoryBoostItem("🧠", "Мозг / контроль", RecoveryBoosts.SOS_WIN.brain),
    VictoryBoostItem("🛡", "Сила воли", RecoveryBoosts.SOS_WIN.willpower)
)

@Composable
fun BodyVictoryScreen(
    before: RecoveryIndex,
    after: RecoveryIndex,
    showCoinAnimation: Boolean,
    totalCoins: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val waveProgress = remember { Animatable(0f) }
    val organsAfter = remember(after) { RecoveryCalculator.toOrgans(after) }

    LaunchedEffect(Unit) {
        delay(300)
        waveProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2200, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VictoryBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showCoinAnimation) {
                CoinDropAnimation(visible = true, totalCoins = totalCoins)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("✨", fontSize = 40.sp)
            Text(
                text = "Отличная работа!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ты выдержал тягу и помог своему телу стать сильнее",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0D1A12))
            ) {
                BodySilhouette(
                    organs = organsAfter,
                    waveProgress = waveProgress.value,
                    highlightHeartLungs = true,
                    glowColor = VictoryGreen
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Организм восстановился ещё на",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                victoryBoosts.forEach { item ->
                    AnimatedBoostRow(
                        item = item,
                        beforeValue = valueForOrgan(before, item.label),
                        afterValue = valueForOrgan(after, item.label)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .padding(16.dp)
            ) {
                Text(
                    text = "«",
                    fontSize = 32.sp,
                    color = VictoryGreen
                )
                Text(
                    text = "Каждая минута без сигареты — это вклад в твоё будущее.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VictoryGreen,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Продолжить",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnimatedBoostRow(
    item: VictoryBoostItem,
    beforeValue: Float,
    afterValue: Float
) {
    val animatedValue = remember { Animatable(beforeValue) }

    LaunchedEffect(afterValue) {
        delay(400)
        animatedValue.animateTo(
            targetValue = afterValue,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        Text(
            text = "+${formatBoost(item.boost)}%",
            style = MaterialTheme.typography.titleSmall,
            color = VictoryGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun valueForOrgan(index: RecoveryIndex, label: String): Float = when {
    label.startsWith("Сердце") -> index.heart
    label.startsWith("Лёгкие") -> index.lungs
    label.startsWith("Кровь") -> index.blood
    label.startsWith("Мозг") -> index.brain
    else -> index.willpower
}

private fun formatBoost(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)
