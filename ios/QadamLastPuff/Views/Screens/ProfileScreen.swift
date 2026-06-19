import SwiftUI
import PhotosUI

struct ProfileScreen: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var smokeType = ""
    @State private var cigarettesPerDay = ""
    @State private var packPrice = ""
    @State private var cigarettesInPack = ""
    @State private var selectedReasons: Set<String> = []
    @State private var sosName = ""
    @State private var sosPhone = ""
    @State private var sosMessage = ""
    @State private var personalLetter = ""
    @State private var lastSmokeDate = Date()
    @State private var showResetDialog = false
    @State private var photoItem: PhotosPickerItem?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                SectionTitle(title: "Профиль")

                if let profile = viewModel.profile {
                    profileForm(profile: profile)
                } else {
                    QadamCard {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Профиль не настроен")
                                .font(.headline)
                            Text("Данные не найдены. Пройди onboarding заново — это займёт пару минут.")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .padding(20)
        }
        .onAppear { loadFromViewModel() }
        .alert("Сбросить прогресс?", isPresented: $showResetDialog) {
            Button("Отмена", role: .cancel) {}
            Button("Сбросить", role: .destructive) { viewModel.resetProgress(); loadFromViewModel() }
        } message: {
            Text("Будут удалены все тяги и срывы. Профиль сохранится.")
        }
    }

    @ViewBuilder
    private func profileForm(profile: UserProfile) -> some View {
        Text("Последняя сигарета: \(TimeFormatUtils.formatDateTime(lastSmokeDate))")
        DatePicker("Дата и время", selection: $lastSmokeDate)

        SectionTitle(title: "Данные курения")
        ChipFlowView(items: AppConstants.smokeTypes, selection: smokeType) { smokeType = $0 }
        TextField("В день", text: $cigarettesPerDay).textFieldStyle(.roundedBorder).keyboardType(.numberPad)
        TextField("Цена пачки", text: $packPrice).textFieldStyle(.roundedBorder).keyboardType(.decimalPad)
        TextField("В пачке", text: $cigarettesInPack).textFieldStyle(.roundedBorder).keyboardType(.numberPad)

        SectionTitle(title: "Причины бросить")
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 96), spacing: 8)], alignment: .leading, spacing: 8) {
            ForEach(AppConstants.quitReasons, id: \.self) { reason in
                ChipButton(title: reason, selected: selectedReasons.contains(reason)) {
                    if selectedReasons.contains(reason) { selectedReasons.remove(reason) }
                    else { selectedReasons.insert(reason) }
                }
            }
        }

        SectionTitle(title: "SOS-контакт")
        TextField("Имя", text: $sosName).textFieldStyle(.roundedBorder)
        PhoneTextField(text: $sosPhone)
            .textFieldStyle(.roundedBorder)
        TextField("Сообщение", text: $sosMessage, axis: .vertical).textFieldStyle(.roundedBorder)

        SectionTitle(title: "Письмо себе")
        TextEditor(text: $personalLetter)
            .frame(minHeight: 120)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(.secondary.opacity(0.3)))

        SectionTitle(title: "Фото близких")
        PhotosPicker(selection: $photoItem, matching: .images) {
            Text("Выбрать фото")
        }
        .onChange(of: photoItem) { _, item in
            Task {
                if let data = try? await item?.loadTransferable(type: Data.self) {
                    let url = FileManager.default.temporaryDirectory.appendingPathComponent("family_photo.jpg")
                    try? data.write(to: url)
                    viewModel.setFamilyPhotoUri(url.absoluteString)
                }
            }
        }
        if let uri = viewModel.familyPhotoUri, let url = URL(string: uri) {
            AsyncImage(url: url) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                ProgressView()
            }
            .frame(height: 180)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }

        SectionTitle(title: "Настройки")
        Toggle("Тёмная тема", isOn: Binding(
            get: { viewModel.darkTheme ?? false },
            set: { viewModel.setDarkTheme($0) }
        ))
        Toggle("Уведомления", isOn: Binding(
            get: { viewModel.notificationsEnabled },
            set: { viewModel.setNotificationsEnabled($0) }
        ))
        Text("Время уведомления: \(viewModel.notificationHour):\(String(format: "%02d", viewModel.notificationMinute))")
            .foregroundStyle(.secondary)

        Button("Сохранить изменения") { save(profile: profile) }
            .buttonStyle(.borderedProminent)
            .tint(QadamColors.greenPrimary)
            .frame(maxWidth: .infinity)

        Button("Сбросить прогресс", role: .destructive) { showResetDialog = true }
            .frame(maxWidth: .infinity)
    }

    private func loadFromViewModel() {
        guard let p = viewModel.profile else { return }
        smokeType = p.smokeType
        cigarettesPerDay = "\(p.cigarettesPerDay)"
        packPrice = "\(p.packPrice)"
        cigarettesInPack = "\(p.cigarettesInPack)"
        selectedReasons = Set(p.reasons)
        lastSmokeDate = p.lastSmokeDate
        sosName = viewModel.sosContact?.name ?? ""
        sosPhone = PhoneMask.format(viewModel.sosContact?.phone ?? "")
        sosMessage = viewModel.sosContact?.message ?? ""
        personalLetter = viewModel.personalLetter ?? ""
    }

    private func save(profile: UserProfile) {
        var updated = profile
        updated.smokeType = smokeType
        updated.cigarettesPerDay = Int(cigarettesPerDay) ?? profile.cigarettesPerDay
        updated.packPrice = Double(packPrice) ?? profile.packPrice
        updated.cigarettesInPack = Int(cigarettesInPack) ?? profile.cigarettesInPack
        updated.reasons = Array(selectedReasons)
        updated.lastSmokeDate = lastSmokeDate
        viewModel.updateProfile(updated)
        viewModel.updateLastSmokeDate(lastSmokeDate)
        viewModel.updateSosContact(SosContact(name: sosName, phone: sosPhone, message: sosMessage))
        viewModel.updatePersonalLetter(personalLetter.isEmpty ? nil : personalLetter)
    }
}
