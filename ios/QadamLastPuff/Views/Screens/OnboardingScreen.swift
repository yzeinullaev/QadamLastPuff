import SwiftUI

struct OnboardingScreen: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var step = 0
    @State private var smokeType = AppConstants.smokeTypes[0]
    @State private var cigarettesPerDay = "10"
    @State private var packPrice = "1500"
    @State private var cigarettesInPack = "20"
    @State private var selectedReasons: Set<String> = []
    @State private var sosName = ""
    @State private var sosPhone = ""
    @State private var sosMessage = "Мне сейчас очень хочется курить. Пожалуйста, поддержи меня."
    @State private var personalLetter = """
    Если ты читаешь это — значит снова захотел курить.

    Пожалуйста, не сдавайся. Ты сам просил напомнить тебе об этом.
    """
    @State private var lastSmokeDate = Date()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Qadam Last Puff")
                    .font(.largeTitle.bold())
                    .foregroundStyle(QadamColors.greenPrimary)
                Text("Твой путь к свободе от курения")
                    .foregroundStyle(.secondary)

                switch step {
                case 0: smokingStep
                case 1: dateStep
                case 2: reasonsStep
                case 3: letterStep
                default: contactStep
                }

                HStack {
                    if step > 0 {
                        Button("Назад") { step -= 1 }
                    }
                    Spacer()
                    Button(step < 4 ? "Далее" : "Начать путь") {
                        if step < 4 { step += 1 } else { complete() }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(QadamColors.greenPrimary)
                    .disabled(step == 2 && selectedReasons.isEmpty)
                }
            }
            .padding(24)
        }
    }

    private var smokingStep: some View {
        Group {
            SectionTitle(title: "Тип курения")
            ChipFlowView(items: AppConstants.smokeTypes, selection: smokeType) { smokeType = $0 }
            TextField("Сколько в день (сигарет/затяжек)", text: $cigarettesPerDay)
                .keyboardType(.numberPad)
                .textFieldStyle(.roundedBorder)
            TextField("Цена пачки (₸)", text: $packPrice)
                .keyboardType(.decimalPad)
                .textFieldStyle(.roundedBorder)
            TextField("Сигарет в пачке", text: $cigarettesInPack)
                .keyboardType(.numberPad)
                .textFieldStyle(.roundedBorder)
        }
    }

    private var dateStep: some View {
        Group {
            SectionTitle(title: "Когда была последняя сигарета?")
            DatePicker("Дата и время", selection: $lastSmokeDate)
                .datePickerStyle(.graphical)
        }
    }

    private var reasonsStep: some View {
        Group {
            SectionTitle(title: "Почему ты бросаешь?")
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 96), spacing: 8)], alignment: .leading, spacing: 8) {
                ForEach(AppConstants.quitReasons, id: \.self) { reason in
                    ChipButton(title: reason, selected: selectedReasons.contains(reason)) {
                        if selectedReasons.contains(reason) { selectedReasons.remove(reason) }
                        else { selectedReasons.insert(reason) }
                    }
                }
            }
        }
    }

    private var letterStep: some View {
        Group {
            SectionTitle(title: "Письмо себе")
            TextEditor(text: $personalLetter)
                .frame(minHeight: 160)
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(.secondary.opacity(0.3)))
        }
    }

    private var contactStep: some View {
        Group {
            SectionTitle(title: "SOS-контакт")
            TextField("Имя", text: $sosName).textFieldStyle(.roundedBorder)
            PhoneTextField(text: $sosPhone)
                .textFieldStyle(.roundedBorder)
            TextField("Сообщение", text: $sosMessage, axis: .vertical).textFieldStyle(.roundedBorder)
        }
    }

    private func complete() {
        let profile = UserProfile(
            smokeType: smokeType,
            cigarettesPerDay: Int(cigarettesPerDay) ?? 10,
            packPrice: Double(packPrice) ?? 1500,
            cigarettesInPack: Int(cigarettesInPack) ?? 20,
            lastSmokeDate: lastSmokeDate,
            reasons: Array(selectedReasons)
        )
        let contact = SosContact(
            name: sosName.isEmpty ? "Близкий" : sosName,
            phone: sosPhone,
            message: sosMessage
        )
        viewModel.completeOnboarding(profile: profile, sosContact: contact, personalLetter: personalLetter)
    }
}

struct ChipButton: View {
    let title: String
    let selected: Bool
    var lightOnDark: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .frame(maxWidth: .infinity)
                .background(selected
                    ? (lightOnDark ? Color.white : QadamColors.greenPrimary)
                    : (lightOnDark ? Color.white.opacity(0.2) : Color.secondary.opacity(0.15)))
                .foregroundStyle(selected
                    ? (lightOnDark ? QadamColors.sosRedDark : .white)
                    : (lightOnDark ? .white : Color.primary))
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

struct ChipFlowView: View {
    let items: [String]
    let selection: String
    var lightOnDark: Bool = false
    let onSelect: (String) -> Void

    private let columns = [GridItem(.adaptive(minimum: 96), spacing: 8)]

    var body: some View {
        LazyVGrid(columns: columns, alignment: .leading, spacing: 8) {
            ForEach(items, id: \.self) { item in
                ChipButton(title: item, selected: selection == item, lightOnDark: lightOnDark) {
                    onSelect(item)
                }
            }
        }
    }
}

struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrange(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrange(proposal: proposal, subviews: subviews)
        for (index, position) in result.positions.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + position.x, y: bounds.minY + position.y), proposal: .unspecified)
        }
    }

    private func arrange(proposal: ProposedViewSize, subviews: Subviews) -> (size: CGSize, positions: [CGPoint]) {
        let resolved = proposal.replacingUnspecifiedDimensions(by: CGSize(width: 340, height: 1000))
        let maxWidth = resolved.width
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var positions: [CGPoint] = []

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth, x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
        }
        return (CGSize(width: maxWidth, height: y + rowHeight), positions)
    }
}
