#!/usr/bin/env python3
"""Deterministic offline runner for the initial IIM EVM/Solana identity fixtures."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

from jsonschema import Draft202012Validator, FormatChecker

from identity_resolver import resolve_observations

ROOT = Path(__file__).resolve().parent
SCHEMA_PATH = ROOT / "schema" / "identity-fixture.schema.json"
FIXTURE_DIR = ROOT / "fixtures"


class FixtureError(ValueError):
    pass


def load_schema() -> dict[str, Any]:
    return json.loads(SCHEMA_PATH.read_text(encoding="utf-8"))


def validate_fixture(fixture: dict[str, Any], schema: dict[str, Any]) -> None:
    Draft202012Validator.check_schema(schema)
    validator = Draft202012Validator(schema, format_checker=FormatChecker())
    errors = sorted(validator.iter_errors(fixture), key=lambda e: list(e.absolute_path))
    if errors:
        details = "; ".join(
            f"{'/'.join(map(str, err.absolute_path)) or '<root>'}: {err.message}"
            for err in errors
        )
        raise FixtureError(details)



# Explicit legacy compatibility: older fixtures used UNVERIFIED where the
# resolver contract now uses UNRESOLVED. This is a test-runner crosswalk only.
_LEGACY_RESOLUTION_STATUS = {"UNVERIFIED": "UNRESOLVED"}


def _permission_blocked(fixture: dict[str, Any]) -> bool:
    if fixture["fixture_kind"] == "SYNTHETIC":
        return False
    record = fixture.get("provenance", {}).get("permission_record") or {}
    allowed = set(record.get("authorized_operations", []))
    return not {"storage", "retention"}.issubset(allowed)


def _compare_expected(fixture: dict[str, Any], computed: dict[str, Any]) -> list[str]:
    expected = fixture["expected_result"]
    diagnostics: list[str] = []
    expected_status = _LEGACY_RESOLUTION_STATUS.get(
        expected["resolution_status"], expected["resolution_status"]
    )
    if computed["resolution_status"] != expected_status:
        diagnostics.append("expected_resolution_status_mismatch")

    expected_candidates = expected.get("entity_candidates", [])
    computed_key = computed.get("identity_key")
    if expected_candidates:
        if len(expected_candidates) != 1:
            diagnostics.append("expected_candidate_cardinality_unsupported")
        elif expected_candidates[0].get("key") != computed_key:
            diagnostics.append("expected_identity_key_mismatch")
    elif computed_key is not None:
        diagnostics.append("unexpected_computed_identity_candidate")

    expected_diagnostics = set(expected.get("expected_diagnostics", []))
    if not expected_diagnostics.issubset(set(computed.get("diagnostics", []))):
        diagnostics.append("expected_diagnostic_missing")
    return diagnostics


def evaluate_fixture(fixture: dict[str, Any], schema: dict[str, Any]) -> dict[str, Any]:
    try:
        validate_fixture(fixture, schema)
    except FixtureError as exc:
        return {
            "fixture_id": fixture.get("fixture_id", "<unknown>"),
            "execution_status": "FAIL",
            "resolution_status": "NOT_ASSESSED",
            "diagnostics": ["schema_validation_failed", str(exc)],
        }
    if _permission_blocked(fixture):
        return {
            "fixture_id": fixture["fixture_id"],
            "execution_status": "BLOCKED",
            "resolution_status": "NOT_ASSESSED",
            "diagnostics": ["captured_payload_storage_and_retention_not_authorized"],
        }

    computed = resolve_observations(
        fixture["profile_id"],
        fixture["input"]["network_context"],
        fixture["input"]["observations"],
    )
    comparison_diagnostics = _compare_expected(fixture, computed)
    execution_status = "FAIL" if comparison_diagnostics else "PASS"
    return {
        "fixture_id": fixture["fixture_id"],
        "execution_status": execution_status,
        "resolution_status": computed["resolution_status"],
        "expected_resolution_status": fixture["expected_result"]["resolution_status"],
        "identity_key": computed["identity_key"],
        "resolver_version": computed["resolver_version"],
        "registry_version": computed["registry_version"],
        "profile_version": computed["profile_version"],
        "evidence_refs": computed["evidence_refs"],
        "diagnostics": computed["diagnostics"] + comparison_diagnostics,
    }


def load_fixtures(directory: Path = FIXTURE_DIR) -> list[dict[str, Any]]:
    return [json.loads(path.read_text(encoding="utf-8"))
            for path in sorted(directory.glob("*.json"))]


def run_suite(only: set[str] | None = None, directory: Path = FIXTURE_DIR) -> list[dict[str, Any]]:
    schema = load_schema()
    results = []
    for fixture in load_fixtures(directory):
        if only is not None and fixture["fixture_id"] not in only:
            results.append({"fixture_id": fixture["fixture_id"], "execution_status": "NOT_RUN",
                            "diagnostics": ["excluded_by_fixture_selection"]})
        else:
            results.append(evaluate_fixture(fixture, schema))
    return results


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--only", action="append", help="fixture_id to execute; repeatable")
    args = parser.parse_args(argv)
    results = run_suite(set(args.only) if args.only else None)
    for result in results:
        print(json.dumps(result, sort_keys=True))
    statuses = {item["execution_status"] for item in results}
    if "FAIL" in statuses:
        return 1
    if "BLOCKED" in statuses:
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
