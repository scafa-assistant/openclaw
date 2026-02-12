# 🔄 JARVIS LOOP
**Ralph Loop für OpenClaw - Autonome Entwicklung**

> Framework für kontrollierte, autonome KI-Entwicklung mit Safeguards

---

## 🎯 WAS IST JARVIS LOOP?

JARVIS Loop ist eine **Open-Source-Alternative zu Ralph Loop**, angepasst für OpenClaw:

- ✅ **Iterative Entwicklung** bis Projekt fertig
- ✅ **Safeguards** gegen Endlosschleifen
- ✅ **Multi-Agent** Orchestration (parallel)
- ✅ **Session Persistence** (überlebt Abstürze)
- ✅ **TUI** (Terminal UI) wie im Original
- ✅ **PDR Generator** durch interaktiven Dialog

---

## 📦 INSTALLATION

```bash
# 1. Repository klonen
cd ~/openclaw-backup/jarvis-loop

# 2. Dependencies installieren
pip install rich questionary

# 3. Ausführbar machen
chmod +x jarvis-loop.py
```

---

## 🚀 USAGE

### 1. Setup (wie im Ralph Loop Video [03:58])

```bash
python jarvis-loop.py setup
```

Fragt nach:
- Projekt Name
- Iteration-Limit (10/25/35/50)
- Budget ($5/$10/$25/$50)

### 2. PDR Erstellen (wie im Video [05:35])

```bash
python jarvis-loop.py pdr create
```

Interaktiver Dialog:
- Projekt-Ziel
- Plattform (Web/Mobile/Desktop)
- Umfang (MVP/Vollversion)
- Features (Auth, DB, API, UI, Tests...)

**Ergebnis:** `pdr.json` + automatisch generierte `tasks.json`

### 3. Loop Starten (wie im Video [09:24])

```bash
python jarvis-loop.py start
```

**TUI Controls:**
- `S` - Start/Pause
- `T` - Agent Traces anzeigen
- `H` - History/Log
- `Q` - Beenden (mit Speichern)

### 4. Status Check

```bash
python jarvis-loop.py status
```

Zeigt:
- Iteration: X/35
- Cost: $X.XX/$10.00
- Progress: XX%
- Tasks: done/in_progress/pending/failed

### 5. Fortsetzen nach Absturz

```bash
python jarvis-loop.py resume
```

Lädt letzte Session aus `session.jsonl`

---

## 📁 PROJEKTSTRUKTUR

```
my-project/
├── jarvis-loop.json          # Loop Config
├── pdr.json                   # Product Requirement Document
├── tasks.json                 # Task List (wichtig!)
├── session.jsonl             # Event Log (Persistence)
└── output/                   # Agent Outputs
```

---

## 🛡️ SAFEGUARDS

Wie im Ralph Loop Video [04:35]:

| Limit | Default | Aktion bei Erreichen |
|-------|---------|---------------------|
| Max Iterations | 35 | Auto-Pause |
| Max Cost | $10.00 | Auto-Pause |
| Max Time/Task | 30 min | Auto-Pause |
| Auto-Save | 30 sec | Session speichern |

**Config:** `config/default_config.json`

---

## 🎨 TUI ANSICHT

```
┌─────────────────────────────────────────────────────────┐
│  🎯 JARVIS LOOP v1.0 - Meine App                        │
├──────────────────────────┬──────────────────────────────┤
│  📋 TASKS (5/35)         │  🖥️  LIVE OUTPUT             │
│                          │                              │
│  ⏳ #1 Setup Projekt     │  [14:32:01] Agent-1: Start   │
│  ✅ #2 Install Deps      │  [14:32:05] npm install...   │
│  🔄 #3 Create Components │  [14:32:12] Building...      │
│  ⏳ #4 API Integration   │                              │
│  ⏳ #5 Testing           │  Cost: $2.34 / $10.00        │
│                          │  Iteration: 8 / 35           │
│  [S] Start  [T] Traces   │  Status: RUNNING             │
│  [P] Pause  [Q] Quit     │                              │
└──────────────────────────┴──────────────────────────────┘
```

---

## 🔧 ARCHITEKTUR

```
jarvis-loop/
├── core/
│   ├── task_manager.py       # JSON Task Management
│   ├── agent_orchestrator.py # Multi-Agent Coordination
│   └── safeguard.py          # Limits & Cost Control
├── ui/
│   └── jarvis_tui.py         # Terminal Interface
├── pdr/
│   └── pdr_generator.py      # Interactive PDR Dialog
├── config/
│   └── default_config.json   # Default Safeguards
└── jarvis-loop.py            # Main Entry Point
```

---

## 🔄 WORKFLOW

```mermaid
1. setup → Erstellt jarvis-loop.json
2. pdr create → Interaktiver Dialog → pdr.json
3. start → TUI öffnet
4. Agents arbeiten parallel
5. Auto-Save alle 30s
6. Safeguards prüfen Limits
7. Fertig/Auto-Pause
```

---

## 📝 BEISPIEL

### Projekt: "Todo App"

```bash
# Setup
python jarvis-loop.py setup
# → Name: Todo App
# → Limit: 25 Iterationen
# → Budget: $10

# PDR erstellen
python jarvis-loop.py pdr create
# → Plattform: Web
# → Umfang: MVP
# → Features: Auth, DB, UI

# Starten
python jarvis-loop.py start
# → TUI öffnet sich
# → Taste 'S'
# → Agents arbeiten...

# Nach 45 Minuten:
# ✅ Todo App fertig!
# ✅ Getestet & Deployed
```

---

## 🎓 RALPH LOOP vs JARVIS LOOP

| Feature | Ralph Loop | JARVIS Loop |
|---------|------------|-------------|
| **Basis** | Claude Code | OpenClaw |
| **TUI** | Ralph TUI | Jarvis TUI |
| **PDR** | ✅ Interaktiv | ✅ Interaktiv |
| **Multi-Agent** | ✅ | ✅ |
| **Safeguards** | ✅ | ✅ |
| **Persistence** | ✅ | ✅ |
| **Cost** | $$$ | $ (OpenClaw) |

---

## 🔗 RESSOURCEN

- **Ralph Loop Original:** GitHub (siehe Video)
- **OpenClaw Docs:** docs.openclaw.ai
- **Inspiration:** Video von Ralph Loop Demo

---

## ✅ TODO

- [x] Task Manager (JSON)
- [x] PDR Generator (interaktiv)
- [x] Agent Orchestrator (parallel)
- [x] Safeguards
- [x] TUI (Terminal UI)
- [x] Session Persistence
- [ ] OpenClaw Integration (sessions_spawn)
- [ ] Echte Agent-Ausführung
- [ ] Cost Tracking API

---

**Status:** ✅ MVP Fertig - Bereit für Tests
**Version:** 1.0.0
**Author:** JARVIS SWARM v3.2 (inspiriert von Ralph Loop)

*"Hartnäckig ahnungslos, aber unerbittlich weitermacht" - Ralph Wiggum*
