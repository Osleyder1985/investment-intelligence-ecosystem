package com.iie.android.data.local.projection

/**
 * Read model joining an asset observation with the source registry.
 * It intentionally excludes raw payload JSON from presentation.
 */
data class AssetObservationProvenanceRow(
    val observationId: String,
    val sourceDisplayName: String,
    val sourceProviderType: String,
    val sourcePermittedUseStatus: String,
    val observationType: String,
    val observedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long,
    val status: String,
    val quality: String
)
