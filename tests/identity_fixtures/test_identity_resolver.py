import copy
import unittest

import harness
from identity_resolver import (
    SYNTHETIC_REGISTRY,
    NetworkDefinition,
    NetworkIdentityRegistry,
    canonical_result_bytes,
    resolve_observations,
    result_digest,
)


class IndependentIdentityResolverTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixtures = {item["fixture_id"]: item for item in harness.load_fixtures()}

    def resolve_fixture(self, fixture, registry=SYNTHETIC_REGISTRY):
        return resolve_observations(
            fixture["profile_id"],
            fixture["input"]["network_context"],
            fixture["input"]["observations"],
            registry,
        )

    def test_evm_case_variants_derive_same_network_scoped_key(self):
        fixture = self.fixtures["FX-EVM-CASE-001"]
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "RESOLVED")
        self.assertEqual(result["identity_key"], [
            "NET-EVM-SYNTH-01", "0x52908400098527886e0f7030069857d2e4169ee7"
        ])
        self.assertEqual(len(result["evidence_refs"]), 2)

    def test_same_evm_address_on_distinct_registered_networks_does_not_collide(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        first = self.resolve_fixture(fixture)
        fixture["input"]["network_context"]["chain_id"] = "999002"
        fixture["input"]["network_context"]["genesis_hash"] = "0x" + "d" * 64
        second = self.resolve_fixture(fixture)
        self.assertEqual(first["resolution_status"], "RESOLVED")
        self.assertEqual(second["resolution_status"], "RESOLVED")
        self.assertNotEqual(first["identity_key"], second["identity_key"])
        self.assertEqual(first["identity_key"][1], second["identity_key"][1])

    def test_unregistered_or_mismatched_network_is_unresolved(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        fixture["input"]["network_context"]["chain_id"] = "1"
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "UNRESOLVED")
        self.assertIn("network_not_in_versioned_registry", result["diagnostics"])

    def test_missing_checkpoint_is_unresolved(self):
        fixture = self.fixtures["FX-EVM-INCOMPLETE-001"]
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "UNRESOLVED")
        self.assertIn("missing_network_checkpoint", result["diagnostics"])

    def test_expected_result_mutation_cannot_change_resolver_output(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        before = self.resolve_fixture(fixture)
        fixture["expected_result"]["resolution_status"] = "CONFLICTED"
        fixture["expected_result"]["entity_candidates"] = []
        after = self.resolve_fixture(fixture)
        self.assertEqual(before, after)

    def test_evm_malformed_address_fails_closed(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        fixture["input"]["observations"][0]["raw_identifier"] = "0x123"
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "UNRESOLVED")
        self.assertIn("invalid_evm_address", result["diagnostics"])

    def test_evm_divergent_observations_are_conflicted(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        fixture["input"]["observations"][1]["raw_identifier"] = "0x" + "1" * 40
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "CONFLICTED")

    def test_solana_pubkey_derives_identity_from_network_and_raw_key(self):
        fixture = self.fixtures["FX-SOL-MINT-001"]
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "RESOLVED")
        self.assertEqual(result["identity_key"], [
            "NET-SOL-SYNTH-01", "29d2S7vB453rNYFdR5Ycwt7y9haRT5fwVwL9zTmBhfV2"
        ])

    def test_solana_owner_program_is_registry_checked_not_label_trusted(self):
        fixture = copy.deepcopy(self.fixtures["FX-SOL-MINT-001"])
        fixture["input"]["observations"][0]["mint_account"]["token_program"] = "TOKEN_2022"
        fixture["input"]["observations"][1]["mint_account"]["token_program"] = "UNTRUSTED_LABEL"
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "RESOLVED")

    def test_solana_unknown_owner_program_is_unresolved(self):
        fixture = self.fixtures["FX-SOL-MINT-001"]
        registry = NetworkIdentityRegistry(
            version="solana-registry-without-token-owner",
            networks=tuple(
                NetworkDefinition(
                    network_key=n.network_key, family=n.family, chain_id=n.chain_id,
                    genesis_hash=n.genesis_hash,
                    owner_programs=() if n.family == "SOLANA" else n.owner_programs,
                )
                for n in SYNTHETIC_REGISTRY.networks
            ),
        )
        result = self.resolve_fixture(fixture, registry)
        self.assertEqual(result["resolution_status"], "UNRESOLVED")
        self.assertIn("mint_owner_program_not_in_registry", result["diagnostics"])
        self.assertEqual(result["registry_version"], "solana-registry-without-token-owner")

    def test_solana_malformed_pubkey_is_unresolved(self):
        fixture = copy.deepcopy(self.fixtures["FX-SOL-MINT-001"])
        fixture["input"]["observations"][0]["raw_identifier"] = "1"
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "UNRESOLVED")

    def test_solana_unknown_genesis_is_unresolved(self):
        fixture = copy.deepcopy(self.fixtures["FX-SOL-MINT-001"])
        fixture["input"]["network_context"]["genesis_hash"] = "unregistered-genesis"
        result = self.resolve_fixture(fixture)
        self.assertEqual(result["resolution_status"], "UNRESOLVED")

    def test_registry_ambiguity_is_conflicted(self):
        fixture = self.fixtures["FX-EVM-CASE-001"]
        duplicate = NetworkDefinition(
            network_key="NET-EVM-SYNTH-DUPLICATE", family="EVM", chain_id="999001",
            genesis_hash="0x" + "a" * 64,
        )
        registry = NetworkIdentityRegistry(
            version="ambiguous-test-registry",
            networks=SYNTHETIC_REGISTRY.networks + (duplicate,),
        )
        result = self.resolve_fixture(fixture, registry)
        self.assertEqual(result["resolution_status"], "CONFLICTED")

    def test_identical_input_replays_to_identical_canonical_bytes(self):
        fixture = self.fixtures["FX-EVM-CASE-001"]
        first = self.resolve_fixture(fixture)
        second = self.resolve_fixture(copy.deepcopy(fixture))
        self.assertEqual(canonical_result_bytes(first), canonical_result_bytes(second))
        self.assertEqual(result_digest(first), result_digest(second))


if __name__ == "__main__":
    unittest.main()
