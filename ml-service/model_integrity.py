"""Checksum SHA-256 des pipelines multidomain + registry DEMO_PUBLIC_DATASET."""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
MODELS_ROOT = ROOT / "models"
REGISTRY_PATH = MODELS_ROOT / "model_registry.json"

DOMAINS = ("credit", "medical", "education")


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def pipeline_path(domain: str) -> Path:
    return MODELS_ROOT / domain / f"{domain}_pipeline.joblib"


def metadata_path(domain: str) -> Path:
    return MODELS_ROOT / domain / "metadata.json"


def expected_checksum(domain: str) -> str | None:
    meta = metadata_path(domain)
    if not meta.exists():
        return None
    data = json.loads(meta.read_text(encoding="utf-8"))
    return data.get("checksumSha256") or data.get("checksum")


def verify_domain(domain: str, *, require_checksum: bool = False) -> dict:
    path = pipeline_path(domain)
    if not path.exists():
        return {"domain": domain, "ok": False, "error": "MODEL_MISSING"}
    digest = sha256_file(path)
    expected = expected_checksum(domain)
    if expected is None:
        status = "CHECKSUM_NOT_DECLARED"
        ok = not require_checksum
    else:
        ok = digest.lower() == str(expected).lower()
        status = "VALID" if ok else "CHECKSUM_MISMATCH"
    return {
        "domain": domain.upper(),
        "ok": ok,
        "status": status,
        "checksumSha256": digest,
        "expectedChecksum": expected,
        "path": str(path.name),
        "governanceStatus": "DEMO_PUBLIC_DATASET",
    }


def verify_all(require_checksum: bool = False) -> dict:
    results = [verify_domain(d, require_checksum=require_checksum) for d in DOMAINS]
    return {
        "allOk": all(r["ok"] for r in results),
        "models": results,
        "governance": "DEMO_PUBLIC_DATASET",
        "note": "Modèles entraînés sur datasets publics/recherche — non VALIDATED_PRODUCTION",
    }


def load_registry() -> dict:
    if REGISTRY_PATH.exists():
        return json.loads(REGISTRY_PATH.read_text(encoding="utf-8"))
    return {"models": [], "note": "Registry absent"}
