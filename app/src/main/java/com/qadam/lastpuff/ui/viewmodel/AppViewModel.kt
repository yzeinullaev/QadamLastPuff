package com.qadam.lastpuff.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qadam.lastpuff.data.repository.UserRepository
import com.qadam.lastpuff.domain.model.Achievement
import com.qadam.lastpuff.domain.model.HealthMilestone
import com.qadam.lastpuff.domain.model.HomeStats
import com.qadam.lastpuff.domain.model.MoneyGoal
import com.qadam.lastpuff.domain.model.ProgressStats
import com.qadam.lastpuff.domain.model.RecoveryIndex
import com.qadam.lastpuff.domain.model.SosContact
import com.qadam.lastpuff.domain.model.UserProfile
import com.qadam.lastpuff.domain.model.VictoryRecord
import com.qadam.lastpuff.domain.support.MessageSession
import com.qadam.lastpuff.domain.support.SosComposeFlags
import com.qadam.lastpuff.domain.support.SosMessageComposer
import com.qadam.lastpuff.domain.support.SosMode
import com.qadam.lastpuff.domain.support.SupportContext
import com.qadam.lastpuff.util.AppConstants
import com.qadam.lastpuff.util.StatsCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AppViewModel(private val repository: UserRepository) : ViewModel() {

    private val tickFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    private var messageSession: MessageSession? = null

    val onboardingCompleted = repository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkTheme = repository.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val profile = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sosContact = repository.observeSosContact()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val moneyGoal = repository.observeMoneyGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val homeStats: StateFlow<HomeStats?> = repository.observeHomeStats(tickFlow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val progressStats: StateFlow<ProgressStats?> = repository.observeProgressStats(tickFlow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthMilestones: StateFlow<List<HealthMilestone>> = repository.observeHealthMilestones(tickFlow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.observeAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val victories: StateFlow<List<VictoryRecord>> = repository.observeVictories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoveryIndex: StateFlow<RecoveryIndex> = repository.observeRecoveryIndex(tickFlow)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecoveryIndex())

    val personalLetter = repository.personalLetter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationsEnabled = repository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationHour = repository.notificationHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9)

    val notificationMinute = repository.notificationMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val familyPhotoUri = repository.familyPhotoUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _sosState = MutableStateFlow(SosUiState())
    val sosState = _sosState.asStateFlow()

    fun completeOnboarding(profile: UserProfile, sosContact: SosContact, personalLetter: String?) {
        viewModelScope.launch {
            repository.completeOnboarding(profile, sosContact, personalLetter)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch { repository.saveProfile(profile) }
    }

    fun updateSosContact(contact: SosContact) {
        viewModelScope.launch { repository.saveSosContact(contact) }
    }

    fun updatePersonalLetter(letter: String?) {
        viewModelScope.launch { repository.setPersonalLetter(letter) }
    }

    fun saveMoneyGoal(goal: MoneyGoal) {
        viewModelScope.launch { repository.saveMoneyGoal(goal) }
    }

    fun updateLastSmokeDate(timestamp: Long) {
        viewModelScope.launch { repository.updateLastSmokeDate(timestamp) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch { repository.setNotificationTime(hour, minute) }
    }

    fun setFamilyPhotoUri(uri: String?) {
        viewModelScope.launch { repository.setFamilyPhotoUri(uri) }
    }

    fun resetProgress() {
        viewModelScope.launch { repository.resetProgress() }
    }

    fun startSos() {
        messageSession = MessageSession()
        val mode = SosMode.forDay(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
        _sosState.value = SosUiState(
            step = SosStep.TIMER,
            timerSecondsLeft = AppConstants.SOS_TIMER_SECONDS,
            elapsedSeconds = 0,
            sosMode = mode
        )
        applySosMoment(0)
    }

    fun startEmergencySos() {
        messageSession = MessageSession()
        _sosState.value = SosUiState(step = SosStep.EMERGENCY, isEmergency = true)
    }

    fun startTimerFromEmergency() {
        val mode = SosMode.forDay(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
        if (messageSession == null) messageSession = MessageSession()
        _sosState.value = _sosState.value.copy(
            step = SosStep.TIMER,
            timerSecondsLeft = AppConstants.SOS_TIMER_SECONDS,
            elapsedSeconds = 0,
            sosMode = mode
        )
        applySosMoment(0)
    }

    fun resumeTimer() {
        val elapsed = _sosState.value.elapsedSeconds
        _sosState.value = _sosState.value.copy(step = SosStep.TIMER)
        applySosMoment(elapsed)
    }

    fun startBreathing() {
        _sosState.value = _sosState.value.copy(step = SosStep.BREATHING, breathingPhase = 0)
    }

    fun showOptionalDetails() {
        _sosState.value = _sosState.value.copy(showOptionalDetails = true)
    }

    fun dismissOptionalDetails() {
        _sosState.value = _sosState.value.copy(showOptionalDetails = false)
    }

    fun setOptionalTrigger(trigger: String) {
        _sosState.value = _sosState.value.copy(trigger = trigger)
    }

    fun setOptionalIntensity(intensity: Int) {
        _sosState.value = _sosState.value.copy(intensity = intensity)
    }

    fun tickTimer() {
        val state = _sosState.value
        if (state.step != SosStep.TIMER) return
        if (state.timerSecondsLeft <= 1) {
            _sosState.value = state.copy(step = SosStep.RESULT, timerSecondsLeft = 0)
        } else {
            val newSeconds = state.timerSecondsLeft - 1
            val elapsed = AppConstants.SOS_TIMER_SECONDS - newSeconds
            _sosState.value = state.copy(timerSecondsLeft = newSeconds, elapsedSeconds = elapsed)
            if (shouldUpdateSosMessage(elapsed)) {
                applySosMoment(elapsed)
            }
        }
    }

    private fun shouldUpdateSosMessage(elapsed: Int): Boolean =
        elapsed == 0 || elapsed % AppConstants.SOS_MESSAGE_INTERVAL_SECONDS == 0

    private fun applySosMoment(elapsed: Int) {
        val session = messageSession ?: return
        val state = _sosState.value
        val moment = SosMessageComposer.compose(
            elapsedSeconds = elapsed,
            ctx = buildSupportContext(),
            session = session,
            mode = state.sosMode,
            flags = SosComposeFlags(state.factShown, state.letterShown)
        )
        _sosState.value = state.copy(
            currentMessage = moment.primary,
            secondaryMessage = moment.secondary,
            currentAction = moment.action,
            showPersonalLetterDialog = moment.showPersonalLetter && !state.letterShown,
            factShown = state.factShown || (elapsed == 30 && moment.secondary != null),
            letterShown = state.letterShown || moment.showPersonalLetter
        )
    }

    private fun buildSupportContext(): SupportContext {
        val p = profile.value
        val goal = moneyGoal.value
        val pricePerCig = if (p != null && p.cigarettesInPack > 0) {
            p.packPrice / p.cigarettesInPack
        } else 0.0
        return SupportContext(
            reasons = p?.reasons ?: emptyList(),
            goalTitle = goal?.title,
            currency = p?.currency ?: "₸",
            pricePerCigarette = pricePerCig,
            totalWins = victories.value.size,
            personalLetter = personalLetter.value,
            hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        )
    }

    fun tickBreathing() {
        val state = _sosState.value
        if (state.step != SosStep.BREATHING) return
        _sosState.value = state.copy(breathingPhase = state.breathingPhase + 1)
    }

    fun dismissPersonalLetter() {
        _sosState.value = _sosState.value.copy(
            showPersonalLetterDialog = false,
            letterShown = true
        )
    }

    fun showPhoto() {
        _sosState.value = _sosState.value.copy(showPhoto = true)
    }

    fun dismissPhoto() {
        _sosState.value = _sosState.value.copy(showPhoto = false)
    }

    fun showReasons() {
        _sosState.value = _sosState.value.copy(showReasons = true)
    }

    fun dismissReasons() {
        _sosState.value = _sosState.value.copy(showReasons = false)
    }

    fun completeSos(success: Boolean) {
        val state = _sosState.value
        val session = messageSession
        viewModelScope.launch {
            val recoveryPair = if (success) {
                repository.applySosRecoveryBoost()
            } else {
                RecoveryIndex() to RecoveryIndex()
            }
            repository.recordCraving(
                intensity = state.intensity,
                trigger = state.trigger.ifBlank { "Не указано" },
                success = success
            )
            val endMessage = if (success && session != null) {
                SosMessageComposer.victoryMessage(session)
            } else if (!success && session != null) {
                SosMessageComposer.relapseMessage(session)
            } else ""
            _sosState.value = state.copy(
                step = if (success) SosStep.SUCCESS else SosStep.RELAPSE,
                success = success,
                showCoinAnimation = success,
                currentMessage = endMessage,
                recoveryBefore = recoveryPair.first,
                recoveryAfter = recoveryPair.second
            )
        }
    }

    fun dismissCoinAnimation() {
        _sosState.value = _sosState.value.copy(showCoinAnimation = false)
    }

    fun resetSos() {
        messageSession = null
        _sosState.value = SosUiState()
    }

    fun moneySavedNow(): Double {
        val p = profile.value ?: return 0.0
        return StatsCalculator.moneySaved(p)
    }

    fun quitReasons(): List<String> = profile.value?.reasons ?: emptyList()

    fun liveHealthNow(): Pair<String, String> {
        val profile = profile.value ?: return "Сейчас" to "Начни путь — и тело сразу начнёт восстанавливаться."
        val elapsed = StatsCalculator.elapsedMillis(profile.lastSmokeDate)
        return AppConstants.liveHealthMessage(elapsed)
    }
}

enum class SosStep {
    EMERGENCY, TIMER, BREATHING, RESULT, SUCCESS, RELAPSE
}

data class SosUiState(
    val step: SosStep = SosStep.TIMER,
    val isEmergency: Boolean = false,
    val intensity: Int = 5,
    val trigger: String = "",
    val timerSecondsLeft: Int = AppConstants.SOS_TIMER_SECONDS,
    val elapsedSeconds: Int = 0,
    val currentMessage: String = "",
    val secondaryMessage: String? = null,
    val currentAction: String? = null,
    val sosMode: SosMode = SosMode.CHALLENGE,
    val breathingPhase: Int = 0,
    val factShown: Boolean = false,
    val letterShown: Boolean = false,
    val showOptionalDetails: Boolean = false,
    val showReasons: Boolean = false,
    val showPhoto: Boolean = false,
    val showPersonalLetterDialog: Boolean = false,
    val showCoinAnimation: Boolean = false,
    val success: Boolean = false,
    val recoveryBefore: RecoveryIndex = RecoveryIndex(),
    val recoveryAfter: RecoveryIndex = RecoveryIndex()
)

class AppViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
