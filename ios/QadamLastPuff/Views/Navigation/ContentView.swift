import SwiftUI

enum AppTab: Int, CaseIterable {
    case home, progress, money, body, achievements, profile

    var icon: String {
        switch self {
        case .home: return "house.fill"
        case .progress: return "chart.line.uptrend.xyaxis"
        case .money: return "banknote.fill"
        case .body: return "figure.stand"
        case .achievements: return "trophy.fill"
        case .profile: return "person.fill"
        }
    }
}

struct ContentView: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var selectedTab: AppTab = .home
    @State private var showSos = false
    @State private var splashDone = false
    @Environment(\.colorScheme) private var systemScheme

    private var useDarkTheme: Bool {
        viewModel.darkTheme ?? (systemScheme == .dark)
    }

    private var canShowMainApp: Bool {
        viewModel.onboardingCompleted && viewModel.profile != nil
    }

    var body: some View {
        ZStack {
            Group {
                if splashDone {
                    if canShowMainApp {
                        mainApp
                    } else {
                        OnboardingScreen(viewModel: viewModel)
                    }
                }
            }
            .preferredColorScheme(viewModel.darkTheme == nil ? nil : (useDarkTheme ? .dark : .light))
            .tint(QadamColors.greenPrimary)

            if !splashDone {
                SplashRoute(viewModel: viewModel) { splashDone = true }
            }
        }
        .onAppear {
            viewModel.syncLaunchState()
            NotificationManager.shared.requestPermission()
            if viewModel.notificationsEnabled {
                NotificationManager.shared.reschedule(
                    enabled: true,
                    hour: viewModel.notificationHour,
                    minute: viewModel.notificationMinute
                )
            }
        }
    }

    private var mainApp: some View {
        VStack(spacing: 0) {
            tabContent
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(QadamTheme.background(useDarkTheme))
                .foregroundStyle(Color.primary)

            CustomTabBar(selectedTab: $selectedTab)
        }
        .fullScreenCover(isPresented: $showSos) {
            SosScreen(
                viewModel: viewModel,
                onBack: { showSos = false },
                onComplete: { showSos = false }
            )
        }
    }

    @ViewBuilder
    private var tabContent: some View {
        switch selectedTab {
        case .home:
            HomeScreen(
                viewModel: viewModel,
                onSosClick: { viewModel.startSos(); showSos = true },
                onEmergencySosClick: { viewModel.startEmergencySos(); showSos = true }
            )
        case .progress:
            ProgressScreen(viewModel: viewModel)
        case .money:
            MoneyScreen(viewModel: viewModel)
        case .body:
            BodyScreen(
                organs: RecoveryCalculator.toOrgans(index: viewModel.recoveryIndex),
                willpower: viewModel.recoveryIndex.willpower
            )
        case .achievements:
            AchievementsScreen(viewModel: viewModel)
        case .profile:
            ProfileScreen(viewModel: viewModel)
        }
    }
}

private struct CustomTabBar: View {
    @Binding var selectedTab: AppTab
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        HStack(spacing: 0) {
            ForEach(AppTab.allCases, id: \.rawValue) { tab in
                Button {
                    selectedTab = tab
                } label: {
                    Image(systemName: tab.icon)
                        .font(.system(size: 20))
                        .foregroundStyle(selectedTab == tab ? QadamColors.greenPrimary : .secondary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 4)
        .padding(.bottom, 4)
        .background(QadamTheme.surface(colorScheme == .dark))
        .overlay(alignment: .top) {
            Divider()
        }
    }
}
