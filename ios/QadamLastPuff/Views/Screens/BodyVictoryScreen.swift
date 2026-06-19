import SwiftUI

struct BodyVictoryScreen: View {
    let before: RecoveryIndex
    let after: RecoveryIndex
    let showCoinAnimation: Bool
    let totalCoins: Int
    var coinsEarned: Int = 1
    let onContinue: () -> Void

    @State private var waveProgress: CGFloat = 0

    private var boosts: [(String, String, Float)] {
        [
            ("❤️", "Сердце", after.heart - before.heart),
            ("🫁", "Лёгкие", after.lungs - before.lungs),
            ("🩸", "Кровь", after.blood - before.blood),
            ("🧠", "Мозг", after.brain - before.brain),
            ("🛡", "Сила воли", after.willpower - before.willpower)
        ].filter { $0.2 > 0.01 }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                if showCoinAnimation {
                    CoinDropAnimation(
                        visible: true,
                        totalCoins: totalCoins,
                        coinsEarned: coinsEarned
                    )
                }

                Text("✨").font(.largeTitle)
                Text("Отличная работа!")
                    .font(.title.bold())
                    .foregroundStyle(.white)
                Text("Ты выдержал тягу и помог своему телу стать сильнее")
                    .foregroundStyle(.white.opacity(0.75))
                    .multilineTextAlignment(.center)

                BodySilhouette(organs: RecoveryCalculator.toOrgans(index: after), waveProgress: waveProgress, highlightHeartLungs: true)
                    .frame(height: 300)
                    .background(Color(hex: 0x0D1A12))
                    .clipShape(RoundedRectangle(cornerRadius: 20))

                ForEach(boosts, id: \.1) { emoji, label, boost in
                    HStack {
                        Text(emoji)
                        Text(label).foregroundStyle(.white)
                        Spacer()
                        Text("+\(String(format: "%.1f", boost))%")
                            .foregroundStyle(QadamColors.recoveryGreen)
                    }
                    .padding(12)
                    .background(QadamColors.bodyCard)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                QadamCard {
                    Text("\"Каждая пережитая тяга — тренировка силы воли.\"")
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                }

                Button("Продолжить", action: onContinue)
                    .buttonStyle(.borderedProminent)
                    .tint(QadamColors.recoveryGreen)
                    .frame(maxWidth: .infinity)
            }
            .padding(24)
        }
        .background(QadamColors.bodyBackground)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                withAnimation(.easeInOut(duration: 2.2)) { waveProgress = 1 }
            }
        }
    }
}
