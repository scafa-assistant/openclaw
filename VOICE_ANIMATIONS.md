# 🎨 Voice Interface Animationen

## Design-Spezifikation

### Zustände & Animationen

```
┌─────────────────────────────────────────────┐
│  STATE: IDLE (Wartend)                      │
│  ┌─────────┐                                │
│  │    ◉    │  ← Subtil pulsiert (1.5s)     │
│  │  ╭───╮  │                                │
│  │  │ ◉ │  │  Farbe: Weiß/Grau             │
│  │  ╰───╯  │                                │
│  └─────────┘                                │
│  Touch → LISTENING                          │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  STATE: LISTENING (Hört zu)                 │
│  ┌─────────┐                                │
│  │ ╭─────╮ │  ← Aktive Wellen-Animation    │
│  │ │◉◉◉◉◉│ │     (Amplitude = Lautstärke)  │
│  │ │◉◉◉◉◉│ │                                │
│  │ ╰─────╯ │  Farbe: #00D4AA (Türkis)      │
│  └─────────┘     Pulsierend, dynamisch      │
│  Sprache → PROCESSING                       │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  STATE: PROCESSING (Denkt)                  │
│  ┌─────────┐                                │
│  │  ⟳ ⟳ ⟳  │  ← Spinning Dots              │
│  │  ⟳ ◉ ⟳  │     (Rotiert im Kreis)        │
│  │  ⟳ ⟳ ⟳  │                                │
│  └─────────┘  Farbe: #d4af37 (Gold)         │
│  Response → SPEAKING                        │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  STATE: SPEAKING (Antwortet)                │
│  ┌─────────┐                                │
│  │ ▓▓▓▓▓▓▓ │  ← Sound Wave Ausgabe          │
│  │ ▓▓▓▓▓▓▓ │     (Frequenz visualisiert)   │
│  │ ▓▓▓▓▓▓▓ │                                │
│  └─────────┘  Farbe: Weiß/Lila Gradient     │
│  Fertig → IDLE                              │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  STATE: ERROR (Fehler)                      │
│  ┌─────────┐                                │
│  │   ⚠️    │  ← Roter Puls                  │
│  │ ╭─────╮ │     (Schnelles Blinken)       │
│  │ │  ✕  │ │                                │
│  │ ╰─────╯ │  Farbe: Rot (#FF4444)         │
│  └─────────┘  Retry Button erscheint        │
└─────────────────────────────────────────────┘
```

## Technische Umsetzung

### Android (Jetpack Compose)

```kotlin
@Composable
fun VoiceAnimation(
    state: VoiceState,
    amplitude: Float = 0.5f
) {
    val transition = updateTransition(state, label = "voice")
    
    // Animated properties based on state
    val scale by transition.animateFloat { s ->
        when(s) {
            VoiceState.IDLE -> 1f
            VoiceState.LISTENING -> 1f + amplitude * 0.3f
            VoiceState.PROCESSING -> 1f
            VoiceState.SPEAKING -> 1f + amplitude * 0.2f
            VoiceState.ERROR -> 1.1f
        }
    }
    
    val color by transition.animateColor { s ->
        when(s) {
            VoiceState.IDLE -> Color.Gray
            VoiceState.LISTENING -> Color(0xFF00D4AA)
            VoiceState.PROCESSING -> Color(0xFFd4af37)
            VoiceState.SPEAKING -> Color.White
            VoiceState.ERROR -> Color(0xFFFF4444)
        }
    }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .background(color.copy(alpha = 0.2f), CircleShape)
    ) {
        // State-specific content
        when(state) {
            VoiceState.IDLE -> IdleIcon()
            VoiceState.LISTENING -> WaveformAnimation(amplitude)
            VoiceState.PROCESSING -> SpinningDots()
            VoiceState.SPEAKING -> SoundWaveBars(amplitude)
            VoiceState.ERROR -> ErrorIcon()
        }
    }
}
```

### iOS (SwiftUI)

```swift
struct VoiceAnimation: View {
    let state: VoiceState
    let amplitude: CGFloat
    
    @State private var isAnimating = false
    
    var body: some View {
        ZStack {
            // Background circle
            Circle()
                .fill(backgroundColor.opacity(0.2))
                .frame(width: 80, height: 80)
            
            // State content
            switch state {
            case .idle:
                Image(systemName: "mic.fill")
                    .font(.system(size: 40))
                    .foregroundColor(.gray)
                    .opacity(isAnimating ? 0.5 : 1)
                    .animation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true), value: isAnimating)
            
            case .listening:
                WaveformView(amplitude: amplitude)
                    .foregroundColor(.accentColor)
            
            case .processing:
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .yellow))
                    .scaleEffect(1.5)
            
            case .speaking:
                SoundWaveView(amplitude: amplitude)
                    .foregroundColor(.white)
            
            case .error:
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 40))
                    .foregroundColor(.red)
                    .opacity(isAnimating ? 1 : 0.3)
                    .animation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true), value: isAnimating)
            }
        }
        .onAppear { isAnimating = true }
    }
    
    var backgroundColor: Color {
        switch state {
        case .idle: return .gray
        case .listening: return .accentColor
        case .processing: return .yellow
        case .speaking: return .purple
        case .error: return .red
        }
    }
}
```

## Timing & Dauer

| State | Durchschnitt | Max | Animation |
|-------|--------------|-----|-----------|
| IDLE | ∞ | ∞ | Subtiler Puls (1.5s) |
| LISTENING | 2-5s | 30s | Amplitude-basiert |
| PROCESSING | 1-3s | 10s | Endloses Spinning |
| SPEAKING | 2-10s | 60s | Frequenz-basiert |
| ERROR | 3s | ∞ | Schnelles Blinken |

## Farbschema

```yaml
Voice States:
  IDLE:
    primary: "#808080"      # Grau
    background: "#404040"   # Dunkelgrau
    
  LISTENING:
    primary: "#00D4AA"      # Türkis
    background: "#00D4AA20" # Türkis transparent
    
  PROCESSING:
    primary: "#d4af37"      # Gold (ungehört.)
    background: "#d4af3720" # Gold transparent
    
  SPEAKING:
    primary: "#FFFFFF"      # Weiß
    background: "#9C27B020" # Lila transparent
    
  ERROR:
    primary: "#FF4444"      # Rot
    background: "#FF444420" # Rot transparent
```

## Haptik (Vibration)

```kotlin
// Android
when(state) {
    VoiceState.LISTENING -> vibrator.vibrate(VibrationEffect.createOneShot(50, 100))
    VoiceState.SPEAKING -> vibrator.vibrate(VibrationEffect.createOneShot(30, 50))
    VoiceState.ERROR -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
}

// iOS
let generator = UINotificationFeedbackGenerator()
generator.notificationOccurred(.success) // or .error
```

## Implementierungs-Checkliste

- [ ] VoiceState Enum erstellen
- [ ] Grundlayout (Circle + Icon)
- [ ] IDLE Animation (Puls)
- [ ] LISTENING Animation (Waveform)
- [ ] PROCESSING Animation (Spinning)
- [ ] SPEAKING Animation (Bars)
- [ ] ERROR Animation (Blink + Retry)
- [ ] Haptik hinzufügen
- [ ] State-Transitions smooth machen
- [ ] Amplitude aus Audio-Input

---

**Status:** Spezifikation fertig. Implementierung wartet auf Priorisierung.
