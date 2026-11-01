package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.AssetDeploymentWithNetwork
import com.iie.android.data.local.entity.AssetDeploymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDeploymentDao {
    @Query("SELECT * FROM asset_deployments WHERE assetId = :assetId ORDER BY networkId, deploymentId")
    fun observeDeploymentsForAsset(assetId: String): Flow<List<AssetDeploymentEntity>>

    @Query(
        """
        SELECT d.deploymentId AS deploymentId,
               n.name AS networkName,
               d.representationKind AS representationKind,
               d.namespace AS namespace,
               d.reference AS reference,
               d.decimals AS decimals
        FROM asset_deployments AS d
        INNER JOIN networks AS n ON n.networkId = d.networkId
        WHERE d.assetId = :assetId
        ORDER BY n.name COLLATE NOCASE, d.deploymentId
        """
    )
    fun observeSummariesForAsset(assetId: String): Flow<List<AssetDeploymentWithNetwork>>

    @Query("SELECT * FROM asset_deployments WHERE deploymentId = :deploymentId LIMIT 1")
    suspend fun getDeployment(deploymentId: String): AssetDeploymentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDeployment(deployment: AssetDeploymentEntity)

    @androidx.room.Update
    suspend fun updateDeployment(deployment: AssetDeploymentEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDeployments(deployments: List<AssetDeploymentEntity>)
}
