package com.iie.android.domain.model

/** Read model for the network-specific representation shown in the asset detail screen. */
data class AssetDeploymentSummary(
    val deploymentId: String,
    val networkName: String,
    val representationKind: String,
    val namespace: String,
    val reference: String?,
    val decimals: Int?
)
