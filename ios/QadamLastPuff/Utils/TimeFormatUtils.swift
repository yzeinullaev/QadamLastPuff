import Foundation

enum TimeFormatUtils {
    private static let timeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f
    }()

    private static let dateTimeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "dd.MM.yyyy HH:mm"
        return f
    }()

    static func formatTime(_ date: Date) -> String {
        timeFormatter.string(from: date)
    }

    static func formatDateTime(_ date: Date) -> String {
        dateTimeFormatter.string(from: date)
    }

    static func formatAgo(_ date: Date, now: Date = Date()) -> String {
        let diff = now.timeIntervalSince(date)
        guard diff >= 0 else { return "только что" }
        let minutes = Int(diff / 60)
        let hours = Int(diff / 3600)
        let days = Int(diff / 86400)
        if minutes < 1 { return "только что" }
        if minutes < 60 { return "\(minutes) мин. назад" }
        if hours < 24 { return "\(hours) ч. назад" }
        return "\(days) дн. назад"
    }

    static func intensityStars(_ intensity: Int) -> String {
        let clamped = min(max(intensity, 1), 5)
        return String(repeating: "★", count: clamped) + String(repeating: "☆", count: 5 - clamped)
    }
}
