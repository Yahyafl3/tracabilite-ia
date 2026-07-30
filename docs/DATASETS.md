# Datasets synthétiques marocains

> **Les datasets fournis sont synthétiques et contextualisés au Maroc.**
> Ils servent au développement, aux tests et à la démonstration.
> Ils ne constituent **pas** des données réelles de banques, d’établissements de santé ou d’universités marocaines.

## Emplacement

```
datasets/
├── credit/credit_maroc_synthetic.csv (+ DATASET_CARD.md)
├── medical/medical_diabetes_maroc_synthetic.csv (+ DATASET_CARD.md)
└── education/students_maroc_dropout_synthetic.csv (+ DATASET_CARD.md)
```

## Génération

```bash
cd ml-service
python scripts/generate_moroccan_datasets.py --seed 42
# options: --credit-rows 10000 --medical-rows 8000 --education-rows 10000
```

## Cibles

| Dataset | Cible binaire | Niveaux dérivés |
|---------|---------------|-----------------|
| Crédit | `defaut_paiement` | FAIBLE / MOYEN / ELEVE |
| Médical | `risque_diabete` | FAIBLE / MODERE / ELEVE |
| Éducation | `decrochage` | FAIBLE / MOYEN / ELEVE |

Les cibles sont générées via un **logit multi-facteurs + bruit**, jamais une seule règle fixe.

Voir chaque `DATASET_CARD.md` pour colonnes, biais, limites et usages interdits.
