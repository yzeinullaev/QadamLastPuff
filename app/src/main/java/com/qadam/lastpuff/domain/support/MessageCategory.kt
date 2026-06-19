package com.qadam.lastpuff.domain.support

enum class MessageCategory {
    MORNING,
    DAY,
    EVENING,
    SOS_START,
    SOS_15_SEC,
    SOS_30_SEC,
    SOS_45_SEC,
    SOS_60_SEC,
    SOS_75_SEC,
    SOS_90_SEC,
    SOS_105_SEC,
    SOS_120_SEC,
    SOS_140_SEC,
    SOS_LAST_20,
    VICTORY,
    RELAPSE,
    MONEY,
    HEALTH,
    FAMILY,
    CHALLENGE,
    BREATH,
    FACT,
    HUMOR,
    PSYCHOLOGY,
    DIALOG,
    CHECK_IN,
    FUTURE_HOLD,
    WIN_STREAK,
    ACHIEVEMENT_MOMENT
}

enum class SosMode {
    BREATHING,
    FAMILY,
    CHALLENGE;

    companion object {
        fun forDay(dayOfYear: Int): SosMode = entries[dayOfYear % entries.size]
    }
}

data class SupportContext(
    val reasons: List<String> = emptyList(),
    val goalTitle: String? = null,
    val currency: String = "₸",
    val pricePerCigarette: Double = 0.0,
    val moneySaved: Double = 0.0,
    val totalWins: Int = 0,
    val personalLetter: String? = null,
    val hourOfDay: Int = 12
)

data class SosMoment(
    val primary: String,
    val secondary: String? = null,
    val action: String? = null,
    val showPersonalLetter: Boolean = false
)
