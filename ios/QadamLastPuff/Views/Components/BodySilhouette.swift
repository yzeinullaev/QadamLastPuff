import SwiftUI

struct BodySilhouette: View {
    let organs: [OrganInfo]
    var waveProgress: CGFloat?
    var highlightHeartLungs = false

    @State private var pulse: CGFloat = 0.88

    var body: some View {
        Canvas { context, size in
            let w = size.width
            let h = size.height
            let cx = w / 2

            drawSilhouette(context: &context, cx: cx, w: w, h: h)

            if let waveProgress {
                let rect = CGRect(x: 0, y: h * (1 - waveProgress), width: w, height: h * waveProgress)
                context.fill(Path(rect), with: .color(QadamColors.recoveryGreen.opacity(0.15)))
            }

            for organ in organs {
                guard let pos = organPositions.first(where: { $0.id == organ.id }) else { continue }
                let center = CGPoint(x: w * pos.x, y: h * pos.y)
                let intensity = CGFloat(min(max(organ.value / 100, 0.2), 1))
                let extra: CGFloat = highlightHeartLungs && ["heart", "lungs"].contains(organ.id) ? 1.5 : 1
                let radius = w * pos.radius * intensity * extra * pulse
                let color = Color(hex: organ.glowColorHex)
                context.fill(
                    Path(ellipseIn: CGRect(x: center.x - radius, y: center.y - radius, width: radius * 2, height: radius * 2)),
                    with: .color(color.opacity(0.5 * intensity))
                )
            }
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 1.8).repeatForever(autoreverses: true)) {
                pulse = 1.0
            }
        }
    }

    private let organPositions: [(id: String, x: CGFloat, y: CGFloat, radius: CGFloat)] = [
        ("brain", 0.50, 0.10, 0.055),
        ("smell", 0.50, 0.145, 0.04),
        ("lungs", 0.50, 0.30, 0.09),
        ("heart", 0.44, 0.28, 0.06),
        ("blood", 0.54, 0.36, 0.05)
    ]

    private func drawSilhouette(context: inout GraphicsContext, cx: CGFloat, w: CGFloat, h: CGFloat) {
        var path = Path()
        let headR = w * 0.08
        path.addEllipse(in: CGRect(x: cx - headR, y: h * 0.04, width: headR * 2, height: headR * 2))
        path.addRoundedRect(in: CGRect(x: cx - w * 0.1, y: h * 0.14, width: w * 0.2, height: h * 0.12), cornerSize: CGSize(width: 8, height: 8))
        path.addRoundedRect(in: CGRect(x: cx - w * 0.14, y: h * 0.26, width: w * 0.28, height: h * 0.28), cornerSize: CGSize(width: 16, height: 16))
        path.addRoundedRect(in: CGRect(x: cx - w * 0.1, y: h * 0.54, width: w * 0.2, height: h * 0.22), cornerSize: CGSize(width: 12, height: 12))
        context.fill(path, with: .color(Color(hex: 0x1E3A5F)))
        context.stroke(path, with: .color(Color(hex: 0x4A7AB5)), lineWidth: 1.5)
    }
}
