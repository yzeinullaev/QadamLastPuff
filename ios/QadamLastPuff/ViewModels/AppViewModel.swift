import Foundation
import SwiftUI
import Combine

@MainActor
final class AppViewModel: ObservableObject {
    private let repository: UserRepository
    private var messageSession: MessageSession?
    private var minuteTimer: Timer?

    @Published var onboardingCompleted: Bool = false
    @Published var isFirstLaunch: Bool = true
    @Published var darkTheme: Bool?
    @Published var profile: UserProfile?
    @Published var sosContact: SosContact?
    @Published var moneyGoal: MoneyGoal?
    @Published var completedGoals: [CompletedMoneyGoal] = []
    @Published var homeStats: HomeStats?
    @Published var progressStats: ProgressStats?
    @Published var healthMilestones: [HealthMilestone] = []
    @Published var achievements: [Achievement] = []
    @Published var victories: [VictoryRecord] = []
    @Published var recoveryIndex = RecoveryIndex()
    @Published var personalLetter: String?
    @Published var notificationsEnabled: Bool = true
    @Published var notificationHour: Int = 9
    @Published var notificationMinute: Int = 0
    @Published var familyPhotoUri: String?
    @Published var sosState = SosUiState()

    init(repository: UserRepository) {
        self.repository = repository
        refreshAll()
        startMinuteTimer()
        repository.objectWillChange
            .receive(on: RunLoop.main)
            .sink { [weak self] _ in self?.refreshAll() }
            .store(in: &cancellables)
    }

    private var cancellables = Set<AnyCancellable>()

    private func startMinuteTimer() {
        minuteTimer?.invalidate()
        minuteTimer = Timer.scheduledTimer(withTimeInterval: 60, repeats: true) { [weak self] _ in
            Task { @MainActor in self?.refreshComputed() }
        }
    }

    func refreshAll() {
        onboardingCompleted = repository.onboardingCompleted
        isFirstLaunch = repository.isFirstLaunch
        darkTheme = repository.preferences.darkTheme
        profile = repository.profile
        sosContact = repository.sosContact
        moneyGoal = repository.moneyGoal
        completedGoals = repository.completedGoals
        personalLetter = repository.preferences.personalLetter
        notificationsEnabled = repository.preferences.notificationsEnabled
        notificationHour = repository.preferences.notificationHour
        notificationMinute = repository.preferences.notificationMinute
        familyPhotoUri = repository.preferences.familyPhotoUri
        refreshComputed()
    }

    func refreshComputed() {
        homeStats = repository.buildHomeStats()
        progressStats = repository.buildProgressStats()
        healthMilestones = repository.buildHealthMilestones()
        achievements = repository.buildAchievements()
        victories = repository.buildVictories()
        recoveryIndex = repository.computeRecoveryIndex()
    }

    func syncLaunchState() {
        repository.syncLaunchState()
        refreshAll()
    }

    func completeOnboarding(profile: UserProfile, sosContact: SosContact, personalLetter: String?) {
        repository.completeOnboarding(profile: profile, sosContact: sosContact, personalLetter: personalLetter)
        refreshAll()
    }

    func completeFirstLaunch() {
        repository.isFirstLaunch = false
        isFirstLaunch = false
    }

    func updateProfile(_ profile: UserProfile) {
        repository.saveProfile(profile)
        refreshAll()
    }

    func updateSosContact(_ contact: SosContact) {
        repository.saveSosContact(contact)
        refreshAll()
    }

    func updatePersonalLetter(_ letter: String?) {
        repository.preferences.personalLetter = letter
        personalLetter = letter
    }

    func saveMoneyGoal(_ goal: MoneyGoal) {
        repository.saveMoneyGoal(goal)
        refreshAll()
    }

    func archiveCompletedGoal() {
        repository.archiveCompletedGoal()
        refreshAll()
    }

