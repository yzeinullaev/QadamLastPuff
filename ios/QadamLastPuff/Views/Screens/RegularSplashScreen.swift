import SwiftUI

enum SplashQuotes {
    static let phrases = [
        "Ты уже на пути к свободе.",
        "Каждый день имеет значение.",
        "Сегодня организм станет ещё сильнее.",
        "Не сдавайся.",
        "Ты уже начал новую жизнь.",
        "Свобода начинается с одного решения.",
        "Каждая победа делает тебя сильнее.",
        "Твоё тело благодарит тебя."
    ]
}

struct RegularSplashScreen: View {
    @ObservedObject var viewModel: AppViewModel
    let onFinished: () -> Void

    private let quote = SplashQuotes.phrases.randomElement() ?? SplashQuotes.phrases[0]
    @State private var syncDone = false

    private var hasProfile: Bool { viewModel.profile != nil }

    var body: some View {
        ZStack {
            QadamColors.splashBackground.ignoresSafeArea()

            VStack(spacing: 20) {
                GlowingQLogo(size: 88)

                Text(quote)
                    .font(.system(size: 17, weight: .medium))
                    .foregroundStyle(Color(hex: 0xD8F0E2))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 28)

                recoveryBlock

                Text(syncDone ? "✓ Организм восстанавливается" : "Синхронизация прогресса...")
                    .font(.system(size: 14))
                    .foregroundStyle(Color(hex: 0x8FD4AA))
                    .animation(.easeInOut, value: syncDone)
            }
            .padding(.vertical, 48)
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) { syncDone = true }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { onFinished() }
        }
    }

    private var recoveryBlock: some View {
        VStack(spacing: 6) {
            Text("Организм восстанавливается")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Color(hex: 0xB8F5D4))

            recoveryRow("❤️", "Сердце", viewModel.recoveryIndex.heart)
            recoveryRow("🫁", "Лёгкие", viewModel.recoveryIndex.lungs)
            recoveryRow("🩸", "Кровь", viewModel.recoveryIndex.blood)
            recoveryRow("🧠", "Контроль", viewModel.recoveryIndex.willpower)

            if !hasProfile {
                Text("Пройди onboarding, чтобы начать отслеживать прогресс")
                    .font(.system(size: 12))
                    .foregroundStyle(Color(hex: 0x8FD4AA).opacity(0.7))
                    .multilineTextAlignment(.center)
                    .padding(.top, 4)
            }
        }
        .padding(.top, 8)
    }

    private func recoveryRow(_ emoji: String, _ label: String, _ value: Float) -> some View {
        Text("\(emoji) \(label): \(Int(min(max(value, 0), 100)))%")
            .font(.system(size: 14))
            .foregroundStyle(Color(hex: 0x9FD4B8))
    }
}
