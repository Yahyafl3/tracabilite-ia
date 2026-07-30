"""Entraînement du modèle EDUCATION (risque de décrochage — accompagnement pédagogique)."""
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
        return Path(env) / "education" / "students_maroc_dropout_synthetic.csv"
    candidates = [
        Path(__file__).resolve().parents[2] / "datasets" / "education" / "students_maroc_dropout_synthetic.csv",
        Path("/datasets/education/students_maroc_dropout_synthetic.csv"),
    ]
    for c in candidates:
        if c.exists():
            return c
    return candidates[0]


DATASET = _dataset_path()
REPORT = ROOT / "reports" / "education_evaluation.json"
RANDOM_STATE = 42
MODEL_VERSION = "education-model-v1.0.0"
DATASET_VERSION = "students-maroc-dropout-synthetic-v1.0.0"

NUMERIC = [
    "moyenne_semestre_1",
    "moyenne_semestre_2",
    "taux_absence",
    "modules_non_valides",
    "distance_logement_km",
]
CATEGORICAL = [
    "region",
    "type_etablissement",
    "filiere",
    "niveau_etude",
    "participation",
    "bourse",
    "acces_internet",
    "activite_professionnelle",
    "historique_redoublement",
    "situation_academique",
]
TARGET = "decrochage"
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
        score = selection_score(metrics, recall_weight=0.40)
        comparisons.append({"model": name, "metrics": metrics, "selection_score": round(score, 4)})
        print(f"[education] {name}: recall={metrics['recall']} f1={metrics['f1']} score={score:.4f}")
        if score > best_score:
            best_name, best_pipe, best_metrics, best_score = name, pipe, metrics, score

    assert best_pipe is not None and best_metrics is not None
    meta = {
        "domain": "EDUCATION",
        "modelVersion": MODEL_VERSION,
        "datasetVersion": DATASET_VERSION,
        "modelType": best_name,
        "target": TARGET,
        "features": FEATURES,
        "numericFeatures": NUMERIC,
        "categoricalFeatures": CATEGORICAL,
        "transformedFeatureNames": extract_feature_names(best_pipe),
        "outputClasses": ["FAIBLE", "MOYEN", "ELEVE"],
        "binaryTargetMeaning": "1 = risque de décrochage",
        "disclaimer": (
            "Aide à l'accompagnement pédagogique. "
            "Ne constitue pas une sanction automatique contre l'étudiant. Dataset synthétique."
        ),
    }
    save_model("education", best_pipe, meta)
    write_report(
        REPORT,
        {
            "domain": "EDUCATION",
            "selectedModel": best_name,
            "modelVersion": MODEL_VERSION,
            "datasetVersion": DATASET_VERSION,
            "features": FEATURES,
            "comparisons": comparisons,
            "selectedMetrics": best_metrics,
            "selectionCriterion": "0.40*recall + 0.30*f1 + 0.25*roc_auc",
        },
    )
    print(f"[education] selected={best_name} → models/education/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
