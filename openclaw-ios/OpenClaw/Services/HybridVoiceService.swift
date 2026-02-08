import SwiftUI
import Speech
import AVFoundation

/**
 * Hybrid Voice Service für iOS
 * Nutzt Siri für einfache Anfragen (kostenlos)
 * OpenClaw Backend nur für komplexe Anfragen
 */
class HybridVoiceService: ObservableObject {
    @Published var voiceState: VoiceState = .idle
    
    private var speechRecognizer: SFSpeechRecognizer?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()
    private let synthesizer = AVSpeechSynthesizer()
    
    enum VoiceState {
        case idle, listening, processing, speaking, error
    }
    
    enum ResultSource {
        case builtinSiri    // Kostenlos
        case externalAI     // Bezahlt
    }
    
    struct HybridResult {
        let source: ResultSource
        let content: String?
        let siriIntent: SiriIntent?
        let estimatedCost: String
    }
    
    struct SiriIntent {
        let intent: String
        let parameters: [String: Any]
    }
    
    init() {
        speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "de-DE"))
        requestAuthorization()
    }
    
    private func requestAuthorization() {
        SFSpeechRecognizer.requestAuthorization { _ in }
        AVAudioSession.sharedInstance().requestRecordPermission { _ in }
    }
    
    /**
     * Startet Spracheingabe und entscheidet: Siri oder OpenClaw
     */
    func startVoiceInput(completion: @escaping (HybridResult) -> Void) {
        voiceState = .listening
        
        // Starte Spracherkennung
        startRecording { [weak self] transcript in
            self?.voiceState = .processing
            
            // Entscheide: Siri oder externe AI
            if self?.isBuiltInQuery(transcript) == true {
                // Nutze Siri (KOSTENLOS)
                self?.delegateToSiri(query: transcript)
                completion(HybridResult(
                    source: .builtinSiri,
                    content: nil,
                    siriIntent: SiriIntent(intent: "custom", parameters: ["query": transcript]),
                    estimatedCost: "Kostenlos"
                ))
            } else {
                // Sende an OpenClaw Backend
                completion(HybridResult(
                    source: .externalAI,
                    content: nil,
                    siriIntent: nil,
                    estimatedCost: "~$0.003"
                ))
            }
        }
    }
    
    /**
     * Prüft ob Siri die Anfrage beantworten kann
     */
    private func isBuiltInQuery(_ query: String) -> Bool {
        let lower = query.lowercased()
        
        let siriPatterns = [
            /^(wie spät|uhrzeit|wie viel uhr)/,
            /^(welcher tag|datum|wann ist heute)/,
            /^(wetter|temperatur|regnet es)/,
            /^(timer|wecker|erinner mich|termin)/,
            /^(navigiere|weg nach|route zu)/,
            /^(wer ist|was ist|wo liegt)/,
            /^(rechne|wie viel ist \d)/,
            /^(licht|lampe|musik|lautstärke)/,
            /^(ruf|anrufen|nachricht|sms)/
        ]
        
        return siriPatterns.contains { lower.range(of: $0, options: .regularExpression) != nil }
    }
    
    /**
     * Delegiert an Siri (kostenlos)
     */
    private func delegateToSiri(query: String) {
        // Siri wird die Anfrage nativ beantworten
        // Keine externe API nötig
        voiceState = .idle
    }
    
    /**
     * Lokale Spracherkennung
     */
    private func startRecording(completion: @escaping (String) -> Void) {
        // Implementation der Speech Recognition
        // ...
    }
    
    /**
     * Text-to-Speech
     */
    func speak(_ text: String) {
        voiceState = .speaking
        
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: "de-DE")
        utterance.rate = 0.5
        
        synthesizer.speak(utterance)
    }
    
    func stop() {
        audioEngine.stop()
        recognitionRequest?.endAudio()
        voiceState = .idle
    }
}

/**
 * SwiftUI View für Voice Interface mit Kostenschätzung
 */
struct HybridVoiceButton: View {
    @StateObject private var voiceService = HybridVoiceService()
    @State private var showCostInfo = false
    
    var body: some View {
        VStack {
            Button(action: {
                voiceService.startVoiceInput { result in
                    handleResult(result)
                }
            }) {
                Image(systemName: voiceIcon)
                    .font(.system(size: 30))
                    .foregroundColor(voiceColor)
                    .frame(width: 70, height: 70)
                    .background(voiceColor.opacity(0.2))
                    .clipShape(Circle())
                    .scaleEffect(voiceService.voiceState == .listening ? 1.2 : 1.0)
                    .animation(.easeInOut(duration: 0.3), value: voiceService.voiceState)
            }
            
            if showCostInfo {
                Text("Kostenlos über Siri")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
    }
    
    private var voiceIcon: String {
        switch voiceService.voiceState {
        case .idle: return "mic.fill"
        case .listening: return "waveform"
        case .processing: return "brain"
        case .speaking: return "speaker.wave.2.fill"
        case .error: return "exclamationmark.triangle.fill"
        }
    }
    
    private var voiceColor: Color {
        switch voiceService.voiceState {
        case .idle: return .gray
        case .listening: return .accentColor
        case .processing: return .orange
        case .speaking: return .green
        case .error: return .red
        }
    }
    
    private func handleResult(_ result: HybridVoiceService.HybridResult) {
        showCostInfo = (result.source == .builtinSiri)
        
        // Verberge Cost Info nach 3 Sekunden
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            showCostInfo = false
        }
    }
}
