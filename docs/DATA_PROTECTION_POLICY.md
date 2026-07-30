# Politique de protection des données

> Cadre pour la plateforme Traçabilité IA (démonstration technique multidomain).
> **Les modèles sont `DEMO_SYNTHETIC` — non validés pour décisions bancaires, médicales ou académiques réelles.**

> Soft-delete / anonymisation automatisée : **partiellement absents du code** (écart M8) — cette politique définit les règles cibles.

---

## 1. Classification

| Niveau | Exemples plateforme | Traitement |
|--------|---------------------|------------|
| **Public** | Docs ouvertes, health UP/DOWN, pages login | Pas de contrainte PII |
| **Interne** | Métriques agrégées, configs non secrètes, logs techniques sans PII | Accès personnel habilité |
| **Confidentiel** | Comptes utilisateurs, décisions CREDIT/EDUCATION, historiques, exports | Auth JWT + rôles ; pas de partage hors besoin |
| **Sensible** | Justificatifs, commentaires de validation, désaccords IA/humain | Minimisation ; audit des accès |
| **Médical** | Payloads MEDICAL, exports détaillés santé | Règles section 3 — restriction maximale |

---

## 2. Principes communs

- Minimisation : ne collecter que les champs nécessaires au formulaire domaine.
- Limitation des finalités : aide à la décision tracée / démo — pas de revente, pas de profiling externe.
- Accès par rôle (`ADMIN`, validateurs domaine, créateurs).
- Intégrité : hash SHA-256 de snapshot (pas un coffre anti-modification).
- Pas de secrets / tokens dans les logs (`SECRETS_MANAGEMENT.md`).

---

## 3. Règles MEDICAL

**Implémenté / à respecter :**

- Le module médical **n’est pas un diagnostic** ; sortie indicative uniquement.
- Validation humaine obligatoire avant statut final (voir `HUMAN_OVERSIGHT_POLICY.md`).
- Export détaillé MEDICAL : restreindre aux rôles santé / admin ; journaliser l’export (`AuditLog` partiel).

**Cibles / renforcements :**

- Masquage des champs cliniques dans logs et tickets support.
- Interdiction d’entraîner / fine-tuner un modèle externe avec des dossiers MEDICAL réels sans accord juridique.
- Rétention plus courte que CREDIT (voir `DATA_RETENTION_POLICY.md`).
- Pas d’envoi de payload MEDICAL brut vers LLM sans revue de politique (agents = informatifs ; minimiser les champs transmis).

> **WARNING :** traiter toute donnée MEDICAL de staging/prod réelle comme **médicale** même si issue d’un formulaire démo, dès qu’elle peut identifier une personne.

---

## 4. CREDIT / EDUCATION

- CREDIT : données financières personnelles → **Confidentiel** ; pas de modèle « bancaire officiel ».
- EDUCATION : données scolaires → **Confidentiel** ; pas de sanction automatique.

---

## 5. Datasets du dépôt

Les CSV sous `datasets/` sont **synthétiques / fictifs**. Ne pas y injecter de données personnelles réelles. Voir `DATASETS.md` et `LIMITATIONS.md`.

---

## 6. Transferts et sous-traitants

| Flux | Données possibles | Contrôle |
|------|-------------------|----------|
| Backend → ML | Features décision | Réseau privé ; auth ML **planifiée** |
| Backend → Groq / OpenRouter | Contexte agents | Clés env ; minimiser PII |
| Backend → Resend / SMTP | Email reset / support | Pas de dossier médical dans l’email |

---

## 7. Droits personnes (cible organisationnelle)

Accès, rectification, effacement, limitation — procédures humaines via support / admin, tracées. Automatisation GDPR complète = **hors scope technique actuel**.
