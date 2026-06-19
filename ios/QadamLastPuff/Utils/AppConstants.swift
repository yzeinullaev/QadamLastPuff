import Foundation

enum AppConstants {
    static let sosTimerSeconds = 180
    static let sosMessageIntervalSeconds = 15
    static let longPressMs: UInt64 = 2_000_000_000
    static let strongCravingIntensity = 8
    static let coinRewardNormal = 1
    static let coinRewardStrong = 2

    static func coinReward(for intensity: Int) -> Int {
        intensity >= strongCravingIntensity ? coinRewardStrong : coinRewardNormal
    }

    static let smokeTypes = ["Сигареты", "Вейп", "IQOS", "Кальян", "Другое"]
    static let quitReasons = ["Здоровье", "Семья", "Дети", "Деньги", "Спорт", "Запах", "Надоело", "Другое"]
    static let cravingTriggers = [
        "Стресс", "Кофе", "После еды", "Скука", "Алкоголь",
        "Друзья", "Работа", "За рулём", "Привычка", "Другое"
    ]

    static let cravingLocations = [
        "Дом", "Работа", "Улица", "Кафе", "В машине", "У друзей", "Другое"
    ]

    static let achievementEmoji: [String: String] = [
        "first_hour": "⏱️",
        "first_day": "🌅",
        "three_days": "🔥",
        "seven_days": "📅",
        "thirty_days": "🏆",
        "hundred_days": "💎",
        "cigarettes_100": "🚭",
        "money_10000": "💰",
        "wins_10": "⭐",
        "wins_50": "🌟",
        "coins_20": "🪙",
        "coins_40": "💰",
        "coins_80": "🏦",
        "coins_110": "👑",
        "relapse_survived": "💚"
    ]

    static func achievementImage(for id: String) -> String {
        achievementEmoji[id] ?? "🏅"
    }

    static let healthMilestones: [(String, String, Int64)] = [
        ("20 минут", "Пульс и давление нормализуются", 20 * 60 * 1000),
        ("8 часов", "Уровень кислорода в крови восстанавливается", 8 * 60 * 60 * 1000),
        ("24 часа", "Риск сердечного приступа начинает снижаться", 24 * 60 * 60 * 1000),
        ("48 часов", "Нервные окончания начинают восстанавливаться", 48 * 60 * 60 * 1000),
        ("72 часа", "Дыхание становится легче", 72 * 60 * 60 * 1000),
        ("1 неделя", "Обоняние и вкус улучшаются", 7 * 24 * 60 * 60 * 1000),
        ("2 недели", "Кровообращение улучшается", 14 * 24 * 60 * 60 * 1000),
        ("1 месяц", "Функция лёгких начинает восстанавливаться", 30 * 24 * 60 * 60 * 1000),
        ("3 месяца", "Кашель и одышка уменьшаются", 90 * 24 * 60 * 60 * 1000),
        ("6 месяцев", "Риск инфекций дыхательных путей снижается", 180 * 24 * 60 * 60 * 1000),
        ("1 год", "Риск сердечно-сосудистых заболеваний снижается вдвое", 365 * 24 * 60 * 60 * 1000)
    ]

    static let achievements: [(String, String, String)] = [
        ("first_hour", "Первый час", "Ты продержался первый час без курения"),
        ("first_day", "Первый день", "Целый день без сигарет"),
        ("three_days", "3 дня", "Три дня свободы"),
        ("seven_days", "7 дней", "Неделя без курения"),
        ("thirty_days", "30 дней", "Месяц без сигарет"),
        ("hundred_days", "100 дней", "Сто дней свободы"),
        ("cigarettes_100", "100 сигарет", "100 сигарет не выкурено"),
        ("money_10000", "10 000 ₸", "10 000 ₸ сэкономлено"),
        ("wins_10", "10 побед", "10 побед над тягой"),
        ("wins_50", "50 побед", "50 побед над тягой"),
        ("coins_20", "20 монет", "20 монет в копилке побед"),
        ("coins_40", "40 монет", "40 монет в копилке побед"),
        ("coins_80", "80 монет", "80 монет в копилке побед"),
        ("coins_110", "110 монет", "110 монет в копилке побед"),
        ("relapse_survived", "Срыв пережит", "Первый срыв пережит без отказа от цели")
    ]

    private static let liveHealthMessages: [(String, String, Int64)] = [
        ("Сейчас", "В крови уже меньше никотина. Тело начинает восстанавливаться.", 0),
        ("20 минут", "Пульс и давление нормализуются.", 20 * 60 * 1000),
        ("1 час", "Уровень угарного газа в крови снижается.", 60 * 60 * 1000),
        ("8 часов", "Кислород в крови восстанавливается.", 8 * 60 * 60 * 1000),
        ("24 часа", "Риск сердечного приступа начинает снижаться.", 24 * 60 * 60 * 1000)
    ]

    static func personalReasonMessage(reasons: [String], goalTitle: String?) -> String? {
        guard let reason = reasons.first else {
            return goalTitle.map { "Ты хочешь купить \($0)." }
        }
        switch reason {
        case "Дети": return "Ты обещал быть рядом с детьми — здоровым и свободным."
        case "Семья": return "Твоя семья верит в тебя. Не сдавайся сейчас."
        case "Деньги":
            return goalTitle.map { "Ты копишь на \($0). Каждая сигарета — шаг назад." }
                ?? "Ты экономишь деньги на что-то важное."
        case "Здоровье": return "Твоё тело уже начало восстанавливаться. Не останавливай это."
        case "Спорт": return "Ты хочешь дышать свободно. Эта тяга пройдёт."
        default: return "Ты выбрал путь вперёд. Этот момент — часть победы."
        }
    }

    static func dailyLifeCard(days: Int, hours: Int64) -> String {
        let templates = [
            "Сегодня ты спас %d часов жизни.",
            "За это время можно было бы пройти %d тысяч шагов.",
            "На сэкономленные деньги можно купить бургер 🍔",
            "Твоё сердце уже работает легче.",
            "Каждая победа делает тебя сильнее вчерашнего."
        ]
        let index = (days + Int(hours)) % templates.count
        let template = templates[index]
        if template.contains("часов") {
            return String(format: template, max(1, Int(hours)))
        }
        if template.contains("шагов") {
            return String(format: template, max(1, days * 2))
        }
        return template
    }

    static func liveHealthMessage(elapsedMillis: Int64) -> (String, String) {
        let match = liveHealthMessages.last { elapsedMillis >= $0.2 } ?? liveHealthMessages[0]
        return (match.0, match.1)
    }
}
