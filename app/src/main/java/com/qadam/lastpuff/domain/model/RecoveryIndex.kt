package com.qadam.lastpuff.domain.model

data class RecoveryIndex(
    val heart: Float = 0f,
    val lungs: Float = 0f,
    val blood: Float = 0f,
    val brain: Float = 0f,
    val smellTaste: Float = 0f,
    val willpower: Float = 0f
) {
    fun clamp(): RecoveryIndex = RecoveryIndex(
        heart = heart.coerceIn(0f, 100f),
        lungs = lungs.coerceIn(0f, 100f),
        blood = blood.coerceIn(0f, 100f),
        brain = brain.coerceIn(0f, 100f),
        smellTaste = smellTaste.coerceIn(0f, 100f),
        willpower = willpower.coerceIn(0f, 100f)
    )

    operator fun plus(other: RecoveryIndex): RecoveryIndex = RecoveryIndex(
        heart = heart + other.heart,
        lungs = lungs + other.lungs,
        blood = blood + other.blood,
        brain = brain + other.brain,
        smellTaste = smellTaste + other.smellTaste,
        willpower = willpower + other.willpower
    ).clamp()

    fun isEmpty(): Boolean =
        heart + lungs + blood + brain + smellTaste + willpower < 0.1f
}

data class OrganInfo(
    val id: String,
    val label: String,
    val description: String,
    val emoji: String,
    val value: Float,
    val glowColor: Long
)

object RecoveryBoosts {
    val SOS_WIN = RecoveryIndex(
        heart = 0.5f,
        lungs = 0.3f,
        blood = 0.4f,
        brain = 1.0f,
        smellTaste = 0f,
        willpower = 1.0f
    )
}
