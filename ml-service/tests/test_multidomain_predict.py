"""Tests non-régression des endpoints et modèles multidomain (datasets publics v2)."""
from __future__ import annotations

import pytest

from domain_predict_service import predict_domain
from domain_schemas import normalize_domain_features


CREDIT_PAYLOAD = {
    "age": 35,
    "dureeMois": 48,
    "typeContrat": "CDI",
    "statutLogement": "PROPRIETAIRE",
    "incidentPaiementBam": 0,
    "montantDemandeMad": 80000,
    "nouvelleEcheanceMad": 2500,
    "revenuMensuelMad": 12000,
    "tauxEndettement": 0.33,
}

MEDICAL_PAYLOAD = {
    "age": 52,
    "grossesses": 0,
    "glycemieMgDl": 148,
    "pressionArterielleMmhg": 72,
    "epaisseurPliCutaneMm": 35,
    "insulineMicroUMl": 125,
    "imcKgM2": 33.6,
}

EDUCATION_PAYLOAD = {
    "ageInscription": 20,
    "noteAdmission": 127.3,
    "noteQualificationPrecedente": 122.0,
    "unitesValideesS1": 0,
    "moyenneS1": 0.0,
    "unitesValideesS2": 0,
    "moyenneS2": 0.0,
    "tauxChomage": 10.8,
    "tauxInflation": 1.4,
    "pib": 1.74,
    "sexe": "HOMME",
    "boursier": "NON",
    "fraisAJour": "OUI",
    "debiteur": "NON",
    "deplace": "OUI",
    "international": "NON",
}


def test_credit_normalize_and_predict():
    feats = normalize_domain_features("CREDIT", CREDIT_PAYLOAD)
    assert feats["taux_endettement"] == 0.33
    result = predict_domain("CREDIT", CREDIT_PAYLOAD)
    assert result["domain"] == "CREDIT"
    assert result["prediction"].startswith("RISQUE_")
    assert 0 <= result["probability"] <= 1
    assert result["modelVersion"]
    assert result["analysisId"]


def test_medical_predict():
    result = predict_domain("MEDICAL", MEDICAL_PAYLOAD)
    assert result["domain"] == "MEDICAL"
    assert result["riskLevel"] in {"FAIBLE", "MODERE", "ELEVE"}
    assert result["disclaimer"]


def test_education_predict():
    result = predict_domain("EDUCATION", EDUCATION_PAYLOAD)
    assert result["domain"] == "EDUCATION"
    assert isinstance(result["factors"], list)


def test_missing_feature():
    bad = dict(CREDIT_PAYLOAD)
    del bad["tauxEndettement"]
    with pytest.raises(ValueError, match="Features manquantes"):
        normalize_domain_features("CREDIT", bad)


def test_invalid_category():
    bad = dict(CREDIT_PAYLOAD)
    bad["typeContrat"] = "AUTRE"
    with pytest.raises(ValueError, match="invalide"):
        normalize_domain_features("CREDIT", bad)


def test_reproducibility():
    a = predict_domain("CREDIT", CREDIT_PAYLOAD)
    b = predict_domain("CREDIT", CREDIT_PAYLOAD)
    assert a["probability"] == b["probability"]
    assert a["prediction"] == b["prediction"]
