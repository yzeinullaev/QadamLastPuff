import Foundation
import Combine

@MainActor
final class UserRepository: ObservableObject {
    let store: DataStore
    let preferences: PreferencesManager
    private var cancellables = Set<AnyCancellable>()

    init(store: DataStore, preferences: PreferencesManager) {
        self.store = store
        self.preferences = preferences
        syncLaunchState()
        store.objectWillChange
            .receive(on: RunLoop.main)
            .sink { [weak self] _ in self?.objectWillChange.send() }
            .store(in: &cancellables)
    }

    var onboardingCompleted: Bool {
        get { preferences.onboardingCompleted }
        set { preferences.onboardingCompleted = newValue }
    }

    var isFirstLaunch: Bool {
        get { preferences.isFirstLaunch }
        set { preferences.isFirstLaunch = newValue }
    }

    var profile: UserProfile? { store.data.profile }
    var sosContact: SosContact? { store.data.sosContact }
    var moneyGoal: MoneyGoal? { store.data.moneyGoal }
    var completedGoals: [CompletedMoneyGoal] { store.data.completedGoals }
    var cravings: [CravingEvent] { store.data.cravings }
    var relapses: [RelapseEvent] { store.data.relapses }

    /// Синхронизирует флаги запуска с реальными данными профиля.
    func syncLaunchState() {
        if profile != nil {
            if !preferences.onboardingCompleted {
                preferences.onboardingCompleted = true
            }
        } else if preferences.onboardingCompleted {
            // Флаг есть, а профиля нет — сбрасываем, чтобы снова показать onboarding.
            preferences.onboardingCompleted = false
        }
    }

    func saveProfile(_ profile: UserProfile) {
        store.update { $0.profile = profile }
        checkAndUnlockAchievements()
    }

    func completeOnboarding(profile: UserProfile, sosContact: SosContact, personalLetter: String?) {
        store.update {
            $0.profile = profile
            $0.sosContact = sosContact
        }
        preferences.personalLetter = personalLetter
        preferences.onboardingCompleted = true
    }

    func saveSosContact(_ contact: SosContact) {
        store.update { data in
            var c = contact
            if let existing = data.sosContact { c.id = existing.id }
            data.sosContact = c
        }
    }

    func saveMoneyGoal(_ goal: MoneyGoal) {
        store.update { data in
            var g = goal
            if let existing = data.moneyGoal { g.id = existing.id }
            data.moneyGoal = g
        }
        checkGoalReached()
    }

    func archiveCompletedGoal() {
        guard let profile, let goal = moneyGoal else { return }
        let saved = StatsCalculator.moneySaved(profile: profile)
        guard saved >= goal.amount else { return }
        store.update { data in
            let completed = CompletedMoneyGoal(
                id: data.nextCompletedGoalId,
                title: goal.title,
                amount: goal.amount,
                savedAmount: saved,
                completedAt: Date()
            )
            data.nextCompletedGoalId += 1
            data.completedGoals.insert(completed, at: 0)
            data.moneyGoal = nil
        }
    }

    func checkGoalReached() -> Bool {
        guard let profile, let goal = moneyGoal else { return false }
        return StatsCalculator.moneySaved(profile: profile) >= goal.amount
    }

    func moneySavedNow() -> Double {
        guard let profile else { return 0 }
        return StatsCalculator.moneySaved(profile: profile)
    }

    @discardableResult
    func recordCraving(intensity: Int, trigger: String, location: String, success: Bool) -> Int {
        store.update { data in
            let event = CravingEvent(
                id: data.nextCravingId,
                createdAt: Date(),
                intensity: intensity,
                trigger: trigger,
                location: location,
                success: success
            )
            data.nextCravingId += 1
            data.cravings.insert(event, at: 0)

            if !success {
                let relapse = RelapseEvent(id: data.nextRelapseId, createdAt: Date())
                data.nextRelapseId += 1
                data.relapses.insert(relapse, at: 0)
                if var profile = data.profile {
                    profile.lastSmokeDate = Date()
                    data.profile = profile
                }
            }
        }
        if success {
            let coins = AppConstants.coinReward(for: intensity)
            preferences.addCoins(coins)
            checkAndUnlockAchievements()
            return coins
        }
        checkAndUnlockAchievements()
        return 0
    }

