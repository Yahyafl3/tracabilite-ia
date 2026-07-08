# ARCHITECTURE INFORMATIQUE - TRAÇABILITÉ IA

**Projet** : Système de Traçabilité des Décisions d'Intelligence Artificielle  
**Date** : 8 juillet 2026  
**Équipe** : Yahya Falhaoui & Badderdine Chourane  
**Version** : 1.0

---

## 1. VUE D'ENSEMBLE DE L'ARCHITECTURE

### 1.1 Architecture Globale

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          COUCHE PRÉSENTATION                            │
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │  Dashboard   │  │  Validation  │  │    Audit     │  │   Admin    │ │
│  │   React UI   │  │   React UI   │  │  React UI    │  │  React UI  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └────────────┘ │
│                                                                         │
│                    React 18 + Vite + Axios                             │
│                    Port 5173 (Docker Container)                        │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↕ HTTP/REST
                                    ↕ JSON
┌─────────────────────────────────────────────────────────────────────────┐
│                         COUCHE API (BACKEND)                            │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                    REST API Controllers                        │   │
│  │  /api/auth  /api/decisions  /api/validations  /api/dashboard  │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                ↕                                       │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │              JWT Security Filter + RBAC                        │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                ↕                                       │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                  Services (Logique Métier)                     │   │
│  │  AuthService  DecisionService  ValidationService  HashChain   │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                ↕                                       │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │                 Repositories (JPA/Hibernate)                   │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│              Spring Boot 3 + Java 21 + Maven                           │
│              Port 8080 (Docker Container)                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↕ JDBC
                                    ↕ SQL
┌─────────────────────────────────────────────────────────────────────────┐
│                      COUCHE DONNÉES (DATABASE)                          │
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                │
│  │ utilisateurs │  │  decisions   │  │  validation_ │                │
│  │              │  │              │  │   actions    │                │
│  │  - id        │  │  - id        │  │  - id        │                │
│  │  - email     │  │  - contenu   │  │  - decision  │                │
│  │  - role      │  │  - statut    │  │  - validateur│                │
│  │  - password  │  │  - hash_*    │  │  - action    │                │
│  └──────────────┘  └──────────────┘  └──────────────┘                │
│                                                                         │
│                PostgreSQL 16 (Docker Container)                        │
│                Port 5432                                               │
└─────────────────────────────────────────────────────────────────────────┘


### 1.2 Architecture en Conteneurs (Docker)

```
┌────────────────────────────────────────────────────────────────┐
│                  HOST MACHINE (Windows)                        │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Docker Network: tracabilite-network         │ │
│  │                                                          │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │ │
│  │  │  Container   │  │  Container   │  │  Container   │  │ │
│  │  │   Frontend   │  │   Backend    │  │  PostgreSQL  │  │ │
│  │  │              │  │              │  │              │  │ │
│  │  │  React+Vite  │  │ Spring Boot  │  │  Database    │  │ │
│  │  │  Port 5173   │  │  Port 8080   │  │  Port 5432   │  │ │
│  │  │              │  │              │  │              │  │ │
│  │  │  Nginx Serve │  │  Tomcat      │  │  PG Engine   │  │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  │ │
│  │         ↕                  ↕                  ↕         │ │
│  │    localhost:5173   localhost:8080     localhost:5432  │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  Docker Compose (orchestration des 3 services)                │
└────────────────────────────────────────────────────────────────┘
```

**Avantages de l'architecture Docker** :
- Isolation des services
- Déploiement simplifié (1 commande)
- Environnement reproductible
- Scalabilité facilitée
- Réseau interne sécurisé

---

## 2. ARCHITECTURE DÉTAILLÉE DU BACKEND

### 2.1 Architecture en Couches (Layered Architecture)

```
┌───────────────────────────────────────────────────────────────┐
│                    COUCHE CONTROLLER                          │
│                   (REST API Endpoints)                        │
│                                                               │
│  AuthController     DecisionController     ValidationController│
│  DashboardController    AuditController                       │
│                                                               │
│  Responsabilité :                                             │
│  - Réception des requêtes HTTP                                │
│  - Validation des données entrantes                           │
│  - Conversion DTO ↔ Entity                                    │
│  - Gestion des réponses HTTP                                  │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                    COUCHE SECURITY                            │
│                  (JWT + RBAC Filters)                         │
│                                                               │
│  JwtAuthenticationFilter    JwtProvider    JwtUtils           │
│  SecurityConfig (RBAC)                                        │
│                                                               │
│  Responsabilité :                                             │
│  - Authentification par JWT                                   │
│  - Autorisation par rôles (ADMIN, VALIDATEUR, etc.)           │
│  - Protection des endpoints                                   │
│  - Génération et validation des tokens                        │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                     COUCHE SERVICE                            │
│                   (Logique Métier)                            │
│                                                               │
│  AuthService              DecisionService                     │
│  ValidationService        HashChainService                    │
│  DashboardService                                             │
│                                                               │
│  Responsabilité :                                             │
│  - Logique métier complexe                                    │
│  - Gestion des transactions                                   │
│  - Calcul des hash cryptographiques                           │
│  - Validation des règles métier                               │
│  - Orchestration des opérations                               │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                   COUCHE REPOSITORY                           │
│                 (Accès aux Données)                           │
│                                                               │
│  UtilisateurRepository                                        │
│  DecisionRepository                                           │
│  ValidationActionRepository                                   │
│                                                               │
│  Responsabilité :                                             │
│  - Opérations CRUD sur la base                                │
│  - Requêtes personnalisées (findBy...)                        │
│  - Gestion des relations entre entités                        │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                    COUCHE PERSISTENCE                         │
│                    (JPA/Hibernate)                            │
│                                                               │
│  Utilisateur (abstract)    Decision    ValidationAction       │
│  Administrateur            SystemeIA   Validateur             │
│                                                               │
│  Responsabilité :                                             │
│  - Mapping Objet-Relationnel (ORM)                            │
│  - Gestion du cycle de vie des entités                        │
│  - Relations entre entités                                    │
│  - Héritage (@Inheritance SINGLE_TABLE)                       │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                  BASE DE DONNÉES PostgreSQL                   │
└───────────────────────────────────────────────────────────────┘

```

