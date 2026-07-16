"""Explicabilité SHAP pour les décisions crédit — classe APPROUVER."""
from __future__ import annotations

from typing import Any

import numpy as np
import pandas as pd
from sklearn.pipeline import Pipeline

from model_loader import load_model
from shap_config import (
    EXPLAINED_CLASS_INDEX,
    EXPLAINED_CLASS_LABEL,
    get_explanation_metadata,
)


def _features_to_frame(features: dict[str, Any]) -> pd.DataFrame:
    from config import ALL_INPUT_FEATURES

    return pd.DataFrame([{key: features[key] for key in ALL_INPUT_FEATURES}])


def _transform_features(pipeline: Pipeline, features: dict[str, Any]) -> np.ndarray:
    return pipeline.named_steps["preprocessor"].transform(_features_to_frame(features))


def _map_transformed_name_to_input(name: str) -> str:
    if name.startswith("num__"):
        return name.replace("num__", "", 1)
    if name.startswith("cat__sector_"):
        return "sector"
    return name


def _extract_shap_for_approval_class(shap_values: Any) -> np.ndarray:
    """
    Extrait les SHAP values pour la classe APPROUVER (index 1).

    sklearn classes_: [0=REJETER, 1=APPROUVER]
    LinearExplainer (binaire) renvoie un ndarray (classe positive) ou une liste [classe0, classe1].
    """
    if isinstance(shap_values, list):
        if len(shap_values) > EXPLAINED_CLASS_INDEX:
            return np.asarray(shap_values[EXPLAINED_CLASS_INDEX][0], dtype=float)
        return np.asarray(shap_values[0][0], dtype=float)
    return np.asarray(shap_values[0], dtype=float)


def _impact_from_shap(shap_value: float) -> tuple[str, str]:
    if shap_value > 0:
        return "POSITIVE", "Favorise APPROUVER"
    if shap_value < 0:
        return "NEGATIVE", "Favorise REJETER"
    return "NEUTRAL", "Neutre"


def compute_contribution_percent(shap_value: float, total_abs: float) -> float:
    if total_abs <= 0:
        return 0.0
    return round(abs(shap_value) / total_abs * 100, 2)


def compute_shap_factors(features: dict[str, Any]) -> list[dict[str, Any]]:
    state = load_model()
    pipeline: Pipeline = state["pipeline"]
    explainer = state["explainer"]
    transformed_names: list[str] = state["feature_names"]

    transformed = _transform_features(pipeline, features)
    shap_values = explainer.shap_values(transformed)
    values = _extract_shap_for_approval_class(shap_values)

    if len(values) != len(transformed_names):
        raise ValueError(
            f"Incohérence SHAP: {len(values)} valeurs pour {len(transformed_names)} features transformées."
        )

    raw_factors = []
    for idx, shap_value in enumerate(values):
        transformed_name = transformed_names[idx]
        input_name = _map_transformed_name_to_input(transformed_name)
        raw_value = features["sector"] if input_name == "sector" else features[input_name]
        impact, impact_label = _impact_from_shap(float(shap_value))
        raw_factors.append(
            {
                "name": input_name,
                "transformedName": transformed_name,
                "value": raw_value,
                "shapValue": round(float(shap_value), 6),
                "impact": impact,
                "impactLabel": impact_label,
                "explainedClass": EXPLAINED_CLASS_LABEL,
            }
        )

    merged: dict[str, dict[str, Any]] = {}
    for factor in raw_factors:
        name = factor["name"]
        if name not in merged:
            merged[name] = {
                "name": name,
                "value": factor["value"],
                "shapValue": 0.0,
                "impact": "NEUTRAL",
                "impactLabel": "Neutre",
                "explainedClass": EXPLAINED_CLASS_LABEL,
                "transformedNames": [],
            }
        merged[name]["shapValue"] += factor["shapValue"]
        merged[name]["transformedNames"].append(factor["transformedName"])

    for factor in merged.values():
        impact, impact_label = _impact_from_shap(factor["shapValue"])
        factor["impact"] = impact
        factor["impactLabel"] = impact_label
        factor["shapValue"] = round(float(factor["shapValue"]), 6)

    total_abs = sum(abs(item["shapValue"]) for item in merged.values()) or 1.0
    ranked = sorted(merged.values(), key=lambda item: abs(item["shapValue"]), reverse=True)

    factors = []
    for rank, factor in enumerate(ranked, start=1):
        factors.append(
            {
                **factor,
                "rank": rank,
                "contributionPercent": compute_contribution_percent(
                    factor["shapValue"], total_abs
                ),
            }
        )
    return factors


def get_shap_explanation_bundle(features: dict[str, Any]) -> dict[str, Any]:
    """Facteurs SHAP + métadonnées scientifiques pour la réponse API."""
    state = load_model()
    pipeline: Pipeline = state["pipeline"]
    explainer = state["explainer"]
    transformed_names: list[str] = state["feature_names"]

    transformed = _transform_features(pipeline, features)
    shap_values = _extract_shap_for_approval_class(explainer.shap_values(transformed))
    expected = float(np.asarray(explainer.expected_value).reshape(-1)[0])
    classifier = pipeline.named_steps["classifier"]
    log_odds = float(classifier.decision_function(transformed)[0])

    return {
        "factors": compute_shap_factors(features),
        "metadata": {
            **get_explanation_metadata(transformed_names),
            "expectedValue": round(expected, 6),
            "shapSum": round(float(shap_values.sum()), 6),
            "logOddsApproval": round(log_odds, 6),
            "logOddsCheck": round(expected + float(shap_values.sum()), 6),
        },
    }
