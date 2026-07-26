# Traçabilité IA

Plateforme de traçabilité, d’explicabilité, d’audit et de validation humaine des décisions assistées par intelligence artificielle.

**Projet de fin d’études (PFA) 2025/2026** — preuve de concept académique.

Traçabilité IA est une application web permettant d’analyser des demandes de crédit à l’aide d’un modèle Machine Learning, d’explications SHAP et de plusieurs agents IA. Chaque décision est conservée dans un dossier complet comprenant le contexte, les données, les réponses IA, les sources, l’historique, les empreintes SHA-256 et la validation humaine finale.

**Principes importants :**

- l’IA **assiste** la décision ; elle ne la remplace pas ;
- la **décision finale reste humaine** (validateur) ;
- le dataset ML est **synthétique** (prototype académique) ;
- le système **n’est pas destiné** à une utilisation bancaire réelle en production.

---

## Fonctionnalités principales

- authentification JWT ;
- rôles internes : Administrateur, Agent de crédit (`UTILISATEUR`), Validateur, Auditeur ;
- le client (demandeur de crédit) n’a **pas** de compte applicatif ;
- création et consultation des décisions ;
- analyse de demandes de crédit ;
- prédiction ML (régression logistique) ;
- confiance du modèle ;
- explication SHAP ;
- réponses multi-agents Groq ;
- consensus multi-agents (informatif) ;
- mode dégradé si les agents IA sont indisponibles (ML + SHAP conservés) ;
- validation humaine finale sur le dossier complet ;
- sources documentaires ;
- historique des décisions ;
- module d’audit ;
- correlation ID (suivi des requêtes) ;
- empreintes SHA-256 et chaînage d’intégrité ;
- comparaison des agents IA ;
- administration des utilisateurs ;
- supervision Groq ;
- mot de passe oublié / réinitialisation (SMTP) ;
- support utilisateur (formulaire public + gestion admin) ;
- landing page publique ;
- interface Angular + PrimeNG (thème Aura, layout inspiré de Sakai) ;
- light mode / dark mode ;
- déploiement local via Docker Compose.

---

## Intégrité des décisions

Le projet utilise des empreintes **SHA-256** et un **chaînage d’intégrité** pour détecter les modifications et vérifier la cohérence historique des décisions.

> **Ce mécanisme n’est pas une blockchain.**
> Il s’agit d’un chaînage cryptographique interne basé sur SHA-256, stocké en base PostgreSQL avec le dossier de décision.

---

## Architecture technique

### Frontend

- Angular 21 (composants standalone)
- PrimeNG 21
- thème Aura (`@primeuix/themes`)
- layout inspiré de Sakai
- Chart.js (graphiques audit / dashboard)
- servi en production via **Nginx** (image Docker)

### Backend

- Java 17
- Spring Boot 3.4.1
- Spring Security + JWT
- Spring Data JPA
- Spring Mail (`JavaMailSender`)
- validation Bean Validation
- OpenAPI / Swagger (si activé dans l’environnement)

### Base de données

- PostgreSQL 16

### Machine Learning

- Python
- Flask
- Scikit-learn (`LogisticRegression`)
- SHAP (`LinearExplainer`)
- dataset synthétique (`ml-service/data/synthetic_credit_dataset.csv`)

### IA générative

- **Groq** : provider actif pour les **nouvelles** analyses multi-agents
- **OpenRouter** : conservé pour la **compatibilité historique** (anciennes décisions, retry / comparaison selon le code existant)

### Déploiement local

- Docker Compose
- services séparés : `frontend`, `backend`, `postgres`, `ml-service`

### Schéma simplifié

```text
Agent de crédit (compte interne)
   ↓
Frontend Angular (Nginx :80)
   ↓
Backend Spring Boot (:8080)
   ├── PostgreSQL (:5432)
   ├── Service ML Python (:5000)
   └── API Groq (agents IA)
```

---

## Agent de crédit vs client

**L’Agent de crédit** est un utilisateur interne de l’application (rôle technique `UTILISATEUR`, autorité Spring `ROLE_USER`). Il saisit et suit les dossiers des clients. Exemples de profils : agent de crédit, chargé de dossier, analyste métier.

**Le client** (demandeur de crédit) est uniquement la personne dont les données sont saisies dans une décision. Il n’accède pas à l’application et ne possède pas de compte. Aucun rôle `CLIENT` n’existe.

---

## Workflow principal

