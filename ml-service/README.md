# ML Service — Traçabilité IA

Service Flask de prédiction crédit avec **Scikit-learn** et explicabilité **SHAP**.

## Avertissement dataset

**Aucun dataset réel n'est disponible dans ce projet.**

Le fichier `data/synthetic_credit_dataset.csv` est **synthétique** et généré uniquement pour le prototype académique. Les métriques affichées dans `artifacts/metadata.json` sont mesurées sur un hold-out de ce jeu synthétique — elles **ne doivent pas** être interprétées comme des performances de production.

## Architecture

```text
ml-service/
├── app.py                      # API Flask (/predict, /explain, /ready, /model/info)
├── config.py                   # Constantes et chemins
├── dataset_service.py          # Génération / nettoyage dataset
├── feature_validator.py        # Validation des features entrantes
├── train_model.py              # Entraînement + évaluation + sauvegarde
├── model_loader.py             # Chargement modèle + SHAP LinearExplainer
├── prediction_service.py       # Prédiction crédit
├── explainability_service.py   # Facteurs SHAP
├── risk_service.py             # Niveau de risque métier
├── data/
│   └── synthetic_credit_dataset.csv
└── artifacts/
    ├── model.joblib
    └── metadata.json
```

## Features

| Feature | Type | Description |
|---------|------|-------------|
| `amount` | numérique | Montant demandé |
| `monthlyIncome` | numérique | Revenu mensuel |
| `companyAgeYears` | numérique | Ancienneté entreprise (années) |
| `paymentIncidents` | numérique | Incidents de paiement |
| `debtRatio` | numérique | Ratio d'endettement [0, 1] |
| `sector` | catégoriel | SERVICES, INDUSTRIE, COMMERCE, TECH, AGRICULTURE |

**Cible :** `approved` (0 = REJETER, 1 = APPROUVER)

## Modèle

- **Algorithme :** `LogisticRegression` (interprétable)
- **Pipeline :** imputation → `StandardScaler` (num) + `OneHotEncoder` (sector) → classifieur
- **Explicabilité :** `SHAP LinearExplainer` sur features transformées
- **Moteur supprimé :** `WeightedRuleEngine` / `MARGINAL_CONTRIBUTION` (plus de fallback)

## Démarrage

### Windows (recommandé)

```powershell
cd c:\Users\yahya\Downloads\tracabilite-ia\ml-service
.\setup.ps1
python app.py
```

> **Python 3.14** : utilisez `--only-binary=:all:` (inclus dans `setup.ps1`).  
> Ne pas lancer `cd ml-service` si vous êtes déjà dans ce dossier.

### Manuel

```bash
cd ml-service
python -m pip install --only-binary=:all: -r requirements.txt
python train_model.py   # génère artifacts/model.joblib + metadata.json
python app.py           # http://localhost:5000
```

> Si un ancien container Docker tourne encore (`tracabilite-ml-service`), arrêtez-le :
> `docker stop tracabilite-ml-service`

## Endpoints

| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/health` | Santé du service |
| GET | `/ready` | Modèle chargé et prêt |
| GET | `/model/info` | Métadonnées + métriques mesurées |
| POST | `/predict` | Prédiction + facteurs SHAP |
| POST | `/explain` | Explication SHAP seule |

### Exemple `/predict`

```json
{
  "amount": 25000,
  "monthlyIncome": 15000,
  "companyAgeYears": 5,
  "paymentIncidents": 0,
  "debtRatio": 0.22,
  "sector": "SERVICES"
}
```

Réponse (contrat inchangé pour Spring Boot) :

```json
{
  "decision": "APPROUVER",
  "scoreConfiance": 78.5,
  "explanationSource": "SHAP",
  "factors": [ ... ],
  "explanation": { "explainedClass": "APPROUVER", ... }
}
```

## Interprétation SHAP (cohérence scientifique)

- **Classe expliquée :** `APPROUVER` (index sklearn `1`, `classes_ = [0=REJETER, 1=APPROUVER]`).
- **Formule SHAP (LogisticRegression) :** `shapValue_j ≈ coef_j × (x_j_transformé − E[x_j_transformé])`.
- **Formule contribution % :** `contributionPercent_i = |shapValue_i| / Σ_j|shapValue_j| × 100`.
- **Identité log-odds :** `Σ shapValue_j + expected_value ≈ decision_function(x)` (log-odds APPROUVER).

### Pourquoi un signe peut sembler « inversé » ?

Les SHAP sont calculés sur les **features transformées** (StandardScaler + OneHot), par rapport à la **moyenne du dataset d'entraînement**.

Exemple : `paymentIncidents = 1` avec SHAP **positif** vers APPROUVER signifie que **1 incident est en dessous de la moyenne** du dataset (~2 incidents) ; avec un coefficient **négatif**, l'écart `(x − moyenne) < 0` produit un produit positif. Ce n'est **pas** une inversion de signe.

### Limites du dataset synthétique

- Label généré via ratio `monthlyIncome/amount` → `monthlyIncome` seul corrèle faiblement avec `approved`.
- Données 100 % synthétiques — prototype académique uniquement.

### Tests de cohérence

```bash
python -m pytest test_shap_coherence.py -v
```

## Docker

```bash
docker compose build ml-service
docker compose up -d ml-service
```

L'image exécute `train_model.py` au build pour produire `artifacts/model.joblib`.