### 2.2 Composants Transversaux

```
┌───────────────────────────────────────────────────────────────┐
│                   COMPOSANTS TRANSVERSAUX                     │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │    AUDIT     │  │  EXCEPTION   │  │    UTILS     │       │
│  │              │  │   HANDLER    │  │              │       │
│  │ AuditListener│  │  Global      │  │  HashUtils   │       │
│  │ AuditAspect  │  │  Exception   │  │  DateUtils   │       │
│  │              │  │  Handler     │  │              │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │    MAPPER    │  │    CONFIG    │                         │
│  │              │  │              │                         │
│  │  Decision    │  │  CorsConfig  │                         │
│  │  Mapper      │  │  SwaggerConfig│                        │
│  │  Utilisateur │  │              │                         │
│  │  Mapper      │  │              │                         │
│  └──────────────┘  └──────────────┘                         │
└───────────────────────────────────────────────────────────────┘
```

---

## 3. ARCHITECTURE DÉTAILLÉE DU FRONTEND

### 3.1 Structure React (Component-Based Architecture)

```
┌───────────────────────────────────────────────────────────────┐
│                        APP ROOT                               │
│                        (App.jsx)                              │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              React Router (AppRoutes.jsx)           │     │
│  │                                                     │     │
│  │  /login          → LoginPage                        │     │
│  │  /dashboard      → Dashboard (Protected)            │     │
│  │  /decisions      → DecisionListPage (Protected)     │     │
│  │  /decisions/:id  → DecisionDetailPage (Protected)   │     │
│  │  /validations    → ValidationPage (Protected)       │     │
│  │  /audit          → AuditPage (Protected)            │     │
│  └─────────────────────────────────────────────────────┘     │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                    COUCHE CONTEXT                             │
│                  (State Management)                           │
│                                                               │
│  ┌──────────────────────┐  ┌──────────────────────┐          │
│  │   AuthContext        │  │   DecisionContext    │          │
│  │                      │  │                      │          │
│  │  - user              │  │  - decisions[]       │          │
│  │  - token             │  │  - loading           │          │
│  │  - isAuthenticated   │  │  - filters           │          │
│  │  - login()           │  │  - fetchDecisions()  │          │
│  │  - logout()          │  │  - createDecision()  │          │
│  └──────────────────────┘  └──────────────────────┘          │
│                                                               │
│  Responsabilité : État global partagé entre composants       │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                      COUCHE HOOKS                             │
│              (Custom React Hooks - Logique)                   │
│                                                               │
│  useAuth()         useDecisions()       useValidation()       │
│                                                               │
│  Responsabilité :                                             │
│  - Encapsulation de la logique réutilisable                   │
│  - Accès aux Contexts                                         │
│  - Gestion d'état local complexe                              │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                    COUCHE SERVICES                            │
│                  (API Communication)                          │
│                                                               │
│  api.js (Axios config + interceptors)                         │
│  authService.js      decisionService.js                       │
│  validationService.js    dashboardService.js                  │
│                                                               │
│  Responsabilité :                                             │
│  - Communication avec le backend REST API                     │
│  - Gestion des tokens JWT dans les headers                    │
│  - Interception des erreurs HTTP                              │
│  - Transformation des données                                 │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                    COUCHE PAGES                               │
│                  (Smart Components)                           │
│                                                               │
│  LoginPage          Dashboard           DecisionListPage      │
│  DecisionDetailPage ValidationPage      AuditPage             │
│                                                               │
│  Responsabilité :                                             │
│  - Structure de la page                                       │
│  - Orchestration des composants                               │
│  - Gestion d'état local de la page                            │
│  - Appels aux services                                        │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                   COUCHE COMPONENTS                           │
│                  (Dumb Components)                            │
│                                                               │
│  COMMON:           DECISION:          VALIDATION:             │
│  - Button          - DecisionCard     - ValidationForm        │
│  - Modal           - DecisionList     - ValidationHistory     │
│  - Navbar          - DecisionForm                             │
│  - Table                              DASHBOARD:              │
│  - Loader          AUTH:              - Stats                 │
│                    - Login            - Charts                │
│                    - Register                                 │
│                                       AUDIT:                  │
│                                       - AuditLog              │
│                                       - AuditTrail            │
│                                                               │
│  Responsabilité :                                             │
│  - Affichage (UI pure)                                        │
│  - Réception de props                                         │
│  - Émission d'événements vers parents                         │
│  - Réutilisables et testables                                │
└───────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────────────────────────────────────────────────────┐
│                     COUCHE UTILS                              │
│                  (Fonctions Utilitaires)                      │
│                                                               │
│  constants.js (ROLES, STATUTS, API_ENDPOINTS)                 │
│  formatters.js (dates, nombres, textes)                       │
│  helpers.js (fonctions génériques)                            │
│                                                               │
│  Responsabilité :                                             │
│  - Constantes globales                                        │
│  - Fonctions de formatage                                     │
│  - Helpers réutilisables                                      │
└───────────────────────────────────────────────────────────────┘
```

