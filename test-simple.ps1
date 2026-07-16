# Test Simple - Application Tracabilite IA

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Tests Application Tracabilite IA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Test conteneurs
Write-Host "`n[1] Conteneurs Docker:" -ForegroundColor Yellow
docker ps --format "table {{.Names}}`t{{.Status}}"

# 2. Test Backend Health
Write-Host "`n[2] Backend Health:" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "Erreur: $_" -ForegroundColor Red
}

# 3. Test Ollama
Write-Host "`n[3] Ollama Models:" -ForegroundColor Yellow
try {
    $ollama = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 5
    foreach ($model in $ollama.models) {
        $sizeMB = [math]::Round($model.size / 1MB, 2)
        Write-Host "  - $($model.name): $sizeMB MB" -ForegroundColor Green
    }
} catch {
    Write-Host "Erreur: $_" -ForegroundColor Red
}

# 4. Test Authentification
Write-Host "`n[4] Test Authentification (admin):" -ForegroundColor Yellow
try {
    $body = @{
        email = "admin@tracabilite.ia"
        motDePasse = "admin123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -TimeoutSec 5

    Write-Host "  Token recu (longueur: $($response.token.Length))" -ForegroundColor Green
    Write-Host "  Email: $($response.email)" -ForegroundColor Green
    Write-Host "  Role: $($response.role)" -ForegroundColor Green
    
    $token = $response.token

    # 5. Test API IA
    Write-Host "`n[5] Test Analyse IA:" -ForegroundColor Yellow
    
    $analysisBody = @{
        prompt = "Analyser la decision: Approuver credit 50000 EUR sur 5 ans"
        contexte = "Demande de credit client avec bon historique de paiement, revenus stables, pas d'incidents bancaires"
    } | ConvertTo-Json

    Write-Host "  Envoi requete vers Ollama..." -ForegroundColor Gray
    
    $analysis = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/analyze-decision" `
        -Method Post `
        -Body $analysisBody `
        -Headers @{
            "Content-Type" = "application/json"
            "Authorization" = "Bearer $token"
        } `
        -TimeoutSec 30

    Write-Host "  Analyse recue (longueur: $($analysis.analysis.Length) caracteres)" -ForegroundColor Green
    Write-Host "  Modele: $($analysis.model)" -ForegroundColor Green
    
    $excerpt = $analysis.analysis.Substring(0, [Math]::Min(150, $analysis.analysis.Length))
    Write-Host "`n  Extrait de l'analyse:" -ForegroundColor Cyan
    Write-Host "  $excerpt..." -ForegroundColor White

} catch {
    Write-Host "Erreur: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "URLs Principales:" -ForegroundColor Yellow
Write-Host "  Frontend:  http://localhost" -ForegroundColor White
Write-Host "  Backend:   http://localhost:8080" -ForegroundColor White
Write-Host "  Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
