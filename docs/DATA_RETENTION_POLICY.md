# Politique de rétention des données

> **État code :** soft-delete / jobs d’anonymisation **non formalisés** (écart audit M8). Ce document fixe la **stratégie cible** pour un RC technique.

> Modèles = `DEMO_SYNTHETIC`. Ne pas conserver de données réelles « pour améliorer le modèle » sans cadre juridique.

---

## 1. Objectifs

- Limiter la durée de conservation au besoin de traçabilité / audit.
- Distinguer **suppression** (effacement) et **anonymisation** (conservation statistique sans ré-identification).
- Protéger particulièrement MEDICAL.

---

## 2. Durées cibles (à valider métier)

| Catégorie | Rétention cible | Action fin de période |
|-----------|-----------------|------------------------|
| Comptes utilisateurs inactifs | 24 mois après dernier login | Anonymiser ou supprimer |
| Décisions CREDIT | 36 mois après clôture | Anonymiser features ; garder agrégats audit |
| Décisions EDUCATION | 24 mois après clôture | Anonymiser |
| Décisions MEDICAL | **12 mois** après clôture (plus strict) | Supprimer détail clinique ou anonymiser fort |
| Tokens reset password | TTL applicatif (~20 min) | Déjà expirants |
| Logs applicatifs | 30–90 jours | Rotation / purge |
| Backups DB | 7–30 jours (selon offre) | Purge chiffrée |
| Exports téléchargés | Responsabilité du client ; pas de re-stockage serveur | — |
| Datasets synthétiques repo | Durée de vie du projet démo | Pas de PII réelle |

> Ces durées sont **aspirations organisationnelles**, pas des jobs déjà déployés.

---

## 3. Suppression

**Quand :** demande légitime, fin de rétention, compte test, incident fuite.

**Procédure cible :**

1. Identifier les entités (user, décisions, historiques, audit liés).
2. Vérifier contraintes d’intégrité / hash (recalcul ou marqueur « purged »).
3. Supprimer ou soft-delete selon capacité future du code.
4. Purger caches / exports temporaires.
5. Journaliser l’action (qui / quoi / quand) **sans** re-logger le contenu sensible.
6. Confirmer non-restauration depuis le backup le plus récent hors politique légale.

**Local uniquement :** `reset-local-decisions.sql` — **interdit** en staging/prod.

---

## 4. Anonymisation (stratégie)

Techniques cibles :

- Remplacer identifiants utilisateur par `user_anon_{hash court}`.
- Bucketiser âges, revenus, régions (agrégats).
- Supprimer commentaires libres et justifications textuelles.
- Conserver : domaine, statut final, timestamps mois, scores agrégés, version modèle — pour stats **sans** ré-identification.

MEDICAL : préférer **suppression du détail** plutôt qu’une anonymisation faible.

---

## 5. Backups vs rétention

Un enregistrement « effacé » en base live peut survivre dans les backups jusqu’à expiration du cycle. Documenter le délai max de survie (aligné sur rétention backups).

---

## 6. Responsabilités

| Rôle | Responsabilité |
|------|----------------|
| Admin plateforme | Exécuter / valider purges |
| Ops | Cycles backup / purge |
| Métier domaine | Valider délais CREDIT / MEDICAL / EDUCATION |
| Dev | Ne pas ajouter de stockage PII hors tables prévues |
