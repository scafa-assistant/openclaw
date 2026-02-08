import SwiftUI

struct OnboardingView: View {
    @State private var currentPage = 0
    @Binding var isCompleted: Bool
    
    // Reduziert auf 3 Screens
    let pages = [
        OnboardingPage(
            title: "Dein AI-Assistent",
            description: "Sprich natürlich mit OpenClaw. Er versteht dich und hilft dir bei allem.",
            image: "mic.fill",
            color: .accentColor
        ),
        OnboardingPage(
            title: "Wähle dein Modell",
            description: "Nutze Gemini, Claude oder GPT. Wechsle jederzeit nach deinen Bedürfnissen.",
            image: "brain.head.profile",
            color: .purple
        ),
        OnboardingPage(
            title: "Datenschutz first",
            description: "Deine Daten bleiben deine. Verschlüsselt und sicher gespeichert.",
            image: "lock.shield.fill",
            color: .green
        )
    ]
    
    var body: some View {
        VStack(spacing: 0) {
            // Skip button
            HStack {
                Spacer()
                Button("Überspringen") {
                    completeOnboarding()
                }
                .foregroundColor(.secondary)
                .padding()
            }
            
            // Page content
            TabView(selection: $currentPage) {
                ForEach(0..<pages.count, id: \.self) { index in
                    OnboardingPageView(page: pages[index])
                        .tag(index)
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
            
            // Bottom section
            VStack(spacing: 24) {
                // Page indicators
                HStack(spacing: 8) {
                    ForEach(0..<pages.count, id: \.self) { index in
                        RoundedRectangle(cornerRadius: 4)
                            .fill(currentPage == index ? Color.accentColor : Color.gray.opacity(0.3))
                            .frame(width: currentPage == index ? 24 : 8, height: 8)
                    }
                }
                
                // Action button
                Button(action: {
                    if currentPage == pages.count - 1 {
                        completeOnboarding()
                    } else {
                        withAnimation {
                            currentPage += 1
                        }
                    }
                }) {
                    Text(currentPage == pages.count - 1 ? "Los geht's" : "Weiter")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.accentColor)
                        .cornerRadius(12)
                }
            }
            .padding(24)
        }
    }
    
    private func completeOnboarding() {
        UserDefaults.standard.set(true, forKey: "has_completed_onboarding")
        withAnimation {
            isCompleted = true
        }
    }
}

struct OnboardingPage {
    let title: String
    let description: String
    let image: String
    let color: Color
}

struct OnboardingPageView: View {
    let page: OnboardingPage
    
    var body: some View {
        VStack(spacing: 32) {
            Spacer()
            
            // Icon
            ZStack {
                Circle()
                    .fill(page.color.opacity(0.2))
                    .frame(width: 140, height: 140)
                
                Image(systemName: page.image)
                    .font(.system(size: 60))
                    .foregroundColor(page.color)
            }
            
            // Text
            VStack(spacing: 16) {
                Text(page.title)
                    .font(.title)
                    .fontWeight(.bold)
                    .multilineTextAlignment(.center)
                
                Text(page.description)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            
            Spacer()
        }
    }
}