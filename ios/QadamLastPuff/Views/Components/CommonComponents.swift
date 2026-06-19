import SwiftUI

struct QadamCard<Content: View>: View {
    let content: Content
    @Environment(\.colorScheme) private var colorScheme

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(QadamTheme.surface(colorScheme == .dark))
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
    }
}

struct StatCard: View {
    let title: String
    let value: String
    var subtitle: String?

    var body: some View {
        QadamCard {
            VStack(spacing: 6) {
                Text(title)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(.title2.bold())
                    .foregroundStyle(QadamColors.greenPrimary)
                    .multilineTextAlignment(.center)
                if let subtitle {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            .frame(maxWidth: .infinity)
        }
    }
}

struct SectionTitle: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.title2.bold())
            .padding(.vertical, 8)
    }
}

struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
            Spacer()
            Text(value)
                .foregroundStyle(QadamColors.greenPrimary)
        }
        .padding(.vertical, 6)
    }
}

struct RelapseReportCard: View {
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(QadamColors.coralAccent.opacity(colorScheme == .dark ? 0.22 : 0.14))
                        .frame(width: 48, height: 48)
                    Text("💚")
                        .font(.title2)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Был срыв?")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(.primary)
                    Text("Честно записать — и продолжить путь")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer(minLength: 8)

                Image(systemName: "chevron.right")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.tertiary)
            }
            .padding(16)
            .background(QadamTheme.surface(colorScheme == .dark))
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay {
                RoundedRectangle(cornerRadius: 20)
                    .strokeBorder(QadamColors.coralAccent.opacity(0.28), lineWidth: 1)
            }
            .shadow(color: .black.opacity(colorScheme == .dark ? 0.2 : 0.06), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
    }
}
