# Production Gap Analysis — Traçabilité IA

**Branche :** `refactor/multidomain-decisions`
**Date d’audit :** 2026-07-30
**Périmètre :** repository local (sans déploiement Vercel / Render / Neon)
**Classification cible :** release candidate technique — **pas** validation métier des modèles synthétiques

> Les modèles CREDIT / MEDICAL / EDUCATION sont des **démonstrateurs** entraînés sur datasets synthétiques marocains. Ils ne sont **pas** validés pour prendre de vraies décisions bancaires, médicales ou académiques.

---

## 1. Architecture actuelle

| Couche | Technologie | Rôle |
|--------|-------------|------|
| Frontend | Angular 21 + PrimeNG + Nginx | SPA, proxy `/api` → backend |
| Backend | Spring Boot 3.4.1 / Java (pom 17, Docker 21) | REST JWT, JPA, mail, Actuator |
| ML | Flask + scikit-learn + SHAP + Gunicorn | Prédiction / explain legacy + multidomain |
| DB | PostgreSQL 16 | Persistance décisions, users, audit |
| Orchestration | docker-compose | postgres, backend, frontend, ml-service |
| Cloud (hors scope local) | Vercel / Render / Neon | Déploiements existants — **non modifiés** |

**Flux :** Browser → Nginx:80 → Backend:8080 → Postgres + ML:5000 + Groq/OpenRouter.

**Domaines :** CREDIT, MEDICAL, EDUCATION — datasets `datasets/`, modèles `ml-service/models/{domain}/`.

---

## 2. Versions

| Composant | Version |
|-----------|---------|
| Angular / CLI | ^21.2 / ^21.0 |
| Spring Boot | 3.4.1 |
| Java (pom / Docker) | 17 / Temurin 21 (**écart**) |
| Flask | 3.0.0 |
| Python image | 3.11-slim |
| PostgreSQL | 16-alpine |
| JJWT | 0.12.6 |
| Flyway / Liquibase | **Absent** |
| Bucket4j / Resilience4j | **Absent** |

---

## 3. Services Docker

| Service | Ports host | Notes |
|---------|------------|-------|
| postgres | 5432:5432 | Mot de passe compose `tracabilite123` |
| backend | 8080:8080 | `ddl-auto=update`, `SHOW_SQL=true` |
| frontend | 80:80 | Nginx |
| ml-service | 5000:5000 | Sans auth API ; CORS ouvert |

Volume : `postgres_data`. Réseau : `tracabilite-network`.

---

## 4. Endpoints exposés (synthèse)

**Backend (auth JWT sauf public) :** `/api/auth/**`, `/api/decisions/**` (legacy + multidomain), `/api/validation/**`, `/api/dashboard`, `/api/comparaison`, `/api/audit/**`, `/api/users/**`, `/api/ai/**`, `/api/support/**`, admin Groq/OpenRouter/support.

**Public :** `/api/auth/**`, POST support, Swagger, `/actuator/health`, `/api/ai/ping`, `/api/ai/test-post`.

**ML (aucune auth) :** `/health`, `/ready`, `/predict`, `/explain`, `/predict/{credit|medical|education}`, `/models`, `/schema`.

---

## 5. Spring Security / CORS / JWT

| Élément | État |
|---------|------|
| Auth DB + BCrypt | OK (pas d’InMemoryUserDetailsManager) |
| JWT HMAC | Secret **avec défaut faible** dans `application.properties` |
| CORS | Allowlist localhost + Vercel + `FRONTEND_URL` ; headers `*` |
| CSRF | Désactivé (stateless JWT) |
| Method security | `@PreAuthorize` présent |
| Rôles domaine | RESPONSABLE_CREDIT / PROFESSIONNEL_SANTE / RESPONSABLE_PEDAGOGIQUE |
| Swagger | **Public** |
| Rate limit global | **Absent** (seul SupportRateLimiter) |

---

## 6. Gestion des erreurs

`GlobalExceptionHandler` mappe 400/401/403/404/409/503.
**Risque :** catch-all 500 renvoie `ex.getMessage()` → fuite possible.
ML : certaines erreurs exposent `details: str(exc)`.

---

## 7. Migrations / Hibernate

| Mécanisme | État |
|-----------|------|
| `ddl-auto` | Défaut **`update`** (+ forcé dans compose) |
| Flyway / Liquibase | **Absent** |
| Scripts manuels | `backend/scripts/migration/V1__multidomain_decisions.sql` |
| DataInitializer | `ALTER TABLE` runtime (rôles, CHECK) |

**Production :** aucun schéma ne doit être géré par `update`.

---

## 8. Stockage des modèles

- Legacy : `ml-service/artifacts/model.joblib`
- Multidomain : `ml-service/models/{credit,medical,education}/*_pipeline.joblib` + `metadata.json`
- Datasets : `datasets/{credit,medical,education}/*.csv`
- Checksum / registre ACTIVE/RETIRED : **absent**

---

## 9. Variables d’environnement

Template : `.env.example`. Props : `application.properties` (profil unique).

Défauts dangereux : `JWT_SECRET`, `DATABASE_PASSWORD=tracabilite123`, logging DEBUG security.

Pas de `application-prod.properties` / fail-fast secrets.

---

## 10. Audit et intégrité

| Mécanisme | Maturité |
|-----------|----------|
| DecisionHistory | Actif |
| SHA-256 DecisionHashService | Actif |
| AuditLog entity/service | Partiel (exports) |
| AuditAspect / AuditListener | **Vides** |
| CorrelationIdFilter | Actif |
| SensitiveDataSanitizer | Actif |

