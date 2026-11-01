package com.iie.android.data.local

import com.iie.android.data.local.entity.AssetResearchDossierSnapshotEntity
import com.iie.android.domain.model.AssetResearchDossier
import com.iie.android.domain.model.MarketResearchSection
import com.iie.android.domain.model.ResearchDeployment
import com.iie.android.domain.model.ResearchFieldStatus
import com.iie.android.domain.model.ResearchEvidence
import com.iie.android.domain.model.TokenomicsResearchSection
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
                .put("periodLowUsd", market.periodLowUsd ?: JSONObject.NULL)
                .put("quoteComparisonStatus", market.quoteComparisonStatus)
                .put("quoteDifferencePercent", market.quoteDifferencePercent ?: JSONObject.NULL)
                .put("quoteDifferenceThresholdPercent", market.quoteDifferenceThresholdPercent ?: JSONObject.NULL)
                .put("quoteComparisonPrimaryObservationId", market.quoteComparisonPrimaryObservationId ?: JSONObject.NULL)
                .put("quoteComparisonSecondaryObservationId", market.quoteComparisonSecondaryObservationId ?: JSONObject.NULL)
                .put("quoteComparedAtEpochMs", market.quoteComparedAtEpochMs ?: JSONObject.NULL))
            .put("fundamentals", dossier.fundamentals.name)
            .put("tokenomics", dossier.tokenomics.name)
            .put("tokenomicsEvidence", dossier.tokenomicsEvidence?.let { section ->
                JSONObject()
                    .put("circulatingSupply", encodeDoubleEvidence(section.circulatingSupply))
                    .put("totalSupply", encodeDoubleEvidence(section.totalSupply))
                    .put("maxSupply", encodeDoubleEvidence(section.maxSupply))
                    .put("marketCapUsd", encodeDoubleEvidence(section.marketCapUsd))
                    .put("fullyDilutedMarketCapUsd", encodeDoubleEvidence(section.fullyDilutedMarketCapUsd))
                    .put("annualInflationPercent", encodeDoubleEvidence(section.annualInflationPercent))
                    .put("emissionSchedule", encodeStringEvidence(section.emissionSchedule))
                    .put("unlockEvents", JSONArray().apply {
                        section.unlockEvents.forEach { put(encodeStringEvidence(it)) }
                    })
            } ?: JSONObject.NULL)
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
            periodLowUsd = nullableDouble(m, "periodLowUsd"),
            quoteComparisonStatus = m.optString("quoteComparisonStatus", "NOT_AVAILABLE"),
            quoteDifferencePercent = nullableDouble(m, "quoteDifferencePercent"),
            quoteDifferenceThresholdPercent = nullableDouble(m, "quoteDifferenceThresholdPercent"),
            quoteComparisonPrimaryObservationId = nullableString(m, "quoteComparisonPrimaryObservationId"),
            quoteComparisonSecondaryObservationId = nullableString(m, "quoteComparisonSecondaryObservationId"),
            quoteComparedAtEpochMs = nullableLong(m, "quoteComparedAtEpochMs")
        )
        val tokenomicsJson = root.optJSONObject("tokenomicsEvidence")
        val tokenomicsEvidence = tokenomicsJson?.let { json ->
            TokenomicsResearchSection(
                circulatingSupply = decodeDoubleEvidence(json.getJSONObject("circulatingSupply")),
                totalSupply = decodeDoubleEvidence(json.getJSONObject("totalSupply")),
                maxSupply = decodeDoubleEvidence(json.getJSONObject("maxSupply")),
                marketCapUsd = json.optJSONObject("marketCapUsd")?.let(::decodeDoubleEvidence)
                    ?: unknownDoubleEvidence("marketCapUsd"),
                fullyDilutedMarketCapUsd = json.optJSONObject("fullyDilutedMarketCapUsd")?.let(::decodeDoubleEvidence)
                    ?: unknownDoubleEvidence("fullyDilutedMarketCapUsd"),
                annualInflationPercent = decodeDoubleEvidence(json.getJSONObject("annualInflationPercent")),
                emissionSchedule = decodeStringEvidence(json.getJSONObject("emissionSchedule")),
                unlockEvents = (0 until json.getJSONArray("unlockEvents").length()).map { index ->
                    decodeStringEvidence(json.getJSONArray("unlockEvents").getJSONObject(index))
                }
            )
        }
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
            tokenomicsEvidence = tokenomicsEvidence,
            security = ResearchFieldStatus.valueOf(root.getString("security")),
            governance = ResearchFieldStatus.valueOf(root.getString("governance")),
            evidenceCount = root.getInt("evidenceCount"),
            generatedAtEpochMs = root.getLong("generatedAtEpochMs")
        )
    }

    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }


    private fun unknownDoubleEvidence(fieldKey: String) = ResearchEvidence<Double>(
        fieldKey = fieldKey,
        value = null,
        status = ResearchFieldStatus.UNKNOWN,
        sourceId = null,
        sourceName = null,
        sourceUrl = null,
        licenseReference = null,
        permittedUseStatus = "UNKNOWN",
        observedAtEpochMs = null,
        retrievedAtEpochMs = null,
        qualityStatus = "NOT_AVAILABLE"
    )

    private fun encodeDoubleEvidence(evidence: ResearchEvidence<Double>): JSONObject =
        encodeEvidence(evidence).put("value", evidence.value ?: JSONObject.NULL)

    private fun encodeStringEvidence(evidence: ResearchEvidence<String>): JSONObject =
        encodeEvidence(evidence).put("value", evidence.value ?: JSONObject.NULL)

    private fun encodeEvidence(evidence: ResearchEvidence<*>): JSONObject =
        JSONObject()
            .put("fieldKey", evidence.fieldKey)
            .put("status", evidence.status.name)
            .put("sourceId", evidence.sourceId ?: JSONObject.NULL)
            .put("sourceName", evidence.sourceName ?: JSONObject.NULL)
            .put("sourceUrl", evidence.sourceUrl ?: JSONObject.NULL)
            .put("licenseReference", evidence.licenseReference ?: JSONObject.NULL)
            .put("permittedUseStatus", evidence.permittedUseStatus)
            .put("observedAtEpochMs", evidence.observedAtEpochMs ?: JSONObject.NULL)
            .put("retrievedAtEpochMs", evidence.retrievedAtEpochMs ?: JSONObject.NULL)
            .put("qualityStatus", evidence.qualityStatus)
            .put("notes", JSONArray(evidence.notes))

    private fun decodeDoubleEvidence(json: JSONObject): ResearchEvidence<Double> =
        decodeEvidence(json, if (json.isNull("value")) null else json.getDouble("value"))

    private fun decodeStringEvidence(json: JSONObject): ResearchEvidence<String> =
        decodeEvidence(json, if (json.isNull("value")) null else json.getString("value"))

    private fun <T> decodeEvidence(json: JSONObject, value: T?): ResearchEvidence<T> =
        ResearchEvidence(
            fieldKey = json.getString("fieldKey"),
            value = value,
            status = ResearchFieldStatus.valueOf(json.getString("status")),
            sourceId = nullableString(json, "sourceId"),
            sourceName = nullableString(json, "sourceName"),
            sourceUrl = nullableString(json, "sourceUrl"),
            licenseReference = nullableString(json, "licenseReference"),
            permittedUseStatus = json.getString("permittedUseStatus"),
            observedAtEpochMs = nullableLong(json, "observedAtEpochMs"),
            retrievedAtEpochMs = nullableLong(json, "retrievedAtEpochMs"),
            qualityStatus = json.getString("qualityStatus"),
            notes = json.optJSONArray("notes")?.let { notes ->
                (0 until notes.length()).map { notes.getString(it) }
            } ?: emptyList()
        )

    private fun nullableString(json: JSONObject, key: String): String? =
        if (json.isNull(key)) null else json.getString(key)

    private fun nullableDouble(json: JSONObject, key: String): Double? =
        if (json.isNull(key)) null else json.getDouble(key)

    private fun nullableLong(json: JSONObject, key: String): Long? =
        if (json.isNull(key)) null else json.getLong(key)
}
