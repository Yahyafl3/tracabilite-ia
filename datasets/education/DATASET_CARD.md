# Dataset Card — students_maroc_dropout_synthetic

> **Données synthétiques.** Ce fichier ne contient aucune donnée réelle
> de banques, d'hôpitaux ou d'universités marocaines.

- **Nom** : `students_maroc_dropout_synthetic`
- **Objectif** : Évaluer le risque de décrochage universitaire et proposer un accompagnement.
- **Domaine** : EDUCATION
- **Nombre de lignes** : 10000
- **Colonne cible** : `decrochage` (binaire 0/1)
- **Méthode de génération** : Échantillonnage probabiliste multi-facteurs (moyennes, absences, situation socio-académique).
- **Date de génération** : 2026-07-30
- **Seed** : `42`
- **Version** : `students-maroc-dropout-synthetic-v1.0.0`

## Colonnes

| Colonne | Type |
|---------|------|
| `region` | str |
| `type_etablissement` | str |
| `filiere` | str |
| `niveau_etude` | str |
| `moyenne_semestre_1` | float64 |
| `moyenne_semestre_2` | float64 |
| `taux_absence` | float64 |
| `modules_non_valides` | int64 |
| `participation` | str |
| `bourse` | str |
| `distance_logement_km` | float64 |
| `acces_internet` | str |
| `activite_professionnelle` | str |
| `historique_redoublement` | str |
| `situation_academique` | str |
| `decrochage` | int64 |

## Règles probabilistes

- Probabilité basée sur absences, modules non validés, moyennes, participation, internet, distance.
- Bruit aléatoire contrôlé ; pas de sanction automatique simulée.

## Sources statistiques générales (contexte)

- Indicateurs pédagogiques génériques (absences, moyennes /20).
- Aucune donnée réelle d'université marocaine.

## Limites

- Ne constitue pas une sanction contre l'étudiant.
- Contexte synthétique simplifié.

## Biais possibles

- Sur-pondération possible de l'absence et de la distance.
- Filières échantillonnées uniformément (hors pondération réelle).

## Usages autorisés

- Développement et tests de la plateforme Traçabilité IA
- Démonstration pédagogique d'explicabilité et de validation humaine
- Benchmarks internes de pipelines ML

## Usages interdits

- Présenter ces données comme réelles
- Décisions opérationnelles bancaires, médicales ou pédagogiques en production
- Entraînement de modèles déployés sans réentraînement sur données réelles anonymisées
- Identification de personnes (aucune PII réelle n'est présente)
