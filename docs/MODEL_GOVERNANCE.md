# Gouvernance des modèles ML

> **Statut obligatoire de tous les modèles livrés :** `DEMO_SYNTHETIC`.
> Entraînés sur datasets synthétiques marocains — **non validés** pour décisions réelles bancaires, médicales ou académiques.

> Registre ACTIVE / checksums : **planifié** (écart E10). Artefacts actuels : `ml-service/models/{domain}/` + `metadata.json`.

---

## 1. Domaines

| Domaine | Pipeline typique | Dataset |
|---------|------------------|---------|
| CREDIT | `models/credit/credit_pipeline.joblib` | `datasets/credit/` |
| MEDICAL | `models/medical/...` | `datasets/medical/` |
| EDUCATION | `models/education/...` | `datasets/education/` |

Un **seul** modèle **ACTIVE** par domaine à la fois (règle cible).

---

## 2. Champs registre (cible `metadata.json` / registry)

| Champ | Description |
|-------|-------------|
| `domain` | CREDIT \| MEDICAL \| EDUCATION |
| `modelVersion` | Semver / tag ex. `credit-model-v1.0.0` |
| `datasetVersion` | Tag dataset synthétique |
| `modelType` | Algo (LR, RF, HGB…) |
| `status` | `ACTIVE` \| `CANDIDATE` \| `RETIRED` |
| `governanceStatus` | **`DEMO_SYNTHETIC`** (obligatoire tant que synthétique) |
| `checksumSha256` | Hash du `.joblib` — **planifié** |
| `trainedAt` | Horodatage |
| `metrics` | recall, f1, roc_auc (eval holdout synthétique) |
| `features` | Liste figée des features |
| `owner` | Équipe ML / PFA |
| `approvedForRealDecisions` | Toujours **`false`** pour DEMO_SYNTHETIC |

---

## 3. Cycle de vie (cible)

```
train → CANDIDATE → (revue) → ACTIVE (1 seul / domaine) → RETIRED
```

1. Entraîner via `ml-service/training/train_*_model.py` (voir `MODEL_TRAINING.md`).
2. Déposer artefacts + `metadata.json` avec `governanceStatus=DEMO_SYNTHETIC`.
3. Vérifier checksum et smoke `/predict/{domain}`.
4. Basculer ACTIVE : l’ancien passe RETIRED (garder fichiers pour rollback).
5. Interdit : deux ACTIVE sur le même domaine.

**Aujourd’hui :** fichiers sur disque chargés au démarrage ; pas d’API de bascule ACTIVE formalisée.

---

## 4. Interdictions

- Présenter un score comme décision exécutoire.
- Déployer un modèle entraîné sur PII réelle sans validation juridique + `governanceStatus` mis à jour.
- Écraser l’ACTIVE sans conserver l’artefact précédent.
- Utiliser MEDICAL comme diagnostic.

---

## 5. Traçabilité décision

Chaque décision doit conserver (déjà partiellement en place) :

- version / type modèle lorsque disponible
- facteurs techniques (SHAP / importances)
- avis agents LLM **informatifs seulement** (ne modifient pas la prédiction)

---

## 6. Revue périodique

- Après tout réentraînement
- Avant tout passage staging → prod technique
- Si dérive détectée (`MODEL_MONITORING.md`)

Checklist revue : metrics, disclaimer DEMO_SYNTHETIC, features inchangées ou documentées, rollback prêt.