---

## 4. FLUX DE DONNÉES (DATA FLOW)

### 4.1 Flux d'Authentification

```
┌──────────┐                                    ┌──────────┐
│  USER    │                                    │ FRONTEND │
│          │                                    │          │
└────┬─────┘                                    └────┬─────┘
     │                                               │
     │ 1. Saisit email + password                    │
     │──────────────────────────────────────────────>│
     │                                               │
     │                                               │ 2. POST /api/auth/login
     │                                               │    {email, password}
     │                                               │──────────────┐
     │                                               │              │
     │                                         ┌─────▼────┐         │
     │                                         │ BACKEND  │         │
     │                                         │          │         │
     │                                         │          │         │
     │                                         └─────┬────┘         │
     │                                               │              │
     │                                               │ 3. Vérifie dans DB
     │                                               │    BCrypt compare
     │                                         ┌─────▼──────┐       │
     │                                         │ PostgreSQL │       │
     │                                         │            │       │
     │                                         └─────┬──────┘       │
     │                                               │              │
     │                                               │ 4. Si OK     │
     │                                               │ Génère JWT   │
     │                                               │<─────────────┘
     │                                               │
     │                                               │ 5. Response
     │                                               │ {token, user}
     │                   6. Stocke token             │<─────────────
     │                      localStorage             │
     │<──────────────────────────────────────────────│
     │                                               │
     │ 7. Redirige vers Dashboard                    │
     │<──────────────────────────────────────────────│
     │                                               │

Note : Le token JWT contient : {id, email, role, exp}
```

### 4.2 Flux de Création de Décision (par Système IA)

```
┌──────────────┐                             ┌──────────┐
│  SYSTÈME IA  │                             │ FRONTEND │
│              │                             │          │
└──────┬───────┘                             └────┬─────┘
       │                                          │
       │ 1. Génère une décision                   │
       │    (algorithme IA)                       │
       │                                          │
       │ 2. Envoie la décision                    │
       │────────────────────────────────────────> │
       │    - contenu                             │
       │    - contexte                            │
       │    - scoreConfiance                      │
       │                                          │
       │                                          │ 3. POST /api/decisions
       │                                          │    + JWT token header
       │                                          │──────────────┐
       │                                          │              │
       │                                    ┌─────▼────┐         │
       │                                    │ BACKEND  │         │
       │                                    │          │         │
       │                                    └─────┬────┘         │
       │                                          │              │
       │                                          │ 4. Vérifie JWT
       │                                          │    Rôle = SYSTEME_IA ?
       │                                          │              │
       │                                          │ 5. Récupère décision
       │                                          │    précédente
       │                                    ┌─────▼──────┐       │
       │                                    │ PostgreSQL │       │
       │                                    │            │       │
       │                                    └─────┬──────┘       │
       │                                          │              │
       │                                          │ 6. Calcule hash
       │                                          │ SHA256(contenu+contexte+
       │                                          │        timestamp+hashPrecedent)
       │                                          │              │
       │                                          │ 7. Crée Decision
       │                                          │    - statut = EN_ATTENTE
       │                                          │    - hashCourant
       │                                          │    - hashPrecedent
       │                                    ┌─────▼──────┐       │
       │                                    │ PostgreSQL │       │
       │                                    │  INSERT    │       │
       │                                    └─────┬──────┘       │
       │                                          │              │
       │                                          │ 8. Response  │
       │                                          │<─────────────┘
       │                                          │ {decision créée}
       │ 9. Confirmation                          │<─────────────
       │<─────────────────────────────────────────│
       │                                          │
       │                                          │ 10. Notification
       │                                          │     aux validateurs
       │                                          │
```


### 4.3 Flux de Validation de Décision

