# Test ML Service - PowerShell Script
# Run: .\test-ml.ps1

Write-Host "Testing ML Service (Scikit-learn + SHAP)..." -ForegroundColor Cyan
Write-Host ""

$sample = @{
    amount = 25000
    monthlyIncome = 15000
    companyAgeYears = 5
    paymentIncidents = 0
    debtRatio = 0.22
    sector = "SERVICES"
}

# Test 1: Health Check
Write-Host "1. Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5000/health" -Method Get
    Write-Host "OK Status: $($response.status)" -ForegroundColor Green
    Write-Host "   Engine: $($response.engine)" -ForegroundColor Green
    Write-Host "   Ready: $($response.creditModelReady)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 2: Ready
Write-Host "2. Ready..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5000/ready" -Method Get
    Write-Host "OK Ready: $($response.ready)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 3: Model Info
Write-Host "3. Model Info..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5000/model/info" -Method Get
    Write-Host "OK Model: $($response.modelType)" -ForegroundColor Green
    Write-Host "   Explainability: $($response.explainability)" -ForegroundColor Green
    Write-Host "   Metrics: $($response.metrics | ConvertTo-Json -Compress)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 4: Predict
Write-Host "4. Predict (should be APPROUVER)..." -ForegroundColor Yellow
try {
    $body = $sample | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:5000/predict" -Method Post -Body $body -ContentType "application/json"
    Write-Host "OK Decision: $($response.decision)" -ForegroundColor Green
    Write-Host "   Confiance: $($response.scoreConfiance)%" -ForegroundColor Green
    Write-Host "   Source: $($response.explanationSource)" -ForegroundColor Green
    Write-Host "   Factors: $($response.factors.Count)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 5: Explain
Write-Host "5. Explain..." -ForegroundColor Yellow
try {
    $body = $sample | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:5000/explain" -Method Post -Body $body -ContentType "application/json"
    Write-Host "OK Source: $($response.explanationSource)" -ForegroundColor Green
    Write-Host "   Top factor: $($response.factors[0].name)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host "Tests termines." -ForegroundColor Cyan