    func recordRelapse() {
        recordCraving(intensity: 5, trigger: "Срыв", location: "", success: false)
    }

    func applySosRecoveryBoost() -> (RecoveryIndex, RecoveryIndex) {
        guard let profile else { return (RecoveryIndex(), RecoveryIndex()) }
        let now = Date()
        let elapsed = StatsCalculator.elapsedMillis(lastSmokeDate: profile.lastSmokeDate, now: now)
        let wins = cravings.filter(\.success).count
        let before = computeRecoveryIndex(elapsed: elapsed, wins: wins)
        let after = (before + RecoveryBoosts.sosWin).clamped()
        preferences.recoveryIndexRaw = RecoveryCalculator.encode(after)
        return (before, after)
    }

    func updateLastSmokeDate(_ date: Date) {
        guard var p = profile else { return }
        p.lastSmokeDate = date
        saveProfile(p)
    }

    func resetProgress() {
        store.update { data in
            data.cravings = []
            data.relapses = []
            if var profile = data.profile {
                profile.lastSmokeDate = Date()
                data.profile = profile
            }
        }
        preferences.clearAll()
        preferences.onboardingCompleted = true
    }

    func computeRecoveryIndex(now: Date = Date()) -> RecoveryIndex {
        guard let profile else { return RecoveryIndex() }
        let elapsed = StatsCalculator.elapsedMillis(lastSmokeDate: profile.lastSmokeDate, now: now)
        let wins = cravings.filter(\.success).count
        return computeRecoveryIndex(elapsed: elapsed, wins: wins)
    }

    private func computeRecoveryIndex(elapsed: Int64, wins: Int) -> RecoveryIndex {
        let stored = RecoveryCalculator.decode(preferences.recoveryIndexRaw)
        let timeBased = RecoveryCalculator.fromTimeWithoutSmoking(elapsedMs: elapsed, wins: wins)
        return RecoveryCalculator.merge(stored: stored, timeBased: timeBased)
    }

    func buildHomeStats(now: Date = Date()) -> HomeStats? {
        guard let profile else { return nil }
        let dayIndex = Int(now.timeIntervalSince1970 / 86400)
        let hour = Calendar.current.component(.hour, from: now)
        let session = MessageSession(seed: dayIndex)
        let quote = SupportMessageBank.dayMessage(hour: hour, session: session)
        let achievements = buildAchievements(now: now)
        let nextAchievement = achievements.first { !$0.isUnlocked }
        let days = StatsCalculator.daysWithoutSmoking(lastSmokeDate: profile.lastSmokeDate, now: now)
        let hours = StatsCalculator.hoursWithoutSmoking(lastSmokeDate: profile.lastSmokeDate, now: now) % 24
        let lastWin = cravings.first { $0.success }

        return HomeStats(
            days: days,
            hours: hours,
            minutes: StatsCalculator.minutesWithoutSmoking(lastSmokeDate: profile.lastSmokeDate, now: now),
            cigarettesNotSmoked: StatsCalculator.cigarettesNotSmoked(profile: profile, now: now),
            moneySaved: StatsCalculator.moneySaved(profile: profile, now: now),
            motivationalQuote: quote,
            nextAchievement: nextAchievement,
            lastVictoryAgo: lastWin.map { TimeFormatUtils.formatAgo($0.createdAt, now: now) },
            dailyLifeCard: AppConstants.dailyLifeCard(days: days, hours: hours),
            personalReason: SupportMessageBank.memoryForReason(profile.reasons.first ?? "", goalTitle: moneyGoal?.title)
                ?? AppConstants.personalReasonMessage(reasons: profile.reasons, goalTitle: moneyGoal?.title),
            totalCoins: preferences.totalCoins,
            isJustStarted: days == 0 && hours < 1
        )
    }

