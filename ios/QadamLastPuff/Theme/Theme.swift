import SwiftUI

enum QadamColors {
    static let greenPrimary = Color(hex: 0x2E9E6A)
    static let greenSecondary = Color(hex: 0x5BB98C)
    static let coralAccent = Color(hex: 0xE87461)
    static let softBackground = Color(hex: 0xF5F7F6)
    static let softSurface = Color.white
    static let darkBackground = Color(hex: 0x121816)
    static let darkSurface = Color(hex: 0x1C2420)
    static let sosRed = Color(hex: 0xE53935)
    static let sosRedDark = Color(hex: 0xB71C1C)
    static let bodyBackground = Color(hex: 0x0A0A0A)
    static let bodyCard = Color(hex: 0x1A1F1C)
    static let recoveryGreen = Color(hex: 0x4CAF50)
    static let willpowerYellow = Color(hex: 0xFFD54F)
    static let splashBackground = Color(hex: 0x0A120E)
}

extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }

    init(hex: UInt32, alpha: Double) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b, opacity: alpha)
    }
}

struct QadamTheme {
    static func background(_ dark: Bool) -> Color { dark ? QadamColors.darkBackground : QadamColors.softBackground }
    static func surface(_ dark: Bool) -> Color { dark ? QadamColors.darkSurface : QadamColors.softSurface }
    static func primary(_ dark: Bool) -> Color { dark ? QadamColors.greenSecondary : QadamColors.greenPrimary }
}
