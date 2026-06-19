import SwiftUI

struct SplashRoute: View {
    @ObservedObject var viewModel: AppViewModel
    let onFinished: () -> Void

    private var showFirstLaunchIntro: Bool {
        viewModel.isFirstLaunch && !viewModel.onboardingCompleted
    }

    var body: some View {
        Group {
            if showFirstLaunchIntro {
                FirstLaunchIntroScreen {
                    viewModel.completeFirstLaunch()
                    onFinished()
                }
            } else {
                RegularSplashScreen(viewModel: viewModel) {
                    onFinished()
                }
            }
        }
        .onAppear {
            viewModel.refreshComputed()
            if viewModel.isFirstLaunch && viewModel.onboardingCompleted {
                viewModel.completeFirstLaunch()
            }
        }
    }
}
