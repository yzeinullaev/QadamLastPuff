package com.qadam.lastpuff.domain.model

data class UserProfile(
    val id: Long = 1L,
    val smokeType: String,
    val cigarettesPerDay: Int,
    val packPrice: Double,
    val cigarettesInPack: Int,
    val lastSmokeDate: Long,
    val reasons: List<String>,
    val currency: String = "₸"
)

data class CravingEvent(
    val id: Long = 0L,
    val createdAt: Long,
    val intensity: Int,
    val trigger: String,
    val success: Boolean
)

data class RelapseEvent(
    val id: Long = 0L,
    val createdAt: Long,
    val note: String? = null
)

data class MoneyGoal(
    val id: Long = 0L,
    val title: String,
    val amount: Double
)

data class SosContact(
    val id: Long = 0L,
    val name: String,
    val phone: String,
    val message: String
)

data class HealthMilestone(
    val id: String,
    val title: String,
    val description: String,
    val durationMillis: Long,
    val isUnlocked: Boolean
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)

data class ProgressStats(
    val daysWithoutSmoking: Int,
    val hoursWithoutSmoking: Long,
    val cigarettesNotSmoked: Int,
    val moneySaved: Double,
    val totalCravings: Int,
    val cravingsWon: Int,
    val relapses: Int,
    val winRate: Float,
    val longestStreak: Int,
    val dangerousHours: List<Pair<Int, Int>>,
    val topTriggers: List<Pair<String, Int>>,
    val averageIntensity: Float
)

data class HomeStats(
    val days: Int,
    val hours: Long,
    val minutes: Long,
    val cigarettesNotSmoked: Int,
    val moneySaved: Double,
    val motivationalQuote: String,
    val nextAchievement: Achievement?,
    val lastVictoryAgo: String?,
    val dailyLifeCard: String,
    val personalReason: String?,
    val totalCoins: Int,
    val isJustStarted: Boolean
)

data class VictoryRecord(
    val id: Long,
    val time: String,
    val trigger: String,
    val intensity: Int,
    val message: String
)
