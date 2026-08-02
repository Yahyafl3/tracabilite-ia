"""Normalise les datasets publics fournis et remplace datasets/{credit,medical,education}/."""
from __future__ import annotations

import hashlib
import json
import re
from datetime import datetime, timezone
from pathlib import Path

import pandas as pd

SRC = Path(r"c:\Users\yahya\Downloads\Datasets")
ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "datasets"
SEED = 42


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def prepare_credit() -> dict:
    src = SRC / "credit_analysis_dataset.csv"
    df = pd.read_csv(src)
    contrat_map = {
        "CDI": "CDI",
        "CDD": "CDD",
        "Fonctionnaire": "FONCTIONNAIRE",
        "Informel": "INFORMEL",
    }
    logement_map = {
        "Proprietaire": "PROPRIETAIRE",
        "Locataire": "LOCATAIRE",
        "Logement de Fonction": "LOGEMENT_DE_FONCTION",
    }
    out = pd.DataFrame(
        {
            "age": df["age"].astype(int),
            "duree_mois": df["duree_mois"].astype(int),
            "type_contrat": df["type_contrat"].map(contrat_map).fillna("CDI"),
            "statut_logement": df["statut_logement"].map(logement_map).fillna("LOCATAIRE"),
            "incident_paiement_bam": df["incident_paiement_bam"].astype(int),
            "montant_demande_mad": df["montant_demande_mad"].astype(float),
            "nouvelle_echeance_mad": df["nouvelle_echeance_mad"].astype(float),
            "revenu_mensuel_mad": df["revenu_mensuel_mad"].astype(float),
            # dataset en % (0-100) → ratio 0-1 pour le modèle
            "taux_endettement": (df["taux_endettement"].astype(float) / 100.0).clip(0, 1),
            # 1 = risque (non approuvé) pour aligner avec logique FAIBLE/MOYEN/ELEVE
            "risque_non_approbation": (1 - df["target_approved"].astype(int)),
        }
    )
    dest_dir = OUT / "credit"
    dest_dir.mkdir(parents=True, exist_ok=True)
    # remove old synthetic
    for old in dest_dir.glob("*.csv"):
        old.unlink()
    dest = dest_dir / "credit_analysis_dataset.csv"
    out.to_csv(dest, index=False)
    return {
        "rows": len(out),
        "path": str(dest.relative_to(ROOT)).replace("\\", "/"),
        "target_rate": float(out["risque_non_approbation"].mean()),
        "checksum_sha256": _sha256(dest),
        "source": str(src),
    }


def prepare_medical() -> dict:
    src = SRC / "medical_diabetes_european_dataset.csv"
    df = pd.read_csv(src)
    # niveau_risque exclu (fuite potentielle / quasi-label)
    out = pd.DataFrame(
        {
            "age": df["age"].astype(int),
            "grossesses": df["grossesses"].astype(int),
            "glycemie_mg_dl": df["glycemie_mg_dl"].astype(float),
            "pression_arterielle_mmhg": df["pression_arterielle_mmhg"].astype(float),
            "epaisseur_pli_cutane_mm": df["epaisseur_pli_cutane_mm"].astype(float),
            "insuline_micro_u_ml": df["insuline_micro_u_ml"].astype(float),
            "imc_kg_m2": df["imc_kg_m2"].astype(float),
            "risque_diabete": df["target_diabetes"].astype(int),
        }
    )
    dest_dir = OUT / "medical"
    dest_dir.mkdir(parents=True, exist_ok=True)
    for old in dest_dir.glob("*.csv"):
        old.unlink()
    dest = dest_dir / "medical_diabetes_european_dataset.csv"
    out.to_csv(dest, index=False)
    return {
        "rows": len(out),
        "path": str(dest.relative_to(ROOT)).replace("\\", "/"),
        "target_rate": float(out["risque_diabete"].mean()),
        "checksum_sha256": _sha256(dest),
        "source": str(src),
    }


def _norm_col(name: str) -> str:
    name = name.replace("\t", "").strip()
    name = name.lower()
    name = name.replace("'", "")
    name = re.sub(r"[^a-z0-9]+", "_", name)
    return name.strip("_")


