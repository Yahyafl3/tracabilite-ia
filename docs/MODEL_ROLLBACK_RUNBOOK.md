# Runbook — rollback d’un modèle ML

> Objectif : revenir au pipeline précédent pour un domaine (CREDIT, MEDICAL ou EDUCATION) sans toucher inutilement à la base applicative.

> Tous les modèles = `DEMO_SYNTHETIC`. Un rollback ne « valide » pas un modèle pour la prod métier.

> Automatisation registre ACTIVE : **planifiée**. Procédure actuelle = fichiers + redémarrage ml-service.

---

## 1. Prérequis

- [ ] Artefact précédent conservé (joblib + metadata) — ex. `models/{domain}/archive/`
- [ ] Checksum / version connus
- [ ] Accès déploiement ml-service
- [ ] Fenêtre courte acceptée (prédictions en erreur pendant restart)

---

## 2. Déclencheurs

- Métriques hors seuils (`MODEL_MONITORING.md`)
- Bugs de préprocessing / schéma features
- Déploiement accidentel d’une mauvaise version
- Incident sécurité sur artefact

---

## 3. Procédure

### 3.1 Préparer

1. Identifier le domaine impacté.
2. Noter `modelVersion` ACTIVE actuelle (logs, `metadata.json`, endpoint `/models` si dispo).
3. Localiser l’artefact **précédent** validé.

### 3.2 Basculer fichiers

1. Déplacer / renommer le pipeline défaillant hors chemin de chargement (ne pas supprimer tout de suite).
2. Restaurer `*_pipeline.joblib` + `metadata.json` de la version précédente.
3. Vérifier que `governanceStatus` reste `DEMO_SYNTHETIC`.
4. S’assurer **un seul** jeu de fichiers ACTIVE par domaine.

### 3.3 Redémarrer

```bash
# Exemple Compose local / staging auto-hébergé
docker compose restart ml-service
```

Attendre `/health` et `/ready` OK.

### 3.4 Valider

- [ ] `GET /models` (ou lecture metadata) = version attendue
- [ ] Smoke `POST /predict/{domain}` avec payload schéma
- [ ] Créer une décision test depuis le backend (staging)
- [ ] Confirmer que les nouvelles décisions tracent la bonne version

### 3.5 Clôturer

- Journaliser : domaine, from→to version, auteur, motif
- Ouvrir / mettre à jour l’incident si applicable
- Planifier analyse post-mortem du modèle retiré

---

## 4. Rollback avec image Docker

Si les modèles sont **bakés** dans l’image :

1. Redéployer le tag d’image ml-service **précédent**.
2. Ne pas mixer volumes montés et bakés sans vérifier la priorité de chargement (`docker-entrypoint.sh`).

---

## 5. Ce qu’on ne fait pas

- Modifier rétroactivement les décisions déjà persistées (garder la vérité historique).
- « Corriger » en réentraînant en urgence sans archive.
- Rollback schéma DB pour un problème purement ML.

---

## 6. Escalade

Si le modèle précédent est aussi défaillant : désactiver le domaine (feature flag / maintenance) plutôt que servir des scores faux — **procédure feature flag = à confirmer selon déploiement**.
