"""Tests non-régression des endpoints et modèles multidomain."""
from __future__ import annotations

import pytest

from domain_predict_service import predict_domain
from domain_schemas import normalize_domain_features


CREDIT_PAYLOAD = {
    "secteurActivite": "SERVICES",
    "region": "Casablanca-Settat",
    "ageDemandeur": 35,
    "statutProfessionnel": "SALARIE_CDI",
    "revenuMensuelMad": 12000,
    "chargesMensuellesMad": 4000,
    "montantDemandeMad": 80000,
    "dureeCreditMois": 48,
    "ancienneteProfessionnelleAnnees": 8,
    "creditsExistants": 1,
    "incidentsPaiement24Mois": 0,
    "ratioEndettement": 0.33,
    "typeGarantie": "CAUTION",
    "typeCredit": "CONSOMMATION",
}

MEDICAL_PAYLOAD = {
    "region": "Rabat-Salé-Kénitra",
    "age": 52,
    "sexe": "HOMME",
    "imc": 31.2,
    "niveauActivitePhysique": "SEDENTAIRE",
    "antecedentsFamiliauxDiabete": "OUI",
    "hypertension": "OUI",
    "glycemie": 1.45,
    "polyurie": "OUI",
    "polydipsie": "NON",
    "pertePoidsSoudaine": "NON",
    "faiblesse": "OUI",
    "obesite": "OUI",
    "suiviMedical": "NON",
}

EDUCATION_PAYLOAD = {
    "region": "Marrakech-Safi",
    "typeEtablissement": "UNIVERSITE_PUBLIQUE",
    "filiere": "INFORMATIQUE",
    "niveauEtude": "L2",
    "moyenneSemestre1": 9.5,
    "moyenneSemestre2": 8.8,
    "tauxAbsence": 28,
    "modulesNonValides": 3,
    "participation": "FAIBLE",
    "bourse": "NON",
    "distanceLogementKm": 35,
    "accesInternet": "OUI",
    "activiteProfessionnelle": "OUI",
    "historiqueRedoublement": "NON",
    "situationAcademique": "DIFFICULTE",
}


def test_credit_normalize_and_predict():
    feats = normalize_domain_features("CREDIT", CREDIT_PAYLOAD)
    assert feats["ratio_endettement"] == 0.33
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
    del bad["ratioEndettement"]
    with pytest.raises(ValueError, match="Features manquantes"):
        normalize_domain_features("CREDIT", bad)


def test_invalid_category():
    bad = dict(MEDICAL_PAYLOAD)
    bad["sexe"] = "AUTRE"
    with pytest.raises(ValueError, match="invalide"):
        normalize_domain_features("MEDICAL", bad)


def test_reproducibility():
    a = predict_domain("CREDIT", CREDIT_PAYLOAD)
    b = predict_domain("CREDIT", CREDIT_PAYLOAD)
    assert a["probability"] == b["probability"]
    assert a["prediction"] == b["prediction"]
