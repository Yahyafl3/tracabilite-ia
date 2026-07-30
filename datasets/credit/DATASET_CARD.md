# Dataset Card — credit_maroc_synthetic

> **Données synthétiques.** Ce fichier ne contient aucune donnée réelle
> de banques, d'hôpitaux ou d'universités marocaines.

- **Nom** : `credit_maroc_synthetic`
- **Objectif** : Évaluer le risque de défaut de paiement (contexte marocain synthétique).
- **Domaine** : CREDIT
- **Nombre de lignes** : 10000
- **Colonne cible** : `defaut_paiement` (binaire 0/1)
- **Méthode de génération** : Échantillonnage probabiliste multi-facteurs (logit + bruit gaussien).
- **Date de génération** : 2026-07-30
- **Seed** : `42`
- **Version** : `credit-maroc-synthetic-v1.0.0`

## Colonnes

| Colonne | Type |
|---------|------|
| `secteur_activite` | str |
| `region` | str |
| `age_demandeur` | int64 |
| `statut_professionnel` | str |
| `revenu_mensuel_mad` | int64 |
| `charges_mensuelles_mad` | int64 |
| `montant_demande_mad` | int64 |
| `duree_credit_mois` | int64 |
| `anciennete_professionnelle_annees` | int64 |
| `credits_existants` | int64 |
| `incidents_paiement_24_mois` | int64 |
| `ratio_endettement` | float64 |
| `type_garantie` | str |
| `type_credit` | str |
| `defaut_paiement` | int64 |

## Règles probabilistes

- Probabilité de défaut basée sur ratio d'endettement, incidents, revenus, garanties, âge, statut.
- Bruit aléatoire contrôlé (σ≈0.55) pour éviter une règle unique déterministe.
- Montants en MAD ; régions administratives marocaines.

## Sources statistiques générales (contexte)

- Distributions inspirées de statistiques publiques générales (emploi, revenus, régions).
- Aucune source de données bancaires réelles n'a été utilisée.

## Limites

- Synthétique — ne reflète pas un portefeuille bancaire réel.
- Ne doit pas être présenté comme un modèle bancaire officiel marocain.

## Biais possibles

- Corrélations artificielles entre secteurs et risque.
- Sous-représentation possible de certaines régions.

## Usages autorisés

- Développement et tests de la plateforme Traçabilité IA
- Démonstration pédagogique d'explicabilité et de validation humaine
- Benchmarks internes de pipelines ML

## Usages interdits

- Présenter ces données comme réelles
- Décisions opérationnelles bancaires, médicales ou pédagogiques en production
- Entraînement de modèles déployés sans réentraînement sur données réelles anonymisées
- Identification de personnes (aucune PII réelle n'est présente)
