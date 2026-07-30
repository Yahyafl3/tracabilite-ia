"""Évaluation et rapports JSON pour les modèles multidomain."""
from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import numpy as np
import pandas as pd
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
)


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def evaluate_binary_classifier(
    pipeline,
    x_test: pd.DataFrame,
    y_test: pd.Series,
    *,
    positive_label: int = 1,
) -> dict[str, Any]:
    y_pred = pipeline.predict(x_test)
    y_proba = pipeline.predict_proba(x_test)[:, 1]
    cm = confusion_matrix(y_test, y_pred).tolist()
    class_dist = {
        str(k): int(v) for k, v in y_test.value_counts().sort_index().items()
    }

    # Erreurs : faux négatifs prioritaires pour crédit/médical
    fn_mask = (y_test == positive_label) & (y_pred != positive_label)
    fp_mask = (y_test != positive_label) & (y_pred == positive_label)

    return {
        "accuracy": round(float(accuracy_score(y_test, y_pred)), 4),
        "precision": round(
            float(precision_score(y_test, y_pred, zero_division=0)), 4
        ),
        "recall": round(float(recall_score(y_test, y_pred, zero_division=0)), 4),
        "f1": round(float(f1_score(y_test, y_pred, zero_division=0)), 4),
        "roc_auc": round(float(roc_auc_score(y_test, y_proba)), 4),
        "confusion_matrix": cm,
        "class_distribution_test": class_dist,
        "false_negatives": int(fn_mask.sum()),
        "false_positives": int(fp_mask.sum()),
        "classification_report": classification_report(
            y_test, y_pred, output_dict=True, zero_division=0
        ),
        "error_analysis": {
            "fn_rate": round(float(fn_mask.mean()), 4),
            "fp_rate": round(float(fp_mask.mean()), 4),
            "note": (
                "Pour CREDIT et MEDICAL, le recall de la classe à risque "
                "est prioritaire sur l'accuracy seule."
            ),
        },
    }


def selection_score(metrics: dict[str, Any], *, recall_weight: float = 0.45) -> float:
    """Score composite : recall prioritaire, puis F1 et ROC-AUC."""
    return (
        recall_weight * metrics["recall"]
        + 0.30 * metrics["f1"]
        + 0.25 * metrics["roc_auc"]
    )


def write_report(path: Path, report: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = {"generatedAt": _utc_now(), **report}
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False), encoding="utf-8")


def extract_feature_names(pipeline) -> list[str]:
    preprocessor = pipeline.named_steps["preprocessor"]
    return list(preprocessor.get_feature_names_out())