def prepare_education() -> dict:
    src = SRC / "education_portugal_raw.csv"
    df = pd.read_csv(src, sep=";")
    df.columns = [_norm_col(c) for c in df.columns]
    # colonnes utiles (UCI Portugal — Predict students dropout)
    rename = {
        "age_at_enrollment": "age_inscription",
        "admission_grade": "note_admission",
        "previous_qualification_grade": "note_qualification_precedente",
        "curricular_units_1st_sem_approved": "unites_validees_s1",
        "curricular_units_1st_sem_grade": "moyenne_s1",
        "curricular_units_2nd_sem_approved": "unites_validees_s2",
        "curricular_units_2nd_sem_grade": "moyenne_s2",
        "unemployment_rate": "taux_chomage",
        "inflation_rate": "taux_inflation",
        "gdp": "pib",
        "gender": "sexe",
        "scholarship_holder": "boursier",
        "tuition_fees_up_to_date": "frais_a_jour",
        "debtor": "debiteur",
        "displaced": "deplace",
        "international": "international",
        "target": "target",
    }
    missing = [k for k in rename if k not in df.columns]
    if missing:
        raise KeyError(f"Colonnes education manquantes après normalisation: {missing}. cols={list(df.columns)}")

    out = pd.DataFrame(
        {
            "age_inscription": df["age_at_enrollment"].astype(int),
            "note_admission": df["admission_grade"].astype(float),
            "note_qualification_precedente": df["previous_qualification_grade"].astype(float),
            "unites_validees_s1": df["curricular_units_1st_sem_approved"].astype(int),
            "moyenne_s1": df["curricular_units_1st_sem_grade"].astype(float),
            "unites_validees_s2": df["curricular_units_2nd_sem_approved"].astype(int),
            "moyenne_s2": df["curricular_units_2nd_sem_grade"].astype(float),
            "taux_chomage": df["unemployment_rate"].astype(float),
            "taux_inflation": df["inflation_rate"].astype(float),
            "pib": df["gdp"].astype(float),
            "sexe": df["gender"].map({0: "FEMME", 1: "HOMME", "0": "FEMME", "1": "HOMME"}).fillna("HOMME"),
            "boursier": df["scholarship_holder"].map({0: "NON", 1: "OUI", "0": "NON", "1": "OUI"}).fillna("NON"),
            "frais_a_jour": df["tuition_fees_up_to_date"].map({0: "NON", 1: "OUI", "0": "NON", "1": "OUI"}).fillna("OUI"),
            "debiteur": df["debtor"].map({0: "NON", 1: "OUI", "0": "NON", "1": "OUI"}).fillna("NON"),
            "deplace": df["displaced"].map({0: "NON", 1: "OUI", "0": "NON", "1": "OUI"}).fillna("NON"),
            "international": df["international"].map({0: "NON", 1: "OUI", "0": "NON", "1": "OUI"}).fillna("NON"),
            "decrochage": df["target"].astype(str).str.strip().eq("Dropout").astype(int),
        }
    )
    dest_dir = OUT / "education"
    dest_dir.mkdir(parents=True, exist_ok=True)
    for old in dest_dir.glob("*.csv"):
        old.unlink()
    dest = dest_dir / "education_portugal_dropout.csv"
    out.to_csv(dest, index=False)
    return {
        "rows": len(out),
        "path": str(dest.relative_to(ROOT)).replace("\\", "/"),
        "target_rate": float(out["decrochage"].mean()),
        "checksum_sha256": _sha256(dest),
        "source": str(src),
    }


def main() -> int:
    summary = {
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "seed": SEED,
        "version": "2.0.0-public",
        "governanceStatus": "DEMO_PUBLIC_DATASET",
        "note": "Datasets publics / recherche — pas des données métier confidentielles. Modèles non VALIDATED_PRODUCTION.",
        "credit": prepare_credit(),
        "medical": prepare_medical(),
        "education": prepare_education(),
    }
    (OUT / "generation_summary.json").write_text(
        json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    print(json.dumps(summary, indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
