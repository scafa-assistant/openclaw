# JARVIS LOOP - Autonomous Development Framework
## Für OpenClaw - Inspiriert von Ralph Loop

---

## 🎯 KONZEPT

**JARVIS Loop** ist ein Framework für kontrollierte, autonome Entwicklung mit KI-Agenten:
- Iterative Verbesserung bis Projekt fertig
- Schutz gegen Endlosschleifen
- Kostenkontrolle (API-Limits)
- Session Persistence (überlebt Abstürze)
- Multi-Agent Orchestration

---

## 📦 ARCHITEKTUR

```
jarvis-loop/
├── core/
│   ├── task_manager.py        # Task List Management (JSON)
│   ├── agent_orchestrator.py   # Multi-Agent Coordination
│   ├── session_persistence.py  # State Saving/Recovery
│   └── safeguard.py           # Limits & Cost Control
├── ui/
│   └── jarvis_tui.py          # Terminal Interface
├── pdr/
│   └── pdr_generator.py       # Product Requirement Document
└── config/
    └── loop_config.json       # Safeguards & Settings
```

---

## 🚀 FEATURES (aus dem Video übernommen)

### 1. Task Manager (JSON-basiert)
- Wie im Video: Option 1 (JSON Datei)
- Tasks mit Status: pending, in_progress, done, failed
- Automatische Task-Generierung aus PDR

### 2. Terminal UI (Ralph TUI Style)
- **Links:** Task List (wie im Video [01:32])
- **Rechts:** Live Log/Output
- Steuerung: Pause, Resume, Stop, Inspect History
- Keyboard Shortcuts (S=Start, T=Traces, Q=Quit)

### 3. PDR Generator (Product Requirement Document)
- Interaktiver Dialog (wie im Video [05:35])
- Multiple-Choice Fragen:
  - Plattform (Web/Mobile/Desktop)
  - Umfang (MVP vs Full)
  - Features
- Automatische Task-Generierung aus PDR

### 4. Safeguards
- Iteration-Limit (z.B. 35 wie im Video [04:35])
- API-Cost-Tracking
- Time-Limit pro Task
- Auto-Pause bei Fehlern

### 5. Multi-Agent Support
- Mehrere Agents parallel (wie im Video [10:02])
- Task-Verteilung an spezialisierte Agents
- Agent Traces sichtbar (Taste 'T')

### 6. Session Persistence
- Alle 30 Sekunden: State speichern
- Recovery nach Absturz möglich
- JSONL Log aller Aktionen

---

## 🛠️ IMPLEMENTIERUNG

### Phase 1: Core (JSON Task Manager)
```python
# tasks.json Format (wie Ralph Loop)
{
  "project": "OpenClaw App",
  "iteration_limit": 35,
  "current_iteration": 0,
  "tasks": [
    {
      "id": 1,
      "title": "Setup Projektstruktur",
      "status": "pending",
      "assigned_agent": null,
      "estimated_cost": 0.50,
      "dependencies": []
    }
  ],
  "safeguards": {
    "max_iterations": 35,
    "max_cost_usd": 10.00,
    "auto_pause_on_error": true
  }
}
```

### Phase 2: PDR Generator
```python
# Interaktiver Dialog
Fragen = [
  "Was ist das Ziel des Projekts?",
  "Plattform? [1] Web [2] Mobile [3] Desktop",
  "Umfang? [1] MVP [2] Vollversion",
  "Budget/Iterationen-Limit?"
]
→ Generiert tasks.json
```

### Phase 3: Agent Orchestrator
```python
# Parallele Agent-Ausführung
for task in ready_tasks:
    agent = select_agent(task.type)  # coding-agent, research-agent, etc.
    spawn_subagent(task, agent)
```

### Phase 4: TUI (Terminal UI)
```python
# Mit 'rich' oder 'textual' library
# Links: Task Tree
# Rechts: Live Logs
# Unten: Status Bar mit Iteration/Cost
```

---

## 📋 USAGE

### 1. Setup (wie im Video [03:58])
```bash
jarvis-loop setup
# → Fragt nach Issue-Tracker (JSON)
# → Fragt nach Iteration-Limit
# → Erstellt jarvis-loop.json
```

### 2. PDR Erstellen (wie im Video [05:35])
```bash
jarvis-loop pdr create
# → Interaktiver Dialog
# → Generiert tasks.json
```

### 3. Loop Starten (wie im Video [09:24])
```bash
jarvis-loop start
# → TUI öffnet sich
# → Taste 'S' zum Starten
# → Agents arbeiten autonom
```

### 4. Steuerung (wie im Video)
- `S` - Start/Pause
- `T` - Agent Traces anzeigen
- `Q` - Beenden (mit Speichern)
- `H` - History/Log

---

## 🔒 SAFEGUARDS (Wichtig!)

```json
{
  "safeguards": {
    "max_iterations": 35,
    "max_cost_per_iteration_usd": 0.50,
    "max_total_cost_usd": 10.00,
    "max_time_per_task_min": 30,
    "auto_pause_on": [
      "api_error",
      "cost_limit_reached",
      "infinite_loop_detected"
    ]
  }
}
```

---

## 🎨 TUI MOCKUP

```
┌─────────────────────────────────────────────────────────┐
│  JARVIS LOOP v1.0 - OpenClaw Autonomous Development    │
├──────────────────────────┬──────────────────────────────┤
│  📋 TASKS (12/35)        │  🖥️  LIVE OUTPUT             │
│                          │                              │
│  ⏳ Setup Project        │  [14:32:01] Agent-1: Start   │
│  ✅ Install Deps         │  [14:32:05] Cloning repo...  │
│  🔄 Create Components    │  [14:32:12] npm install...   │
│  ⏳ API Integration      │  [14:32:15] Building...      │
│  ⏳ Testing              │                              │
│                          │  Cost: $2.34 / $10.00        │
│  [S] Start  [T] Traces   │  Iteration: 8 / 35           │
│  [P] Pause  [Q] Quit     │  Status: RUNNING             │
└──────────────────────────┴──────────────────────────────┘
```

---

## 🔗 INTEGRATION MIT OPENCLAW

### Als Skill verpacken:
```
skills/jarvis-loop/
├── SKILL.md
├── scripts/
│   ├── jarvis_loop.py      # Hauptskript
│   ├── task_manager.py
│   ├── tui.py
│   └── pdr_generator.py
└── config/
    └── default_config.json
```

### Verwendung:
```bash
# Als OpenClaw Skill
openclaw skill add jarvis-loop

# Dann nutzen:
jarvis-loop setup
jarvis-loop pdr create
jarvis-loop start
```

---

## ✅ NÄCHSTE SCHRITTE

1. **Core implementieren** (Task Manager + JSON)
2. **PDR Generator** bauen (interaktiver Dialog)
3. **TUI** mit rich/textual
4. **Agent Orchestrator** (Multi-Agent Support)
5. **Safeguards** einbauen
6. **Testen** mit kleinem Projekt

---

**Status:** 📋 Konzept fertig - Bereit für Implementierung
**Ziel:** Ralph Loop für OpenClaw nachbauen
**Est. Time:** 2-3 Stunden für MVP

*Inspiriert von Ralph Loop (GitHub) - für OpenClaw adaptiert*
