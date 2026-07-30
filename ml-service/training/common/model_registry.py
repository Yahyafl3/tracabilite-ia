"""Registre et persistance des modèles multidomain."""
from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import joblib

ROOT = Path(__file__).resolve().parents[2]
MODELS_DIR = ROOT / "models"


def domain_model_dir(domain: str) -> Path:
    return MODELS_DIR / domain.lower()


def pipeline_path(domain: str) -> Path:
    return domain_model_dir(domain) / f"{domain.lower()}_pipeline.joblib"


def metadata_path(domain: str) -> Path:
    return domain_model_dir(domain) / "metadata.json"


def save_model(
    domain: str,
    pipeline,
    metadata: dict[str, Any],
) -> tuple[Path, Path]:
    d = domain_model_dir(domain)
    d.mkdir(parents=True, exist_ok=True)
    p_path = pipeline_path(domain)
    m_path = metadata_path(domain)
    joblib.dump(pipeline, p_path)
    meta = {
        **metadata,
        "savedAt": datetime.now(timezone.utc).isoformat(),
        "pipelinePath": str(p_path.relative_to(ROOT)),
    }
    m_path.write_text(json.dumps(meta, indent=2, ensure_ascii=False), encoding="utf-8")
    return p_path, m_path


def load_model(domain: str):
    p = pipeline_path(domain)
    if not p.exists():
        raise FileNotFoundError(f"Modèle introuvable pour {domain}: {p}")
    return joblib.load(p)


def load_metadata(domain: str) -> dict[str, Any]:
    m = metadata_path(domain)
    if not m.exists():
        raise FileNotFoundError(f"Metadata introuvable pour {domain}: {m}")
    return json.loads(m.read_text(encoding="utf-8"))


def list_registered_models() -> list[dict[str, Any]]:
    result = []
    for domain in ("credit", "medical", "education"):
        m = metadata_path(domain)
        if m.exists():
            result.append(load_metadata(domain))
    return result
