"""Pure offline identity resolver for synthetic EVM and Solana observations.

The registry shipped here is deliberately synthetic. This module does not query RPC,
prove consensus/finality, verify bytecode hashes, or establish economic identity.
"""
from __future__ import annotations

import hashlib
import json
import re
from dataclasses import dataclass
from typing import Any, Mapping, Sequence

RESOLVER_VERSION = "0.1.0"
REGISTRY_VERSION = "synthetic-registry-0.1.0"
EVM_PROFILE_VERSION = "evm-address-0.1.0"
SOLANA_PROFILE_VERSION = "solana-mint-0.1.0"

_BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
_EVM_ADDRESS = re.compile(r"^0x[0-9a-fA-F]{40}$")
_EVM_HASH = re.compile(r"^0x[0-9a-fA-F]{64}$")


@dataclass(frozen=True)
class NetworkDefinition:
    network_key: str
    family: str
    chain_id: str | None = None
    genesis_hash: str | None = None
    owner_programs: tuple[tuple[str, str], ...] = ()


@dataclass(frozen=True)
class NetworkIdentityRegistry:
    version: str
    networks: tuple[NetworkDefinition, ...]

    def match(self, family: str, context: Mapping[str, Any]) -> tuple[NetworkDefinition, ...]:
        matches = []
        for network in self.networks:
            if network.family != family:
                continue
            if family == "EVM":
                if (str(context.get("chain_id", "")) == str(network.chain_id)
                        and _norm_hash(context.get("genesis_hash")) == _norm_hash(network.genesis_hash)):
                    matches.append(network)
            elif family == "SOLANA":
                if _norm_hash(context.get("genesis_hash")) == _norm_hash(network.genesis_hash):
                    matches.append(network)
        return tuple(matches)


SYNTHETIC_REGISTRY = NetworkIdentityRegistry(
    version=REGISTRY_VERSION,
    networks=(
        NetworkDefinition(
            network_key="NET-EVM-SYNTH-01", family="EVM", chain_id="999001",
            genesis_hash="0x" + "a" * 64,
        ),
        NetworkDefinition(
            network_key="NET-EVM-SYNTH-02", family="EVM", chain_id="999002",
            genesis_hash="0x" + "d" * 64,
        ),
        NetworkDefinition(
            network_key="NET-SOL-SYNTH-01", family="SOLANA",
            genesis_hash="synthetic-genesis-hash",
            owner_programs=(("3JF3sEqM796hk5WFqA6EtmEwJQ9quALszsfJyvXNQKy3",
                             "SYNTHETIC_SPL_TOKEN_PROGRAM"),),
        ),
    ),
)


def _norm_hash(value: Any) -> str | None:
    if not isinstance(value, str):
        return None
    return value.lower()