```
┌─────────────┐                             ┌──────────┐
│ VALIDATEUR  │                             │ FRONTEND │
│             │                             │          │
└──────┬──────┘                             └────┬─────┘
       │                                         │
       │ 1. Consulte décisions EN_ATTENTE        │
       │────────────────────────────────────────>│
       │                                         │ 2. GET /api/decisions?statut=EN_ATTENTE
       │                                         │    + JWT token
       │                                         │──────────────┐
       │                                         │              │
       │                                   ┌─────▼────┐         │
       │                                   │ BACKEND  │         │
       │                                   │          │         │
       │                                   └─────┬────┘         │
       │                                         │              │
       │                                         │ 3. Vérifie JWT
       │                                         │    Rôle = VALIDATEUR ?
       │                                         │              │
       │                                   ┌─────▼──────┐       │
       │                                   │ PostgreSQL │       │
       │                                   │  SELECT    │       │
       │                                   └─────┬──────┘       │
       │                                         │              │
       │                                         │ 4. Liste     │
       │                                         │<─────────────┘
       │ 5. Affiche liste                        │
       │<────────────────────────────────────────│
       │                                         │
       │ 6. Sélectionne une décision             │
       │    Examine : contenu, contexte, score   │
       │                                         │
       │ 7. Choisit action :                     │
       │    - APPROUVER                          │
       │    - REJETER (+ commentaire)            │
       │    - MODIFIER (+ justification)         │
       │────────────────────────────────────────>│
       │                                         │ 8. POST /api/validations
       │                                         │    {decisionId, action, commentaire}
       │                                         │──────────────┐
       │                                         │              │
       │                                   ┌─────▼────┐         │
       │                                   │ BACKEND  │         │
       │                                   │          │         │
       │                                   └─────┬────┘         │
       │                                         │              │
       │                                         │ 9. Vérifie JWT + Rôle
       │                                         │    Commentaire obligatoire ?
       │                                         │              │
       │                                         │ 10. Crée ValidationAction
       │                                         │     - validateurId
       │                                         │     - decisionId
       │                                         │     - typeAction
       │                                         │     - commentaire
       │                                         │     - dateAction
       │                                         │     - hashAction
       │                                         │              │
       │                                         │ 11. Met à jour Decision
       │                                         │     - statut = APPROUVE/REJETE/MODIFIE
       │                                   ┌─────▼──────┐       │
       │                                   │ PostgreSQL │       │
       │                                   │ INSERT +   │       │
       │                                   │  UPDATE    │       │
       │                                   └─────┬──────┘       │
       │                                         │              │
       │                                         │ 12. Response │
       │                                         │<─────────────┘
       │ 13. Confirmation + mise à jour UI       │
       │<────────────────────────────────────────│
       │                                         │
```

---

## 5. ARCHITECTURE DE SÉCURITÉ

### 5.1 Couches de Sécurité

```
┌─────────────────────────────────────────────────────────────────┐
│                     NIVEAU 1 : HTTPS (Production)               │
│                   Chiffrement transport (TLS 1.3)               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    NIVEAU 2 : CORS Policy                       │
│            Limitation des origines autorisées                   │
│              (http://localhost:5173 en dev)                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│               NIVEAU 3 : JWT Authentication                     │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter (Spring Security)              │   │
│  │                                                         │   │
│  │  1. Extrait token du header "Authorization: Bearer..." │   │
│  │  2. Vérifie la signature du token                      │   │
│  │  3. Vérifie l'expiration                               │   │
│  │  4. Extrait les claims (id, email, role)               │   │
│  │  5. Charge l'utilisateur dans SecurityContext          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  Token JWT structure :                                          │
│  {                                                              │
│    "sub": "user@example.com",                                   │
│    "id": 123,                                                   │
│    "role": "VALIDATEUR",                                        │
│    "iat": 1234567890,                                           │
│    "exp": 1234654290                                            │
│  }                                                              │
│  Signature : HMACSHA256(header + payload + secret)             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│           NIVEAU 4 : RBAC (Role-Based Access Control)           │
│                                                                 │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │     ADMIN      │  │  SYSTEME_IA    │  │  VALIDATEUR    │   │
│  │                │  │                │  │                │   │
│  │ • Tous droits  │  │ • Créer        │  │ • Lire         │   │
│  │ • Gestion      │  │   décisions    │  │   décisions    │   │
│  │   utilisateurs │  │ • Lire ses     │  │   EN_ATTENTE   │   │
│  │ • Config       │  │   décisions    │  │ • Valider      │   │
│  │   système      │  │                │  │   (Approuver/  │   │
│  │                │  │                │  │    Rejeter/    │   │
│  │                │  │                │  │    Modifier)   │   │
│  └────────────────┘  └────────────────┘  └────────────────┘   │
│                                                                 │
│  ┌────────────────┐                                             │
│  │   AUDITEUR     │                                             │
│  │                │                                             │
│  │ • Lecture      │                                             │
│  │   seule        │                                             │
│  │ • Audit trail  │                                             │
│  │ • Vérification │                                             │
│  │   intégrité    │                                             │
│  │ • Rapports     │                                             │
│  └────────────────┘                                             │
│                                                                 │
│  Implémentation :                                               │
│  @PreAuthorize("hasRole('VALIDATEUR')")                         │
│  public Decision validateDecision(...)                          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│          NIVEAU 5 : Validation des Données                      │
│                                                                 │
│  • @Valid sur les DTOs                                          │
│  • @NotNull, @NotBlank, @Size, @Email, etc.                     │
│  • Validation côté service (règles métier)                      │
│  • Échappement SQL (JPA prépare les statements)                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│         NIVEAU 6 : Protection des Mots de Passe                 │
│                                                                 │
│  BCrypt Hashing :                                               │
│  - Salt aléatoire par mot de passe                              │
│  - Coût de 10 rounds (2^10 itérations)                          │
│  - Hash one-way (impossible à déchiffrer)                       │
│                                                                 │
│  Exemple :                                                      │
│  Password: "MyPassword123"                                      │
│  Hash: "$2a$10$N9qo8uLO.../abcdefgh..."                         │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Matrice de Permissions

```
┌──────────────────┬───────┬────────────┬────────────┬───────────┐
│   ENDPOINT       │ ADMIN │ SYSTEME_IA │ VALIDATEUR │ AUDITEUR  │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ POST /auth/login │  ALL  │    ALL     │    ALL     │    ALL    │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ GET /decisions   │  YES  │    YES     │    YES     │    YES    │
│                  │ (all) │ (own only) │(EN_ATTENTE)│   (all)   │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ POST /decisions  │  YES  │    YES     │    NO      │    NO     │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ POST /validations│  YES  │    NO      │    YES     │    NO     │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ GET /dashboard   │  YES  │    YES     │    YES     │    YES    │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ GET /audit       │  YES  │    NO      │    NO      │    YES    │
├──────────────────┼───────┼────────────┼────────────┼───────────┤
│ POST /users      │  YES  │    NO      │    NO      │    NO     │
└──────────────────┴───────┴────────────┴────────────┴───────────┘
```

---

## 6. ARCHITECTURE DU CHAÎNAGE CRYPTOGRAPHIQUE

### 6.1 Principe du Chaînage (Blockchain-Like)

```
┌─────────────────────────────────────────────────────────────────┐
│                     CHAÎNE DE DÉCISIONS                         │
│                                                                 │
│  ┌───────────────┐      ┌───────────────┐      ┌────────────┐ │
│  │  DECISION 1   │      │  DECISION 2   │      │ DECISION 3 │ │
│  │               │      │               │      │            │ │
│  │ ID: 1         │      │ ID: 2         │      │ ID: 3      │ │
│  │ Contenu: ...  │      │ Contenu: ...  │      │ Contenu: ..│ │
│  │               │      │               │      │            │ │
│  │ Hash Prec:    │      │ Hash Prec:    │      │ Hash Prec: │ │
│  │   null        │      │   a3f5b2...   │──┐   │  7c8d9e... │─┐│
│  │               │      │               │  │   │            │ ││
│  │ Hash Courant: │──┐   │ Hash Courant: │<─┘   │ Hash Cour: │<┘│
│  │   a3f5b2...   │  │   │   7c8d9e...   │      │  2b4f8a... │ │
│  │               │  └──>│               │      │            │ │
│  └───────────────┘      └───────────────┘      └────────────┘ │
│                                                                 │
│  Chaînage : Decision(N).hashPrecedent = Decision(N-1).hashCourant│
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Calcul du Hash SHA-256

