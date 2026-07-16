# Système de Traçabilité des Décisions Assistées par IA

**Projet Fin d'Études 2025/2026**

Système complet de traçabilité des décisions assistées par intelligence artificielle avec validation humaine, analyse ML et blockchain pour garantir l'intégrité.

---

## 🚀 Démarrage Rapide

### Prérequis

- **Docker Desktop** (Windows/Mac) ou Docker + Docker Compose (Linux)
- **8 GB RAM** minimum (16 GB recommandé)
- **20 GB** d'espace disque libre
- **Java 21** (pour compilation locale)
- **Node.js 18+** (pour développement frontend)

### Installation

```powershell
# 1. Cloner le projet
git clone <url-du-repo>
cd tracabilite-ia

# 2. Copier .env.example vers .env et renseigner OPENROUTER_API_KEY

# 3. Démarrer tous les services
docker compose up -d

# 4. Accéder à l'application
# Frontend : http://localhost:80
# Backend API : http://localhost:8080
# Swagger : http://localhost:8080/swagger-ui.html
```

### Comptes par Défaut

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| admin@tracabilite.ia | admin123 | ADMIN |
| validator@tracabilite.ia | validator123 | VALIDATOR |
| user@tracabilite.ia | user123 | USER |

---

## 📦 Architecture

### Stack Technique

**Backend**
- Java 21
- Spring Boot 3.4.1
- Spring Data JPA
- Spring Security (JWT)
- PostgreSQL 16
- Swagger/OpenAPI

**Frontend**
- Angular 17+ (ou React - à vérifier)
- Material UI
- TypeScript
- Nginx

**IA & ML**
- OpenRouter (Llama 3.3 70B, Gemma 4 31B, GPT-OSS 20B)
- FastAPI (service ML)
- Scikit-learn LogisticRegression
- SHAP (explainabilité)

**Infrastructure**
- Docker & Docker Compose
- PostgreSQL (données métier)
- MinIO (stockage fichiers) - À venir
- Mailpit (emails dev) - À venir
- OpenSearch (logs) - À venir

### Architecture Système

```
┌─────────────┐
│   Frontend  │ (Angular/React)
│  Port 80    │
└──────┬──────┘
       │ HTTP/JWT
       ↓
┌─────────────┐
│   Backend   │ (Spring Boot)
│  Port 8080  │
└──────┬──────┘
       │
       ├─→ PostgreSQL (Port 5432)
       │
       ├─→ OpenRouter API (3 agents LLM)
       │
       └─→ ML Service (Port 5000) ──→ LogisticRegression + SHAP
```

> Migration depuis Ollama : voir [`docs/MIGRATION_OLLAMA_OPENROUTER.md`](./docs/MIGRATION_OLLAMA_OPENROUTER.md)

---

## 🤖 Installation Ollama

Ollama fournit l'IA générative pour analyser les décisions.

### Option 1 : Ollama Natif Windows (Recommandé ✅)

```powershell
# 1. Télécharger depuis https://ollama.com/download/windows
# 2. Installer le .exe
# 3. Télécharger le modèle
ollama pull qwen3:4b

# 4. Vérifier
ollama list
ollama run qwen3:4b "Bonjour"

# 5. Le backend se connecte automatiquement via host.docker.internal:11434
```

### Option 2 : Ollama dans Docker

```powershell
# 1. Corriger les problèmes DNS si nécessaire
.\fix-docker-dns.ps1

# 2. Lancer le téléchargement (en arrière-plan)
docker exec tracabilite-ollama ollama pull qwen3:0.6b

# 3. Vérifier la progression
.\verifier-progression.ps1

# 4. Attendre et démarrer automatiquement les services
.\attendre-et-demarrer.ps1

# OU manuellement :
docker compose --profile ollama up -d ollama
docker exec -it tracabilite-ollama ollama pull qwen3:0.6b
docker exec -it tracabilite-ollama ollama list

# Une fois le téléchargement terminé :
docker compose up -d
```

**Scripts disponibles** :
- `fix-docker-dns.ps1` : Corrige les problèmes DNS et recrée le conteneur Ollama
- `verifier-progression.ps1` : Vérifie rapidement l'état du téléchargement
- `attendre-et-demarrer.ps1` : Attend la fin du téléchargement et démarre tous les services
- `installer-ollama.ps1` : Installation automatique complète (ancien script)

