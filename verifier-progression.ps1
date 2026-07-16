# Script pour vérifier rapidement la progression du téléchargement
# Utilisation: .\verifier-progression.ps1

Write-Host "=== État du téléchargement Ollama ===" -ForegroundColor Cyan
Write-Host ""

# Vérifier si le conteneur fonctionne
Write-Host "📦 Conteneur Ollama:" -ForegroundColor Yellow
$container = docker ps --filter "name=tracabilite-ollama" --format "{{.Status}}"
if ($container) {
    Write-Host "   ✅ En cours d'exécution: $container" -ForegroundColor Green
} else {
    Write-Host "   ❌ Non démarré" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Vérifier les processus de téléchargement
Write-Host "⬇️  Processus de téléchargement:" -ForegroundColor Yellow
$downloadProcesses = docker exec tracabilite-ollama ps aux 2>&1 | Select-String "ollama pull"
if ($downloadProcesses) {
    Write-Host "   ✅ Téléchargement actif:" -ForegroundColor Green
    $downloadProcesses | ForEach-Object { Write-Host "      $_" -ForegroundColor Gray }
} else {
    Write-Host "   ℹ️  Aucun téléchargement en cours" -ForegroundColor Yellow
}
Write-Host ""

# Vérifier les modèles installés
Write-Host "📚 Modèles installés:" -ForegroundColor Yellow
$models = docker exec tracabilite-ollama ollama list 2>&1
if ($models -match "qwen3:0.6b") {
    Write-Host "   ✅ qwen3:0.6b est installé !" -ForegroundColor Green
    docker exec tracabilite-ollama ollama list
} else {
    Write-Host "   ⏳ En attente... Le modèle n'est pas encore disponible" -ForegroundColor Yellow
}
Write-Host ""

# Suggestion
if ($models -notmatch "qwen3:0.6b" -and -not $downloadProcesses) {
    Write-Host "💡 Suggestion:" -ForegroundColor Cyan
    Write-Host "   Le téléchargement semble terminé ou arrêté." -ForegroundColor White
    Write-Host "   Si le modèle n'apparaît pas, relancez le téléchargement:" -ForegroundColor White
    Write-Host "   docker exec tracabilite-ollama ollama pull qwen3:0.6b" -ForegroundColor Gray
}
