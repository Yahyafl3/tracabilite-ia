# 🤖 ML Service - Machine Learning pour Décisions Automatiques

## 📋 Description

Service Python Flask qui utilise Machine Learning (RandomForest) pour générer des décisions automatiques de crédit basées sur les données du client.

## 🎯 Fonctionnalités

- **Prédiction ML**: Analyse automatique des demandes de crédit
- **Modèle RandomForest**: Entraîné avec données simulées
- **Score de confiance**: Probabilité de la décision
- **Facteurs d'importance**: Quels critères influencent la décision
- **Réentraînement**: API pour améliorer le modèle avec nouvelles données

## 🏗️ Architecture

```
Request → Flask API → RandomForest Model → Response
                          ↓
                    PostgreSQL
                    (via Backend)
```

## 📡 Endpoints

### 1. Health Check
```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "ML Decision Service",
  "model_loaded": true,
  "timestamp": "2026-07-08T10:30:00"
}
```

### 2. Prédiction ML
```http
POST /predict
Content-Type: application/json

{
  "revenuMensuel": 15000,
  "dettesActuelles": 2000,
  "age": 35,
  "ancienneteEmploi": 5,
  "montantDemande": 50000
}
```

**Response:**
```json
{
  "decision": "APPROUVER",
  "contenu": "Crédit de 50000 DH - APPROUVÉ",
  "contexte": "Revenu mensuel: 15000 DH, Dettes: 2000 DH, Âge: 35 ans, Ancienneté: 5 ans, Montant demandé: 50000 DH",
  "scoreConfiance": 87.5,
  "raison": "Profil financier solide, capacité de remboursement suffisante",
  "probabilities": {
    "refuser": 12.5,
    "approuver": 87.5
  },
  "factors": [
    {
      "name": "Revenu mensuel",
      "importance": 0.45
    },
    {
      "name": "Dettes actuelles",
      "importance": 0.30
    },
    {
      "name": "Ancienneté emploi",
      "importance": 0.15
    },
    {
      "name": "Âge",
      "importance": 0.10
    }
  ],
  "timestamp": "2026-07-08T10:30:00"
}
```

### 3. Réentraînement
```http
POST /train
Content-Type: application/json

{
  "data": [
    {
      "revenuMensuel": 15000,
      "dettes": 2000,
      "age": 35,
      "anciennete": 5,
      "approuve": true
    },
    {
      "revenuMensuel": 8000,
      "dettes": 20000,
      "age": 45,
      "anciennete": 2,
      "approuve": false
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Modèle réentraîné avec 2 exemples",
  "timestamp": "2026-07-08T10:30:00"
}
```

## 🧮 Modèle ML

### Algorithme: RandomForest Classifier
- **100 arbres de décision**
- **Profondeur max: 5**
- **Features**: 4 (revenu, dettes, âge, ancienneté)
- **Target**: Binaire (0=Refuser, 1=Approuver)

### Features utilisées:
1. **Revenu mensuel** (DH) - Importance: ~45%
2. **Dettes actuelles** (DH) - Importance: ~30%
3. **Âge** (années) - Importance: ~10%
4. **Ancienneté emploi** (années) - Importance: ~15%

### Logique de décision:
- **APPROUVER**: Revenu > 12000 DH, Dettes < 10000 DH, Ancienneté > 3 ans
- **REFUSER**: Revenu < 10000 DH, Dettes > 15000 DH, Ancienneté < 2 ans
- **Zone grise**: Analysée par le modèle avec score de confiance

## 🐳 Docker

### Build
```bash
docker build -t tracabilite-ml-service .
```

### Run standalone
```bash
docker run -p 5000:5000 tracabilite-ml-service
```

### Avec docker-compose
```bash
docker-compose up ml-service
```

## 🧪 Tests

### Test direct (sans Backend)
```bash
# Windows
curl -X POST http://localhost:5000/predict ^
  -H "Content-Type: application/json" ^
  -d "{\"revenuMensuel\":15000,\"dettesActuelles\":2000,\"age\":35,\"ancienneteEmploi\":5,\"montantDemande\":50000}"

# Linux/Mac
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{"revenuMensuel":15000,"dettesActuelles":2000,"age":35,"ancienneteEmploi":5,"montantDemande":50000}'
```

### Test via Backend
```bash
curl -X POST http://localhost:8080/api/ml/analyze ^
  -H "Content-Type: application/json" ^
  -d "{\"revenuMensuel\":15000,\"dettesActuelles\":2000,\"age\":35,\"ancienneteEmploi\":5,\"montantDemande\":50000}"
```

## 📦 Dépendances

- **Flask**: Web framework
- **Flask-CORS**: Cross-Origin Resource Sharing
- **scikit-learn**: Machine Learning
- **joblib**: Model persistence
- **numpy**: Calculs numériques

## 🔧 Configuration

### Variables d'environnement
- `FLASK_ENV`: development/production
- `MODEL_PATH`: Chemin du modèle (default: credit_decision_model.pkl)

### Port
- **Port**: 5000 (configurable dans app.py)

## 📊 Données d'entraînement

Le modèle est pré-entraîné avec 12 exemples simulés:

| Revenu | Dettes | Âge | Ancienneté | Décision |
|--------|--------|-----|------------|----------|
| 15000  | 0      | 35  | 5          | ✅ Approuver |
| 20000  | 2000   | 30  | 8          | ✅ Approuver |
| 25000  | 5000   | 28  | 10         | ✅ Approuver |
| 8000   | 20000  | 45  | 2          | ❌ Refuser   |
| 6000   | 15000  | 50  | 1          | ❌ Refuser   |
| 9000   | 18000  | 48  | 3          | ❌ Refuser   |

## 🚀 Intégration avec Backend

Le Backend Java communique avec ce service via HTTP:

```java
// Backend → ML Service
RestTemplate restTemplate = new RestTemplate();
String url = "http://ml-service:5000/predict";
MLPredictionResponse response = restTemplate.postForEntity(
    url, 
    creditAnalysisRequest, 
    MLPredictionResponse.class
).getBody();
```

## 🔐 Sécurité

⚠️ **Note**: Version actuelle sans authentification (pour développement)

Pour production, ajouter:
- API Key authentication
- Rate limiting
- Input validation stricte
- HTTPS

## 📈 Améliorations futures

1. **Modèle plus complexe**: Neural Networks, Gradient Boosting
2. **Plus de features**: Historique bancaire, profession, éducation
3. **Base de données**: Sauvegarder prédictions dans PostgreSQL
4. **Monitoring**: Logs, metrics, alertes
5. **A/B Testing**: Comparer plusieurs modèles
6. **Explainability**: SHAP values pour expliquer décisions

## 📞 Support

- **Yahya**: Frontend (Angular)
- **Badderdine**: Backend (Spring Boot + ML Integration)

---

Made with ❤️ for Traçabilité IA Project
