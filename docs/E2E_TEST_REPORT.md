# E2E Test Report — 2026-07-30

**Environnement :** Docker production-like (`docker compose -f docker-compose.yml`)
**API :** `http://127.0.0.1:8080`
**Script :** `python ops/e2e_api_smoke.py`
**Modèles :** DEMO_SYNTHETIC

## Résumé

| Total | PASS | FAIL |
|------:|-----:|-----:|
| 32 | 32 | 0 |

## Scénarios

| Scénario | Commande / données | Attendu | Observé | Statut |
|----------|-------------------|---------|---------|--------|
| Unauthenticated list | GET `/api/decisions` | 401 | 401 | PASS |
| Login user | `user@tracabilite.ia` / user123 | token | ok | PASS |
| Login credit validator | `credit@tracabilite.ia` | token | ok | PASS |
| Login medical validator | `sante@tracabilite.ia` | token | ok | PASS |
| Login education validator | `pedago@tracabilite.ia` | token | ok | PASS |
| User → admin users | GET `/api/users` | 403 | 403 | PASS |
| CREDIT create+ML | POST `/api/decisions/credit` payload synthétique | 201 | 201 | PASS |
| CREDIT submit | POST `/{id}/submit` | 200 | 200 | PASS |
| CREDIT validate | ACCEPTEE + justification | 200 | 200 | PASS |
| CREDIT detail | GET `/{id}` | 200 | 200 | PASS |
| CREDIT integrity endpoint | POST `/{id}/verify-integrity` | 200 + status | VALID | PASS |
| CREDIT integrity VALID | status=VALID | VALID | VALID | PASS |
| CREDIT history | GET `/{id}/history` | 200 | 200 | PASS |
| MEDICAL create+ML | POST `/api/decisions/medical` | 201 | 201 | PASS |
| MEDICAL submit/validate/detail/history/integrity | SUIVI_STANDARD | 200 / VALID | ok | PASS |
| EDUCATION create+ML | POST `/api/decisions/education` `situationAcademique=DIFFICULTE` | 201 | 201 | PASS |
| EDUCATION submit/validate/detail/history/integrity | ACCOMPAGNEMENT | 200 / VALID | ok | PASS |
| Author self-validate | user token on own CREDIT | 403 | 403 | PASS |
| Export CSV | auditeur | 200 | 200 | PASS |
| Export XLSX | auditeur | 200 | 200 | PASS |
| List paginated | `?page=0&size=10` | 200 | 200 | PASS |
| Backend health | `/actuator/health` | UP | UP | PASS |

## Auth ML (complément)

| Test | Observé |
|------|---------|
| GET `/models` sans token (exec container) | HTTP 401 |
| GET `/models` avec `X-Internal-Token` | HTTP 200 |
| `/health/live` public | 200 |

## Notes

- Agents non cochés (`includeAgents=false`) pour stabilité E2E ; ML reste fonctionnel.
- Intégrité : bug d’alignement hash / précision `validatedAt` corrigé (`DecisionHashServiceImpl` + truncature secondes) puis revalidé PASS.
- Pas de Playwright/Cypress UI ; couverture API automatisée des 3 domaines.
