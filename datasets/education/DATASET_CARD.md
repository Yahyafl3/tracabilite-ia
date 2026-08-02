# Dataset Card — education_portugal_dropout

> **Dataset public UCI Portugal** (« Predict students' dropout and academic success »).
> Gouvernance : `DEMO_PUBLIC_DATASET` — non validé pour décisions pédagogiques réelles.

- **Nom** : `education_portugal_dropout`
- **Source brute** : `education_portugal_raw.csv` (`;`)
- **Fichier normalisé** : `education_portugal_dropout.csv`
- **Objectif** : prédire le décrochage (`Dropout`)
- **Domaine** : EDUCATION
- **Lignes** : 4424
- **Cible** : `decrochage` (1 = Dropout ; Graduate/Enrolled = 0)
- **Version** : `education-portugal-dropout-public-v2.0.0`

## Colonnes modèle

Numériques : `age_inscription`, `note_admission`, `note_qualification_precedente`,
`unites_validees_s1`, `moyenne_s1`, `unites_validees_s2`, `moyenne_s2`,
`taux_chomage`, `taux_inflation`, `pib`

Catégorielles : `sexe`, `boursier`, `frais_a_jour`, `debiteur`, `deplace`, `international`

## Interdits

- Présenter comme données d’une université marocaine
- Décisions d’orientation / exclusion sans validation institutionnelle
