# Identity fixture harness (initial EVM/Solana scope)

This directory contains an offline, deterministic first implementation of the IIM identity fixture contract.

## Scope

- EVM: 20-byte hexadecimal address validation, case-insensitive canonicalization, network-scoped fixture key, and synthetic code-presence evidence.
- Solana: Base58 decoding to exactly 32 bytes, canonical Base58 representation, network-scoped mint key, and synthetic initialized-mint/program-owner evidence.
- Fixture schema validation against `schema/identity-fixture.schema.json`.
- Independent resolver outcomes: RESOLVED, UNRESOLVED, and CONFLICTED.
- Harness execution outcomes: PASS, FAIL, BLOCKED, and NOT_RUN.
- Conformance comparison between computed resolver output and fixture `expected_result`; expected results are never resolver inputs.

The harness does **not** connect to RPC/explorers, verify real network state, prove token standards, validate consensus/finality, infer economic equivalence, or approve sources/permissions. Synthetic fixture evidence is not real-world evidence.

## Run

From the repository root:

```bash
python -m pip install -r requirements-dev.txt
python -m unittest discover -s tests/identity_fixtures -p 'test_*.py' -v
python tests/identity_fixtures/harness.py
```

Run only one fixture (other fixtures are explicitly reported as NOT_RUN):

```bash
python tests/identity_fixtures/harness.py --only FX-EVM-CASE-001
```

The runner exits non-zero for FAIL or BLOCKED. A fixture that correctly expects abstention can have `execution_status=PASS` while the independent resolver reports `resolution_status=UNRESOLVED`. A conformance-test PASS means the implementation matched the fixture expectation; it does not mean the asset identity was resolved.

## Initial fixtures

- `FX-EVM-CASE-001`: two capitalization forms of the same synthetic EVM address.
- `FX-SOL-MINT-001`: a synthetic Solana mint public key with synthetic account initialization evidence.
- `FX-EVM-INCOMPLETE-001`: missing network checkpoint; legacy fixture expectation `UNVERIFIED` is explicitly crosswalked by the runner to resolver outcome `UNRESOLVED`. The conformance test passes when the resolver correctly abstains.

All fixture payloads are synthetic. No RPC data, provider payload, private key, credential, or real capture is included.

## Contract relationship

The JSON Schema formalizes the current fixture contract v0.1.0 for the initial EVM/Solana scope. The resolver computes from `profile_id`, `input.network_context`, and `input.observations`; it does not read `expected_result`. The harness then compares the computed result with `expected_result`. The explicit `UNVERIFIED` → `UNRESOLVED` mapping is a temporary legacy conformance crosswalk, not a global state-catalog approval. This is an implementation artifact, not a claim that the methodology or network profiles have been independently approved. Unsupported profiles are rejected by the schema rather than silently processed.