**Documentation complète** : Voir [`GUIDE_INSTALLATION_OLLAMA_DOCKER.md`](./GUIDE_INSTALLATION_OLLAMA_DOCKER.md)

**Dépannage DNS** : Si le téléchargement échoue, consultez les solutions dans le guide d'installation

---

## 🏗️ Structure du Projet

```
tracabilite-ia/
├── backend/                # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pfa/tracabilite_ia/
│   │   │   │   ├── ai/              # Intégration Ollama
│   │   │   │   ├── audit/           # Traçabilité
│   │   │   │   ├── config/          # Configuration
│   │   │   │   ├── controller/      # API REST
│   │   │   │   ├── dto/             # Objets de transfert
│   │   │   │   ├── entities/        # Modèle de données
│   │   │   │   ├── enumeration/     # Enums
│   │   │   │   ├── exception/       # Gestion erreurs
│   │   │   │   ├── filter/          # Filtres HTTP
│   │   │   │   ├── jwt/             # Authentification
│   │   │   │   ├── repository/      # Accès données
│   │   │   │   ├── service/         # Logique métier
│   │   │   │   └── util/            # Utilitaires
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                    # Tests unitaires
│   ├── Dockerfile
│   ├── pom.xml
│   └── settings.xml
│
├── frontend/               # Application web
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── ml-service/            # Service ML (FastAPI)
│   ├── app/
│   ├── models/
│   ├── tests/
│   ├── requirements.txt
│   └── Dockerfile
│
├── docs/                  # Documentation
│   └── OLLAMA.md
│
├── docker-compose.yml     # Orchestration
├── .env.example           # Variables d'environnement (template)
├── README.md              # Ce fichier
├── GUIDE_INSTALLATION_OLLAMA_DOCKER.md
├── DEPANNAGE_DNS_OLLAMA.md
├── RAPPORT_OLLAMA_INTEGRATION.md
└── installer-ollama.ps1   # Script automatisé
```

---

## 🔑 Fonctionnalités Implémentées

### ✅ PROMPT 1 : Authentification (À venir - Keycloak)
- [ ] Intégration Keycloak
- [ ] Rôles : ADMIN, VALIDATOR, AUDITOR, USER
- [ ] Authorization Code Flow avec PKCE

### ✅ PROMPT 2 : IA Générative avec Ollama

- [x] Architecture propre avec interface `AIProvider`
- [x] `OllamaClient` avec RestClient Spring
- [x] Retry automatique et gestion d'erreurs
- [x] Configuration externalisée (modèle, timeouts)
- [x] Templates de prompts structurés
- [x] Sanitisation des données sensibles
- [x] Traçabilité complète (table `appel_ia`)
- [x] Correlation ID
- [x] Endpoint `/api/ai/analyze-decision` sécurisé
- [x] Tests unitaires avec MockWebServer
- [x] Docker Compose avec profil ollama
- [x] Documentation complète

**Documentation** : [`docs/OLLAMA.md`](./docs/OLLAMA.md) et [`RAPPORT_OLLAMA_INTEGRATION.md`](./RAPPORT_OLLAMA_INTEGRATION.md)

### 🚧 PROMPT 3 : Service ML avec FastAPI, Scikit-learn et SHAP
- [ ] Microservice FastAPI
- [ ] Modèle de classification
- [ ] Explainabilité SHAP
- [ ] Endpoint `/predict` et `/explain`
- [ ] Intégration avec Spring Boot

### 🚧 PROMPT 4 : Stockage avec MinIO
- [ ] Configuration MinIO
- [ ] Upload/Download de fichiers
- [ ] Métadonnées dans PostgreSQL
- [ ] Checksum SHA-256

### 🚧 PROMPT 5 : Notifications avec Mailpit
- [ ] Spring Mail
- [ ] Mailpit pour développement
- [ ] Templates HTML
- [ ] Envoi asynchrone

### 🚧 PROMPT 6 : Rapports PDF avec Apache PDFBox
- [ ] Génération PDF
- [ ] QR Codes
- [ ] Rapports d'audit

