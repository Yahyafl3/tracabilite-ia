# Dataset Card — medical_diabetes_european_dataset

> **Dataset public / clinique de recherche** (structure type UCI/Pima, 768 lignes).
> **Pas un dispositif médical.** Gouvernance : `DEMO_PUBLIC_DATASET`.

- **Nom** : `medical_diabetes_european_dataset`
- **Objectif** : risque de diabète (binaire)
- **Domaine** : MEDICAL
- **Lignes** : 768
- **Cible** : `risque_diabete`
- **Version** : `medical-diabetes-european-public-v2.0.0`

## Colonnes modèle

`age`, `grossesses`, `glycemie_mg_dl`, `pression_arterielle_mmhg`,
`epaisseur_pli_cutane_mm`, `insuline_micro_u_ml`, `imc_kg_m2`

`niveau_risque` du fichier source est **exclu** (fuite / quasi-label).

## Interdits

- Usage clinique sans validation
- Identification de patients réels (IDs synthétiques/publics)
