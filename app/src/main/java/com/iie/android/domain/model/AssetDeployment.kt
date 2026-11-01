package com.iie.android.domain.model

/**
 * A network-specific native asset or token representation.
 *
 * deploymentKey is a deterministic, unique IIE key. For native assets it should
 * include the network and canonical asset ID; for tokens it should include the
 * network, namespace and normalized contract/address reference.
 */
data class AssetDeployment(
    val deploymentId: String,
    val deploymentKey: String,
    val assetId: String,
    val networkId: String,
    val representationKind: String,
    val namespace: String,
    val reference: String?,
    val decimals: Int?,
    val validFromEpochMs: Long?,
    val validUntilEpochMs: Long?
)
