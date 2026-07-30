#!/usr/bin/env python3
"""
Génération de datasets synthétiques contextualisés au Maroc.

IMPORTANT — Données synthétiques uniquement.
Ces datasets servent au développement, aux tests et à la démonstration.
Ils ne constituent PAS des données réelles de banques, d'établissements
de santé ou d'universités marocaines. Aucun nom, CIN, email ou téléphone réel.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from datetime import date, datetime, timezone
from pathlib import Path

import numpy as np
import pandas as pd

ROOT = Path(__file__).resolve().parents[2]
DATASETS_DIR = ROOT / "datasets"
DEFAULT_SEED = 42
DATASET_VERSION = "1.0.0"
GENERATION_DATE = date.today().isoformat()

REGIONS_MAROC = [
    "Casablanca-Settat",
    "Rabat-Salé-Kénitra",
    "Marrakech-Safi",
    "Fès-Meknès",
    "Tanger-Tétouan-Al Hoceïma",
    "Souss-Massa",
    "Oriental",
    "Béni Mellal-Khénifra",
    "Drâa-Tafilalet",
    "Guelmim-Oued Noun",
    "Laâyoune-Sakia El Hamra",
    "Dakhla-Oued Ed-Dahab",
]

CREDIT_SECTORS = ["SERVICES", "INDUSTRIE", "COMMERCE", "TECH", "AGRICULTURE"]
STATUTS_PRO = ["SALARIE_CDI", "SALARIE_CDD", "FONCTIONNAIRE", "INDEPENDANT", "RETRAITE"]
TYPES_GARANTIE = ["AUCUNE", "HYPOTHEQUE", "CAUTION", "NANTISSEMENT"]
TYPES_CREDIT = ["CONSOMMATION", "IMMOBILIER", "PROFESSIONNEL", "AUTO"]

SEXES = ["HOMME", "FEMME"]
NIVEAUX_ACTIVITE = ["SEDENTAIRE", "LEGER", "MODERE", "INTENSE"]
BOOL_CAT = ["OUI", "NON"]

TYPES_ETAB = ["UNIVERSITE_PUBLIQUE", "UNIVERSITE_PRIVEE", "ECOLE_INGENIEUR", "FACULTE", "IUT"]
FILIERES = ["SCIENCES", "LETTRES", "DROIT", "ECONOMIE", "INGENIERIE", "MEDECINE", "INFORMATIQUE"]
NIVEAUX_ETUDE = ["L1", "L2", "L3", "M1", "M2"]
PARTICIPATION = ["FAIBLE", "MOYENNE", "ELEVEE"]
SITUATIONS_ACAD = ["NORMALE", "DIFFICULTE", "REDOUBLEMENT", "REORIENTATION"]


def _sigmoid(x: np.ndarray) -> np.ndarray:
    return 1.0 / (1.0 + np.exp(-np.clip(x, -20, 20)))


def _dedupe(df: pd.DataFrame, cols: list[str], name: str) -> pd.DataFrame:
    before = len(df)
    out = df.drop_duplicates(subset=cols).reset_index(drop=True)
    removed = before - len(out)
    if removed:
        print(f"[{name}] {removed} doublons retirés")
    return out


def _write_dataset_card(
    path: Path,
    *,
    name: str,
    domain: str,
    objective: str,
    n_rows: int,
    columns: dict[str, str],
    target: str,
    method: str,
    rules: list[str],
    sources: list[str],
    limits: list[str],
    biases: list[str],
    seed: int,
    version: str,
) -> None:
    lines = [
        f"# Dataset Card — {name}",
        "",
        "> **Données synthétiques.** Ce fichier ne contient aucune donnée réelle",
        "> de banques, d'hôpitaux ou d'universités marocaines.",
        "",
        f"- **Nom** : `{name}`",
        f"- **Objectif** : {objective}",
        f"- **Domaine** : {domain}",
        f"- **Nombre de lignes** : {n_rows}",
        f"- **Colonne cible** : `{target}` (binaire 0/1)",
        f"- **Méthode de génération** : {method}",
        f"- **Date de génération** : {GENERATION_DATE}",
        f"- **Seed** : `{seed}`",
        f"- **Version** : `{version}`",
        "",
        "## Colonnes",
        "",
        "| Colonne | Type |",
        "|---------|------|",
    ]
    for col, typ in columns.items():
        lines.append(f"| `{col}` | {typ} |")
    lines.extend(
        [
            "",
            "## Règles probabilistes",
            "",
        ]
    )
    for r in rules:
        lines.append(f"- {r}")
    lines.extend(["", "## Sources statistiques générales (contexte)", ""])
    for s in sources:
        lines.append(f"- {s}")
    lines.extend(["", "## Limites", ""])
    for lim in limits:
        lines.append(f"- {lim}")
    lines.extend(["", "## Biais possibles", ""])
    for b in biases:
        lines.append(f"- {b}")
    lines.extend(
        [
            "",
            "## Usages autorisés",
            "",
            "- Développement et tests de la plateforme Traçabilité IA",
            "- Démonstration pédagogique d'explicabilité et de validation humaine",
            "- Benchmarks internes de pipelines ML",
            "",
            "## Usages interdits",
            "",
            "- Présenter ces données comme réelles",
            "- Décisions opérationnelles bancaires, médicales ou pédagogiques en production",
            "- Entraînement de modèles déployés sans réentraînement sur données réelles anonymisées",
            "- Identification de personnes (aucune PII réelle n'est présente)",
            "",
        ]
    )
    path.write_text("\n".join(lines), encoding="utf-8")


def generate_credit(n: int, rng: np.random.Generator) -> pd.DataFrame:
    secteur = rng.choice(CREDIT_SECTORS, size=n, p=[0.28, 0.18, 0.25, 0.17, 0.12])
    region = rng.choice(REGIONS_MAROC, size=n)
    age = rng.integers(18, 81, size=n)
    statut = rng.choice(STATUTS_PRO, size=n, p=[0.35, 0.15, 0.20, 0.22, 0.08])
    revenu = np.round(rng.lognormal(mean=9.0, sigma=0.45, size=n)).clip(2500, 80000)
    charges = np.round(revenu * rng.uniform(0.15, 0.65, size=n), 0)
    montant = np.round(rng.lognormal(mean=10.5, sigma=0.7, size=n)).clip(5000, 2_000_000)
    duree = rng.choice([12, 24, 36, 48, 60, 84, 120], size=n)
    anciennete = np.clip(age - 18 - rng.integers(0, 10, size=n), 0, 45)
    credits_existants = rng.integers(0, 6, size=n)
    incidents = rng.choice([0, 1, 2, 3, 4, 5], size=n, p=[0.55, 0.20, 0.12, 0.07, 0.04, 0.02])
    ratio = np.round(np.clip(charges / np.maximum(revenu, 1) + rng.normal(0, 0.05, n), 0, 1), 3)
    garantie = rng.choice(TYPES_GARANTIE, size=n, p=[0.35, 0.25, 0.30, 0.10])
    type_credit = rng.choice(TYPES_CREDIT, size=n, p=[0.40, 0.25, 0.25, 0.10])

    # Score de risque multi-facteurs + bruit (pas de règle unique déterministe)
    logit = (
        -1.2
        + 1.8 * ratio
        + 0.35 * incidents
        + 0.15 * credits_existants
        + 0.0000008 * montant
        - 0.00004 * revenu
        - 0.02 * anciennete
        + np.where(np.isin(statut, ["INDEPENDANT", "SALARIE_CDD"]), 0.35, 0.0)
        + np.where(garantie == "AUCUNE", 0.25, -0.15)
        + np.where(type_credit == "CONSOMMATION", 0.2, 0.0)
        + np.where(age < 25, 0.2, 0.0)
        + np.where(age > 65, 0.15, 0.0)
        + rng.normal(0, 0.55, size=n)
    )
    proba = _sigmoid(logit)
    defaut = (rng.random(n) < proba).astype(int)

    df = pd.DataFrame(
        {
            "secteur_activite": secteur,
            "region": region,
            "age_demandeur": age,
            "statut_professionnel": statut,
            "revenu_mensuel_mad": revenu.astype(int),
            "charges_mensuelles_mad": charges.astype(int),
            "montant_demande_mad": montant.astype(int),
            "duree_credit_mois": duree,
            "anciennete_professionnelle_annees": anciennete,
            "credits_existants": credits_existants,
            "incidents_paiement_24_mois": incidents,
            "ratio_endettement": ratio,
            "type_garantie": garantie,
            "type_credit": type_credit,
            "defaut_paiement": defaut,
        }
    )
    df = _dedupe(
        df,
        [
            "secteur_activite",
            "region",
            "age_demandeur",
            "revenu_mensuel_mad",
            "montant_demande_mad",
            "ratio_endettement",
            "incidents_paiement_24_mois",
        ],
        "credit",
    )
    assert df["age_demandeur"].between(18, 80).all()
    assert (df["revenu_mensuel_mad"] > 0).all()
    assert (df["charges_mensuelles_mad"] >= 0).all()
    assert df["ratio_endettement"].between(0, 1).all()
    return df


def generate_medical(n: int, rng: np.random.Generator) -> pd.DataFrame:
    region = rng.choice(REGIONS_MAROC, size=n)
    age = rng.integers(18, 90, size=n)
    sexe = rng.choice(SEXES, size=n)
    imc = np.round(rng.normal(26.5, 4.5, size=n).clip(16, 48), 1)
    activite = rng.choice(NIVEAUX_ACTIVITE, size=n, p=[0.30, 0.35, 0.25, 0.10])
    antecedents = rng.choice(BOOL_CAT, size=n, p=[0.35, 0.65])
    hypertension = rng.choice(BOOL_CAT, size=n, p=[0.28, 0.72])
    glycemie = np.round(rng.normal(1.05, 0.35, size=n).clip(0.6, 3.5), 2)
    polyurie = rng.choice(BOOL_CAT, size=n, p=[0.22, 0.78])
    polydipsie = rng.choice(BOOL_CAT, size=n, p=[0.20, 0.80])
    perte_poids = rng.choice(BOOL_CAT, size=n, p=[0.15, 0.85])
    faiblesse = rng.choice(BOOL_CAT, size=n, p=[0.25, 0.75])
    obesite = np.where(imc >= 30, "OUI", "NON")
    suivi = rng.choice(BOOL_CAT, size=n, p=[0.45, 0.55])

    logit = (
        -2.5
        + 0.035 * (age - 40)
        + 0.12 * (imc - 25)
        + 1.6 * (glycemie - 1.0)
        + np.where(antecedents == "OUI", 0.7, 0.0)
        + np.where(hypertension == "OUI", 0.45, 0.0)
        + np.where(polyurie == "OUI", 0.55, 0.0)
        + np.where(polydipsie == "OUI", 0.5, 0.0)
        + np.where(perte_poids == "OUI", 0.35, 0.0)
        + np.where(faiblesse == "OUI", 0.25, 0.0)
        + np.where(obesite == "OUI", 0.4, 0.0)
        + np.where(activite == "SEDENTAIRE", 0.35, 0.0)
        + np.where(activite == "INTENSE", -0.25, 0.0)
        + np.where(suivi == "NON", 0.2, -0.1)
        + rng.normal(0, 0.6, size=n)
    )
    risque = (rng.random(n) < _sigmoid(logit)).astype(int)

    df = pd.DataFrame(
        {
            "region": region,
            "age": age,
            "sexe": sexe,
            "imc": imc,
            "niveau_activite_physique": activite,
            "antecedents_familiaux_diabete": antecedents,
            "hypertension": hypertension,
            "glycemie": glycemie,
            "polyurie": polyurie,
            "polydipsie": polydipsie,
            "perte_poids_soudaine": perte_poids,
            "faiblesse": faiblesse,
            "obesite": obesite,
            "suivi_medical": suivi,
            "risque_diabete": risque,
        }
    )
    df = _dedupe(df, ["region", "age", "sexe", "imc", "glycemie", "hypertension"], "medical")
    assert df["age"].between(18, 90).all()
    assert df["imc"].between(15, 50).all()
    assert (df["glycemie"] > 0).all()
    return df


def generate_education(n: int, rng: np.random.Generator) -> pd.DataFrame:
    region = rng.choice(REGIONS_MAROC, size=n)
    etablissement = rng.choice(TYPES_ETAB, size=n)
    filiere = rng.choice(FILIERES, size=n)
    niveau = rng.choice(NIVEAUX_ETUDE, size=n, p=[0.25, 0.22, 0.20, 0.18, 0.15])
    moy1 = np.round(rng.normal(11.5, 3.0, size=n).clip(0, 20), 2)
    moy2 = np.round(np.clip(moy1 + rng.normal(0, 1.5, size=n), 0, 20), 2)
    absence = np.round(rng.beta(2, 8, size=n) * 100, 1)
    modules_nv = rng.integers(0, 8, size=n)
    participation = rng.choice(PARTICIPATION, size=n, p=[0.25, 0.50, 0.25])
    bourse = rng.choice(BOOL_CAT, size=n, p=[0.40, 0.60])
    distance = np.round(rng.exponential(12, size=n).clip(0, 200), 1)
    internet = rng.choice(BOOL_CAT, size=n, p=[0.75, 0.25])
    activite_pro = rng.choice(BOOL_CAT, size=n, p=[0.30, 0.70])
    redoublement = rng.choice(BOOL_CAT, size=n, p=[0.18, 0.82])
    situation = rng.choice(SITUATIONS_ACAD, size=n, p=[0.55, 0.25, 0.12, 0.08])

    logit = (
        -1.8
        + 0.045 * (absence - 20)
        + 0.35 * modules_nv
        - 0.18 * (moy1 - 10)
        - 0.18 * (moy2 - 10)
        + np.where(participation == "FAIBLE", 0.55, 0.0)
        + np.where(participation == "ELEVEE", -0.35, 0.0)
        + np.where(internet == "NON", 0.4, 0.0)
        + np.where(activite_pro == "OUI", 0.3, 0.0)
        + np.where(redoublement == "OUI", 0.5, 0.0)
        + np.where(situation == "DIFFICULTE", 0.45, 0.0)
        + np.where(situation == "REDOUBLEMENT", 0.55, 0.0)
        + np.where(bourse == "NON", 0.15, -0.1)
        + 0.008 * distance
        + rng.normal(0, 0.55, size=n)
    )
    decrochage = (rng.random(n) < _sigmoid(logit)).astype(int)

    df = pd.DataFrame(
        {
            "region": region,
            "type_etablissement": etablissement,
            "filiere": filiere,
            "niveau_etude": niveau,
            "moyenne_semestre_1": moy1,
            "moyenne_semestre_2": moy2,
            "taux_absence": absence,
            "modules_non_valides": modules_nv,
            "participation": participation,
            "bourse": bourse,
            "distance_logement_km": distance,
            "acces_internet": internet,
            "activite_professionnelle": activite_pro,
            "historique_redoublement": redoublement,
            "situation_academique": situation,
            "decrochage": decrochage,
        }
    )
    df = _dedupe(
        df,
        ["region", "filiere", "niveau_etude", "moyenne_semestre_1", "taux_absence", "modules_non_valides"],
        "education",
    )
    assert df["moyenne_semestre_1"].between(0, 20).all()
    assert df["moyenne_semestre_2"].between(0, 20).all()
    assert df["taux_absence"].between(0, 100).all()
    return df


def _stats(df: pd.DataFrame, target: str) -> dict:
    vc = df[target].value_counts().to_dict()
    return {
        "rows": len(df),
        "columns": list(df.columns),
        "target_distribution": {str(k): int(v) for k, v in vc.items()},
        "target_rate": round(float(df[target].mean()), 4),
        "checksum_sha256": hashlib.sha256(
            pd.util.hash_pandas_object(df, index=True).values.tobytes()
        ).hexdigest(),
    }


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Génère les datasets synthétiques marocains.")
    parser.add_argument("--seed", type=int, default=DEFAULT_SEED)
    parser.add_argument("--credit-rows", type=int, default=10_000)
    parser.add_argument("--medical-rows", type=int, default=8_000)
    parser.add_argument("--education-rows", type=int, default=10_000)
    parser.add_argument("--output-dir", type=Path, default=DATASETS_DIR)
    args = parser.parse_args(argv)

    rng = np.random.default_rng(args.seed)
    out = args.output_dir
    (out / "credit").mkdir(parents=True, exist_ok=True)
    (out / "medical").mkdir(parents=True, exist_ok=True)
    (out / "education").mkdir(parents=True, exist_ok=True)

    print(f"[generate] seed={args.seed} version={DATASET_VERSION}")

    credit = generate_credit(args.credit_rows, rng)
    credit_path = out / "credit" / "credit_maroc_synthetic.csv"
    credit.to_csv(credit_path, index=False)
    credit_stats = _stats(credit, "defaut_paiement")
    _write_dataset_card(
        out / "credit" / "DATASET_CARD.md",
        name="credit_maroc_synthetic",
        domain="CREDIT",
        objective="Évaluer le risque de défaut de paiement (contexte marocain synthétique).",
        n_rows=len(credit),
        columns={c: str(credit[c].dtype) for c in credit.columns},
        target="defaut_paiement",
        method="Échantillonnage probabiliste multi-facteurs (logit + bruit gaussien).",
        rules=[
            "Probabilité de défaut basée sur ratio d'endettement, incidents, revenus, garanties, âge, statut.",
            "Bruit aléatoire contrôlé (σ≈0.55) pour éviter une règle unique déterministe.",
            "Montants en MAD ; régions administratives marocaines.",
        ],
        sources=[
            "Distributions inspirées de statistiques publiques générales (emploi, revenus, régions).",
            "Aucune source de données bancaires réelles n'a été utilisée.",
        ],
        limits=[
            "Synthétique — ne reflète pas un portefeuille bancaire réel.",
            "Ne doit pas être présenté comme un modèle bancaire officiel marocain.",
        ],
        biases=[
            "Corrélations artificielles entre secteurs et risque.",
            "Sous-représentation possible de certaines régions.",
        ],
        seed=args.seed,
        version=f"credit-maroc-synthetic-v{DATASET_VERSION}",
    )
    print(f"[credit] {credit_path} -> {credit_stats}")

    medical = generate_medical(args.medical_rows, rng)
    medical_path = out / "medical" / "medical_diabetes_maroc_synthetic.csv"
    medical.to_csv(medical_path, index=False)
    medical_stats = _stats(medical, "risque_diabete")
    _write_dataset_card(
        out / "medical" / "DATASET_CARD.md",
        name="medical_diabetes_maroc_synthetic",
        domain="MEDICAL",
        objective="Estimation indicative du risque de diabète (aide à la décision, non diagnostique).",
        n_rows=len(medical),
        columns={c: str(medical[c].dtype) for c in medical.columns},
        target="risque_diabete",
        method="Échantillonnage probabiliste multi-facteurs (IMC, glycémie, symptômes, antécédents).",
        rules=[
            "Probabilité basée sur âge, IMC, glycémie, symptômes classiques et antécédents.",
            "Bruit aléatoire pour éviter une cible déterminée par une seule règle.",
            "Aucun identifiant personnel ; données fictives.",
        ],
        sources=[
            "Facteurs de risque diabète documentés dans la littérature générale.",
            "Aucune donnée hospitalière réelle marocaine.",
        ],
        limits=[
            "Ne remplace pas un diagnostic médical.",
            "Pas de biomarqueurs de laboratoire réels.",
        ],
        biases=[
            "Symptômes auto-déclarés simulés.",
            "Corrélation IMC/obésité structurelle.",
        ],
        seed=args.seed,
        version=f"medical-diabetes-maroc-synthetic-v{DATASET_VERSION}",
    )
    print(f"[medical] {medical_path} -> {medical_stats}")

    education = generate_education(args.education_rows, rng)
    education_path = out / "education" / "students_maroc_dropout_synthetic.csv"
    education.to_csv(education_path, index=False)
    education_stats = _stats(education, "decrochage")
    _write_dataset_card(
        out / "education" / "DATASET_CARD.md",
        name="students_maroc_dropout_synthetic",
        domain="EDUCATION",
        objective="Évaluer le risque de décrochage universitaire et proposer un accompagnement.",
        n_rows=len(education),
        columns={c: str(education[c].dtype) for c in education.columns},
        target="decrochage",
        method="Échantillonnage probabiliste multi-facteurs (moyennes, absences, situation socio-académique).",
        rules=[
            "Probabilité basée sur absences, modules non validés, moyennes, participation, internet, distance.",
            "Bruit aléatoire contrôlé ; pas de sanction automatique simulée.",
        ],
        sources=[
            "Indicateurs pédagogiques génériques (absences, moyennes /20).",
            "Aucune donnée réelle d'université marocaine.",
        ],
        limits=[
            "Ne constitue pas une sanction contre l'étudiant.",
            "Contexte synthétique simplifié.",
        ],
        biases=[
            "Sur-pondération possible de l'absence et de la distance.",
            "Filières échantillonnées uniformément (hors pondération réelle).",
        ],
        seed=args.seed,
        version=f"students-maroc-dropout-synthetic-v{DATASET_VERSION}",
    )
    print(f"[education] {education_path} -> {education_stats}")

    summary = {
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "seed": args.seed,
        "version": DATASET_VERSION,
        "credit": credit_stats,
        "medical": medical_stats,
        "education": education_stats,
        "disclaimer": (
            "Données synthétiques contextualisées au Maroc. "
            "Pas de données réelles de banques, hôpitaux ou universités."
        ),
    }
    summary_path = out / "generation_summary.json"
    summary_path.write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"[summary] {summary_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
