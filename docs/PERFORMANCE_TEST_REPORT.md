# Performance Test Report (k6) — 2026-07-30

**Exécution :** Docker `grafana/k6:0.54.0` sur réseau `tracabilite-edge`
**BASE_URL :** `http://tracabilite-backend:8080`
**Auth :** `user@tracabilite.ia` via env (non stocké dans les scripts)

## Smoke (`performance/k6-smoke.js`)

| Métrique | Valeur |
|----------|--------|
| VUs | 1 |
| Durée | 45 s |
| http_reqs | 87 |
| RPS | ~1.90 |
| checks | 100 % (86/86) |
| http_req_failed | **0.00 %** |
| p50 duration | ~24.8 ms |
| p95 duration | **79.3 ms** |
| p99 (approx max) | 122 ms |
| Seuils | PASS (`failed < 5 %`, `p95 < 3s`) |

## Load (`performance/k6-load.js`)

| Métrique | Valeur |
|----------|--------|
| VUs | ramp 0→5→10→0 |
| Durée | ~80 s |
| iterations | 704 |
| http_reqs | 1409 |
| RPS | ~17.45 |
| checks | 100 % (704/704) |
| http_req_failed | **0.00 %** |
| med duration | ~16.9 ms |
| p95 duration | **63.7 ms** |
| max duration | 8.39 s (outlier cold/GC) |
| Seuils | PASS (`failed < 1 %`, `p95 < 2s`) |

## Objectifs documentés

| Objectif | Résultat |
|---------|----------|
| Erreur < 1 % | PASS (0 %) |
| p95 lecture < 500 ms | PASS (~64–79 ms) |
| Pas de 5xx sous smoke | PASS |

## Limites

- Charge limitée à health + list décisions (pas de création ML massive pour ne pas saturer le prototype).
- Pas de mesure DB connections pool sous charge extrême.
