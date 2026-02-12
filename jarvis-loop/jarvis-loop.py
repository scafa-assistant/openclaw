#!/usr/bin/env python3
"""
JARVIS LOOP - Haupt-Einstiegspunkt
Ralph Loop für OpenClaw

Usage:
  jarvis-loop setup              # Projekt initialisieren
  jarvis-loop pdr create         # PDR erstellen (interaktiv)
  jarvis-loop start              # Loop starten
  jarvis-loop status             # Status anzeigen
  jarvis-loop resume             # Fortsetzen nach Absturz
"""

import sys
import os
import json
import argparse
from pathlib import Path

# Füge Core zum Path hinzu
sys.path.insert(0, str(Path(__file__).parent / "core"))
sys.path.insert(0, str(Path(__file__).parent / "pdr"))
sys.path.insert(0, str(Path(__file__).parent / "ui"))

from task_manager import TaskManager
from agent_orchestrator import AgentOrchestrator, SafeguardMonitor
from pdr_generator import PDRGenerator
from jarvis_tui import JarvisTUI


class JarvisLoop:
    """Hauptklasse für JARVIS Loop"""
    
    def __init__(self, project_path: str = "."):
        self.project_path = Path(project_path)
        self.config_file = self.project_path / "jarvis-loop.json"
        self.tasks_file = self.project_path / "tasks.json"
        self.tm = None
        self.orchestrator = None
        
    def setup(self) -> None:
        """
        Setup wie im Ralph Loop Video [03:58]
        Fragt nach:
        - Issue-Tracker (JSON)
        - Iteration-Limit
        """
        print("🚀 JARVIS LOOP Setup")
        print("=" * 50)
        
        # Projekt Name
        project_name = input("Projekt Name: ").strip() or "Mein Projekt"
        
        # Iteration Limit (wie im Video [04:35])
        print("\nWähle Safeguard-Limit:")
        print("[1] 10 Iterationen (klein)")
        print("[2] 25 Iterationen (mittel)")
        print("[3] 35 Iterationen (groß - empfohlen)")
        print("[4] 50 Iterationen (sehr groß)")
        
        choice = input("Auswahl (1-4) [3]: ").strip() or "3"
        limits = {"1": 10, "2": 25, "3": 35, "4": 50}
        iteration_limit = limits.get(choice, 35)
        
        # Budget
        budget = input("\nBudget in USD (default: 10): ").strip() or "10"
        
        # Speichern
        config = {
            "project": project_name,
            "iteration_limit": iteration_limit,
            "budget_usd": float(budget),
            "created_at": datetime.now().isoformat(),
            "status": "initialized"
        }
        
        self.project_path.mkdir(exist_ok=True)
        with open(self.config_file, 'w') as f:
            json.dump(config, f, indent=2)
        
        # Task Manager initialisieren
        self.tm = TaskManager(self.project_path)
        self.tm.create_project(project_name, "")
        
        print(f"\n✅ Setup abgeschlossen!")
        print(f"   Projekt: {project_name}")
        print(f"   Limit: {iteration_limit} Iterationen")
        print(f"   Budget: ${budget} USD")
        print(f"\nNächster Schritt: jarvis-loop pdr create")
    
    def create_pdr(self) -> None:
        """
        Erstellt PDR durch interaktiven Dialog
        Wie im Video [05:35]
        """
        print("🎯 PDR Generator")
        print("=" * 50)
        
        gen = PDRGenerator()
        result = gen.create_interactive()
        
        # Speichern
        pdr_file = self.project_path / "pdr.json"
        with open(pdr_file, 'w', encoding='utf-8') as f:
            json.dump(result, f, indent=2, ensure_ascii=False)
        
        # Tasks in Task Manager übernehmen
        self.tm = TaskManager(self.project_path)
        for task_data in result['tasks']:
            self.tm.add_task(
                title=task_data['title'],
                task_type=task_data.get('type', 'coding'),
                dependencies=task_data.get('dependencies', []),
                estimated_cost=0.50
            )
        
        print(f"\n💾 PDR gespeichert: {pdr_file}")
        print(f"   {len(result['tasks'])} Tasks erstellt")
        print(f"\nNächster Schritt: jarvis-loop start")
    
    def start(self) -> None:
        """
        Startet den Loop mit TUI
        Wie im Video [09:24] - Taste 'S'
        """
        if not self.tasks_file.exists():
            print("❌ Kein tasks.json gefunden!")
            print("   Führe zuerst aus: jarvis-loop pdr create")
            return
        
        self.tm = TaskManager(self.project_path)
        
        print("🎬 Starte JARVIS Loop...")
        print("   Taste 'S' zum Starten")
        print("   Taste 'Q' zum Beenden")
        print()
        
        # TUI starten
        tui = JarvisTUI(self.project_path)
        
        try:
            tui.run(self.tm)
        except KeyboardInterrupt:
            print("\n\n👋 Loop beendet.")
            print("   Um fortzufahren: jarvis-loop resume")
    
    def status(self) -> None:
        """Zeigt aktuellen Status"""
        if not self.tasks_file.exists():
            print("❌ Kein Projekt gefunden!")
            return
        
        self.tm = TaskManager(self.project_path)
        status = self.tm.get_status()
        
        print("📊 JARVIS Loop Status")
        print("=" * 50)
        print(f"Projekt: {status['project']['name']}")
        print(f"Iteration: {status['iteration']['current']}/{status['iteration']['limit']}")
        print(f"Kosten: ${status['iteration']['cost_usd']:.2f}")
        print(f"Fortschritt: {status['progress_percent']:.1f}%")
        print()
        print(f"Tasks: {status['tasks']['done']} done, "
              f"{status['tasks']['in_progress']} in progress, "
              f"{status['tasks']['pending']} pending, "
              f"{status['tasks']['failed']} failed")
    
    def resume(self) -> None:
        """Setzt nach Absturz fort (Session Persistence)"""
        print("🔄 Setze Session fort...")
        
        if not self.tasks_file.exists():
            print("❌ Keine Session gefunden!")
            return
        
        # Lade letzten State
        self.tm = TaskManager(self.project_path)
        
        # Zeige was passiert ist
        self.status()
        
        print("\n✅ Session wiederhergestellt!")
        print("   Starte mit: jarvis-loop start")


def main():
    parser = argparse.ArgumentParser(
        description="JARVIS Loop - Autonomous Development Framework"
    )
    parser.add_argument(
        "command",
        choices=["setup", "pdr", "start", "status", "resume", "help"],
        help="Auszuführender Befehl"
    )
    parser.add_argument(
        "--project", "-p",
        default=".",
        help="Projekt-Pfad (default: current directory)"
    )
    parser.add_argument(
        "--pdr-action",
        choices=["create"],
        help="PDR Aktion (nur mit 'pdr' command)"
    )
    
    args = parser.parse_args()
    
    # Workaround für subcommand
    if args.command == "pdr":
        if not args.pdr_action:
            args.pdr_action = "create"
    
    loop = JarvisLoop(args.project)
    
    if args.command == "setup":
        loop.setup()
    elif args.command == "pdr":
        if args.pdr_action == "create":
            loop.create_pdr()
    elif args.command == "start":
        loop.start()
    elif args.command == "status":
        loop.status()
    elif args.command == "resume":
        loop.resume()
    elif args.command == "help":
        print(__doc__)


if __name__ == "__main__":
    from datetime import datetime
    main()
