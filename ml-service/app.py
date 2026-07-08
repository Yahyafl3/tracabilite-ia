from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import numpy as np
import os
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Dictionnaire de modèles par domaine
MODELS = {}
MODEL_DIR = 'models'

# Créer le dossier models s'il n'existe pas
if not os.path.exists(MODEL_DIR):
    os.makedirs(MODEL_DIR)

def load_or_create_model(domain='general'):
    """Charge le modèle existant pour un domaine ou en crée un nouveau"""
    model_path = os.path.join(MODEL_DIR, f'{domain}_model.pkl')
    
    if os.path.exists(model_path):
        return joblib.load(model_path)
    else:
        from sklearn.ensemble import RandomForestClassifier
        from sklearn.preprocessing import StandardScaler
        
        # Données d'entraînement génériques (features variables selon domaine)
        # Exemple: score1, score2, score3, score4
        X_train = np.array([
            [0.8, 0.9, 0.7, 0.85],  # Approuvé
            [0.85, 0.8, 0.9, 0.75], # Approuvé
            [0.9, 0.7, 0.8, 0.9],   # Approuvé
            [0.75, 0.85, 0.8, 0.8], # Approuvé
            [0.8, 0.75, 0.85, 0.85],# Approuvé
            [0.3, 0.2, 0.4, 0.25],  # Refusé
            [0.2, 0.3, 0.25, 0.3],  # Refusé
            [0.35, 0.25, 0.3, 0.2], # Refusé
            [0.25, 0.4, 0.2, 0.35], # Refusé
            [0.4, 0.3, 0.35, 0.25], # Refusé
            [0.55, 0.5, 0.6, 0.5],  # Limite (Refusé)
            [0.65, 0.7, 0.6, 0.68], # Limite (Approuvé)
        ])
        
        y_train = np.array([1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1])
        
        # Entraînement du modèle
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X_train)
        
        model = RandomForestClassifier(
            n_estimators=100,
            max_depth=5,
            random_state=42
        )
        model.fit(X_scaled, y_train)
        
        # Sauvegarder le modèle et le scaler
        joblib.dump({'model': model, 'scaler': scaler}, model_path)
        
        return {'model': model, 'scaler': scaler}

# Charger le modèle général au démarrage
MODELS['general'] = load_or_create_model('general')

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'ML Decision Service - Multi-Domain',
        'models_loaded': list(MODELS.keys()),
        'domains_available': ['general', 'credit', 'medical', 'insurance', 'hr', 'legal', 'education'],
        'timestamp': datetime.now().isoformat()
    })

