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

    def test_expected_abstention_is_a_passing_conformance_test(self):
        result = harness.evaluate_fixture(self.fixtures["FX-EVM-INCOMPLETE-001"], self.schema)
        self.assertEqual(result["execution_status"], "PASS")
        self.assertEqual(result["resolution_status"], "UNRESOLVED")
        self.assertEqual(result["expected_resolution_status"], "UNVERIFIED")
        self.assertIn("missing_network_checkpoint", result["diagnostics"])

    def test_expected_result_is_only_used_by_conformance_comparator(self):
        fixture = copy.deepcopy(self.fixtures["FX-EVM-CASE-001"])
        original = harness.evaluate_fixture(fixture, self.schema)
        fixture["expected_result"]["entity_candidates"][0]["key"][1] = "0x" + "0" * 40
        changed = harness.evaluate_fixture(fixture, self.schema)
        self.assertEqual(original["identity_key"], changed["identity_key"])
        self.assertEqual(original["resolution_status"], changed["resolution_status"])
        self.assertEqual(original["execution_status"], "PASS")
        self.assertEqual(changed["execution_status"], "FAIL")
        self.assertIn("expected_identity_key_mismatch", changed["diagnostics"])

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


if __name__ == "__main__":
    unittest.main()
