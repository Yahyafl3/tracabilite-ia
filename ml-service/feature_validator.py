"""Validation et normalisation des features d'entrée."""
from __future__ import annotations

from typing import Any

from pydantic import ValidationError

from config import ALL_INPUT_FEATURES
from schemas import CreditFeaturesSchema, PredictRequestSchema, validate_sector_fields

ALLOWED_TOP_LEVEL_KEYS = {
    *ALL_INPUT_FEATURES,
    "includeExplanation",
    "domain",
    "features",
}


def _format_validation_error(exc: ValidationError) -> str:
    messages = []
    for error in exc.errors():
        loc = ".".join(str(part) for part in error.get("loc", ()))
        msg = error.get("msg", "valeur invalide")
        messages.append(f"{loc}: {msg}" if loc else msg)
    return "; ".join(messages)


def normalize_features(payload: dict[str, Any]) -> dict[str, Any]:
    """Accepte le format direct ou legacy `{ features: {...} }`."""
    if payload is None:
        payload = {}

    unknown_top_level = sorted(set(payload.keys()) - ALLOWED_TOP_LEVEL_KEYS)
    if unknown_top_level:
        raise ValueError(
            f"Champs non reconnus: {', '.join(unknown_top_level)}. "
            f"Clés acceptées: {', '.join(sorted(ALLOWED_TOP_LEVEL_KEYS))}"
        )

    try:
        request = PredictRequestSchema.model_validate(payload)
        features = request.extract_features()
    except ValidationError as exc:
        raise ValueError(_format_validation_error(exc)) from exc

    normalized = features.to_model_dict()
    validate_sector_fields(normalized["sector"], normalized)
    return normalized
