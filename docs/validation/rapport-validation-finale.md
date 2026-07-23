# Rapport de validation finale

**Projet :** Traçabilité IA  
**Branche :** `main` (`c5c6f11` — merge `feat/sakai-primeng-migration`)  
**Date :** 2026-07-23  
**Périmètre :** validation pré-soutenance (code, tests, build, Docker, parcours API, documentation)  
**Méthode :** commandes automatisées + sondes HTTP/API ; UI navigateur non exercée pixel-par-pixel (couverture partielle via tests unitaires frontend).

---

## Résumé exécutif

| Critère | Résultat |
|---------|----------|
| Statut global | **Prêt sous réserve** |
| Git `main` / `origin/main` | Synchronisés, working tree clean |
| Backend tests | **120 / 120** — BUILD SUCCESS (`mvn clean test`, ~1 min 12 s) |
| Frontend tests | **112 / 112** — 30 fichiers — OK |
| Frontend build | OK (`npm run build`) |
| Docker | 4 services **Up** (frontend, backend, postgres healthy, ml-service) |
| Scénario décision complet (API) | **Validé** (ML + 3 agents Groq + consensus + approbation humaine) |
| Secrets versionnés | Aucune clé API réelle ; placeholders demo dans `.env.example` |
| Anomalies bloquantes | **0** |
| Anomalies majeures | **2** (environnement démo / packaging local) |
| Anomalies mineures | **plusieurs** (doc legacy, warnings, asset vidéo inutilisé) |

**Verdict soutenance :** le cœur métier et technique est opérationnel sur Docker. Avant la soutenance, préparer des **comptes de démo stables** (la base locale actuelle ne contient plus les comptes seed `user@` / `validateur@` / `auditeur@`) et vérifier l’**envoi SMTP** réel.

---

## Résultats détaillés

### Git

- Branche courante : `main`
- `git status` : clean
- `git fetch` + `status -sb` : `main...origin/main` à jour
- `git diff --check` : OK
- `.env` **non versionné** (présent dans `.gitignore`)
- Aucun `node_modules`, `dist`, `target` tracké

### Secrets

Recherche `git grep` sur motifs clés :

| Motif | Résultat |
|-------|----------|
| `sk-or-v1-` / clés réelles | Absent |
| `gsk_` | Uniquement assertions de tests « ne doit pas contenir » |
| `GROQ_API_KEY=` / `OPENROUTER_API_KEY=` / `MAIL_PASSWORD=` | Présents dans `.env.example` avec valeurs **vides** |
| `JWT_SECRET=` | Placeholder de développement dans `.env.example` (non vide) |
| `BEGIN PRIVATE KEY` | Absent |

**Confirmation :** aucun secret de production n’a été lu ni affiché depuis `.env` durant cette campagne.

### Backend

- `mvn clean test` : **120 tests, 0 échec, 0 erreur**
- Aucune classe Ollama dans `target` après `clean`
- `mvn clean package -DskipTests=false` en local : **échec repackage** (PKIX / certificat Maven Central — environnement Avast SSL), **pas un bug applicatif**
- Packaging JAR **OK via Docker** (`docker compose build --no-cache`)

### Frontend

- Node `v24.13.1` / npm `11.8.0`
- `npm ci` : OK (sans `--legacy-peer-deps`)
- `npm test -- --watch=false` : **112 tests OK**
- Aucun `fit` / `fdescribe` / `xit` / `xdescribe`
- `npm run build` : OK
- Warnings NG8102 (nullish coalescing) sur `validation-queue.component.html` : **non bloquants**
- Warnings Chart.js / canvas en tests unitaires : **acceptés** (environnement jsdom)
- `npm audit` : 6 vulnérabilités signalées (dépendance) — hors correctif métier

### Assets

| Asset | Statut |
|-------|--------|
| `frontend/public/assets/videos/ai-traceability-bg.mp4` | Présent (~1,0 Mo) |
| Référence vidéo dans le template login actuel | **Absente** (login en dégradé CSS) |
| Images landing dédiées | Peu d’images sous `public/assets/images` (README seulement) |
| UML SVG générés | Présents (`docs/uml/generated/…`) |

### Docker

- `docker compose down` puis `build --no-cache` puis `up -d` : OK
- Services : frontend `:80`, backend `:8080`, ml-service `:5000`, postgres `:5432` (healthy)
- Pas de restart loop observé
- Logs : démarrage Spring OK ; seed « base déjà initialisée, 3 compte(s) »

### Santé HTTP

