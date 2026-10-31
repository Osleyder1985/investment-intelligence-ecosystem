# Identity fixture harness (initial EVM/Solana scope)

This directory contains an offline, deterministic first implementation of the IIM identity fixture contract.

## Scope

- EVM: 20-byte hexadecimal address validation, case-insensitive canonicalization, network-scoped fixture key, and synthetic code-presence evidence.
- Solana: Base58 decoding to exactly 32 bytes, canonical Base58 representation, network-scoped mint key, and synthetic initialized-mint/program-owner evidence.
- Fixture schema validation against `schema/identity-fixture.schema.json`.
- Execution outcomes: PASS, FAIL, INCONCLUSIVE, BLOCKED, and NOT_RUN.

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

The runner exits non-zero for FAIL or BLOCKED. INCONCLUSIVE is an expected, auditable outcome for fixtures that intentionally lack evidence or contain unsupported Token-2022 extensions; it is not a passing identity resolution. Unit tests also assert that the same EVM address on different fixture networks yields different composite keys.

## Initial fixtures

- `FX-EVM-CASE-001`: two capitalization forms of the same synthetic EVM address.
- `FX-SOL-MINT-001`: a synthetic Solana mint public key with synthetic account initialization evidence.
- `FX-EVM-INCOMPLETE-001`: missing network checkpoint; expected resolution remains UNVERIFIED and execution is INCONCLUSIVE.
- `FX-EVM-NO-CODE-001`: valid address without contract code at the checkpoint; do not classify it as a confirmed token contract.
- `FX-SOL-TOKEN2022-UNSUPPORTED-EXT-001`: mint identity is retained with limitations, while behavioral/transferability conclusions abstain for an unsupported extension.

All fixture payloads are synthetic. No RPC data, provider payload, private key, credential, or real capture is included.

## Contract relationship

The JSON Schema formalizes the current fixture contract v0.1.0 for the initial EVM/Solana scope. It is an implementation artifact, not a claim that the methodology or network profiles have been independently approved. Unsupported profiles are rejected by the schema rather than silently processed.
