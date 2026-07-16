# =============================================================================
# Nettoyage DEV des décisions de test — Traçabilité IA
# =============================================================================
# Ne touche PAS aux volumes Docker, utilisateurs, systeme_ia, modèle ML, Ollama.
#
# Usage (depuis la racine du projet) :
#   .\scripts\dev-clean-decisions.ps1
#   .\scripts\dev-clean-decisions.ps1 -Backup
# =============================================================================

param(
    [switch]$Backup,
    [string]$Container = "tracabilite-postgres",
    [string]$DbUser = "tracabilite",
    [string]$DbName = "tracabilite_ia"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$SqlFile = Join-Path $Root "backend\scripts\clean-test-decisions.sql"
$BackupDir = Join-Path $Root "backend\scripts\backups"

function Invoke-Psql {
    param([string]$Query)
    docker exec $Container psql -U $DbUser -d $DbName -t -A -c $Query
}

function Show-Counts {
    param([string]$Title)
    Write-Host ""
    Write-Host "=== $Title ===" -ForegroundColor Cyan
    docker exec $Container psql -U $DbUser -d $DbName -c @"
SELECT table_name, row_count FROM (
  SELECT 'validation_action' AS table_name, COUNT(*)::bigint AS row_count FROM validation_action
  UNION ALL SELECT 'decision', COUNT(*) FROM decision
  UNION ALL SELECT 'explanation_factor', COUNT(*) FROM explanation_factor
  UNION ALL SELECT 'appel_ia', COUNT(*) FROM appel_ia
  UNION ALL SELECT 'systeme_ia', COUNT(*) FROM systeme_ia
  UNION ALL SELECT 'utilisateur', COUNT(*) FROM utilisateur
  UNION ALL SELECT 'trace_capture_job', COUNT(*) FROM trace_capture_job
) t ORDER BY 1;
"@
}

Write-Host "Nettoyage des decisions de test" -ForegroundColor Yellow
Write-Host "Conteneur : $Container | Base : $DbName"
Write-Host ""
Write-Host "Tables concernees par la suppression :" -ForegroundColor White
Write-Host "  - explanation_factor  (facteurs SHAP, FK -> decision)"
Write-Host "  - decision            (decisions + chaine hash decision_precedente_id)"
Write-Host "  - appel_ia            (historique appels Ollama des tests)"
Write-Host ""
Write-Host "Tables conservees :" -ForegroundColor Green
Write-Host "  - utilisateur, systeme_ia, trace_capture_job"

Show-Counts -Title "COMPTAGE AVANT SUPPRESSION"

if ($Backup) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
    $backupFile = Join-Path $BackupDir "decisions-backup-$timestamp.sql"
    Write-Host ""
    Write-Host "Sauvegarde des tables concernees -> $backupFile" -ForegroundColor Cyan
    docker exec $Container pg_dump -U $DbUser -d $DbName `
        --data-only `
        --table=decision `
        --table=explanation_factor `
        --table=appel_ia `
        | Out-File -FilePath $backupFile -Encoding utf8
    Write-Host "Sauvegarde terminee." -ForegroundColor Green
}

Write-Host ""
Write-Host "Commande executee :" -ForegroundColor Cyan
Write-Host "  Get-Content `"$SqlFile`" | docker exec -i $Container psql -U $DbUser -d $DbName"
Write-Host ""

Get-Content $SqlFile -Raw | docker exec -i $Container psql -U $DbUser -d $DbName

Show-Counts -Title "COMPTAGE APRES SUPPRESSION"

Write-Host ""
Write-Host "Verification API (optionnelle) :" -ForegroundColor Cyan
Write-Host "  GET http://localhost/api/decisions  (totalElements doit etre 0)"
Write-Host ""
