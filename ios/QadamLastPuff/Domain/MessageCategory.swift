import Foundation

enum MessageCategory: String, CaseIterable {
    case morning, day, evening
    case sosStart, sos15Sec, sos30Sec, sos45Sec, sos60Sec, sos75Sec
    case sos90Sec, sos105Sec, sos120Sec, sos140Sec, sosLast20
    case victory, relapse, money, health, family, challenge, breath
    case fact, humor, psychology, dialog, checkIn, futureHold, winStreak, achievementMoment
}

enum SosMode: CaseIterable {
    case breathing, family, challenge

    static func forDay(dayOfYear: Int) -> SosMode {
        allCases[dayOfYear % allCases.count]
    }

    var emoji: String {
        switch self {
        case .breathing: return "🌬"
        case .family: return "❤️"
        case .challenge: return "🎯"
        }
    }
}

struct SupportContext {
    var reasons: [String] = []
    var goalTitle: String?
    var currency: String = "₸"
    var pricePerCigarette: Double = 0
    var moneySaved: Double = 0
    var totalWins: Int = 0
    var personalLetter: String?
    var hourOfDay: Int = 12
}

struct SosMoment {
    let primary: String
    var secondary: String?
    var action: String?
    var showPersonalLetter: Bool = false
}

struct SosComposeFlags {
    var factShown: Bool = false
    var letterShown: Bool = false
}