def decode_base58(value: str) -> bytes:
    if not isinstance(value, str) or not value:
        raise ValueError("empty_or_non_string_base58")
    number = 0
    for char in value:
        digit = _BASE58.find(char)
        if digit < 0:
            raise ValueError("invalid_base58_character")
        number = number * 58 + digit
    body = number.to_bytes((number.bit_length() + 7) // 8, "big") if number else b""
    leading_zeroes = len(value) - len(value.lstrip("1"))
    return (b"\x00" * leading_zeroes) + body


def encode_base58(value: bytes) -> str:
    number = int.from_bytes(value, "big")
    encoded = ""
    while number:
        number, remainder = divmod(number, 58)
        encoded = _BASE58[remainder] + encoded
    leading_zeroes = len(value) - len(value.lstrip(b"\x00"))
    return ("1" * leading_zeroes) + encoded


def _network_checkpoint_present(context: Mapping[str, Any]) -> bool:
    checkpoint = context.get("checkpoint")
    return (
        isinstance(checkpoint, Mapping)
        and isinstance(checkpoint.get("height_or_slot"), int)
        and not isinstance(checkpoint.get("height_or_slot"), bool)
        and checkpoint.get("height_or_slot") >= 0
        and isinstance(checkpoint.get("block_or_state_hash"), str)
        and bool(checkpoint.get("block_or_state_hash"))
    )


def _result(status: str, family: str, diagnostics: Sequence[str], *,
            network_key: str | None = None, canonical_identifier: str | None = None,
            evidence_refs: Sequence[str] = ()) -> dict[str, Any]:
    identity_key = ([network_key, canonical_identifier]
                    if network_key is not None and canonical_identifier is not None else None)
    return {
        "resolver_version": RESOLVER_VERSION,
        "registry_version": REGISTRY_VERSION,
        "profile_version": {
            "EVM": EVM_PROFILE_VERSION,
            "SOLANA": SOLANA_PROFILE_VERSION,
        }.get(family, "unsupported"),
        "family": family,
        "resolution_status": status,
        "identity_key": identity_key,
        "evidence_refs": sorted(set(evidence_refs)),
        "diagnostics": list(diagnostics),
    }


def resolve_observations(
    family: str,
    network_context: Mapping[str, Any],
    observations: Sequence[Mapping[str, Any]],
    registry: NetworkIdentityRegistry = SYNTHETIC_REGISTRY,
) -> dict[str, Any]:
    """Resolve representation identity from observations, never expected fixture output."""
    observation_trace: list[dict[str, Any]] = []

    def finish(status: str, selected_family: str, diagnostics: Sequence[str], **kwargs: Any) -> dict[str, Any]:
        result = _result(status, selected_family, diagnostics, **kwargs)
        result["registry_version"] = registry.version
        result["observation_trace"] = list(observation_trace)
        return result
    if family not in {"EVM", "SOLANA"}:
        return finish("UNRESOLVED", str(family), ["unsupported_profile"])
    if not isinstance(network_context, Mapping) or not isinstance(observations, Sequence) or not observations:
        return finish("UNRESOLVED", family, ["missing_network_context_or_observations"])
    if network_context.get("family") != family:
        return finish("CONFLICTED", family, ["network_family_mismatch"])
    if not _network_checkpoint_present(network_context):
        return finish("UNRESOLVED", family, ["missing_network_checkpoint"])
    if not isinstance(registry.version, str) or not registry.version.strip():
        return finish("UNRESOLVED", family, ["registry_version_missing"])
    network_keys = [item.network_key for item in registry.networks]
    if len(network_keys) != len(set(network_keys)):
        return finish("CONFLICTED", family, ["registry_network_key_collision"])

    matches = registry.match(family, network_context)
    if not matches:
        return finish("UNRESOLVED", family, ["network_not_in_versioned_registry"])
    if len(matches) != 1:
        return finish("CONFLICTED", family, ["ambiguous_network_registry_match"])
    network = matches[0]

    identifiers: list[str] = []
    refs: list[str] = []
    evm_code_hashes: list[str] = []
    solana_owner_programs: list[str] = []
    for observation in observations:
        if not isinstance(observation, Mapping):
            return finish("UNRESOLVED", family, ["malformed_observation"])
        if observation.get("entity_type") != "asset_representation":
            return finish("UNRESOLVED", family, ["observation_entity_type_not_asset_representation"])
        raw = observation.get("raw_identifier")
        ref = observation.get("observation_ref")
        if not isinstance(ref, str) or not ref:
            return finish("UNRESOLVED", family, ["observation_reference_missing"])
        if isinstance(ref, str) and ref:
            refs.append(ref)
        observation_trace.append({
            "observation_ref": ref if isinstance(ref, str) else None,
            "raw_identifier": raw,
            "encoding": observation.get("encoding"),
            "normalized_identifier_claim": observation.get("normalized_identifier"),
            "normalization_method_claim": observation.get("normalization_method"),
        })
        if family == "EVM":
            if not isinstance(raw, str) or not _EVM_ADDRESS.fullmatch(raw):
                return finish("UNRESOLVED", family, ["invalid_evm_address"])
            canonical = "0x" + raw[2:].lower()
            state = observation.get("contract_state")
            if not isinstance(state, Mapping) or state.get("code_present") is not True:
                return finish("UNRESOLVED", family, ["contract_code_presence_unproven"],
                               network_key=network.network_key, evidence_refs=refs)
            code_hash = state.get("code_hash")
            if not isinstance(code_hash, str) or not _EVM_HASH.fullmatch(code_hash):
                return finish("UNRESOLVED", family, ["contract_code_hash_format_unproven"],
                               network_key=network.network_key, evidence_refs=refs)
            # Hash syntax is checked only; no bytecode is available to recompute it.
            evm_code_hashes.append(code_hash.lower())
            if observation.get("normalized_identifier") != canonical:
                return finish("UNRESOLVED", family, ["declared_normalization_mismatch"],
                               network_key=network.network_key, evidence_refs=refs)
            identifiers.append(canonical)
        else:
            try:
                decoded = decode_base58(raw)
            except (ValueError, TypeError):
                return finish("UNRESOLVED", family, ["invalid_solana_public_key"],
                               network_key=network.network_key, evidence_refs=refs)
            if len(decoded) != 32 or encode_base58(decoded) != raw:
                return finish("UNRESOLVED", family, ["solana_public_key_not_canonical_32_bytes"],
                               network_key=network.network_key, evidence_refs=refs)
            mint = observation.get("mint_account")
            if not isinstance(mint, Mapping) or mint.get("exists") is not True or mint.get("initialized") is not True:
                return finish("UNRESOLVED", family, ["mint_account_not_proven_initialized"],
                               network_key=network.network_key, evidence_refs=refs)
            owner = mint.get("owner_program_id")
            if not isinstance(owner, str):
                return finish("UNRESOLVED", family, ["mint_owner_program_unproven"],
                               network_key=network.network_key, evidence_refs=refs)
            try:
                owner_bytes = decode_base58(owner)
            except ValueError:
                return finish("UNRESOLVED", family, ["invalid_mint_owner_program_id"],
                               network_key=network.network_key, evidence_refs=refs)
            if len(owner_bytes) != 32 or encode_base58(owner_bytes) != owner:
                return finish("UNRESOLVED", family, ["invalid_mint_owner_program_id"],
                               network_key=network.network_key, evidence_refs=refs)
            allowed_owners = dict(network.owner_programs)
            if owner not in allowed_owners:
                return finish("UNRESOLVED", family, ["mint_owner_program_not_in_registry"],
                               network_key=network.network_key, evidence_refs=refs)
            # Deliberately ignore the fixture's free-text token_program label.
            if observation.get("normalized_identifier") != raw:
                return finish("UNRESOLVED", family, ["declared_normalization_mismatch"],
                               network_key=network.network_key, evidence_refs=refs)
            solana_owner_programs.append(owner)
            identifiers.append(raw)

    if len(set(evm_code_hashes)) > 1:
        return finish("CONFLICTED", family, ["observations_have_conflicting_contract_code_hashes"],
                       network_key=network.network_key, evidence_refs=refs)
    if len(set(solana_owner_programs)) > 1:
        return finish("CONFLICTED", family, ["observations_have_conflicting_mint_owner_programs"],
                       network_key=network.network_key, evidence_refs=refs)
    if len(set(identifiers)) != 1:
        return finish("CONFLICTED", family, ["observations_identify_different_representations"],
                       network_key=network.network_key, evidence_refs=refs)
    return finish("RESOLVED", family, ["representation_key_derived_from_observations"],
                   network_key=network.network_key, canonical_identifier=identifiers[0],
                   evidence_refs=refs)


def canonical_result_bytes(result: Mapping[str, Any]) -> bytes:
    """Stable JSON bytes for deterministic replay and digesting."""
    return json.dumps(result, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode("utf-8")


def result_digest(result: Mapping[str, Any]) -> str:
    return hashlib.sha256(canonical_result_bytes(result)).hexdigest()
