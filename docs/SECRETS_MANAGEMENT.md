# Gestion des secrets

> **Règle d’or :** secrets **uniquement** via variables d’environnement (ou coffre type vault) — jamais dans le code, les images, les tickets, ni les commits.

> État actuel : `.env.example` OK ; défauts faibles JWT/DB encore présents dans `application.properties` / compose — **à éliminer en prod**.

---

## 1. Inventaire des secrets

| Secret | Usage | Criticité |
|--------|--------|-----------|
| `JWT_SECRET` | Signature tokens | Critique |
| `DATABASE_PASSWORD` / URL complète | Accès Postgres | Critique |
| `GROQ_API_KEY` | Agents LLM | Élevée |
| `OPENROUTER_API_KEY` | Agents (legacy / optionnel) | Élevée |
| `RESEND_API_KEY` | Email cloud | Élevée |
| `MAIL_PASSWORD` | SMTP | Élevée |
| Mots de passe comptes admin | Auth applicative | Critique |

Non-secrets (config) : URLs publiques, noms de modèles LLM, timeouts.

---

## 2. Stockage

**Autorisé :**

- Variables d’environnement de la plateforme (Render, etc.)
- Fichier `.env` **local** gitignoré
- Coffre secrets (Vault, Doppler, etc.) — **planifié / recommandé**

**Interdit :**

- Commit Git (y compris historique)
- README, issues, captures d’écran
- Images Docker hardcodées
- Backups SQL versionnés contenant PII / mots de passe
- Logs applicatifs

---

## 3. Rotation

| Secret | Fréquence cible | Procédure courte |
|--------|-----------------|------------------|
| `JWT_SECRET` | 90 jours ou compromission | Générer nouveau → redéployer backend → **invalide toutes les sessions** |
| DB password | 90–180 jours | Changer côté DB → maj env → rolling restart |
| Clés API LLM / Resend | 90 jours ou alerte fuite | Révoquer ancienne → injecter nouvelle → smoke email/agents |
| Comptes admin | Après départ / incident | Reset forcé + revue audit |

Après rotation JWT : prévenir les utilisateurs (reconnexion obligatoire).

---

## 4. Ce qu’il ne faut **jamais** logger

- `JWT_SECRET`, tokens Bearer, refresh
- Mots de passe, hash BCrypt en clair, tokens reset password
- Clés API (`GROQ_*`, `OPENROUTER_*`, `RESEND_*`, SMTP)
- Corps complets de décisions MEDICAL (données sensibles)
- Dumps de `Authorization`, cookies, query `token=`
- Contenu de `.env`

Le sanitizer existant (`SensitiveDataSanitizer`) aide — **ne pas** le contourner dans de nouveaux logs DEBUG.

---

## 5. Checklist avant release

- [ ] Aucun secret réel dans le repo (`git grep` / scan)
- [ ] Prod sans défaut JWT/DB
- [ ] `.env` absent du tracking
- [ ] Backups `backend/scripts/backups/*.sql` revus (risque données démo versionnées — écart audit)
- [ ] Accès env prod limité aux ops / admins

---

## 6. En cas de fuite suspectée

1. Révoquer / rotater immédiatement le secret concerné.
2. Invalider sessions (rotation JWT).
3. Ouvrir un incident (`INCIDENT_RESPONSE.md`).
4. Auditer accès DB / exports récents.
5. Ne pas republier l’ancien secret « pour debug ».
