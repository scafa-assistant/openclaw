import SwiftUI

struct OnboardingView: View {
    @State private var currentPage = 0
    @Binding var isCompleted: Bool
    
    let pages = [
        OnboardingPage(
            title: "Willkommen bei OpenClaw",
            description: "Dein AI-Assistent, der Siri ersetzt und dir mit intelligenter Sprachsteuerung hilft.",
            image: "waveform.circle.fill",
            color: .accentColor
        ),
        OnboardingPage(
            title: "Sprachsteuerung",
            description: "Sag einfach \"Hey OpenClaw\" oder nutze den Widget. Dein Assistent ist immer bereit.",
            image: "mic.fill",
            color: .green
        ),
        OnboardingPage(
            title: "Wähle dein AI-Modell",
            description: "Nutze Gemini, Claude oder GPT. Wechsle jederzeit nach deinen Bedürfnissen.",
            image: "brain.head.profile",
            color: .purple
        ),
        OnboardingPage(
            title: "Siri Integration",
            description: "Sage \"Hey Siri, frag OpenClaw\" für schnellen Zugriff überall.",
            image: "command",
            color: .orange
        ),
        OnboardingPage(
            title: "Datenschutz first",
            description: "Deine Daten bleiben deine. Ende-zu-Ende Verschlüsselung, keine Weitergabe.",
            image: "lock.shield.fill",
            color: .red
        )
    ]
    
    var body: some View {
        VStack {
            TabView(selection: $currentPage) {
                ForEach(0..<pages.count, id: \.self) { index in
                    OnboardingPageView(page: pages[index])
                        .tag(index)
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .always))
            .indexViewStyle(PageIndexViewStyle(backgroundDisplayMode: .always))
            
            Spacer()
            
            HStack {
                if currentPage > 0 {
                    Button("Zurück") {
                        withAnimation {
                            currentPage -= 1
                        }
                    }
                    .buttonStyle(.bordered)
                }
                
                Spacer()
                
                Button(currentPage == pages.count - 1 ? "Los geht's" : "Weiter") {
                    if currentPage == pages.count - 1 {
                        completeOnboarding()
                    } else {
                        withAnimation {
                            currentPage += 1
                        }
                    }
                }
                .buttonStyle(.borderedProminent)
            }
            .padding()
            
            if currentPage < pages.count - 1 {
                Button("Überspringen") {
                    completeOnboarding()
                }
                .font(.caption)
                .foregroundColor(.secondary)
                .padding(.bottom)
            }
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
            
            Image(systemName: page.image)
                .font(.system(size: 100))
                .foregroundColor(page.color)
                .symbolRenderingMode(.hierarchical)
            
            VStack(spacing: 16) {
                Text(page.title)
                    .font(.title)
                    .fontWeight(.bold)
                    .multilineTextAlignment(.center)
                
                Text(page.description)
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 32)
            }
            
            Spacer()
            Spacer()
        }
        .padding()
    }
}

// Permission request view
struct PermissionsView: View {
    @Binding var isCompleted: Bool
    @State private var microphoneAuthorized = false
    @State private var speechAuthorized = false
    
    var body: some View {
        VStack(spacing: 24) {
            Text("Berechtigungen")
                .font(.title)
                .fontWeight(.bold)
            
            Text("OpenClaw benötigt folgende Berechtigungen, um zu funktionieren:")
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
                .padding(.horizontal)
            
            VStack(alignment: .leading, spacing: 16) {
                PermissionRow(
                    icon: "mic.fill",
                    title: "Mikrofon",
                    description: "Für Spracheingabe",
                    isGranted: $microphoneAuthorized
                )
                
                PermissionRow(
                    icon: "waveform",
                    title: "Spracherkennung",
                    description: "Zur Umwandlung von Sprache in Text",
                    isGranted: $speechAuthorized
                )
            }
            .padding()
            
            Spacer()
            
            Button("Berechtigungen erteilen") {
                requestPermissions()
            }
            .buttonStyle(.borderedProminent)
            .disabled(microphoneAuthorized && speechAuthorized)
            
            if microphoneAuthorized && speechAuthorized {
                Button("Weiter") {
                    isCompleted = true
                }
                .buttonStyle(.bordered)
            }
        }
        .padding()
    }
    
    private func requestPermissions() {
        // Request microphone permission
        AVAudioSession.sharedInstance().requestRecordPermission { granted in
            microphoneAuthorized = granted
        }
        
        // Request speech recognition permission
        SFSpeechRecognizer.requestAuthorization { status in
            speechAuthorized = (status == .authorized)
        }
    }
}

struct PermissionRow: View {
    let icon: String
    let title: String
    let description: String
    @Binding var isGranted: Bool
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(isGranted ? .green : .orange)
                .frame(width: 40)
            
            VStack(alignment: .leading) {
                Text(title)
                    .font(.headline)
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: isGranted ? "checkmark.circle.fill" : "circle")
                .foregroundColor(isGranted ? .green : .gray)
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
}
