import Foundation

enum SosMessageComposer {
    private static let milestones: [(Int, MessageCategory)] = [
        (0, .sosStart), (15, .sos15Sec), (30, .sos30Sec), (45, .sos45Sec),
        (60, .sos60Sec), (75, .sos75Sec), (90, .sos90Sec), (105, .sos105Sec),
        (120, .sos120Sec), (135, .sos140Sec), (150, .sosLast20),
        (165, .sosLast20), (180, .sosLast20)
    ]

    static func compose(
        elapsedSeconds: Int,
        ctx: SupportContext,
        session: MessageSession,
        mode: SosMode,
        flags: SosComposeFlags
    ) -> SosMoment {
        let category = milestones.last { elapsedSeconds >= $0.0 }?.1 ?? .sosStart
        let primary = session.pick(category)
        var secondary: String?
        let action = session.pickAction(mode: mode)
        var showLetter = false

        switch elapsedSeconds {
        case 30:
            if !flags.factShown { secondary = session.pick(.fact) }
        case 45:
            secondary = session.pick(.futureHold)
        case 60:
            if ctx.moneySaved >= 1 {
                secondary = "Пока ты держишься, ты уже сэкономил \(Int(ctx.moneySaved)) \(ctx.currency)."
            } else if ctx.pricePerCigarette > 0 {
                secondary = session.pick(.money)
            } else {
                secondary = session.pick(.money)
            }
        case 75:
            let reason = ctx.reasons.first
            let memory = reason.flatMap { SupportMessageBank.memoryForReason($0, goalTitle: ctx.goalTitle) }
            if let letter = ctx.personalLetter, !letter.isEmpty, !flags.letterShown {
                showLetter = true
            } else if let memory {
                secondary = memory
            } else {
                secondary = session.pick(.family)
            }
        case 90:
            secondary = ctx.totalWins >= 3
                ? "Ты уже победил \(ctx.totalWins) приступов.\nПочему этот должен быть другим?"
                : session.pick(.psychology)
        case 105:
            secondary = session.pick(.psychology)
        case 120:
            secondary = session.pickHumorRarely() ?? session.pick(.dialog)
        case 135:
            secondary = session.pick(.achievementMoment)
        default:
            break
        }

        return SosMoment(primary: primary, secondary: secondary, action: action, showPersonalLetter: showLetter)
    }

    static func victoryMessage(session: MessageSession) -> String {
        session.pick(.victory)
    }

    static func relapseMessage(session: MessageSession) -> String {
        session.pick(.relapse)
    }
}
