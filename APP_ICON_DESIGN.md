# OpenClaw App Icon Design

## Brand-Integration (ungehört.)

### Farbpalette:
```
Primary:    #d4af37 (Gelbgold) - ungehört. Brand Color
Secondary:  #00D4AA (Türkis) - Tech/AI
Background: #0D0D0D (Tiefschwarz) - Premium/Dark
Accent:     #1A1A2E (Dunkelblau) - Tiefe
```

### Design-Konzept:

**Name: "The Claw Wave"**

Kombination aus:
1. **Kralle/Klaue** → ungehört. Musik-Brand
2. **Wellenform** → Voice/AI Assistant  
3. **Goldener Ring** → Premium Qualität
4. **Türkiser Punkt** → Aktiv/Online Status

### Elemente:

#### Äußerer Ring (Gold)
- Symbolisiert: Premium, ungehört. Brand
- Gradient von #d4af37 zu #b8941d
- Leichter Glow-Effekt

#### Innere Form (Kralle + Welle)
- Drei spitzen Formen = Kralle/Klaue
- Fließende Wellen = Voice/Audio
- Dunkler Hintergrund für Kontrast

#### Status-Indikator (Türkis)
- Kleiner Punkt unten
- Zeigt: AI aktiv/bereit
- Pulsiert bei Spracheingabe

### Varianten:

#### Light Mode (f Notifications):
- Weißer Hintergrund
- Goldene Kralle
- Türkis Akzent

#### Dark Mode (Standard):
- Schwarzer Hintergrund
- Goldene Kralle
- Türkis Akzent

#### Adaptive Icon (Android):
- Separate Vordergrund/Ebene
- Parallax-Effekt
- System-konform

### Umsetzung:

1. **Vector Asset** erstellen (Android Studio)
2. **iOS App Icon** (1024x1024 PNG)
3. **Adaptive Icon** (Android)
4. **Favicon** (Web)

### Dateien:

```
android/
├── res/drawable/ic_launcher_foreground_branded.xml
├── res/drawable/ic_launcher_background_branded.xml
├── res/mipmap-xxxhdpi/
│   ├── ic_launcher_foreground.png
│   └── ic_launcher_background.png

ios/
├── Assets.xcassets/AppIcon.appiconset/
│   ├── AppIcon-20@2x.png
│   ├── ...
│   └── AppIcon-1024.png

branding/
├── openclaw_icon_gold.svg
├── openclaw_icon_white.svg
└── openclaw_icon_symbol.svg
```

---

**Next:** Icons als PNG exportieren für iOS/Android.
