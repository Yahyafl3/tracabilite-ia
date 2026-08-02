# Documentation réseau Docker — Traçabilité IA

## Objectif

Séparer le trafic **interne** (DB, ML) du trafic **edge** (navigateur → frontend/backend).

## Réseaux

| Réseau | Membres | Rôle |
|--------|---------|------|
| `tracabilite-internal` | postgres, ml-service, backend | Données et prédictions |
| `tracabilite-edge` | frontend, backend | Exposition HTTP utilisateur |

Le frontend **ne rejoint pas** `tracabilite-internal` : il ne peut pas appeler Flask directement.
Les appels ML passent uniquement par Spring (`ML_SERVICE_URL=http://ml-service:5000`).

## Ports

### Mode production-like (base `docker-compose.yml`)

| Service | Host | Container |
|---------|------|-----------|
| postgres | **non publié** | `expose: 5432` |
| ml-service | **non publié** | `expose: 5000` |
| backend | `8080` | `8080` |
| frontend | `80` | `80` |

Commande :

```bash
docker compose -f docker-compose.yml up -d --build
```

Vérifier l’inaccessibilité host :

```bash
# Doit échouer / timeout
Test-NetConnection localhost -Port 5432
Test-NetConnection localhost -Port 5000
# Doit réussir
Invoke-RestMethod http://localhost:8080/actuator/health
```

### Mode développement local (override)

`docker-compose.override.yml` republie `5432` et `5000` pour debug.

```bash
docker compose up -d   # charge automatiquement override.yml
```

## Authentification interne ML

Header : `X-Internal-Token`
Variable : `ML_SERVICE_TOKEN` (Spring + Flask)

- Public : `/health/live`, `/health`, `/ready`, `/health/ready` (healthchecks)
- Protégé : `/predict*`, `/models*`, `/schema`, `/explain`, `/model/info`

`/health/ready` reste **public** volontairement pour les probes Docker ; le port 5000 n’étant pas publié en production-like, l’exposition est limitée au réseau interne.

## Interdit

- Ne jamais exposer Postgres/ML sur Internet.
- Ne jamais mettre `ML_SERVICE_TOKEN` dans le frontend.
- Ne jamais logger le token.
