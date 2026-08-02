"""Entraînement EDUCATION — dataset Portugal (dropout académique)."""
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
        return Path(env) / "education" / "education_portugal_dropout.csv"
    candidates = [
        Path(__file__).resolve().parents[2]
        / "datasets"
        / "education"
        / "education_portugal_dropout.csv",
        Path("/datasets/education/education_portugal_dropout.csv"),
    ]
    for c in candidates:
        if c.exists():
            return c
    return candidates[0]


DATASET = _dataset_path()
REPORT = ROOT / "reports" / "education_evaluation.json"
RANDOM_STATE = 42
MODEL_VERSION = "education-model-v2.0.0-public"
DATASET_VERSION = "education-portugal-dropout-public-v2.0.0"

NUMERIC = [
    "age_inscription",
    "note_admission",
    "note_qualification_precedente",
    "unites_validees_s1",
    "moyenne_s1",
    "unites_validees_s2",
    "moyenne_s2",
    "taux_chomage",
    "taux_inflation",
    "pib",
]
CATEGORICAL = [
    "sexe",
    "boursier",
    "frais_a_jour",
    "debiteur",
    "deplace",
    "international",
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
        raise FileNotFoundError(f"Dataset manquant: {DATASET}")
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
        print(f"[education] {name}: recall={metrics['recall']} f1={metrics['f1']} score={score:.4f}")
        if score > best_score:
            best_name, best_pipe, best_metrics, best_score = name, pipe, metrics, score

    assert best_pipe is not None and best_metrics is not None
    meta = {
        "domain": "EDUCATION",
        "modelVersion": MODEL_VERSION,
        "datasetVersion": DATASET_VERSION,
        "governanceStatus": "DEMO_PUBLIC_DATASET",
        "approvedForRealDecisions": False,
        "modelType": best_name,
        "target": TARGET,
        "features": FEATURES,
        "numericFeatures": NUMERIC,
        "categoricalFeatures": CATEGORICAL,
        "transformedFeatureNames": extract_feature_names(best_pipe),
        "outputClasses": ["FAIBLE", "MOYEN", "ELEVE"],
        "binaryTargetMeaning": "1 = décrochage (Dropout)",
        "metrics": best_metrics,
        "selectionScore": round(best_score, 4),
        "disclaimer": "Dataset public Portugal (UCI) — DEMO_PUBLIC_DATASET, pas VALIDATED_PRODUCTION.",
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
        },
    )
    print(f"[education] selected={best_name} → models/education/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
