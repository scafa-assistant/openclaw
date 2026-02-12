#!/usr/bin/env python3
"""
JARVIS Loop - Task Manager
JSON-basierter Task Manager wie Ralph Loop
"""

import json
import os
import time
from datetime import datetime
from typing import List, Dict, Optional
from pathlib import Path

class TaskManager:
    """Verwaltet Tasks im JSON Format (wie Ralph Loop)"""
    
    def __init__(self, project_path: str):
        self.project_path = Path(project_path)
        self.tasks_file = self.project_path / "tasks.json"
        self.session_file = self.project_path / "session.jsonl"
        self.tasks = []
        self.current_iteration = 0
        self.config = self._load_config()
        
    def _load_config(self) -> Dict:
        """Lädt Loop Konfiguration"""
        config_path = Path(__file__).parent.parent / "config" / "default_config.json"
        with open(config_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    def create_project(self, name: str, description: str) -> None:
        """Erstellt neues Projekt (wie ralph loop setup)"""
        self.project_path.mkdir(parents=True, exist_ok=True)
        
        project_data = {
            "project": {
                "name": name,
                "description": description,
                "created_at": datetime.now().isoformat(),
                "status": "initialized"
            },
            "iteration": {
                "current": 0,
                "limit": self.config["safeguards"]["max_iterations"],
                "cost_usd": 0.00
            },
            "tasks": [],
            "safeguards": self.config["safeguards"],
            "agents": self.config["agents"]
        }
        
        self._save_tasks(project_data)
        self._log_event("project_created", {"name": name})
        
    def add_task(self, title: str, task_type: str = "coding", 
                 dependencies: List[int] = None, 
                 estimated_cost: float = 0.50) -> int:
        """Fügt neuen Task hinzu"""
        task_id = len(self.tasks) + 1
        
        task = {
            "id": task_id,
            "title": title,
            "type": task_type,
            "status": "pending",  # pending, in_progress, done, failed
            "assigned_agent": None,
            "estimated_cost": estimated_cost,
            "actual_cost": 0.00,
            "dependencies": dependencies or [],
            "created_at": datetime.now().isoformat(),
            "started_at": None,
            "completed_at": None,
            "attempts": 0,
            "output": None
        }
        
        data = self._load_tasks()
        data["tasks"].append(task)
        self._save_tasks(data)
        
        self._log_event("task_added", {"id": task_id, "title": title})
        return task_id
    
    def get_ready_tasks(self) -> List[Dict]:
        """Gibt Tasks zurück die bereit sind (Dependencies erfüllt)"""
        data = self._load_tasks()
        tasks = data["tasks"]
        
        ready = []
        for task in tasks:
            if task["status"] != "pending":
                continue
                
            # Prüfe Dependencies
            deps_satisfied = all(
                any(t["id"] == dep and t["status"] == "done" 
                    for t in tasks)
                for dep in task["dependencies"]
            )
            
            if deps_satisfied:
                ready.append(task)
        
        return ready
    
    def assign_task(self, task_id: int, agent: str) -> bool:
        """Weist Task einem Agenten zu"""
        data = self._load_tasks()
        
        for task in data["tasks"]:
            if task["id"] == task_id:
                task["assigned_agent"] = agent
                task["status"] = "in_progress"
                task["started_at"] = datetime.now().isoformat()
                task["attempts"] += 1
                break
        
        self._save_tasks(data)
        self._log_event("task_assigned", {"id": task_id, "agent": agent})
        return True
    
    def complete_task(self, task_id: int, output: str = None, 
                     cost: float = 0.00) -> bool:
        """Markiert Task als erledigt"""
        data = self._load_tasks()
        
        for task in data["tasks"]:
            if task["id"] == task_id:
                task["status"] = "done"
                task["completed_at"] = datetime.now().isoformat()
                task["actual_cost"] = cost
                task["output"] = output
                break
        
        data["iteration"]["cost_usd"] += cost
        self._save_tasks(data)
        
        self._log_event("task_completed", {"id": task_id, "cost": cost})
        return True
    
    def fail_task(self, task_id: int, error: str) -> bool:
        """Markiert Task als fehlgeschlagen"""
        data = self._load_tasks()
        
        for task in data["tasks"]:
            if task["id"] == task_id:
                task["status"] = "failed"
                task["output"] = error
                break
        
        self._save_tasks(data)
        self._log_event("task_failed", {"id": task_id, "error": error})
        return True
    
    def check_safeguards(self) -> Dict:
        """Prüft ob Safeguards ausgelöst werden"""
        data = self._load_tasks()
        iteration = data["iteration"]
        safeguards = data["safeguards"]
        
        alerts = []
        
        # Iteration Limit
        if iteration["current"] >= safeguards["max_iterations"]:
            alerts.append({
                "type": "iteration_limit",
                "message": f"Limit {safeguards['max_iterations']} erreicht"
            })
        
        # Cost Limit
        if iteration["cost_usd"] >= safeguards["max_total_cost_usd"]:
            alerts.append({
                "type": "cost_limit", 
                "message": f"Kosten ${iteration['cost_usd']:.2f} / ${safeguards['max_total_cost_usd']:.2f}"
            })
        
        return {
            "should_pause": len(alerts) > 0,
            "alerts": alerts
        }
    
    def increment_iteration(self) -> int:
        """Erhöht Iterations-Zähler"""
        data = self._load_tasks()
        data["iteration"]["current"] += 1
        self._save_tasks(data)
        return data["iteration"]["current"]
    
    def get_status(self) -> Dict:
        """Gibt aktuellen Status zurück"""
        data = self._load_tasks()
        tasks = data["tasks"]
        
        total = len(tasks)
        done = sum(1 for t in tasks if t["status"] == "done")
        failed = sum(1 for t in tasks if t["status"] == "failed")
        in_progress = sum(1 for t in tasks if t["status"] == "in_progress")
        pending = sum(1 for t in tasks if t["status"] == "pending")
        
        return {
            "project": data["project"],
            "iteration": data["iteration"],
            "tasks": {
                "total": total,
                "done": done,
                "failed": failed,
                "in_progress": in_progress,
                "pending": pending
            },
            "progress_percent": (done / total * 100) if total > 0 else 0
        }
    
    def _load_tasks(self) -> Dict:
        """Lädt tasks.json"""
        if not self.tasks_file.exists():
            return {"tasks": [], "iteration": {}, "project": {}}
        
        with open(self.tasks_file, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    def _save_tasks(self, data: Dict) -> None:
        """Speichert tasks.json"""
        with open(self.tasks_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
    
    def _log_event(self, event_type: str, data: Dict) -> None:
        """Schreibt Event in Session Log (JSONL)"""
        event = {
            "timestamp": datetime.now().isoformat(),
            "type": event_type,
            "data": data
        }
        
        with open(self.session_file, 'a', encoding='utf-8') as f:
            f.write(json.dumps(event, ensure_ascii=False) + '\n')


if __name__ == "__main__":
    # Test
    tm = TaskManager("./test_project")
    tm.create_project("Test", "Test Projekt")
    tm.add_task("Setup Projekt", "coding")
    tm.add_task("API bauen", "coding", dependencies=[1])
    
    print(json.dumps(tm.get_status(), indent=2))
