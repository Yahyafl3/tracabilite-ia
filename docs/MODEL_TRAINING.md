# Entraînement des modèles multidomain

## Scripts

```bash
cd ml-service
python training/train_credit_model.py
python training/train_medical_model.py
python training/train_education_model.py
```

## Comparaison

Pour chaque domaine : LogisticRegression, RandomForestClassifier, HistGradientBoostingClassifier.

Sélection composite (pas accuracy seule) :

- Crédit / Médical : `0.50*recall + 0.30*f1 + 0.25*roc_auc`
- Éducation : `0.40*recall + 0.30*f1 + 0.25*roc_auc`

## Artefacts

```
ml-service/models/{credit,medical,education}/
  *_pipeline.joblib
  metadata.json
ml-service/reports/
  *_evaluation.json
```

## Explications

Les facteurs techniques viennent des coefficients / importances du modèle.
Les agents LLM restent **informatifs** et ne modifient jamais la prédiction ML.
