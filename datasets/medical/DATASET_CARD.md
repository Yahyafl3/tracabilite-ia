# Dataset Card — medical_diabetes_maroc_synthetic

> **Données synthétiques.** Ce fichier ne contient aucune donnée réelle
> de banques, d'hôpitaux ou d'universités marocaines.

- **Nom** : `medical_diabetes_maroc_synthetic`
- **Objectif** : Estimation indicative du risque de diabète (aide à la décision, non diagnostique).
- **Domaine** : MEDICAL
- **Nombre de lignes** : 7997
- **Colonne cible** : `risque_diabete` (binaire 0/1)
- **Méthode de génération** : Échantillonnage probabiliste multi-facteurs (IMC, glycémie, symptômes, antécédents).
- **Date de génération** : 2026-07-30
- **Seed** : `42`
- **Version** : `medical-diabetes-maroc-synthetic-v1.0.0`

## Colonnes

| Colonne | Type |
|---------|------|
| `region` | str |
| `age` | int64 |
| `sexe` | str |
| `imc` | float64 |
| `niveau_activite_physique` | str |
| `antecedents_familiaux_diabete` | str |
| `hypertension` | str |
| `glycemie` | float64 |
| `polyurie` | str |
| `polydipsie` | str |
| `perte_poids_soudaine` | str |
| `faiblesse` | str |
| `obesite` | str |
| `suivi_medical` | str |
| `risque_diabete` | int64 |

## Règles probabilistes

- Probabilité basée sur âge, IMC, glycémie, symptômes classiques et antécédents.
- Bruit aléatoire pour éviter une cible déterminée par une seule règle.
- Aucun identifiant personnel ; données fictives.

## Sources statistiques générales (contexte)

- Facteurs de risque diabète documentés dans la littérature générale.
- Aucune donnée hospitalière réelle marocaine.

## Limites

- Ne remplace pas un diagnostic médical.
- Pas de biomarqueurs de laboratoire réels.

## Biais possibles

- Symptômes auto-déclarés simulés.
- Corrélation IMC/obésité structurelle.

## Usages autorisés

- Développement et tests de la plateforme Traçabilité IA
- Démonstration pédagogique d'explicabilité et de validation humaine
- Benchmarks internes de pipelines ML

## Usages interdits

- Présenter ces données comme réelles
- Décisions opérationnelles bancaires, médicales ou pédagogiques en production
- Entraînement de modèles déployés sans réentraînement sur données réelles anonymisées
- Identification de personnes (aucune PII réelle n'est présente)
