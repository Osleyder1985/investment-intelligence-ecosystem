# Independent identity resolver — initial offline slice

## Status

PROPOSED IMPLEMENTATION / SYNTHETIC TESTS ONLY / NOT APPROVED FOR PRODUCTION OR INVESTMENT DECISIONS.

## What is implemented in this branch

- A pure resolver API that receives profile, network context, observations, and an explicit versioned registry.
- Canonical EVM representation keys are derived from the raw 20-byte address and a registry-resolved network key.
- Solana public keys are decoded and required to be canonical Base58 encodings of exactly 32 bytes.
- Solana mint owner program IDs are checked against the registry; free-text `token_program` labels are ignored.
- Missing checkpoints, unregistered networks, malformed identifiers, and insufficient account/contract evidence produce unresolved outcomes; contradictory observations or ambiguous registry matches produce conflicted outcomes.
- Results include resolver, registry and profile versions, evidence references, diagnostics, and deterministic canonical JSON/digest helpers.

## Explicit limitations

- The registry contains synthetic network identities and a synthetic Solana owner-program mapping only.
- The resolver does not connect to RPC, validate chain consensus/finality, establish canonicality of a live checkpoint, or verify a genesis hash against a network.
- EVM code hash is format-checked only. No bytecode is supplied, so the hash is not recomputed or authenticated.
- Solana mint account state is supplied as an observation. The resolver does not independently fetch or verify account state.
- This identifies a network-scoped representation, not an economic asset, issuer, backing claim, bridge relationship, solvency, market listing, or investment suitability.
- The existing fixture harness remains a separate conformance runner in this increment. Resolver output does not consume `expected_result`; tests compare independently derived output to explicit expectations.

## Run

From repository root:

```bash
python -m unittest discover -s tests/identity_fixtures -p 'test_*.py' -v
```

All inputs and registry values used by these tests are synthetic. A passing suite is not live-network validation.
