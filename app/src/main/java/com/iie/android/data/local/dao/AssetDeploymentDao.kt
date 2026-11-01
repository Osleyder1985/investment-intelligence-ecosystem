package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.AssetDeploymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDeploymentDao {
    @Query("SELECT * FROM asset_deployments WHERE assetId = :assetId ORDER BY networkId, deploymentId")
    fun observeDeploymentsForAsset(assetId: String): Flow<List<AssetDeploymentEntity>>

    @Query("SELECT * FROM asset_deployments WHERE deploymentId = :deploymentId LIMIT 1")
    suspend fun getDeployment(deploymentId: String): AssetDeploymentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDeployment(deployment: AssetDeploymentEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDeployments(deployments: List<AssetDeploymentEntity>)
}
