import SwiftUI

enum PhoneMask {
    static let placeholder = "+7 (___) ___-__-__"

    static func format(_ input: String) -> String {
        var digits = input.filter(\.isNumber)
        if digits.hasPrefix("8") {
            digits = "7" + digits.dropFirst()
        }
        if digits.hasPrefix("7") {
            digits = String(digits.dropFirst())
        }
        digits = String(digits.prefix(10))

        var result = "+7"
        guard !digits.isEmpty else { return result }

        result += " ("
        let area = digits.prefix(3)
        result += area
        let rest = digits.dropFirst(3)

        if area.count == 3, !rest.isEmpty {
            result += ") "
            let mid = rest.prefix(3)
            result += mid
            let tail = rest.dropFirst(3)
            if mid.count == 3, !tail.isEmpty {
                result += "-" + tail.prefix(2)
                let last = tail.dropFirst(2)
                if tail.count >= 2, !last.isEmpty {
                    result += "-" + last.prefix(2)
                }
            }
        }
        return result
    }

    static func isComplete(_ input: String) -> Bool {
        ContactUtils.normalizePhone(input).count == 11
    }
}

struct PhoneTextField: View {
    @Binding var text: String
    var placeholder: String = PhoneMask.placeholder

    var body: some View {
        TextField(placeholder, text: Binding(
            get: { PhoneMask.format(text) },
            set: { text = PhoneMask.format($0) }
        ))
        .keyboardType(.phonePad)
        .textContentType(.telephoneNumber)
    }
}
