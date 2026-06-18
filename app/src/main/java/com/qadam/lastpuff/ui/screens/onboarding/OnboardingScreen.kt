package com.qadam.lastpuff.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qadam.lastpuff.domain.model.SosContact
import com.qadam.lastpuff.domain.model.UserProfile
import com.qadam.lastpuff.ui.components.SectionTitle
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.util.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    var step by remember { mutableStateOf(0) }
    var smokeType by remember { mutableStateOf(AppConstants.SMOKE_TYPES.first()) }
    var cigarettesPerDay by remember { mutableStateOf("10") }
    var packPrice by remember { mutableStateOf("1500") }
    var cigarettesInPack by remember { mutableStateOf("20") }
    var selectedReasons by remember { mutableStateOf(setOf<String>()) }
    var sosName by remember { mutableStateOf("") }
    var sosPhone by remember { mutableStateOf("") }
    var sosMessage by remember {
        mutableStateOf("Мне сейчас очень хочется курить. Пожалуйста, поддержи меня.")
    }
    var personalLetter by remember {
        mutableStateOf(
            "Если ты читаешь это — значит снова захотел курить.\n\n" +
                "Пожалуйста, не сдавайся. Ты сам просил напомнить тебе об этом."
        )
    }

    val calendar = remember { Calendar.getInstance() }
    var lastSmokeDate by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Qadam Last Puff",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Твой путь к свободе от курения",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            0 -> {
                SectionTitle("Тип курения")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppConstants.SMOKE_TYPES.forEach { type ->
                        FilterChip(
                            selected = smokeType == type,
                            onClick = { smokeType = type },
                            label = { Text(type) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = cigarettesPerDay,
                    onValueChange = { cigarettesPerDay = it.filter { c -> c.isDigit() } },
                    label = { Text("Сколько в день (сигарет/затяжек)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = packPrice,
                    onValueChange = { packPrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Цена пачки (₸)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cigarettesInPack,
                    onValueChange = { cigarettesInPack = it.filter { c -> c.isDigit() } },
                    label = { Text("Штук в пачке") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            1 -> {
                SectionTitle("Последняя сигарета")
                Text(
                    text = dateFormat.format(lastSmokeDate),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(onClick = { showDatePicker = true }) { Text("Выбрать дату") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showTimePicker = true }) { Text("Выбрать время") }
            }
            2 -> {
                SectionTitle("Почему ты бросаешь?")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppConstants.QUIT_REASONS.forEach { reason ->
                        FilterChip(
                            selected = reason in selectedReasons,
                            onClick = {
                                selectedReasons = if (reason in selectedReasons) {
                                    selectedReasons - reason
                                } else {
                                    selectedReasons + reason
                                }
                            },
                            label = { Text(reason) }
                        )
                    }
                }
            }
            3 -> {
                SectionTitle("Письмо себе")
                Text(
                    text = "Напиши себе на момент тяги. Это может стать самой сильной поддержкой.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = personalLetter,
                    onValueChange = { personalLetter = it },
                    label = { Text("Письмо себе") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
            }
            4 -> {
                SectionTitle("SOS-контакт")
                Text(
                    text = "Человек, которому можно позвонить или написать в трудный момент",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = sosName,
                    onValueChange = { sosName = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = sosPhone,
                    onValueChange = { sosPhone = it },
                    label = { Text("Телефон") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = sosMessage,
                    onValueChange = { sosMessage = it },
                    label = { Text("Текст сообщения") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (step < 4) step++ else {
                    viewModel.completeOnboarding(
                        profile = UserProfile(
                            smokeType = smokeType,
                            cigarettesPerDay = cigarettesPerDay.toIntOrNull() ?: 10,
                            packPrice = packPrice.toDoubleOrNull() ?: 0.0,
                            cigarettesInPack = cigarettesInPack.toIntOrNull() ?: 20,
                            lastSmokeDate = lastSmokeDate,
                            reasons = selectedReasons.toList().ifEmpty { listOf("Здоровье") }
                        ),
                        sosContact = SosContact(
                            name = sosName.ifBlank { "Близкий" },
                            phone = sosPhone,
                            message = sosMessage
                        ),
                        personalLetter = personalLetter.ifBlank { null }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (step < 4) "Далее" else "Начать путь")
        }
        if (step > 0) {
            TextButton(onClick = { step-- }, modifier = Modifier.fillMaxWidth()) {
                Text("Назад")
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = lastSmokeDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { selected ->
                        val cal = Calendar.getInstance().apply { timeInMillis = lastSmokeDate }
                        val selectedCal = Calendar.getInstance().apply { timeInMillis = selected }
                        cal.set(Calendar.YEAR, selectedCal.get(Calendar.YEAR))
                        cal.set(Calendar.MONTH, selectedCal.get(Calendar.MONTH))
                        cal.set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH))
                        lastSmokeDate = cal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = calendar.apply { timeInMillis = lastSmokeDate }.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = lastSmokeDate }
                    cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                    cal.set(Calendar.MINUTE, timeState.minute)
                    lastSmokeDate = cal.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            }
        ) {
            TimePicker(state = timeState)
        }
    }
}
