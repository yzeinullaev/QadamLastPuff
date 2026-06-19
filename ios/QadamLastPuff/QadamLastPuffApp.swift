import SwiftUI

@main
struct QadamLastPuffApp: App {
    @StateObject private var viewModel: AppViewModel

    init() {
        let store = DataStore()
        let prefs = PreferencesManager()
        let repository = UserRepository(store: store, preferences: prefs)
        NotificationManager.shared.configure(repository: repository)
        _viewModel = StateObject(wrappedValue: AppViewModel(repository: repository))
    }

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: viewModel)
        }
    }
}