    func buildProgressStats(now: Date = Date()) -> ProgressStats? {
        guard let profile else { return nil }
        let days = StatsCalculator.daysWithoutSmoking(lastSmokeDate: profile.lastSmokeDate, now: now)
        let longestStreak = max(days, preferences.longestStreak)
        if days > preferences.longestStreak { preferences.longestStreak = days }
        let wins = cravings.filter(\.success).count

        return ProgressStats(
            daysWithoutSmoking: days,
            hoursWithoutSmoking: StatsCalculator.hoursWithoutSmoking(lastSmokeDate: profile.lastSmokeDate, now: now),
            cigarettesNotSmoked: StatsCalculator.cigarettesNotSmoked(profile: profile, now: now),
            moneySaved: StatsCalculator.moneySaved(profile: profile, now: now),
            totalCravings: cravings.count,
            cravingsWon: wins,
            relapses: relapses.count,
            winRate: StatsCalculator.winRate(wins: wins, total: cravings.count),
            longestStreak: longestStreak,
            dangerousHours: StatsCalculator.dangerousHours(events: cravings.map(\.createdAt)),
            topTriggers: StatsCalculator.topTriggers(triggers: cravings.map(\.trigger)),
            averageIntensity: StatsCalculator.averageIntensity(intensities: cravings.map(\.intensity))
        )
    }

    func buildVictories() -> [VictoryRecord] {
        cravings.filter(\.success).map { event in
            let message: String
            if event.intensity >= 8 {
                message = "Очень хотелось. Справился."
            } else if !event.trigger.isEmpty {
                message = "После «\(event.trigger.lowercased())». Не закурил."
            } else {
                message = "Справился с тягой."
            }
            return VictoryRecord(
                id: event.id,
                time: TimeFormatUtils.formatTime(event.createdAt),
                trigger: event.trigger.isEmpty ? "Тяга" : event.trigger,
                intensity: event.intensity,
                message: message
            )
        }
    }

    func buildHealthMilestones(now: Date = Date()) -> [HealthMilestone] {
        guard let profile else { return [] }
        let elapsed = StatsCalculator.elapsedMillis(lastSmokeDate: profile.lastSmokeDate, now: now)
        return AppConstants.healthMilestones.map { title, desc, duration in
            HealthMilestone(id: title, title: title, description: desc, durationMillis: duration, isUnlocked: elapsed >= duration)
        }
    }

    func buildAchievements(now: Date = Date()) -> [Achievement] {
        guard let profile else {
            return AppConstants.achievements.map { id, title, desc in
                Achievement(id: id, title: title, description: desc, isUnlocked: false)
            }
        }
        let unlocked = preferences.unlockedAchievements
        let elapsed = StatsCalculator.elapsedMillis(lastSmokeDate: profile.lastSmokeDate, now: now)
        let days = StatsCalculator.daysWithoutSmoking(lastSmokeDate: profile.lastSmokeDate, now: now)
        let cigarettes = StatsCalculator.cigarettesNotSmoked(profile: profile, now: now)
        let money = StatsCalculator.moneySaved(profile: profile, now: now)
        let wins = cravings.filter(\.success).count
        let hasRelapse = cravings.contains { !$0.success }
        let totalCoins = preferences.totalCoins

        let conditions: [String: Bool] = [
            "first_hour": elapsed >= 60 * 60 * 1000,
            "first_day": days >= 1,
            "three_days": days >= 3,
            "seven_days": days >= 7,
            "thirty_days": days >= 30,
            "hundred_days": days >= 100,
            "cigarettes_100": cigarettes >= 100,
            "money_10000": money >= 10000,
            "wins_10": wins >= 10,
            "wins_50": wins >= 50,
            "coins_20": totalCoins >= 20,
            "coins_40": totalCoins >= 40,
            "coins_80": totalCoins >= 80,
            "coins_110": totalCoins >= 110,
            "relapse_survived": hasRelapse
        ]

        return AppConstants.achievements.map { id, title, desc in
            Achievement(id: id, title: title, description: desc, isUnlocked: unlocked.contains(id) || (conditions[id] == true))
        }
    }

    private func checkAndUnlockAchievements() {
        guard let profile else { return }
        let achievements = buildAchievements()
        let unlocked = preferences.unlockedAchievements
        for achievement in achievements where achievement.isUnlocked && !unlocked.contains(achievement.id) {
            preferences.unlockAchievement(achievement.id)
        }
        _ = profile
    }
}
