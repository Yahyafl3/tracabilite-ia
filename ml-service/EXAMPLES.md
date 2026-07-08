# 🎯 Exemples d'Utilisation - ML Service Multi-Domaine

## 📋 Table des matières
1. [Crédit Bancaire](#1-crédit-bancaire)
2. [Décisions Médicales](#2-décisions-médicales)
3. [Assurances](#3-assurances)
4. [Ressources Humaines](#4-ressources-humaines)
5. [Juridique](#5-juridique)
6. [Éducation](#6-éducation)
7. [Général](#7-général)

---

## 1. 🏦 Crédit Bancaire

### Demande de crédit - APPROUVÉ
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "credit",
    "features": {
      "revenuMensuel": 15000,
      "dettesActuelles": 2000,
      "age": 35,
      "ancienneteEmploi": 5
    },
    "description": "Demande de crédit immobilier",
    "metadata": {
      "montantDemande": 500000,
      "duree": 20,
      "typeCredit": "immobilier"
    }
  }'
```

### Demande de crédit - REFUSÉ
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "credit",
    "features": {
      "revenuMensuel": 5000,
      "dettesActuelles": 25000,
      "age": 50,
      "ancienneteEmploi": 1
    },
    "description": "Demande de crédit personnel",
    "metadata": {
      "montantDemande": 100000,
      "typeCredit": "personnel"
    }
  }'
```

---

## 2. 🏥 Décisions Médicales

### Chirurgie programmée - APPROUVÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "medical",
    "features": {
      "urgence": 0.8,
      "risque": 0.3,
      "disponibilite": 0.9,
      "priorite": 0.85
    },
    "description": "Chirurgie cardiaque programmée",
    "metadata": {
      "patient": "PATIENT_001",
      "medecin": "Dr. Ahmed",
      "hopital": "CHU Mohammed VI"
    }
  }'
```

### Intervention non urgente - REFUSÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "medical",
    "features": {
      "urgence": 0.2,
      "risque": 0.7,
      "disponibilite": 0.3,
      "priorite": 0.4
    },
    "description": "Intervention esthétique",
    "metadata": {
      "patient": "PATIENT_002",
      "type": "chirurgie_esthetique"
    }
  }'
```

---

## 3. 🛡️ Assurances

### Assurance auto - APPROUVÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "insurance",
    "features": {
      "risqueClient": 0.3,
      "historique": 0.9,
      "montantCouverture": 0.6,
      "age": 0.8
    },
    "description": "Assurance auto tous risques",
    "metadata": {
      "vehicule": "Dacia Logan",
      "annee": 2020,
      "usage": "personnel"
    }
  }'
```

### Assurance à risque - REFUSÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "insurance",
    "features": {
      "risqueClient": 0.9,
      "historique": 0.2,
      "montantCouverture": 0.9,
      "age": 0.3
    },
    "description": "Assurance voiture de sport",
    "metadata": {
      "vehicule": "Ferrari",
      "accidents": 3,
      "usage": "course"
    }
  }'
```

---

## 4. 👥 Ressources Humaines

### Recrutement - CANDIDAT ACCEPTÉ
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "hr",
    "features": {
      "experience": 0.85,
      "competences": 0.9,
      "formation": 0.8,
      "performance": 0.88
    },
    "description": "Candidature Senior Developer",
    "metadata": {
      "candidat": "Mohammed Alami",
      "poste": "Senior Full Stack Developer",
      "salaireDemande": 25000
    }
  }'
```

### Recrutement - CANDIDAT REFUSÉ
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "hr",
    "features": {
      "experience": 0.2,
      "competences": 0.3,
      "formation": 0.4,
      "performance": 0.3
    },
    "description": "Candidature Junior",
    "metadata": {
      "candidat": "Candidat X",
      "poste": "Senior Position",
      "experience_annees": 0.5
    }
  }'
```

---

## 5. ⚖️ Juridique

### Dossier juridique - RECEVABLE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "legal",
    "features": {
      "soliditeDossier": 0.85,
      "jurisprudence": 0.8,
      "preuves": 0.9,
      "complexite": 0.6
    },
    "description": "Affaire de litige commercial",
    "metadata": {
      "type": "commercial",
      "montant": 500000,
      "avocat": "Me. Benali"
    }
  }'
```

### Dossier juridique - NON RECEVABLE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "legal",
    "features": {
      "soliditeDossier": 0.3,
      "jurisprudence": 0.2,
      "preuves": 0.25,
      "complexite": 0.9
    },
    "description": "Plainte sans fondement",
    "metadata": {
      "type": "civil",
      "preuves_manquantes": true
    }
  }'
```

---

## 6. 🎓 Éducation

### Admission universitaire - ACCEPTÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "education",
    "features": {
      "moyenneGenerale": 0.88,
      "test": 0.85,
      "experience": 0.75,
      "motivation": 0.9
    },
    "description": "Admission Master en IA",
    "metadata": {
      "etudiant": "Fatima Zahra",
      "universite": "ENSIAS",
      "programme": "Master IA"
    }
  }'
```

### Admission universitaire - REFUSÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "education",
    "features": {
      "moyenneGenerale": 0.5,
      "test": 0.4,
      "experience": 0.3,
      "motivation": 0.6
    },
    "description": "Admission Master",
    "metadata": {
      "etudiant": "Candidat Y",
      "note_insuffisante": true
    }
  }'
```

---

## 7. 🔧 Général (tout domaine)

### Décision générique - APPROUVÉE
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "general",
    "features": {
      "critere1": 0.85,
      "critere2": 0.8,
      "critere3": 0.9,
      "critere4": 0.75
    },
    "description": "Décision personnalisée",
    "metadata": {
      "contexte": "Projet spécifique"
    }
  }'
