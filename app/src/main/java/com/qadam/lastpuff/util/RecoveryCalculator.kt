package com.qadam.lastpuff.util

import com.qadam.lastpuff.domain.model.OrganInfo
import com.qadam.lastpuff.domain.model.RecoveryIndex

object RecoveryCalculator {

    /** Базовый индекс от времени без курения (медленный рост) */
    fun fromTimeWithoutSmoking(elapsedMs: Long, wins: Int): RecoveryIndex {
        val hours = elapsedMs / (60 * 60 * 1000.0)
        val days = elapsedMs / (24 * 60 * 60 * 1000.0)
        return RecoveryIndex(
            heart = (hours * 0.75).toFloat(),
            lungs = (days * 2.8).toFloat(),
            blood = (hours * 1.4).toFloat(),
            brain = (days * 1.5).toFloat(),
            smellTaste = (days * 0.9).toFloat(),
            willpower = (wins * 1.2f + days * 0.5).toFloat()
        ).clamp()
    }

    /** Объединяет сохранённый индекс и время (берём максимум по каждому органу) */
    fun merge(stored: RecoveryIndex, timeBased: RecoveryIndex): RecoveryIndex =
        RecoveryIndex(
            heart = maxOf(stored.heart, timeBased.heart),
            lungs = maxOf(stored.lungs, timeBased.lungs),
            blood = maxOf(stored.blood, timeBased.blood),
            brain = maxOf(stored.brain, timeBased.brain),
            smellTaste = maxOf(stored.smellTaste, timeBased.smellTaste),
            willpower = maxOf(stored.willpower, timeBased.willpower)
        ).clamp()

    fun toOrgans(index: RecoveryIndex): List<OrganInfo> = listOf(
        OrganInfo("heart", "Сердце", "Пульс и кровоток становятся лучше", "❤️", index.heart, 0xFFE57373),
        OrganInfo("lungs", "Лёгкие", "Лёгкие очищаются и наполняются кислородом", "🫁", index.lungs, 0xFF64B5F6),
        OrganInfo("blood", "Кровь", "Уровень кислорода в крови растёт", "🩸", index.blood, 0xFFEF5350),
        OrganInfo("brain", "Мозг", "Настроение и концентрация улучшаются", "🧠", index.brain, 0xFFBA68C8),
        OrganInfo("smell", "Обоняние и вкус", "Чувствительность возвращается", "👃", index.smellTaste, 0xFFFFB74D)
    )

    fun encode(index: RecoveryIndex): String =
        "${index.heart},${index.lungs},${index.blood},${index.brain},${index.smellTaste},${index.willpower}"

    fun decode(raw: String?): RecoveryIndex {
        if (raw.isNullOrBlank()) return RecoveryIndex()
        val parts = raw.split(",").mapNotNull { it.toFloatOrNull() }
        if (parts.size != 6) return RecoveryIndex()
        return RecoveryIndex(
            heart = parts[0],
            lungs = parts[1],
            blood = parts[2],
            brain = parts[3],
            smellTaste = parts[4],
            willpower = parts[5]
        )
    }
}
