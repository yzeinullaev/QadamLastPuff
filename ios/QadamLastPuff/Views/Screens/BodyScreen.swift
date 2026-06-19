import SwiftUI

struct BodyScreen: View {
    let organs: [OrganInfo]
    let willpower: Float

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Твоё тело")
                    .font(.title.bold())
                    .foregroundStyle(.white)
                Text("Твоё тело восстанавливается каждый день без сигарет")
                    .foregroundStyle(.white.opacity(0.6))
                Text("Индекс восстановления")
                    .font(.subheadline.bold())
                    .foregroundStyle(QadamColors.recoveryGreen)

                BodySilhouette(organs: organs)
                    .frame(height: 260)
                    .background(Color(hex: 0x0D1520))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                ForEach(organs) { organ in
                    OrganRecoveryCard(organ: organ)
                }

                WillpowerCard(willpower: willpower)

                HStack(spacing: 12) {
                    Text("🌿").font(.title)
                    Text("Ты уже на пути к новой жизни! Продолжай в том же духе.")
                        .foregroundStyle(.white.opacity(0.85))
                }
                .padding(16)
                .background(QadamColors.bodyCard)
                .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .padding(20)
        }
        .background(QadamColors.bodyBackground)
    }
}

private struct OrganRecoveryCard: View {
    let organ: OrganInfo
    @State private var animated: CGFloat = 0

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(organ.emoji)
                Text("\(organ.label) \(Int(organ.value))%")
                    .font(.subheadline.bold())
                    .foregroundStyle(.white)
            }
            ProgressView(value: animated)
                .tint(Color(hex: organ.glowColorHex))
            Text(organ.description)
                .font(.caption)
                .foregroundStyle(.white.opacity(0.55))
        }
        .padding(14)
        .background(QadamColors.bodyCard)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .onAppear {
            withAnimation(.easeOut(duration: 0.8)) {
                animated = CGFloat(organ.value / 100)
            }
        }
    }
}

private struct WillpowerCard: View {
    let willpower: Float
    @State private var animated: CGFloat = 0

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("🛡")
                Text("Сила воли \(Int(willpower))%")
                    .font(.subheadline.bold())
                    .foregroundStyle(QadamColors.willpowerYellow)
            }
            ProgressView(value: animated)
                .tint(QadamColors.willpowerYellow)
        }
        .padding(16)
        .background(QadamColors.bodyCard)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .onAppear {
            withAnimation(.easeOut(duration: 0.8)) {
                animated = CGFloat(willpower / 100)
            }
        }
    }
}
