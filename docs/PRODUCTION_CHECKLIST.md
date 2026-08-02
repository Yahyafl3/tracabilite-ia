# Checklist production (release candidate technique)

> Cocher uniquement ce qui est **vérifié**. État initial post-audit : la plupart des items sont **non satisfaits** — voir `PRODUCTION_READINESS_REPORT.md` (`NOT_READY`).

> **Les modèles CREDIT / MEDICAL / EDUCATION restent `DEMO_SYNTHETIC` même si la checklist technique passe.** Ils ne sont pas validés pour de vraies décisions.

---

## 1. Configuration & secrets

- [ ] Profil `prod` actif (ou équivalent durci)
- [ ] `ddl-auto=validate`
- [ ] `JWT_SECRET` fort, sans défaut, fail-fast
- [ ] Mots de passe DB non triviaux
- [ ] Aucun secret dans Git
- [ ] Seeds comptes démo **désactivés**
- [ ] Logging INFO ; `SHOW_SQL=false`

## 2. Réseau & exposition

- [ ] Postgres non publié publiquement
- [ ] ML non publié publiquement + auth service-à-service (cible)
- [ ] HTTPS
- [ ] CORS allowlist stricte
- [ ] Swagger / OpenAPI OFF
- [ ] Endpoints AI de test publics désactivés ou protégés

## 3. Données & migrations

- [ ] Backup pré-deploy testé (restore smoke)
- [ ] Migrations versionnées appliquées (Flyway ou scripts)
- [ ] Rollback SQL validé en staging
- [ ] Pas de dump PII versionné

## 4. ML & gouvernance

- [ ] `/ready` OK 3 domaines
- [ ] `metadata.json` : `DEMO_SYNTHETIC` / `approvedForRealDecisions=false`
- [ ] Un ACTIVE par domaine (cible registre)
- [ ] Artefact précédent archivé (rollback)
- [ ] Disclaimer visible pour les utilisateurs / validateurs

## 5. Sécurité applicative

- [ ] Rate limiting login / exports (cible si pas encore livré = **FAIL**)
- [ ] Erreurs 500 sans fuite de détails
- [ ] Actuator lockdown
- [ ] Revue rôles validateurs domaine
- [ ] Exports MEDICAL restreints

## 6. Observabilité & ops

- [ ] Healthchecks branchés sur l’orchestrateur
- [ ] Runbooks lus par l’équipe ops
- [ ] Canal incident défini
- [ ] RPO/RTO aspirants acceptés explicitement comme non prouvés **ou** mesurés

## 7. Tests

- [ ] Suites backend / frontend / ml vertes sur le tag
- [ ] Smoke staging multidomain
- [ ] Pas de régression validation humaine

## 8. Gouvernance IA & données

- [ ] `AI_USAGE_POLICY.md` communiquée
- [ ] `HUMAN_OVERSIGHT_POLICY.md` respectée (pas d’auto-décision finale)
- [ ] Rétention / classification acceptées par le métier démo

---

## Verdict rapide

| Si… | Alors… |
|-----|--------|
| Un item section 1–3 non coché | **Ne pas** déployer en prod |
| Technique OK mais modèles présentés comme décisionnels | **Interdit** |
| Staging OK, prod secrets/DB non prêts | Rester `STAGING_READY` au mieux |
