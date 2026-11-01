package com.iie.android.domain.model

/**
 * A time-bounded observation about an asset or one of its deployments.
 * valueJson stores a normalized, versioned JSON value; raw provider payloads
 * should be retained as files only when licensing and storage policy permit.
 */
data class AssetObservation(
    val observationId: String,
    val assetId: String,
    val deploymentId: String?,
    val sourceId: String,
    val observationType: String,
    val observedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long,
    val status: String,
    val quality: String,
    val valueSchemaVersion: Int,
    val valueJson: String,
    val payloadSha256: String?,
    val licenseReference: String?
)
