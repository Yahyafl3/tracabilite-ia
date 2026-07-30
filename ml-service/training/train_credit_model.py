"""Entraînement du modèle CREDIT (risque de défaut — données synthétiques Maroc)."""
from __future__ import annotations

import os
import sys
from pathlib import Path

import pandas as pd
from sklearn.ensemble import HistGradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
sys.path.insert(0, str(Path(__file__).resolve().parent))

from common.evaluation import (  # noqa: E402
    evaluate_binary_classifier,
    extract_feature_names,
    selection_score,
    write_report,
)
from common.model_registry import save_model  # noqa: E402
from common.preprocessing import build_model_pipeline  # noqa: E402

def _dataset_path() -> Path:
    env = os.environ.get("DATASETS_DIR")
    if env:
        return Path(env) / "credit" / "credit_maroc_synthetic.csv"
    candidates = [
        Path(__file__).resolve().parents[2] / "datasets" / "credit" / "credit_maroc_synthetic.csv",
        Path("/datasets/credit/credit_maroc_synthetic.csv"),
    ]
    for c in candidates:
        if c.exists():
            return c
    return candidates[0]


DATASET = _dataset_path()
REPORT = ROOT / "reports" / "credit_evaluation.json"
RANDOM_STATE = 42
MODEL_VERSION = "credit-model-v1.0.0"
DATASET_VERSION = "credit-maroc-synthetic-v1.0.0"

NUMERIC = [
    "age_demandeur",
    "revenu_mensuel_mad",
    "charges_mensuelles_mad",
    "montant_demande_mad",
    "duree_credit_mois",
    "anciennete_professionnelle_annees",
    "credits_existants",
    "incidents_paiement_24_mois",
    "ratio_endettement",
]
CATEGORICAL = [
    "secteur_activite",
    "region",
    "statut_professionnel",
    "type_garantie",
    "type_credit",
]
TARGET = "defaut_paiement"
FEATURES = NUMERIC + CATEGORICAL


def candidate_classifiers() -> dict:
    return {
        "LogisticRegression": LogisticRegression(
            max_iter=2000, class_weight="balanced", random_state=RANDOM_STATE
        ),
        "RandomForestClassifier": RandomForestClassifier(
            n_estimators=200,
            max_depth=12,
            class_weight="balanced_subsample",
            random_state=RANDOM_STATE,
            n_jobs=-1,
        ),
        "HistGradientBoostingClassifier": HistGradientBoostingClassifier(
            max_depth=8,
            learning_rate=0.08,
            max_iter=200,
            random_state=RANDOM_STATE,
        ),
    }


def main() -> int:
    if not DATASET.exists():
        raise FileNotFoundError(
            f"Dataset manquant: {DATASET}. Exécutez generate_moroccan_datasets.py"
        )
    df = pd.read_csv(DATASET)
    x = df[FEATURES]
    y = df[TARGET]
    x_train, x_test, y_train, y_test = train_test_split(
        x, y, test_size=0.2, random_state=RANDOM_STATE, stratify=y
    )

    comparisons = []
    best_name, best_pipe, best_metrics, best_score = None, None, None, -1.0

    for name, clf in candidate_classifiers().items():
        pipe = build_model_pipeline(clf, NUMERIC, CATEGORICAL)
        pipe.fit(x_train, y_train)
        metrics = evaluate_binary_classifier(pipe, x_test, y_test)
        score = selection_score(metrics, recall_weight=0.50)
        comparisons.append({"model": name, "metrics": metrics, "selection_score": round(score, 4)})
        print(f"[credit] {name}: recall={metrics['recall']} f1={metrics['f1']} score={score:.4f}")
        if score > best_score:
            best_name, best_pipe, best_metrics, best_score = name, pipe, metrics, score

    assert best_pipe is not None and best_metrics is not None
    feature_names = extract_feature_names(best_pipe)
    meta = {
        "domain": "CREDIT",
        "modelVersion": MODEL_VERSION,
        "datasetVersion": DATASET_VERSION,
        "modelType": best_name,
        "target": TARGET,
        "features": FEATURES,
        "numericFeatures": NUMERIC,
        "categoricalFeatures": CATEGORICAL,
        "transformedFeatureNames": feature_names,
        "outputClasses": ["FAIBLE", "MOYEN", "ELEVE"],
        "binaryTargetMeaning": "1 = défaut de paiement (risque)",
        "riskMapping": "probabilité → FAIBLE (<0.33) / MOYEN / ELEVE (>=0.66)",
        "metrics": best_metrics,
        "selectionScore": round(best_score, 4),
        "disclaimer": "Dataset synthétique — pas un modèle bancaire officiel.",
    }
    save_model("credit", best_pipe, meta)
    write_report(
        REPORT,
        {
            "domain": "CREDIT",
            "selectedModel": best_name,
            "modelVersion": MODEL_VERSION,
            "datasetVersion": DATASET_VERSION,
            "features": FEATURES,
            "comparisons": comparisons,
            "selectedMetrics": best_metrics,
            "selectionCriterion": "0.50*recall + 0.30*f1 + 0.25*roc_auc",
        },
    )
    print(f"[credit] selected={best_name} → models/credit/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