---

## 11. Fichiers sensibles versionnés

| Élément | Verdict |
|---------|---------|
| `.env` | Absent du tracking (gitignore) — OK |
| `.env.example` | Placeholders — OK |
| `backend/scripts/backups/*.sql` | **Versionnés** — risque données démo |
| `backend/avast-root.crt` | Versionné — pollution trust store |
| Demo passwords dans `DataInitializer` | Loggés / connus — BLOQUANT hors profil local |
| Clés privées / tokens | Non trouvés dans le code source |

**git status (2026-07-30) :** branche `refactor/multidomain-decisions`, nombreux changements multidomaine non commités + suppression `ml-service/data/synthetic_credit_dataset.csv`. Aucun commit/push effectué dans cet audit.

---

## 12. Tests existants

| Suite | Ordre de grandeur |
|-------|-------------------|
| Angular `*.spec.ts` | ~32 fichiers |
| Spring `*Test.java` | ~43 fichiers |
| Python pytest | ~3 fichiers |
| E2E / k6 / security scan automatisé | **Absents ou incomplets** |

---

## 13. Classification des écarts

### BLOQUANT

| ID | Écart |
|----|-------|
| B1 | `ddl-auto=update` par défaut et dans Docker — pas de migrations auto versionnées |
| B2 | Secrets JWT / DB avec valeurs par défaut faibles — pas de fail-fast prod |
| B3 | Comptes démo seedés avec mots de passe connus (`DataInitializer`) |
| B4 | Service ML sans authentification, port 5000 publié, CORS ouvert |
| B5 | Swagger / OpenAPI publics en production potentielle |
| B6 | Pas de Flyway/Liquibase — schéma non reproductible |

### ÉLEVÉ

| ID | Écart |
|----|-------|
| E1 | Postgres `5432` exposé avec mot de passe faible |
| E2 | Logging DEBUG / `SHOW_SQL=true` en compose |
| E3 | Messages d’erreur 500 potentiellement verbeux |
| E4 | Pas de rate limiting login / ML / exports / agents |
| E5 | Endpoints AI de test publics |
| E6 | Écart Java 17 (pom) vs 21 (Docker) |
| E7 | Dumps SQL backup versionnés |
| E8 | Pas de profils Spring `local` / `test` / `prod` |
| E9 | Pas de circuit breaker / timeouts Resilience4j documentés unifiés |
| E10 | Pas de checksum modèles / gouvernance ACTIVE |

### MOYEN

| ID | Écart |
|----|-------|
| M1 | AuditAspect vide ; couverture AuditLog incomplète |
| M2 | Index DB non garantis pour filtres domaine/statut/dates |
| M3 | Pas d’API `/api/v1` versionnée |
| M4 | Pas d’observabilité Prometheus/Grafana |
| M5 | Healthchecks ML live/ready partiels vs 3 modèles |
| M6 | Headers sécurité frontend (CSP, HSTS) incomplets |
| M7 | Pas de CI GitHub Actions complète (security + SBOM) |
| M8 | Soft delete / rétention / anonymisation non formalisées en code |
| M9 | Self-validation auteur → validateur à renforcer par tests |
| M10 | Docker non multi-stage / non-root partout |

### FAIBLE

| ID | Écart |
|----|-------|
| F1 | `CorsConfig` vide (dead code) |
| F2 | `TestController` message trompeur |
| F3 | Dual stack Groq + OpenRouter (complexité) |
| F4 | Documentation dispersée / runbooks prod absents |
| F5 | Warnings canvas Vitest (non bloquants) |

---

## 14. Risques de production (synthèse)

1. **Schéma DB non maîtrisé** → corruption / drift silencieux.
2. **Secrets faibles + comptes démo** → compromission immédiate.
3. **ML ouvert** → abuse compute / fuite de prédictions.
4. **Absence rate limit** → brute force auth / DoS exports.
5. **Modèles synthétiques** → risque **juridique et éthique** si présentés comme décisionnels réels (hors technique).
6. **Données MEDICAL** → besoin minimisation / masquage export / rétention.
7. **Pas de backup/restore testé** documenté pour ops.
8. **Pas de rollback schéma / modèle** formalisé.

---

## 15. Plan de remédiation priorisé

| Priorité | Actions |
|----------|---------|
| P0 | Profils Spring + fail-fast secrets + `ddl-auto=validate` + Flyway |
| P0 | Désactiver seed démo hors `local` ; Swagger off en prod |
| P0 | Auth backend→ML ; ne plus publier 5000/5432 en prod |
| P1 | Rate limiting, erreurs standardisées, indexes, healthchecks |
| P1 | Gouvernance modèles DEMO_SYNTHETIC + checksums |
| P2 | Observabilité, CI/CD, backup scripts, runbooks, E2E, k6 |
| P2 | Policies data protection / AI usage / human oversight |

---

## 16. Verdict d’audit initial

| Critère | État |
|---------|------|
| Prototype avancé multidomain | Oui |
| Staging-ready | Non |
| Production release candidate | **Non** (bloqueurs B1–B6) |
| Production-ready métier modèles | **Non applicable / interdit** sans données réelles + validation |

**Classification audit (pré-remédiation) :** `NOT_READY`

La remédiation technique des phases suivantes vise au mieux `STAGING_READY` ou `PRODUCTION_RELEASE_CANDIDATE` **technique**, avec mention explicite que les modèles restent `DEMO_SYNTHETIC`.
