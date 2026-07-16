"""Tests de cohérence scientifique et métier SHAP."""
from __future__ import annotations

import numpy as np
import pandas as pd
import pytest

from config import ALL_INPUT_FEATURES, TARGET_COLUMN
from dataset_service import load_or_create_dataset
from explainability_service import (
    compute_contribution_percent,
    compute_shap_factors,
    get_shap_explanation_bundle,
)
from model_loader import load_model
from prediction_service import predict_credit
from shap_config import (
    EXPLAINED_CLASS_INDEX,
    EXPLAINED_CLASS_LABEL,
    EXPECTED_COEF_SIGN_APPROVE,
)


@pytest.fixture(scope="module")
def model_state():
    return load_model(force_reload=True)


@pytest.fixture(scope="module")
def baseline_features() -> dict:
    return {
        "amount": 25000,
        "monthlyIncome": 15000,
        "companyAgeYears": 5,
        "paymentIncidents": 1,
        "debtRatio": 0.22,
        "sector": "SERVICES",
    }


def test_explained_class_is_approve(model_state, baseline_features):
    factors = compute_shap_factors(baseline_features)
    assert factors, "Aucun facteur SHAP"
    assert all(f["explainedClass"] == EXPLAINED_CLASS_LABEL for f in factors)
    assert model_state["pipeline"].named_steps["classifier"].classes_[EXPLAINED_CLASS_INDEX] == 1


def test_log_odds_identity(model_state, baseline_features):
    bundle = get_shap_explanation_bundle(baseline_features)
    meta = bundle["metadata"]
    assert abs(meta["logOddsApproval"] - meta["logOddsCheck"]) < 1e-4


def test_transformed_feature_order_matches_pipeline(model_state):
    pipeline = model_state["pipeline"]
    preprocessor = pipeline.named_steps["preprocessor"]
    expected = list(preprocessor.get_feature_names_out())
    assert model_state["feature_names"] == expected
    assert len(expected) == 10
    assert expected[0] == "num__amount"
    assert expected[4] == "num__debtRatio"
    assert expected[5].startswith("cat__sector_")


def test_model_coefficient_signs_match_business_expectation(model_state):
    clf = model_state["pipeline"].named_steps["classifier"]
    feature_names = model_state["feature_names"]
    for idx, name in enumerate(feature_names):
        if name not in EXPECTED_COEF_SIGN_APPROVE:
            continue
        coef = clf.coef_[0][idx]
        expected = EXPECTED_COEF_SIGN_APPROVE[name]
        if expected == "positive":
            assert coef > 0, f"{name} devrait avoir coef>0 pour APPROUVER, obtenu {coef}"
        else:
            assert coef < 0, f"{name} devrait avoir coef<0 pour APPROUVER, obtenu {coef}"


def test_higher_payment_incidents_reduces_approval_probability(baseline_features):
    low = {**baseline_features, "paymentIncidents": 0}
    high = {**baseline_features, "paymentIncidents": 5}
    p_low = predict_credit(low, include_explanation=False)["probabilities"]["approuver"]
    p_high = predict_credit(high, include_explanation=False)["probabilities"]["approuver"]
    assert p_low > p_high, (
        f"Plus d'incidents devrait réduire P(APPROUVER): {p_low} vs {p_high}"
    )


def test_higher_debt_ratio_reduces_approval_probability(baseline_features):
    low = {**baseline_features, "debtRatio": 0.10}
    high = {**baseline_features, "debtRatio": 0.80}
    p_low = predict_credit(low, include_explanation=False)["probabilities"]["approuver"]
    p_high = predict_credit(high, include_explanation=False)["probabilities"]["approuver"]
    assert p_low > p_high


def test_contribution_percent_sums_to_100(baseline_features):
    factors = compute_shap_factors(baseline_features)
    total = sum(f["contributionPercent"] for f in factors)
    assert 99.0 <= total <= 101.0


def test_contribution_percent_formula_unit():
    assert compute_contribution_percent(2.0, 10.0) == 20.0
    assert compute_contribution_percent(-3.0, 10.0) == 30.0


def test_payment_incidents_below_mean_can_have_positive_shap_toward_approve(
    model_state, baseline_features
):
    """
    Cas métier documenté : paymentIncidents=1 peut être sous la moyenne du dataset.
    Avec coef<0, SHAP = coef*(x-mean) devient positif → favorise APPROUVER.
    Ce n'est PAS une inversion de signe.
    """
    factors = {f["name"]: f for f in compute_shap_factors(baseline_features)}
    clf = model_state["pipeline"].named_steps["classifier"]
    pre = model_state["pipeline"].named_steps["preprocessor"]
    bg = model_state["background"]
    fn = model_state["feature_names"]
    idx = fn.index("num__paymentIncidents")
    assert clf.coef_[0][idx] < 0

    frame = pd.DataFrame([{k: baseline_features[k] for k in ALL_INPUT_FEATURES}])
    xt = pre.transform(frame)[0]
    mean = bg.mean(axis=0)
    if xt[idx] < mean[idx]:
        assert factors["paymentIncidents"]["shapValue"] > 0
        assert factors["paymentIncidents"]["impact"] == "POSITIVE"


def test_dataset_label_logic_correlates_with_risk_factors():
    df = load_or_create_dataset()
    assert len(df) > 100
    assert df[TARGET_COLUMN].isin([0, 1]).all()
    corr = df[["paymentIncidents", "debtRatio", TARGET_COLUMN]].corr()[TARGET_COLUMN]
    assert corr["paymentIncidents"] < 0
    assert corr["debtRatio"] < 0


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
