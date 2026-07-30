# Runbook — opérations courantes

> Opérations jour-2 pour Traçabilité IA (Compose local, staging, future prod technique).

> Modèles = `DEMO_SYNTHETIC`.

---

## 1. Services et dépendances

| Service | Port typique | Dépend de |
|---------|--------------|-----------|
| postgres | 5432 (local) | — |
| ml-service | 5000 | modèles disque |
| backend | 8080 | postgres, ml, clés LLM optionnelles |
| frontend | 80 | backend |

Réseau Compose : `tracabilite-network`.

---

## 2. Santé quotidienne

```text
GET /actuator/health     (backend)
GET /health , GET /ready (ml-service)
```

Contrôles manuels :

- [ ] Trois domaines ready
- [ ] Espace disque volume Postgres
- [ ] Pas d’erreur 5xx en rafale (logs)
- [ ] Files d’attente validation non bloquées anormalement

---

## 3. Comptes et rôles

- Rôles domaine : `RESPONSABLE_CREDIT`, `PROFESSIONNEL_SANTE`, `RESPONSABLE_PEDAGOGIQUE`, admin, users.
- Prod : **pas** de mots de passe démo `DataInitializer`.
- Création users : UI admin / API users (droits admin).

---

## 4. Tâches récurrentes

| Fréquence | Tâche |
|-----------|--------|
| Quotidien | Health + erreurs |
| Hebdo | Revue versions modèles + disclaimer |
| Hebdo | Rotation logs / taille volumes |
| Mensuel | Revue accès env secrets |
| Avant release | `PRODUCTION_CHECKLIST.md` |
| Trimestriel (cible) | Test restore backup |

---

## 5. Support et email

- Rate limit support applicatif existant (secondes configurables).
- Staging : jamais d’email réel patient/client.
- Si reset password KO : vérifier `EMAIL_PROVIDER`, clés Resend/SMTP, `FRONTEND_URL`.

---

## 6. Exports et audit

- Exports décisions : journaliser (AuditLog partiel implémenté).
- MEDICAL : restreindre qui exporte.
- Hash SHA-256 = intégrité snapshot, pas anti-tamper fort.

---

## 7. Maintenance planifiée

1. Annonce fenêtre.
2. Backup.
3. Déployer / migrer (`DEPLOYMENT_RUNBOOK.md`).
4. Smoke.
5. Clôture.

Urgences : `INCIDENT_RESPONSE.md`.
