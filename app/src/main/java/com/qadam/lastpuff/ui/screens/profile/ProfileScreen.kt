package com.qadam.lastpuff.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qadam.lastpuff.domain.model.SosContact
import com.qadam.lastpuff.ui.components.SectionTitle
import com.qadam.lastpuff.ui.viewmodel.AppViewModel
import com.qadam.lastpuff.util.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val profile by viewModel.profile.collectAsState()
    val sosContact by viewModel.sosContact.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationHour by viewModel.notificationHour.collectAsState()
    val notificationMinute by viewModel.notificationMinute.collectAsState()

    val personalLetterFlow by viewModel.personalLetter.collectAsState()

    var personalLetter by remember(personalLetterFlow) {
        mutableStateOf(personalLetterFlow ?: "")
    }

    val familyPhotoUri by viewModel.familyPhotoUri.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.setFamilyPhotoUri(it.toString()) } }

    var showResetDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var smokeType by remember(profile) { mutableStateOf(profile?.smokeType ?: "") }
    var cigarettesPerDay by remember(profile) { mutableStateOf(profile?.cigarettesPerDay?.toString() ?: "") }
    var packPrice by remember(profile) { mutableStateOf(profile?.packPrice?.toString() ?: "") }
    var cigarettesInPack by remember(profile) { mutableStateOf(profile?.cigarettesInPack?.toString() ?: "") }
    var selectedReasons by remember(profile) { mutableStateOf(profile?.reasons?.toSet() ?: emptySet()) }
    var sosName by remember(sosContact) { mutableStateOf(sosContact?.name ?: "") }
    var sosPhone by remember(sosContact) { mutableStateOf(sosContact?.phone ?: "") }
    var sosMessage by remember(sosContact) { mutableStateOf(sosContact?.message ?: "") }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        SectionTitle("Профиль")

        profile?.let { p ->
            Text(
                text = "Последняя сигарета: ${dateFormat.format(p.lastSmokeDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text("Изменить дату")
            }
            OutlinedButton(onClick = { showTimePicker = true }) {
                Text("Изменить время")
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Данные курения")

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppConstants.SMOKE_TYPES.forEach { type ->
                    FilterChip(
                        selected = smokeType == type,
                        onClick = { smokeType = type },
                        label = { Text(type) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = cigarettesPerDay,
                onValueChange = { cigarettesPerDay = it.filter { c -> c.isDigit() } },
                label = { Text("В день") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = packPrice,
                onValueChange = { packPrice = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Цена пачки") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cigarettesInPack,
                onValueChange = { cigarettesInPack = it.filter { c -> c.isDigit() } },
                label = { Text("В пачке") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Причины бросить")
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

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("SOS-контакт")
            OutlinedTextField(
                value = sosName,
                onValueChange = { sosName = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = sosPhone,
                onValueChange = { sosPhone = it },
                label = { Text("Телефон") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = sosMessage,
                onValueChange = { sosMessage = it },
                label = { Text("Сообщение") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Письмо себе")
            OutlinedTextField(
                value = personalLetter,
                onValueChange = { personalLetter = it },
                label = { Text("Письмо на момент тяги") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Фото близких")
            Text(
                text = "Покажем во время тяги — сильная мотивация",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (familyPhotoUri != null) {
                AsyncImage(
                    model = familyPhotoUri,
                    contentDescription = "Фото близких",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (familyPhotoUri == null) "Добавить фото" else "Изменить фото")
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Настройки")

            RowSetting(
                title = "Тёмная тема",
                checked = darkTheme ?: false,
                onCheckedChange = { viewModel.setDarkTheme(it) }
            )
            RowSetting(
                title = "Уведомления",
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )
            Text(
                text = "Время уведомления: ${String.format("%02d:%02d", notificationHour, notificationMinute)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.updateProfile(
                        p.copy(
                            smokeType = smokeType,
                            cigarettesPerDay = cigarettesPerDay.toIntOrNull() ?: p.cigarettesPerDay,
                            packPrice = packPrice.toDoubleOrNull() ?: p.packPrice,
                            cigarettesInPack = cigarettesInPack.toIntOrNull() ?: p.cigarettesInPack,
                            reasons = selectedReasons.toList()
                        )
                    )
                    viewModel.updateSosContact(
                        SosContact(
                            id = sosContact?.id ?: 0,
                            name = sosName,
                            phone = sosPhone,
                            message = sosMessage
                        )
                    )
                    viewModel.updatePersonalLetter(personalLetter.ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сохранить изменения") }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить прогресс", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить прогресс?") },
            text = { Text("Все данные о тягах и срывах будут удалены. Профиль сохранится.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProgress()
                    showResetDialog = false
                }) { Text("Сбросить") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showDatePicker && profile != null) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = profile!!.lastSmokeDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { selected ->
                        val cal = Calendar.getInstance().apply { timeInMillis = profile!!.lastSmokeDate }
                        val selectedCal = Calendar.getInstance().apply { timeInMillis = selected }
                        cal.set(Calendar.YEAR, selectedCal.get(Calendar.YEAR))
                        cal.set(Calendar.MONTH, selectedCal.get(Calendar.MONTH))
                        cal.set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH))
                        viewModel.updateLastSmokeDate(cal.timeInMillis)
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

    if (showTimePicker && profile != null) {
        val cal = Calendar.getInstance().apply { timeInMillis = profile!!.lastSmokeDate }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                    cal.set(Calendar.MINUTE, timeState.minute)
                    viewModel.updateLastSmokeDate(cal.timeInMillis)
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

@Composable
private fun RowSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
