import SwiftUI

struct PowerUserSettingsView: View {
    @State private var config = UserConfig()
    @State private var selectedModel = "auto"
    @State private var showApiKeys = false
    var onBack: () -> Void
    var onSave: (UserConfig) -> Void
    
    var body: some View {
        NavigationView {
            Form {
                // Info Section
                Section {
                    HStack {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(.accentColor)
                        Text("Standardmäßig wählt OpenClaw automatisch das beste Modell. Für fortgeschrittene Features kannst du eigene API-Keys hinzufügen.")
                            .font(.caption)
                    }
                }
                
                // Default Models
                Section("KI-Modell") {
                    ForEach(DefaultModel.all) { model in
                        ModelRow(
                            title: model.name,
                            description: model.description,
                            selected: selectedModel == model.id
                        ) {
                            selectedModel = model.id
                            config.preferredModel = nil
                            showApiKeys = false
                        }
                    }
                }
                
                // Power Models
                Section("Power-User Modelle") {
                    Text("Für komplexere Aufgaben mit eigenen API-Keys:")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    ForEach(PowerModel.all) { model in
                        let hasKey = hasKeyFor(model.provider)
                        PowerModelRow(
                            model: model,
                            hasKey: hasKey,
                            selected: selectedModel == model.id && hasKey
                        ) {
                            if hasKey {
                                selectedModel = model.id
                                config.preferredModel = model.id
                            } else {
                                showApiKeys = true
                            }
                        }
                    }
                }
                
                // API Keys Section
                if showApiKeys {
                    Section("API-Keys") {
                        Text("Deine Keys werden verschlüsselt gespeichert.")
                            .font(.caption)
                        
                        SecureField("OpenAI API Key", text: binding(for: \.openai))
                        SecureField("Anthropic API Key", text: binding(for: \.anthropic))
                        SecureField("Google AI Key", text: binding(for: \.google))
                        SecureField("Moonshot API Key", text: binding(for: \.moonshot))
                        SecureField("DeepSeek API Key", text: binding(for: \.deepseek))
                        SecureField("Mistral API Key", text: binding(for: \.mistral))
                    }
                }
                
                // Voice Settings
                Section("Spracheingabe") {
                    Toggle("Aktiviert", isOn: $config.voiceEnabled)
                }
                
                // Appearance
                Section("Erscheinungsbild") {
                    Picker("Theme", selection: Binding(
                        get: { 
                            if config.darkMode == nil { return 0 }
                            return config.darkMode == true ? 1 : 2
                        },
                        set: { newValue in
                            switch newValue {
                            case 0: config.darkMode = nil
                            case 1: config.darkMode = true
                            case 2: config.darkMode = false
                            default: break
                            }
                        }
                    )) {
                        Text("System").tag(0)
                        Text("Dunkel").tag(1)
                        Text("Hell").tag(2)
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Erweitert")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Zurück") { onBack() }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Speichern") { onSave(config) }
                }
            }
        }
    }
    
    private func hasKeyFor(_ provider: String) -> Bool {
        guard let keys = config.apiKeys else { return false }
        switch provider {
        case "openai": return keys.openai != nil
        case "anthropic": return keys.anthropic != nil
        case "google": return keys.google != nil
        case "moonshot": return keys.moonshot != nil
        case "deepseek": return keys.deepseek != nil
        case "mistral": return keys.mistral != nil
        default: return false
        }
    }
    
    private func binding(for keyPath: WritableKeyPath<APIKeys, String?>) -> Binding<String> {
        Binding(
            get: {
                if config.apiKeys == nil {
                    config.apiKeys = APIKeys()
                }
                return config.apiKeys![keyPath: keyPath] ?? ""
            },
            set: { newValue in
                if config.apiKeys == nil {
                    config.apiKeys = APIKeys()
                }
                config.apiKeys![keyPath: keyPath] = newValue.isEmpty ? nil : newValue
            }
        )
    }
}

struct ModelRow: View {
    let title: String
    let description: String
    let selected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.body)
                    Text(description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                if selected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.accentColor)
                }
            }
        }
        .foregroundColor(.primary)
    }
}

struct PowerModelRow: View {
    let model: PowerModel
    let hasKey: Bool
    let selected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(model.name)
                        .font(.body)
                    Text(model.description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    if !hasKey {
                        Text("API-Key erforderlich →")
                            .font(.caption)
                            .foregroundColor(.accentColor)
                    }
                }
                Spacer()
                if selected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.accentColor)
                } else if hasKey {
                    Image(systemName: "lock.fill")
                        .foregroundColor(.secondary)
                } else {
                    Image(systemName: "plus.circle")
                        .foregroundColor(.gray)
                }
            }
        }
        .foregroundColor(hasKey ? .primary : .gray)
        .opacity(hasKey ? 1.0 : 0.7)
    }
}