"""Calcul du niveau de risque métier à partir de la prédiction ML."""
from __future__ import annotations


def compute_risk_level(
    decision: str,
    confidence_percent: float,
    debt_ratio: float,
    payment_incidents: float,
) -> str:
    if decision == "REJETER":
        if confidence_percent >= 75 or debt_ratio >= 0.45 or payment_incidents >= 3:
            return "HIGH"
        return "MEDIUM"
    if confidence_percent < 65 or debt_ratio >= 0.35 or payment_incidents >= 2:
        return "MEDIUM"
    return "LOW"
