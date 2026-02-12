#!/usr/bin/env python3
"""
JARVIS Loop - Agent Orchestrator
Multi-Agent Coordination wie in Ralph Loop [10:02]
"""

import json
import time
import subprocess
from typing import Dict, List, Optional
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed


class AgentOrchestrator:
    """
    Koordiniert mehrere Agents parallel
    - Task-Verteilung an spezialisierte Agents
    - Monitoring von Agent-Traces
    - Session Management
    """
    
    def __init__(self, project_path: str):
        self.project_path = project_path
        self.active_agents = {}  # agent_id -> process
        self.completed_tasks = []
        self.failed_tasks = []
        
    def select_agent(self, task_type: str) -> str:
        """Wählt besten Agent für Task-Typ"""
        agent_map = {
            "coding": "coding-agent",
            "setup": "coding-agent",
            "config": "coding-agent",
            "testing": "testing-agent",
            "research": "research-agent",
            "security": "security-agent",
            "content": "content-agent",
            "review": "coding-agent"
        }
        return agent_map.get(task_type, "coding-agent")
    
    def spawn_agent(self, task: Dict, agent_type: str) -> Dict:
        """
        Startet Sub-Agent für Task (wie im Ralph Loop Video)
        Nutzt OpenClaw sessions_spawn
        """
        task_id = task["id"]
        title = task["title"]
        
        print(f"🚀 Spawning {agent_type} für Task #{task_id}: {title}")
        
        # Erstelle Sub-Agent Task
        subagent_task = f"""
JARVIS LOOP TASK #{task_id}: {title}

Kontext:
- Projekt: {self.project_path}
- Task Type: {task['type']}
- Beschreibung: {task.get('description', title)}

Deine Mission:
1. Führe diesen Task vollständig aus
2. Speichere Ergebnisse in {self.project_path}/output/{task_id}/
3. Aktualisiere {self.project_path}/tasks.json Status auf 'done'
4. Bei Fehlern: Status auf 'failed' setzen + Fehlermeldung

Safeguards:
- Max 30 Minuten pro Task
- Bei Unklarheiten: Bestätigung einholen
- Dokumentiere alle Änderungen

Wenn fertig, führe aus:
openclaw gateway wake --text "Task {task_id} fertig" --mode now
"""
        
        # Simuliere Sub-Agent Spawn (in echt: sessions_spawn)
        agent_info = {
            "task_id": task_id,
            "agent_type": agent_type,
            "status": "running",
            "started_at": datetime.now().isoformat(),
            "session_id": f"agent-{task_id}-{int(time.time())}"
        }
        
        self.active_agents[task_id] = agent_info
        
        return agent_info
    
    def run_parallel(self, tasks: List[Dict], max_parallel: int = 3) -> Dict:
        """
        Führt mehrere Tasks parallel aus (wie im Video [10:02])
        """
        results = {
            "completed": [],
            "failed": [],
            "running": []
        }
        
        print(f"🎬 Starting Parallel Execution: {len(tasks)} tasks, max {max_parallel} parallel")
        
        with ThreadPoolExecutor(max_workers=max_parallel) as executor:
            # Submit all tasks
            future_to_task = {}
            
            for task in tasks:
                agent_type = self.select_agent(task["type"])
                future = executor.submit(self._execute_task, task, agent_type)
                future_to_task[future] = task
            
            # Collect results as they complete
            for future in as_completed(future_to_task):
                task = future_to_task[future]
                try:
                    result = future.result(timeout=1800)  # 30 Min Timeout
                    if result["success"]:
                        results["completed"].append(result)
                        print(f"✅ Task #{task['id']} completed")
                    else:
                        results["failed"].append(result)
                        print(f"❌ Task #{task['id']} failed")
                except Exception as e:
                    print(f"💥 Task #{task['id']} crashed: {e}")
                    results["failed"].append({
                        "task_id": task["id"],
                        "error": str(e)
                    })
        
        return results
    
    def _execute_task(self, task: Dict, agent_type: str) -> Dict:
        """Führt einzelnen Task aus (simuliert)"""
        task_id = task["id"]
        
        # In echt würde hier sessions_spawn oder subprocess laufen
        # Für Demo: Simuliere Arbeit
        time.sleep(2)  # Simuliere 2 Sekunden Arbeit
        
        # Update Task Status
        return {
            "task_id": task_id,
            "success": True,
            "agent_type": agent_type,
            "output": f"Task {task_id} completed by {agent_type}",
            "cost_usd": 0.10
        }
    
    def get_agent_traces(self, task_id: int = None) -> List[Dict]:
        """
        Gibt Agent Traces zurück (wie 'T' Taste im Ralph Loop Video [09:44])
        """
        traces = []
        
        if task_id:
            # Spezifischer Task
            if task_id in self.active_agents:
                agent = self.active_agents[task_id]
                traces.append({
                    "task_id": task_id,
                    "agent": agent["agent_type"],
                    "status": agent["status"],
                    "started": agent["started_at"]
                })
        else:
            # Alle Agents
            for tid, agent in self.active_agents.items():
                traces.append({
                    "task_id": tid,
                    "agent": agent["agent_type"],
                    "status": agent["status"],
                    "started": agent["started_at"]
                })
        
        return traces
    
    def pause_all(self) -> None:
        """Pausiert alle laufenden Agents (wie im Video)"""
        print("⏸️  Pausing all agents...")
        for task_id, agent in self.active_agents.items():
            if agent["status"] == "running":
                agent["status"] = "paused"
                print(f"  Paused Task #{task_id}")
    
    def resume_all(self) -> None:
        """Setzt alle pausierten Agents fort"""
        print("▶️  Resuming all agents...")
        for task_id, agent in self.active_agents.items():
            if agent["status"] == "paused":
                agent["status"] = "running"
                print(f"  Resumed Task #{task_id}")
    
    def kill_agent(self, task_id: int) -> bool:
        """Beendet spezifischen Agent"""
        if task_id in self.active_agents:
            print(f"💀 Killing agent for Task #{task_id}")
            del self.active_agents[task_id]
            return True
        return False