```
┌─────────────────────────────────────────────────────────────────┐
│                    ALGORITHME DE HACHAGE                        │
│                                                                 │
│  Entrées pour le calcul :                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  1. Contenu de la décision                               │  │
│  │  2. Contexte de la décision                              │  │
│  │  3. Timestamp (date et heure de création)                │  │
│  │  4. Hash de la décision précédente (ou null si première) │  │
│  │  5. ID du système IA                                     │  │
│  │  6. Score de confiance                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Concaténation des données                        │  │
│  │  data = contenu + contexte + timestamp + hashPrec + ...  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              SHA-256 Hashing Algorithm                   │  │
│  │                                                          │  │
│  │  Input: "Approuver crédit 50000€..."                     │  │
│  │  Output: a3f5b2c8d4e7f1a2b5c8d9e3f7a1b4c7d2e5f8a3b6c9... │  │
│  │          (64 caractères hexadécimaux)                    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │        Stockage dans la base de données                  │  │
│  │  - hashCourant (UNIQUE INDEX)                            │  │
│  │  - hashPrecedent (FK vers décision précédente)           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 Vérification de l'Intégrité

```
┌─────────────────────────────────────────────────────────────────┐
│              ALGORITHME DE VÉRIFICATION                         │
│                                                                 │
│  Pour chaque décision dans la chaîne :                          │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ÉTAPE 1 : Vérifier le chaînage                          │  │
│  │                                                          │  │
│  │  if (decision.hashPrecedent !=                           │  │
│  │      decisionPrecedente.hashCourant) {                   │  │
│  │      return "ERREUR: Chaîne brisée !";                   │  │
│  │  }                                                       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ÉTAPE 2 : Recalculer le hash                            │  │
│  │                                                          │  │
│  │  calculatedHash = SHA256(                                │  │
│  │      decision.contenu +                                  │  │
│  │      decision.contexte +                                 │  │
│  │      decision.dateCreation +                             │  │
│  │      decision.hashPrecedent                              │  │
│  │  );                                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ÉTAPE 3 : Comparer avec le hash stocké                  │  │
│  │                                                          │  │
│  │  if (calculatedHash != decision.hashCourant) {           │  │
│  │      return "ERREUR: Hash modifié !";                    │  │
│  │  }                                                       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  RÉSULTAT : Chaîne intègre ✓                             │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 6.4 Détection de Manipulation

```
SCÉNARIO : Tentative de modification d'une décision

État initial :
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Decision 1   │───>│ Decision 2   │───>│ Decision 3   │
│ Hash: A123   │    │ Hash: B456   │    │ Hash: C789   │
│              │    │ HashPrec:A123│    │ HashPrec:B456│
└──────────────┘    └──────────────┘    └──────────────┘

Attaquant modifie Decision 2 :
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Decision 1   │  ╳ │ Decision 2   │───>│ Decision 3   │
│ Hash: A123   │    │ Hash: X999   │    │ Hash: C789   │
│              │    │ HashPrec:A123│    │ HashPrec:B456│ ← CASSÉ !
└──────────────┘    └──────────────┘    └──────────────┘

Détection :
- Decision3.hashPrecedent (B456) != Decision2.hashCourant (X999)
- ALERTE : Manipulation détectée !
```

