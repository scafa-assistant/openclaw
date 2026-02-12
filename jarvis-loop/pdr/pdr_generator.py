#!/usr/bin/env python3
"""
JARVIS Loop - PDR Generator
Product Requirement Document durch interaktiven Dialog
Wie im Ralph Loop Video gezeigt [05:35]
"""

import json
import questionary
from typing import Dict, List
from datetime import datetime

class PDRGenerator:
    """Generiert PDR durch interaktiven Dialog"""
    
    def __init__(self):
        self.pdr = {}
        
    def create_interactive(self) -> Dict:
        """
        Interaktiver Dialog wie im Video:
        - Fragen zu Ziel, Plattform, Umfang
        - Automatische Task-Generierung
        """
        print("🎯 JARVIS LOOP - PDR Generator")
        print("=" * 50)
        print("Beantworte die Fragen um dein Projekt zu definieren.\n")
        
        # 1. Projekt Name
        self.pdr["name"] = questionary.text(
            "Wie heißt dein Projekt?",
            default="Meine App"
        ).ask()
        
        # 2. Beschreibung
        self.pdr["description"] = questionary.text(
            "Beschreibe kurz das Ziel:",
            default="Eine Web-App für..."
        ).ask()
        
        # 3. Plattform (wie im Video [06:32])
        self.pdr["platform"] = questionary.select(
            "Welche Plattform?",
            choices=[
                "Web (Browser)",
                "Mobile (Android/iOS)",
                "Desktop (Windows/Mac/Linux)",
                "Backend/API",
                "Multi-Platform"
            ]
        ).ask()
        
        # 4. Umfang (wie im Video [07:02])
        self.pdr["scope"] = questionary.select(
            "Welcher Umfang?",
            choices=[
                "MVP (Minimum Viable Product)",
                "Vollversion (mit allen Features)",
                "Prototype (nur zum Testen)"
            ]
        ).ask()
        
        # 5. Haupt-Features
        features = questionary.checkbox(
            "Welche Features soll es geben?",
            choices=[
                "User Authentifizierung",
                "Datenbank/Storage",
                "API Integration",
                "UI/Frontend",
                "Tests",
                "Dokumentation",
                "Deployment Setup"
            ]
        ).ask()
        self.pdr["features"] = features
        
        # 6. Iteration Limit (Safeguard wie im Video [04:35])
        self.pdr["iteration_limit"] = int(questionary.select(
            "Wie viele Iterationen maximal? (Safeguard)",
            choices=[
                "10 (kleines Projekt)",
                "25 (mittleres Projekt)",
                "35 (großes Projekt)",
                "50 (sehr großes Projekt)"
            ],
            default="35 (großes Projekt)"
        ).ask().split()[0])
        
        # 7. Budget
        budget = questionary.select(
            "Geschätztes Budget (API-Kosten)?",
            choices=[
                "$5 (Experiment)",
                "$10 (klein)",
                "$25 (mittel)",
                "$50 (groß)"
            ]
        ).ask()
        self.pdr["budget_usd"] = float(budget.split('$')[1].split()[0])
        
        # Zeitstempel
        self.pdr["created_at"] = datetime.now().isoformat()
        
        # Generiere Tasks aus PDR
        tasks = self._generate_tasks()
        
        print("\n" + "=" * 50)
        print(f"✅ PDR erstellt: {self.pdr['name']}")
        print(f"📋 {len(tasks)} Tasks generiert")
        print(f"🛡️  Safeguards: {self.pdr['iteration_limit']} Iterationen, ${self.pdr['budget_usd']} Budget")
        
        return {
            "pdr": self.pdr,
            "tasks": tasks
        }
    
    def _generate_tasks(self) -> List[Dict]:
        """Generiert Tasks basierend auf PDR"""
        tasks = []
        task_id = 1
        
        # Basis Setup (immer)
        tasks.append({
            "id": task_id,
            "title": "Projektstruktur erstellen",
            "type": "setup",
            "description": "Ordner, Config, Dependencies"
        })
        setup_id = task_id
        task_id += 1
        
        # Features zu Tasks
        feature_tasks = {
            "User Authentifizierung": [
                "Auth-System implementieren",
                "Login/Register UI",
                "Session Management"
            ],
            "Datenbank/Storage": [
                "Datenbank-Schema designen",
                "Models/Entities erstellen",
                "CRUD Operationen"
            ],
            "API Integration": [
                "API Endpoints definieren",
                "API implementieren",
                "API Dokumentation"
            ],
            "UI/Frontend": [
                "UI Design/Wireframes",
                "Frontend implementieren",
                "Responsive Design"
            ],
            "Tests": [
                "Unit Tests schreiben",
                "Integration Tests",
                "E2E Tests"
            ],
            "Dokumentation": [
                "README erstellen",
                "Code dokumentieren",
                "API Docs"
            ],
            "Deployment Setup": [
                "Docker/Container Setup",
                "CI/CD Pipeline",
                "Deployment konfigurieren"
            ]
        }
        
        for feature in self.pdr.get("features", []):
            if feature in feature_tasks:
                for subtask in feature_tasks[feature]:
                    tasks.append({
                        "id": task_id,
                        "title": subtask,
                        "type": "coding" if "implement" in subtask else "config",
                        "dependencies": [setup_id]
                    })
                    task_id += 1
        
        # Testing & QA (immer am Ende)
        if "Tests" not in self.pdr.get("features", []):
            tasks.append({
                "id": task_id,
                "title": "Basis Tests",
                "type": "testing",
                "dependencies": [t["id"] for t in tasks if t["type"] == "coding"][:3]
            })
            task_id += 1
        
        # Final Review
        tasks.append({
            "id": task_id,
            "title": "Final Review & Cleanup",
            "type": "review",
            "dependencies": [task_id - 1]
        })
        
        return tasks
    
    def save_pdr(self, filepath: str) -> None:
        """Speichert PDR als JSON"""
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(self.pdr, f, indent=2, ensure_ascii=False)
    
    @staticmethod
    def load_pdr(filepath: str) -> Dict:
        """Lädt PDR aus JSON"""
        with open(filepath, 'r', encoding='utf-8') as f:
            return json.load(f)


# CLI Interface
def main():
    """PDR Generator CLI"""
    import argparse
    
    parser = argparse.ArgumentParser(description="JARVIS Loop PDR Generator")
    parser.add_argument("--output", "-o", default="pdr.json", help="Output file")
    args = parser.parse_args()
    
    gen = PDRGenerator()
    result = gen.create_interactive()
    
    # Speichern
    with open(args.output, 'w', encoding='utf-8') as f:
        json.dump(result, f, indent=2, ensure_ascii=False)
    
    print(f"\n💾 Gespeichert: {args.output}")
    
    return result


if __name__ == "__main__":
    main()
