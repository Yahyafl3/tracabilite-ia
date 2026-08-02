# Security Scan Report — 2026-07-30

## npm audit (frontend)

**Commande :** `cd frontend && npm audit`
**Date :** 2026-07-30

| Sévérité | Count |
|----------|------:|
| critical | **0** |
| high | 3 |
| moderate | 4 |
| low | 2 |
| total | 9 |

### HIGH (non corrigés immédiatement — risque accepté documenté)

| Composant | Advisory | Mitigation | Accepté |
|-----------|----------|------------|---------|
| brace-expansion | GHSA-mh99-v99m-4gvg DoS | Dev/transitive ; `npm audit fix` possible | Oui — hors runtime prod SPA build |
| fast-uri | GHSA-v2hh-gcrm-f6hx | Transitive CLI tooling | Oui — toolchain |
| postcss | GHSA-r28c-9q8g-f849 source map | Build-time ; fix via audit fix | Oui — build machine |

Pas de `npm audit fix --force` (breaking Angular CLI).

## pip-audit (ml-service)

**Statut :** **BLOCKED** — `SSLCertVerificationError` vers pypi.org (interception TLS Avast locale). SSL non désactivé.
À relancer sur CI Linux sans interception.

## Trivy images

**Statut :** **PARTIEL** (2026-07-30)

| Image | CRITICAL | Note |
|-------|----------|------|
| `tracabilite-ia-frontend` | **0** | Scan CRITICAL OK |
| `tracabilite-ia-backend` | NOT_TESTED | timeout / erreur d’analyse locale |
| `tracabilite-ia-ml-service` | NOT_TESTED | idem |

Commande cible :

```bash
docker run --rm -v //var/run/docker.sock:/var/run/docker.sock aquasec/trivy:0.58.1 \
  image --severity CRITICAL,HIGH tracabilite-ia-backend:latest
```

## Secrets versionnés

| Check | Résultat |
|-------|----------|
| `.env` tracké | Non (gitignore) |
| `.env.example` | Placeholders only |
| Token dans logs Docker | Aucune occurrence détectée |

## Critère RC

- Zéro critical npm : **PASS**
- Secrets non versionnés : **PASS**
- HIGH documentés : **PASS** (acceptés avec mitigation)
- pip-audit / Trivy complets : **NOT_TESTED** (infrastructure locale)
