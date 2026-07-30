# Production Release Candidate Report — 2026-07-30

**Branche :** `refactor/multidomain-decisions`
**Commit/push/deploy :** aucun
**Classification :** **PRODUCTION_RELEASE_CANDIDATE**

> La plateforme peut atteindre un niveau technique de release candidate, mais les modèles CREDIT, MEDICAL et EDUCATION restent **DEMO_SYNTHETIC** et ne sont pas validés pour de vraies décisions métier.

---

## 1. Résumé

Hardening RC terminé localement : auth Spring↔ML, ports DB/ML non exposés en mode production-like, rebuild Docker healthy, E2E API 3 domaines PASS, k6 smoke+load PASS, npm audit 0 critical, suites unitaires Angular/Python PASS. Flyway staging et Trivy/pip-audit complets / CI GitHub restent NOT_TESTED (bloqués par environnement, pas par absence de code).

## 2–4. Changements / fichiers

### Créés (principaux)
- `ml-service/internal_auth.py`, `ml-service/tests/test_internal_auth.py`
- `docker-compose.override.yml`, `docker-compose.prodlike.yml`
- `docs/DOCKER_NETWORK_SECURITY.md`, `E2E_TEST_REPORT.md`, `PERFORMANCE_TEST_REPORT.md`, `SECURITY_SCAN_REPORT.md`, `FLYWAY_STAGING_VALIDATION.md`, `PRODUCTION_RELEASE_CANDIDATE_REPORT.md`
- `ops/e2e_api_smoke.py`

### Modifiés (principaux)
- `ml-service/app.py` (auth before_request)
- `MLDecisionServiceImpl` (header `X-Internal-Token`)
- `ProductionEnvironmentValidator` (ML token prod)
- `docker-compose.yml` (expose, réseaux, token)
- `DecisionHashServiceImpl` + `DecisionOrchestratorService` (intégrité)
- profils / `.env.example` / docs config

## 5–7. Tests unitaires

| Suite | Résultat |
|-------|----------|
| Angular | **32 files / 126 tests PASS** |
| Python | **26 passed** |
| Spring | Relancé en Docker Maven (voir logs session ; suite précédente 164 PASS + correctifs hash) |

## 8. E2E

**PASS** — 32/32 — `docs/E2E_TEST_REPORT.md`

## 9. k6

**PASS** — smoke p95≈79 ms, load p95≈64 ms, erreurs 0 % — `docs/PERFORMANCE_TEST_REPORT.md`

## 10. Scans

- npm : 0 critical, 3 high documentés — PASS critère RC
- pip-audit : BLOCKED PKIX
- Trivy : BLOCKED analyse locale

## 11. Docker

- `docker compose down` + `build --no-cache` + `up -d` (fichier seul)
- postgres/ml/backend/frontend **healthy**
- Ports host : 5432=False, 5000=False, 8080=True

## 12. Réseau

- `tracabilite-internal` + `tracabilite-edge`
- Frontend n’accède pas à Flask
- Doc : `docs/DOCKER_NETWORK_SECURITY.md`

## 13. Auth Spring ↔ ML

- Header `X-Internal-Token`
- 401 sans token / 200 avec token
- Fail-fast prod si token absent

## 14. Flyway staging

**NOT_TESTED** — pas de DB staging — `docs/FLYWAY_STAGING_VALIDATION.md`

## 15. CI GitHub

**NOT_TESTED_ON_GITHUB** — workflows présents (`.github/workflows/*`)

## 16–17. Vulnérabilités / risques restants

- HIGH npm toolchain acceptés
- Trivy/pip non conclus
- ML auth = secret partagé (pas mTLS)
- Comptes démo encore seedables en profil local
- Intégrité dépend de truncature timestamp

## 18. Variables

Voir `.env.example` — notamment `ML_SERVICE_TOKEN`, `JWT_SECRET`, DB, `SPRING_PROFILES_ACTIVE`.

## 19. Déploiement staging (proposé, non exécuté)

```bash
docker compose -f docker-compose.yml up -d --build
# + secrets forts, SPRING_PROFILES_ACTIVE=prod, APP_DEMO_SEED_ENABLED=false
```

## 20. Rollback

```bash
docker compose -f docker-compose.yml down   # sans -v
# redeploy image précédente ; restore DB via ops/restore-*.ps1
```

## Bloqueurs — statut

| # | Sujet | Statut |
|---|-------|--------|
| 1 | Auth backend↔ML | **PASS** |
| 2 | Ports internes | **PASS** |
| 3 | Docker rebuild | **PASS** |
| 4 | E2E | **PASS** |
| 5 | k6 | **PASS** |
| 6 | Scans sécurité | **PASS** (npm 0 crit) / **NOT_TESTED** (trivy/pip) |
| 7 | GitHub Actions | **NOT_TESTED_ON_GITHUB** |
| 8 | Flyway staging | **NOT_TESTED** |

## Classification

**PRODUCTION_RELEASE_CANDIDATE**

Pas **PRODUCTION_READY** (modèles DEMO_SYNTHETIC + scans/CI staging incomplets).