| URL | Code | ~latence |
|-----|------|----------|
| `http://localhost/` | 200 | ~175 ms |
| `http://localhost/auth/login` | 200 | ~82 ms |
| `http://localhost/support` | 200 | ~57 ms |
| `http://localhost:8080/actuator/health` | 200 | ~1,3 s |
| `http://localhost:5000/health` | 200 | ~26 ms |
| `http://localhost:5000/ready` | 200 | ~17 ms |

### Auth / login

- Mauvais credentials → HTTP **401** (message backend générique ; mapping UI frontend testé unitairement vers *« Adresse email ou mot de passe incorrect. »*)
- Login admin seed → **200** + JWT
- Formulaire vide / anti-autofill : couverts par tests unitaires login
- Mot de passe non stocké : confirmé par design + tests auth

### Forgot / reset password

- `POST /api/auth/forgot-password` email connu et inconnu → **200**, **même message public**
- Token jamais affiché dans ce rapport
- Envoi SMTP réel : **non confirmé** dans les logs récents de cette session (à retester avant soutenance avec App Password)

### Support

- `POST /api/support/messages` → **201**
- Persistance PostgreSQL : table `support_message` peuplée (3 lignes observées)
- Admin support : endpoints protégés (accès sans token → 401)

### Rôles et sécurité

État réel de la base au moment du test :

| Email | Rôle |
|-------|------|
| `admin@tracabilite.ia` | ADMINISTRATEUR |
| compte personnel | VALIDATEUR |
| compte personnel | AUDITEUR |

- Comptes seed `user@tracabilite.ia`, `validateur@tracabilite.ia`, `auditeur@tracabilite.ia` **absents** (base déjà initialisée + comptes modifiés/supprimés — comportement seed conforme)
- Admin → `/api/users` **200**
- Anonyme → `/api/users` **401**
- Routes admin protégées côté API

**Réserve soutenance :** prévoir recreation des comptes demo (reset volume ou création manuelle admin) pour le scénario multi-rôles.

### Décision / ML / SHAP / Groq / consensus / validation

Parcours API réel (admin) :

1. `POST /api/decisions/analyze` (~6 s)
2. Statut initial `EN_ATTENTE`
3. ML : `LogisticRegression` v2.0.0 — décision `APPROUVER`, confiance ~93,38, risque `LOW`
4. SHAP : facteurs présents (`explanationSource` / factors)
5. Groq : **3/3 SUCCESS** — modèles `llama-3.3-70b-versatile`, `openai/gpt-oss-120b`, `openai/gpt-oss-20b`, provider `GROQ`
6. Consensus : `APPROUVER`, `agreementRate=100`, `consensusAvailable=true`, note informative (ML non modifié)
7. `POST .../approve` → statut **`APPROUVEE`**, décision humaine finale enregistrée
8. Historique / audit décision : **200**
9. Hash courant présent

ML direct :

- Dossier favorable → `APPROUVER`
- Dossier risqué → `REJETER` / `HIGH`

Admin Groq `/api/admin/groq/status` : `configured=true`, `reachable=true`, 3 modèles `available=true` (aucune clé affichée).

### Audit / dashboard / comparaison

- `/api/audit/recent` 200
- `/api/audit/integrity/summary` 200
- `/api/dashboard` 200
- `/api/comparaison` 200

### Base de données

Tables présentes : `utilisateur`, `decision`, `reponse_agent_ia`, `explanation_factor`, `decision_history`, `decision_source`, `validation_action`, `password_reset_token`, `support_message`, `appel_ia`, `systeme_ia`, `trace_capture_job`.

Seed : non rejoué à chaque démarrage (3 comptes déjà présents).

### Documentation

- `README.md` aligné (SHA-256 ≠ blockchain, Groq actif, dataset synthétique, Angular 21 / Java 17)
- UML PlantUML + SVG présents
- `INDEX_DOCUMENTATION.md` encore orienté Ollama (legacy) — **mineur**
- `docs/MIGRATION_OLLAMA_OPENROUTER.md` : historique acceptable

### Code mort / legacy

| Élément | Classification |
|---------|----------------|
| Mentions Ollama dans docs d’index / migration | Historique |
| Alias `includeOllama` dans DTO | Compatibilité |
| Libellé « Consensus OpenRouter » | Interdit dans UI active (tests de non-régression) |
| Vidéo login non branchée | Asset orphelin (UI utilise dégradé) |
| `TestController` `/api/test/ping` | Surface démo — information |

### UI manuelle non exhaustive

Non rejoués exhaustivement dans cette campagne (pas de parcours navigateur automatisé complet) :

