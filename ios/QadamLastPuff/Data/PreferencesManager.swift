import Foundation

final class PreferencesManager {
    private let defaults = UserDefaults.standard

    private enum Keys {
        static let isFirstLaunch = "is_first_launch"
        static let onboardingCompleted = "onboarding_completed"
        static let darkTheme = "dark_theme"
        static let notificationsEnabled = "notifications_enabled"
        static let notificationHour = "notification_hour"
        static let notificationMinute = "notification_minute"
        static let unlockedAchievements = "unlocked_achievements"
        static let totalCoins = "total_coins"
        static let longestStreak = "longest_streak"
        static let familyPhotoUri = "family_photo_uri"
        static let personalLetter = "personal_letter"
        static let recoveryIndex = "recovery_index"
        static let lastPersonalLetterDay = "last_personal_letter_day"
    }

    var isFirstLaunch: Bool {
        get {
            if defaults.object(forKey: Keys.isFirstLaunch) == nil { return true }
            return defaults.bool(forKey: Keys.isFirstLaunch)
        }
        set { defaults.set(newValue, forKey: Keys.isFirstLaunch) }
    }

    var onboardingCompleted: Bool {
        get { defaults.bool(forKey: Keys.onboardingCompleted) }
        set { defaults.set(newValue, forKey: Keys.onboardingCompleted) }
    }

    var darkTheme: Bool? {
        get {
            guard defaults.object(forKey: Keys.darkTheme) != nil else { return nil }
            return defaults.bool(forKey: Keys.darkTheme)
        }
        set {
            if let newValue { defaults.set(newValue, forKey: Keys.darkTheme) }
            else { defaults.removeObject(forKey: Keys.darkTheme) }
        }
    }

    var notificationsEnabled: Bool {
        get { defaults.object(forKey: Keys.notificationsEnabled) as? Bool ?? true }
        set { defaults.set(newValue, forKey: Keys.notificationsEnabled) }
    }

    var notificationHour: Int {
        get { defaults.object(forKey: Keys.notificationHour) as? Int ?? 9 }
        set { defaults.set(newValue, forKey: Keys.notificationHour) }
    }

    var notificationMinute: Int {
        get { defaults.object(forKey: Keys.notificationMinute) as? Int ?? 0 }
        set { defaults.set(newValue, forKey: Keys.notificationMinute) }
    }

    var unlockedAchievements: Set<String> {
        get {
            let raw = defaults.string(forKey: Keys.unlockedAchievements) ?? ""
            return Set(raw.split(separator: ",").map(String.init).filter { !$0.isEmpty })
        }
        set {
            defaults.set(newValue.sorted().joined(separator: ","), forKey: Keys.unlockedAchievements)
        }
    }

    var totalCoins: Int {
        get { defaults.integer(forKey: Keys.totalCoins) }
        set { defaults.set(newValue, forKey: Keys.totalCoins) }
    }

    var longestStreak: Int {
        get { defaults.integer(forKey: Keys.longestStreak) }
        set { defaults.set(newValue, forKey: Keys.longestStreak) }
    }

    var familyPhotoUri: String? {
        get { defaults.string(forKey: Keys.familyPhotoUri) }
        set { defaults.set(newValue, forKey: Keys.familyPhotoUri) }
    }

    var personalLetter: String? {
        get { defaults.string(forKey: Keys.personalLetter) }
        set { defaults.set(newValue, forKey: Keys.personalLetter) }
    }

    var recoveryIndexRaw: String? {
        get { defaults.string(forKey: Keys.recoveryIndex) }
        set { defaults.set(newValue, forKey: Keys.recoveryIndex) }
    }

    var lastPersonalLetterDay: Int {
        get { defaults.integer(forKey: Keys.lastPersonalLetterDay) }
        set { defaults.set(newValue, forKey: Keys.lastPersonalLetterDay) }
    }

    func canShowPersonalLetterToday() -> Bool {
        let today = Int(Date().timeIntervalSince1970 / 86400)
        return lastPersonalLetterDay != today
    }

    func markPersonalLetterShownToday() {
        lastPersonalLetterDay = Int(Date().timeIntervalSince1970 / 86400)
    }

    func addCoin() { totalCoins += 1 }

    func unlockAchievement(_ id: String) {
        var set = unlockedAchievements
        set.insert(id)
        unlockedAchievements = set
    }

    func clearAll() {
        let keys = [
            Keys.unlockedAchievements, Keys.totalCoins, Keys.longestStreak,
            Keys.familyPhotoUri, Keys.personalLetter, Keys.recoveryIndex
        ]
        keys.forEach { defaults.removeObject(forKey: $0) }
    }
}
