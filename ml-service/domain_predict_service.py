"""Prédiction et explication pour les modèles CREDIT / MEDICAL / EDUCATION."""
from __future__ import annotations

import uuid
from datetime import datetime, timezone
from typing import Any

import numpy as np
import pandas as pd

from domain_schemas import DOMAIN_SCHEMAS, normalize_domain_features, probability_to_risk
from training.common.model_registry import list_registered_models, load_metadata, load_model

_DOMAIN_CACHE: dict[str, Any] = {}


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def get_domain_pipeline(domain: str):
    key = domain.upper()
    if key not in _DOMAIN_CACHE:
        _DOMAIN_CACHE[key] = {
            "pipeline": load_model(key.lower()),
            "metadata": load_metadata(key.lower()),
        }
    return _DOMAIN_CACHE[key]["pipeline"], _DOMAIN_CACHE[key]["metadata"]


def clear_domain_cache() -> None:
    _DOMAIN_CACHE.clear()


def _recommendation(domain: str, risk: str) -> str:
    domain = domain.upper()
    if domain == "CREDIT":
        mapping = {
            "FAIBLE": "Dossier favorable — validation humaine recommandée avant accord.",
            "MOYEN": "Risque intermédiaire — analyse approfondie et garanties à examiner.",
            "ELEVE": "Risque élevé de défaut — refus ou garanties renforcées à étudier.",
        }
        return mapping.get(risk, "Analyse crédit disponible pour validation humaine.")
    if domain == "MEDICAL":
        mapping = {
            "FAIBLE": "Risque faible — suivi standard possible (indicatif, non diagnostique).",
            "MODERE": "Risque modéré — examens complémentaires à discuter avec un professionnel.",
            "ELEVE": "Risque élevé — orientation vers un professionnel de santé recommandée.",
        }
        return mapping.get(risk, "Estimation indicative — avis médical requis.")
    mapping = {
        "FAIBLE": "Risque faible — aucune intervention urgente ; suivi pédagogique habituel.",
        "MOYEN": "Risque moyen — accompagnement pédagogique recommandé.",
        "ELEVE": "Risque élevé de décrochage — entretien pédagogique / tutorat à envisager.",
    }
    return mapping.get(risk, "Évaluation pédagogique indicative.")


def _feature_importance_factors(
    pipeline,
    features: dict[str, Any],
    metadata: dict[str, Any],
    top_n: int = 5,
) -> list[dict[str, Any]]:
    """Importance via coefficients (LR) ou feature_importances_ — pas d'invention."""
    clf = pipeline.named_steps["classifier"]
    feature_names = metadata.get("transformedFeatureNames") or list(
        pipeline.named_steps["preprocessor"].get_feature_names_out()
    )
    importances: np.ndarray | None = None

    if hasattr(clf, "coef_"):
        importances = np.abs(clf.coef_[0])
    elif hasattr(clf, "feature_importances_"):
        importances = np.asarray(clf.feature_importances_)

    if importances is None or len(importances) != len(feature_names):
        return []

    # Mapper vers features brutes (num__x / cat__y_val)
    raw_scores: dict[str, float] = {}
    for name, imp in zip(feature_names, importances):
        if name.startswith("num__"):
            raw = name[5:]
        elif name.startswith("cat__"):
            raw = name[5:].split("_")[0] if "__" not in name[5:] else name.split("__", 1)[1].rsplit("_", 1)[0]
            # cat__secteur_activite_SERVICES → secteur_activite
            parts = name.split("__", 1)[1]
            for feat in metadata.get("categoricalFeatures", []):
                if parts.startswith(feat):
                    raw = feat
                    break
            else:
                raw = parts
        else:
            raw = name
        raw_scores[raw] = raw_scores.get(raw, 0.0) + float(imp)

    total = sum(raw_scores.values()) or 1.0
    ranked = sorted(raw_scores.items(), key=lambda x: x[1], reverse=True)[:top_n]

    # Direction approximative via contribution (valeur haute vs médiane simple)
    factors = []
    for feat, score in ranked:
        val = features.get(feat)
        impact = "NEGATIVE" if isinstance(val, (int, float)) and float(val) > 0 else "NEUTRAL"
        # Pour LR: signe du coefficient agrégé
        if hasattr(clf, "coef_"):
            signed = 0.0
            for name, coef in zip(feature_names, clf.coef_[0]):
                if feat in name:
                    signed += float(coef)
            if signed > 0.05:
                impact = "NEGATIVE"  # augmente le risque
            elif signed < -0.05:
                impact = "POSITIVE"
            else:
                impact = "NEUTRAL"
        factors.append(
            {
                "feature": _to_camel(feat),
                "impact": impact,
                "importance": round(score / total, 4),
            }
        )
    return factors


def _to_camel(snake: str) -> str:
    parts = snake.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def predict_domain(domain: str, payload: dict[str, Any]) -> dict[str, Any]:
    domain = domain.upper()
    features = normalize_domain_features(domain, payload)
    pipeline, metadata = get_domain_pipeline(domain)

    frame = pd.DataFrame([features])
    # Ordre des colonnes selon metadata
    cols = metadata.get("features")
    if cols:
        frame = frame[cols]

    proba = float(pipeline.predict_proba(frame)[0][1])
    risk = probability_to_risk(domain, proba)
    factors = _feature_importance_factors(pipeline, features, metadata)

    prediction_label = f"RISQUE_{risk}" if not risk.startswith("RISQUE_") else risk

    return {
        "analysisId": str(uuid.uuid4()),
        "domain": domain,
        "prediction": prediction_label,
        "riskLevel": risk,
        "probability": round(proba, 4),
        "confidence": round(proba, 4),
        "recommendation": _recommendation(domain, risk),
        "modelVersion": metadata.get("modelVersion"),
        "datasetVersion": metadata.get("datasetVersion"),
        "modelType": metadata.get("modelType"),
        "explanationMethod": "model_coefficients_or_feature_importance",
        "factors": factors,
        "disclaimer": metadata.get("disclaimer"),
        "generatedAt": _utc_now(),
    }


def models_overview() -> dict[str, Any]:
    models = list_registered_models()
    return {
        "domains": [m.get("domain") for m in models],
        "models": models,
        "generatedAt": _utc_now(),
    }


def model_detail(domain: str) -> dict[str, Any]:
    return load_metadata(domain.lower())
