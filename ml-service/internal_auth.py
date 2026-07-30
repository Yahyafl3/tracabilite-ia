"""Authentification interne Spring ↔ Flask via header X-Internal-Token."""
from __future__ import annotations

import hmac
import os
from typing import Iterable

from flask import Request, jsonify

HEADER_NAME = "X-Internal-Token"

# Endpoints publics (healthchecks Docker / orchestration)
PUBLIC_PREFIXES: tuple[str, ...] = (
    "/health/live",
    "/health",  # alias live
    "/ready",
    "/health/ready",
)


def get_expected_token() -> str:
    return (os.environ.get("ML_SERVICE_TOKEN") or "").strip()


def is_public_path(path: str) -> bool:
    if path in PUBLIC_PREFIXES:
        return True
    # Exact /health and /health/live only — not /health/something-secret
    if path.rstrip("/") in ("/health", "/health/live", "/ready", "/health/ready"):
        return True
    return False


def auth_required_for_path(path: str) -> bool:
    return not is_public_path(path)


def verify_request_token(request: Request) -> tuple[bool, str | None]:
    """
    Returns (ok, error_code).
    If ML_SERVICE_TOKEN is unset/empty: auth is disabled (local only).
    """
    expected = get_expected_token()
    if not expected:
        return True, None

    provided = (request.headers.get(HEADER_NAME) or "").strip()
    if not provided:
        return False, "TOKEN_MISSING"
    if not hmac.compare_digest(provided, expected):
        return False, "TOKEN_INVALID"
    return True, None


def unauthorized_response(code: str):
    # Never echo the token
    return jsonify({
        "error": "Unauthorized",
        "code": code,
        "message": "Authentification interne requise",
    }), 401
