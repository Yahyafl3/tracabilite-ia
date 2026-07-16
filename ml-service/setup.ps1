# Setup ML Service - Windows
# Usage: .\setup.ps1
# Depuis le dossier ml-service (pas ml-service\ml-service)

$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $here

Write-Host "=== ML Service Setup ===" -ForegroundColor Cyan
python --version

Write-Host "`n1. Installation des dependances (wheels binaires)..." -ForegroundColor Yellow
python -m pip install --upgrade pip
python -m pip install --only-binary=:all: -r requirements.txt
if ($LASTEXITCODE -ne 0) {
    Write-Host "Echec pip. Si Python 3.14, installez Python 3.11 ou reessayez." -ForegroundColor Red
    exit 1
}

Write-Host "`n2. Entrainement du modele..." -ForegroundColor Yellow
python train_model.py
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "`n3. Verification..." -ForegroundColor Yellow
python -c "from model_loader import load_model; s=load_model(); print('OK engine:', s.get('metadata',{}).get('modelType')); print('OK SHAP ready')"

Write-Host "`n=== Pret. Lancez: python app.py ===" -ForegroundColor Green
