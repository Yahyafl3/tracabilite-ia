Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  TEST ENDPOINT IA AVEC TOKEN JWT - VERSION 2" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Token JWT fourni par l'utilisateur
$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlMGM2ZmU3ZS1lMmJmLTRiMDctODdhZS0wMDg4NzgwNjU1ZGIiLCJlbWFpbCI6ImFkbWluQHRyYWNhYmlsaXRlLmlhIiwicm9sZSI6IkFETUlOSVNUUkFURVVSIiwiaWF0IjoxNzg0MDQyNzgyLCJleHAiOjE3ODQxMjkxODJ9.a5ngeBEtCkh49tsUwNeLPfpDGb7DXn_lGETI0F3XqJc6s6X3moy97SabcBmItXzdW6dBwaegIYRwCP2Zq_7hHQ"

Write-Host "Token JWT: $($token.Substring(0,50))..." -ForegroundColor Yellow
Write-Host ""

# Test 1: Vérifier le health du backend
Write-Host "[1/3] Vérification du backend..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "  ✅ Backend: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Backend inaccessible!" -ForegroundColor Red
    Write-Host "     Erreur: $($_.Exception.Message)" -ForegroundColor Gray
    exit 1
}

Write-Host ""

# Test 2: Tester l'endpoint IA SANS token (devrait réussir avec permitAll)
Write-Host "[2/3] Test sans authentification (permitAll)..." -ForegroundColor Cyan
$headers_no_auth = @{
    "Content-Type" = "application/json"
}
$body = @{
    prompt = "Analyser la décision: Refuser un crédit de 10000 EUR"
    contexte = "Client avec 2 incidents de paiement récents, score de crédit: 580"
} | ConvertTo-Json

try {
    Write-Host "  Requête POST /api/ai/analyze-decision (SANS Authorization)" -ForegroundColor Gray
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/analyze-decision" `
        -Method Post `
        -Body $body `
        -Headers $headers_no_auth `
        -TimeoutSec 30
    Write-Host "  ✅ Réponse reçue SANS token!" -ForegroundColor Green
    Write-Host "  Modèle: $($response.model)" -ForegroundColor Yellow
    Write-Host "  Analyse: $($response.analysis.Substring(0, [Math]::Min(100, $response.analysis.Length)))..." -ForegroundColor White
} catch {
    Write-Host "  ❌ Erreur SANS token: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "     Code HTTP: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Gray
    }
}

Write-Host ""

# Test 3: Tester l'endpoint IA AVEC token
Write-Host "[3/3] Test avec authentification JWT..." -ForegroundColor Cyan
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    Write-Host "  Requête POST /api/ai/analyze-decision (AVEC Authorization)" -ForegroundColor Gray
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/analyze-decision" `
        -Method Post `
        -Body $body `
        -Headers $headers `
        -TimeoutSec 30
    Write-Host "  ✅ Réponse reçue AVEC token!" -ForegroundColor Green
    Write-Host "  Modèle: $($response.model)" -ForegroundColor Yellow
    Write-Host "  Analyse: $($response.analysis.Substring(0, [Math]::Min(100, $response.analysis.Length)))..." -ForegroundColor White
} catch {
    Write-Host "  ❌ Erreur AVEC token: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "     Code HTTP: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  TEST TERMINÉ" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Pour voir les logs détaillés du backend:" -ForegroundColor Yellow
Write-Host "   docker logs tracabilite-backend --tail 50" -ForegroundColor Gray
