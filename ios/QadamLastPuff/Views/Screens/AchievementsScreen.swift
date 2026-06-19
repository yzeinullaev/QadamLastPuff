import SwiftUI

struct AchievementsScreen: View {
    @ObservedObject var viewModel: AppViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                SectionTitle(title: "Достижения")
                let unlocked = viewModel.achievements.filter(\.isUnlocked).count
                Text("Открыто: \(unlocked) из \(viewModel.achievements.count)")
                    .foregroundStyle(.secondary)

                ForEach(viewModel.achievements) { achievement in
                    QadamCard {
                        HStack(spacing: 16) {
                            ZStack {
                                Circle()
                                    .fill(achievement.isUnlocked
                                          ? QadamColors.greenPrimary.opacity(0.15)
                                          : Color.secondary.opacity(0.1))
                                    .frame(width: 56, height: 56)
                                Text(AppConstants.achievementImage(for: achievement.id))
                                    .font(.system(size: 28))
                                    .grayscale(achievement.isUnlocked ? 0 : 1)
                                    .opacity(achievement.isUnlocked ? 1 : 0.4)
                            }
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(achievement.title).font(.headline)
                                    if !achievement.isUnlocked {
                                        Image(systemName: "lock.fill")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                                Text(achievement.description)
                                    .foregroundStyle(.secondary)
                                    .font(.subheadline)
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
    }
}
