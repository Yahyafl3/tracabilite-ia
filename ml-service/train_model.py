"""Entraînement, évaluation et persistance du modèle Scikit-learn."""
from __future__ import annotations

import json
from datetime import datetime, timezone

import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

from config import (
    ALL_INPUT_FEATURES,
    ARTIFACTS_DIR,
    CATEGORICAL_FEATURES,
    EXPLAINABILITY_METHOD,
    METADATA_PATH,
    MODEL_PATH,
    MODEL_TYPE,
    MODEL_VERSION,
    NUMERIC_FEATURES,
    RANDOM_STATE,
    TARGET_COLUMN,
    TRAIN_TEST_SIZE,
)
from dataset_service import dataset_summary, load_or_create_dataset
from sector_schema import SCHEMA_DESCRIPTION, SCHEMA_TYPE
from shap_config import get_explanation_metadata


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def build_pipeline() -> Pipeline:
    numeric_transformer = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="median")),
            ("scaler", StandardScaler()),
        ]
    )
    categorical_transformer = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="most_frequent")),
            (
                "encoder",
                OneHotEncoder(handle_unknown="ignore", sparse_output=False),
            ),
        ]
    )
    preprocessor = ColumnTransformer(
        transformers=[
            ("num", numeric_transformer, NUMERIC_FEATURES),
            ("cat", categorical_transformer, CATEGORICAL_FEATURES),
        ]
    )
    classifier = LogisticRegression(
        max_iter=2000,
        class_weight="balanced",
        random_state=RANDOM_STATE,
    )
    return Pipeline(
        steps=[
            ("preprocessor", preprocessor),
            ("classifier", classifier),
        ]
    )


def extract_feature_names(pipeline: Pipeline) -> list[str]:
    preprocessor: ColumnTransformer = pipeline.named_steps["preprocessor"]
    return list(preprocessor.get_feature_names_out())


def evaluate_model(pipeline: Pipeline, x_test: pd.DataFrame, y_test: pd.Series) -> dict:
    y_pred = pipeline.predict(x_test)
    y_proba = pipeline.predict_proba(x_test)[:, 1]

    return {
        "accuracy": round(float(accuracy_score(y_test, y_pred)), 4),
        "precision": round(float(precision_score(y_test, y_pred, zero_division=0)), 4),
        "recall": round(float(recall_score(y_test, y_pred, zero_division=0)), 4),
        "f1": round(float(f1_score(y_test, y_pred, zero_division=0)), 4),
        "roc_auc": round(float(roc_auc_score(y_test, y_proba)), 4),
    }


def benchmark_alternatives(
    x_train: pd.DataFrame,
    y_train: pd.Series,
    x_test: pd.DataFrame,
    y_test: pd.Series,
) -> dict:
    """Compare des modèles alternatifs sans les déployer."""
    preprocessor = build_pipeline().named_steps["preprocessor"]
    x_train_t = preprocessor.fit_transform(x_train)
    x_test_t = preprocessor.transform(x_test)

    candidates = {
        "RandomForestClassifier": RandomForestClassifier(
            n_estimators=120,
            max_depth=6,
            min_samples_leaf=2,
            class_weight="balanced",
            random_state=RANDOM_STATE,
        ),
    }

    benchmark = {}
    for name, estimator in candidates.items():
        estimator.fit(x_train_t, y_train)
        y_pred = estimator.predict(x_test_t)
        y_proba = estimator.predict_proba(x_test_t)[:, 1]
        benchmark[name] = {
            "accuracy": round(float(accuracy_score(y_test, y_pred)), 4),
            "f1": round(float(f1_score(y_test, y_pred, zero_division=0)), 4),
            "roc_auc": round(float(roc_auc_score(y_test, y_proba)), 4),
        }
    return benchmark


def train_and_save(force_regenerate_dataset: bool = False) -> dict:
    df = load_or_create_dataset(force_regenerate=force_regenerate_dataset)
    x_data = df[ALL_INPUT_FEATURES]
    y_data = df[TARGET_COLUMN]

    x_train, x_test, y_train, y_test = train_test_split(
        x_data,
        y_data,
        test_size=TRAIN_TEST_SIZE,
        random_state=RANDOM_STATE,
        stratify=y_data,
    )

    pipeline = build_pipeline()
    pipeline.fit(x_train, y_train)

    metrics = evaluate_model(pipeline, x_test, y_test)
    benchmark = benchmark_alternatives(x_train, y_train, x_test, y_test)
    feature_names = extract_feature_names(pipeline)

    background = pipeline.named_steps["preprocessor"].transform(x_train)
    background_sample = background[: min(200, len(background))]

    ARTIFACTS_DIR.mkdir(parents=True, exist_ok=True)
    metadata = {
        "modelType": MODEL_TYPE,
        "version": MODEL_VERSION,
        "domain": "credit",
        "explainability": EXPLAINABILITY_METHOD,
        "explanationSource": "SHAP",
        "dataset": {
            **dataset_summary(df),
            "trainRows": int(len(x_train)),
            "testRows": int(len(x_test)),
        },
        "features": {
            "numeric": NUMERIC_FEATURES,
            "categorical": CATEGORICAL_FEATURES,
            "schemaType": SCHEMA_TYPE,
            "description": SCHEMA_DESCRIPTION,
        },
        "metrics": metrics,
        "benchmarkAlternatives": benchmark,
        "featureNames": feature_names,
        "shapExplanation": get_explanation_metadata(feature_names),
        "trainedAt": _utc_now(),
        "randomState": RANDOM_STATE,
        "note": (
            "Métriques mesurées sur jeu de test hold-out. "
            "Dataset synthétique — prototype académique uniquement."
        ),
    }

    artifact = {
        "pipeline": pipeline,
        "feature_names": feature_names,
        "background": background_sample,
        "metadata": metadata,
        "version": MODEL_VERSION,
    }

    joblib.dump(artifact, MODEL_PATH)
    METADATA_PATH.write_text(json.dumps(metadata, indent=2, ensure_ascii=False), encoding="utf-8")

    return metadata


if __name__ == "__main__":
    result = train_and_save(force_regenerate_dataset=True)
    print("Entraînement terminé.")
    print(json.dumps(result["metrics"], indent=2))
    print(f"Modèle sauvegardé: {MODEL_PATH}")
    print(f"Métadonnées: {METADATA_PATH}")
