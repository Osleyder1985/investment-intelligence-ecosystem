# Distribution reconciliation and sell-pressure methodology

## Scope

The engine reconciles verified schedule entries and independently observed on-chain movements over an explicit time window. Amounts remain integer raw token units; all inputs for one calculation must use the same token contract and decimals.

## State distinctions

- **Scheduled unlock:** a source-published entitlement date and amount. It is not proof of on-chain claimability.
- **Observed unlock:** an independently verified chain event/state confirming that tokens became unlocked.
- **Claimed:** an independently evidenced claim event.
- **Transferred to beneficiary:** a verified token transfer from the distribution/vesting source to the beneficiary.
- **Sold:** requires separate market-trade evidence and is not inferred by this engine.

The movement enum preserves these states. `CLAIMED_AND_TRANSFERRED` represents a single observed withdrawal where the protocol event and token transfer were both validated; it must not be double-counted as two amounts.

## Reconciliation

For a requested window, the engine calculates scheduled unlocks, observed unlocks, claims, transfers to beneficiaries, and the delta between scheduled unlocks and observed claims/transfers. The delta is a reconciliation signal, not automatically a protocol deficit: schedules and event windows can have different semantics or incomplete coverage.

Recipient concentration is calculated from verified transfers to destination addresses. It does not identify beneficial owners, related wallets, exchange custody, or ultimate recipients.

## Potential sell pressure

Potential sell pressure is a transparent scenario envelope:

`observed transfers to beneficiaries × explicit sell-through fraction`

The low/high sell-through fractions are caller-supplied basis-point assumptions with a rationale and evidence reference. The result is not a prediction, observed sale volume, or probability. The engine reports both raw token units and percentages of the supplied circulating-supply denominator.

Do not use the output as an automatic valuation discount unless the sell-through assumptions, observation coverage, supply denominator, and market-liquidity model have been separately reviewed. Exchange deposits, order-book depth, market impact, vesting restrictions, wallet attribution, and actual execution evidence are outside this first domain-engine increment.

## Evidence and failure policy

All schedule entries and movements must be marked verified and include stable evidence IDs. Duplicate identities, invalid addresses/hashes, invalid amounts, unverified observations, invalid windows, missing assumption provenance, and invalid circulating supply cause abstention. The engine never silently upgrades quarantined evidence.
