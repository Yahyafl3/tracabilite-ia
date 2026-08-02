# Runbook — rollback applicatif

> Remettre en service une version **précédente connue bonne** (frontend, backend, ml-service, éventuellement schéma).

> Distinguer : rollback **app** (ce doc) vs rollback **modèle seul** (`MODEL_ROLLBACK_RUNBOOK.md`) vs **restore DB** (`BACKUP_RESTORE_RUNBOOK.md`).

---

## 1. Décision rapide

| Symptôme | Action préférée |
|----------|-----------------|
| Front cassé, API OK | Rollback frontend seul |
| API 5xx / boot fail | Rollback backend (+ vérifier DB) |
| Predict KO | Rollback ml-service / modèle |
| Schéma incompatible | Restore DB ou re-migrate + images alignées |
| Fuite secret | Rotation **puis** rollback si besoin |

---

## 2. Procédure générale

1. Déclarer incident (severityité S1/S2).
2. Geler nouveaux déploiements.
3. Identifier dernier tag/image **healthy**.
4. Redéployer ce tag (même ordre inverse si multi-services : front → backend → ml selon dépendance).
5. Si migration avant-cassante : **ne pas** laisser la nouvelle app sur un vieux schéma — aligner (rollback SQL ou restore).
6. Smoke tests.
7. Communiquer + post-mortem.

---

## 3. Backend

1. Redéployer image précédente.
2. Confirmer `ddl-auto=validate` et boot OK.
3. Si la nouvelle migration est déjà appliquée et non rétrocompatible :
   - Option A : script `*_rollback.sql` (staging validé d’abord)
   - Option B : restore backup pré-migration
4. Invalider caches CDN si applicable.

---

## 4. Frontend

1. Republier build précédent (`API_URL` inchangé vers backend healthy).
2. Hard refresh / vider cache CDN.

---

## 5. ML

Voir `MODEL_ROLLBACK_RUNBOOK.md` ou redéployer tag image précédent.

---

## 6. Vérifications post-rollback

- [ ] Login
- [ ] Lecture décisions existantes
- [ ] Pas de création partielle corrompue
- [ ] Versions affichées / logs cohérentes
- [ ] Disclaimer modèles DEMO toujours assumé

---

## 7. Interdits

- Force-push pour « corriger » l’historique Git en urgence sans accord.
- Restore prod Neon non validé depuis une doc générique.
- Effacer des décisions pour masquer l’incident.
