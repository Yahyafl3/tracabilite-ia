# Configuration production — Traçabilité IA

> **Modèles :** CREDIT / MEDICAL / EDUCATION = `DEMO_SYNTHETIC` — non validés pour décisions réelles.

## Profils Spring

| Profil | Usage | `ddl-auto` | Seeds | Swagger | Token ML |
|--------|--------|------------|-------|---------|----------|
| `local` | Docker / poste | `update` | ON | ON | défaut local (à remplacer) |
| `test` | CI | `create-drop` | OFF | OFF | token de test |
| `prod` | Staging / prod | **`validate`** | **OFF** | **OFF** | **obligatoire** ≥24 chars |

```bash
SPRING_PROFILES_ACTIVE=prod
```

## Variables obligatoires (prod)

| Variable | Règle |
|----------|--------|
| `JWT_SECRET` | ≥ 32 chars, pas de valeur faible |
| `ML_SERVICE_TOKEN` | ≥ 24 chars ; header `X-Internal-Token` |
| `SPRING_DATASOURCE_URL` / user / password | Pas de défauts demo |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` |
| `FRONTEND_URL` | Origine CORS HTTPS |
| `ML_SERVICE_URL` | URL **interne** (`http://ml-service:5000`) |

Fail-fast : `ProductionEnvironmentValidator` (profil `prod`).

## Auth Spring ↔ ML

1. Spring lit `ml.service.token=${ML_SERVICE_TOKEN}` et envoie `X-Internal-Token` sur chaque appel RestClient.
2. Flask vérifie via `hmac.compare_digest` (`ml-service/internal_auth.py`).
3. Public : `/health/live`, `/ready`, `/health/ready` (probes Docker).
4. Protégé : `/predict*`, `/models*`, `/schema`, `/explain`, `/model/info`.
5. Token jamais loggé ni renvoyé.

Voir aussi `docs/DOCKER_NETWORK_SECURITY.md`.

## Docker

- Production-like : `docker compose -f docker-compose.yml up -d` (pas de ports 5432/5000 host).
- Local debug : `docker compose up -d` (charge `docker-compose.override.yml`).

## Actuator

Prod : `health`, `info`, `prometheus` — pas de détails santé publics.
