# OpenClaw Test Script
# Schneller Funktionstest aller Komponenten

Write-Host "🚀 OpenClaw - Schnelltest" -ForegroundColor Cyan
Write-Host "==========================" -ForegroundColor Cyan
Write-Host ""

# 1. Backend Status
Write-Host "1️⃣  Backend Status prüfen..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:3000/health" -UseBasicParsing -TimeoutSec 3
    $data = $response.Content | ConvertFrom-Json
    Write-Host "   ✅ Backend läuft" -ForegroundColor Green
    Write-Host "   📊 Users: $($data.users), Messages: $($data.messages)" -ForegroundColor Gray
} catch {
    Write-Host "   ❌ Backend nicht erreichbar" -ForegroundColor Red
    Write-Host "   💡 Starte mit: cd openclaw-backend; node server-local.js" -ForegroundColor Gray
    exit 1
}

# 2. Test Accounts
Write-Host ""
Write-Host "2️⃣  Test-Accounts laden..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:3000/api/v1/auth/test-accounts" -UseBasicParsing -TimeoutSec 3
    $data = $response.Content | ConvertFrom-Json
    Write-Host "   ✅ $($data.accounts.Count) Test-Accounts verfügbar:" -ForegroundColor Green
    $data.accounts | ForEach-Object {
        Write-Host "      📧 $($_.email) / $($_.password)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ⚠️  Test-Accounts Endpunkt nicht verfügbar" -ForegroundColor Yellow
}

# 3. Login Test
Write-Host ""
Write-Host "3️⃣  Login Test..." -ForegroundColor Yellow
$loginBody = @{
    email = "demo1@openclaw.test"
    password = "demo123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:3000/api/v1/auth/login" `
        -Method POST -ContentType "application/json" `
        -Body $loginBody -UseBasicParsing -TimeoutSec 5
    $data = $response.Content | ConvertFrom-Json
    $token = $data.accessToken
    Write-Host "   ✅ Login erfolgreich" -ForegroundColor Green
    Write-Host "   🔑 Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
    
    # 4. Chat Test
    Write-Host ""
    Write-Host "4️⃣  Chat Test..." -ForegroundColor Yellow
    $chatBody = @{
        message = "Hallo OpenClaw!"
        model = "gemini"
    } | ConvertTo-Json
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-WebRequest -Uri "http://127.0.0.1:3000/api/v1/chat/message" `
            -Method POST -Headers $headers `
            -Body $chatBody -UseBasicParsing -TimeoutSec 10
        $chatData = $response.Content | ConvertFrom-Json
        Write-Host "   ✅ Chat funktioniert" -ForegroundColor Green
        Write-Host "   💬 Antwort: $($chatData.content.Substring(0, [Math]::Min(50, $chatData.content.Length)))..." -ForegroundColor Gray
    } catch {
        Write-Host "   ❌ Chat Fehler: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "   ❌ Login Fehler: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "==========================" -ForegroundColor Cyan
Write-Host "🎯 Test abgeschlossen!" -ForegroundColor Cyan
