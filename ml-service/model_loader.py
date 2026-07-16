"""Chargement du modèle entraîné et initialisation SHAP."""
from __future__ import annotations

import json
from typing import Any

import joblib
import shap
from sklearn.pipeline import Pipeline

from config import METADATA_PATH, MODEL_PATH, MODEL_VERSION
from sector_schema import get_schema_payload
from shap_config import get_explanation_metadata


class ModelNotReadyError(RuntimeError):
    pass


_state: dict[str, Any] = {
    "ready": False,
    "pipeline": None,
    "explainer": None,
    "feature_names": [],
    "background": None,
    "metadata": None,
}


def is_ready() -> bool:
    return bool(_state.get("ready"))


def _load_metadata() -> dict:
    if METADATA_PATH.exists():
        return json.loads(METADATA_PATH.read_text(encoding="utf-8"))
    artifact = joblib.load(MODEL_PATH)
    return artifact.get("metadata", {})


def load_model(force_reload: bool = False) -> dict[str, Any]:
    if is_ready() and not force_reload:
        return _state

    if not MODEL_PATH.exists():
        raise ModelNotReadyError(
            "Aucun modèle entraîné trouvé. Exécutez: python train_model.py"
        )

    artifact = joblib.load(MODEL_PATH)
    pipeline: Pipeline = artifact["pipeline"]
    feature_names = artifact.get("feature_names") or list(
        pipeline.named_steps["preprocessor"].get_feature_names_out()
    )
    background = artifact.get("background")
    metadata = artifact.get("metadata") or _load_metadata()

    if background is None:
        raise ModelNotReadyError(
            "Artefact incomplet: échantillon background manquant pour SHAP."
        )

    classifier = pipeline.named_steps["classifier"]
    explainer = shap.LinearExplainer(
        classifier,
        background,
        feature_names=feature_names,
    )

    _state.update(
        {
            "ready": True,
            "pipeline": pipeline,
            "explainer": explainer,
            "feature_names": feature_names,
            "background": background,
            "metadata": metadata,
            "version": artifact.get("version", MODEL_VERSION),
        }
    )
    return _state


def get_model_info() -> dict[str, Any]:
    state = load_model()
    metadata = state.get("metadata") or {}
    pipeline: Pipeline = state["pipeline"]
    classifier = pipeline.named_steps["classifier"]

    return {
        "domain": "credit",
        "version": state.get("version", MODEL_VERSION),
        "features": metadata.get(
            "features",
            {
                "numeric": [],
                "categorical": [],
            },
        ),
        "outputClasses": ["REJETER", "APPROUVER"],
        "ready": True,
        "engine": "SKLEARN_SHAP",
        "modelType": metadata.get("modelType", "LogisticRegression"),
        "explainability": metadata.get("explainability", "SHAP LinearExplainer"),
        "explanationSource": "SHAP",
        "modelPath": str(MODEL_PATH),
        "metadataPath": str(METADATA_PATH),
        "metrics": metadata.get("metrics"),
        "dataset": metadata.get("dataset"),
        "trainedAt": metadata.get("trainedAt"),
        "classifier": type(classifier).__name__,
        "note": metadata.get("note"),
        "schema": get_schema_payload(),
        "shapExplanation": get_explanation_metadata(state["feature_names"]),
    }
