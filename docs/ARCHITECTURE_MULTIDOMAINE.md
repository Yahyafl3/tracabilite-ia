# Architecture multidomain — Traçabilité IA

## Vue d’ensemble

La plateforme gère **trois domaines métier séparés** :

| Domaine | Cas d’usage | Validateur | Décisions humaines |
|---------|-------------|------------|--------------------|
| CREDIT | Risque de défaut (synthétique Maroc) | RESPONSABLE_CREDIT | ACCEPTEE, REFUSEE, A_REVOIR |
| MEDICAL | Risque diabète (indicatif) | PROFESSIONNEL_SANTE | SUIVI_STANDARD, EXAMEN_COMPLEMENTAIRE, ORIENTATION_SPECIALISTE, A_REVOIR |
| EDUCATION | Risque décrochage | RESPONSABLE_PEDAGOGIQUE | AUCUNE_INTERVENTION, ACCOMPAGNEMENT, ENTRETIEN_PEDAGOGIQUE, TUTORAT, ORIENTATION, A_REVOIR |

Chaque domaine possède son dataset, pipeline, modèle, formulaire Angular, endpoint Flask et logique de validation.

Les fonctions de **traçabilité restent communes** : recommandation IA, score, facteurs, décision humaine, audit, SHA-256.

## Séparation des couches

```
Angular (3 formulaires)
    → Spring Boot (Decision + *DecisionData + Orchestrator)
        → Flask (/predict/credit|medical|education)
            → pipelines joblib par domaine
```

## Compatibilité

- Endpoint legacy `POST /api/decisions/analyze` et Flask `POST /predict` conservés.
- Décisions existantes migrées avec `domaine = CREDIT`.
- Rôle `VALIDATEUR` encore accepté pour validation (compat).
- `ADMINISTRATEUR` n’est **pas** validateur métier par défaut.

## Entités

- `Decision` (commune) + `CreditDecisionData` / `MedicalDecisionData` / `EducationDecisionData` (1-1)
- `AuditLog` pour les actions importantes

## Distinction importante

- **Secteur économique** (`SERVICES`, `INDUSTRIE`…) : feature du formulaire **CRÉDIT uniquement**.
- **Domaine de décision** (`CREDIT`, `MEDICAL`, `EDUCATION`) : premier champ de la plateforme.