- responsive multi-breakpoints ;
- dark mode contraste ;
- accessibilité clavier détaillée ;
- charts audit en runtime navigateur (tests unitaires canvas déjà bruyants).

Ces points restent **recommandés** en smoke test humain 15–20 min avant la soutenance.

---

## Anomalies

### 1. Comptes seed demo absents de la base locale

- **Sévérité :** MAJEURE (démo soutenance)
- **Reproduction :** login `user@` / `validateur@` / `auditeur@` → 401 ; DB = 3 comptes (admin + 2 comptes personnalisés)
- **Cause :** `DataInitializer` ne reseede pas si la base n’est pas vide
- **Correction recommandée :** avant soutenance, soit `docker compose down -v` puis `up` (recréation seed), soit recreation manuelle des comptes via admin
- **Corrigée :** non (volontaire — pas de changement métier)

### 2. `mvn clean package` local échoue (PKIX)

- **Sévérité :** MAJEURE (outillage local uniquement)
- **Cause :** interception TLS / certificat (Avast) vers Maven Central au repackage
- **Correction :** utiliser Docker pour packager ; ou importer le certificat Avast dans le truststore JDK local
- **Corrigée :** non (environnement)

### 3. Placeholder `JWT_SECRET` / mots de passe Postgres dans `.env.example`

- **Sévérité :** MINEURE
- **Cause :** valeurs de développement documentées
- **Correction optionnelle :** laisser vide + commentaire « à renseigner »
- **Corrigée :** non (hors scope validation ; risque faible en local)

### 4. Asset vidéo login non référencé

- **Sévérité :** INFORMATION / MINEURE
- **Cause :** UI login basculée sur dégradé CSS ; fichier mp4 toujours présent
- **Correction optionnelle :** retirer l’asset ou le rebrancher
- **Corrigée :** non

### 5. Documentation index Ollama / « FastAPI » dans `.env.example`

- **Sévérité :** MINEURE
- **Correction :** aligner index + commentaire ML (Flask)
- **Corrigée :** non

### 6. Warnings NG8102 + npm audit + Chart.js jsdom

- **Sévérité :** INFORMATION
- **Impact soutenance :** aucun sur le scénario principal

### 7. SMTP non prouvé en bout-en-bout dans cette session

- **Sévérité :** MAJEURE *si* la démo forgot-password / mail support est au programme ; sinon MINEURE
- **Action :** tester un forgot-password réel la veille ; distinguer App Password vs bug code

---

## Warnings acceptés

| Warning | Classe |
|---------|--------|
| NG8102 validation-queue `??` | Non bloquant |
| Chart.js `getContext` en Vitest/jsdom | Dépendance / environnement de test |
| Spring « Using generated security password » en tests context | Normal (UserDetails auto-config tests) |
| Logs JWT « Pas d’Authorization header » sur endpoints publics | Normal |

---

## Scénario soutenance (≈ 8 minutes)

1. **Landing** (`http://localhost/`) — valeur produit, CTA connexion.  
2. **Login admin** — formulaire vide, puis connexion.  
3. **Nouvelle décision crédit** — saisie réelle → ML + SHAP.  
4. **Agents Groq** — 3 réponses, provider GROQ, modèles configurés.  
5. **Consensus** — informatif, ML inchangé.  
6. **Validation humaine** — approuver / rejeter sur dossier global.  
7. **Audit** — historique, hash, intégrité.  
8. **Admin Groq + users + support** — supervision et demande support publique.  
9. *(Optionnel)* Forgot-password si SMTP validé.

**Avant le jour J :** quotas Groq, SMTP, comptes demo (user / validateur / auditeur).

---

## Conclusion

**Prêt sous réserve.**

Le produit sur `main` est cohérent : tests verts, build frontend OK, stack Docker saine, parcours décision AI (ML + Groq + consensus + validation + audit) **prouvé par API**.  

Réserves à lever avant soutenance :

1. Recréer / documenter les **comptes multi-rôles** de démonstration.  
2. Confirmer **SMTP** si le reset password est montré.  
3. Smoke UI humain (responsive / dark / charts).

Aucun correctif applicatif n’a été introduit durant cette campagne (documentation de validation uniquement).

---

## Annexes — commandes exécutées (extrait)

```text
git branch / status / fetch
git grep (secrets, Ollama, Consensus OpenRouter)
cd backend && mvn clean test
cd frontend && npm ci && npm test -- --watch=false && npm run build
docker compose down && docker compose build --no-cache && docker compose up -d
Probes HTTP + API auth/ML/Groq/decision/approve/audit/support
```
