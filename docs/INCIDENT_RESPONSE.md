# Réponse aux incidents

> Processus cible pour incidents sécurité, disponibilité, intégrité données ou dérive modèle.
> Outillage on-call formalisé : **partiel / à organiser** selon l’hébergeur.

> Modèles = `DEMO_SYNTHETIC` — un « mauvais score » n’est pas un sinistre métier bancaire/médical, mais une panne technique ou un risque de mauvaise communication l’est.

---

## 1. Severities

| Sévérité | Exemples | Réponse cible |
|----------|----------|---------------|
| **S1** | Fuite données, DB down prod, auth cassée globale | Immédiat ; bridge incident |
| **S2** | ML down, emails reset KO, erreur massive 5xx | < 1 h |
| **S3** | Dégradation latence, un domaine predict KO | Jour ouvré |
| **S4** | Doc / UI mineure | Backlog |

---

## 2. Détection

- Healthchecks frontend / backend / ML
- Alertes monitoring (cible) — voir `MODEL_MONITORING.md`, Actuator
- Signal utilisateur / support
- Anomalie audit / exports

---

## 3. Procédure (tous S1–S2)

1. **Déclarer** : canal ops, severity, horodatage, impact (domaines / users).
2. **Sécuriser** :
   - S1 données : rotation secrets (`SECRETS_MANAGEMENT.md`), restreindre accès
   - Disponibilité : rollback app (`ROLLBACK_RUNBOOK.md`) ou scale
3. **Diagnostiquer** : correlationId, logs **sanitisés**, status `/ready`
4. **Contenir** : maintenance page, désactiver domaine, rate limit si abuse
5. **Corriger** : hotfix / rollback modèle / restore backup
6. **Valider** : smoke staging puis prod
7. **Communiquer** : parties prenantes (sans PII dans les canaux publics)
8. **Post-mortem** sous 5 jours ouvrés (S1–S2)

---

## 4. Incidents types

| Scénario | Première action |
|----------|-----------------|
| Suspicion fuite JWT / clés | Rotation immédiate + invalidation sessions |
| Corruption schéma | Stop writers → backup → restore / migrate |
| Modèle aberrant | `MODEL_ROLLBACK_RUNBOOK.md` |
| Abuse ML ouvert | Couper exposition publique ; auth (cible) |
| Emails spam reset | Couper provider / rate limit support |

---

## 5. Données MEDICAL en incident

- Minimiser le nombre de personnes exposées aux payloads.
- Ne pas coller de dossiers dans Slack/tickets.
- Appliquer `DATA_PROTECTION_POLICY.md`.

---

## 6. Journal d’incident (template)

- ID / severity / timeline
- Impact (service, pas listes nominatives)
- Cause racine
- Actions correctives + preventives
- Liens PR / runbooks utilisés
