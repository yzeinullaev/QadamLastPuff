import SwiftUI

struct HugeSosButton: View {
    let onTap: () -> Void
    let onLongPress: () -> Void

    @State private var isPressed = false
    @State private var longPressTriggered = false
    @State private var pulse = false

    var body: some View {
        Button(action: {}) {
            VStack(spacing: 8) {
                Text("🔥").font(.system(size: 40))
                Text("Я сейчас\nхочу курить")
                    .font(.title.bold())
                    .foregroundStyle(.white)
                    .multilineTextAlignment(.center)
                Text("Нажми сюда")
                    .font(.title3)
                    .foregroundStyle(.white.opacity(0.9))
                Text("Удержи 2 сек — критическая тяга")
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.7))
            }
            .frame(maxWidth: .infinity)
            .frame(height: 260)
            .background(QadamColors.sosRed)
            .clipShape(RoundedRectangle(cornerRadius: 32))
            .scaleEffect(pulse ? 1.02 : 1.0)
        }
        .buttonStyle(.plain)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if !isPressed {
                        isPressed = true
                        longPressTriggered = false
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            if isPressed {
                                longPressTriggered = true
                                isPressed = false
                                onLongPress()
                            }
                        }
                    }
                }
                .onEnded { _ in
                    if !longPressTriggered { onTap() }
                    isPressed = false
                }
        )
        .onAppear {
            withAnimation(.easeInOut(duration: 0.9).repeatForever(autoreverses: true)) {
                pulse = true
            }
        }
    }
}

struct BreathingCircle: View {
    let phase: Int
    @State private var scale: CGFloat = 1.0

    private var instruction: String {
        let cycle = phase % 8
        if cycle < 3 { return "Вдох..." }
        if cycle < 5 { return "Задержка..." }
        return "Выдох..."
    }

    private var targetScale: CGFloat {
        let cycle = phase % 8
        return cycle < 5 ? 1.4 : 0.7
    }

    var body: some View {
        VStack(spacing: 24) {
            ZStack {
                Circle()
                    .fill(.white.opacity(0.25))
                    .frame(width: 200, height: 200)
                    .scaleEffect(scale)
                Circle()
                    .fill(.white.opacity(0.4))
                    .frame(width: 120, height: 120)
                    .scaleEffect(scale)
            }
            Text(instruction)
                .font(.title.bold())
                .foregroundStyle(.white)
            Text("Следуй за кругом")
                .foregroundStyle(.white.opacity(0.8))
        }
        .onChange(of: phase) { _, _ in
            withAnimation(.easeInOut(duration: 3)) { scale = targetScale }
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 3)) { scale = targetScale }
        }
    }
}

struct CoinDropAnimation: View {
    let visible: Bool
    let totalCoins: Int
    @State private var scale: CGFloat = 0

    var body: some View {
        if visible {
            VStack(spacing: 8) {
                Text("💰")
                    .font(.system(size: 64 * max(scale, 0.1)))
                    .scaleEffect(scale)
                Text("+1 монетка")
                    .font(.title2.bold())
                    .foregroundStyle(QadamColors.willpowerYellow)
                Text("В копилке: \(totalCoins)")
                    .foregroundStyle(.white.opacity(0.9))
            }
            .onAppear {
                withAnimation(.spring(duration: 0.4)) { scale = 1.2 }
                withAnimation(.easeOut(duration: 0.2).delay(0.4)) { scale = 1.0 }
            }
        }
    }
}

struct SosFullscreenBackground<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        ZStack {
            QadamColors.sosRedDark.ignoresSafeArea()
            content
        }
    }
}
