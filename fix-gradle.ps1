# Android Studio Gradle Fix Script
# Führe dieses Script aus, dann ist alles erledigt

Write-Host "🧹 Lösche alten Gradle Cache..." -ForegroundColor Yellow

# Lösche den problematischen Gradle 8.7 Cache
$gradleCache = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.7*"
if (Test-Path $gradleCache) {
    Remove-Item -Path $gradleCache -Recurse -Force
    Write-Host "✅ Gradle 8.7 Cache gelöscht" -ForegroundColor Green
} else {
    Write-Host "ℹ️ Kein 8.7 Cache gefunden" -ForegroundColor Cyan
}

# Lösche auch 8.4 falls vorhanden (sauberer Neustart)
$gradleCache84 = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.4*"
if (Test-Path $gradleCache84) {
    Remove-Item -Path $gradleCache84 -Recurse -Force
    Write-Host "✅ Gradle 8.4 Cache gelöscht" -ForegroundColor Green
}

Write-Host ""
Write-Host "📝 Gradle Version geändert zu: 8.5 (stabil)" -ForegroundColor Green
Write-Host ""
Write-Host "👉 Nächster Schritt:" -ForegroundColor Yellow
Write-Host "   1. Android Studio NEU STARTEN (komplett schließen und öffnen)"
Write-Host "   2. Warte auf automatischen Sync (oder klick Elefanten-Icon)"
Write-Host ""
Read-Host "Drücke ENTER wenn Android Studio neu gestartet wurde"