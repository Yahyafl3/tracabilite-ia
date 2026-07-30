"""
E2E API automatisé — cycles CREDIT / MEDICAL / EDUCATION via backend Docker.
Usage:
  $env:API_URL="http://localhost:8080"
  python ops/e2e_api_smoke.py
"""
from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass, field
from typing import Any

API = os.environ.get("API_URL", "http://localhost:8080").rstrip("/")


@dataclass
class Result:
    name: str
    expected: str
    observed: str
    status: str  # PASS | FAIL
    detail: str = ""


RESULTS: list[Result] = []


def record(name: str, expected: str, observed: str, ok: bool, detail: str = "") -> None:
    RESULTS.append(
        Result(name=name, expected=expected, observed=observed, status="PASS" if ok else "FAIL", detail=detail)
    )


def http(
    method: str,
    path: str,
    body: dict | None = None,
    token: str | None = None,
    accept: str | None = None,
) -> tuple[int, Any]:
    data = None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if accept:
        headers["Accept"] = accept
    if body is not None:
        data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(f"{API}{path}", data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            raw = resp.read().decode("utf-8")
            try:
                return resp.status, json.loads(raw) if raw else {}
            except json.JSONDecodeError:
                return resp.status, raw
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        try:
            payload = json.loads(raw) if raw else {}
        except json.JSONDecodeError:
            payload = raw
        return e.code, payload


def login(email: str, password: str) -> str | None:
    status, body = http("POST", "/api/auth/login", {"email": email, "motDePasse": password})
    if status == 200 and isinstance(body, dict):
        return body.get("token") or body.get("accessToken")
    return None


CREDIT = {
    "dossierReference": "E2E-CREDIT-001",
    "description": "Dossier E2E crédit synthétique pour tests automatisés.",
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
    "includeAgents": False,
}

MEDICAL = {
    "dossierReference": "E2E-MED-001",
    "description": "Dossier E2E médical synthétique — pas un diagnostic.",
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
    "includeAgents": False,
}

EDUCATION = {
    "dossierReference": "E2E-EDU-001",
    "description": "Dossier E2E éducation synthétique — accompagnement uniquement.",
    "region": "Fès-Meknès",
    "typeEtablissement": "UNIVERSITE_PUBLIQUE",
    "filiere": "SCIENCES",
    "niveauEtude": "L2",
    "moyenneSemestre1": 10.5,
    "moyenneSemestre2": 9.8,
    "tauxAbsence": 0.22,
    "modulesNonValides": 2,
    "participation": "FAIBLE",
    "bourse": "OUI",
    "distanceLogementKm": 18,
    "accesInternet": "OUI",
    "activiteProfessionnelle": "NON",
    "historiqueRedoublement": "NON",
    "situationAcademique": "DIFFICULTE",
    "includeAgents": False,
}


def flow_domain(
    label: str,
    create_path: str,
    payload: dict,
    user_token: str,
    validator_token: str,
    decision_value: str,
) -> str | None:
    status, body = http("POST", create_path, payload, token=user_token)
    ok = status in (200, 201) and isinstance(body, dict) and body.get("decisionId")
    record(f"{label} create+ML", "201/200 + decisionId", f"{status}", bool(ok), str(body)[:200])
    if not ok:
        return None
    decision_id = body["decisionId"]

    status, _ = http("POST", f"/api/decisions/{decision_id}/submit", {}, token=user_token)
    record(f"{label} submit", "200", f"{status}", status == 200)

    status, _ = http(
        "POST",
        f"/api/decisions/{decision_id}/validate",
        {
            "decisionFinale": decision_value,
            "justificationHumaine": "Validation E2E automatisée — dossier synthétique vérifié.",
            "accordAvecIa": True,
        },
        token=validator_token,
    )
    record(f"{label} validate", "200", f"{status}", status == 200)

    status, detail = http("GET", f"/api/decisions/{decision_id}", token=user_token)
    record(f"{label} detail", "200", f"{status}", status == 200)

    status, integ = http("POST", f"/api/decisions/{decision_id}/verify-integrity", token=validator_token)
    record(f"{label} integrity endpoint", "200 + status field", f"{status} {integ}", status == 200 and isinstance(integ, dict) and "status" in integ)
    if isinstance(integ, dict):
        record(f"{label} integrity VALID", "VALID", str(integ.get("status")), integ.get("status") == "VALID")

    status, hist = http("GET", f"/api/decisions/{decision_id}/history", token=user_token)
    record(f"{label} history", "200", f"{status}", status == 200)

    return decision_id


def main() -> int:
    # Auth negatives
    status, _ = http("GET", "/api/decisions")
    record("unauthenticated list", "401", f"{status}", status == 401)

    user_token = login("user@tracabilite.ia", "user123")
    admin_token = login("admin@tracabilite.ia", "admin123")
    credit_v = login("credit@tracabilite.ia", "credit123")
    medical_v = login("sante@tracabilite.ia", "sante123")
    edu_v = login("pedago@tracabilite.ia", "pedago123")
    auditor_token = login("auditeur@tracabilite.ia", "auditor123")

    record("login user", "token", "ok" if user_token else "fail", bool(user_token))
    record("login credit validator", "token", "ok" if credit_v else "fail", bool(credit_v))
    record("login medical validator", "token", "ok" if medical_v else "fail", bool(medical_v))
    record("login education validator", "token", "ok" if edu_v else "fail", bool(edu_v))

    if not user_token:
        print("FATAL: cannot login user — abort E2E")
        _print_report()
        return 2

    # Forbidden role sample
    if user_token:
        status, _ = http("GET", "/api/users", token=user_token)
        record("user accessing admin users", "403", f"{status}", status in (403, 401))

    credit_id = None
    if credit_v:
        credit_id = flow_domain("CREDIT", "/api/decisions/credit", CREDIT, user_token, credit_v, "ACCEPTEE")
    if medical_v:
        flow_domain("MEDICAL", "/api/decisions/medical", MEDICAL, user_token, medical_v, "SUIVI_STANDARD")
    if edu_v:
        flow_domain("EDUCATION", "/api/decisions/education", EDUCATION, user_token, edu_v, "ACCOMPAGNEMENT")

    # Self-validation refusal if same user tries to validate
    if credit_id and user_token:
        status, _ = http(
            "POST",
            f"/api/decisions/{credit_id}/validate",
            {
                "decisionFinale": "ACCEPTEE",
                "justificationHumaine": "Tentative auto-validation interdite pour E2E.",
                "accordAvecIa": True,
            },
            token=user_token,
        )
        record("author self-validate", "403/409/400", f"{status}", status in (403, 409, 400, 401))

    # Export (admin/auditor)
    export_token = auditor_token or admin_token
    if export_token:
        status, body = http("GET", "/api/decisions/export?format=csv", token=export_token, accept="text/csv")
        record("export CSV", "200", f"{status}", status == 200)
        status, body = http("GET", "/api/decisions/export?format=xlsx", token=export_token)
        record("export XLSX", "200", f"{status}", status == 200)

    # Pagination / list
    status, _ = http("GET", "/api/decisions?page=0&size=10", token=user_token)
    record("list paginated", "200", f"{status}", status == 200)

    health_s, health_b = http("GET", "/actuator/health")
    record("backend health", "200 UP", f"{health_s} {health_b}", health_s == 200)

    return _print_report()


def _print_report() -> int:
    fails = [r for r in RESULTS if r.status == "FAIL"]
    print(f"E2E results: {len(RESULTS) - len(fails)} PASS / {len(fails)} FAIL / {len(RESULTS)} total")
    for r in RESULTS:
        print(f"[{r.status}] {r.name} | expected={r.expected} | observed={r.observed}")
    # Write markdown snippet path for later report assembly
    out = os.path.join(os.path.dirname(__file__), "..", "docs", "_e2e_raw_results.json")
    with open(out, "w", encoding="utf-8") as f:
        json.dump([r.__dict__ for r in RESULTS], f, ensure_ascii=False, indent=2)
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
