# Restore PostgreSQL Docker local — confirmation obligatoire
param(
  [Parameter(Mandatory = $true)][string]$BackupFile,
  [string]$EnvName = "local",
  [string]$Container = "tracabilite-postgres",
  [string]$DbName = "tracabilite_ia",
  [string]$DbUser = "tracabilite"
)

$ErrorActionPreference = "Stop"
if ($EnvName -in @("neon", "production")) {
  throw "Restauration refusee pour EnvName=$EnvName"
}
if (-not (Test-Path $BackupFile)) { throw "Fichier introuvable: $BackupFile" }

Write-Host "Environment: $EnvName"
Write-Host "Container:   $Container"
Write-Host "Database:    $DbName"
Write-Host "Backup:      $BackupFile"
$confirm = Read-Host "Confirmer restauration DESTRUCTIVE locale (oui/non)"
if ($confirm -ne "oui") {
  Write-Host "Annule."
  exit 0
}

Get-Content -Raw $BackupFile | docker exec -i $Container psql -U $DbUser -d $DbName
Write-Host "Restore termine."
