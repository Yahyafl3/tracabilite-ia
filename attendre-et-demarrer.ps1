# Script pour attendre la fin du téléchargement Ollama et démarrer les services
# Utilisation: .\attendre-et-demarrer.ps1

Write-Host "=== Surveillance du téléchargement du modèle Ollama ===" -ForegroundColor Cyan
Write-Host ""

# Fonction pour vérifier si le modèle est téléchargé
function Test-ModelDownloaded {
    $output = docker exec tracabilite-ollama ollama list 2>&1
    return $output -match "qwen3:0.6b"
}

# Fonction pour obtenir la progression
function Get-DownloadProgress {
    $processes = docker exec tracabilite-ollama ps aux 2>&1 | Select-String "ollama pull"
    return $processes.Count
}

Write-Host "[1/3] Vérification de l'état du téléchargement..." -ForegroundColor Yellow
Write-Host ""

$iteration = 0
$maxIterations = 60  # 60 * 30 secondes = 30 minutes max

while (-not (Test-ModelDownloaded) -and $iteration -lt $maxIterations) {
    $iteration++
    $downloadingProcesses = Get-DownloadProgress
    
    if ($downloadingProcesses -gt 0) {
        Write-Host "⏳ Téléchargement en cours... ($downloadingProcesses processus actif(s))" -ForegroundColor Cyan
        Write-Host "   Vérification #$iteration - Prochaine vérification dans 30 secondes" -ForegroundColor Gray
        Write-Host "   Appuyez sur Ctrl+C pour annuler la surveillance" -ForegroundColor Gray
        Write-Host ""
        Start-Sleep -Seconds 30
    } else {
        Write-Host "⚠️  Aucun processus de téléchargement détecté" -ForegroundColor Yellow
        Write-Host "   Vérification du statut du modèle..." -ForegroundColor Gray
        Start-Sleep -Seconds 10
    }
}

Write-Host ""
if (Test-ModelDownloaded) {
    Write-Host "✅ Téléchargement terminé avec succès !" -ForegroundColor Green
    Write-Host ""
    
    # Afficher les modèles disponibles
    Write-Host "[2/3] Modèles Ollama disponibles:" -ForegroundColor Yellow
    docker exec tracabilite-ollama ollama list
    Write-Host ""
    
    # Demander confirmation avant de démarrer
    Write-Host "[3/3] Démarrage de tous les services Docker Compose" -ForegroundColor Yellow
    $response = Read-Host "Voulez-vous démarrer tous les services maintenant ? (O/N)"
    
    if ($response -eq "O" -or $response -eq "o" -or $response -eq "Y" -or $response -eq "y") {
        Write-Host ""
        Write-Host "🚀 Démarrage des services..." -ForegroundColor Cyan
        Write-Host ""
        
        # Arrêter les conteneurs existants si nécessaire
        docker-compose down 2>$null
        
        # Démarrer tous les services
        docker-compose up -d
        
        Write-Host ""
        Write-Host "✅ Services démarrés !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Vérification de l'état des conteneurs:" -ForegroundColor Yellow
        docker-compose ps
        
        Write-Host ""
        Write-Host "📋 Accès aux services:" -ForegroundColor Cyan
        Write-Host "   - Frontend:  http://localhost" -ForegroundColor White
        Write-Host "   - Backend:   http://localhost:8080" -ForegroundColor White
        Write-Host "   - Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor White
        Write-Host "   - ML Service: http://localhost:5000" -ForegroundColor White
        Write-Host "   - Ollama:    http://localhost:11434" -ForegroundColor White
        Write-Host ""
        Write-Host "Pour voir les logs: docker-compose logs -f" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "ℹ️  Démarrage annulé. Pour démarrer manuellement:" -ForegroundColor Yellow
        Write-Host "   docker-compose up -d" -ForegroundColor White
    }
} else {
    Write-Host "⏱️  Temps d'attente maximal atteint ($($maxIterations * 30) secondes)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Le téléchargement est peut-être toujours en cours." -ForegroundColor Yellow
    Write-Host "Vérifiez manuellement avec:" -ForegroundColor Yellow
    Write-Host "   docker exec tracabilite-ollama ollama list" -ForegroundColor White
    Write-Host ""
    Write-Host "Pour voir la progression:" -ForegroundColor Yellow
    Write-Host "   docker exec tracabilite-ollama ps aux | findstr 'ollama pull'" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Script terminé ===" -ForegroundColor Cyan
