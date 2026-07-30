# Runbook — déploiement

> Déploiement ordonné des couches : DB → ML → Backend → Frontend.
> Environnements cloud historiques (Vercel / Render / Neon) : **ne pas modifier à l’aveugle** ; suivre les consoles respectives.

> Prérequis RC : profils prod, `ddl-auto=validate`, secrets fail-fast — **planifiés** si non encore mergés. Sinon rester en staging.

> Modèles = `DEMO_SYNTHETIC`.

---

## 1. Pré-déploiement

- [ ] Tag / commit connu déployé
- [ ] Migrations préparées + backup (`DATABASE_MIGRATION_RUNBOOK.md`, `BACKUP_RESTORE_RUNBOOK.md`)
- [ ] Secrets env à jour (pas de défauts)
- [ ] Checklist `PRODUCTION_CHECKLIST.md` revue
- [ ] Fenêtre communiquée
- [ ] Plan de rollback prêt (`ROLLBACK_RUNBOOK.md`)

---

## 2. Ordre de déploiement

### 2.1 Base de données

1. Backup.
2. Appliquer migrations versionnées (staging d’abord, puis prod hors bande).
3. Vérifier schéma.

### 2.2 ML service

1. Déployer image avec pipelines des 3 domaines (ou volume modèles).
2. Attendre `/health` + `/ready`.
3. Noter `modelVersion` ACTIVE par domaine (`DEMO_SYNTHETIC`).

### 2.3 Backend

1. `SPRING_PROFILES_ACTIVE=prod` (cible).
2. `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`.
3. JWT / DB / `FRONTEND_URL` / `ML_SERVICE_URL` injectés.
4. Déployer ; vérifier boot sans seed démo.
5. Smoke : `/actuator/health`, login API.

### 2.4 Frontend

1. Build avec `API_URL` correct.
2. Déployer (Nginx / Vercel).
3. Smoke UI : login, nouvelle décision, validation.

---

## 3. Compose local (référence dev — pas prod)

```bash
docker compose build
docker compose up -d
docker compose ps
```

Vérifier que les mots de passe compose par défaut **ne** sont **pas** réutilisés en prod.

---

## 4. Post-déploiement

- [ ] Trois domaines prédictibles
- [ ] Agents LLM (si clés présentes) non bloquants si down
- [ ] Reset password (staging : boîte test)
- [ ] Swagger inaccessible en prod
- [ ] Logs sans secrets / PII MEDICAL
- [ ] Annoncer fin de fenêtre

---

## 5. Abort

Si health KO après 15 min : exécuter `ROLLBACK_RUNBOOK.md`, ne pas « empiler » des hotfixes non tracés.