@app.route('/predict', methods=['POST'])
def predict():
    """
    Prédit une décision basée sur les données fournies (multi-domaine)
    
    Body JSON attendu:
    {
        "domain": "credit|medical|insurance|hr|legal|education|general",
        "features": {
            "feature1": value1,
            "feature2": value2,
            ...
        },
        "description": "Description de la demande",
        "metadata": {
            "key": "value"
        }
    }
    
    Exemples par domaine:
    
    CREDIT:
    {
        "domain": "credit",
        "features": {
            "revenuMensuel": 15000,
            "dettesActuelles": 2000,
            "age": 35,
            "ancienneteEmploi": 5
        },
        "description": "Demande de crédit de 50000 DH",
        "metadata": {"montantDemande": 50000}
    }
    
    MEDICAL:
    {
        "domain": "medical",
        "features": {
            "urgence": 0.8,
            "risque": 0.3,
            "disponibilite": 0.9,
            "priorite": 0.7
        },
        "description": "Chirurgie programmée",
        "metadata": {"patient": "ID123"}
    }
    
    INSURANCE:
    {
        "domain": "insurance",
        "features": {
            "risqueClient": 0.4,
            "historique": 0.8,
            "montantCouverture": 0.6,
            "age": 0.7
        },
        "description": "Assurance auto",
        "metadata": {"vehicule": "SUV"}
    }
    """
    try:
        data = request.get_json()
        
        # Validation des données
        if 'domain' not in data:
            data['domain'] = 'general'
        
        if 'features' not in data or not isinstance(data['features'], dict):
            return jsonify({'error': 'Champ "features" manquant ou invalide'}), 400
        
        if 'description' not in data:
            data['description'] = 'Décision automatique'
        
        domain = data['domain']
        features_dict = data['features']
        description = data['description']
        metadata = data.get('metadata', {})
        
        # Charger ou créer le modèle pour ce domaine
        if domain not in MODELS:
            MODELS[domain] = load_or_create_model(domain)
        
        model_components = MODELS[domain]
        model = model_components['model']
        scaler = model_components['scaler']
        
        # Convertir features dict en array (ordre alphabétique des clés)
        feature_names = sorted(features_dict.keys())
        features = np.array([[features_dict[key] for key in feature_names]])
        
        # Normalisation
        features_scaled = scaler.transform(features)
        
        # Prédiction
        prediction = model.predict(features_scaled)[0]
        probabilities = model.predict_proba(features_scaled)[0]
        
        # Score de confiance
        confidence_score = float(probabilities[prediction] * 100)
        
        # Décision
        decision_type = "APPROUVER" if prediction == 1 else "REJETER"
        
        # Analyse des facteurs de décision
        feature_importance = model.feature_importances_
        factors = []
        for i, name in enumerate(feature_names):
            if i < len(feature_importance):
                factors.append({
                    'name': name,
                    'value': features_dict[name],
                    'importance': float(feature_importance[i])
                })
        
        # Construction du contenu de la décision (adapté au domaine)
        if decision_type == "APPROUVER":
            contenu = f"{description} - APPROUVÉ"
            raison = generate_approval_reason(domain, features_dict, metadata)
        else:
            contenu = f"{description} - REFUSÉ"
            raison = generate_rejection_reason(domain, features_dict, metadata)
        
        # Construction du contexte
        contexte = f"Domaine: {domain}, "
        contexte += ", ".join([f"{k}: {v}" for k, v in features_dict.items()])
        if metadata:
            contexte += " | " + ", ".join([f"{k}: {v}" for k, v in metadata.items()])
        
        response = {
            'decision': decision_type,
            'domain': domain,
            'contenu': contenu,
            'contexte': contexte,
            'scoreConfiance': round(confidence_score, 2),
            'raison': raison,
            'probabilities': {
                'refuser': round(float(probabilities[0]) * 100, 2),
                'approuver': round(float(probabilities[1]) * 100, 2)
            },
            'factors': sorted(factors, key=lambda x: x['importance'], reverse=True),
            'metadata': metadata,
            'timestamp': datetime.now().isoformat()
        }
        
        return jsonify(response), 200
        
    except Exception as e:
        return jsonify({
            'error': 'Erreur lors de la prédiction',
            'details': str(e)
        }), 500

def generate_approval_reason(domain, features, metadata):
    """Génère une raison d'approbation adaptée au domaine"""
    reasons = {
        'credit': "Profil financier solide, capacité de remboursement suffisante",
        'medical': "Intervention justifiée, risques maîtrisés, ressources disponibles",
        'insurance': "Profil de risque acceptable, historique favorable",
        'hr': "Candidat qualifié, compétences correspondantes, expérience adéquate",
        'legal': "Dossier recevable, arguments solides, jurisprudence favorable",
        'education': "Profil académique satisfaisant, critères d'admission remplis",
        'general': "Critères d'évaluation satisfaits, décision favorable"
    }
    return reasons.get(domain, reasons['general'])

def generate_rejection_reason(domain, features, metadata):
    """Génère une raison de rejet adaptée au domaine"""
    reasons = {
        'credit': "Risque de défaut élevé, ratio dettes/revenus défavorable",
        'medical': "Risques médicaux trop élevés, ressources insuffisantes",
        'insurance': "Profil de risque trop élevé, critères non satisfaits",
        'hr': "Qualifications insuffisantes, expérience inadéquate",
        'legal': "Dossier incomplet, arguments insuffisants",
        'education': "Critères d'admission non satisfaits, dossier incomplet",
        'general': "Critères d'évaluation non satisfaits, décision défavorable"
    }
    return reasons.get(domain, reasons['general'])

