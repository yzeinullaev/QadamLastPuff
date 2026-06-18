package com.qadam.lastpuff.util

import com.qadam.lastpuff.domain.model.UserProfile
import java.util.Calendar
import kotlin.math.floor

object StatsCalculator {
    fun elapsedMillis(lastSmokeDate: Long, now: Long = System.currentTimeMillis()): Long =
        (now - lastSmokeDate).coerceAtLeast(0)

    fun daysWithoutSmoking(lastSmokeDate: Long, now: Long = System.currentTimeMillis()): Int =
        (elapsedMillis(lastSmokeDate, now) / (24 * 60 * 60 * 1000L)).toInt()

    fun hoursWithoutSmoking(lastSmokeDate: Long, now: Long = System.currentTimeMillis()): Long =
        elapsedMillis(lastSmokeDate, now) / (60 * 60 * 1000L)

    fun minutesWithoutSmoking(lastSmokeDate: Long, now: Long = System.currentTimeMillis()): Long =
        (elapsedMillis(lastSmokeDate, now) / (60 * 1000L)) % 60

    fun cigarettesNotSmoked(profile: UserProfile, now: Long = System.currentTimeMillis()): Int {
        val days = elapsedMillis(profile.lastSmokeDate, now).toDouble() / (24 * 60 * 60 * 1000.0)
        return floor(days * profile.cigarettesPerDay).toInt()
    }

    fun moneySaved(profile: UserProfile, now: Long = System.currentTimeMillis()): Double {
        if (profile.cigarettesInPack <= 0) return 0.0
        val pricePerCigarette = profile.packPrice / profile.cigarettesInPack
        return cigarettesNotSmoked(profile, now) * pricePerCigarette
    }

    fun winRate(wins: Int, total: Int): Float =
        if (total == 0) 0f else wins.toFloat() / total * 100f

    fun dangerousHours(events: List<Long>): List<Pair<Int, Int>> {
        val hourCounts = IntArray(24)
        events.forEach { timestamp ->
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            hourCounts[cal.get(Calendar.HOUR_OF_DAY)]++
        }
        return hourCounts.mapIndexed { hour, count -> hour to count }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(3)
    }

    fun topTriggers(triggers: List<String>, limit: Int = 5): List<Pair<String, Int>> =
        triggers.groupingBy { it }.eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }

    fun averageIntensity(intensities: List<Int>): Float =
        if (intensities.isEmpty()) 0f else intensities.average().toFloat()

    fun currentStreakDays(lastSmokeDate: Long, relapses: List<Long>, now: Long = System.currentTimeMillis()): Int {
        if (relapses.isEmpty()) return daysWithoutSmoking(lastSmokeDate, now)
        val lastRelapse = relapses.max()
        return daysWithoutSmoking(lastRelapse, now)
    }
}
