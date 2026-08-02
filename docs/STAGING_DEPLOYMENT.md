# Déploiement staging

> Staging = copie **technique** proche de la prod, **sans** données personnelles réelles ni emails réels de clients.

> Classification actuelle plateforme : `NOT_READY` (voir `PRODUCTION_READINESS_REPORT.md`). Staging-ready = objectif de remédiation, pas l’état audit initial.

> Modèles = `DEMO_SYNTHETIC`.

---

## 1. Staging vs production

| Aspect | Staging | Production |
|--------|---------|------------|
| Données | Synthétiques / fictives uniquement | Interdit : imports dossiers réels tant que pas cadre légal |
| Emails | Catch-all / Resend test / désactivé | Provider réel, listes contrôlées |
| Secrets | Dédiés staging (≠ prod) | Coffre prod |
| Swagger | OFF ou auth | OFF |
| `ddl-auto` | `validate` (+ migrations) | `validate` |
| Seeds démo | Comptes test connus OK si réseau restreint | Interdits |
| ML port | Privé | Privé |
| URL | Sous-domaine staging | Domaine prod |

---

## 2. Données et emails — règles strictes

**Interdit en staging :**

- Dump Neon prod contenant de vrais usagers
- Envoi d’emails à de vraies adresses patients / clients
- Clés API prod partagées « pour aller plus vite »

**Autorisé :**

- Datasets synthétiques `datasets/`
- Boîtes `+staging@` / provider test
- `EMAIL_PROVIDER=resend` avec destinataires whitelist équipe

---

## 3. Procédure déploiement staging

1. Backup staging actuel (si données de test à conserver).
2. Appliquer migrations (`DATABASE_MIGRATION_RUNBOOK.md`).
3. Déployer ml-service (3 pipelines) → `/ready`.
4. Déployer backend profil proche prod (`validate`, pas de seed dangereux non voulu).
5. Déployer frontend pointant `API_URL` staging.
6. Smoke :

   - [ ] Login compte test
   - [ ] Création CREDIT / MEDICAL / EDUCATION
   - [ ] Validation file d’attente
   - [ ] Reset password **vers boîte test**
   - [ ] Disclaimer DEMO visible / connu de l’équipe

7. Noter versions images + `modelVersion` par domaine.

---

## 4. Promotion vers prod

Uniquement si checklist `PRODUCTION_CHECKLIST.md` verte **et** modèles toujours labellisés non décisionnels métier.

Pas de copie DB staging → prod avec comptes test.

---

## 5. Teardown / reset

Utiliser scripts **reset local** uniquement sur bases jetables. Jamais sur Neon prod.
