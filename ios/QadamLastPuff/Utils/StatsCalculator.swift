import Foundation

enum StatsCalculator {
    static func elapsedMillis(lastSmokeDate: Date, now: Date = Date()) -> Int64 {
        max(0, Int64(now.timeIntervalSince(lastSmokeDate) * 1000))
    }

    static func daysWithoutSmoking(lastSmokeDate: Date, now: Date = Date()) -> Int {
        Int(elapsedMillis(lastSmokeDate: lastSmokeDate, now: now) / (24 * 60 * 60 * 1000))
    }

    static func hoursWithoutSmoking(lastSmokeDate: Date, now: Date = Date()) -> Int64 {
        elapsedMillis(lastSmokeDate: lastSmokeDate, now: now) / (60 * 60 * 1000)
    }

    static func minutesWithoutSmoking(lastSmokeDate: Date, now: Date = Date()) -> Int64 {
        (elapsedMillis(lastSmokeDate: lastSmokeDate, now: now) / (60 * 1000)) % 60
    }

    static func cigarettesNotSmoked(profile: UserProfile, now: Date = Date()) -> Int {
        Int(floor(cigarettesNotSmokedExact(profile: profile, now: now)))
    }

    /// Дробное число «не выкуренных» сигарет — для расчёта денег по часам.
    static func cigarettesNotSmokedExact(profile: UserProfile, now: Date = Date()) -> Double {
        let elapsedHours = Double(elapsedMillis(lastSmokeDate: profile.lastSmokeDate, now: now)) / (60 * 60 * 1000)
        let perHour = Double(profile.cigarettesPerDay) / 24.0
        return elapsedHours * perHour
    }

    static func moneySaved(profile: UserProfile, now: Date = Date()) -> Double {
        guard profile.cigarettesInPack > 0 else { return 0 }
        let pricePerCigarette = profile.packPrice / Double(profile.cigarettesInPack)
        return cigarettesNotSmokedExact(profile: profile, now: now) * pricePerCigarette
    }

    static func formatMoney(_ amount: Double) -> String {
        amount < 0.5 ? "—" : String(format: "%.0f", amount.rounded())
    }

    static func winRate(wins: Int, total: Int) -> Float {
        total == 0 ? 0 : Float(wins) / Float(total) * 100
    }

    static func dangerousHours(events: [Date]) -> [(Int, Int)] {
        var hourCounts = Array(repeating: 0, count: 24)
        let calendar = Calendar.current
        for date in events {
            let hour = calendar.component(.hour, from: date)
            hourCounts[hour] += 1
        }
        return hourCounts.enumerated()
            .filter { $0.1 > 0 }
            .sorted { $0.1 > $1.1 }
            .prefix(3)
            .map { ($0.0, $0.1) }
    }

    static func topTriggers(triggers: [String], limit: Int = 5) -> [(String, Int)] {
        var counts: [String: Int] = [:]
        for trigger in triggers { counts[trigger, default: 0] += 1 }
        return counts.sorted { $0.value > $1.value }.prefix(limit).map { ($0.key, $0.value) }
    }

    static func averageIntensity(intensities: [Int]) -> Float {
        intensities.isEmpty ? 0 : Float(intensities.reduce(0, +)) / Float(intensities.count)
    }
}