1. L’administrateur crée le compte de l’agent de crédit (ou utilisation du compte de démo).
2. L’agent de crédit s’authentifie (JWT).
3. Il crée une décision et saisit les données du **dossier client**.
4. Le backend appelle le service ML.
5. Le service ML retourne la prédiction et les facteurs SHAP.
6. Le backend interroge les trois agents Groq.
7. Le système calcule un consensus informatif.
8. Le dossier complet est enregistré avec ses hashes SHA-256.
9. La décision passe en attente de validation (`EN_ATTENTE`).
10. Le validateur humain consulte le **dossier complet** (ML, SHAP, agents, sources, historique) et prend la **décision finale**.
11. L’auditeur contrôle l’historique et l’intégrité.
12. L’agent de crédit peut suivre le statut de ses dossiers (sans valider définitivement).

La validation humaine porte sur le **dossier complet**, et non sur chaque agent séparément.

Statuts métier observés : `BROUILLON` → `EN_ATTENTE` → `APPROUVEE` | `MODIFIEE` | `REJETEE`.

---

## Consensus multi-agents

Trois agents IA sont configurés. Le consensus est calculé à partir des réponses **réussies** uniquement.

Règles principales (implémentation actuelle) :

- moins de 2 réponses réussies → pas de consensus (`INSUFFICIENT_RESPONSES`) ;
- 2 réponses d’accord → consensus (taux d’accord 100 %) ;
- 2 réponses divergentes → pas de consensus (50 %) ;
- 3 réponses : majorité (≥ 2) → consensus (environ 66,7 % ou 100 % si unanimité) ;
- 3 votes tous différents → pas de consensus.

Principes :

- aucun agent ne modifie la prédiction ML ni les facteurs SHAP ;
- le consensus est **informatif** : il ne remplace jamais la validation humaine ;
- si Groq est indisponible ou partiellement en échec, le système peut fonctionner en **mode dégradé** (ML + SHAP).

Libellé actif : **consensus multi-agents** (pas « Consensus OpenRouter »).

---

## Agents IA configurés

Provider actif (nouvelles décisions) : **Groq**

| Agent | Modèle (valeur par défaut) |
|-------|----------------------------|
| Agent 1 | Llama 3.3 70B Versatile (`llama-3.3-70b-versatile`) |
| Agent 2 | GPT-OSS 120B (`openai/gpt-oss-120b`) |
| Agent 3 | GPT-OSS 20B (`openai/gpt-oss-20b`) |

Notes :

- les modèles dépendent de la disponibilité et des quotas du compte Groq ;
- sans `GROQ_API_KEY`, le parcours reste utilisable en mode dégradé (ML + SHAP) ;
- **ne jamais** committer ni publier de clé API.

OpenRouter reste présent dans la configuration pour la compatibilité avec l’historique et certains parcours annexes.

---

## Rôles utilisateurs

| Rôle affiché | Valeur technique | Accès principaux |
|--------------|------------------|------------------|
| **Administrateur** | `ADMINISTRATEUR` → `ROLE_ADMIN` | Comptes internes, supervision Groq, support, audit, décisions, dashboard, validation |
| **Agent de crédit** | `UTILISATEUR` → `ROLE_USER` | Dashboard, création / consultation de ses décisions, saisie dossier client, analyse IA (consultation), soumission à validation |
| **Validateur** | `VALIDATEUR` → `ROLE_VALIDATOR` | File de validation, consultation du dossier, **décision humaine finale** |
| **Auditeur** | `AUDITEUR` → `ROLE_AUDITOR` | Module d’audit et éléments d’intégrité |

L’Agent de crédit **ne peut pas** gérer les comptes, accéder à `/admin/*`, modifier ML/SHAP/Groq, ni valider définitivement une décision.

Les garde-fous d’accès sont définis côté frontend (`roleGuard`) et côté backend (Spring Security / `@PreAuthorize`).

---

## Prérequis

Pour démarrer l’application **avec Docker** :

- Docker Desktop (ou Docker Engine)
- Docker Compose
- Git

Pour le développement local hors Docker (optionnel) :

- Node.js 18+ (frontend)
- Java 17 + Maven (backend)
- Python 3.x (ml-service)

---

## Démarrage rapide

```bash
git clone https://github.com/Yahyafl3/tracabilite-ia.git
cd tracabilite-ia
```

Copier le fichier d’exemple d’environnement :

```powershell
# Windows
copy .env.example .env
```

```bash
# Linux / macOS
cp .env.example .env
```

Renseigner au minimum `GROQ_API_KEY` (et la configuration SMTP si vous testez le reset password / support). Puis :

```bash
docker compose up -d --build
```

Accès :

| Service | URL |
|---------|-----|
| Application | http://localhost |
| Backend API | http://localhost:8080 |
| Service ML | http://localhost:5000 |

