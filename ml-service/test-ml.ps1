# Test ML Service - PowerShell Script
# Run: .\test-ml.ps1

Write-Host "🤖 Testing ML Service..." -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "1️⃣ Test Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5000/health" -Method Get
    Write-Host "✅ Status: $($response.status)" -ForegroundColor Green
    Write-Host "   Service: $($response.service)" -ForegroundColor Green
    Write-Host "   Models: $($response.models_loaded -join ', ')" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 2: List Domains
Write-Host "2️⃣ Test List Domains..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5000/domains" -Method Get
    Write-Host "✅ Total domains: $($response.total_domains)" -ForegroundColor Green
    Write-Host "   Available: credit, medical, insurance, hr, legal, education, general" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 3: Prediction - Credit APPROUVÉ
Write-Host "3️⃣ Test Prediction - Credit (should be APPROVED)..." -ForegroundColor Yellow
try {
    $body = @{
        domain = "credit"
        features = @{
            revenuMensuel = 15000
            dettesActuelles = 2000
            age = 35
            ancienneteEmploi = 5
        }
        description = "Demande de crédit immobilier"
        metadata = @{
            montantDemande = 500000
            typeCredit = "immobilier"
        }
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:5000/predict" -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "✅ Decision: $($response.decision)" -ForegroundColor Green
    Write-Host "   Contenu: $($response.contenu)" -ForegroundColor Green
    Write-Host "   Confiance: $($response.scoreConfiance)%" -ForegroundColor Green
    Write-Host "   Raison: $($response.raison)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 4: Prediction - Credit REFUSÉ
Write-Host "4️⃣ Test Prediction - Credit (should be REJECTED)..." -ForegroundColor Yellow
try {
    $body = @{
        domain = "credit"
        features = @{
            revenuMensuel = 5000
            dettesActuelles = 25000
            age = 50
            ancienneteEmploi = 1
        }
        description = "Demande de crédit"
        metadata = @{
            montantDemande = 100000
        }
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:5000/predict" -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "✅ Decision: $($response.decision)" -ForegroundColor Green
    Write-Host "   Contenu: $($response.contenu)" -ForegroundColor Green
    Write-Host "   Confiance: $($response.scoreConfiance)%" -ForegroundColor Green
    Write-Host "   Raison: $($response.raison)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 5: Prediction - Medical
Write-Host "5️⃣ Test Prediction - Medical..." -ForegroundColor Yellow
try {
    $body = @{
        domain = "medical"
        features = @{
            urgence = 0.8
            risque = 0.3
            disponibilite = 0.9
            priorite = 0.85
        }
        description = "Chirurgie cardiaque"
        metadata = @{
            patient = "PATIENT_001"
            medecin = "Dr. Ahmed"
        }
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:5000/predict" -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "✅ Decision: $($response.decision)" -ForegroundColor Green
    Write-Host "   Contenu: $($response.contenu)" -ForegroundColor Green
    Write-Host "   Confiance: $($response.scoreConfiance)%" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host "🎉 Tests terminés!" -ForegroundColor Cyan
