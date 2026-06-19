import Foundation

struct UserProfile: Codable, Equatable {
    var id: Int64 = 1
    var smokeType: String
    var cigarettesPerDay: Int
    var packPrice: Double
    var cigarettesInPack: Int
    var lastSmokeDate: Date
    var reasons: [String]
    var currency: String = "₸"
}

struct CravingEvent: Codable, Identifiable, Equatable {
    var id: Int64 = 0
    var createdAt: Date
    var intensity: Int
    var trigger: String
    var location: String = ""
    var success: Bool
}

struct CompletedMoneyGoal: Codable, Identifiable, Equatable {
    var id: Int64
    var title: String
    var amount: Double
    var savedAmount: Double
    var completedAt: Date
}

struct RelapseEvent: Codable, Identifiable, Equatable {
    var id: Int64 = 0
    var createdAt: Date
    var note: String?
}

struct MoneyGoal: Codable, Equatable {
    var id: Int64 = 0
    var title: String
    var amount: Double
}

struct SosContact: Codable, Equatable {
    var id: Int64 = 0
    var name: String
    var phone: String
    var message: String
}

struct HealthMilestone: Identifiable, Equatable {
    let id: String
    let title: String
    let description: String
    let durationMillis: Int64
    let isUnlocked: Bool
}

struct Achievement: Identifiable, Equatable {
    let id: String
    let title: String
    let description: String
    let isUnlocked: Bool
    var unlockedAt: Date?
}

struct ProgressStats {
    let daysWithoutSmoking: Int
    let hoursWithoutSmoking: Int64
    let cigarettesNotSmoked: Int
    let moneySaved: Double
    let totalCravings: Int
    let cravingsWon: Int
    let relapses: Int
    let winRate: Float
    let longestStreak: Int
    let dangerousHours: [(Int, Int)]
    let topTriggers: [(String, Int)]
    let averageIntensity: Float
}

struct HomeStats: Equatable {
    let days: Int
    let hours: Int64
    let minutes: Int64
    let cigarettesNotSmoked: Int
    let moneySaved: Double
    let motivationalQuote: String
    let nextAchievement: Achievement?
    let lastVictoryAgo: String?
    let dailyLifeCard: String
    let personalReason: String?
    let totalCoins: Int
    let isJustStarted: Bool
}

struct VictoryRecord: Identifiable, Equatable {
    let id: Int64
    let time: String
    let trigger: String
    let intensity: Int
    let message: String
}

struct RecoveryIndex: Equatable {
    var heart: Float = 0
    var lungs: Float = 0
    var blood: Float = 0
    var brain: Float = 0
    var smellTaste: Float = 0
    var willpower: Float = 0

    func clamped() -> RecoveryIndex {
        RecoveryIndex(
            heart: min(max(heart, 0), 100),
            lungs: min(max(lungs, 0), 100),
            blood: min(max(blood, 0), 100),
            brain: min(max(brain, 0), 100),
            smellTaste: min(max(smellTaste, 0), 100),
            willpower: min(max(willpower, 0), 100)
        )
    }

    static func + (lhs: RecoveryIndex, rhs: RecoveryIndex) -> RecoveryIndex {
        RecoveryIndex(
            heart: lhs.heart + rhs.heart,
            lungs: lhs.lungs + rhs.lungs,
            blood: lhs.blood + rhs.blood,
            brain: lhs.brain + rhs.brain,
            smellTaste: lhs.smellTaste + rhs.smellTaste,
            willpower: lhs.willpower + rhs.willpower
        ).clamped()
    }
}

struct OrganInfo: Identifiable, Equatable {
    let id: String
    let label: String
    let description: String
    let emoji: String
    let value: Float
    let glowColorHex: UInt32
}

enum RecoveryBoosts {
    static let sosWin = RecoveryIndex(
        heart: 0.5, lungs: 0.3, blood: 0.4,
        brain: 1.0, smellTaste: 0, willpower: 1.0
    )
}

enum SosStep {
    case emergency, intake, timer, breathing, result, success, relapse
}

struct SosUiState: Equatable {
    var step: SosStep = .intake
    var isEmergency: Bool = false
    var intensity: Int = 5
    var trigger: String = ""
    var location: String = ""
    var showExitDialog: Bool = false
    var timerSecondsLeft: Int = AppConstants.sosTimerSeconds
    var elapsedSeconds: Int = 0
    var currentMessage: String = ""
    var secondaryMessage: String?
    var currentAction: String?
    var sosMode: SosMode = .challenge
    var breathingPhase: Int = 0
    var factShown: Bool = false
    var letterShown: Bool = false
    var showOptionalDetails: Bool = false
    var showReasons: Bool = false
    var showPhoto: Bool = false
    var showPersonalLetterDialog: Bool = false
    var showCoinAnimation: Bool = false
    var success: Bool = false
    var recoveryBefore: RecoveryIndex = RecoveryIndex()
    var recoveryAfter: RecoveryIndex = RecoveryIndex()
}

struct AppData: Codable {
    var profile: UserProfile?
    var sosContact: SosContact?
    var moneyGoal: MoneyGoal?
    var completedGoals: [CompletedMoneyGoal] = []
    var cravings: [CravingEvent] = []
    var relapses: [RelapseEvent] = []
    var nextCravingId: Int64 = 1
    var nextRelapseId: Int64 = 1
    var nextCompletedGoalId: Int64 = 1
}