---

## 7. ARCHITECTURE DE LA BASE DE DONNÉES

### 7.1 Modèle Entité-Relation (ERD)

```
┌────────────────────────────────────────────────────────────────┐
│                        UTILISATEURS                            │
│                        (Table unique)                          │
│                                                                │
│  PK  id                 BIGINT                                 │
│      email              VARCHAR(255) UNIQUE                    │
│      mot_de_passe       VARCHAR(255) (BCrypt hash)             │
│      nom                VARCHAR(100)                           │
│      prenom             VARCHAR(100)                           │
│      role               VARCHAR(50) (ADMIN, VALIDATEUR, ...)   │
│      type_utilisateur   VARCHAR(31) (discriminator)            │
│      actif              BOOLEAN                                │
│      date_creation      TIMESTAMP                              │
│      --- Champs spécifiques (nullable) ---                     │
│      specialite         VARCHAR(100) (pour Validateur)         │
│      nombre_validations INT (pour Validateur)                  │
│      nom_modele         VARCHAR(100) (pour SystemeIA)          │
│      version_modele     VARCHAR(50) (pour SystemeIA)           │
│      api_key            VARCHAR(255) (pour SystemeIA)          │
└────────────────────────────────────────────────────────────────┘
                              │
                              │ 1:N (créateur)
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                          DECISIONS                             │
│                                                                │
│  PK  id                    BIGINT                              │
│  FK  systeme_ia_id         BIGINT → utilisateurs.id            │
│  FK  decision_precedente   BIGINT → decisions.id (nullable)    │
│      contenu               TEXT                                │
│      contexte              TEXT                                │
│      statut                VARCHAR(20) (EN_ATTENTE, APPROUVE...)│
│      score_confiance       DECIMAL(5,2) (0.00-100.00)          │
│      hash_courant          VARCHAR(64) UNIQUE                  │
│      hash_precedent        VARCHAR(64) (nullable pour première)│
│      date_creation         TIMESTAMP                           │
│      date_modification     TIMESTAMP                           │
└────────────────────────────────────────────────────────────────┘
                              │
                              │ 1:N (validation)
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                     VALIDATION_ACTIONS                         │
│                                                                │
│  PK  id                 BIGINT                                 │
│  FK  decision_id        BIGINT → decisions.id                  │
│  FK  validateur_id      BIGINT → utilisateurs.id               │
│      type_action        VARCHAR(20) (APPROUVER, REJETER, ...)  │
│      commentaire        TEXT (obligatoire)                     │
│      date_action        TIMESTAMP                              │
│      hash_action        VARCHAR(64)                            │
└────────────────────────────────────────────────────────────────┘
```


### 7.2 Index et Contraintes

```sql
-- INDEX pour optimisation des requêtes

-- Utilisateurs
CREATE INDEX idx_utilisateurs_email ON utilisateurs(email);
CREATE INDEX idx_utilisateurs_role ON utilisateurs(role);
CREATE INDEX idx_utilisateurs_type ON utilisateurs(type_utilisateur);

-- Decisions
CREATE UNIQUE INDEX idx_decisions_hash_courant ON decisions(hash_courant);
CREATE INDEX idx_decisions_statut ON decisions(statut);
CREATE INDEX idx_decisions_systeme_ia ON decisions(systeme_ia_id);
CREATE INDEX idx_decisions_date_creation ON decisions(date_creation DESC);
CREATE INDEX idx_decisions_hash_precedent ON decisions(hash_precedent);

-- Validation Actions
CREATE INDEX idx_validations_decision ON validation_actions(decision_id);
CREATE INDEX idx_validations_validateur ON validation_actions(validateur_id);
CREATE INDEX idx_validations_date ON validation_actions(date_action DESC);
CREATE INDEX idx_validations_type ON validation_actions(type_action);

-- CONTRAINTES

-- Unicité et intégrité
ALTER TABLE utilisateurs 
  ADD CONSTRAINT uk_email UNIQUE (email);

ALTER TABLE decisions 
  ADD CONSTRAINT uk_hash_courant UNIQUE (hash_courant);

-- Clés étrangères avec cascade
ALTER TABLE decisions 
  ADD CONSTRAINT fk_decisions_systeme_ia 
  FOREIGN KEY (systeme_ia_id) REFERENCES utilisateurs(id);

ALTER TABLE decisions 
  ADD CONSTRAINT fk_decisions_precedente 
  FOREIGN KEY (decision_precedente) REFERENCES decisions(id);

ALTER TABLE validation_actions 
  ADD CONSTRAINT fk_validations_decision 
  FOREIGN KEY (decision_id) REFERENCES decisions(id) ON DELETE CASCADE;

ALTER TABLE validation_actions 
  ADD CONSTRAINT fk_validations_validateur 
  FOREIGN KEY (validateur_id) REFERENCES utilisateurs(id);

-- Contraintes de vérification
ALTER TABLE decisions 
  ADD CONSTRAINT chk_score_confiance 
  CHECK (score_confiance >= 0 AND score_confiance <= 100);

ALTER TABLE utilisateurs 
  ADD CONSTRAINT chk_role 
  CHECK (role IN ('ADMIN', 'VALIDATEUR', 'SYSTEME_IA', 'AUDITEUR'));

ALTER TABLE decisions 
  ADD CONSTRAINT chk_statut 
  CHECK (statut IN ('EN_ATTENTE', 'APPROUVE', 'REJETE', 'MODIFIE'));

ALTER TABLE validation_actions 
  ADD CONSTRAINT chk_type_action 
  CHECK (type_action IN ('APPROUVER', 'REJETER', 'MODIFIER', 'CONSULTER'));
```

