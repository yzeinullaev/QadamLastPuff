import SwiftUI

struct ProgressScreen: View {
    @ObservedObject var viewModel: AppViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                SectionTitle(title: "Твой прогресс")

                if !viewModel.victories.isEmpty {
                    SectionTitle(title: "История побед")
                    ForEach(viewModel.victories.prefix(10)) { victory in
                        QadamCard {
                            HStack {
                                Text(victory.time).font(.headline)
                                Spacer()
                                Text(TimeFormatUtils.intensityStars(victory.intensity))
                                    .foregroundStyle(QadamColors.coralAccent)
                            }
                            Text(victory.message).foregroundStyle(.secondary)
                        }
                    }
                }

                if let stats = viewModel.progressStats {
                    HStack(spacing: 12) {
                        StatCard(title: "Дней", value: "\(stats.daysWithoutSmoking)")
                        StatCard(title: "Не выкурено", value: "\(stats.cigarettesNotSmoked)")
                    }
                    StatCard(
                        title: "Сэкономлено",
                        value: String(format: "%.0f \(viewModel.profile?.currency ?? "₸")", stats.moneySaved)
                    )
                    QadamCard {
                        InfoRow(label: "Всего тяг", value: "\(stats.totalCravings)")
                        InfoRow(label: "Справился", value: "\(stats.cravingsWon)")
                        InfoRow(label: "Срывов", value: "\(stats.relapses)")
                        InfoRow(label: "Процент побед", value: String(format: "%.0f%%", stats.winRate))
                        InfoRow(label: "Самая длинная серия", value: "\(stats.longestStreak) дн.")
                        InfoRow(label: "Средняя сила тяги", value: String(format: "%.1f", stats.averageIntensity))
                    }
                    if !stats.dangerousHours.isEmpty {
                        SectionTitle(title: "Самые опасные часы")
                        QadamCard {
                            ForEach(stats.dangerousHours, id: \.0) { hour, count in
                                InfoRow(label: "\(hour):00 – \(hour + 1):00", value: "\(count) раз")
                            }
                        }
                    }
                    if !stats.topTriggers.isEmpty {
                        SectionTitle(title: "Частые причины тяги")
                        QadamCard {
                            ForEach(stats.topTriggers, id: \.0) { trigger, count in
                                InfoRow(label: trigger, value: "\(count) раз")
                            }
                        }
                    }
                } else {
                    Text("Загрузка...")
                }
            }
            .padding(20)
        }
    }
}
