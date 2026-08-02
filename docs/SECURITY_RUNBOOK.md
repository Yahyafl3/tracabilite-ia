# Runbook — sécurité opérationnelle

> Actions concrètes sécurité pour ops/dev. Politiques connexes : `SECRETS_MANAGEMENT.md`, `DATA_PROTECTION_POLICY.md`, `SECURITY_AND_PRIVACY.md`, `PRODUCTION_GAP_ANALYSIS.md`.

> **État audit :** plusieurs contrôles encore absents (rate limit global, auth ML, Swagger public, défauts JWT). Ce runbook indique le **mode opératoire** et les **gaps** restants.

---

## 1. Surface d’attaque (rappel)

| Surface | Mitigation actuelle | Gap |
|---------|---------------------|-----|
| API JWT | BCrypt + JWT + `@PreAuthorize` | Secret défaut ; rate limit login absent |
| Swagger | Public | Doit être OFF prod |
| ML :5000 | Aucune auth | Auth + réseau privé **planifiés** |
| Postgres :5432 | Exposé en Compose | Ne pas exposer en prod |
| Agents LLM | Clés env | Minimiser PII envoyée |
| Actuator | Health public | Lockdown détails |

---

## 2. Durcissement au déploiement

- [ ] Pas de ports DB/ML publics
- [ ] HTTPS front + backend
- [ ] `JWT_SECRET` fort, fail-fast (cible)
- [ ] Swagger OFF
- [ ] Seeds démo OFF hors local
- [ ] Logs INFO, pas de SHOW_SQL
- [ ] CORS restreint à `FRONTEND_URL`
- [ ] Comptes admin MFA côté organisation (hors app si non supporté)

---

## 3. Procédures ops sécurité

### Compromission suspectée

1. Rotation JWT + clés API + DB password.
2. Revue `AuditLog` / historiques connexions.
3. Invalider sessions (redéploiement JWT).
4. Incident S1.

### Compte admin perdu / départ

1. Désactiver compte.
2. Révoquer accès consoles cloud.
3. Rotater secrets s’il y avait accès env.

### Revue accès (mensuelle)

- Qui a les env prod ?
- Clés LLM encore nécessaires ?
- Exports MEDICAL récents légitimes ?

---

## 4. Développement sécurisé

- Pas de secrets dans le code / PR.
- Pas d’exposer `ex.getMessage()` verbeux en prod (écart E3 à corriger).
- Tests `@PreAuthorize` à maintenir (ex. `DecisionControllerUserSecurityTest`).
- Ne pas logger payloads MEDICAL.

---

## 5. Scanner / CI (cible)

- Dependabot / audit npm & Maven — **CI complète absente** (M7).
- Ne pas bloquer un RC technique uniquement sur l’absence de CI, mais la traiter avant prod réelle.

---

## 6. Contacts

Maintenir hors dépôt : owners sécurité, hébergeur, canal incident.
