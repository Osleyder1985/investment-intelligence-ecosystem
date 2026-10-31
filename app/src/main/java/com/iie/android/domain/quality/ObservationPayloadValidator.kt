package com.iie.android.domain.quality

import com.iie.android.domain.model.ObservationPayloadContract
import com.iie.android.domain.model.ObservationPayloadValidation
import com.iie.android.domain.model.PayloadFieldType
import com.iie.android.domain.model.PayloadValidationIssue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Validates normalized JSON payloads against an explicit observation-type/version contract.
 * It does not infer contracts and does not claim domain-level truth or market-data validity.
 */
class ObservationPayloadValidator(
    contracts: List<ObservationPayloadContract>,
    private val json: Json = Json
) {
    private val contractsByKey = contracts.associateBy {
        ContractKey(it.observationType, it.schemaVersion)
    }

    init {
        require(contractsByKey.size == contracts.size) {
            "Duplicate payload contract for observation type and schema version"
        }
    }

    fun validate(
        observationType: String,
        schemaVersion: Int,
        payloadJson: String
    ): ObservationPayloadValidation {
        val contract = contractsByKey[ContractKey(observationType, schemaVersion)]
            ?: return ObservationPayloadValidation.NotEvaluated(
                ObservationPayloadValidation.NotEvaluated.Reason.NO_MATCHING_CONTRACT
            )

        val root = try {
            json.parseToJsonElement(payloadJson)
        } catch (_: SerializationException) {
            return invalid(
                contract,
                PayloadValidationIssue(
                    PayloadValidationIssue.Code.MALFORMED_JSON,
                    "$",
                    "Payload is not valid JSON."
                )
            )
        } catch (_: IllegalArgumentException) {
            return invalid(
                contract,
                PayloadValidationIssue(
                    PayloadValidationIssue.Code.MALFORMED_JSON,
                    "$",
                    "Payload is not valid JSON."
                )
            )
        }

        if (!matches(root, contract.requiredRootType)) {
            return invalid(
                contract,
                PayloadValidationIssue(
                    PayloadValidationIssue.Code.ROOT_TYPE_MISMATCH,
                    "$",
                    "Payload root does not match the contract's required root type."
                )
            )
        }

        val rootObject = root as JsonObject

        val issues = mutableListOf<PayloadValidationIssue>()
        val declaredFields = contract.fields.associateBy { it.name }

        contract.fields.forEach { field ->
            val value = rootObject[field.name]
            when {
                value == null && field.required -> issues += PayloadValidationIssue(
                    PayloadValidationIssue.Code.MISSING_REQUIRED_FIELD,
                    "$.${field.name}",
                    "Required field is missing."
                )
                value != null && !matches(value, field.type) -> issues += PayloadValidationIssue(
                    PayloadValidationIssue.Code.FIELD_TYPE_MISMATCH,
                    "$.${field.name}",
                    "Field type does not match the declared contract."
                )
            }
        }

        if (!contract.allowAdditionalFields) {
            rootObject.keys.filterNot { it in declaredFields }.sorted().forEach { name ->
                issues += PayloadValidationIssue(
                    PayloadValidationIssue.Code.UNEXPECTED_FIELD,
                    "$.$name",
                    "Field is not declared by this versioned contract."
                )
            }
        }

        return if (issues.isEmpty()) {
            ObservationPayloadValidation.Valid(observationType, schemaVersion)
        } else {
            ObservationPayloadValidation.Invalid(observationType, schemaVersion, issues)
        }
    }

    private fun matches(element: JsonElement, type: PayloadFieldType): Boolean = when (type) {
        PayloadFieldType.STRING -> element is JsonPrimitive && element.isString
        PayloadFieldType.BOOLEAN -> (element as? JsonPrimitive)?.booleanOrNull != null
        PayloadFieldType.NUMBER -> (element as? JsonPrimitive)?.let {
            !it.isString && it.doubleOrNull != null
        } == true
        PayloadFieldType.OBJECT -> element is JsonObject
        PayloadFieldType.ARRAY -> element is JsonArray
    }

    private fun invalid(
        contract: ObservationPayloadContract,
        issue: PayloadValidationIssue
    ) = ObservationPayloadValidation.Invalid(
        contract.observationType,
        contract.schemaVersion,
        listOf(issue)
    )

    private data class ContractKey(
        val observationType: String,
        val schemaVersion: Int
    )
}
