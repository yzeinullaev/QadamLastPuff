package com.qadam.lastpuff.data.repository

import com.qadam.lastpuff.data.datastore.PreferencesManager
import com.qadam.lastpuff.data.local.dao.CravingEventDao
import com.qadam.lastpuff.data.local.dao.MoneyGoalDao
import com.qadam.lastpuff.data.local.dao.RelapseEventDao
import com.qadam.lastpuff.data.local.dao.SosContactDao
import com.qadam.lastpuff.data.local.dao.UserProfileDao
import com.qadam.lastpuff.data.local.entity.RelapseEventEntity
import com.qadam.lastpuff.data.mapper.toDomain
import com.qadam.lastpuff.data.mapper.toEntity
import com.qadam.lastpuff.domain.model.Achievement
import com.qadam.lastpuff.domain.model.CravingEvent
import com.qadam.lastpuff.domain.model.HealthMilestone
import com.qadam.lastpuff.domain.model.HomeStats
import com.qadam.lastpuff.domain.model.MoneyGoal
import com.qadam.lastpuff.domain.model.ProgressStats
import com.qadam.lastpuff.domain.model.RecoveryBoosts
import com.qadam.lastpuff.domain.model.RecoveryIndex
import com.qadam.lastpuff.domain.model.SosContact
import com.qadam.lastpuff.domain.model.UserProfile
import com.qadam.lastpuff.domain.model.VictoryRecord
import com.qadam.lastpuff.domain.support.MessageSession
import com.qadam.lastpuff.domain.support.SupportMessageBank
import com.qadam.lastpuff.util.AppConstants
import com.qadam.lastpuff.util.RecoveryCalculator
import com.qadam.lastpuff.util.StatsCalculator
import com.qadam.lastpuff.util.TimeFormatUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userProfileDao: UserProfileDao,
    private val cravingEventDao: CravingEventDao,
    private val relapseEventDao: RelapseEventDao,
    private val moneyGoalDao: MoneyGoalDao,
    private val sosContactDao: SosContactDao,
    private val preferencesManager: PreferencesManager
) {
    val onboardingCompleted: Flow<Boolean> = preferencesManager.onboardingCompleted
    val darkTheme: Flow<Boolean?> = preferencesManager.darkTheme
    val notificationsEnabled: Flow<Boolean> = preferencesManager.notificationsEnabled
    val notificationHour: Flow<Int> = preferencesManager.notificationHour
    val notificationMinute: Flow<Int> = preferencesManager.notificationMinute
    val familyPhotoUri: Flow<String?> = preferencesManager.familyPhotoUri
    val personalLetter: Flow<String?> = preferencesManager.personalLetter

    fun observeProfile(): Flow<UserProfile?> =
        userProfileDao.observeProfile().map { it?.toDomain() }

    fun observeSosContact(): Flow<SosContact?> =
        sosContactDao.observeContact().map { it?.toDomain() }

    fun observeMoneyGoal(): Flow<MoneyGoal?> =
        moneyGoalDao.observeActiveGoal().map { it?.toDomain() }

    fun observeCravingEvents(): Flow<List<CravingEvent>> =
        cravingEventDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getProfile(): UserProfile? = userProfileDao.getProfile()?.toDomain()

    suspend fun saveProfile(profile: UserProfile) {
        userProfileDao.insert(profile.toEntity())
    }

    suspend fun completeOnboarding(
        profile: UserProfile,
        sosContact: SosContact,
        personalLetter: String?
    ) {
        userProfileDao.insert(profile.toEntity())
        sosContactDao.insert(sosContact.toEntity())
        preferencesManager.setPersonalLetter(personalLetter)
        preferencesManager.setOnboardingCompleted(true)
    }

    suspend fun saveSosContact(contact: SosContact) {
        val existing = sosContactDao.observeContact().first()
        if (existing != null) {
            sosContactDao.update(contact.copy(id = existing.id).toEntity())
        } else {
            sosContactDao.insert(contact.toEntity())
        }
    }

    suspend fun saveMoneyGoal(goal: MoneyGoal) {
        val existing = moneyGoalDao.observeActiveGoal().first()
        if (existing != null) {
            moneyGoalDao.update(goal.copy(id = existing.id).toEntity())
        } else {
            moneyGoalDao.insert(goal.toEntity())
        }
    }

    suspend fun recordCraving(intensity: Int, trigger: String, success: Boolean) {
        val event = CravingEvent(
            createdAt = System.currentTimeMillis(),
            intensity = intensity,
            trigger = trigger,
            success = success
        )
        cravingEventDao.insert(event.toEntity())

        if (!success) {
            relapseEventDao.insert(
                RelapseEventEntity(createdAt = System.currentTimeMillis())
            )
            val profile = getProfile() ?: return
            userProfileDao.update(
                profile.copy(lastSmokeDate = System.currentTimeMillis()).toEntity()
            )
        } else {
            preferencesManager.addCoin()
        }

        checkAndUnlockAchievements()
    }

    suspend fun applySosRecoveryBoost(): Pair<RecoveryIndex, RecoveryIndex> {
        val profile = getProfile() ?: return RecoveryIndex() to RecoveryIndex()
        val cravings = cravingEventDao.getAll().map { it.toDomain() }
        val now = System.currentTimeMillis()
        val elapsed = StatsCalculator.elapsedMillis(profile.lastSmokeDate, now)
        val wins = cravings.count { it.success }
        val before = computeRecoveryIndex(elapsed, wins)
        val after = (before + RecoveryBoosts.SOS_WIN).clamp()
        preferencesManager.setRecoveryIndex(RecoveryCalculator.encode(after))
        return before to after
    }

    fun observeRecoveryIndex(
        nowFlow: Flow<Long> = kotlinx.coroutines.flow.flowOf(System.currentTimeMillis())
    ): Flow<RecoveryIndex> = combine(
        preferencesManager.recoveryIndexRaw,
        observeProfile(),
        observeCravingEvents(),
        nowFlow
    ) { raw, profile, cravings, now ->
        val profileData = profile ?: return@combine RecoveryIndex()
        val elapsed = StatsCalculator.elapsedMillis(profileData.lastSmokeDate, now)
        val wins = cravings.count { it.success }
        computeRecoveryIndexFromRaw(raw, elapsed, wins)
    }

    private fun computeRecoveryIndexFromRaw(raw: String?, elapsed: Long, wins: Int): RecoveryIndex {
        val stored = RecoveryCalculator.decode(raw)
        val timeBased = RecoveryCalculator.fromTimeWithoutSmoking(elapsed, wins)
        return RecoveryCalculator.merge(stored, timeBased)
    }

    private suspend fun computeRecoveryIndex(elapsed: Long, wins: Int): RecoveryIndex {
        val raw = preferencesManager.recoveryIndexRaw.first()
        return computeRecoveryIndexFromRaw(raw, elapsed, wins)
    }

    suspend fun updateLastSmokeDate(timestamp: Long) {
        val profile = getProfile() ?: return
        userProfileDao.update(profile.copy(lastSmokeDate = timestamp).toEntity())
        checkAndUnlockAchievements()
    }

    suspend fun resetProgress() {
        cravingEventDao.deleteAll()
        relapseEventDao.deleteAll()
        val profile = getProfile()
        if (profile != null) {
            userProfileDao.update(
                profile.copy(lastSmokeDate = System.currentTimeMillis()).toEntity()
            )
        }
        preferencesManager.clearAll()
        preferencesManager.setOnboardingCompleted(true)
    }

    suspend fun setDarkTheme(enabled: Boolean) = preferencesManager.setDarkTheme(enabled)
    suspend fun setNotificationsEnabled(enabled: Boolean) = preferencesManager.setNotificationsEnabled(enabled)
    suspend fun setNotificationTime(hour: Int, minute: Int) = preferencesManager.setNotificationTime(hour, minute)
    suspend fun setPersonalLetter(letter: String?) = preferencesManager.setPersonalLetter(letter)
    suspend fun setFamilyPhotoUri(uri: String?) = preferencesManager.setFamilyPhotoUri(uri)

    fun observeHomeStats(nowFlow: Flow<Long> = kotlinx.coroutines.flow.flowOf(System.currentTimeMillis())): Flow<HomeStats?> =
        combine(
            combine(
                observeProfile(),
                observeCravingEvents(),
                observeMoneyGoal()
            ) { profile, cravings, goal -> Triple(profile, cravings, goal) },
            combine(
                preferencesManager.unlockedAchievementsFlow(),
                preferencesManager.totalCoinsFlow(),
                nowFlow
            ) { unlocked, coins, now -> Triple(unlocked, coins, now) }
        ) { data, meta ->
            val (profile, cravings, goal) = data
            val (unlocked, coins, now) = meta
            profile ?: return@combine null
            val dayIndex = (now / (24 * 60 * 60 * 1000L)).toInt()
            val hour = java.util.Calendar.getInstance().apply { timeInMillis = now }
                .get(java.util.Calendar.HOUR_OF_DAY)
            val daySession = MessageSession(dayIndex)
            val quote = SupportMessageBank.dayMessage(hour, daySession)
            val achievements = buildAchievements(profile, unlocked, cravings)
            val nextAchievement = achievements.firstOrNull { !it.isUnlocked }
            val days = StatsCalculator.daysWithoutSmoking(profile.lastSmokeDate, now)
            val hours = StatsCalculator.hoursWithoutSmoking(profile.lastSmokeDate, now) % 24
            val lastWin = cravings.firstOrNull { it.success }

            HomeStats(
                days = days,
                hours = hours,
                minutes = StatsCalculator.minutesWithoutSmoking(profile.lastSmokeDate, now),
                cigarettesNotSmoked = StatsCalculator.cigarettesNotSmoked(profile, now),
                moneySaved = StatsCalculator.moneySaved(profile, now),
                motivationalQuote = quote,
                nextAchievement = nextAchievement,
                lastVictoryAgo = lastWin?.let { TimeFormatUtils.formatAgo(it.createdAt, now) },
                dailyLifeCard = AppConstants.dailyLifeCard(days, hours),
                personalReason = SupportMessageBank.memoryForReason(
                    profile.reasons.firstOrNull() ?: "",
                    goal?.title
                ) ?: AppConstants.personalReasonMessage(profile.reasons, goal?.title),
                totalCoins = coins,
                isJustStarted = days == 0 && hours < 1
            )
        }

    fun observeVictories(): Flow<List<VictoryRecord>> =
        observeCravingEvents().map { events ->
            events.filter { it.success }.map { event ->
                VictoryRecord(
                    id = event.id,
                    time = TimeFormatUtils.formatTime(event.createdAt),
                    trigger = event.trigger.ifBlank { "Тяга" },
                    intensity = event.intensity,
                    message = when {
                        event.intensity >= 8 -> "Очень хотелось. Справился."
                        event.trigger.isNotBlank() -> "После «${event.trigger.lowercase()}». Не закурил."
                        else -> "Справился с тягой."
                    }
                )
            }
        }

    fun observeProgressStats(nowFlow: Flow<Long> = kotlinx.coroutines.flow.flowOf(System.currentTimeMillis())): Flow<ProgressStats?> =
        combine(
            observeProfile(),
            observeCravingEvents(),
            relapseEventDao.observeAll(),
            preferencesManager.longestStreakFlow(),
            nowFlow
        ) { profile, cravings, relapses, storedStreak, now ->
            profile ?: return@combine null
            val days = StatsCalculator.daysWithoutSmoking(profile.lastSmokeDate, now)
            val longestStreak = maxOf(days, storedStreak)
            val wins = cravings.count { it.success }
            val relapseCount = relapses.size

            ProgressStats(
                daysWithoutSmoking = days,
                hoursWithoutSmoking = StatsCalculator.hoursWithoutSmoking(profile.lastSmokeDate, now),
                cigarettesNotSmoked = StatsCalculator.cigarettesNotSmoked(profile, now),
                moneySaved = StatsCalculator.moneySaved(profile, now),
                totalCravings = cravings.size,
                cravingsWon = wins,
                relapses = relapseCount,
                winRate = StatsCalculator.winRate(wins, cravings.size),
                longestStreak = longestStreak,
                dangerousHours = StatsCalculator.dangerousHours(cravings.map { it.createdAt }),
                topTriggers = StatsCalculator.topTriggers(cravings.map { it.trigger }),
                averageIntensity = StatsCalculator.averageIntensity(cravings.map { it.intensity })
            )
        }

    fun observeHealthMilestones(nowFlow: Flow<Long> = kotlinx.coroutines.flow.flowOf(System.currentTimeMillis())): Flow<List<HealthMilestone>> =
        combine(observeProfile(), nowFlow) { profile, now ->
            val lastSmoke = profile?.lastSmokeDate ?: return@combine emptyList()
            val elapsed = StatsCalculator.elapsedMillis(lastSmoke, now)
            AppConstants.HEALTH_MILESTONES.map { (title, desc, duration) ->
                HealthMilestone(
                    id = title,
                    title = title,
                    description = desc,
                    durationMillis = duration,
                    isUnlocked = elapsed >= duration
                )
            }
        }

    fun observeAchievements(): Flow<List<Achievement>> =
        combine(observeProfile(), observeCravingEvents(), preferencesManager.unlockedAchievementsFlow()) { profile, cravings, unlocked ->
            if (profile == null) emptyList()
            else buildAchievements(profile, unlocked, cravings)
        }

    private suspend fun checkAndUnlockAchievements() {
        val profile = getProfile() ?: return
        val cravings = cravingEventDao.getAll().map { it.toDomain() }
        val unlocked = preferencesManager.unlockedAchievementsFlow().first()
        val achievements = buildAchievements(profile, unlocked, cravings)
        achievements.filter { it.isUnlocked && it.id !in unlocked }.forEach {
            preferencesManager.unlockAchievement(it.id)
        }
    }

    private fun buildAchievements(
        profile: UserProfile,
        unlocked: Set<String>,
        cravings: List<CravingEvent> = emptyList()
    ): List<Achievement> {
        val now = System.currentTimeMillis()
        val elapsed = StatsCalculator.elapsedMillis(profile.lastSmokeDate, now)
        val days = StatsCalculator.daysWithoutSmoking(profile.lastSmokeDate, now)
        val cigarettes = StatsCalculator.cigarettesNotSmoked(profile, now)
        val money = StatsCalculator.moneySaved(profile, now)
        val wins = cravings.count { it.success }
        val hasRelapse = cravings.any { !it.success }

        val conditions = mapOf(
            "first_hour" to (elapsed >= 60 * 60 * 1000L),
            "first_day" to (days >= 1),
            "three_days" to (days >= 3),
            "seven_days" to (days >= 7),
            "thirty_days" to (days >= 30),
            "hundred_days" to (days >= 100),
            "cigarettes_100" to (cigarettes >= 100),
            "money_10000" to (money >= 10000),
            "wins_10" to (wins >= 10),
            "wins_50" to (wins >= 50),
            "relapse_survived" to hasRelapse
        )

        return AppConstants.ACHIEVEMENTS.map { (id, title, desc) ->
            val isUnlocked = id in unlocked || conditions[id] == true
            Achievement(id = id, title = title, description = desc, isUnlocked = isUnlocked)
        }
    }
}
