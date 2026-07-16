# Test de l'API IA avec Token JWT

$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlMGM2ZmU3ZS1lMmJmLTRiMDctODdhZS0wMDg4NzgwNjU1ZGIiLCJlbWFpbCI6ImFkbWluQHRyYWNhYmlsaXRlLmlhIiwicm9sZSI6IkFETUlOSVNUUkFURVVSIiwiaWF0IjoxNzg0MDQyNzgyLCJleHAiOjE3ODQxMjkxODJ9.a5ngeBEtCkh49tsUwNeLPfpDGb7DXn_lGETI0F3XqJc6s6X3moy97SabcBmItXzdW6dBwaegIYRwCP2Zq_7hHQ"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$body = @{
    prompt = "Analyser la décision : Refuser un crédit de 10000 EUR"
    contexte = "Client avec 2 incidents de paiement récents"
} | ConvertTo-Json

Write-Host "`n=== Test de l'endpoint IA ===" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8080/api/ai/analyze-decision" -ForegroundColor Gray
Write-Host "Token: $($token.Substring(0, 50))..." -ForegroundColor Gray
Write-Host ""

try {
    Write-Host "Envoi de la requête à Ollama..." -ForegroundColor Yellow
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/analyze-decision" `
        -Method Post `
        -Body $body `
        -Headers $headers `
        -TimeoutSec 30
    
    Write-Host "✅ Analyse reçue avec succès !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Modèle utilisé: $($response.model)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "=== Analyse générée par Ollama ===" -ForegroundColor Cyan
    Write-Host $response.analysis -ForegroundColor White
    Write-Host ""
    
    if ($response.timestamp) {
        Write-Host "Timestamp: $($response.timestamp)" -ForegroundColor Gray
    }
    if ($response.tokensUsed) {
        Write-Host "Tokens utilisés: $($response.tokensUsed)" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "❌ Erreur lors de l'appel à l'API" -ForegroundColor Red
    Write-Host ""
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Yellow
    
    if ($_.Exception.Response) {
        Write-Host "Code HTTP: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
        Write-Host "Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Détails techniques:" -ForegroundColor Gray
    Write-Host $_.Exception -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "=== Test terminé ===" -ForegroundColor Cyan
