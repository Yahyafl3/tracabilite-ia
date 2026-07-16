# Script de Test Complet - Application Traçabilité IA
# Date : 14 juillet 2026

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Test Complet - Traçabilité IA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction d'affichage avec couleur
function Test-Result {
    param(
        [string]$TestName,
        [bool]$Success,
        [string]$Details = ""
    )
    
    if ($Success) {
        Write-Host "[✓] $TestName" -ForegroundColor Green
        if ($Details) {
            Write-Host "    → $Details" -ForegroundColor Gray
        }
    } else {
        Write-Host "[✗] $TestName" -ForegroundColor Red
        if ($Details) {
            Write-Host "    → $Details" -ForegroundColor Yellow
        }
    }
}

# 1. TEST DES CONTENEURS
Write-Host "`n[1/6] Vérification des conteneurs Docker..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────" -ForegroundColor Gray

$containers = docker ps --format "{{.Names}}"
$requiredContainers = @(
    "tracabilite-backend",
    "tracabilite-frontend", 
    "tracabilite-postgres",
    "tracabilite-ml-service",
    "tracabilite-ollama"
)

foreach ($container in $requiredContainers) {
    $isRunning = $containers -contains $container
    Test-Result -TestName "Conteneur $container" -Success $isRunning
}

# 2. TEST BACKEND
Write-Host "`n[2/6] Test du Backend Spring Boot..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────" -ForegroundColor Gray

try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    $backendUp = $healthResponse.status -eq "UP"
    Test-Result -TestName "Backend Health" -Success $backendUp -Details "Status: $($healthResponse.status)"
} catch {
    Test-Result -TestName "Backend Health" -Success $false -Details "Erreur: $_"
}

# 3. TEST FRONTEND
Write-Host "`n[3/6] Test du Frontend Angular..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────" -ForegroundColor Gray

try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost" -TimeoutSec 5 -ErrorAction Stop
    $frontendUp = $frontendResponse.StatusCode -eq 200
    Test-Result -TestName "Frontend Accessible" -Success $frontendUp -Details "Code HTTP: $($frontendResponse.StatusCode)"
} catch {
    Test-Result -TestName "Frontend Accessible" -Success $false -Details "Erreur: $_"
}

# 4. TEST OLLAMA
Write-Host "`n[4/6] Test d'Ollama..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────" -ForegroundColor Gray

try {
    $ollamaResponse = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 5 -ErrorAction Stop
    $hasModels = $ollamaResponse.models.Count -gt 0
    Test-Result -TestName "Ollama API" -Success $hasModels
    
    if ($hasModels) {
        foreach ($model in $ollamaResponse.models) {
            $sizeMB = [math]::Round($model.size / 1MB, 2)
            Write-Host "    → Modèle: $($model.name) ($sizeMB MB)" -ForegroundColor Gray
        }
    }
} catch {
    Test-Result -TestName "Ollama API" -Success $false -Details "Erreur: $_"
}

# 5. TEST AUTHENTIFICATION
Write-Host "`n[5/6] Test d'Authentification..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────" -ForegroundColor Gray

try {
    $loginBody = @{
        email = "admin@tracabilite.ia"
        password = "admin123"
    } | ConvertTo-Json

    $headers = @{
        "Content-Type" = "application/json"
    }

    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -Headers $headers -TimeoutSec 5 -ErrorAction Stop
    
    $hasToken = $null -ne $loginResponse.token
    Test-Result -TestName "Connexion Admin" -Success $hasToken
    
    if ($hasToken) {
        Write-Host "    → Token JWT reçu (longueur: $($loginResponse.token.Length))" -ForegroundColor Gray
        Write-Host "    → Utilisateur: $($loginResponse.email)" -ForegroundColor Gray
        Write-Host "    → Rôle: $($loginResponse.role)" -ForegroundColor Gray
        
        # Sauvegarder le token pour les tests suivants
        $global:authToken = $loginResponse.token
    }
} catch {
    Test-Result -TestName "Connexion Admin" -Success $false -Details "Erreur: $_"
    $global:authToken = $null
}

# 6. TEST API IA (SI TOKEN DISPONIBLE)
Write-Host "`n[6/6] Test de l'API IA (Analyse de Décision)..." -ForegroundColor Yellow
Write-Host "─────────────────────────────────────────────" -ForegroundColor Gray

if ($global:authToken) {
    try {
        $analysisBody = @{
            context = "Demande de crédit pour un client avec bon historique de paiement"
            decision = "Approuver le crédit de 50000 EUR sur 5 ans"
            metadata = @{
                clientId = "TEST-001"
                amount = 50000
                duration = 60
            }
        } | ConvertTo-Json

        $headers = @{
            "Content-Type" = "application/json"
            "Authorization" = "Bearer $global:authToken"
        }

        Write-Host "    → Envoi de la requête d'analyse..." -ForegroundColor Gray
        $analysisResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/analyze-decision" -Method Post -Body $analysisBody -Headers $headers -TimeoutSec 30 -ErrorAction Stop
        
        $hasAnalysis = $null -ne $analysisResponse.analysis
        Test-Result -TestName "Analyse IA Générée" -Success $hasAnalysis
        
        if ($hasAnalysis) {
            Write-Host "    → Analyse reçue (longueur: $($analysisResponse.analysis.Length) caractères)" -ForegroundColor Gray
            Write-Host "    → Modèle utilisé: $($analysisResponse.model)" -ForegroundColor Gray
            
            # Afficher un extrait de l'analyse
            $excerpt = $analysisResponse.analysis.Substring(0, [Math]::Min(100, $analysisResponse.analysis.Length))
            Write-Host "    → Extrait: $excerpt..." -ForegroundColor Gray
        }
    } catch {
        Test-Result -TestName "Analyse IA Générée" -Success $false -Details "Erreur: $_"
    }
} else {
    Write-Host "    ⚠ Test ignoré (pas de token d'authentification)" -ForegroundColor Yellow
}

# RÉSUMÉ
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "           RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n✅ Services Testés:" -ForegroundColor Green
Write-Host "   • Backend Spring Boot" -ForegroundColor White
Write-Host "   • Frontend Angular" -ForegroundColor White
Write-Host "   • Base de données PostgreSQL" -ForegroundColor White
Write-Host "   • Service ML Python" -ForegroundColor White
Write-Host "   • Ollama IA (qwen3:0.6b)" -ForegroundColor White

Write-Host "`n🔗 URLs Principales:" -ForegroundColor Cyan
Write-Host "   • Frontend:  http://localhost" -ForegroundColor White
Write-Host "   • Backend:   http://localhost:8080" -ForegroundColor White
Write-Host "   • Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   • ML Service: http://localhost:5000" -ForegroundColor White
Write-Host "   • Ollama:    http://localhost:11434" -ForegroundColor White

Write-Host "`n👤 Comptes de Test:" -ForegroundColor Cyan
Write-Host "   • Admin:      admin@tracabilite.ia / admin123" -ForegroundColor White
Write-Host "   • Validateur: validateur@tracabilite.ia / validateur123" -ForegroundColor White
Write-Host "   • Utilisateur: user@tracabilite.ia / user123" -ForegroundColor White

Write-Host "`n📝 Prochaines étapes:" -ForegroundColor Yellow
Write-Host "   1. Ouvrir http://localhost dans votre navigateur" -ForegroundColor White
Write-Host "   2. Se connecter avec admin@tracabilite.ia / admin123" -ForegroundColor White
Write-Host "   3. Explorer le tableau de bord" -ForegroundColor White
Write-Host "   4. Créer une décision et tester l'analyse IA" -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host ""
