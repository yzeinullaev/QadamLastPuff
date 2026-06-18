package com.qadam.lastpuff.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatAgo(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - timestamp
        if (diff < 0) return "только что"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            minutes < 1 -> "только что"
            minutes < 60 -> "$minutes мин. назад"
            hours < 24 -> "$hours ч. назад"
            else -> "$days дн. назад"
        }
    }

    fun intensityStars(intensity: Int): String =
        "★".repeat(intensity.coerceIn(1, 5)) + "☆".repeat((5 - intensity).coerceAtLeast(0))
}
