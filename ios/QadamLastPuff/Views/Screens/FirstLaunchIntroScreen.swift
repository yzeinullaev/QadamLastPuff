import SwiftUI

struct FirstLaunchIntroScreen: View {
    let onFinished: () -> Void

    @State private var textPhase = 0

    var body: some View {
        ZStack {
            QadamColors.splashBackground.ignoresSafeArea()

            VStack(spacing: 40) {
                SmokeQLogo(size: 220)

                Group {
                    switch textPhase {
                    case 1:
                        introText("Сегодня ты сделал", "самый важный шаг.")
                    case 2:
                        introText("Добро пожаловать", "в жизнь без сигарет.")
                    default:
                        Color.clear.frame(height: 72)
                    }
                }
                .animation(.easeInOut(duration: 0.5), value: textPhase)
            }
            .padding(.horizontal, 32)
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 3.4) { textPhase = 1 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 4.4) { textPhase = 2 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 5.6) { onFinished() }
        }
    }

    private func introText(_ line1: String, _ line2: String) -> some View {
        VStack(spacing: 4) {
            Text(line1)
            Text(line2)
        }
        .font(.system(size: 22, weight: .medium))
        .foregroundStyle(Color(hex: 0xE8F5EC))
        .multilineTextAlignment(.center)
        .transition(.opacity)
    }
}
