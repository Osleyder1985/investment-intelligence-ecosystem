package com.iie.android.domain.model

/**
 * Canonical asset identity, independent of a particular network deployment.
 *
 * assetId is an IIE-owned stable identifier. Symbols are descriptive attributes,
 * never primary identity keys.
 */
data class Asset(
    val assetId: String,
    val canonicalKey: String,
    val name: String,
    val symbol: String,
    val assetType: String,
    val identityStatus: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
