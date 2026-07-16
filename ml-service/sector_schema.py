"""Configuration centralisée des champs ML par secteur.

Le pipeline Scikit-learn utilise le MÊME schéma pour tous les secteurs.
Le secteur est une feature catégorielle encodée via OneHotEncoder.
Les entrées par secteur listent les mêmes clés ML avec labels/aides/valeurs
par défaut adaptés au contexte métier.
"""
from __future__ import annotations

from typing import Any

from config import ALL_INPUT_FEATURES, CATEGORICAL_FEATURES, NUMERIC_FEATURES, SECTORS

SCHEMA_TYPE = "unified"
SCHEMA_DESCRIPTION = (
    "Le modèle LogisticRegression utilise un pipeline commun pour tous les secteurs. "
    "Le secteur est une feature catégorielle encodée en OneHot (cat__sector_*)."
)

NUMERIC_FIELD_DEFS: list[dict[str, Any]] = [
    {
        "key": "amount",
        "type": "number",
        "min": 1,
        "step": 1,
        "required": True,
    },
    {
        "key": "monthlyIncome",
        "type": "number",
        "min": 1,
        "step": 1,
        "required": True,
    },
    {
        "key": "companyAgeYears",
        "type": "number",
        "min": 0,
        "step": 1,
        "required": True,
    },
    {
        "key": "paymentIncidents",
        "type": "number",
        "min": 0,
        "step": 1,
        "required": True,
    },
    {
        "key": "debtRatio",
        "type": "number",
        "min": 0,
        "max": 1,
        "step": 0.01,
        "required": True,
    },
]

SECTOR_PRESENTATION: dict[str, dict[str, Any]] = {
    "SERVICES": {
        "label": "Services",
        "hint": (
            "Même schéma ML que les autres secteurs. Le secteur SERVICES influence "
            "la décision via son coefficient OneHot appris à l'entraînement."
        ),
        "defaults": {
            "amount": 25000,
            "monthlyIncome": 15000,
            "companyAgeYears": 5,
            "paymentIncidents": 0,
            "debtRatio": 0.22,
        },
        "labels": {
            "amount": "Montant demandé",
            "monthlyIncome": "Revenu mensuel",
            "companyAgeYears": "Ancienneté de l'entreprise (années)",
            "paymentIncidents": "Incidents de paiement",
            "debtRatio": "Ratio d'endettement (0-1)",
        },
        "help": {
            "amount": "Montant du crédit professionnel demandé.",
            "monthlyIncome": "Revenu mensuel net de l'entreprise de services.",
            "companyAgeYears": "Nombre d'années d'activité.",
            "paymentIncidents": "Incidents de paiement sur les 24 derniers mois.",
            "debtRatio": "Endettement actuel / revenus (0 = aucun, 1 = maximum).",
        },
    },
    "INDUSTRIE": {
        "label": "Industrie",
        "hint": (
            "Le modèle n'ajoute pas de champs spécifiques à l'industrie. "
            "Le secteur INDUSTRIE est transmis comme feature catégorielle."
        ),
        "defaults": {
            "amount": 50000,
            "monthlyIncome": 20000,
            "companyAgeYears": 8,
            "paymentIncidents": 1,
            "debtRatio": 0.35,
        },
        "labels": {
            "amount": "Montant d'investissement industriel",
            "monthlyIncome": "Chiffre d'affaires mensuel moyen",
            "companyAgeYears": "Ancienneté du site industriel (années)",
            "paymentIncidents": "Incidents de paiement fournisseurs",
            "debtRatio": "Ratio d'endettement industriel (0-1)",
        },
        "help": {
            "amount": "Investissement ou crédit d'équipement industriel.",
            "monthlyIncome": "Revenus mensuels récurrents de l'activité industrielle.",
            "companyAgeYears": "Ancienneté de l'exploitation industrielle.",
            "paymentIncidents": "Retards ou incidents de paiement récents.",
            "debtRatio": "Part de la dette dans les revenus mensuels.",
        },
    },
    "COMMERCE": {
        "label": "Commerce",
        "hint": (
            "Schéma ML unifié : le secteur COMMERCE modifie la prédiction via "
            "OneHot, sans champs supplémentaires."
        ),
        "defaults": {
            "amount": 30000,
            "monthlyIncome": 12000,
            "companyAgeYears": 4,
            "paymentIncidents": 0,
            "debtRatio": 0.28,
        },
        "labels": {
            "amount": "Montant de financement commercial",
            "monthlyIncome": "Revenu mensuel du point de vente",
            "companyAgeYears": "Ancienneté du commerce (années)",
            "paymentIncidents": "Incidents de paiement",
            "debtRatio": "Ratio d'endettement (0-1)",
        },
        "help": {
            "amount": "Besoin de trésorerie ou d'investissement commercial.",
            "monthlyIncome": "Revenus mensuels moyens du commerce.",
            "companyAgeYears": "Durée d'exploitation du commerce.",
            "paymentIncidents": "Incidents bancaires ou fournisseurs récents.",
            "debtRatio": "Niveau d'endettement actuel.",
        },
    },
    "TECH": {
        "label": "Technologie",
        "hint": (
            "Le dataset synthétique accorde un bonus d'approbation au secteur TECH, "
            "appris par le modèle via l'encodage OneHot du secteur."
        ),
        "defaults": {
            "amount": 40000,
            "monthlyIncome": 25000,
            "companyAgeYears": 3,
            "paymentIncidents": 0,
            "debtRatio": 0.18,
        },
        "labels": {
            "amount": "Montant de financement tech",
            "monthlyIncome": "Revenu mensuel récurrent (MRR)",
            "companyAgeYears": "Ancienneté de la startup (années)",
            "paymentIncidents": "Incidents de paiement",
            "debtRatio": "Ratio d'endettement (0-1)",
        },
        "help": {
            "amount": "Montant demandé pour développement ou croissance tech.",
            "monthlyIncome": "Revenus mensuels (abonnements, licences, etc.).",
            "companyAgeYears": "Ancienneté de la structure technologique.",
            "paymentIncidents": "Incidents de paiement sur la période récente.",
            "debtRatio": "Endettement rapporté aux revenus mensuels.",
        },
    },
    "AGRICULTURE": {
        "label": "Agriculture",
        "hint": (
            "Le secteur AGRICULTURE est une feature catégorielle du modèle. "
            "Les mêmes 5 variables numériques sont utilisées pour tous les secteurs."
        ),
        "defaults": {
            "amount": 20000,
            "monthlyIncome": 8000,
            "companyAgeYears": 10,
            "paymentIncidents": 2,
            "debtRatio": 0.40,
        },
        "labels": {
            "amount": "Montant de financement agricole",
            "monthlyIncome": "Revenu mensuel de l'exploitation",
            "companyAgeYears": "Ancienneté de l'exploitation (années)",
            "paymentIncidents": "Incidents de paiement",
            "debtRatio": "Ratio d'endettement (0-1)",
        },
        "help": {
            "amount": "Crédit pour matériel, intrants ou trésorerie agricole.",
            "monthlyIncome": "Revenus mensuels moyens de l'exploitation.",
            "companyAgeYears": "Ancienneté de l'activité agricole.",
            "paymentIncidents": "Retards ou incidents de paiement.",
            "debtRatio": "Part de la dette dans les revenus mensuels.",
        },
    },
}