    func isGoalReached() -> Bool {
        repository.checkGoalReached()
    }

    func moneySavedNow() -> Double {
        repository.moneySavedNow()
    }

    func updateLastSmokeDate(_ date: Date) {
        repository.updateLastSmokeDate(date)
        refreshAll()
    }

    func setDarkTheme(_ enabled: Bool) {
        repository.preferences.darkTheme = enabled
        darkTheme = enabled
    }

    func setNotificationsEnabled(_ enabled: Bool) {
        repository.preferences.notificationsEnabled = enabled
        notificationsEnabled = enabled
        NotificationManager.shared.reschedule(enabled: enabled, hour: notificationHour, minute: notificationMinute)
    }

    func setNotificationTime(hour: Int, minute: Int) {
        repository.preferences.notificationHour = hour
        repository.preferences.notificationMinute = minute
        notificationHour = hour
        notificationMinute = minute
        if notificationsEnabled {
            NotificationManager.shared.reschedule(enabled: true, hour: hour, minute: minute)
        }
    }

    func setFamilyPhotoUri(_ uri: String?) {
        repository.preferences.familyPhotoUri = uri
        familyPhotoUri = uri
    }

    func resetProgress() {
        repository.resetProgress()
        refreshAll()
    }

    func startSos() {
        messageSession = MessageSession()
        let dayOfYear = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1
        sosState = SosUiState(
            step: .intake,
            sosMode: SosMode.forDay(dayOfYear: dayOfYear)
        )
    }

    func confirmSosIntakeAndStartTimer() {
        let dayOfYear = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1
        sosState.step = .timer
        sosState.timerSecondsLeft = AppConstants.sosTimerSeconds
        sosState.elapsedSeconds = 0
        sosState.sosMode = SosMode.forDay(dayOfYear: dayOfYear)
        applySosMoment(0)
    }

    func setSosTrigger(_ trigger: String) { sosState.trigger = trigger }
    func setSosLocation(_ location: String) { sosState.location = location }
    func setSosIntensity(_ intensity: Int) { sosState.intensity = intensity }

    func showSosExitDialog() { sosState.showExitDialog = true }
    func dismissSosExitDialog() { sosState.showExitDialog = false }

    func exitSosWithoutTimer() {
        resetSos()
    }

    func restartSosTimer() {
        sosState.timerSecondsLeft = AppConstants.sosTimerSeconds
        sosState.elapsedSeconds = 0
        sosState.factShown = false
        sosState.letterShown = false
        applySosMoment(0)
        dismissSosExitDialog()
    }

    func startEmergencySos() {
        messageSession = MessageSession()
        sosState = SosUiState(step: .emergency, isEmergency: true)
    }

    func startTimerFromEmergency() {
        let dayOfYear = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1
        if messageSession == nil { messageSession = MessageSession() }
        sosState.step = .intake
        sosState.sosMode = SosMode.forDay(dayOfYear: dayOfYear)
    }

    func resumeTimer() {
        sosState.step = .timer
        applySosMoment(sosState.elapsedSeconds)
    }

    func startBreathing() {
        sosState.step = .breathing
        sosState.breathingPhase = 0
    }

    func showOptionalDetails() { sosState.showOptionalDetails = true }
    func dismissOptionalDetails() { sosState.showOptionalDetails = false }
    func setOptionalTrigger(_ trigger: String) { sosState.trigger = trigger }
    func setOptionalIntensity(_ intensity: Int) { sosState.intensity = intensity }

    func tickTimer() {
        guard sosState.step == .timer else { return }
        if sosState.timerSecondsLeft <= 1 {
            sosState.step = .result
            sosState.timerSecondsLeft = 0
        } else {
            sosState.timerSecondsLeft -= 1
            sosState.elapsedSeconds = AppConstants.sosTimerSeconds - sosState.timerSecondsLeft
            if shouldUpdateSosMessage(sosState.elapsedSeconds) {
                applySosMoment(sosState.elapsedSeconds)
            }
        }
    }

