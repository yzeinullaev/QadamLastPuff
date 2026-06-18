package com.qadam.lastpuff.ui.screens.money

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qadam.lastpuff.domain.model.MoneyGoal
import com.qadam.lastpuff.ui.components.QadamCard
import com.qadam.lastpuff.ui.components.SectionTitle
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import java.util.Locale

@Composable
fun MoneyScreen(viewModel: AppViewModel) {
    val goal by viewModel.moneyGoal.collectAsState()
    val stats by viewModel.homeStats.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var title by remember(goal) { mutableStateOf(goal?.title ?: "") }
    var amount by remember(goal) { mutableStateOf(goal?.amount?.toInt()?.toString() ?: "") }

    val saved = stats?.moneySaved ?: 0.0
    val targetAmount = goal?.amount ?: 0.0
    val progress = if (targetAmount > 0) (saved / targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        SectionTitle("Деньги и цель")

        if (goal != null) {
            QadamCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = goal!!.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.0f / %.0f ${profile?.currency ?: "₸"}",
                            saved, targetAmount
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f%%", progress * 100),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = if (goal == null) "Создай цель, на что потратишь сэкономленные деньги" else "Изменить цель",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название цели") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Например: Новые кроссовки") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = { Text("Сумма цели (₸)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                if (title.isNotBlank()) {
                    viewModel.saveMoneyGoal(MoneyGoal(title = title, amount = parsedAmount))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && amount.isNotBlank()
        ) {
            Text(if (goal == null) "Создать цель" else "Сохранить")
        }
    }
}
