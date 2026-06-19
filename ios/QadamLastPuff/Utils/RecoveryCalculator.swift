import Foundation

enum RecoveryCalculator {
    static func fromTimeWithoutSmoking(elapsedMs: Int64, wins: Int) -> RecoveryIndex {
        let hours = Double(elapsedMs) / (60 * 60 * 1000)
        let days = Double(elapsedMs) / (24 * 60 * 60 * 1000)
        return RecoveryIndex(
            heart: Float(hours * 0.75),
            lungs: Float(days * 2.8),
            blood: Float(hours * 1.4),
            brain: Float(days * 1.5),
            smellTaste: Float(days * 0.9),
            willpower: Float(Double(wins) * 1.2 + days * 0.5)
        ).clamped()
    }

    static func merge(stored: RecoveryIndex, timeBased: RecoveryIndex) -> RecoveryIndex {
        RecoveryIndex(
            heart: max(stored.heart, timeBased.heart),
            lungs: max(stored.lungs, timeBased.lungs),
            blood: max(stored.blood, timeBased.blood),
            brain: max(stored.brain, timeBased.brain),
            smellTaste: max(stored.smellTaste, timeBased.smellTaste),
            willpower: max(stored.willpower, timeBased.willpower)
        ).clamped()
    }

    static func toOrgans(index: RecoveryIndex) -> [OrganInfo] {
        [
            OrganInfo(id: "heart", label: "Сердце", description: "Пульс и кровоток становятся лучше", emoji: "❤️", value: index.heart, glowColorHex: 0xFFE57373),
            OrganInfo(id: "lungs", label: "Лёгкие", description: "Лёгкие очищаются и наполняются кислородом", emoji: "🫁", value: index.lungs, glowColorHex: 0xFF64B5F6),
            OrganInfo(id: "blood", label: "Кровь", description: "Уровень кислорода в крови растёт", emoji: "🩸", value: index.blood, glowColorHex: 0xFFEF5350),
            OrganInfo(id: "brain", label: "Мозг", description: "Настроение и концентрация улучшаются", emoji: "🧠", value: index.brain, glowColorHex: 0xFFBA68C8),
            OrganInfo(id: "smell", label: "Обоняние и вкус", description: "Чувствительность возвращается", emoji: "👃", value: index.smellTaste, glowColorHex: 0xFFFFB74D)
        ]
    }

    static func encode(_ index: RecoveryIndex) -> String {
        "\(index.heart),\(index.lungs),\(index.blood),\(index.brain),\(index.smellTaste),\(index.willpower)"
    }

    static func decode(_ raw: String?) -> RecoveryIndex {
        guard let raw, !raw.isEmpty else { return RecoveryIndex() }
        let parts = raw.split(separator: ",").compactMap { Float($0) }
        guard parts.count == 6 else { return RecoveryIndex() }
        return RecoveryIndex(
            heart: parts[0], lungs: parts[1], blood: parts[2],
            brain: parts[3], smellTaste: parts[4], willpower: parts[5]
        )
    }
}
