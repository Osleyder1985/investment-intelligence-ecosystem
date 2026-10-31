"""Conformance tests for the preliminary Portfolio & Execution JSON Schemas.

All generated instances are synthetic and exist only to exercise schema behavior.
These tests do not establish semantic or operational correctness.
"""
from __future__ import annotations

import copy
import json
import unittest
from pathlib import Path

from jsonschema import Draft202012Validator, FormatChecker

ROOT = Path(__file__).resolve().parents[2]
SCHEMA_DIR = ROOT / "schemas" / "contracts"
SCHEMA_FILES = (
    "investment-mandate.schema.json",
    "exposure-snapshot.schema.json",
    "risk-policy.schema.json",
    "risk-budget.schema.json",
    "portfolio-proposal.schema.json",
    "audit-event.schema.json",
)
FORMATS = FormatChecker()


def sample_for(schema):
    """Build a minimal synthetic instance satisfying the schema's required shape."""
    if "const" in schema:
        return schema["const"]
    if "enum" in schema:
        return schema["enum"][0]
    kind = schema.get("type")
    if isinstance(kind, list):
        kind = next((item for item in kind if item != "null"), kind[0])
    if kind == "object":
        props = schema.get("properties", {})
        return {
            key: sample_for(props[key])
            for key in schema.get("required", [])
            if key in props
        }
    if kind == "array":
        count = schema.get("minItems", 0)
        return [sample_for(schema["items"]) for _ in range(count)]
    if kind == "string":
        fmt = schema.get("format")
        if fmt == "date-time":
            return "2026-10-31T12:00:00Z"
        if fmt == "date":
            return "2026-10-31"
        if fmt == "uri":
            return "https://example.org/evidence/fixture-1"
        pattern = schema.get("pattern", "")
        if "0-9" in pattern and r"\." in pattern:
            return "1.0.0"
        if "[a-fA-F0-9]{64}" in pattern:
            return "a" * 64
        if "0-9" in pattern and r"\." in pattern:
            return "USD"
        if "0-9" in pattern and r"\." in pattern:
            return "fixture-1"
        if pattern.startswith("^-?"):
            return "1.25"
        return "fixture"
    if kind == "integer":
        return schema.get("minimum", 1)
    if kind == "number":
        return schema.get("minimum", 1)
    if kind == "boolean":
        return True
    if kind == "null":
        return None
    return {}


def find_pattern_path(schema, pattern, path=()):
    if not isinstance(schema, dict):
        return None
    if schema.get("pattern") == pattern:
        return path
    for key, child in schema.get("properties", {}).items():
        found = find_pattern_path(child, pattern, path + (key,))
        if found is not None:
            return found
    if isinstance(schema.get("items"), dict):
        return find_pattern_path(schema["items"], pattern, path + (0,))
    return None


def set_at_path(value, path, replacement):
    target = value
    for part in path[:-1]:
        target = target[part]
    target[path[-1]] = replacement


class ContractSchemaTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.schemas = {}
        cls.instances = {}
        for filename in SCHEMA_FILES:
            with (SCHEMA_DIR / filename).open(encoding="utf-8") as handle:
                schema = json.load(handle)
            cls.schemas[filename] = schema
            Draft202012Validator.check_schema(schema)
            cls.instances[filename] = sample_for(schema)

    def test_all_six_schemas_are_valid_draft_2020_12(self):
        self.assertEqual(set(self.schemas), set(SCHEMA_FILES))
        for filename, schema in self.schemas.items():
            with self.subTest(schema=filename):
                self.assertEqual(
                    schema.get("$schema"),
                    "https://json-schema.org/draft/2020-12/schema",
                )
                self.assertTrue(schema.get("$id"))
                self.assertIs(schema.get("additionalProperties"), False)

    def test_synthetic_minimal_instances_are_valid(self):
        for filename, schema in self.schemas.items():
            with self.subTest(schema=filename):
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                errors = list(validator.iter_errors(self.instances[filename]))
                self.assertEqual(errors, [], msg="; ".join(e.message for e in errors))

    def test_missing_required_root_property_is_rejected(self):
        for filename, schema in self.schemas.items():
            with self.subTest(schema=filename):
                instance = copy.deepcopy(self.instances[filename])
                required = schema["required"][0]
                del instance[required]
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                self.assertTrue(list(validator.iter_errors(instance)))

    def test_unknown_root_property_is_rejected(self):
        for filename, schema in self.schemas.items():
            with self.subTest(schema=filename):
                instance = copy.deepcopy(self.instances[filename])
                instance["__unexpected_property__"] = "must be rejected"
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                self.assertTrue(list(validator.iter_errors(instance)))

    def test_wrong_schema_version_is_rejected(self):
        for filename, schema in self.schemas.items():
            with self.subTest(schema=filename):
                instance = copy.deepcopy(self.instances[filename])
                instance["schema_version"] = "999.0.0"
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                self.assertTrue(list(validator.iter_errors(instance)))

    def test_invalid_date_time_format_is_rejected_when_present(self):
        checked = 0
        for filename, schema in self.schemas.items():
            # Contract timestamp fields are declared with format=date-time.
            timestamp_path = self._find_format_path(schema, "date-time")
            if timestamp_path is None:
                continue
            checked += 1
            with self.subTest(schema=filename):
                instance = copy.deepcopy(self.instances[filename])
                set_at_path(instance, timestamp_path, "not-a-timestamp")
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                self.assertTrue(list(validator.iter_errors(instance)))
        self.assertGreater(checked, 0)

    @staticmethod
    def _find_format_path(schema, fmt, path=()):
        if not isinstance(schema, dict):
            return None
        if schema.get("format") == fmt:
            return path
        for key, child in schema.get("properties", {}).items():
            found = ContractSchemaTests._find_format_path(child, fmt, path + (key,))
            if found is not None:
                return found
        if isinstance(schema.get("items"), dict):
            return ContractSchemaTests._find_format_path(
                schema["items"], fmt, path + (0,)
            )
        return None

    def test_decimal_pattern_rejects_non_decimal_text(self):
        checked = 0
        decimal_pattern = r"^-?(0|[1-9][0-9]*)(\.[0-9]+)?$"
        for filename, schema in self.schemas.items():
            path = find_pattern_path(schema, decimal_pattern)
            if path is None:
                continue
            checked += 1
            with self.subTest(schema=filename, path=path):
                instance = copy.deepcopy(self.instances[filename])
                set_at_path(instance, path, "1.2.3")
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                self.assertTrue(list(validator.iter_errors(instance)))
        self.assertGreater(checked, 0)


if __name__ == "__main__":
    unittest.main()