```

---

## 📡 Endpoints Additionnels

### Health Check
```bash
curl http://localhost:5000/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "ML Decision Service - Multi-Domain",
  "models_loaded": ["general"],
  "domains_available": ["general", "credit", "medical", "insurance", "hr", "legal", "education"],
  "timestamp": "2026-07-08T10:30:00"
}
```

### Liste des domaines
```bash
curl http://localhost:5000/domains
```

**Response:**
```json
{
  "domains": {
    "credit": {
      "name": "Crédit Bancaire",
      "description": "Décisions d'octroi de crédit...",
      "features_example": ["revenuMensuel", "dettesActuelles", ...],
      "use_case": "Banques, institutions financières"
    },
    ...
  },
  "models_loaded": ["general", "credit"],
  "total_domains": 7
}
```

### Réentraînement d'un modèle
```bash
curl -X POST http://localhost:5000/train \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "credit",
    "data": [
      {
        "features": {
          "revenuMensuel": 18000,
          "dettesActuelles": 3000,
          "age": 32,
          "ancienneteEmploi": 6
        },
        "approved": true
      },
      {
        "features": {
          "revenuMensuel": 7000,
          "dettesActuelles": 20000,
          "age": 48,
          "ancienneteEmploi": 2
        },
        "approved": false
      }
    ]
  }'
```

---

## 🎨 Format de Response Standard

Toutes les prédictions retournent le même format:

```json
{
  "decision": "APPROUVER" | "REJETER",
  "domain": "credit",
  "contenu": "Description - APPROUVÉ/REFUSÉ",
  "contexte": "Détails des features et metadata",
  "scoreConfiance": 87.5,
  "raison": "Explication adaptée au domaine",
  "probabilities": {
    "refuser": 12.5,
    "approuver": 87.5
  },
  "factors": [
    {
      "name": "feature_name",
      "value": 15000,
      "importance": 0.45
    }
  ],
  "metadata": {...},
  "timestamp": "2026-07-08T10:30:00"
}
```

---

## 💡 Notes

- **Features**: Peuvent être des valeurs absolues (15000) ou normalisées (0.0-1.0)
- **Domain**: Si omis, utilise "general" par défaut
- **Metadata**: Optionnel, utilisé pour contexte additionnel
- **Modèles**: Créés automatiquement au premier usage
- **Persistence**: Modèles sauvegardés dans dossier `/app/models/`

---

Made with ❤️ for Traçabilité IA Project