### 🚧 PROMPT 7 : Logs avec OpenSearch
- [ ] OpenSearch + Dashboards
- [ ] Logs structurés JSON
- [ ] Correlation ID global
- [ ] Filtrage des secrets

### 🚧 PROMPT 8 : Documentation avec Swagger/OpenAPI
- [x] Springdoc OpenAPI
- [x] Swagger UI
- [ ] Documentation complète de toutes les routes

### 🚧 PROMPT 9 : Docker Compose Global
- [x] PostgreSQL
- [x] Backend
- [x] Frontend
- [x] ML Service
- [x] Ollama (optionnel)
- [ ] Keycloak + PostgreSQL
- [ ] MinIO
- [ ] Mailpit
- [ ] OpenSearch + Dashboards
- [ ] Healthchecks complets
- [ ] Variables d'environnement

---

## 🧪 Tests

### Backend

```powershell
cd backend

# Tous les tests
./mvnw.cmd test

# Tests IA uniquement
./mvnw.cmd test -Dtest=*Ollama*,*AI*

# Tests spécifiques
./mvnw.cmd test -Dtest=OllamaClientTest
./mvnw.cmd test -Dtest=AIServiceImplTest
./mvnw.cmd test -Dtest=SensitiveDataSanitizerTest
```

### Frontend

```powershell
cd frontend

# Tests unitaires
npm test

# Tests e2e
npm run e2e
```

### ML Service

```powershell
cd ml-service

# Tests Python
pytest

# Avec couverture
pytest --cov=app tests/
```

---

## 📊 API Documentation

### Swagger UI

Une fois le backend démarré, accédez à :

**URL** : http://localhost:8080/swagger-ui.html

### Endpoints Principaux

#### Authentification

```http
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
```

#### Décisions

```http
GET    /api/decisions
POST   /api/decisions
GET    /api/decisions/{id}
PUT    /api/decisions/{id}
DELETE /api/decisions/{id}
GET    /api/decisions/my
```

#### Validation

```http
GET  /api/validation/pending
POST /api/decisions/{id}/approve
POST /api/decisions/{id}/reject
```

#### IA Générative

```http
POST /api/ai/analyze-decision
```

#### Audit

```http
GET /api/audit/logs
GET /api/audit/user/{userId}
```

---

## 🔧 Configuration

### Variables d'Environnement

Créez un fichier `.env` à la racine (voir `.env.example`) :

```env
# Base de données
POSTGRES_DB=tracabilite_ia
POSTGRES_USER=tracabilite
POSTGRES_PASSWORD=tracabilite123

# JWT
JWT_SECRET=your-secret-key-here-min-256-bits
JWT_EXPIRATION_MS=86400000

# Ollama
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:4b

# ML Service
ML_SERVICE_URL=http://ml-service:5000

# Frontend
FRONTEND_URL=http://localhost:80
BACKEND_URL=http://localhost:8080
```

### application.properties

Le fichier `backend/src/main/resources/application.properties` :

```properties
# Base de données
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/tracabilite_ia}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:tracabilite}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:tracabilite123}

# JPA
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL:true}

# JWT
jwt.secret=${JWT_SECRET:tracabilite-ia-super-secret-key-please-change-in-production}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

# Ollama
ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
ollama.model=${OLLAMA_MODEL:qwen3:4b}
ollama.connect-timeout-ms=${OLLAMA_CONNECT_TIMEOUT_MS:5000}
ollama.read-timeout-ms=${OLLAMA_READ_TIMEOUT_MS:120000}
ollama.max-retries=${OLLAMA_MAX_RETRIES:2}
```

---

## 🐳 Docker

### Commandes Utiles

```powershell
# Démarrer tous les services
docker compose up -d

# Démarrer avec Ollama
docker compose --profile ollama up -d

# Voir les logs
docker compose logs -f backend
docker compose logs -f frontend

# Redémarrer un service
docker compose restart backend

# Arrêter tout
docker compose down

# Arrêter et supprimer les volumes
docker compose down -v

# Reconstruire les images
docker compose build --no-cache
docker compose up -d
```

### Healthchecks

