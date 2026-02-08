// WidgetKit Extension for iOS
import WidgetKit
import SwiftUI

struct OpenClawWidgetEntry: TimelineEntry {
    let date: Date
    let lastMessage: String?
    let quickActions: [String]
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> OpenClawWidgetEntry {
        OpenClawWidgetEntry(
            date: Date(),
            lastMessage: "Hallo! Wie kann ich helfen?",
            quickActions: ["Wetter", "Erinnerung", "Notiz"]
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (OpenClawWidgetEntry) -> Void) {
        let entry = OpenClawWidgetEntry(
            date: Date(),
            lastMessage: "Hallo! Wie kann ich helfen?",
            quickActions: ["Wetter", "Erinnerung", "Notiz"]
        )
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<OpenClawWidgetEntry>) -> Void) {
        let entries = [
            OpenClawWidgetEntry(
                date: Date(),
                lastMessage: UserDefaults.standard.string(forKey: "widget_last_message"),
                quickActions: ["Wetter", "Erinnerung", "Timer"]
            )
        ]
        
        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
}

struct OpenClawWidgetEntryView: View {
    var entry: Provider.Entry
    @Environment(\.widgetFamily) var family

    var body: some View {
        switch family {
        case .systemSmall:
            SmallWidgetView(entry: entry)
        case .systemMedium:
            MediumWidgetView(entry: entry)
        case .systemLarge:
            LargeWidgetView(entry: entry)
        default:
            SmallWidgetView(entry: entry)
        }
    }
}

struct SmallWidgetView: View {
    let entry: OpenClawWidgetEntry
    
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "waveform.circle.fill")
                .font(.system(size: 40))
                .foregroundColor(.accentColor)
            
            Text("OpenClaw")
                .font(.caption)
                .fontWeight(.semibold)
            
            Button(intent: OpenVoiceIntent()) {
                Text("Sprechen")
                    .font(.caption2)
            }
        }
        .padding()
    }
}

struct MediumWidgetView: View {
    let entry: OpenClawWidgetEntry
    
    var body: some View {
        HStack(spacing: 16) {
            VStack {
                Image(systemName: "waveform.circle.fill")
                    .font(.system(size: 50))
                    .foregroundColor(.accentColor)
                
                Button(intent: OpenVoiceIntent()) {
                    Label("Sprechen", systemImage: "mic.fill")
                }
                .buttonStyle(.bordered)
                .controlSize(.small)
            }
            
            VStack(alignment: .leading, spacing: 8) {
                Text("Schnellaktionen")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                ForEach(entry.quickActions.prefix(3), id: \.self) { action in
                    Button(intent: QuickActionIntent(action: action)) {
                        Text(action)
                            .font(.caption)
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.mini)
                }
            }
        }
        .padding()
    }
}

struct LargeWidgetView: View {
    let entry: OpenClawWidgetEntry
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "waveform.circle.fill")
                    .font(.system(size: 30))
                    .foregroundColor(.accentColor)
                
                Text("OpenClaw")
                    .font(.headline)
                
                Spacer()
                
                Button(intent: OpenVoiceIntent()) {
                    Image(systemName: "mic.fill")
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.small)
            }
            
            if let lastMessage = entry.lastMessage {
                Text(lastMessage)
                    .font(.caption)
                    .lineLimit(3)
                    .padding(8)
                    .background(Color.gray.opacity(0.2))
                    .cornerRadius(8)
            }
            
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                ForEach(entry.quickActions, id: \.self) { action in
                    Button(intent: QuickActionIntent(action: action)) {
                        Label(action, systemImage: iconForAction(action))
                            .font(.caption)
                    }
                    .buttonStyle(.bordered)
                }
            }
        }
        .padding()
    }
    
    func iconForAction(_ action: String) -> String {
        switch action {
        case "Wetter": return "cloud.sun.fill"
        case "Erinnerung": return "bell.fill"
        case "Timer": return "timer"
        case "Notiz": return "note.text"
        default: return "bolt.fill"
        }
    }
}

// App Intents for Widget
struct OpenVoiceIntent: AppIntent {
    static var title: LocalizedStringResource = "OpenClaw Sprachsteuerung"
    
    func perform() async throws -> some IntentResult {
        // Open app with voice active
        await MainActor.run {
            NotificationCenter.default.post(name: .init("openVoice"), object: nil)
        }
        return .result()
    }
}

struct QuickActionIntent: AppIntent {
    static var title: LocalizedStringResource = "Schnellaktion"
    
    @Parameter(title: "Aktion")
    var action: String
    
    init() {}
    
    init(action: String) {
        self.action = action
    }
    
    func perform() async throws -> some IntentResult {
        await MainActor.run {
            NotificationCenter.default.post(
                name: .init("quickAction"),
                object: action
            )
        }
        return .result()
    }
}

@main
struct OpenClawWidget: Widget {
    let kind: String = "OpenClawWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            OpenClawWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("OpenClaw")
        .description("Schnellzugriff auf deinen AI-Assistenten")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}
