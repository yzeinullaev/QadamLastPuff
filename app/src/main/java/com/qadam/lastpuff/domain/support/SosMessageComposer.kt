package com.qadam.lastpuff.domain.support

object SosMessageComposer {

    private val milestones = listOf(
        0 to MessageCategory.SOS_START,
        15 to MessageCategory.SOS_15_SEC,
        30 to MessageCategory.SOS_30_SEC,
        45 to MessageCategory.SOS_45_SEC,
        60 to MessageCategory.SOS_60_SEC,
        75 to MessageCategory.SOS_75_SEC,
        90 to MessageCategory.SOS_90_SEC,
        105 to MessageCategory.SOS_105_SEC,
        120 to MessageCategory.SOS_120_SEC,
        135 to MessageCategory.SOS_140_SEC,
        150 to MessageCategory.SOS_LAST_20,
        165 to MessageCategory.SOS_LAST_20,
        180 to MessageCategory.SOS_LAST_20
    )

    fun compose(
        elapsedSeconds: Int,
        ctx: SupportContext,
        session: MessageSession,
        mode: SosMode,
        flags: SosComposeFlags
    ): SosMoment {
        val category = milestones.lastOrNull { (sec, _) -> elapsedSeconds >= sec }?.second
            ?: MessageCategory.SOS_START

        val primary = session.pick(category)
        var secondary: String? = null
        val action = session.pickAction(mode)
        var showLetter = false

        when (elapsedSeconds) {
            30 -> if (!flags.factShown) {
                secondary = session.pick(MessageCategory.FACT)
            }
            45 -> secondary = session.pick(MessageCategory.FUTURE_HOLD)
            60 -> {
                if (ctx.pricePerCigarette > 0) {
                    val saved = ctx.pricePerCigarette.coerceAtLeast(1.0)
                    secondary = "Пока ты держишься, ты уже сохранил ещё ${saved.toInt()} ${ctx.currency}."
                } else {
                    secondary = session.pick(MessageCategory.MONEY)
                }
            }
            75 -> {
                val reason = ctx.reasons.firstOrNull()
                val memory = reason?.let { SupportMessageBank.memoryForReason(it, ctx.goalTitle) }
                if (!ctx.personalLetter.isNullOrBlank() && !flags.letterShown) {
                    showLetter = true
                } else if (memory != null) {
                    secondary = memory
                } else {
                    secondary = session.pick(MessageCategory.FAMILY)
                }
            }
            90 -> {
                secondary = if (ctx.totalWins >= 3) {
                    "Ты уже победил ${ctx.totalWins} приступов.\nПочему этот должен быть другим?"
                } else {
                    session.pick(MessageCategory.PSYCHOLOGY)
                }
            }
            105 -> secondary = session.pick(MessageCategory.PSYCHOLOGY)
            120 -> secondary = session.pickHumorRarely() ?: session.pick(MessageCategory.DIALOG)
            135 -> secondary = session.pick(MessageCategory.ACHIEVEMENT_MOMENT)
        }

        return SosMoment(
            primary = primary,
            secondary = secondary,
            action = action,
            showPersonalLetter = showLetter
        )
    }

    fun victoryMessage(session: MessageSession): String = session.pick(MessageCategory.VICTORY)

    fun relapseMessage(session: MessageSession): String = session.pick(MessageCategory.RELAPSE)
}

data class SosComposeFlags(
    val factShown: Boolean = false,
    val letterShown: Boolean = false
)
