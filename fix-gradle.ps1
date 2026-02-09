# Android Studio Gradle Fix Script
Write-Host "Gradle Fix wird ausgefuehrt..."

# Loesche alten Gradle Cache
$gradleCache = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.7*"
if (Test-Path $gradleCache) {
    Remove-Item -Path $gradleCache -Recurse -Force
    Write-Host "Gradle 8.7 Cache geloescht"
}

Write-Host "Fertig! Starte Android Studio neu."