"""Schémas Pydantic alignés sur le pipeline Scikit-learn."""
from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator

from config import SECTORS
from sector_schema import get_allowed_ml_keys

SectorLiteral = Literal["SERVICES", "INDUSTRIE", "COMMERCE", "TECH", "AGRICULTURE"]


class CreditFeaturesSchema(BaseModel):
    """Features réellement consommées par le pipeline ML."""

    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    amount: float = Field(gt=0, description="Montant demandé (> 0)")
    monthlyIncome: float = Field(gt=0, description="Revenu mensuel (> 0)")
    companyAgeYears: float = Field(ge=0, description="Ancienneté entreprise (>= 0)")
    paymentIncidents: float = Field(ge=0, description="Incidents de paiement (>= 0)")
    debtRatio: float = Field(ge=0, le=1, description="Ratio d'endettement entre 0 et 1")
    sector: SectorLiteral

    @field_validator("sector", mode="before")
    @classmethod
    def normalize_sector(cls, value: Any) -> str:
        return str(value).upper()

    def to_model_dict(self) -> dict[str, Any]:
        return {
            "amount": float(self.amount),
            "monthlyIncome": float(self.monthlyIncome),
            "companyAgeYears": float(self.companyAgeYears),
            "paymentIncidents": float(self.paymentIncidents),
            "debtRatio": float(self.debtRatio),
            "sector": self.sector,
        }


class PredictRequestSchema(BaseModel):
    model_config = ConfigDict(extra="forbid")

    amount: float | None = Field(default=None, gt=0)
    monthlyIncome: float | None = Field(default=None, gt=0)
    companyAgeYears: float | None = Field(default=None, ge=0)
    paymentIncidents: float | None = Field(default=None, ge=0)
    debtRatio: float | None = Field(default=None, ge=0, le=1)
    sector: SectorLiteral | None = None
    includeExplanation: bool = True
    domain: str | None = None
    features: dict[str, Any] | None = None

    def extract_features(self) -> CreditFeaturesSchema:
        if self.features is not None:
            payload = dict(self.features)
        else:
            payload = {
                "amount": self.amount,
                "monthlyIncome": self.monthlyIncome,
                "companyAgeYears": self.companyAgeYears,
                "paymentIncidents": self.paymentIncidents,
                "debtRatio": self.debtRatio,
                "sector": self.sector,
            }
        missing = [key for key, value in payload.items() if value is None]
        if missing:
            raise ValueError(f"Champs manquants: {', '.join(missing)}")
        return CreditFeaturesSchema.model_validate(payload)


def validate_sector_fields(sector: str, payload: dict[str, Any]) -> None:
    """Vérifie que seules les features ML autorisées sont présentes."""
    allowed = get_allowed_ml_keys(sector)
    unknown = sorted(set(payload.keys()) - allowed)
    if unknown:
        raise ValueError(
            f"Champs non autorisés pour le secteur '{sector}': {', '.join(unknown)}. "
            f"Features acceptées: {', '.join(sorted(allowed))}"
        )
