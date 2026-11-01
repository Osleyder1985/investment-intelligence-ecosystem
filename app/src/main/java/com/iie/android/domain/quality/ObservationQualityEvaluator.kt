package com.iie.android.domain.quality

import com.iie.android.domain.model.ObservationQualityAssessment
import com.iie.android.domain.model.ObservationQualityEvaluation
import com.iie.android.domain.model.ObservationQualityFinding
import com.iie.android.domain.model.ObservationQualityFindingCode
import com.iie.android.domain.model.ObservationQualityInput
import com.iie.android.domain.model.ObservationQualityOutcome
import com.iie.android.domain.model.ObservationQualityPolicy
import com.iie.android.domain.model.ObservationQualitySeverity

/**
 * Deterministic metadata-level quality checks. This evaluator does not parse
 * provider payloads or establish the factual correctness of their contents.
 */
class ObservationQualityEvaluator(
    policies: List<ObservationQualityPolicy>
) {
    private val policiesByKey = policies.associateBy {
        PolicyKey(it.sourceId, it.observationType, it.intendedUse)
    }

    init {
        require(policiesByKey.size == policies.size) {
            "Duplicate observation quality policy for source, observation type, and intended use"
        }
    }

    fun evaluate(input: ObservationQualityInput): ObservationQualityEvaluation {
        val observation = input.observation
        val policy = policiesByKey[
            PolicyKey(observation.sourceId, observation.observationType, input.intendedUse)
        ] ?: return ObservationQualityEvaluation.NotEvaluated(
            ObservationQualityEvaluation.Reason.NO_MATCHING_POLICY
        )

        val findings = mutableListOf<ObservationQualityFinding>()

        fun add(
            code: ObservationQualityFindingCode,
            severity: ObservationQualitySeverity,
            detail: String
        ) {
            findings += ObservationQualityFinding(code, severity, detail)
        }

        if (observation.observationId.isBlank() ||
            observation.assetId.isBlank() ||
            observation.sourceId.isBlank() ||
            observation.observationType.isBlank() ||
            input.sourcePermittedUseStatus.isBlank() ||
            input.intendedUse.isBlank()
        ) {
            add(
                ObservationQualityFindingCode.MISSING_REQUIRED_METADATA,
                ObservationQualitySeverity.BLOCKING,
                "One or more required observation, source-use, or intended-use identifiers are blank."
            )
        }

        if (input.evaluatedAtEpochMs < 0L ||
            observation.retrievedAtEpochMs < 0L ||
            (observation.observedAtEpochMs != null && observation.observedAtEpochMs < 0L)
        ) {
            add(
                ObservationQualityFindingCode.INVALID_TIMESTAMP,
                ObservationQualitySeverity.BLOCKING,
                "Evaluation, retrieval, and optional observation timestamps must be non-negative epoch milliseconds."
            )
        } else {
            if (observation.retrievedAtEpochMs > input.evaluatedAtEpochMs) {
                add(
                    ObservationQualityFindingCode.INVALID_TIMESTAMP,
                    ObservationQualitySeverity.BLOCKING,
                    "The retrieval timestamp is later than the evaluation time."
                )
            }
            if (observation.observedAtEpochMs != null &&
                observation.observedAtEpochMs > observation.retrievedAtEpochMs
            ) {
                add(
                    ObservationQualityFindingCode.OBSERVATION_AFTER_RETRIEVAL,
                    ObservationQualitySeverity.BLOCKING,
                    "The observation timestamp is later than the retrieval timestamp."
                )
            }
        }

        if (observation.valueSchemaVersion != policy.expectedValueSchemaVersion) {
            add(
                ObservationQualityFindingCode.UNEXPECTED_VALUE_SCHEMA_VERSION,
                ObservationQualitySeverity.BLOCKING,
                "The observation value schema version does not match the policy's expected version."
            )
        }

        if (observation.valueJson.isBlank()) {
            add(
                ObservationQualityFindingCode.EMPTY_VALUE_PAYLOAD,
                ObservationQualitySeverity.BLOCKING,
                "The normalized observation value is empty."
            )
        }

        if (input.sourcePermittedUseStatus !in policy.allowedSourcePermittedUseStatuses) {
            add(
                ObservationQualityFindingCode.SOURCE_USE_NOT_PERMITTED,
                ObservationQualitySeverity.BLOCKING,
                "The source's declared permitted-use status is not allowed by this policy."
            )
        }

        when {
            observation.quality !in policy.acceptedQualityValues -> add(
                ObservationQualityFindingCode.UNKNOWN_QUALITY_VALUE,
                ObservationQualitySeverity.BLOCKING,
                "The observation quality value is not recognized by this policy."
            )
            observation.quality == "UNVERIFIED" && policy.requireVerifiedEvidence -> add(
                ObservationQualityFindingCode.EVIDENCE_VERIFICATION_REQUIRED,
                ObservationQualitySeverity.BLOCKING,
                "The policy requires verified evidence, but this observation is unverified."
            )
            observation.quality == "UNVERIFIED" -> add(
                ObservationQualityFindingCode.EVIDENCE_UNVERIFIED,
                ObservationQualitySeverity.WARNING,
                "The observation is explicitly unverified; this is not proof that its contents are false."
            )
        }

        val hash = observation.payloadSha256
        if (hash != null && !SHA256_PATTERN.matches(hash)) {
            add(
                ObservationQualityFindingCode.INVALID_PAYLOAD_HASH,
                ObservationQualitySeverity.BLOCKING,
                "A supplied payload hash is not a 64-character hexadecimal SHA-256 value."
            )
        }

        if (observation.licenseReference != null && observation.licenseReference.isBlank()) {
            add(
                ObservationQualityFindingCode.BLANK_LICENSE_REFERENCE,
                ObservationQualitySeverity.WARNING,
                "A license reference is present but blank; licensing admissibility is not established."
            )
        }

        val outcome = when {
            findings.any { it.severity == ObservationQualitySeverity.BLOCKING } ->
                ObservationQualityOutcome.BLOCKED
            findings.isNotEmpty() -> ObservationQualityOutcome.PASS_WITH_LIMITATIONS
            else -> ObservationQualityOutcome.PASS
        }

        return ObservationQualityEvaluation.Evaluated(
            ObservationQualityAssessment(
                policyId = policy.policyId,
                policyVersion = policy.version,
                intendedUse = policy.intendedUse,
                evaluatedAtEpochMs = input.evaluatedAtEpochMs,
                outcome = outcome,
                findings = findings.toList()
            )
        )
    }

    private data class PolicyKey(
        val sourceId: String,
        val observationType: String,
        val intendedUse: String
    )

    private companion object {
        val SHA256_PATTERN = Regex("^[a-fA-F0-9]{64}$")
    }
}