    private func shouldUpdateSosMessage(_ elapsed: Int) -> Bool {
        elapsed == 0 || elapsed % AppConstants.sosMessageIntervalSeconds == 0
    }

    private func applySosMoment(_ elapsed: Int) {
        guard let session = messageSession else { return }
        let canShowLetter = repository.preferences.canShowPersonalLetterToday()
        let moment = SosMessageComposer.compose(
            elapsedSeconds: elapsed,
            ctx: buildSupportContext(),
            session: session,
            mode: sosState.sosMode,
            flags: SosComposeFlags(factShown: sosState.factShown, letterShown: sosState.letterShown)
        )
        sosState.currentMessage = moment.primary
        sosState.secondaryMessage = moment.secondary
        sosState.currentAction = moment.action
        let shouldShowLetter = moment.showPersonalLetter && !sosState.letterShown && canShowLetter
        sosState.showPersonalLetterDialog = shouldShowLetter
        if elapsed == 30 && moment.secondary != nil { sosState.factShown = true }
    }

    private func buildSupportContext() -> SupportContext {
        let p = profile
        let goal = moneyGoal
        let pricePerCig: Double
        if let p, p.cigarettesInPack > 0 {
            pricePerCig = p.packPrice / Double(p.cigarettesInPack)
        } else {
            pricePerCig = 0
        }
        return SupportContext(
            reasons: p?.reasons ?? [],
            goalTitle: goal?.title,
            currency: p?.currency ?? "₸",
            pricePerCigarette: pricePerCig,
            moneySaved: p.map { StatsCalculator.moneySaved(profile: $0) } ?? 0,
            totalWins: victories.count,
            personalLetter: personalLetter,
            hourOfDay: Calendar.current.component(.hour, from: Date())
        )
    }

    func tickBreathing() {
        guard sosState.step == .breathing else { return }
        sosState.breathingPhase += 1
    }

    func dismissPersonalLetter() {
        sosState.showPersonalLetterDialog = false
        sosState.letterShown = true
        repository.preferences.markPersonalLetterShownToday()
    }

    func showPhoto() { sosState.showPhoto = true }
    func dismissPhoto() { sosState.showPhoto = false }
    func showReasons() { sosState.showReasons = true }
    func dismissReasons() { sosState.showReasons = false }

    func completeSos(success: Bool) {
        let session = messageSession
        let recoveryPair = success ? repository.applySosRecoveryBoost() : (RecoveryIndex(), RecoveryIndex())
        repository.recordCraving(
            intensity: sosState.intensity,
            trigger: sosState.trigger.isEmpty ? "Не указано" : sosState.trigger,
            location: sosState.location,
            success: success
        )
        refreshAll()

        let endMessage: String
        if success, let session {
            endMessage = SosMessageComposer.victoryMessage(session: session)
        } else if !success, let session {
            endMessage = SosMessageComposer.relapseMessage(session: session)
        } else {
            endMessage = ""
        }

        sosState.step = success ? .success : .relapse
        sosState.success = success
        sosState.showCoinAnimation = success
        sosState.currentMessage = endMessage
        sosState.recoveryBefore = recoveryPair.0
        sosState.recoveryAfter = recoveryPair.1
    }

    func dismissCoinAnimation() { sosState.showCoinAnimation = false }

    func resetSos() {
        messageSession = nil
        sosState = SosUiState()
    }

    func quitReasons() -> [String] { profile?.reasons ?? [] }

    func liveHealthNow() -> (String, String) {
        guard let profile else {
            return ("Сейчас", "Начни путь — и тело сразу начнёт восстанавливаться.")
        }
        let elapsed = StatsCalculator.elapsedMillis(lastSmokeDate: profile.lastSmokeDate)
        return AppConstants.liveHealthMessage(elapsedMillis: elapsed)
    }
}
