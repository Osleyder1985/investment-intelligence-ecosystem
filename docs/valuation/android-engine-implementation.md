# Android fundamental valuation engine — implementation status

**Implementation branch:** `feat/fundamental-valuation-engine`  
**Status:** Initial domain engine and asset-detail disclosure implemented; empirical calibration and provider-backed model inputs remain pending.

## Implemented domain components

- `FundamentalValuationModels`: deterministic calculation of NAV, holder-attributable DCF, token-capture comparables, monetary-demand scenarios and net redemption.
- `FundamentalValuationEngine`: comparison of a fair-value range with a comparable market reference.
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

## Deliberately not enabled

- No universal percentage thresholds for cheap / very cheap.
- No `EXTREME_UNDERVALUATION` classification. This remains disabled until historical calibration, walk-forward evaluation and independent out-of-sample validation are completed.
- No buy/sell recommendation.

## Remaining work before production use

- Implement evidence-backed typology assignment and method selection per asset.
- Connect permitted, attributable fundamental/on-chain data sources and dated unlock/emission schedules.
- Persist model inputs, parameters, outputs and immutable versioned valuation snapshots.
- Integrate the independent risk-veto engine and executable market-depth data.
- Validate model outputs by typology and regime, including survivorship bias, costs and false-discovery controls.
- Complete Android build, unit tests and UI review before merging.
