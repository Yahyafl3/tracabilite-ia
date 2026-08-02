"""Tests agents consultatifs Flask — ne doivent pas modifier le ML."""
from __future__ import annotations

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


def test_include_agents_flag_ignored_by_ml_layer():
    """Flask ML layer n'utilise pas les agents — includeAgents n'altère pas la prédiction."""
    a = predict_domain("CREDIT", {**CREDIT_PAYLOAD, "includeAgents": False})
    b = predict_domain("CREDIT", {**CREDIT_PAYLOAD, "includeAgents": True})
    assert a["prediction"] == b["prediction"]
    assert a["probability"] == b["probability"]
    assert a["factors"] == b["factors"]


def test_models_endpoint_shape(client=None):
    import os
    from app import app

    token = os.environ.get("ML_SERVICE_TOKEN", "test-ml-token-for-unit-tests-only")
    os.environ["ML_SERVICE_TOKEN"] = token
    with app.test_client() as c:
        r = c.get("/health")
        assert r.status_code == 200
        assert r.get_json()["status"] == "ok"
        denied = c.get("/models")
        assert denied.status_code == 401
        m = c.get("/models", headers={"X-Internal-Token": token})
        assert m.status_code == 200
        body = m.get_json()
        assert "models" in body or "domains" in body