```powershell
# Vérifier le statut de tous les conteneurs
docker compose ps

# Vérifier le healthcheck d'un service
docker inspect tracabilite-backend --format='{{.State.Health.Status}}'
docker inspect tracabilite-ollama --format='{{.State.Health.Status}}'
```

---

## 🛠️ Développement Local

### Backend

```powershell
cd backend

# Compiler
./mvnw.cmd clean compile

# Lancer
./mvnw.cmd spring-boot:run

# Tests
./mvnw.cmd test

# Package
./mvnw.cmd clean package -DskipTests
```

### Frontend

```powershell
cd frontend

# Installer les dépendances
npm install

# Serveur de développement
npm run dev

# Build production
npm run build

# Lint
npm run lint
```

---

## 📝 Notes Importantes

### Java Version

Le projet nécessite **Java 21**. Si vous avez Java 17 :

```powershell
# Télécharger Java 21
# https://adoptium.net/temurin/releases/?version=21

# Vérifier
java -version
```

### Problèmes DNS Ollama

Si le téléchargement des modèles échoue dans Docker :

1. **Solution recommandée** : Installer Ollama nativement sur Windows
2. Voir [`DEPANNAGE_DNS_OLLAMA.md`](./DEPANNAGE_DNS_OLLAMA.md)

### Certificats SSL Maven

Si Maven ne peut pas télécharger les dépendances :

```powershell
# Utiliser le mirror HTTP
./mvnw.cmd clean compile -s settings.xml
```

---

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/ma-fonctionnalite`)
3. Commit (`git commit -am 'Ajout de ma fonctionnalité'`)
4. Push (`git push origin feature/ma-fonctionnalite`)
5. Créer une Pull Request

---

## 📚 Documentation

- [Guide Installation Ollama Docker](./GUIDE_INSTALLATION_OLLAMA_DOCKER.md)
- [Dépannage DNS Ollama](./DEPANNAGE_DNS_OLLAMA.md)
- [Rapport Intégration Ollama](./RAPPORT_OLLAMA_INTEGRATION.md)
- [Documentation Ollama API](./docs/OLLAMA.md)
- [Swagger/OpenAPI](http://localhost:8080/swagger-ui.html)

---

## 🐛 Dépannage

### Le backend ne démarre pas

```powershell
# Vérifier PostgreSQL
docker compose ps postgres

# Voir les logs
docker compose logs backend

# Redémarrer
docker compose restart backend
```

### Le frontend ne se charge pas

```powershell
# Vérifier Nginx
docker compose ps frontend

# Voir les logs
docker compose logs frontend

# Reconstruire
docker compose build frontend
docker compose up -d frontend
```

### Ollama indisponible

```powershell
# Vérifier le conteneur
docker ps | Select-String "ollama"

# Vérifier les logs
docker logs tracabilite-ollama

# Tester la connectivité depuis le backend
docker exec tracabilite-backend curl http://ollama:11434/api/tags
```

---

## 📞 Support

Pour toute question ou problème :

1. Consulter la documentation dans `/docs`
2. Vérifier les issues GitHub
3. Contacter l'équipe de développement

---

## 📄 Licence

Ce projet est développé dans le cadre d'un Projet de Fin d'Études (PFA) 2025/2026.

---

## 🎯 Roadmap

### Phase 1 (Terminée ✅)
- [x] Architecture backend Spring Boot
- [x] API REST avec Spring Security (JWT)
- [x] Frontend de base
- [x] Intégration Ollama pour IA générative
- [x] Docker Compose initial

### Phase 2 (En cours 🚧)
- [ ] Service ML avec FastAPI et Scikit-learn
- [ ] Explainabilité SHAP
- [ ] Tests d'intégration

### Phase 3 (À venir 📅)
- [ ] Intégration Keycloak (OAuth2/OpenID)
- [ ] Stockage MinIO
- [ ] Génération PDF
- [ ] Notifications email

### Phase 4 (À venir 📅)
- [ ] OpenSearch pour logs
- [ ] Dashboards de monitoring
- [ ] CI/CD complet
- [ ] Documentation utilisateur finale

---

**Dernière mise à jour** : 14 juillet 2026
