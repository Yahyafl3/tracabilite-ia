import os

from flask import Flask, jsonify, request
from flask_cors import CORS

from domain_predict_service import model_detail, models_overview, predict_domain
from domain_schemas import DOMAIN_SCHEMAS
from feature_validator import normalize_features
from model_integrity import verify_all
from model_loader import ModelNotReadyError, get_model_info, load_model
from prediction_service import explain_credit, predict_credit
from sector_schema import get_schema_payload

from internal_auth import auth_required_for_path, unauthorized_response, verify_request_token

app = Flask(__name__)
# CORS restreint via env (défaut * pour compat locale ; prod = origines explicites)
_cors_origins = os.environ.get("ML_CORS_ORIGINS", "*")
if _cors_origins.strip() == "*":
    CORS(app)
else:
    CORS(app, origins=[o.strip() for o in _cors_origins.split(",") if o.strip()])

MAX_CONTENT_LENGTH = int(os.environ.get("ML_MAX_CONTENT_LENGTH", str(256 * 1024)))
app.config["MAX_CONTENT_LENGTH"] = MAX_CONTENT_LENGTH


@app.before_request
def require_internal_token():
    """
    Protège tous les endpoints sauf health live/ready.
    /health/ready reste public pour les healthchecks Docker (réseau interne uniquement
    en mode production-like — port 5000 non publié sur le host).
    """
    path = request.path or ""
    if not auth_required_for_path(path):
        return None
    ok, code = verify_request_token(request)
    if ok:
        return None
    return unauthorized_response(code or "TOKEN_INVALID")


@app.route("/health", methods=["GET"])
@app.route("/health/live", methods=["GET"])
def health():
    """Liveness — process up, no model load required. Public."""
    return jsonify({"status": "ok", "probe": "live"}), 200


@app.route("/ready", methods=["GET"])
@app.route("/health/ready", methods=["GET"])
def ready():
    require_checksum = os.environ.get("ML_REQUIRE_CHECKSUM", "false").lower() in ("1", "true", "yes")
    try:
        load_model()
        domains_ready = {}
        for domain in ("credit", "medical", "education"):
            try:
                model_detail(domain)
                domains_ready[domain] = True
            except Exception:
                domains_ready[domain] = False

        integrity = verify_all(require_checksum=require_checksum)
        all_domains = all(domains_ready.values())
        ready_ok = all_domains and integrity["allOk"]

        payload = {
            "ready": ready_ok,
            "creditModelReady": domains_ready.get("credit", False),
            "domainsReady": domains_ready,
            "integrity": integrity,
            "engine": "SKLEARN_MULTIDOMAIN",
            "governance": "DEMO_SYNTHETIC",
        }
        return jsonify(payload), (200 if ready_ok else 503)
    except ModelNotReadyError as exc:
        return jsonify({"ready": False, "error": "MODEL_NOT_READY", "detail": str(exc)}), 503
    except Exception:
        return jsonify({"ready": False, "error": "READY_CHECK_FAILED"}), 503


@app.route("/schema", methods=["GET"])
def schema():
    return jsonify(get_schema_payload()), 200


@app.route("/model/info", methods=["GET"])
def model_info():
    try:
        return jsonify(get_model_info()), 200
    except ModelNotReadyError as exc:
        return jsonify({"error": str(exc)}), 503
    except Exception as exc:
        return jsonify(
            {"error": "Impossible de charger les informations du modèle", "details": str(exc)}
        ), 500


@app.route("/predict", methods=["POST"])
def predict():
    """
    Prédiction crédit avec explicabilité SHAP intégrée.

    Format direct:
    {
      "amount": 25000,
      "monthlyIncome": 15000,
      "companyAgeYears": 5,
      "paymentIncidents": 0,
      "debtRatio": 0.22,
      "sector": "SERVICES"
    }

    Format legacy:
    {
      "domain": "credit",
      "features": { ... }
    }
    """
    try:
        payload = request.get_json(silent=True) or {}
        include_explanation = payload.get("includeExplanation", True)
        features = normalize_features(payload)
        result = predict_credit(features, include_explanation=include_explanation)
        return jsonify(result), 200
    except ValueError as exc:
        return jsonify({"error": str(exc), "code": "VALIDATION_ERROR"}), 400
    except ModelNotReadyError as exc:
        return jsonify({"error": str(exc)}), 503
    except Exception as exc:
        return jsonify({"error": "Erreur lors de la prédiction", "details": str(exc)}), 500


@app.route("/explain", methods=["POST"])
def explain():
    try:
        payload = request.get_json(silent=True) or {}
        features = normalize_features(payload)
        result = explain_credit(features)
        return jsonify(result), 200
    except ValueError as exc:
        return jsonify({"error": str(exc), "code": "VALIDATION_ERROR"}), 400
    except ModelNotReadyError as exc:
        return jsonify({"error": str(exc)}), 503
    except Exception as exc:
        return jsonify({"error": "Erreur lors de l'explication", "details": str(exc)}), 500


def _predict_domain_handler(domain: str):
    try:
        payload = request.get_json(silent=True) or {}
        result = predict_domain(domain, payload)
        return jsonify(result), 200
    except FileNotFoundError as exc:
        return jsonify({"error": str(exc), "code": "MODEL_NOT_FOUND"}), 503
    except ValueError as exc:
        return jsonify({"error": str(exc), "code": "VALIDATION_ERROR"}), 400
    except Exception as exc:
        return jsonify({"error": "Erreur lors de la prédiction", "code": "PREDICT_ERROR"}), 500


@app.route("/predict/credit", methods=["POST"])
def predict_credit_domain():
    return _predict_domain_handler("CREDIT")


@app.route("/predict/medical", methods=["POST"])
def predict_medical_domain():
    return _predict_domain_handler("MEDICAL")


@app.route("/predict/education", methods=["POST"])
def predict_education_domain():
    return _predict_domain_handler("EDUCATION")


@app.route("/models", methods=["GET"])
def models():
    try:
        return jsonify(models_overview()), 200
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


@app.route("/models/<domain>", methods=["GET"])
def models_domain(domain: str):
    try:
        if domain.upper() not in DOMAIN_SCHEMAS:
            return jsonify({"error": f"Domaine inconnu: {domain}"}), 404
        return jsonify(model_detail(domain)), 200
    except FileNotFoundError as exc:
        return jsonify({"error": str(exc)}), 404
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


# Eager load for gunicorn workers (entrypoint trains model if missing).
try:
    load_model()
except Exception as exc:
    print(f"Modele legacy non charge au demarrage: {exc}")

try:
    for _d in ("credit", "medical", "education"):
        model_detail(_d)
    print("Modeles multidomain prets.")
except Exception as exc:
    print(f"Modeles multidomain non charges: {exc}")


if __name__ == "__main__":
    port = int(os.environ.get("PORT", "5000"))
    print("Demarrage du service ML multidomain...")
    load_model()
    print("Modele legacy credit + pipelines multidomain.")
    app.run(host="0.0.0.0", port=port, debug=False)
