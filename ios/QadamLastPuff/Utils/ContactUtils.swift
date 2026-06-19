import UIKit

enum ContactUtils {
    static func call(phone: String) {
        let digits = normalizePhone(phone)
        guard !digits.isEmpty else { return }
        openURL(URL(string: "tel:+\(digits)"))
    }

    static func sms(phone: String, message: String) {
        let digits = normalizePhone(phone)
        guard !digits.isEmpty else { return }
        var components = URLComponents()
        components.scheme = "sms"
        components.path = "+\(digits)"
        components.queryItems = [URLQueryItem(name: "body", value: message)]
        openURL(components.url)
    }

    static func whatsApp(phone: String, message: String) {
        let digits = normalizePhone(phone)
        guard !digits.isEmpty else { return }

        var components = URLComponents(string: "https://api.whatsapp.com/send")!
        components.queryItems = [
            URLQueryItem(name: "phone", value: digits),
            URLQueryItem(name: "text", value: message)
        ]
        if let url = components.url {
            openURL(url)
            return
        }

        let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        openURL(URL(string: "https://wa.me/\(digits)?text=\(encoded)"))
    }

    static func telegram(phone: String, message: String) {
        let digits = normalizePhone(phone)
        guard !digits.isEmpty else { return }

        // tg://resolve открывает чат по номеру в установленном Telegram
        var tgComponents = URLComponents()
        tgComponents.scheme = "tg"
        tgComponents.host = "resolve"
        tgComponents.queryItems = [
            URLQueryItem(name: "phone", value: "+\(digits)"),
            URLQueryItem(name: "text", value: message)
        ]
        if let tgURL = tgComponents.url {
            openURL(tgURL)
            return
        }

        var webComponents = URLComponents(string: "https://t.me/+\(digits)")!
        webComponents.queryItems = [URLQueryItem(name: "text", value: message)]
        openURL(webComponents.url)
    }

    static func normalizePhone(_ phone: String) -> String {
        var digits = phone.filter(\.isNumber)
        if digits.hasPrefix("8"), digits.count == 11 {
            digits = "7" + String(digits.dropFirst())
        } else if digits.hasPrefix("7"), digits.count == 11 {
            // already correct for KZ/RU
        } else if digits.count == 10 {
            digits = "7" + digits
        }
        return digits
    }

    private static func openURL(_ url: URL?) {
        guard let url else { return }
        DispatchQueue.main.async {
            UIApplication.shared.open(url, options: [:])
        }
    }
}
