"""Schémas et validation des features par domaine métier (datasets publics v2)."""
from __future__ import annotations

from typing import Any

CREDIT_NUMERIC = {
    "age": ("age", 18, 100),
    "dureeMois": ("duree_mois", 1, None),
    "incidentPaiementBam": ("incident_paiement_bam", 0, None),
    "montantDemandeMad": ("montant_demande_mad", 1, None),
    "nouvelleEcheanceMad": ("nouvelle_echeance_mad", 0, None),
    "revenuMensuelMad": ("revenu_mensuel_mad", 1, None),
    "tauxEndettement": ("taux_endettement", 0, 1),
}
CREDIT_CATEGORICAL = {
    "typeContrat": (
        "type_contrat",
        {"CDI", "CDD", "FONCTIONNAIRE", "INFORMEL"},
    ),
    "statutLogement": (
        "statut_logement",
        {"PROPRIETAIRE", "LOCATAIRE", "LOGEMENT_DE_FONCTION"},
    ),
}

MEDICAL_NUMERIC = {
    "age": ("age", 1, 120),
    "grossesses": ("grossesses", 0, None),
    "glycemieMgDl": ("glycemie_mg_dl", 0, None),
    "pressionArterielleMmhg": ("pression_arterielle_mmhg", 0, None),
    "epaisseurPliCutaneMm": ("epaisseur_pli_cutane_mm", 0, None),
    "insulineMicroUMl": ("insuline_micro_u_ml", 0, None),
    "imcKgM2": ("imc_kg_m2", 10, 80),
}
MEDICAL_CATEGORICAL: dict[str, tuple[str, set[str] | None]] = {}

EDUCATION_NUMERIC = {
    "ageInscription": ("age_inscription", 15, 80),
    "noteAdmission": ("note_admission", 0, 200),
    "noteQualificationPrecedente": ("note_qualification_precedente", 0, 200),
    "unitesValideesS1": ("unites_validees_s1", 0, None),
    "moyenneS1": ("moyenne_s1", 0, 20),
    "unitesValideesS2": ("unites_validees_s2", 0, None),
    "moyenneS2": ("moyenne_s2", 0, 20),
    "tauxChomage": ("taux_chomage", -50, 50),
    "tauxInflation": ("taux_inflation", -50, 50),
    "pib": ("pib", -50, 50),
}
EDUCATION_CATEGORICAL = {
    "sexe": ("sexe", {"HOMME", "FEMME"}),
    "boursier": ("boursier", {"OUI", "NON"}),
    "fraisAJour": ("frais_a_jour", {"OUI", "NON"}),
    "debiteur": ("debiteur", {"OUI", "NON"}),
    "deplace": ("deplace", {"OUI", "NON"}),
    "international": ("international", {"OUI", "NON"}),
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

FLOAT_COLS = {
    "taux_endettement",
    "montant_demande_mad",
    "nouvelle_echeance_mad",
    "revenu_mensuel_mad",
    "glycemie_mg_dl",
    "pression_arterielle_mmhg",
    "epaisseur_pli_cutane_mm",
    "insuline_micro_u_ml",
    "imc_kg_m2",
    "note_admission",
    "note_qualification_precedente",
    "moyenne_s1",
    "moyenne_s2",
    "taux_chomage",
    "taux_inflation",
    "pib",
}


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
        out[col] = num if col in FLOAT_COLS else int(num)

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
