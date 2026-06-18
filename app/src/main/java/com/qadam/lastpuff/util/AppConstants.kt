package com.qadam.lastpuff.util

object AppConstants {
    const val SOS_TIMER_SECONDS = 180
    const val SOS_MESSAGE_INTERVAL_SECONDS = 15
    const val LONG_PRESS_MS = 2000L

    val SMOKE_TYPES = listOf(
        "Сигареты", "Вейп", "IQOS", "Кальян", "Другое"
    )

    val QUIT_REASONS = listOf(
        "Здоровье", "Семья", "Дети", "Деньги", "Спорт", "Запах", "Надоело", "Другое"
    )

    val CRAVING_TRIGGERS = listOf(
        "Стресс", "Кофе", "После еды", "Скука", "Алкоголь",
        "Друзья", "Работа", "За рулём", "Привычка", "Другое"
    )

    val HEALTH_MILESTONES = listOf(
        Triple("20 минут", "Пульс и давление нормализуются", 20L * 60 * 1000),
        Triple("8 часов", "Уровень кислорода в крови восстанавливается", 8L * 60 * 60 * 1000),
        Triple("24 часа", "Риск сердечного приступа начинает снижаться", 24L * 60 * 60 * 1000),
        Triple("48 часов", "Нервные окончания начинают восстанавливаться", 48L * 60 * 60 * 1000),
        Triple("72 часа", "Дыхание становится легче", 72L * 60 * 60 * 1000),
        Triple("1 неделя", "Обоняние и вкус улучшаются", 7L * 24 * 60 * 60 * 1000),
        Triple("2 недели", "Кровообращение улучшается", 14L * 24 * 60 * 60 * 1000),
        Triple("1 месяц", "Функция лёгких начинает восстанавливаться", 30L * 24 * 60 * 60 * 1000),
        Triple("3 месяца", "Кашель и одышка уменьшаются", 90L * 24 * 60 * 60 * 1000),
        Triple("6 месяцев", "Риск инфекций дыхательных путей снижается", 180L * 24 * 60 * 60 * 1000),
        Triple("1 год", "Риск сердечно-сосудистых заболеваний снижается вдвое", 365L * 24 * 60 * 60 * 1000)
    )

    val ACHIEVEMENTS = listOf(
        Triple("first_hour", "Первый час", "Ты продержался первый час без курения"),
        Triple("first_day", "Первый день", "Целый день без сигарет"),
        Triple("three_days", "3 дня", "Три дня свободы"),
        Triple("seven_days", "7 дней", "Неделя без курения"),
        Triple("thirty_days", "30 дней", "Месяц без сигарет"),
        Triple("hundred_days", "100 дней", "Сто дней свободы"),
        Triple("cigarettes_100", "100 сигарет", "100 сигарет не выкурено"),
        Triple("money_10000", "10 000 ₸", "10 000 ₸ сэкономлено"),
        Triple("wins_10", "10 побед", "10 побед над тягой"),
        Triple("wins_50", "50 побед", "50 побед над тягой"),
        Triple("relapse_survived", "Срыв пережит", "Первый срыв пережит без отказа от цели")
    )

    val LIVE_HEALTH_MESSAGES = listOf(
        Triple("Сейчас", "В крови уже меньше никотина. Тело начинает восстанавливаться.", 0L),
        Triple("20 минут", "Пульс и давление нормализуются.", 20L * 60 * 1000),
        Triple("1 час", "Уровень угарного газа в крови снижается.", 60 * 60 * 1000L),
        Triple("8 часов", "Кислород в крови восстанавливается.", 8L * 60 * 60 * 1000),
        Triple("24 часа", "Риск сердечного приступа начинает снижаться.", 24L * 60 * 60 * 1000)
    )

    fun personalReasonMessage(reasons: List<String>, goalTitle: String?): String? {
        val reason = reasons.firstOrNull() ?: return goalTitle?.let { "Ты хочешь купить $it." }
        return when (reason) {
            "Дети" -> "Ты обещал быть рядом с детьми — здоровым и свободным."
            "Семья" -> "Твоя семья верит в тебя. Не сдавайся сейчас."
            "Деньги" -> goalTitle?.let { "Ты копишь на $it. Каждая сигарета — шаг назад." }
                ?: "Ты экономишь деньги на что-то важное."
            "Здоровье" -> "Твоё тело уже начало восстанавливаться. Не останавливай это."
            "Спорт" -> "Ты хочешь дышать свободно. Эта тяга пройдёт."
            else -> "Ты выбрал путь вперёд. Этот момент — часть победы."
        }
    }

    fun dailyLifeCard(days: Int, hours: Long): String {
        val templates = listOf(
            "Сегодня ты спас %d часов жизни.",
            "За это время можно было бы пройти %d тысяч шагов.",
            "На сэкономленные деньги можно купить бургер 🍔",
            "Твоё сердце уже работает легче.",
            "Каждая победа делает тебя сильнее вчерашнего."
        )
        val index = (days + hours.toInt()) % templates.size
        val template = templates[index]
        return when {
            template.contains("%d") && template.contains("часов") ->
                String.format(template, maxOf(1, hours.toInt()))
            template.contains("%d") && template.contains("шагов") ->
                String.format(template, maxOf(1, days * 2))
            else -> template
        }
    }

    fun liveHealthMessage(elapsedMillis: Long): Pair<String, String> {
        val match = LIVE_HEALTH_MESSAGES.lastOrNull { (_, _, duration) -> elapsedMillis >= duration }
            ?: LIVE_HEALTH_MESSAGES.first()
        return match.first to match.second
    }
}