@app.route('/train', methods=['POST'])
def retrain_model():
    """
    Réentraîne le modèle avec de nouvelles données
    
    Body JSON attendu:
    {
        "domain": "credit|medical|insurance|hr|legal|education|general",
        "data": [
            {
                "features": {"feature1": val1, "feature2": val2, ...},
                "approved": true
            },
            ...
        ]
    }
    """
    try:
        data = request.get_json()
        
        if 'domain' not in data:
            data['domain'] = 'general'
        
        if 'data' not in data or len(data['data']) == 0:
            return jsonify({'error': 'Aucune donnée fournie'}), 400
        
        domain = data['domain']
        training_data = data['data']
        
        from sklearn.ensemble import RandomForestClassifier
        from sklearn.preprocessing import StandardScaler
        
        # Préparer les données
        X_new = []
        y_new = []
        
        # Extraire feature names du premier exemple
        first_example = training_data[0]
        if 'features' not in first_example:
            return jsonify({'error': 'Format de données invalide'}), 400
        
        feature_names = sorted(first_example['features'].keys())
        
        for item in training_data:
            if 'features' not in item or 'approved' not in item:
                continue
            
            features = [item['features'].get(key, 0) for key in feature_names]
            X_new.append(features)
            y_new.append(1 if item['approved'] else 0)
        
        X_new = np.array(X_new)
        y_new = np.array(y_new)
        
        # Réentraînement
        new_scaler = StandardScaler()
        X_scaled = new_scaler.fit_transform(X_new)
        
        new_model = RandomForestClassifier(
            n_estimators=100,
            max_depth=5,
            random_state=42
        )
        new_model.fit(X_scaled, y_new)
        
        # Sauvegarder
        model_path = os.path.join(MODEL_DIR, f'{domain}_model.pkl')
        joblib.dump({'model': new_model, 'scaler': new_scaler}, model_path)
        
        # Recharger dans le dictionnaire
        MODELS[domain] = {'model': new_model, 'scaler': new_scaler}
        
        return jsonify({
            'success': True,
            'domain': domain,
            'message': f'Modèle {domain} réentraîné avec {len(y_new)} exemples',
            'features': feature_names,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        return jsonify({
            'error': 'Erreur lors du réentraînement',
            'details': str(e)
        }), 500

@app.route('/domains', methods=['GET'])
def list_domains():
    """Liste tous les domaines disponibles avec exemples"""
    domains_info = {
        'credit': {
            'name': 'Crédit Bancaire',
            'description': 'Décisions d\'octroi de crédit basées sur profil financier',
            'features_example': ['revenuMensuel', 'dettesActuelles', 'age', 'ancienneteEmploi'],
            'use_case': 'Banques, institutions financières'
        },
        'medical': {
            'name': 'Décisions Médicales',
            'description': 'Validation d\'interventions médicales et priorités',
            'features_example': ['urgence', 'risque', 'disponibilite', 'priorite'],
            'use_case': 'Hôpitaux, cliniques'
        },
        'insurance': {
            'name': 'Assurances',
            'description': 'Évaluation de risques et acceptation de polices',
            'features_example': ['risqueClient', 'historique', 'montantCouverture', 'age'],
            'use_case': 'Compagnies d\'assurance'
        },
        'hr': {
            'name': 'Ressources Humaines',
            'description': 'Décisions de recrutement et promotions',
            'features_example': ['experience', 'competences', 'formation', 'performance'],
            'use_case': 'Départements RH'
        },
        'legal': {
            'name': 'Juridique',
            'description': 'Évaluation de dossiers juridiques',
            'features_example': ['soliditeDossier', 'jurisprudence', 'preuves', 'complexite'],
            'use_case': 'Cabinets d\'avocats, tribunaux'
        },
        'education': {
            'name': 'Éducation',
            'description': 'Admissions et évaluations académiques',
            'features_example': ['moyenneGenerale', 'test', 'experience', 'motivation'],
            'use_case': 'Universités, écoles'
        },
        'general': {
            'name': 'Général',
            'description': 'Décisions génériques pour tout domaine',
            'features_example': ['critere1', 'critere2', 'critere3', 'critere4'],
            'use_case': 'Tout type de décision'
        }
    }
    
    return jsonify({
        'domains': domains_info,
        'models_loaded': list(MODELS.keys()),
        'total_domains': len(domains_info)
    })

if __name__ == '__main__':
    print("🤖 Démarrage du service ML de décision multi-domaine...")
    print(f"📊 Domaines disponibles: credit, medical, insurance, hr, legal, education, general")
    print(f"💾 Dossier des modèles: {MODEL_DIR}")
    print(f"🌐 API disponible sur: http://0.0.0.0:5000")
    print(f"📚 Documentation: GET /domains")
    app.run(host='0.0.0.0', port=5000, debug=True)
