package com.qadam.lastpuff.ui.screens.sos

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qadam.lastpuff.ui.screens.body.BodyVictoryScreen
import com.qadam.lastpuff.ui.components.BreathingCircle
import com.qadam.lastpuff.ui.components.SosFullscreenBackground
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.ui.viewmodel.SosStep
import com.qadam.lastpuff.util.AppConstants
import com.qadam.lastpuff.util.ContactUtils
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SosScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val state by viewModel.sosState.collectAsState()
    val sosContact by viewModel.sosContact.collectAsState()
    val familyPhotoUri by viewModel.familyPhotoUri.collectAsState()
    val personalLetter by viewModel.personalLetter.collectAsState()
    val homeStats by viewModel.homeStats.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.step) {
        if (state.step == SosStep.TIMER) {
            while (true) {
                delay(1000)
                if (viewModel.sosState.value.step != SosStep.TIMER) break
                viewModel.tickTimer()
            }
        }
    }

    LaunchedEffect(state.step) {
        if (state.step == SosStep.BREATHING) {
            while (true) {
                delay(4000)
                if (viewModel.sosState.value.step != SosStep.BREATHING) break
                viewModel.tickBreathing()
            }
        }
    }

    LaunchedEffect(state.step, state.showCoinAnimation) {
        if (state.step == SosStep.RELAPSE) {
            delay(3500)
            viewModel.resetSos()
            onComplete()
        }
    }

    when (state.step) {
        SosStep.EMERGENCY -> EmergencyScreen(
            contact = sosContact,
            onCall = { sosContact?.let { ContactUtils.call(context, it.phone) } },
            onSms = { sosContact?.let { ContactUtils.sms(context, it.phone, it.message) } },
            onWhatsApp = { sosContact?.let { ContactUtils.whatsApp(context, it.phone, it.message) } },
            onTelegram = { sosContact?.let { ContactUtils.telegram(context, it.phone, it.message) } },
            onBreathing = { viewModel.startBreathing() },
            onTimer = { viewModel.startTimerFromEmergency() },
            onBack = { viewModel.resetSos(); onBack() }
        )

        SosStep.TIMER, SosStep.BREATHING -> SosFullscreenBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            if (state.step == SosStep.BREATHING) {
                                viewModel.resumeTimer()
                            }
                        },
                        enabled = state.step == SosStep.BREATHING
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "К таймеру",
                            tint = Color.White.copy(alpha = if (state.step == SosStep.BREATHING) 1f else 0f)
                        )
                    }
                }

                if (state.step == SosStep.BREATHING) {
                    Spacer(modifier = Modifier.weight(1f))
                    BreathingCircle(phase = state.breathingPhase)
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(0.12f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val minutes = state.timerSecondsLeft / 60
                        val seconds = state.timerSecondsLeft % 60
                        Text(
                            text = "НЕ ЗАКРЫВАЙ",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Я С ТОБОЙ",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.elapsedSeconds / AppConstants.SOS_TIMER_SECONDS.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.currentMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp,
                                maxLines = 3
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            state.secondaryMessage?.let { secondary ->
                                Text(
                                    text = secondary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.88f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp,
                                    maxLines = 2
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            state.currentAction?.let { action ->
                                Text(
                                    text = when (state.sosMode) {
                                        com.qadam.lastpuff.domain.support.SosMode.BREATHING -> "🌬 $action"
                                        com.qadam.lastpuff.domain.support.SosMode.FAMILY -> "❤️ $action"
                                        com.qadam.lastpuff.domain.support.SosMode.CHALLENGE -> "🎯 $action"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SosActionChip("Дыхание") { viewModel.startBreathing() }
                        SosActionChip("Причины") { viewModel.showReasons() }
                        if (familyPhotoUri != null) {
                            SosActionChip("Фото") { viewModel.showPhoto() }
                        }
                        sosContact?.let {
                            SosActionChip("Позвонить") { ContactUtils.call(context, it.phone) }
                            SosActionChip("WhatsApp") { ContactUtils.whatsApp(context, it.phone, it.message) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.showOptionalDetails() }) {
                        Text("Если хочешь — укажи причину", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.weight(0.08f))
                }
            }
        }

        SosStep.RESULT -> SosFullscreenBackground {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Удалось не закурить?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.completeSos(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                ) {
                    Text("Да, справился", color = Color(0xFFB71C1C))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.completeSos(false) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Продолжаю путь", color = Color.White)
                }
            }
        }

        SosStep.SUCCESS -> BodyVictoryScreen(
            before = state.recoveryBefore,
            after = state.recoveryAfter,
            showCoinAnimation = state.showCoinAnimation,
            totalCoins = homeStats?.totalCoins ?: 1,
            coinsEarned = state.coinsEarned,
            onContinue = {
                viewModel.resetSos()
                onComplete()
            }
        )

        SosStep.RELAPSE -> SosFullscreenBackground {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text("💚", fontSize = 56.sp)
                Text(
                    text = state.currentMessage.ifBlank { "Ты продолжаешь путь" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Один момент не отменяет\nвесь твой прогресс.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (state.showOptionalDetails) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissOptionalDetails() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Если хочешь — укажи причину", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppConstants.CRAVING_TRIGGERS.forEach { trigger ->
                        FilterChip(
                            selected = state.trigger == trigger,
                            onClick = { viewModel.setOptionalTrigger(trigger) },
                            label = { Text(trigger) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Сила тяги: ${state.intensity}")
                Slider(
                    value = state.intensity.toFloat(),
                    onValueChange = { viewModel.setOptionalIntensity(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.dismissOptionalDetails() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Готово") }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (state.showReasons) {
        val reasons = viewModel.quitReasons()
        AlertDialog(
            onDismissRequest = { viewModel.dismissReasons() },
            title = { Text("Почему нельзя сейчас") },
            text = {
                Column {
                    reasons.forEach { Text("• $it", modifier = Modifier.padding(vertical = 4.dp)) }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissReasons() }) { Text("Продолжить") }
            }
        )
    }

    if (state.showPhoto && familyPhotoUri != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPhoto() },
            title = { Text("Ради них") },
            text = {
                AsyncImage(
                    model = familyPhotoUri,
                    contentDescription = "Фото близких",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissPhoto() }) { Text("Продолжить") }
            }
        )
    }

    if (state.showPersonalLetterDialog && !personalLetter.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPersonalLetter() },
            title = { Text("Письмо себе") },
            text = {
                Text(
                    text = personalLetter!!,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissPersonalLetter() }) { Text("Продолжать путь") }
            }
        )
    }
}

@Composable
private fun EmergencyScreen(
    contact: com.qadam.lastpuff.domain.model.SosContact?,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onWhatsApp: () -> Unit,
    onTelegram: () -> Unit,
    onBreathing: () -> Unit,
    onTimer: () -> Unit,
    onBack: () -> Unit
) {
    SosFullscreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🚨", fontSize = 48.sp)
            Text(
                text = "КРИТИЧЕСКАЯ\nТЯГА",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "НЕ СДАВАЙСЯ",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            EmergencyButton("📞 Позвонить ${contact?.name ?: ""}", onCall)
            Spacer(modifier = Modifier.height(8.dp))
            EmergencyButton("💬 SMS", onSms)
            Spacer(modifier = Modifier.height(8.dp))
            EmergencyButton("WhatsApp", onWhatsApp)
            Spacer(modifier = Modifier.height(8.dp))
            EmergencyButton("Telegram", onTelegram)
            Spacer(modifier = Modifier.height(8.dp))
            EmergencyButton("🌬 Дыхание", onBreathing)
            Spacer(modifier = Modifier.height(8.dp))
            EmergencyButton("⏱ 3 минуты вместе", onTimer)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("Назад", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun EmergencyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, color = Color(0xFFB71C1C), modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun SosActionChip(label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label, color = Color.White) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            labelColor = Color.White
        )
    )
}