Arrêt :

```bash
docker compose down
```

---

## Variables d’environnement

Les noms suivants sont définis dans `.env.example` (à adapter localement) :

**Base de données**

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

**JWT**

- `JWT_SECRET`
- `JWT_EXPIRATION_MS`

**Groq**

- `GROQ_API_KEY`
- `GROQ_BASE_URL`
- `GROQ_MODEL_1`
- `GROQ_MODEL_2`
- `GROQ_MODEL_3`
- `GROQ_CONNECT_TIMEOUT_MS`
- `GROQ_READ_TIMEOUT_MS`
- `GROQ_MAX_RETRIES`
- `GROQ_AGENT_DELAY_MS`

**OpenRouter** (compatibilité historique)

- `OPENROUTER_API_KEY`
- `OPENROUTER_BASE_URL`
- `OPENROUTER_MODEL_1`
- `OPENROUTER_MODEL_2`
- `OPENROUTER_MODEL_3`
- (et variables associées dans `.env.example`)

**ML**

- `ML_SERVICE_URL`
- `PORT` (service ML et backend cloud)

**Frontend / CORS**

- `FRONTEND_URL` (origine autorisée CORS + base des liens reset-password)
- `FRONTEND_RESET_PASSWORD_URL` (optionnel ; défaut `${FRONTEND_URL}/auth/reset-password`)
- `API_URL` (build Vercel → `environment.prod.ts`)

**Cloud DB (Neon)**

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

**SMTP / support**

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`
- `SUPPORT_EMAIL`

> **Sécurité :** ne jamais committer le fichier `.env`, ni partager les clés API, mots de passe SMTP ou secrets JWT.
> Les valeurs d’exemple doivent rester vides ou non sensibles dans le dépôt.

---

## Comptes de démonstration

Au **premier démarrage** (base vide), `DataInitializer` crée des comptes de développement.
Si la base contient déjà des utilisateurs, le seed ne recrée pas les comptes (sauf recreation de secours d’un administrateur s’il n’en reste aucun).

| Email | Mot de passe (dev) | Rôle |
|-------|--------------------|------|
| `admin@tracabilite.ia` | `admin123` | Administrateur |
| `user@tracabilite.ia` | `user123` | Agent de crédit (`UTILISATEUR`) |
| `validateur@tracabilite.ia` | `validateur123` | Validateur |
| `auditeur@tracabilite.ia` | `auditor123` | Auditeur |

Ces identifiants sont destinés uniquement à un environnement local de démonstration.

---

## Tests

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend
npm test -- --watch=false
npm run build
```

### Stack Docker

```bash
docker compose up -d --build
```

Au moment de la dernière validation documentaire du projet : environ **33** tests Java côté backend et **30** fichiers `*.spec.ts` côté frontend. Ces chiffres peuvent évoluer.

Il n’y a pas de pipeline CI/CD ni de suite E2E automatisée dans le dépôt à ce stade.

---

## Scénario de démonstration recommandé

1. Connexion avec le compte **Agent de crédit** (`user@tracabilite.ia`).
2. Création d’une décision pour un **dossier client**.
3. Affichage de la prédiction ML et des facteurs SHAP.
4. Affichage des réponses des agents Groq (si clé configurée).
5. Affichage du consensus multi-agents.
6. Déconnexion, puis connexion avec le compte **Validateur**.
7. Ouverture du dossier dans la file de validation et décision humaine finale.
8. Consultation de l’historique de la décision.
9. Connexion **Auditeur** (ou Administrateur) : consultation de l’audit et de l’intégrité.
10. Connexion **Administrateur** : dashboard, utilisateurs, supervision Groq, support.

> Avant la soutenance : vérifier les quotas Groq et, si besoin, l’envoi SMTP (App Password Gmail).

---

## Limites et précautions

- dataset ML **synthétique** ;
- modèle ML à vocation **démonstrative** ;
- projet **académique** (POC) ;
- résultats **non adaptés** à une décision bancaire réelle ;
- disponibilité des agents dépendante de Groq (quotas, latence, indisponibilité) ;
- SMTP Gmail dépendant d’une configuration correcte (App Password) ;
- chaînage d’intégrité SHA-256 **interne** — ce n’est **pas** une blockchain ;
- sécurité de production à renforcer avant tout déploiement réel (secrets, durcissement, monitoring) ;
- absence de CI/CD et de tests E2E dans l’état actuel du dépôt.

---

## Déploiement Vercel + Render + Neon

Cible gratuite recommandée pour une démo cloud :

