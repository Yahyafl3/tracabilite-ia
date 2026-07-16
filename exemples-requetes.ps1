# Exemples de Requetes pour l'API Tracabilite IA
# Copier-coller ces JSON dans Swagger UI

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Exemples de Requetes - Swagger UI" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n[1] LOGIN - POST /api/auth/login" -ForegroundColor Yellow
Write-Host "Copier ce JSON dans Swagger:" -ForegroundColor Gray
$login = @"
{
  "email": "admin@tracabilite.ia",
  "motDePasse": "admin123"
}
"@
Write-Host $login -ForegroundColor White
Set-Clipboard -Value $login
Write-Host "  Copie dans le presse-papiers!" -ForegroundColor Green

Write-Host "`n[2] ANALYSE IA - Exemple Simple" -ForegroundColor Yellow
Write-Host "POST /api/ai/analyze-decision" -ForegroundColor Gray
$example1 = @"
{
  "prompt": "Analyser la decision : Approuver un credit de 30000 EUR sur 3 ans",
  "contexte": "Client avec bon historique, revenus stables 2500 EUR/mois"
}
"@
Write-Host $example1 -ForegroundColor White

Write-Host "`n[3] ANALYSE IA - Credit Important" -ForegroundColor Yellow
$example2 = @"
{
  "prompt": "Fournir une analyse detaillee de la decision : Approuver un pret immobilier de 200000 EUR sur 20 ans",
  "contexte": "Couple marie, 2 enfants, revenus combines 5000 EUR/mois, apport personnel 50000 EUR, premiere acquisition, pas d'incidents bancaires"
}
"@
Write-Host $example2 -ForegroundColor White

Write-Host "`n[4] ANALYSE IA - Refus de Credit" -ForegroundColor Yellow
$example3 = @"
{
  "prompt": "Analyser et justifier la decision : Refuser un credit de 15000 EUR",
  "contexte": "Client avec 3 incidents de paiement dans les 12 derniers mois, revenus irreguliers, taux d'endettement deja a 45%"
}
"@
Write-Host $example3 -ForegroundColor White

Write-Host "`n[5] ANALYSE IA - Investissement" -ForegroundColor Yellow
$example4 = @"
{
  "prompt": "Evaluer les risques de la decision : Investir 100000 EUR dans une startup tech",
  "contexte": "Startup IA, equipe experimentee, levee serie A, marche en croissance 25%/an, 5 concurrents majeurs, cash-flow negatif actuellement"
}
"@
Write-Host $example4 -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Instructions:" -ForegroundColor Yellow
Write-Host "1. Ouvrir Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "2. Se connecter avec l'exemple [1]" -ForegroundColor White
Write-Host "3. Copier le token recu" -ForegroundColor White
Write-Host "4. Cliquer 'Authorize' et entrer: Bearer <token>" -ForegroundColor White
Write-Host "5. Tester les exemples [2] a [5]" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nVoulez-vous ouvrir Swagger UI? (O/N): " -ForegroundColor Cyan -NoNewline
$response = Read-Host

if ($response -eq "O" -or $response -eq "o") {
    Write-Host "Ouverture de Swagger UI..." -ForegroundColor Green
    Start-Process "http://localhost:8080/swagger-ui.html"
    Write-Host "L'exemple de login [1] est dans votre presse-papiers!" -ForegroundColor Green
    Write-Host "Appuyez sur Ctrl+V dans Swagger pour le coller." -ForegroundColor Yellow
}
