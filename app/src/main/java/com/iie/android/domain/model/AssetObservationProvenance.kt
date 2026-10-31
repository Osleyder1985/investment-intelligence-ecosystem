package com.iie.android.domain.model

/**
 * Provenance metadata for one locally persisted asset observation.
 * Freshness is not inferred here: it requires a source-specific policy.
 */
data class AssetObservationProvenance(
    val observationId: String,
    val sourceName: String,
    val providerType: String,
    val permittedUseStatus: String,
    val observationType: String,
    val observedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long,
    val status: String,
    val quality: String
)