| Composant | Plateforme | Dossier / artéfact |
|-----------|------------|--------------------|
| Frontend Angular | **Vercel** | `frontend/` |
| Backend Spring Boot | **Render** (Web Service Docker) | `backend/` |
| Service ML Flask | **Render** (Web Service Docker) | `ml-service/` |
| PostgreSQL | **Neon** | base managée (SSL) |

### 1. Neon (PostgreSQL)

1. Créer un projet Neon et une base.
2. Récupérer l’URL de connexion (avec `sslmode=require` ou laisser le backend l’ajouter pour `*.neon.tech`).
3. Noter user / password.

Le backend accepte :

- `DATABASE_URL` au format `postgres://…`, `postgresql://…` ou `jdbc:postgresql://…`
- et/ou `DATABASE_USERNAME` / `DATABASE_PASSWORD`

`spring.jpa.hibernate.ddl-auto=update` crée/met à jour le schéma **sans** supprimer les données au redémarrage.

### 2. Render — service ML (`ml-service/`)

- Runtime : **Docker**
- Root directory / Dockerfile path : `ml-service`
- Health check : `GET /health` → `{"status":"ok"}`
- Variable : `PORT` (fournie par Render)

### 3. Render — backend (`backend/`)

- Runtime : **Docker**
- Dockerfile path : `backend`
- Health check : `GET /actuator/health`
- Variables minimales :

```text
PORT                  # fourni par Render
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
FRONTEND_URL          # ex. https://votre-app.vercel.app
ML_SERVICE_URL        # URL publique du service ML Render
JWT_SECRET
GROQ_API_KEY
MAIL_HOST / MAIL_PORT / MAIL_USERNAME / MAIL_PASSWORD / MAIL_FROM   # si reset-password
```

CORS autorise uniquement `http://localhost`, `http://localhost:4200` et `FRONTEND_URL` (pas de wildcard avec credentials).

Lien reset-password construit ainsi : `${FRONTEND_URL}/auth/reset-password?token=…` (override possible via `FRONTEND_RESET_PASSWORD_URL`).

### 4. Vercel — frontend (`frontend/`)

1. Importer le monorepo, **Root Directory** = `frontend`.
2. Build : `npm run build` (injecte `API_URL` puis `ng build`).
3. Output : `dist/frontend/browser` (voir `frontend/vercel.json`).
4. Variable de build (optionnelle) : `API_URL=https://tracabilite-ia-backend.onrender.com`  
   (si absente, ce défaut est injecté automatiquement au build).
5. Les rewrites SPA renvoient `index.html` pour `/auth/login`, `/dashboard`, etc.

URLs de référence actuelles :

| Composant | URL |
|-----------|-----|
| Frontend | https://tracabilite-ia.vercel.app |
| Backend | https://tracabilite-ia-backend.onrender.com |
| ML | https://tracabilite-ia.onrender.com |

Sur Render (backend), définir aussi `FRONTEND_URL=https://tracabilite-ia.vercel.app` et `ML_SERVICE_URL=https://tracabilite-ia.onrender.com`.

### 5. Ordre de mise en ligne

1. Neon → 2. ML Render → 3. Backend Render (pointe vers Neon + ML) → 4. Vercel (pointe vers le backend) → 5. vérifier `FRONTEND_URL` côté backend.

Le déploiement **Docker Compose local** (`docker compose up -d --build`) reste inchangé.

---

## Améliorations futures

- CI/CD ;
- tests E2E ;
- Mailpit pour le développement local des e-mails ;
- stockage documentaire externe ;
- moteur de recherche avancé ;
- monitoring ;
- dataset métier réel validé ;
- amélioration du modèle ML ;
- gestion plus avancée des refresh tokens ;
- notifications.

---

## Documentation

- [Résumé applicatif](docs/presentation/resume-application.md)
- [Diagrammes UML — README](docs/uml/README.md)
- [Cas d’utilisation](docs/uml/use-case.puml)
- [Séquence — création de décision](docs/uml/sequence-creation-decision.puml)
- [Séquence — validation humaine](docs/uml/sequence-validation-humaine.puml)
- [Diagramme de classes](docs/uml/class-diagram.puml)
- [Migration historique Ollama → OpenRouter](docs/MIGRATION_OLLAMA_OPENROUTER.md) (contexte historique)

---

## Structure du dépôt

```text
tracabilite-ia/
├── frontend/          # Angular 21 + PrimeNG
├── backend/           # Spring Boot 3.4
├── ml-service/        # Flask + Scikit-learn + SHAP
├── docs/              # Présentation et UML
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Licence et contexte

Projet académique PFA — usage pédagogique.
Les contenus, modèles et comptes de démonstration ne constituent pas un produit bancaire certifié.
