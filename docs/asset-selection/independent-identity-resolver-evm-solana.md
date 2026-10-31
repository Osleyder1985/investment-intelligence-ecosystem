# Independent identity resolver — initial offline slice

## Status

PROPOSED IMPLEMENTATION / SYNTHETIC TESTS ONLY / NOT APPROVED FOR PRODUCTION OR INVESTMENT DECISIONS.

## What is implemented in this branch

- A pure resolver API that receives profile, network context, observations, and an explicit versioned registry.
- Canonical EVM representation keys are derived from the raw 20-byte address and a registry-resolved network key.
- Solana public keys are decoded and required to be canonical Base58 encodings of exactly 32 bytes.
- Solana mint owner program IDs are checked against the registry; free-text `token_program` labels are ignored.
- Conflicting EVM code-hash observations, conflicting registered Solana owner programs, unsupported observation entity types, missing observation references, normalization-claim mismatches, and registry network-key collisions fail closed.
- Missing checkpoints, unregistered networks, malformed identifiers, and insufficient account/contract evidence produce unresolved outcomes; contradictory observations or ambiguous registry matches produce conflicted outcomes.
- Results include resolver, registry and profile versions, evidence references, raw observation trace, claimed encoding/normalization metadata, diagnostics, and deterministic canonical JSON/digest helpers. Claimed normalized values are retained for audit but are not trusted as resolver inputs.

## Explicit limitations

- The registry contains synthetic network identities and a synthetic Solana owner-program mapping only.
- The resolver does not connect to RPC, validate chain consensus/finality, establish canonicality/authenticity of the supplied checkpoint, or verify a genesis hash against a real network. Checkpoint presence is a gate only; the supplied checkpoint hash is not independently authenticated.
- EVM code hash is format-checked and compared across supplied observations only. No bytecode is supplied, so the hash is not recomputed or authenticated.
- Solana mint account state is supplied as an observation. The resolver does not independently fetch or verify account state.
- This identifies a network-scoped representation, not an economic asset, issuer, backing claim, bridge relationship, solvency, market listing, or investment suitability.
- The fixture harness now calls the independent resolver and then compares its output with `expected_result`; the resolver itself never reads expected outputs.
- Harness `execution_status` is distinct from resolver `resolution_status`. A conformance test can be `PASS` while the resolver correctly returns `UNRESOLVED` for an abstention fixture.
- A temporary, explicit legacy crosswalk maps fixture expectation `UNVERIFIED` to resolver outcome `UNRESOLVED`; this does not amend the canonical methodology state catalog.

## Harness and test semantics

- `execution_status`: PASS, FAIL, BLOCKED, NOT_RUN.
- `resolution_status`: RESOLVED, UNRESOLVED, CONFLICTED (or NOT_ASSESSED if the resolver was not run).
- The conformance runner compares the computed status and identity key against the fixture expectation after resolution.

## Run

From repository root:

```bash
python -m unittest discover -s tests/identity_fixtures -p 'test_*.py' -v
```

All inputs and registry values used by these tests are synthetic. A passing suite is not live-network validation.
