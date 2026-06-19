import SwiftUI

struct SosScreen: View {
    @ObservedObject var viewModel: AppViewModel
    let onBack: () -> Void
    let onComplete: () -> Void

    @State private var timerTask: Task<Void, Never>?
    @State private var breathingTask: Task<Void, Never>?

    var body: some View {
        Group {
            switch viewModel.sosState.step {
            case .emergency:
                EmergencyScreen(
                    contact: viewModel.sosContact,
                    onCall: { if let c = viewModel.sosContact { ContactUtils.call(phone: c.phone) } },
                    onSms: { if let c = viewModel.sosContact { ContactUtils.sms(phone: c.phone, message: c.message) } },
                    onWhatsApp: { if let c = viewModel.sosContact { ContactUtils.whatsApp(phone: c.phone, message: c.message) } },
                    onTelegram: { if let c = viewModel.sosContact { ContactUtils.telegram(phone: c.phone, message: c.message) } },
                    onBreathing: { viewModel.startBreathing() },
                    onTimer: { viewModel.startTimerFromEmergency() },
                    onBack: { viewModel.resetSos(); onBack() }
                )
            case .intake:
                intakeView
            case .timer, .breathing:
                timerBreathingView
            case .result:
                resultView
            case .success:
                BodyVictoryScreen(
                    before: viewModel.sosState.recoveryBefore,
                    after: viewModel.sosState.recoveryAfter,
                    showCoinAnimation: viewModel.sosState.showCoinAnimation,
                    totalCoins: viewModel.homeStats?.totalCoins ?? 0,
                    onContinue: { viewModel.resetSos(); onComplete() }
                )
            case .relapse:
                relapseView
            }
        }
        .onAppear { startTimers() }
        .onDisappear { stopTimers() }
        .onChange(of: viewModel.sosState.step) { _, _ in startTimers() }
        .alert("Письмо себе", isPresented: Binding(
            get: { viewModel.sosState.showPersonalLetterDialog },
            set: { if !$0 { viewModel.dismissPersonalLetter() } }
        )) {
            Button("Понятно") { viewModel.dismissPersonalLetter() }
        } message: {
            Text(viewModel.personalLetter ?? "")
        }
        .alert("Почему ты бросаешь?", isPresented: Binding(
            get: { viewModel.sosState.showReasons },
            set: { if !$0 { viewModel.dismissReasons() } }
        )) {
            Button("Закрыть") { viewModel.dismissReasons() }
        } message: {
            Text(viewModel.quitReasons().joined(separator: "\n"))
        }
        .confirmationDialog("Выйти из таймера?", isPresented: Binding(
            get: { viewModel.sosState.showExitDialog },
            set: { if !$0 { viewModel.dismissSosExitDialog() } }
        ), titleVisibility: .visible) {
            Button("Таймер не нужен — выйти") {
                viewModel.exitSosWithoutTimer()
                onBack()
            }
            Button("Обнулить и начать заново") {
                viewModel.restartSosTimer()
            }
            Button("Продолжить таймер", role: .cancel) {
                viewModel.dismissSosExitDialog()
            }
        } message: {
            Text("Ты уже \(viewModel.sosState.elapsedSeconds) сек держишься. Что хочешь сделать?")
        }
        .sheet(isPresented: Binding(
            get: { viewModel.sosState.showPhoto },
            set: { if !$0 { viewModel.dismissPhoto() } }
        )) {
            VStack(spacing: 16) {
                Text("Ради них").font(.title2.bold())
                if let uri = viewModel.familyPhotoUri, let url = URL(string: uri) {
                    AsyncImage(url: url) { $0.resizable().scaledToFit() } placeholder: { ProgressView() }
                }
                Button("Закрыть") { viewModel.dismissPhoto() }
            }
            .padding()
        }
    }

