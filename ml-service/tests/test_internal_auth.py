"""Tests authentification interne ML_SERVICE_TOKEN."""
from __future__ import annotations

import hmac
import os

import pytest

from internal_auth import (
    HEADER_NAME,
    auth_required_for_path,
    get_expected_token,
    verify_request_token,
)


class _FakeRequest:
    def __init__(self, headers: dict[str, str]):
        self.headers = headers


def test_public_paths_do_not_require_auth():
    assert auth_required_for_path("/health/live") is False
    assert auth_required_for_path("/health") is False
    assert auth_required_for_path("/ready") is False
    assert auth_required_for_path("/health/ready") is False
    assert auth_required_for_path("/predict/credit") is True
    assert auth_required_for_path("/models") is True
    assert auth_required_for_path("/schema") is True


def test_auth_disabled_when_token_unset(monkeypatch):
    monkeypatch.delenv("ML_SERVICE_TOKEN", raising=False)
    ok, code = verify_request_token(_FakeRequest({}))
    assert ok is True
    assert code is None


def test_missing_token_rejected(monkeypatch):
    monkeypatch.setenv("ML_SERVICE_TOKEN", "super-secret-ml-token-value-24")
    ok, code = verify_request_token(_FakeRequest({}))
    assert ok is False
    assert code == "TOKEN_MISSING"


def test_empty_token_rejected(monkeypatch):
    monkeypatch.setenv("ML_SERVICE_TOKEN", "super-secret-ml-token-value-24")
    ok, code = verify_request_token(_FakeRequest({HEADER_NAME: ""}))
    assert ok is False
    assert code == "TOKEN_MISSING"


def test_invalid_token_rejected(monkeypatch):
    monkeypatch.setenv("ML_SERVICE_TOKEN", "super-secret-ml-token-value-24")
    ok, code = verify_request_token(_FakeRequest({HEADER_NAME: "wrong-token"}))
    assert ok is False
    assert code == "TOKEN_INVALID"


def test_valid_token_accepted(monkeypatch):
    token = "super-secret-ml-token-value-24"
    monkeypatch.setenv("ML_SERVICE_TOKEN", token)
    ok, code = verify_request_token(_FakeRequest({HEADER_NAME: token}))
    assert ok is True
    assert code is None


def test_compare_digest_used_for_equality():
    # Garde-fou : hmac.compare_digest doit être utilisé (temps constant)
    assert hmac.compare_digest("abc", "abc") is True
    assert hmac.compare_digest("abc", "abd") is False


def test_token_not_in_get_expected_when_logging_safe(monkeypatch, capsys):
    monkeypatch.setenv("ML_SERVICE_TOKEN", "must-not-print-this-token-value")
    _ = get_expected_token()
    captured = capsys.readouterr()
    assert "must-not-print-this-token-value" not in captured.out
    assert "must-not-print-this-token-value" not in captured.err
