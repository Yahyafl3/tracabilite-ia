# Monitoring des modèles

> **État :** pas de stack Prometheus/Grafana dédiée (écart M4). Ce document définit les **métriques cibles** et seuils documentés ; l’alerting automatisé est **planifié**.

> Mesurer **sans PII** : pas de CIN, noms, emails, payloads MEDICAL bruts dans les métriques.

> Modèles = `DEMO_SYNTHETIC` — les seuils ci-dessous servent la démo technique, pas une certification métier.

---

## 1. Métriques autorisées (agrégats)

| Métrique | Labels autorisés | Interdit |
|----------|------------------|----------|
| Volume prédictions | `domain`, `model_version`, `status_code` | user_id, decision_id en clair si corrélable PII |
| Latence p50/p95 | `domain`, endpoint | body request |
| Taux erreur 4xx/5xx | `domain` | stack avec données patient |
| Distribution score (histogramme buckets) | `domain`, `model_version` | score lié à une identité |
| Taux désaccord humain / IA | `domain` | commentaires libres |
| Disponibilité `/ready` | service | — |

Health déjà utile : `/health`, `/ready` (ml-service).

---

## 2. Dérive (data / prediction) — seuils documentés

Seuils **initiaux documentés** (à recalibrer après baseline staging) :

| Signal | Fenêtre | Seuil d’alerte | Action |
|--------|---------|----------------|--------|
| Taux erreur 5xx ML | 15 min | > 5 % | Page ops + vérifier ml-service |
| Latence p95 `/predict/*` | 15 min | > 3 s | Scale / investiguer SHAP |
| Part scores extrêmes (bucket 0–0.05 ou 0.95–1) | 24 h | Écart > **20 points** vs baseline 7 j | Revue dérive |
| PSI features numériques (si calculé) | 7 j | PSI > **0.25** | Alerte dérive data |
| Taux désaccord humain | 7 j | Hausse > **15 points** vs baseline | Revue modèle + process |
| Chute volume | 1 h | < 10 % du volume habituel heure | Vérifier front/backend |

Baseline : calculer sur trafic **synthétique / staging** uniquement tant que pas de prod métier.

---

## 3. Ce qu’on ne monitore pas en clair

- Features individuelles d’une personne
- Textes agents LLM complets
- Exports CSV
- Jetons JWT

---

## 4. Alerting (cible)

1. Alerte warning → canal ops.
2. Alerte critique (5xx, ready down) → incident `INCIDENT_RESPONSE.md`.
3. Alerte dérive → owner ML + éventuel `MODEL_ROLLBACK_RUNBOOK.md`.

---

## 5. Revue hebdomadaire (manuel tant que pas d’outil)

- [ ] `/ready` OK pour 3 domaines
- [ ] Versions ACTIVE = attendues
- [ ] Pas d’anomalie volume / erreurs
- [ ] Rappeler disclaimer DEMO_SYNTHETIC aux parties prenantes
