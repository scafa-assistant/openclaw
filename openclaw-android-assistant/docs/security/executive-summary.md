# Security-Konzept — Executive Summary für Gigi

## 🔐 Die Festung

Jeder User bekommt einen eigenen isolierten Container (Pod) in unserem K3s-Cluster. Network Policies sperren alles ab ("Deny All" Standard) — der Container kann nur mit unserem API-Gateway und den LLM-Providern sprechen. Falco überwacht 24/7 auf verdächtige Aktivitäten.

## 🔑 Die Schlüssel

Alle API-Keys (OpenAI, Claude, etc.) werden in HashiCorp Vault verschlüsselt gespeichert, nie im Code. Jede Verbindung läuft über mTLS (mutual TLS) — beide Seiten müssen sich authentifizieren. Keys rotieren automatisch alle 90 Tage.

## 📋 DSGVO

Alles bleibt in Deutschland (Hetzner). Wir speichern nur das Minimum (User-ID, E-Mail, Chat-History 30 Tage). User hat "Right to Deletion" — 1 Klick, alle seine Daten werden gelöscht (Container, Keys, History).

## 💰 Kosten

Bei 10.000 aktiven Usern ca. €4.200/Monat für die Infrastruktur (€0.42 pro User). Das ist das Sicherheitsniveau einer Bank-App.

---
**Erstellt von:** MORPHEUS  
**Datum:** 2026-02-08  
**Status:** ✅ Fertig für Review
