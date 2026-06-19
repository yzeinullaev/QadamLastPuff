import UserNotifications

final class NotificationManager: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationManager()

    private weak var repository: UserRepository?

    private override init() {
        super.init()
        UNUserNotificationCenter.current().delegate = self
        registerCategories()
    }

    func configure(repository: UserRepository) {
        self.repository = repository
    }

    func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    private func registerCategories() {
        let holding = UNNotificationAction(
            identifier: Self.actionHolding,
            title: "Держусь 💪",
            options: []
        )
        let relapse = UNNotificationAction(
            identifier: Self.actionRelapse,
            title: "Был срыв",
            options: [.destructive]
        )
        let category = UNNotificationCategory(
            identifier: Self.categoryCheckIn,
            actions: [holding, relapse],
            intentIdentifiers: [],
            options: []
        )
        UNUserNotificationCenter.current().setNotificationCategories([category])
    }

    func reschedule(enabled: Bool, hour: Int, minute: Int) {
        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: [Self.dailyId])
        guard enabled else { return }

        var date = DateComponents()
        date.hour = hour
        date.minute = minute

        let day = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1
        let session = MessageSession(seed: day)
        let body = SupportMessageBank.checkInMessage(hour: hour, session: session)

        let content = UNMutableNotificationContent()
        content.title = "Qadam Last Puff"
        content.body = body
        content.sound = .default
        content.categoryIdentifier = Self.categoryCheckIn

        let trigger = UNCalendarNotificationTrigger(dateMatching: date, repeats: true)
        let request = UNNotificationRequest(identifier: Self.dailyId, content: content, trigger: trigger)
        center.add(request)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        Task { @MainActor in
            switch response.actionIdentifier {
            case Self.actionRelapse:
                repository?.recordRelapse()
            case Self.actionHolding:
                showHoldingFollowUp()
            default:
                break
            }
            completionHandler()
        }
    }

    private func showHoldingFollowUp() {
        let day = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1
        let session = MessageSession(seed: day)
        let content = UNMutableNotificationContent()
        content.title = "Qadam Last Puff"
        content.body = SupportMessageBank.holdingEncouragement(session: session)
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(
            identifier: Self.encouragementId,
            content: content,
            trigger: trigger
        )
        UNUserNotificationCenter.current().add(request)
    }

    private static let dailyId = "daily_checkin"
    private static let encouragementId = "holding_encouragement"
    private static let categoryCheckIn = "daily_checkin"
    private static let actionHolding = "ACTION_HOLDING"
    private static let actionRelapse = "ACTION_RELAPSE"
}
