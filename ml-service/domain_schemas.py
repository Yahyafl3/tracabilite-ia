"""Schémas et validation des features par domaine métier."""
from __future__ import annotations

from typing import Any

CREDIT_NUMERIC = {
    "ageDemandeur": ("age_demandeur", 18, 80),
    "revenuMensuelMad": ("revenu_mensuel_mad", 1, None),
    "chargesMensuellesMad": ("charges_mensuelles_mad", 0, None),
    "montantDemandeMad": ("montant_demande_mad", 1, None),
    "dureeCreditMois": ("duree_credit_mois", 1, None),
    "ancienneteProfessionnelleAnnees": ("anciennete_professionnelle_annees", 0, None),
    "creditsExistants": ("credits_existants", 0, None),
    "incidentsPaiement24Mois": ("incidents_paiement_24_mois", 0, None),
    "ratioEndettement": ("ratio_endettement", 0, 1),
}
CREDIT_CATEGORICAL = {
    "secteurActivite": (
        "secteur_activite",
        {"SERVICES", "INDUSTRIE", "COMMERCE", "TECH", "AGRICULTURE"},
    ),
    "region": ("region", None),  # validated loosely
    "statutProfessionnel": (
        "statut_professionnel",
        {"SALARIE_CDI", "SALARIE_CDD", "FONCTIONNAIRE", "INDEPENDANT", "RETRAITE"},
    ),
    "typeGarantie": (
        "type_garantie",
        {"AUCUNE", "HYPOTHEQUE", "CAUTION", "NANTISSEMENT"},
    ),
    "typeCredit": (
        "type_credit",
        {"CONSOMMATION", "IMMOBILIER", "PROFESSIONNEL", "AUTO"},
    ),
}

MEDICAL_NUMERIC = {
    "age": ("age", 1, 120),
    "imc": ("imc", 10, 60),
    "glycemie": ("glycemie", 0.1, None),
}
MEDICAL_CATEGORICAL = {
    "region": ("region", None),
    "sexe": ("sexe", {"HOMME", "FEMME"}),
    "niveauActivitePhysique": (
        "niveau_activite_physique",
        {"SEDENTAIRE", "LEGER", "MODERE", "INTENSE"},
    ),
    "antecedentsFamiliauxDiabete": ("antecedents_familiaux_diabete", {"OUI", "NON"}),
    "hypertension": ("hypertension", {"OUI", "NON"}),
    "polyurie": ("polyurie", {"OUI", "NON"}),
    "polydipsie": ("polydipsie", {"OUI", "NON"}),
    "pertePoidsSoudaine": ("perte_poids_soudaine", {"OUI", "NON"}),
    "faiblesse": ("faiblesse", {"OUI", "NON"}),
    "obesite": ("obesite", {"OUI", "NON"}),
    "suiviMedical": ("suivi_medical", {"OUI", "NON"}),
}

EDUCATION_NUMERIC = {
    "moyenneSemestre1": ("moyenne_semestre_1", 0, 20),
    "moyenneSemestre2": ("moyenne_semestre_2", 0, 20),
    "tauxAbsence": ("taux_absence", 0, 100),
    "modulesNonValides": ("modules_non_valides", 0, None),
    "distanceLogementKm": ("distance_logement_km", 0, None),
}
EDUCATION_CATEGORICAL = {
    "region": ("region", None),
    "typeEtablissement": (
        "type_etablissement",
        {
            "UNIVERSITE_PUBLIQUE",
            "UNIVERSITE_PRIVEE",
            "ECOLE_INGENIEUR",
            "FACULTE",
            "IUT",
        },
    ),
    "filiere": (
        "filiere",
        {
            "SCIENCES",
            "LETTRES",
            "DROIT",
            "ECONOMIE",
            "INGENIERIE",
            "MEDECINE",
            "INFORMATIQUE",
        },
    ),
    "niveauEtude": ("niveau_etude", {"L1", "L2", "L3", "M1", "M2"}),
    "participation": ("participation", {"FAIBLE", "MOYENNE", "ELEVEE"}),
    "bourse": ("bourse", {"OUI", "NON"}),
    "accesInternet": ("acces_internet", {"OUI", "NON"}),
    "activiteProfessionnelle": ("activite_professionnelle", {"OUI", "NON"}),
    "historiqueRedoublement": ("historique_redoublement", {"OUI", "NON"}),
    "situationAcademique": (
        "situation_academique",
        {"NORMALE", "DIFFICULTE", "REDOUBLEMENT", "REORIENTATION"},
    ),
}

DOMAIN_SCHEMAS = {
    "CREDIT": {
        "numeric": CREDIT_NUMERIC,
        "categorical": CREDIT_CATEGORICAL,
        "risk_labels": ("FAIBLE", "MOYEN", "ELEVE"),
        "prediction_prefix": "RISQUE_",
    },
    "MEDICAL": {
        "numeric": MEDICAL_NUMERIC,
        "categorical": MEDICAL_CATEGORICAL,
        "risk_labels": ("FAIBLE", "MODERE", "ELEVE"),
        "prediction_prefix": "RISQUE_",
    },
    "EDUCATION": {
        "numeric": EDUCATION_NUMERIC,
        "categorical": EDUCATION_CATEGORICAL,
        "risk_labels": ("FAIBLE", "MOYEN", "ELEVE"),
        "prediction_prefix": "RISQUE_",
    },
}


def _camel_to_snake_payload(payload: dict[str, Any]) -> dict[str, Any]:
    """Accepte camelCase API ou snake_case dataset."""
    return dict(payload)


def normalize_domain_features(domain: str, payload: dict[str, Any]) -> dict[str, Any]:
    domain = domain.upper()
    if domain not in DOMAIN_SCHEMAS:
        raise ValueError(f"Domaine inconnu: {domain}")

    raw = payload.get("features") if isinstance(payload.get("features"), dict) else payload
    schema = DOMAIN_SCHEMAS[domain]
    out: dict[str, Any] = {}
    missing: list[str] = []

    for api_key, (col, vmin, vmax) in schema["numeric"].items():
        val = raw.get(api_key, raw.get(col))
        if val is None:
            missing.append(api_key)
            continue
        try:
            num = float(val)
        except (TypeError, ValueError) as exc:
            raise ValueError(f"Valeur numérique invalide pour {api_key}") from exc
        if vmin is not None and num < vmin:
            raise ValueError(f"{api_key} doit être >= {vmin}")
        if vmax is not None and num > vmax:
            raise ValueError(f"{api_key} doit être <= {vmax}")
        out[col] = int(num) if col not in {
            "ratio_endettement",
            "imc",
            "glycemie",
            "moyenne_semestre_1",
            "moyenne_semestre_2",
            "taux_absence",
            "distance_logement_km",
        } else num

    for api_key, (col, allowed) in schema["categorical"].items():
        val = raw.get(api_key, raw.get(col))
        if val is None:
            missing.append(api_key)
            continue
        s = str(val).strip().upper()
        if allowed is not None and s not in allowed:
            raise ValueError(f"{api_key} invalide. Valeurs autorisées: {sorted(allowed)}")
        out[col] = s if allowed is not None else str(val).strip()

    if missing:
        raise ValueError(f"Features manquantes: {', '.join(missing)}")

    return out


def probability_to_risk(domain: str, probability: float) -> str:
    labels = DOMAIN_SCHEMAS[domain.upper()]["risk_labels"]
    if probability < 0.33:
        return labels[0]
    if probability < 0.66:
        return labels[1]
    return labels[2]
