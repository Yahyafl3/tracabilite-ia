"""Configuration et documentation SHAP — cohérence scientifique."""
from __future__ import annotations

from config import ALL_INPUT_FEATURES, NUMERIC_FEATURES, OUTPUT_CLASSES

# sklearn classes_: [0=REJETER, 1=APPROUVER]
EXPLAINED_CLASS_INDEX = 1
EXPLAINED_CLASS_LABEL = "APPROUVER"
REJECTED_CLASS_LABEL = "REJETER"

CONTRIBUTION_PERCENT_FORMULA = (
    "contributionPercent_i = |shapValue_i| / sum_j(|shapValue_j|) × 100"
)

SHAP_VALUE_FORMULA = (
    "Pour LogisticRegression + LinearExplainer : "
    "shapValue_j ≈ coef_j × (x_j_transformed − E[x_j_transformed]) "
    "où x_j_transformed provient du pipeline (imputation, StandardScaler, OneHotEncoder)."
)

LOG_ODDS_FORMULA = (
    "sum(shapValue_j) + expected_value ≈ log-odds(APPROUVER) = decision_function(x)"
)

IMPACT_SEMANTICS = {
    "POSITIVE": (
        "Contribution SHAP > 0 vers la classe expliquée APPROUVER "
        "(augmente les log-odds d'approbation par rapport au profil moyen d'entraînement)."
    ),
    "NEGATIVE": (
        "Contribution SHAP < 0 vers la classe expliquée APPROUVER "
        "(diminue les log-odds d'approbation par rapport au profil moyen d'entraînement)."
    ),
    "NEUTRAL": "Contribution SHAP nulle.",
}

# Sens métier attendu des coefficients sur features transformées (classe APPROUVER)
EXPECTED_COEF_SIGN_APPROVE: dict[str, str] = {
    "num__amount": "negative",
    "num__monthlyIncome": "positive",
    "num__companyAgeYears": "positive",
    "num__paymentIncidents": "negative",
    "num__debtRatio": "negative",
}

DATASET_LIMITATIONS = [
    "Dataset 100 % synthétique — non représentatif d'un portefeuille réel.",
    "Le label approved est généré via un score utilisant le ratio revenu/montant ; "
    "monthlyIncome seul a une corrélation quasi nulle avec approved (~0.004).",
    "Les valeurs SHAP sont relatives à la distribution d'entraînement : "
    "une valeur 'faible' en absolu peut avoir un SHAP positif si elle est "
    "meilleure que la moyenne du dataset.",
    "Les features sont transformées (StandardScaler) avant SHAP : "
    "l'interprétation métier directe sur les valeurs brutes est limitée.",
]


def get_explanation_metadata(feature_names: list[str]) -> dict:
    return {
        "explainedClass": EXPLAINED_CLASS_LABEL,
        "explainedClassIndex": EXPLAINED_CLASS_INDEX,
        "outputClasses": {
            "0": REJECTED_CLASS_LABEL,
            "1": EXPLAINED_CLASS_LABEL,
        },
        "inputFeatureOrder": list(ALL_INPUT_FEATURES),
        "numericFeatures": list(NUMERIC_FEATURES),
        "transformedFeatureOrder": list(feature_names),
        "contributionPercentFormula": CONTRIBUTION_PERCENT_FORMULA,
        "shapValueFormula": SHAP_VALUE_FORMULA,
        "logOddsFormula": LOG_ODDS_FORMULA,
        "impactSemantics": IMPACT_SEMANTICS,
        "expectedCoefficientSignForApproval": EXPECTED_COEF_SIGN_APPROVE,
        "datasetLimitations": DATASET_LIMITATIONS,
    }