SECTOR_HINT_UI = "Les données demandées sont adaptées au secteur sélectionné."


def _build_sector_fields(sector: str) -> list[dict[str, Any]]:
    presentation = SECTOR_PRESENTATION[sector]
    fields: list[dict[str, Any]] = []
    for field_def in NUMERIC_FIELD_DEFS:
        key = field_def["key"]
        fields.append(
            {
                **field_def,
                "label": presentation["labels"][key],
                "help": presentation["help"][key],
                "defaultValue": presentation["defaults"][key],
            }
        )
    return fields


SECTOR_FIELDS: dict[str, list[dict[str, Any]]] = {
    sector: _build_sector_fields(sector) for sector in SECTORS
}


def get_allowed_ml_keys(sector: str) -> set[str]:
    if sector not in SECTORS:
        raise ValueError(
            f"Secteur invalide '{sector}'. Valeurs acceptées: {', '.join(SECTORS)}"
        )
    return set(ALL_INPUT_FEATURES)


def get_schema_payload() -> dict[str, Any]:
    return {
        "schemaType": SCHEMA_TYPE,
        "description": SCHEMA_DESCRIPTION,
        "sectors": SECTORS,
        "numericFeatures": NUMERIC_FEATURES,
        "categoricalFeatures": CATEGORICAL_FEATURES,
        "allInputFeatures": ALL_INPUT_FEATURES,
        "sectorHint": SECTOR_HINT_UI,
        "encoding": {
            "numeric": "SimpleImputer(median) + StandardScaler",
            "categorical": "SimpleImputer(most_frequent) + OneHotEncoder",
        },
        "sectorFields": SECTOR_FIELDS,
        "sectorPresentation": {
            sector: {
                "label": SECTOR_PRESENTATION[sector]["label"],
                "hint": SECTOR_PRESENTATION[sector]["hint"],
            }
            for sector in SECTORS
        },
    }