class SafeguardMonitor:
    """Überwacht Safeguards wie in Ralph Loop"""
    
    def __init__(self, config: Dict):
        self.config = config
        self.total_cost = 0.00
        self.iteration_count = 0
        self.start_time = datetime.now()
    
    def check_limits(self, current_cost: float = 0.00) -> Dict:
        """Prüft ob Limits erreicht"""
        alerts = []
        should_stop = False
        
        # Cost Limit
        self.total_cost += current_cost
        if self.total_cost >= self.config.get("max_total_cost_usd", 10.00):
            alerts.append(f"💰 Cost limit reached: ${self.total_cost:.2f}")
            should_stop = True
        
        # Iteration Limit
        self.iteration_count += 1
        if self.iteration_count >= self.config.get("max_iterations", 35):
            alerts.append(f"🔄 Iteration limit reached: {self.iteration_count}")
            should_stop = True
        
        # Time Limit (pro Iteration)
        elapsed = (datetime.now() - self.start_time).total_seconds() / 60
        if elapsed > self.config.get("max_time_per_task_min", 30):
            alerts.append(f"⏰ Time limit reached: {elapsed:.0f} min")
            should_stop = True
        
        return {
            "should_stop": should_stop,
            "alerts": alerts,
            "stats": {
                "total_cost": self.total_cost,
                "iterations": self.iteration_count,
                "elapsed_min": elapsed
            }
        }


if __name__ == "__main__":
    # Test
    orch = AgentOrchestrator("./test_project")
    
    test_tasks = [
        {"id": 1, "title": "Setup", "type": "setup"},
        {"id": 2, "title": "API bauen", "type": "coding"},
        {"id": 3, "title": "Tests", "type": "testing"}
    ]
    
    results = orch.run_parallel(test_tasks, max_parallel=2)
    print(json.dumps(results, indent=2))
