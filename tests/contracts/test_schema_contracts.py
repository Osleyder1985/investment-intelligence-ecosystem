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
from jsonschema.exceptions import FormatError

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
        if pattern.startswith("^[0-9]+") and pattern.count("[0-9]+") >= 3:
            return "1.0.0"
        if "[a-fA-F0-9]{64}" in pattern:
            return "a" * 64
        if pattern.startswith("^[A-Z]"):
            return "USD"
        if pattern.startswith("^[A-Za-z0-9]"):
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


def find_present_schema_path(schema, instance, predicate, path=()):
    """Find a matching schema path that actually exists in the generated instance.

    Optional properties and arrays with minItems=0 are intentionally absent from
    minimal instances. Tests must not mutate those nonexistent paths.
    """
    if not isinstance(schema, dict):
        return None
    if predicate(schema):
        return path
    if isinstance(instance, dict):
        for key, child_schema in schema.get("properties", {}).items():
            if key not in instance:
                continue
            found = find_present_schema_path(
                child_schema, instance[key], predicate, path + (key,)
            )
            if found is not None:
                return found
    if isinstance(instance, list) and isinstance(schema.get("items"), dict):
        for index, item in enumerate(instance):
            found = find_present_schema_path(
                schema["items"], item, predicate, path + (index,)
            )
            if found is not None:
                return found
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

    def test_format_checker_dependency_rejects_invalid_rfc3339(self):
        with self.assertRaises(FormatError):
            FORMATS.check("not-a-timestamp", "date-time")

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
            timestamp_path = find_present_schema_path(
                schema,
                self.instances[filename],
                lambda node: node.get("format") == "date-time",
            )
            if timestamp_path is None:
                continue
            checked += 1
            with self.subTest(schema=filename, path=timestamp_path):
                instance = copy.deepcopy(self.instances[filename])
                set_at_path(instance, timestamp_path, "not-a-timestamp")
                validator = Draft202012Validator(schema, format_checker=FORMATS)
                self.assertTrue(list(validator.iter_errors(instance)))
        self.assertGreater(checked, 0)

    def test_decimal_pattern_rejects_non_decimal_text(self):
        checked = 0
        decimal_pattern = r"^-?(0|[1-9][0-9]*)(\.[0-9]+)?$"
        for filename, schema in self.schemas.items():
            path = find_present_schema_path(
                schema,
                self.instances[filename],
                lambda node: node.get("pattern") == decimal_pattern,
            )
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
