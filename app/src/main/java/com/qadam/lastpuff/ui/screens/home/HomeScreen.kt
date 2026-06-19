package com.qadam.lastpuff.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qadam.lastpuff.ui.components.HugeSosButton
import com.qadam.lastpuff.ui.components.QadamCard
import com.qadam.lastpuff.ui.components.RelapseReportCard
import com.qadam.lastpuff.ui.components.StatCard
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.util.StatsCalculator
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onSosClick: () -> Unit,
    onEmergencySosClick: () -> Unit
) {
    val stats by viewModel.homeStats.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val moneyGoal by viewModel.moneyGoal.collectAsState()
    var showRelapseDialog by remember { mutableStateOf(false) }

    if (showRelapseDialog) {
        AlertDialog(
            onDismissRequest = { showRelapseDialog = false },
            icon = { Text("💚", style = MaterialTheme.typography.headlineMedium) },
            title = { Text("Записать срыв?") },
            text = {
                Text(
                    "Счётчик дней и сэкономленные деньги пересчитаются с этого момента.\n\n" +
                        "Один срыв не отменяет весь путь — ты продолжаешь."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRelapseDialog = false
                    viewModel.recordRelapse()
                }) {
                    Text(
                        "Записать и начать заново",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRelapseDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Главный акцент — SOS-кнопка
        HugeSosButton(
            onClick = onSosClick,
            onLongPress = onEmergencySosClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        stats?.let { s ->
            // Мотивация вместо нулей
            QadamCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (s.isJustStarted) {
                            "Сегодня ты принял важное решение."
                        } else {
                            "Ты на пути к свободе."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s.dailyLifeCard,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            s.personalReason?.let { reason ->
                Spacer(modifier = Modifier.height(12.dp))
                QadamCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Почему нельзя сейчас?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            s.lastVictoryAgo?.let { ago ->
                Spacer(modifier = Modifier.height(12.dp))
                QadamCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Последняя победа",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ты победил тягу $ago",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (s.totalCoins > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                QadamCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Копилка побед", style = MaterialTheme.typography.titleMedium)
                        Text("💰 ${s.totalCoins}", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Твой прогресс",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            QadamCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${s.days}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when {
                            s.days == 0 && s.hours == 0L -> "только начал"
                            s.days == 1 -> "день"
                            s.days in 2..4 -> "дня"
                            else -> "дней"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (s.days > 0 || s.hours > 0) {
                        Text(
                            text = "${s.hours} ч ${s.minutes} мин без курения",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Не выкурено",
                    value = if (s.cigarettesNotSmoked == 0) "—" else "${s.cigarettesNotSmoked}",
                    modifier = Modifier.weight(1f),
                    subtitle = if (s.cigarettesNotSmoked > 0) "сигарет" else "скоро здесь"
                )
                StatCard(
                    title = "Сэкономлено",
                    value = StatsCalculator.formatMoney(s.moneySaved),
                    modifier = Modifier.weight(1f),
                    subtitle = profile?.currency ?: "₸"
                )
            }

            moneyGoal?.let { goal ->
                Spacer(modifier = Modifier.height(12.dp))
                QadamCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Цель: ${goal.title}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%.0f / %.0f ${profile?.currency ?: "₸"}",
                                s.moneySaved, goal.amount
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Честность с собой",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            RelapseReportCard(onClick = { showRelapseDialog = true })

            Spacer(modifier = Modifier.height(12.dp))
            QadamCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "\"${s.motivationalQuote}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
