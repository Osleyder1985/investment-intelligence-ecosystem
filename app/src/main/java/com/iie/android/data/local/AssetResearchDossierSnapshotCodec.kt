package com.iie.android.data.local

import com.iie.android.data.local.entity.AssetResearchDossierSnapshotEntity
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.MarketResearchSection
import com.iie.android.domain.model.ResearchDeployment
import com.iie.android.domain.model.ResearchFieldStatus
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject

/** Versioned JSON codec with integrity verification for locally stored dossier snapshots. */
object AssetResearchDossierSnapshotCodec {
    const val SCHEMA_VERSION = 1

    fun encode(dossier: AssetResearchDossier): String {
        val deployments = JSONArray()
        dossier.deployments.forEach {
            deployments.put(JSONObject()
                .put("networkName", it.networkName)
                .put("representationKind", it.representationKind)
                .put("namespace", it.namespace)
                .put("reference", it.reference ?: JSONObject.NULL)
                .put("decimals", it.decimals ?: JSONObject.NULL))
        }
        val market = dossier.market
        return JSONObject()
            .put("schema", "iie.asset-research-dossier.v1")
            .put("assetId", dossier.assetId)
            .put("assetName", dossier.assetName)
            .put("symbol", dossier.symbol)
            .put("identityStatus", dossier.identityStatus)
            .put("identityAssessment", dossier.identityAssessment.name)
            .put("deployments", deployments)
            .put("market", JSONObject()
                .put("quoteStatus", market.quoteStatus.name)
                .put("priceUsd", market.priceUsd ?: JSONObject.NULL)
                .put("provider", market.provider ?: JSONObject.NULL)
                .put("providerUpdatedAtEpochMs", market.providerUpdatedAtEpochMs ?: JSONObject.NULL)
                .put("retrievedAtEpochMs", market.retrievedAtEpochMs ?: JSONObject.NULL)
                .put("freshnessStatus", market.freshnessStatus)
                .put("historyPointCount", market.historyPointCount)
                .put("latestDailyReturnPercent", market.latestDailyReturnPercent ?: JSONObject.NULL)
                .put("periodReturnPercent", market.periodReturnPercent ?: JSONObject.NULL)
                .put("periodHighUsd", market.periodHighUsd ?: JSONObject.NULL)
                .put("periodLowUsd", market.periodLowUsd ?: JSONObject.NULL))
            .put("fundamentals", dossier.fundamentals.name)
            .put("tokenomics", dossier.tokenomics.name)
            .put("security", dossier.security.name)
            .put("governance", dossier.governance.name)
            .put("evidenceCount", dossier.evidenceCount)
            .put("generatedAtEpochMs", dossier.generatedAtEpochMs)
            .toString()
    }

    fun decode(entity: AssetResearchDossierSnapshotEntity): AssetResearchDossier {
        require(entity.schemaVersion == SCHEMA_VERSION) { "Unsupported dossier snapshot schema" }
        require(sha256(entity.payloadJson) == entity.payloadSha256) { "Dossier snapshot integrity check failed" }
        val root = JSONObject(entity.payloadJson)
        require(root.optString("schema") == "iie.asset-research-dossier.v1")
        val deploymentJson = root.getJSONArray("deployments")
        val deployments = (0 until deploymentJson.length()).map { index ->
            val item = deploymentJson.getJSONObject(index)
            ResearchDeployment(
                networkName = item.getString("networkName"),
                representationKind = item.getString("representationKind"),
                namespace = item.getString("namespace"),
                reference = item.optString("reference").takeUnless { it == "null" },
                decimals = if (item.isNull("decimals")) null else item.getInt("decimals")
            )
        }
        val m = root.getJSONObject("market")
        val market = MarketResearchSection(
            quoteStatus = ResearchFieldStatus.valueOf(m.getString("quoteStatus")),
            priceUsd = nullableDouble(m, "priceUsd"),
            provider = nullableString(m, "provider"),
            providerUpdatedAtEpochMs = nullableLong(m, "providerUpdatedAtEpochMs"),
            retrievedAtEpochMs = nullableLong(m, "retrievedAtEpochMs"),
            freshnessStatus = m.getString("freshnessStatus"),
            historyPointCount = m.getInt("historyPointCount"),
            latestDailyReturnPercent = nullableDouble(m, "latestDailyReturnPercent"),
            periodReturnPercent = nullableDouble(m, "periodReturnPercent"),
            periodHighUsd = nullableDouble(m, "periodHighUsd"),
            periodLowUsd = nullableDouble(m, "periodLowUsd")
        )
        return AssetResearchDossier(
            assetId = root.getString("assetId"),
            assetName = root.getString("assetName"),
            symbol = root.getString("symbol"),
            identityStatus = root.getString("identityStatus"),
            identityAssessment = ResearchFieldStatus.valueOf(root.getString("identityAssessment")),
            deployments = deployments,
            market = market,
            fundamentals = ResearchFieldStatus.valueOf(root.getString("fundamentals")),
            tokenomics = ResearchFieldStatus.valueOf(root.getString("tokenomics")),
            security = ResearchFieldStatus.valueOf(root.getString("security")),
            governance = ResearchFieldStatus.valueOf(root.getString("governance")),
            evidenceCount = root.getInt("evidenceCount"),
            generatedAtEpochMs = root.getLong("generatedAtEpochMs")
        )
    }

    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun nullableString(json: JSONObject, key: String): String? =
        if (json.isNull(key)) null else json.getString(key)

    private fun nullableDouble(json: JSONObject, key: String): Double? =
        if (json.isNull(key)) null else json.getDouble(key)

    private fun nullableLong(json: JSONObject, key: String): Long? =
        if (json.isNull(key)) null else json.getLong(key)
}
