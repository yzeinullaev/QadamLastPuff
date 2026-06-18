package com.qadam.lastpuff.ui.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qadam.lastpuff.ui.components.InfoRow
import com.qadam.lastpuff.ui.components.QadamCard
import com.qadam.lastpuff.ui.components.SectionTitle
import com.qadam.lastpuff.ui.components.StatCard
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.util.TimeFormatUtils
import java.util.Locale

@Composable
fun ProgressScreen(viewModel: AppViewModel) {
    val stats by viewModel.progressStats.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val victories by viewModel.victories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        SectionTitle("Твой прогресс")

        if (victories.isNotEmpty()) {
            SectionTitle("История побед")
            victories.take(10).forEach { victory ->
                QadamCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(victory.time, style = MaterialTheme.typography.titleMedium)
                            Text(
                                TimeFormatUtils.intensityStars(victory.intensity.coerceAtMost(5)),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Text(
                            text = victory.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        stats?.let { s ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Дней",
                    value = "${s.daysWithoutSmoking}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Не выкурено",
                    value = "${s.cigarettesNotSmoked}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatCard(
                title = "Сэкономлено",
                value = String.format(Locale.getDefault(), "%.0f ${profile?.currency ?: "₸"}", s.moneySaved)
            )

            Spacer(modifier = Modifier.height(16.dp))

            QadamCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("Всего тяг", "${s.totalCravings}")
                    InfoRow("Справился", "${s.cravingsWon}")
                    InfoRow("Срывов", "${s.relapses}")
                    InfoRow("Процент побед", String.format(Locale.getDefault(), "%.0f%%", s.winRate))
                    InfoRow("Самая длинная серия", "${s.longestStreak} дн.")
                    InfoRow("Средняя сила тяги", String.format(Locale.getDefault(), "%.1f", s.averageIntensity))
                }
            }

            if (s.dangerousHours.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Самые опасные часы")
                QadamCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        s.dangerousHours.forEach { (hour, count) ->
                            InfoRow("${hour}:00 – ${hour + 1}:00", "$count раз")
                        }
                    }
                }
            }

            if (s.topTriggers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Частые причины тяги")
                QadamCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        s.topTriggers.forEach { (trigger, count) ->
                            InfoRow(trigger, "$count раз")
                        }
                    }
                }
            }
        } ?: Text(
            text = "Загрузка...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
