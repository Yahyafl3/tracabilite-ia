"""Entraînement MEDICAL — dataset diabète (public / style clinique)."""
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
        return Path(env) / "medical" / "medical_diabetes_european_dataset.csv"
    candidates = [
        Path(__file__).resolve().parents[2]
        / "datasets"
        / "medical"
        / "medical_diabetes_european_dataset.csv",
        Path("/datasets/medical/medical_diabetes_european_dataset.csv"),
    ]
    for c in candidates:
        if c.exists():
            return c
    return candidates[0]


DATASET = _dataset_path()
REPORT = ROOT / "reports" / "medical_evaluation.json"
RANDOM_STATE = 42
MODEL_VERSION = "medical-model-v2.0.0-public"
DATASET_VERSION = "medical-diabetes-european-public-v2.0.0"

NUMERIC = [
    "age",
    "grossesses",
    "glycemie_mg_dl",
    "pression_arterielle_mmhg",
    "epaisseur_pli_cutane_mm",
    "insuline_micro_u_ml",
    "imc_kg_m2",
]
CATEGORICAL: list[str] = []
TARGET = "risque_diabete"
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
        print(f"[medical] {name}: recall={metrics['recall']} f1={metrics['f1']} score={score:.4f}")
        if score > best_score:
            best_name, best_pipe, best_metrics, best_score = name, pipe, metrics, score

    assert best_pipe is not None and best_metrics is not None
    meta = {
        "domain": "MEDICAL",
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
        "outputClasses": ["FAIBLE", "MODERE", "ELEVE"],
        "binaryTargetMeaning": "1 = risque diabète",
        "metrics": best_metrics,
        "selectionScore": round(best_score, 4),
        "disclaimer": "Dataset public/recherche — pas un dispositif médical. DEMO_PUBLIC_DATASET.",
    }
    save_model("medical", best_pipe, meta)
    write_report(
        REPORT,
        {
            "domain": "MEDICAL",
            "selectedModel": best_name,
            "modelVersion": MODEL_VERSION,
            "datasetVersion": DATASET_VERSION,
            "features": FEATURES,
            "comparisons": comparisons,
            "selectedMetrics": best_metrics,
        },
    )
    print(f"[medical] selected={best_name} → models/medical/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
