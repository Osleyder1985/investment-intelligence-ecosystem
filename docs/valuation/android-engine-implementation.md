# Android fundamental valuation engine — implementation status

**Implementation branch:** `feat/fundamental-valuation-engine`  
**Status:** Initial domain engine and asset-detail disclosure implemented; empirical calibration and provider-backed model inputs remain pending.

## Implemented domain components

- `FundamentalValuationModels`: deterministic calculation of NAV, holder-attributable DCF, token-capture comparables, monetary-demand scenarios and net redemption.
- `FundamentalValuationEngine`: comparison of a fair-value range with a comparable market reference.
- `AssetValuationMethodSelector`: explicit rights-based method eligibility; no rights are inferred from ticker, price or market capitalization.
- `AssetValuationResearchAdapter`: joins the research dossier to reviewed rights evidence and validates identity and fresh market quote gates.
- `FundamentalValuationOrchestrator`: executes only eligible model requests, checks asset/scope/currency/timestamp comparability, consolidates independent ranges conservatively, and passes the result to the classification engine.
- `TokenomicsSupplyEvidenceAdapter`: validates circulating/total/max supply field provenance, intended-use licence status, observation/retrieval timestamps, supply relations and cross-field timestamp comparability. It emits verified supply facts and source references only; it does not infer demand or fair value.
- `FairValueEstimate`: typed output preserving method, range kind, timestamp, evidence references and assumptions.

## Non-negotiable controls

1. NAV requires a verified token-holder claim on the assets.
2. DCF requires holder-attributable cash-flow rights; protocol revenue alone is not attributed to a token.
3. Comparables require a verified token-capture mechanism and homogeneous metrics/multiples.
4. Monetary-demand output is explicitly a scenario envelope; no scenario probabilities are invented.
5. Redemption requires verified holder eligibility and operational redemption.
6. Unknown, conflicted, non-finite, non-positive or incomplete inputs cause abstention.
7. Market comparison requires the same economic object/unit and an explicit timestamp tolerance.
8. A potential discount is not promoted to model-supported undervaluation unless executable buy price, liquidity/depth, execution costs, sensitivity, alternative methods and risk-veto gates pass.

## Android presentation

The asset detail view now exposes a dedicated **Valoración fundamental** section. It displays available market-cap and supply facts and explicitly reports that fair value is not estimable while typology-specific rights, value capture, emissions/unlocks, execution liquidity and risk evidence are missing. It does not fabricate a target price.

## Tokenomics evidence bridge

- Circulating supply and total supply are mandatory, separately evidenced facts for a verified supply envelope; maximum supply is included only when its evidence is explicitly known and accepted.
- Missing/unknown optional maximum supply does not block the two mandatory facts, but conflicted maximum-supply evidence is never silently discarded.
- Every accepted field must pass source, URL, licence, permitted-use and timestamp quality gates. Cross-field observations must fit the configured skew window.
- The adapter deliberately does not turn market cap/FDV into monetary demand, or label circulating-to-total supply as effective/free-float supply.

## Orchestration and consensus policy

- A single valid model estimate can be evaluated on its own, retaining its original range kind (including an explicitly uncalibrated scenario envelope).
- Multi-model consensus requires at least two model-uncertainty ranges for the same asset, valuation object, unit, currency and exact valuation timestamp, with sufficient evidence and pairwise-disjoint evidence-reference sets.
- Multi-model output is the conservative envelope (minimum lower bound / maximum upper bound), not a mean, weighted average, probability or confidence interval.
- Incompatible or dependent estimates abstain. Missing model requests do not trigger implicit model execution.
- The orchestrator does not emit an extreme-cheapness label or calibrated margin of safety.

## Deliberately not enabled

- No universal percentage thresholds for cheap / very cheap.
- No `EXTREME_UNDERVALUATION` classification. This remains disabled until historical calibration, walk-forward evaluation and independent out-of-sample validation are completed.
- No buy/sell recommendation.

## Remaining work before production use

- Connect provider-backed, permitted and attributable fundamental/on-chain data sources to typed NAV, holder-attributable DCF, token-capture and redemption inputs.
- Supply scenario demand assumptions from separately sourced evidence; do not derive them from market capitalization or the current token price.
- Connect permitted, attributable fundamental/on-chain data sources and dated unlock/emission schedules.
- Persist model inputs, parameters, outputs and immutable versioned valuation snapshots.
- Integrate the independent risk-veto engine and executable market-depth data.
- Validate model outputs by typology and regime, including survivorship bias, costs and false-discovery controls.
- Complete Android build, unit tests and UI review before merging.
