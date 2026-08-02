# API multidomain

## Flask (ml-service)

| Méthode | Path | Description |
|---------|------|-------------|
| GET | `/health` | Liveness |
| GET | `/ready` | Prêt + domaines |
| GET | `/models` | Liste modèles |
| GET | `/models/{domain}` | Metadata |
| POST | `/predict/credit` | Prédiction crédit enrichie |
| POST | `/predict/medical` | Prédiction médicale |
| POST | `/predict/education` | Prédiction éducation |
| POST | `/predict` | **Legacy** crédit SHAP |

## Spring Boot

| Méthode | Path | Rôles |
|---------|------|-------|
| POST | `/api/decisions/credit` | USER, ADMIN |
| POST | `/api/decisions/medical` | USER, ADMIN |
| POST | `/api/decisions/education` | USER, ADMIN |
| POST | `/api/decisions/{id}/submit` | USER, ADMIN, VALIDATOR |
| POST | `/api/decisions/{id}/validate` | VALIDATOR (+ domain) |
| POST | `/api/decisions/{id}/request-review` | VALIDATOR |
| GET | `/api/decisions/domain/{domain}` | Authentifié |
| GET | `/api/decisions/pending-validation` | VALIDATOR, ADMIN |
| POST | `/api/decisions/analyze` | **Legacy** |

## Règles de validation

- Auteur ≠ validateur
- ADMIN ≠ validateur métier automatique
- Désaccord IA → justification obligatoire + `accordAvecIa=false` + audit
