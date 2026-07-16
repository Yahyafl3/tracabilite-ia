"""Configuration partagée du service ML crédit."""
from __future__ import annotations

import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
ARTIFACTS_DIR = BASE_DIR / "artifacts"
DATA_DIR = BASE_DIR / "data"

MODEL_PATH = ARTIFACTS_DIR / "model.joblib"
METADATA_PATH = ARTIFACTS_DIR / "metadata.json"
DATASET_PATH = DATA_DIR / "synthetic_credit_dataset.csv"

MODEL_VERSION = "2.0.0"
MODEL_TYPE = "LogisticRegression"
EXPLAINABILITY_METHOD = "SHAP LinearExplainer"

NUMERIC_FEATURES = [
    "amount",
    "monthlyIncome",
    "companyAgeYears",
    "paymentIncidents",
    "debtRatio",
]
CATEGORICAL_FEATURES = ["sector"]
ALL_INPUT_FEATURES = NUMERIC_FEATURES + CATEGORICAL_FEATURES

SECTORS = ["SERVICES", "INDUSTRIE", "COMMERCE", "TECH", "AGRICULTURE"]
TARGET_COLUMN = "approved"
OUTPUT_CLASSES = ["REJETER", "APPROUVER"]

RANDOM_STATE = int(os.environ.get("ML_RANDOM_STATE", "42"))
TRAIN_TEST_SIZE = float(os.environ.get("ML_TEST_SIZE", "0.2"))
SYNTHETIC_SAMPLE_SIZE = int(os.environ.get("ML_SYNTHETIC_SAMPLES", "5000"))
