# Politique de supervision humaine

> Human-in-the-loop obligatoire pour toute décision à statut final sur la plateforme.
> Les scores ML et avis LLM sont des **aides** — modèles `DEMO_SYNTHETIC`.

---

## 1. Règle fondamentale

Aucune décision CREDIT / MEDICAL / EDUCATION ne doit être considérée comme **opposable** ou **exécutée métier** sur la seule base du modèle.

Le validateur humain du domaine porte la responsabilité de la décision enregistrée (approbation, rejet, ajustement selon statuts applicatifs).

---

## 2. Rôles

| Rôle | Responsabilité oversight |
|------|--------------------------|
| Créateur | Saisie ; ne valide pas sa propre décision en production (à renforcer par tests — écart M9) |
| `RESPONSABLE_CREDIT` | Validation file CREDIT |
| `PROFESSIONNEL_SANTE` | Validation MEDICAL |
| `RESPONSABLE_PEDAGOGIQUE` | Validation EDUCATION |
| Admin | Administration ; pas un substitut métier silencieux |

---

## 3. Parcours attendu

1. Création + analyse ML (+ agents informatifs).
2. Statut en attente de validation humaine.
3. Revue : score, facteurs, contexte, disclaimer DEMO.
4. Décision humaine + commentaire / justification.
5. Historique + hash d’intégrité conservés.

**Interdit :** bypass UI pour forcer un statut final sans passage validation (sauf scripts locaux de test documentés).

---

## 4. Désaccord humain / IA

- Le désaccord est **normal** et doit pouvoir être tracé.
- Le score n’est pas « corrigé » rétroactivement pour coller à l’humain ; on enregistre la décision humaine.
- Surcote de désaccords → revue process / modèle (`MODEL_MONITORING.md`), pas suppression d’historiques.

---

## 5. MEDICAL — oversight renforcé

- Un professionnel de santé (rôle dédié) pour toute clôture MEDICAL en environnement partagé.
- Pas de file d’attente « auto-approuvée » sur seuil de confiance.
- Exports contrôlés.

---

## 6. Escalade

Si pression à « automatiser la validation » pour gagner du temps :

1. Rappeler `AI_USAGE_POLICY.md` et le statut DEMO_SYNTHETIC.
2. Refuser l’automation de la décision finale tant que gouvernance métier non établie.
3. Documenter la demande comme changement de politique (hors simple feature).

---

## 7. Preuves de conformité process

Conserver (cible) : qui a validé, quand, quel modèle version, quel statut final — via historiques / AuditLog existants (couverture à compléter).
