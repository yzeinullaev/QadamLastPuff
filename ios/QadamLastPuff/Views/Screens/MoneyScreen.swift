import SwiftUI

struct MoneyScreen: View {
    @ObservedObject var viewModel: AppViewModel
    @State private var title = ""
    @State private var amount = ""
    @State private var showNewGoalForm = false
    @FocusState private var focusedField: Field?

    private enum Field { case title, amount }

    private var saved: Double { viewModel.moneySavedNow() }
    private var currency: String { viewModel.profile?.currency ?? "₸" }

    var body: some View {
        NavigationStack {
            scrollContent
                .navigationTitle("Цель")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItemGroup(placement: .keyboard) {
                        Spacer()
                        Button("Готово") { focusedField = nil }
                    }
                }
        }
        .onAppear {
            viewModel.refreshComputed()
            title = viewModel.moneyGoal?.title ?? ""
            amount = viewModel.moneyGoal.map { String(Int($0.amount)) } ?? ""
            showNewGoalForm = viewModel.moneyGoal == nil
        }
    }

    private var scrollContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                SectionTitle(title: "Деньги и цель")

                QadamCard {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Сэкономлено сейчас")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        Text("\(StatsCalculator.formatMoney(saved)) \(currency)")
                            .font(.system(size: 36, weight: .bold))
                            .foregroundStyle(QadamColors.greenPrimary)
                        Text("Сумма растёт каждый день без курения")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                if let goal = viewModel.moneyGoal {
                    let target = goal.amount
                    let progress = target > 0 ? min(saved / target, 1.0) : 0
                    let remaining = max(target - saved, 0)
                    let reached = saved >= target

                    QadamCard {
                        VStack(alignment: .leading, spacing: 12) {
                            HStack {
                                Text(goal.title).font(.title3.bold())
                                Spacer()
                                if reached {
                                    Text("🎉 Достигнуто!")
                                        .font(.caption.bold())
                                        .foregroundStyle(QadamColors.greenPrimary)
                                }
                            }

                            ProgressView(value: progress)
                                .tint(reached ? QadamColors.coralAccent : QadamColors.greenPrimary)

                            if reached {
                                Text("Ты накопил \(String(format: "%.0f", saved)) \(currency) — цель выполнена!")
                                    .foregroundStyle(QadamColors.greenPrimary)
                                Button("Сохранить в историю и поставить новую цель") {
                                    viewModel.archiveCompletedGoal()
                                    title = ""
                                    amount = ""
                                    showNewGoalForm = true
                                }
                                .buttonStyle(.borderedProminent)
                                .tint(QadamColors.greenPrimary)
                            } else {
                                Text(String(format: "%.0f / %.0f %@", saved, target, currency))
                                    .font(.headline)
                                Text(String(format: "Осталось: %.0f %@ (%.0f%%)", remaining, currency, progress * 100))
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                } else {
                    QadamCard {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Пока нет активной цели")
                                .font(.headline)
                            Text("Поставь цель — например, на что потратишь сэкономленные деньги. Прогресс будет расти автоматически.")
                                .foregroundStyle(.secondary)
                        }
                    }
                }

                if viewModel.moneyGoal == nil || showNewGoalForm {
                    goalForm
                }

                if !viewModel.completedGoals.isEmpty {
                    SectionTitle(title: "История целей")
                    ForEach(viewModel.completedGoals) { completed in
                        QadamCard {
                            HStack {
                                Text("✅")
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(completed.title).font(.headline)
                                    Text(String(format: "%.0f %@", completed.savedAmount, currency))
                                        .foregroundStyle(QadamColors.greenPrimary)
                                    Text(TimeFormatUtils.formatDateTime(completed.completedAt))
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
    }

    private var goalForm: some View {
        Group {
            Text(viewModel.moneyGoal == nil ? "Новая цель" : "Изменить цель")
                .font(.headline)

            TextField("Название цели", text: $title, prompt: Text("Например: Новые кроссовки"))
                .textFieldStyle(.roundedBorder)
                .focused($focusedField, equals: .title)

            TextField("Сумма цели (\(currency))", text: $amount)
                .keyboardType(.numberPad)
                .textFieldStyle(.roundedBorder)
                .focused($focusedField, equals: .amount)

            Button(viewModel.moneyGoal == nil ? "Создать цель" : "Сохранить") {
                guard let parsed = Double(amount), !title.isEmpty else { return }
                focusedField = nil
                viewModel.saveMoneyGoal(MoneyGoal(title: title, amount: parsed))
                showNewGoalForm = false
            }
            .buttonStyle(.borderedProminent)
            .tint(QadamColors.greenPrimary)
            .disabled(title.isEmpty || amount.isEmpty)
        }
    }
}
