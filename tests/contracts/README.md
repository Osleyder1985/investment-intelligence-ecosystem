# Portfolio & Execution contract schema tests

This suite checks the six preliminary JSON Schemas in `schemas/contracts/`.

## Run

From the repository root:

```bash
python -m pip install -r requirements-dev.txt
python -m unittest discover -s tests/contracts -p 'test_*.py' -v
```

## Coverage

- JSON parsing for all six schema files.
- Meta-validation with `Draft202012Validator.check_schema`.
- Synthetic minimal instances generated from each schema's required structure.
- Explicit `FormatChecker` validation for date-time fields.
- Rejection of missing required root properties, undeclared root properties and wrong schema versions.
- Rejection of malformed decimal strings where the schema declares the shared decimal pattern.

The test instances are generated synthetically by the test helper; they are not market, portfolio or user data. The tests do not validate cross-field semantics, authorization, financial invariants, operational safety, schema compatibility, or production readiness.

A passing suite means only that the tested schema mechanics behave as asserted under the installed `jsonschema` implementation. It is not independent approval of these draft contracts.
