package com.qadam.lastpuff.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qadam_prefs")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    private object Keys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val QUOTE_INDEX = intPreferencesKey("quote_index")
        val QUOTE_DATE = longPreferencesKey("quote_date")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val TOTAL_COINS = intPreferencesKey("total_coins")
        val FAMILY_PHOTO_URI = stringPreferencesKey("family_photo_uri")
        val PERSONAL_LETTER = stringPreferencesKey("personal_letter")
        val RECOVERY_INDEX = stringPreferencesKey("recovery_index")
    }

    val isFirstLaunch: Flow<Boolean> =
        dataStore.data.map { it[Keys.IS_FIRST_LAUNCH] ?: true }

    val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }
    val darkTheme: Flow<Boolean?> = dataStore.data.map { it[Keys.DARK_THEME] }
    val notificationsEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val notificationHour: Flow<Int> = dataStore.data.map { it[Keys.NOTIFICATION_HOUR] ?: 9 }
    val notificationMinute: Flow<Int> = dataStore.data.map { it[Keys.NOTIFICATION_MINUTE] ?: 0 }
    val familyPhotoUri: Flow<String?> = dataStore.data.map { it[Keys.FAMILY_PHOTO_URI] }
    val personalLetter: Flow<String?> = dataStore.data.map { it[Keys.PERSONAL_LETTER] }
    val recoveryIndexRaw: Flow<String?> = dataStore.data.map { it[Keys.RECOVERY_INDEX] }

    fun quoteIndexFlow(): Flow<Int> = dataStore.data.map { it[Keys.QUOTE_INDEX] ?: 0 }
    fun quoteDateFlow(): Flow<Long> = dataStore.data.map { it[Keys.QUOTE_DATE] ?: 0L }
    fun unlockedAchievementsFlow(): Flow<Set<String>> = dataStore.data.map {
        (it[Keys.UNLOCKED_ACHIEVEMENTS] ?: "").split(",").filter { s -> s.isNotBlank() }.toSet()
    }

    fun totalCoinsFlow(): Flow<Int> = dataStore.data.map { it[Keys.TOTAL_COINS] ?: 0 }
    fun longestStreakFlow(): Flow<Int> = dataStore.data.map { it[Keys.LONGEST_STREAK] ?: 0 }

    suspend fun setFirstLaunchComplete() {
        dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = false }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.NOTIFICATION_HOUR] = hour
            it[Keys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setQuoteIndex(index: Int, date: Long) {
        dataStore.edit {
            it[Keys.QUOTE_INDEX] = index
            it[Keys.QUOTE_DATE] = date
        }
    }

    suspend fun unlockAchievement(id: String) {
        dataStore.edit { prefs ->
            val current = (prefs[Keys.UNLOCKED_ACHIEVEMENTS] ?: "").split(",")
                .filter { it.isNotBlank() }.toMutableSet()
            current.add(id)
            prefs[Keys.UNLOCKED_ACHIEVEMENTS] = current.joinToString(",")
        }
    }

    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        dataStore.edit { prefs ->
            prefs[Keys.TOTAL_COINS] = (prefs[Keys.TOTAL_COINS] ?: 0) + amount
        }
    }

    suspend fun setLongestStreak(days: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.LONGEST_STREAK] ?: 0
            if (days > current) prefs[Keys.LONGEST_STREAK] = days
        }
    }

    suspend fun setFamilyPhotoUri(uri: String?) {
        dataStore.edit {
            if (uri == null) it.remove(Keys.FAMILY_PHOTO_URI)
            else it[Keys.FAMILY_PHOTO_URI] = uri
        }
    }

    suspend fun setRecoveryIndex(encoded: String) {
        dataStore.edit { it[Keys.RECOVERY_INDEX] = encoded }
    }

    suspend fun setPersonalLetter(letter: String?) {
        dataStore.edit {
            if (letter.isNullOrBlank()) it.remove(Keys.PERSONAL_LETTER)
            else it[Keys.PERSONAL_LETTER] = letter
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
