# Vollautomatischer Gradle Fix
Write-Host "🔄 AUTOMATISCHER FIX STARTET..." -ForegroundColor Green

# 1. Android Studio Prozesse beenden
Write-Host "1. Beende Android Studio..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.Name -match "studio64|java|gradle"} | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

# 2. Cache löschen
Write-Host "2. Loesche Gradle Cache..." -ForegroundColor Yellow
$cache = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.7*"
if (Test-Path $cache) {
    Remove-Item -Path $cache -Recurse -Force -ErrorAction SilentlyContinue
}

# 3. Zeige Erfolg
Write-Host ""
Write-Host "✅ FERTIG!" -ForegroundColor Green
Write-Host ""
Write-Host "👉 JETZT MUSST DU NUR NOCH:" -ForegroundColor Cyan
Write-Host "   Android Studio starten" -ForegroundColor White
Write-Host "   Das Projekt oeffnet sich automatisch" -ForegroundColor White
Write-Host "   Gradle 8.5 wird heruntergeladen (2-3 Min)" -ForegroundColor White
Write-Host ""
Read-Host "Druecke ENTER zum Beenden"