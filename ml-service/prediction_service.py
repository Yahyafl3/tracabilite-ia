"""Service de prédiction crédit basé sur Scikit-learn."""
from __future__ import annotations

from datetime import datetime, timezone
from typing import Any

import pandas as pd
from sklearn.pipeline import Pipeline

from config import MODEL_TYPE, MODEL_VERSION
from explainability_service import compute_shap_factors, get_shap_explanation_bundle
from model_loader import load_model
from risk_service import compute_risk_level


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _features_to_frame(features: dict[str, Any]) -> pd.DataFrame:
    from config import ALL_INPUT_FEATURES

    return pd.DataFrame([{key: features[key] for key in ALL_INPUT_FEATURES}])


def predict_credit(features: dict[str, Any], include_explanation: bool = True) -> dict[str, Any]:
    state = load_model()
    pipeline: Pipeline = state["pipeline"]
    metadata = state.get("metadata") or {}

    frame = _features_to_frame(features)
    prediction = int(pipeline.predict(frame)[0])
    proba = pipeline.predict_proba(frame)[0]

    decision = "APPROUVER" if prediction == 1 else "REJETER"
    confidence = float(proba[prediction])
    probabilities = {
        "refuser": round(float(proba[0]) * 100, 2),
        "approuver": round(float(proba[1]) * 100, 2),
    }

    response = {
        "decision": decision,
        "domain": "credit",
        "scoreConfiance": round(confidence * 100, 2),
        "riskLevel": compute_risk_level(
            decision,
            confidence * 100,
            features["debtRatio"],
            features["paymentIncidents"],
        ),
        "probabilities": probabilities,
        "features": features,
        "model": {
            "name": metadata.get("modelType", MODEL_TYPE),
            "version": state.get("version", MODEL_VERSION),
            "source": "scikit-learn",
        },
        "explanationSource": "SHAP",
        "timestamp": _utc_now(),
    }

    if include_explanation:
        response["factors"] = compute_shap_factors(features)

    return response


def explain_credit(features: dict[str, Any]) -> dict[str, Any]:
    prediction = predict_credit(features, include_explanation=False)
    explanation = get_shap_explanation_bundle(features)
    return {
        "decision": prediction["decision"],
        "scoreConfiance": prediction["scoreConfiance"],
        "riskLevel": prediction["riskLevel"],
        "probabilities": prediction["probabilities"],
        "factors": explanation["factors"],
        "explanation": explanation["metadata"],
        "explanationSource": "SHAP",
        "timestamp": _utc_now(),
    }
