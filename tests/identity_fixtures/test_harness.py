import copy
import unittest

import harness


class IdentityFixtureHarnessTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.schema = harness.load_schema()
        cls.fixtures = {item["fixture_id"]: item for item in harness.load_fixtures()}

    def test_all_committed_fixtures_match_schema(self):
        for fixture in self.fixtures.values():
            with self.subTest(fixture=fixture["fixture_id"]):
                harness.validate_fixture(fixture, self.schema)

    def test_evm_case_variants_resolve_to_same_identity(self):
        result = harness.evaluate_fixture(self.fixtures["FX-EVM-CASE-001"], self.schema)
        self.assertEqual(result["execution_status"], "PASS")

    def test_solana_mint_key_is_network_scoped(self):
        result = harness.evaluate_fixture(self.fixtures["FX-SOL-MINT-001"], self.schema)
        self.assertEqual(result["execution_status"], "PASS")

    def test_missing_network_checkpoint_is_inconclusive(self):
        result = harness.evaluate_fixture(self.fixtures["FX-EVM-INCOMPLETE-001"], self.schema)
        self.assertEqual(result["execution_status"], "INCONCLUSIVE")
        self.assertIn("missing_network_checkpoint", result["diagnostics"])

    def test_wrong_expected_key_fails(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        fixture["expected_result"]["entity_candidates"][0]["key"][1] = "0x" + "0" * 40
        result = harness.evaluate_fixture(fixture, self.schema)
        self.assertEqual(result["execution_status"], "FAIL")
        self.assertIn("expected_identity_key_mismatch", result["diagnostics"])

    def test_insufficient_capture_permissions_block_execution(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        fixture["fixture_id"] = "FX-CAPTURE-BLOCKED-001"
        fixture["fixture_kind"] = "CAPTURED"
        fixture["provenance"] = {
            "synthetic": False,
            "source": {"provider": "synthetic-test-provider", "reference": "not-a-real-source"},
            "permission_record": {"authorized_operations": ["query"]}
        }
        result = harness.evaluate_fixture(fixture, self.schema)
        self.assertEqual(result["execution_status"], "BLOCKED")

    def test_synthetic_fixture_cannot_claim_real_source(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        fixture["provenance"]["source"] = {"provider": "unverified"}
        with self.assertRaises(harness.FixtureError):
            harness.validate_fixture(fixture, self.schema)

    def test_unselected_fixture_is_not_run(self):
        results = harness.run_suite({"FX-EVM-CASE-001"})
        statuses = {item["fixture_id"]: item["execution_status"] for item in results}
        self.assertEqual(statuses["FX-EVM-CASE-001"], "PASS")
        self.assertEqual(statuses["FX-SOL-MINT-001"], "NOT_RUN")
        self.assertEqual(statuses["FX-EVM-INCOMPLETE-001"], "NOT_RUN")

    def test_malformed_solana_public_key_fails(self):
        fixture = copy.deepcopy(self.fixtures["FX-SOL-MINT-001"])
        fixture["input"]["observations"][0]["raw_identifier"] = "1"
        result = harness.evaluate_fixture(fixture, self.schema)
        self.assertEqual(result["execution_status"], "FAIL")

    def test_evm_address_without_code_is_not_a_confirmed_token_contract(self):
        result = harness.evaluate_fixture(self.fixtures["FX-EVM-NO-CODE-001"], self.schema)
        self.assertEqual(result["execution_status"], "INCONCLUSIVE")
        self.assertEqual(result["resolution_status"], "UNVERIFIED")
        self.assertIn("contract_code_absent_at_checkpoint", result["diagnostics"])

    def test_unsupported_token2022_extension_limits_behavioral_conclusions(self):
        result = harness.evaluate_fixture(
            self.fixtures["FX-SOL-TOKEN2022-UNSUPPORTED-EXT-001"], self.schema
        )
        self.assertEqual(result["execution_status"], "INCONCLUSIVE")
        self.assertEqual(result["resolution_status"], "RESOLVED_WITH_LIMITATIONS")
        self.assertIn("unsupported_token2022_extension", result["diagnostics"])

    def test_same_evm_address_on_distinct_networks_has_distinct_keys(self):
        first = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        second = copy.deepcopy(first)
        second["fixture_id"] = "FX-EVM-OTHER-NETWORK-TEST"
        second["input"]["network_context"]["network_id_fixture"] = "NET-EVM-SYNTH-OTHER"
        second["expected_result"]["entity_candidates"][0]["key"][0] = "NET-EVM-SYNTH-OTHER"
        first_result = harness.evaluate_fixture(first, self.schema)
        second_result = harness.evaluate_fixture(second, self.schema)
        self.assertEqual(first_result["execution_status"], "PASS")
        self.assertEqual(second_result["execution_status"], "PASS")
        self.assertNotEqual(
            first["expected_result"]["entity_candidates"][0]["key"],
            second["expected_result"]["entity_candidates"][0]["key"],
        )


if __name__ == "__main__":
    unittest.main()
