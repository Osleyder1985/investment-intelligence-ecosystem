#!/usr/bin/env python3
"""Deterministic offline runner for the initial IIM EVM/Solana identity fixtures."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any

from jsonschema import Draft202012Validator, FormatChecker

ROOT = Path(__file__).resolve().parent
SCHEMA_PATH = ROOT / "schema" / "identity-fixture.schema.json"
FIXTURE_DIR = ROOT / "fixtures"
EVM_ADDRESS = re.compile(r"^0x[0-9a-fA-F]{40}$")
BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"


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


def encode_base58(value: bytes) -> str:
    number = int.from_bytes(value, "big")
    encoded = ""
    while number:
        number, remainder = divmod(number, 58)
        encoded = BASE58_ALPHABET[remainder] + encoded
    leading_zeroes = len(value) - len(value.lstrip(b"\x00"))
    return ("1" * leading_zeroes) + (encoded or "")


def decode_base58(value: str) -> bytes:
    if not value:
        raise FixtureError("empty Base58 identifier")
    number = 0
    for char in value:
        try:
            digit = BASE58_ALPHABET.index(char)
        except ValueError as exc:
            raise FixtureError(f"invalid Base58 character: {char!r}") from exc
        number = number * 58 + digit
    body = number.to_bytes((number.bit_length() + 7) // 8, "big") if number else b""
    leading_zeroes = len(value) - len(value.lstrip("1"))
    return (b"\x00" * leading_zeroes) + body


def _permission_blocked(fixture: dict[str, Any]) -> bool:
    if fixture["fixture_kind"] == "SYNTHETIC":
        return False
    record = fixture.get("provenance", {}).get("permission_record") or {}
    allowed = set(record.get("authorized_operations", []))
    return not {"storage", "retention"}.issubset(allowed)


def _network_evidence_incomplete(context: dict[str, Any]) -> bool:
    checkpoint = context.get("checkpoint")
    return not checkpoint or not checkpoint.get("block_or_state_hash")


def _evaluate_evm(fixture: dict[str, Any]) -> tuple[str, list[str]]:
    context = fixture["input"]["network_context"]
    observations = fixture["input"]["observations"]
    if _network_evidence_incomplete(context):
        return "INCONCLUSIVE", ["missing_network_checkpoint"]
    network_id = context["network_id_fixture"]
    normalized: list[str] = []
    for obs in observations:
        raw = obs["raw_identifier"]
        if not EVM_ADDRESS.fullmatch(raw):
            return "FAIL", ["invalid_evm_address_encoding"]
        canonical = "0x" + raw[2:].lower()
        if obs["normalized_identifier"] != canonical:
            return "FAIL", ["evm_normalization_mismatch"]
        state = obs.get("contract_state", {})
        if state.get("code_present") is not True:
            return "INCONCLUSIVE", ["contract_code_presence_not_proven"]
        code_hash = state.get("code_hash", "")
        if not re.fullmatch(r"0x[0-9a-fA-F]{64}", code_hash):
            return "INCONCLUSIVE", ["contract_code_hash_not_proven"]
        normalized.append(canonical)
    if len(set(normalized)) != 1:
        return "FAIL", ["same_representation_observations_diverged"]
    candidate = fixture["expected_result"]["entity_candidates"]
    if len(candidate) != 1 or candidate[0]["key"] != [network_id, normalized[0]]:
        return "FAIL", ["expected_identity_key_mismatch"]
    assertions = set(fixture["expected_result"]["assertions"])
    required = {"all_observations_same_entity", "adapter_version_not_in_identity_key"}
    if not required.issubset(assertions):
        return "FAIL", ["required_evm_assertion_missing"]
    if fixture["expected_result"]["resolution_status"] != "RESOLVED":
        return "FAIL", ["unexpected_expected_resolution_status"]
    return "PASS", ["evm_address_case_canonicalization_verified"]


def _evaluate_solana(fixture: dict[str, Any]) -> tuple[str, list[str]]:
    context = fixture["input"]["network_context"]
    observations = fixture["input"]["observations"]
    if _network_evidence_incomplete(context):
        return "INCONCLUSIVE", ["missing_cluster_checkpoint"]
    network_id = context["network_id_fixture"]
    normalized: list[str] = []
    for obs in observations:
        try:
            decoded = decode_base58(obs["raw_identifier"])
        except FixtureError:
            return "FAIL", ["invalid_solana_base58_identifier"]
        if len(decoded) != 32:
            return "FAIL", ["solana_pubkey_not_32_bytes"]
        if encode_base58(decoded) != obs["raw_identifier"]:
            return "FAIL", ["solana_pubkey_not_canonical_base58"]
        if obs["normalized_identifier"] != obs["raw_identifier"]:
            return "FAIL", ["solana_pubkey_normalization_mismatch"]
        mint = obs.get("mint_account", {})
        if "exists" not in mint or "initialized" not in mint or not mint.get("owner_program_id"):
            return "INCONCLUSIVE", ["mint_account_evidence_incomplete"]
        if mint["exists"] is not True or mint["initialized"] is not True:
            return "INCONCLUSIVE", ["mint_not_confirmed_initialized"]
        try:
            owner_bytes = decode_base58(mint["owner_program_id"])
        except FixtureError:
            return "INCONCLUSIVE", ["mint_owner_program_id_invalid"]
        if len(owner_bytes) != 32 or encode_base58(owner_bytes) != mint["owner_program_id"]:
            return "INCONCLUSIVE", ["mint_owner_program_id_invalid"]
        if mint.get("token_program") not in {"TOKEN_PROGRAM", "TOKEN_2022"}:
            return "INCONCLUSIVE", ["mint_owner_program_not_classified"]
        normalized.append(obs["normalized_identifier"])
    if len(set(normalized)) != 1:
        return "FAIL", ["same_mint_observations_diverged"]
    candidate = fixture["expected_result"]["entity_candidates"]
    if len(candidate) != 1 or candidate[0]["key"] != [network_id, normalized[0]]:
        return "FAIL", ["expected_identity_key_mismatch"]
    if "mint_identity_is_network_scoped" not in set(fixture["expected_result"]["assertions"]):
        return "FAIL", ["required_solana_assertion_missing"]
    if fixture["expected_result"]["resolution_status"] != "RESOLVED":
        return "FAIL", ["unexpected_expected_resolution_status"]
    return "PASS", ["solana_mint_identity_verified_synthetically"]


def evaluate_fixture(fixture: dict[str, Any], schema: dict[str, Any]) -> dict[str, Any]:
    try:
        validate_fixture(fixture, schema)
    except FixtureError as exc:
        return {"fixture_id": fixture.get("fixture_id", "<unknown>"), "execution_status": "FAIL",
                "diagnostics": ["schema_validation_failed", str(exc)]}
    if _permission_blocked(fixture):
        return {"fixture_id": fixture["fixture_id"], "execution_status": "BLOCKED",
                "diagnostics": ["captured_payload_storage_and_retention_not_authorized"]}
    profile = fixture["profile_id"]
    if profile == "EVM":
        status, diagnostics = _evaluate_evm(fixture)
    elif profile == "SOLANA":
        status, diagnostics = _evaluate_solana(fixture)
    else:
        status, diagnostics = "INCONCLUSIVE", ["profile_not_implemented_in_initial_harness"]
    return {"fixture_id": fixture["fixture_id"], "execution_status": status,
            "resolution_status": fixture["expected_result"]["resolution_status"],
            "diagnostics": diagnostics}


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