### 7.3 Stratégie de Héritage JPA (SINGLE_TABLE)

```
Table physique : utilisateurs
┌────────────────────────────────────────────────────────────────┐
│ id │ email │ role │ type_utilisateur │ nom_modele │ specialite │
├────┼───────┼──────┼─────────────────┼───────────┼────────────┤
│ 1  │ a@... │ ADMIN│ Administrateur  │ NULL      │ NULL       │
│ 2  │ v@... │ VALID│ Validateur      │ NULL      │ Finance    │
│ 3  │ ia@...│ SYS_IA│ SystemeIA      │ GPT-4     │ NULL       │
└────────────────────────────────────────────────────────────────┘

Mapping Java (JPA) :
┌──────────────────┐
│   Utilisateur    │ (abstract class)
│   @Entity        │
│   @Inheritance   │
│   (SINGLE_TABLE) │
└────────┬─────────┘
         │
    ┌────┼────┬─────────────────┐
    │    │    │                 │
┌───▼──┐ │ ┌──▼──────┐ ┌───────▼──────┐
│ Admin│ │ │Validateur│ │  SystemeIA   │
└──────┘ │ └─────────┘ └──────────────┘
         │
     ┌───▼──────┐
     │ Auditeur │
     └──────────┘

Avantages :
- Une seule table (performances)
- Requêtes polymorphes simplifiées
- Pas de jointures pour récupérer un utilisateur

Inconvénients :
- Colonnes nullables pour champs spécifiques
```

---

## 8. ARCHITECTURE DES APIS REST

### 8.1 Structure des Endpoints

```
BASE_URL : http://localhost:8080

┌──────────────────────────────────────────────────────────────┐
│                   API AUTHENTIFICATION                       │
├──────────────────────────────────────────────────────────────┤
│ POST   /api/auth/login                                       │
│        Body: {email, password}                               │
│        Response: {token, user{id, email, role, nom}}         │
│                                                              │
│ POST   /api/auth/register                                    │
│        Body: {email, password, nom, prenom, role}            │
│        Response: {message, userId}                           │
│                                                              │
│ POST   /api/auth/refresh                                     │
│        Header: Authorization: Bearer <token>                 │
│        Response: {newToken}                                  │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                     API DECISIONS                            │
├──────────────────────────────────────────────────────────────┤
│ GET    /api/decisions                                        │
│        Params: ?statut=EN_ATTENTE&page=0&size=10             │
│        Response: {content[], totalElements, totalPages}      │
│                                                              │
│ GET    /api/decisions/{id}                                   │
│        Response: {decision détaillée + validations}          │
│                                                              │
│ POST   /api/decisions                                        │
│        Auth: SYSTEME_IA, ADMIN                               │
│        Body: {contenu, contexte, scoreConfiance}             │
│        Response: {decision créée avec hash}                  │
│                                                              │
│ PUT    /api/decisions/{id}                                   │
│        Auth: SYSTEME_IA (own), ADMIN                         │
│        Body: {contenu, contexte}                             │
│        Response: {decision modifiée}                         │
│                                                              │
│ DELETE /api/decisions/{id}                                   │
│        Auth: ADMIN only                                      │
│        Response: 204 No Content                              │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    API VALIDATIONS                           │
├──────────────────────────────────────────────────────────────┤
│ POST   /api/validations                                      │
│        Auth: VALIDATEUR, ADMIN                               │
│        Body: {decisionId, typeAction, commentaire}           │
│        Response: {validationAction, decisionUpdated}         │
│                                                              │
│ GET    /api/validations/decision/{decisionId}                │
│        Response: {validations[] pour cette décision}         │
│                                                              │
│ GET    /api/validations/validateur/{validateurId}            │
│        Auth: VALIDATEUR (self), ADMIN, AUDITEUR              │
│        Response: {historique des validations}                │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                     API DASHBOARD                            │
├──────────────────────────────────────────────────────────────┤
│ GET    /api/dashboard/stats                                  │
│        Response: {                                           │
│          totalDecisions,                                     │
│          decisionsEnAttente,                                 │
│          tauxApprobation,                                    │
│          tempsValidationMoyen,                               │
│          decisionsByStatut{EN_ATTENTE, APPROUVE, ...}        │
│        }                                                     │
│                                                              │
│ GET    /api/dashboard/charts/timeline                        │
│        Params: ?startDate=...&endDate=...                    │
│        Response: {data[] par jour/semaine/mois}              │
│                                                              │
│ GET    /api/dashboard/charts/validateurs                     │
│        Response: {validateurs[] avec stats}                  │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                       API AUDIT                              │
├──────────────────────────────────────────────────────────────┤
│ GET    /api/audit/trail                                      │
│        Auth: AUDITEUR, ADMIN                                 │
│        Params: ?startDate=...&userId=...&action=...          │
│        Response: {auditLog[] chronologique}                  │
│                                                              │
│ GET    /api/audit/verify-chain                               │
│        Auth: AUDITEUR, ADMIN                                 │
│        Response: {                                           │
│          isIntegre: true/false,                              │
│          totalDecisions,                                     │
│          decisionsVerifiees,                                 │
│          errors: [] (si problèmes)                           │
│        }                                                     │
│                                                              │
│ GET    /api/audit/decision/{id}/history                      │
│        Response: {historique complet de la décision}         │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    API UTILISATEURS                          │
├──────────────────────────────────────────────────────────────┤
│ GET    /api/users                                            │
│        Auth: ADMIN                                           │
│        Response: {users[]}                                   │
│                                                              │
│ GET    /api/users/{id}                                       │
│        Auth: ADMIN, SELF                                     │
│        Response: {user détaillé}                             │
│                                                              │
│ POST   /api/users                                            │
│        Auth: ADMIN                                           │
│        Body: {email, password, role, ...}                    │
│        Response: {user créé}                                 │
│                                                              │
│ PUT    /api/users/{id}                                       │
│        Auth: ADMIN, SELF                                     │
│        Body: {nom, prenom, ...}                              │
│        Response: {user modifié}                              │
│                                                              │
│ DELETE /api/users/{id}                                       │
│        Auth: ADMIN                                           │
│        Response: 204 No Content                              │
└──────────────────────────────────────────────────────────────┘
```

