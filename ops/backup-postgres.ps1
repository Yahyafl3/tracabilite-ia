# Backup PostgreSQL Docker local (Windows)
param(
  [string]$EnvName = "local",
  [string]$Container = "tracabilite-postgres",
  [string]$DbName = "tracabilite_ia",
  [string]$DbUser = "tracabilite",
  [string]$BackupDir = ".\backups"
)

$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
$stamp = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$outFile = Join-Path $BackupDir "$EnvName-$DbName-$stamp.sql"

Write-Host "Environment: $EnvName"
Write-Host "Container:   $Container"
Write-Host "Database:    $DbName"
Write-Host "Output:      $outFile"

$running = docker ps --format "{{.Names}}" | Where-Object { $_ -eq $Container }
if (-not $running) { throw "Container $Container not running" }

docker exec -t $Container pg_dump -U $DbUser -d $DbName --no-owner --no-acl | Set-Content -Path $outFile -Encoding utf8
Write-Host "Backup OK: $outFile"
Get-Item $outFile | Format-List Name, Length, LastWriteTime
