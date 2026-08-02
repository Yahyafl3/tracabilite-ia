# Guide de migration multidomain

## Avant de migrer la production

1. Sauvegarde PostgreSQL / Neon.
2. Déployer le ml-service avec les 3 pipelines (ou générer au démarrage).
3. Déployer le backend (Hibernate `ddl-auto=update` ou script SQL).
4. Déployer le frontend.
5. Vérifier `/health`, `/ready`, login, reset password, analyse legacy.

## Script SQL

```
backend/scripts/migration/V1__multidomain_decisions.sql
backend/scripts/migration/V1__multidomain_decisions_rollback.sql
```

Les décisions existantes reçoivent `domaine = 'CREDIT'`. Aucune suppression.

## Rollback

1. Revenir à l’image backend précédente.
2. Exécuter le script rollback **uniquement** si les nouvelles tables peuvent être perdues.
3. Les colonnes ajoutées peuvent rester sans impact sur l’ancien code si ignorées.

## Risques

- Nouveaux rôles validateurs à créer en admin.
- Formulaires Angular branchés sur les nouveaux endpoints.
- Ne pas supprimer `/predict` legacy tant que des clients l’utilisent.
