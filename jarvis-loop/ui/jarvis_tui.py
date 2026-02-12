#!/usr/bin/env python3
"""
JARVIS Loop - Terminal UI
Ralph TUI Style Interface für OpenClaw
Links: Tasks | Rechts: Live Log | Unten: Status
"""

import sys
import time
import json
from datetime import datetime
from typing import Dict, List

# Versuche rich zu importieren, sonst simple Alternative
try:
    from rich.console import Console
    from rich.layout import Layout
    from rich.panel import Panel
    from rich.table import Table
    from rich.live import Live
    from rich.text import Text
    from rich.progress import Progress, BarColumn, TextColumn
    RICH_AVAILABLE = True
except ImportError:
    RICH_AVAILABLE = False
    print("⚠️  'rich' nicht installiert. Nutze simples UI.")
    print("Installieren: pip install rich")


class JarvisTUI:
    """
    Ralph TUI Style Interface
    - Links: Task List
    - Rechts: Live Output
    - Unten: Status Bar
    """
    
    def __init__(self, project_path: str):
        self.project_path = project_path
        self.console = Console() if RICH_AVAILABLE else None
        self.running = True
        self.show_traces = False
        self.current_log = []
        
    def create_layout(self) -> Layout:
        """Erstellt Layout wie im Ralph Loop Video"""
        layout = Layout()
        
        # Header
        layout.split_column(
            Layout(name="header", size=3),
            Layout(name="main"),
            Layout(name="footer", size=3)
        )
        
        # Main: Links Tasks, Rechts Log
        layout["main"].split_row(
            Layout(name="tasks", ratio=1),
            Layout(name="log", ratio=1)
        )
        
        return layout
    
    def render_header(self, status: Dict) -> Panel:
        """Obere Status-Leiste"""
        title = f"🎯 JARVIS LOOP v1.0 - {status['project']['name']}"
        iteration = f"Iteration: {status['iteration']['current']}/{status['iteration']['limit']}"
        cost = f"Cost: ${status['iteration']['cost_usd']:.2f}"
        
        content = f"{title} | {iteration} | {cost}"
        return Panel(content, style="bold blue")
    
    def render_tasks(self, status: Dict) -> Panel:
        """Linke Seite: Task List (wie im Video [01:32])"""
        table = Table(show_header=True, header_style="bold magenta")
        table.add_column("#", style="dim", width=4)
        table.add_column("Status", width=8)
        table.add_column("Task", width=30)
        table.add_column("Agent", width=12)
        
        for task in status.get('tasks_detail', []):
            status_icon = {
                "pending": "⏳",
                "in_progress": "🔄",
                "done": "✅",
                "failed": "❌"
            }.get(task['status'], "⏳")
            
            agent = task.get('assigned_agent', '-') or '-'
            
            table.add_row(
                str(task['id']),
                status_icon,
                task['title'][:28],
                agent[:10]
            )
        
        return Panel(table, title="📋 Tasks", border_style="blue")
    
    def render_log(self) -> Panel:
        """Rechte Seite: Live Log (wie im Video)"""
        # Zeige letzte 15 Log-Einträge
        log_text = "\n".join(self.current_log[-15:])
        return Panel(log_text, title="🖥️  Live Output", border_style="green")
    
    def render_footer(self) -> Panel:
        """Untere Status-Leiste mit Controls"""
        controls = "[S] Start/Pause | [T] Traces | [H] History | [Q] Quit"
        return Panel(controls, style="dim")
    
    def update_display(self, status: Dict) -> None:
        """Aktualisiert komplettes UI"""
        if not RICH_AVAILABLE:
            self._simple_ui(status)
            return
        
        layout = self.create_layout()
        
        layout["header"].update(self.render_header(status))
        layout["tasks"].update(self.render_tasks(status))
        layout["log"].update(self.render_log())
        layout["footer"].update(self.render_footer())
        
        self.console.clear()
        self.console.print(layout)
    
    def _simple_ui(self, status: Dict) -> None:
        """Einfaches UI ohne rich"""
        os.system('cls' if sys.platform == 'win32' else 'clear')
        
        print("=" * 60)
        print(f"🎯 JARVIS LOOP - {status['project']['name']}")
        print("=" * 60)
        print(f"Iteration: {status['iteration']['current']}/{status['iteration']['limit']} | "
              f"Cost: ${status['iteration']['cost_usd']:.2f}")
        print("-" * 60)
        
        print("\n📋 TASKS:")
        for task in status.get('tasks_detail', []):
            icon = {"pending": "⏳", "in_progress": "🔄", "done": "✅", "failed": "❌"}.get(task['status'], "⏳")
            print(f"  {icon} #{task['id']}: {task['title'][:40]}")
        
        print("\n🖥️  LIVE LOG:")
        for line in self.current_log[-10:]:
            print(f"  {line}")
        
        print("\n" + "-" * 60)
        print("[S] Start/Pause | [T] Traces | [H] History | [Q] Quit")
        print("=" * 60)
    
    def add_log(self, message: str) -> None:
        """Fügt Log-Eintrag hinzu"""
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.current_log.append(f"[{timestamp}] {message}")
        
        # Max 100 Einträge behalten
        if len(self.current_log) > 100:
            self.current_log = self.current_log[-100:]
    
    def handle_input(self) -> str:
        """Verarbeitet Tastatureingaben"""
        try:
            if sys.platform == 'win32':
                import msvcrt
                if msvcrt.kbhit():
                    key = msvcrt.getch().decode('utf-8').upper()
                    return key
            else:
                # Für Unix-Systeme
                import tty
                import termios
                import select
                
                if select.select([sys.stdin], [], [], 0)[0]:
                    return sys.stdin.read(1).upper()
        except:
            pass
        
        return None
    
    def run(self, task_manager) -> None:
        """Haupt-Loop für UI"""
        self.add_log("JARVIS Loop gestartet...")
        self.add_log("Drücke 'S' zum Starten")
        
        try:
            while self.running:
                # Status laden
                status = task_manager.get_status()
                
                # Tasks Detail hinzufügen
                data = task_manager._load_tasks()
                status['tasks_detail'] = data.get('tasks', [])
                
                # UI aktualisieren
                self.update_display(status)
                
                # Input prüfen
                key = self.handle_input()
                if key:
                    self._handle_key(key, task_manager)
                
                time.sleep(1)  # Refresh Rate
                
        except KeyboardInterrupt:
            print("\n👋 Beendet.")
    
    def _handle_key(self, key: str, task_manager) -> None:
        """Verarbeitet Tasten"""
        if key == 'Q':
            self.add_log("Beenden...")
            self.running = False
            
        elif key == 'S':
            self.add_log("Start/Pause gedrückt")
            # Hier würde Start/Pause Logik kommen
            
        elif key == 'T':
            self.show_traces = not self.show_traces
            self.add_log(f"Traces: {'ON' if self.show_traces else 'OFF'}")
            
        elif key == 'H':
            self.add_log("Zeige History...")
            # History anzeigen


# Mock für Windows ohne rich
import os

if __name__ == "__main__":
    # Test
    from task_manager import TaskManager
    
    tm = TaskManager("./test_tui")
    tm.create_project("Test", "Test Projekt")
    tm.add_task("Task 1", "coding")
    tm.add_task("Task 2", "testing")
    
    tui = JarvisTUI("./test_tui")
    tui.add_log("System bereit")
    tui.add_log("Warte auf Start...")
    
    try:
        tui.run(tm)
    except KeyboardInterrupt:
        print("\nBeendet")
