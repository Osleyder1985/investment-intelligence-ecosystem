package com.iie.android.data.local

/** Room projection joining a deployment to its network display name. */
data class AssetDeploymentWithNetwork(
    val deploymentId: String,
    val networkName: String,
    val representationKind: String,
    val namespace: String,
    val reference: String?,
    val decimals: Int?
)
