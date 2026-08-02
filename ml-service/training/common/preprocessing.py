"""Utilitaires de prétraitement partagés pour les pipelines multidomain."""
from __future__ import annotations

from sklearn.compose import ColumnTransformer
from sklearn.impute import SimpleImputer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


def build_preprocessor(
    numeric_features: list[str],
    categorical_features: list[str],
) -> ColumnTransformer:
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
    transformers = [("num", numeric_transformer, numeric_features)]
    if categorical_features:
        transformers.append(("cat", categorical_transformer, categorical_features))
    return ColumnTransformer(transformers=transformers)


def build_model_pipeline(
    classifier,
    numeric_features: list[str],
    categorical_features: list[str],
) -> Pipeline:
    return Pipeline(
        steps=[
            (
                "preprocessor",
                build_preprocessor(numeric_features, categorical_features),
            ),
            ("classifier", classifier),
        ]
    )
