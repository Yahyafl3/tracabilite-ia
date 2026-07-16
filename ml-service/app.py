from flask import Flask, jsonify, request

from flask_cors import CORS



from feature_validator import normalize_features

from model_loader import ModelNotReadyError, get_model_info, is_ready, load_model

from prediction_service import explain_credit, predict_credit

from sector_schema import get_schema_payload



app = Flask(__name__)

CORS(app)





@app.route("/health", methods=["GET"])

def health():

    return jsonify(

        {

            "status": "healthy",

            "service": "ML Decision Service - Credit + SHAP",

            "creditModelReady": is_ready(),

            "engine": "SKLEARN_SHAP",

            "timestamp": _utc_now(),

        }

    ), 200





@app.route("/ready", methods=["GET"])

def ready():

    try:

        load_model()

        return jsonify({"ready": True, "creditModelReady": True, "engine": "SKLEARN_SHAP"}), 200

    except ModelNotReadyError as exc:

        return jsonify({"ready": False, "error": str(exc)}), 503

    except Exception as exc:

        return jsonify({"ready": False, "error": str(exc)}), 503





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





def _utc_now():

    from datetime import datetime, timezone



    return datetime.now(timezone.utc).isoformat()





if __name__ == "__main__":

    print("Demarrage du service ML credit + SHAP...")

    load_model()

    print("Modele Scikit-learn + SHAP charge.")

    app.run(host="0.0.0.0", port=5000, debug=False)

