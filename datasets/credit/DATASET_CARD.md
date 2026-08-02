# Dataset Card — credit_analysis_dataset

> **Dataset public / recherche.** Ce n’est **pas** un extrait bancaire confidentiel.
> Les modèles associés restent `DEMO_PUBLIC_DATASET` (non `VALIDATED_PRODUCTION`).

- **Nom** : `credit_analysis_dataset`
- **Fichier** : `credit_analysis_dataset.csv`
- **Objectif** : prédire le risque de non-approbation de crédit
- **Domaine** : CREDIT
- **Lignes** : 1000
- **Cible** : `risque_non_approbation` (1 = non approuvé)
- **Version** : `credit-analysis-public-v2.0.0`

## Colonnes modèle

`age`, `duree_mois`, `type_contrat`, `statut_logement`, `incident_paiement_bam`,
`montant_demande_mad`, `nouvelle_echeance_mad`, `revenu_mensuel_mad`, `taux_endettement`

## Interdits

- Présenter ce jeu comme portefeuille bancaire réel confidentiel
- Décisions métier production sans validation réglementaire