    private var intakeView: some View {
        SosFullscreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack {
                        Button {
                            viewModel.resetSos()
                            onBack()
                        } label: {
                            Image(systemName: "xmark")
                                .foregroundStyle(.white)
                                .padding(8)
                                .background(.white.opacity(0.2))
                                .clipShape(Circle())
                        }
                        Spacer()
                    }

                    Text("Расскажи, что происходит")
                        .font(.title2.bold())
                        .foregroundStyle(.white)
                    Text("Это поможет понять твои триггеры и стать сильнее")
                        .foregroundStyle(.white.opacity(0.8))

                    Text("Из-за чего?").font(.headline).foregroundStyle(.white)
                    ChipFlowView(
                        items: AppConstants.cravingTriggers,
                        selection: viewModel.sosState.trigger,
                        lightOnDark: true,
                        onSelect: { viewModel.setSosTrigger($0) }
                    )

                    Text("Где ты сейчас?").font(.headline).foregroundStyle(.white)
                    ChipFlowView(
                        items: AppConstants.cravingLocations,
                        selection: viewModel.sosState.location,
                        lightOnDark: true,
                        onSelect: { viewModel.setSosLocation($0) }
                    )

                    Text("Насколько сильная тяга: \(viewModel.sosState.intensity)")
                        .font(.headline)
                        .foregroundStyle(.white)
                    Slider(value: Binding(
                        get: { Double(viewModel.sosState.intensity) },
                        set: { viewModel.setSosIntensity(Int($0)) }
                    ), in: 1...10, step: 1)
                    .tint(.white)

                    Button("Начать 3 минуты вместе") {
                        viewModel.confirmSosIntakeAndStartTimer()
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.white)
                    .foregroundStyle(QadamColors.sosRedDark)
                    .frame(maxWidth: .infinity)
                    .disabled(viewModel.sosState.trigger.isEmpty)
                }
                .padding(24)
            }
        }
    }

    private var timerBreathingView: some View {
        SosFullscreenBackground {
            VStack(spacing: 0) {
                HStack {
                    Button {
                        if viewModel.sosState.step == .breathing {
                            viewModel.resumeTimer()
                        } else {
                            viewModel.showSosExitDialog()
                        }
                    } label: {
                        Image(systemName: "chevron.left")
                            .foregroundStyle(.white)
                            .padding(10)
                            .background(.white.opacity(0.2))
                            .clipShape(Circle())
                    }
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 8)

                if viewModel.sosState.step == .breathing {
                    Spacer()
                    BreathingCircle(phase: viewModel.sosState.breathingPhase)
                    Spacer()
                } else {
                    timerHeaderSection
                        .padding(.horizontal, 20)
                        .padding(.top, 8)

                    timerMessageSection
                        .frame(height: 200)
                        .padding(.horizontal, 20)

                    Spacer(minLength: 12)

                    timerActionsSection
                        .padding(.horizontal, 20)
                        .padding(.bottom, 28)
                }
            }
        }
    }

    private var timerHeaderSection: some View {
        VStack(spacing: 8) {
            Text("НЕ ЗАКРЫВАЙ")
                .font(.caption)
                .tracking(2)
                .foregroundStyle(.white.opacity(0.8))
            Text("Я С ТОБОЙ")
                .foregroundStyle(.white.opacity(0.9))
            Text(timerText)
                .font(.system(size: 64, weight: .bold))
                .foregroundStyle(.white)
                .monospacedDigit()
            ProgressView(
                value: Double(viewModel.sosState.elapsedSeconds),
                total: Double(AppConstants.sosTimerSeconds)
            )
            .tint(.white)
        }
        .frame(maxWidth: .infinity)
    }

    private var timerMessageSection: some View {
        VStack(spacing: 0) {
            Text(viewModel.sosState.currentMessage)
                .font(.headline)
                .foregroundStyle(.white)
                .multilineTextAlignment(.center)
                .lineLimit(3)
                .minimumScaleFactor(0.85)
                .frame(maxWidth: .infinity)
                .frame(height: 96)

            Group {
                if let secondary = viewModel.sosState.secondaryMessage {
                    Text(secondary)
                        .foregroundStyle(.white.opacity(0.85))
                        .multilineTextAlignment(.center)
                        .lineLimit(2)
                        .minimumScaleFactor(0.85)
                } else {
                    Color.clear
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 72)

            Group {
                if let action = viewModel.sosState.currentAction {
                    Text("\(viewModel.sosState.sosMode.emoji) \(action)")
                        .font(.subheadline)
                        .foregroundStyle(.white.opacity(0.9))
                        .lineLimit(1)
                } else {
                    Color.clear
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 32)
        }
    }

    private var timerActionsSection: some View {
        let contact = viewModel.sosContact
        let hasPhone = PhoneMask.isComplete(contact?.phone ?? "")
        let message = contact?.message ?? ""

        return VStack(spacing: 10) {
            HStack(spacing: 10) {
                actionRow("Дыхание", compact: true) { viewModel.startBreathing() }
                actionRow("Причины", compact: true) { viewModel.showReasons() }
                if viewModel.familyPhotoUri != nil {
                    actionRow("Фото", compact: true) { viewModel.showPhoto() }
                } else {
                    actionPlaceholder(compact: true)
                }
            }

            HStack(spacing: 10) {
                actionRow("📞 Звонок", enabled: hasPhone, compact: true) {
                    if let phone = contact?.phone { ContactUtils.call(phone: phone) }
                }
                actionRow("💬 WhatsApp", enabled: hasPhone, compact: true) {
                    if let phone = contact?.phone { ContactUtils.whatsApp(phone: phone, message: message) }
                }
                actionRow("✈️ Telegram", enabled: hasPhone, compact: true) {
                    if let phone = contact?.phone { ContactUtils.telegram(phone: phone, message: message) }
                }
            }
        }
    }

    private func actionRow(
        _ title: String,
        enabled: Bool = true,
        compact: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Text(title)
                .font(compact ? .subheadline.weight(.medium) : .body.weight(.medium))
                .foregroundStyle(.white.opacity(enabled ? 1 : 0.45))
                .lineLimit(1)
                .minimumScaleFactor(0.75)
                .frame(maxWidth: .infinity)
                .padding(.vertical, compact ? 10 : 12)
                .background(Color.white.opacity(enabled ? 0.2 : 0.08))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }

    private func actionPlaceholder(compact: Bool) -> some View {
        RoundedRectangle(cornerRadius: 12)
            .fill(Color.clear)
            .frame(maxWidth: .infinity)
            .frame(height: compact ? 38 : 44)
    }

    private var resultView: some View {
        SosFullscreenBackground {
            VStack(spacing: 24) {
                Text("Удалось не закурить?")
                    .font(.title2.bold())
                    .foregroundStyle(.white)
                Button("Да, справился") { viewModel.completeSos(success: true) }
                    .buttonStyle(.borderedProminent)
                    .tint(.white)
                    .foregroundStyle(QadamColors.sosRedDark)
                Button("Продолжаю путь") { viewModel.completeSos(success: false) }
                    .buttonStyle(.bordered)
                    .tint(.white)
            }
            .padding(24)
        }
    }

    private var relapseView: some View {
        SosFullscreenBackground {
            VStack(spacing: 16) {
                Text("💚").font(.largeTitle)
                Text(viewModel.sosState.currentMessage)
                    .font(.title3)
                    .foregroundStyle(.white)
                    .multilineTextAlignment(.center)
                Text("Один момент не отменяет весь твой прогресс.")
                    .foregroundStyle(.white.opacity(0.8))
            }
            .padding(24)
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 3.5) {
                    viewModel.resetSos()
                    onComplete()
                }
            }
        }
    }

    private var timerText: String {
        let m = viewModel.sosState.timerSecondsLeft / 60
        let s = viewModel.sosState.timerSecondsLeft % 60
        return String(format: "%d:%02d", m, s)
    }

    private func startTimers() {
        stopTimers()
        if viewModel.sosState.step == .timer {
            timerTask = Task {
                while !Task.isCancelled && viewModel.sosState.step == .timer {
                    try? await Task.sleep(nanoseconds: 1_000_000_000)
                    await MainActor.run { viewModel.tickTimer() }
                }
            }
        }
        if viewModel.sosState.step == .breathing {
            breathingTask = Task {
                while !Task.isCancelled && viewModel.sosState.step == .breathing {
                    try? await Task.sleep(nanoseconds: 4_000_000_000)
                    await MainActor.run { viewModel.tickBreathing() }
                }
            }
        }
    }

    private func stopTimers() {
        timerTask?.cancel()
        breathingTask?.cancel()
    }
}

private struct EmergencyScreen: View {
    let contact: SosContact?
    let onCall: () -> Void
    let onSms: () -> Void
    let onWhatsApp: () -> Void
    let onTelegram: () -> Void
    let onBreathing: () -> Void
    let onTimer: () -> Void
    let onBack: () -> Void

    var body: some View {
        SosFullscreenBackground {
            ScrollView {
                VStack(spacing: 12) {
                    Text("Критическая тяга")
                        .font(.title.bold())
                        .foregroundStyle(.white)
                    if let contact {
                        Text(contact.name).foregroundStyle(.white.opacity(0.8))
                    }
                    emergencyButton("Позвонить", action: onCall)
                    emergencyButton("SMS", action: onSms)
                    emergencyButton("WhatsApp", action: onWhatsApp)
                    emergencyButton("Telegram", action: onTelegram)
                    emergencyButton("Дыхание", action: onBreathing)
                    emergencyButton("⏱ 3 минуты вместе", action: onTimer)
                    Button("Назад", action: onBack)
                        .foregroundStyle(.white.opacity(0.8))
                        .padding(.top, 16)
                }
                .padding(24)
            }
        }
    }

    private func emergencyButton(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.body.weight(.semibold))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.white.opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}
