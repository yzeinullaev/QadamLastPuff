import SwiftUI

struct SmokeQLogo: View {
    var size: CGFloat = 200
    var animated: Bool = true
    var showSmoke: Bool = true
    var glowPulse: Bool = false

    @State private var progress: CGFloat = 0
    @State private var smokeOpacity: Double = 1
    @State private var glowScale: CGFloat = 1

    private let particles: [SmokeParticle] = SmokeParticle.make(count: 20)

    var body: some View {
        ZStack {
            if showSmoke && smokeOpacity > 0.01 {
                ForEach(particles.indices, id: \.self) { index in
                    SmokePuffView(
                        particle: particles[index],
                        progress: progress,
                        opacity: smokeOpacity
                    )
                }
            }

            GlowingQLetter(progress: progress, glowScale: glowScale)
        }
        .frame(width: size, height: size)
        .onAppear { startAnimation() }
        .onChange(of: glowPulse) { _, pulse in
            if pulse { startGlowPulse() }
        }
    }

    private func startAnimation() {
        if animated {
            withAnimation(.easeInOut(duration: 2.5)) { progress = 1 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                withAnimation(.easeOut(duration: 0.9)) { smokeOpacity = 0 }
            }
        } else {
            progress = 1
            smokeOpacity = 0
            if glowPulse { startGlowPulse() }
        }
    }

    private func startGlowPulse() {
        withAnimation(.easeInOut(duration: 1.8).repeatForever(autoreverses: true)) {
            glowScale = 1.08
        }
    }
}

struct GlowingQLogo: View {
    var size: CGFloat = 88

    var body: some View {
        SmokeQLogo(size: size, animated: false, showSmoke: false, glowPulse: true)
    }
}

private struct GlowingQLetter: View {
    let progress: CGFloat
    let glowScale: CGFloat

    var body: some View {
        let alpha = max(0, min(1, (progress - 0.45) / 0.55))
        ZStack {
            Text("Q")
                .font(.system(size: 72, weight: .bold, design: .rounded))
                .foregroundStyle(QadamColors.greenSecondary.opacity(0.35 * alpha))
                .blur(radius: 12)
                .scaleEffect(glowScale)
            Text("Q")
                .font(.system(size: 72, weight: .bold, design: .rounded))
                .foregroundStyle(
                    LinearGradient(
                        colors: [Color(hex: 0xB8F5D4), QadamColors.greenSecondary],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .opacity(alpha)
                .scaleEffect((0.85 + progress * 0.15) * glowScale)
        }
    }
}

private struct SmokePuffView: View {
    let particle: SmokeParticle
    let progress: CGFloat
    let opacity: Double

    var body: some View {
        let localT = max(0, min(1, (progress - particle.delay) / (1 - particle.delay)))
        let eased = easeInOut(localT)
        let x = lerp(particle.start.x, particle.target.x, eased)
        let y = lerp(particle.start.y, particle.target.y, eased)
        let scale = lerp(1.8, 0.6, eased) * particle.sizeFactor
        let alpha = lerp(0.12, 0.7, eased) * opacity

        Circle()
            .fill(
                RadialGradient(
                    colors: [
                        Color(hex: 0x6BCF9A, alpha: alpha),
                        Color(hex: 0x3A8F62, alpha: alpha * 0.4),
                        .clear
                    ],
                    center: .center,
                    startRadius: 0,
                    endRadius: 30
                )
            )
            .frame(width: 40 * scale, height: 40 * scale)
            .position(x: x, y: y)
    }

    private func easeInOut(_ t: CGFloat) -> CGFloat {
        t < 0.5 ? 2 * t * t : 1 - pow(-2 * t + 2, 2) / 2
    }

    private func lerp(_ a: CGFloat, _ b: CGFloat, _ t: CGFloat) -> CGFloat {
        a + (b - a) * t
    }
}

private struct SmokeParticle {
    let start: CGPoint
    let target: CGPoint
    let delay: CGFloat
    let sizeFactor: CGFloat

    static func make(count: Int) -> [SmokeParticle] {
        let cx: CGFloat = 100
        let cy: CGFloat = 100
        let r: CGFloat = 44
        var particles: [SmokeParticle] = []
        let ringCount = max(1, Int(Double(count) * 0.75))

        for i in 0..<ringCount {
            let t = CGFloat(i) / CGFloat(max(ringCount - 1, 1))
            let angle = lerp(2.35, -0.25, t)
            let target = CGPoint(x: cx + cos(angle) * r, y: cy + sin(angle) * r)
            let scatter = CGFloat.random(in: 0.1...0.3)
            let start = CGPoint(x: cx + CGFloat.random(in: -scatter...scatter) * 80,
                                y: cy + 60 + CGFloat.random(in: 0...scatter) * 40)
            particles.append(SmokeParticle(
                start: start,
                target: target,
                delay: CGFloat(i % 4) * 0.05 + CGFloat.random(in: 0...0.1),
                sizeFactor: CGFloat.random(in: 0.7...1.3)
            ))
        }

        let tailCount = count - ringCount
        for i in 0..<tailCount {
            let t = tailCount <= 1 ? 1 : CGFloat(i) / CGFloat(tailCount - 1)
            let target = CGPoint(x: lerp(116, 148, t), y: lerp(116, 164, t))
            particles.append(SmokeParticle(
                start: CGPoint(x: cx, y: cy + 50),
                target: target,
                delay: 0.15 + CGFloat(i) * 0.04,
                sizeFactor: 0.9
            ))
        }
        return particles
    }

    private static func lerp(_ a: CGFloat, _ b: CGFloat, _ t: CGFloat) -> CGFloat {
        a + (b - a) * t
    }
}