### 8.2 Format des Réponses

```json
Format standard de succès :
{
  "success": true,
  "data": {...},
  "timestamp": "2026-07-08T10:30:00Z"
}

Format standard d'erreur :
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Le commentaire est obligatoire pour rejeter",
    "field": "commentaire"
  },
  "timestamp": "2026-07-08T10:30:00Z"
}

Format pagination :
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 150,
  "totalPages": 15,
  "first": true,
  "last": false
}
```

---

## 9. ARCHITECTURE DE DÉPLOIEMENT

### 9.1 Environnement de Développement (Docker)

```
┌────────────────────────────────────────────────────────────────┐
│              Machine de Développement (Windows)                │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                Docker Desktop                            │ │
│  │                                                          │ │
│  │  ┌─────────────────────────────────────────────────┐    │ │
│  │  │  Network: tracabilite-network (bridge)         │    │ │
│  │  │                                                 │    │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌───────┐│    │ │
│  │  │  │ tracabilite- │  │ tracabilite- │  │tracab-││    │ │
│  │  │  │  postgres    │  │   backend    │  │ility- ││    │ │
│  │  │  │              │  │              │  │frontend││   │ │
│  │  │  │ Image: PG16  │  │ Build: Java  │  │Build: ││    │ │
│  │  │  │ Port: 5432   │  │ Port: 8080   │  │React  ││    │ │
│  │  │  │              │  │              │  │Port:  ││    │ │
│  │  │  │ Volume:      │  │ Volume:      │  │5173   ││    │ │
│  │  │  │ postgres_data│  │ ./backend/src│  │./front││    │ │
│  │  │  └──────────────┘  └──────────────┘  └───────┘│    │ │
│  │  └─────────────────────────────────────────────────┘    │ │
│  │                                                          │ │
│  │  Commandes :                                             │ │
│  │  - docker-compose up -d        (démarrer)                │ │
│  │  - docker-compose down         (arrêter)                 │ │
│  │  - docker-compose logs -f      (logs)                    │ │
│  │  - docker-compose restart      (redémarrer)              │ │
│  │  - rebuild.bat                 (reconstruire tout)       │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  Accès :                                                       │
│  - Frontend : http://localhost:5173                            │
│  - Backend API : http://localhost:8080                         │
│  - PostgreSQL : localhost:5432                                 │
│                                                                │
│  Hot Reload :                                                  │
│  - Frontend : Vite (instantané)                                │
│  - Backend : Spring Boot DevTools (2-3 secondes)               │
└────────────────────────────────────────────────────────────────┘
```

### 9.2 Environnement de Production (Suggestion)

```
┌────────────────────────────────────────────────────────────────┐
│                    CLOUD PROVIDER (AWS/Azure/GCP)              │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Load Balancer (HTTPS)                       │ │
│  │              SSL/TLS Certificate                         │ │
│  └───────────────────┬──────────────────────────────────────┘ │
│                      │                                         │
│          ┌───────────┴───────────┐                             │
│          │                       │                             │
│  ┌───────▼───────┐       ┌──────▼──────┐                      │
│  │   Frontend    │       │   Backend   │                      │
│  │   Container   │       │  Container  │                      │
│  │               │       │             │                      │
│  │  Nginx        │       │ Spring Boot │                      │
│  │  Static Files │       │  App Server │                      │
│  │               │       │             │                      │
│  │  Replicas: 2  │       │ Replicas: 3 │                      │
│  └───────────────┘       └──────┬──────┘                      │
│                                 │                              │
│                          ┌──────▼─────────┐                    │
│                          │   PostgreSQL   │                    │
│                          │   (Managed)    │                    │
│                          │                │                    │
│                          │  - Multi-AZ    │                    │
│                          │  - Backups     │                    │
│                          │  - Read Replica│                    │
│                          └────────────────┘                    │
│                                                                │
│  Services additionnels :                                       │
│  - Redis : Cache + Sessions                                    │
│  - CloudWatch/Prometheus : Monitoring                          │
│  - S3/Blob Storage : Fichiers statiques                        │
│  - CloudFront/CDN : Distribution                               │
└────────────────────────────────────────────────────────────────┘
```
