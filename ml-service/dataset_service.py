"""Génération et préparation du dataset crédit."""
from __future__ import annotations

from pathlib import Path

import numpy as np
import pandas as pd

from config import (
    ALL_INPUT_FEATURES,
    CATEGORICAL_FEATURES,
    DATASET_PATH,
    NUMERIC_FEATURES,
    RANDOM_STATE,
    SECTORS,
    SYNTHETIC_SAMPLE_SIZE,
    TARGET_COLUMN,
)

SYNTHETIC_WARNING = (
    "Dataset synthétique généré pour prototype académique. "
    "Ne pas utiliser en production."
)


def generate_synthetic_dataset(
    n_samples: int = SYNTHETIC_SAMPLE_SIZE,
    random_state: int = RANDOM_STATE,
) -> pd.DataFrame:
    """Génère un dataset crédit synthétique avec une logique de score explicite."""
    rng = np.random.default_rng(random_state)
    sectors = rng.choice(SECTORS, size=n_samples)

    monthly_income = rng.integers(4_000, 35_000, size=n_samples).astype(float)
    amount = (monthly_income * rng.uniform(0.5, 4.0, size=n_samples)).astype(float)
    company_age = rng.integers(0, 25, size=n_samples).astype(float)
    payment_incidents = rng.integers(0, 6, size=n_samples).astype(float)
    debt_ratio = np.clip(rng.normal(0.25, 0.15, size=n_samples), 0.05, 0.95)

    sector_bonus = np.select(
        [sectors == "TECH", sectors == "AGRICULTURE", sectors == "COMMERCE"],
        [0.20, -0.15, 0.05],
        default=0.0,
    )

    score = (
        (monthly_income / np.maximum(amount, 1.0)) * 2.0
        + company_age * 0.05
        - payment_incidents * 0.45
        - debt_ratio * 2.5
        + sector_bonus
        + rng.normal(0, 0.15, size=n_samples)
    )
    approved = (score > 0.55).astype(int)

    df = pd.DataFrame(
        {
            "amount": amount,
            "monthlyIncome": monthly_income,
            "companyAgeYears": company_age,
            "paymentIncidents": payment_incidents,
            "debtRatio": debt_ratio,
            "sector": sectors,
            TARGET_COLUMN: approved,
            "dataset_source": "synthetic",
        }
    )
    return df


def clean_dataset(df: pd.DataFrame) -> pd.DataFrame:
    """Nettoyage minimal : doublons, types, bornes et valeurs manquantes."""
    cleaned = df.copy()
    cleaned = cleaned.drop_duplicates().reset_index(drop=True)

    for column in NUMERIC_FEATURES:
        cleaned[column] = pd.to_numeric(cleaned[column], errors="coerce")
    cleaned[CATEGORICAL_FEATURES[0]] = (
        cleaned[CATEGORICAL_FEATURES[0]].astype(str).str.upper()
    )
    cleaned[TARGET_COLUMN] = pd.to_numeric(cleaned[TARGET_COLUMN], errors="coerce")

    cleaned = cleaned.dropna(subset=ALL_INPUT_FEATURES + [TARGET_COLUMN])
    cleaned = cleaned[cleaned["sector"].isin(SECTORS)]
    cleaned = cleaned[
        (cleaned["amount"] > 0)
        & (cleaned["monthlyIncome"] > 0)
        & (cleaned["companyAgeYears"] >= 0)
        & (cleaned["paymentIncidents"] >= 0)
        & (cleaned["debtRatio"].between(0.0, 1.0))
    ]

    cleaned[TARGET_COLUMN] = cleaned[TARGET_COLUMN].astype(int)
    return cleaned.reset_index(drop=True)


def load_or_create_dataset(
    dataset_path: Path = DATASET_PATH,
    force_regenerate: bool = False,
) -> pd.DataFrame:
    dataset_path.parent.mkdir(parents=True, exist_ok=True)

    if dataset_path.exists() and not force_regenerate:
        df = pd.read_csv(dataset_path)
    else:
        df = generate_synthetic_dataset()
        df.to_csv(dataset_path, index=False)

    return clean_dataset(df)


def dataset_summary(df: pd.DataFrame) -> dict:
    return {
        "source": "synthetic",
        "warning": SYNTHETIC_WARNING,
        "rows": int(len(df)),
        "columns": list(df.columns),
        "target": TARGET_COLUMN,
        "classBalance": {
            "approved": int((df[TARGET_COLUMN] == 1).sum()),
            "rejected": int((df[TARGET_COLUMN] == 0).sum()),
        },
        "missingValuesAfterCleaning": int(df.isna().sum().sum()),
        "duplicateRowsRemoved": True,
    }
