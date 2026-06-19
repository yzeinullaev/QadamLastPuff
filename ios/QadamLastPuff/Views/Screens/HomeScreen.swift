import SwiftUI

struct HomeScreen: View {
    @ObservedObject var viewModel: AppViewModel
    let onSosClick: () -> Void
    let onEmergencySosClick: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                HugeSosButton(onTap: onSosClick, onLongPress: onEmergencySosClick)

                if let stats = viewModel.homeStats {
                    homeStatsContent(stats)
                } else {
                    QadamCard {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Настрой профиль")
                                .font(.title3.bold())
                            Text("Заполни данные в onboarding — и здесь появятся дни без курения, сэкономленные деньги и мотивация.")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .padding(20)
        }
    }

    @ViewBuilder
    private func homeStatsContent(_ stats: HomeStats) -> some View {
        QadamCard {
            VStack(alignment: .leading, spacing: 8) {
                Text(stats.isJustStarted ? "Сегодня ты принял важное решение." : "Ты на пути к свободе.")
                    .font(.title3.bold())
                Text(stats.dailyLifeCard)
                    .foregroundStyle(.secondary)
            }
        }

        if let reason = stats.personalReason {
            QadamCard {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Почему нельзя сейчас?")
                        .font(.subheadline.bold())
                        .foregroundStyle(QadamColors.greenPrimary)
                    Text(reason)
                        .font(.headline)
                }
            }
        }

        if let ago = stats.lastVictoryAgo {
            QadamCard {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Последняя победа").font(.subheadline).foregroundStyle(.secondary)
                    Text("Ты победил тягу \(ago)").foregroundStyle(QadamColors.greenPrimary)
                }
            }
        }

        if stats.totalCoins > 0 {
            QadamCard {
                HStack {
                    Text("Копилка побед").font(.headline)
                    Spacer()
                    Text("💰 \(stats.totalCoins)").font(.title2)
                }
            }
        }

        Text("Твой прогресс")
            .font(.headline)
            .foregroundStyle(.secondary)
            .frame(maxWidth: .infinity, alignment: .leading)

        QadamCard {
            VStack(spacing: 6) {
                Text("\(stats.days)")
                    .font(.system(size: 56, weight: .bold))
                    .foregroundStyle(QadamColors.greenPrimary)
                Text(dayLabel(stats.days, hours: stats.hours))
                if stats.days > 0 || stats.hours > 0 {
                    Text("\(stats.hours) ч \(stats.minutes) мин без курения")
                        .foregroundStyle(.secondary)
                }
            }
            .frame(maxWidth: .infinity)
        }

        HStack(spacing: 12) {
            StatCard(
                title: "Не выкурено",
                value: stats.cigarettesNotSmoked == 0 ? "—" : "\(stats.cigarettesNotSmoked)",
                subtitle: stats.cigarettesNotSmoked > 0 ? "сигарет" : "скоро здесь"
            )
            StatCard(
                title: "Сэкономлено",
                value: StatsCalculator.formatMoney(stats.moneySaved),
                subtitle: viewModel.profile?.currency ?? "₸"
            )
        }

        if let goal = viewModel.moneyGoal {
            QadamCard {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Цель: \(goal.title)")
                    Text(String(format: "%.0f / %.0f \(viewModel.profile?.currency ?? "₸")", stats.moneySaved, goal.amount))
                        .font(.headline)
                        .foregroundStyle(QadamColors.greenPrimary)
                }
            }
        }

        QadamCard {
            Text("\"\(stats.motivationalQuote)\"")
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
        }
    }

    private func dayLabel(_ days: Int, hours: Int64) -> String {
        if days == 0 && hours == 0 { return "только начал" }
        if days == 1 { return "день" }
        if (2...4).contains(days) { return "дня" }
        return "дней"
    }
}
