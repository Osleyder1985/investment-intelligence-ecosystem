package com.iie.android.domain.freshness

import com.iie.android.domain.model.FreshnessAssessment
import com.iie.android.domain.model.FreshnessEvaluation
import com.iie.android.domain.model.FreshnessPolicy
import com.iie.android.domain.model.FreshnessStatus
import com.iie.android.domain.model.TimestampBasis

/**
 * Deterministic policy evaluator. It never invents a default TTL and never
 * labels a record fresh when the required policy/timestamp is unavailable.
 */
class FreshnessEvaluator(
    policies: List<FreshnessPolicy>
) {
    private val policiesByKey = policies.associateBy {
        PolicyKey(it.sourceId, it.observationType, it.intendedUse)
    }

    init {
        require(policiesByKey.size == policies.size) {
            "Duplicate freshness policy for source, observation type, and intended use"
        }
    }

    fun evaluate(
        sourceId: String,
        observationType: String,
        intendedUse: String,
        observedAtEpochMs: Long?,
        retrievedAtEpochMs: Long,
        evaluatedAtEpochMs: Long
    ): FreshnessEvaluation {
        val policy = policiesByKey[PolicyKey(sourceId, observationType, intendedUse)]
            ?: return FreshnessEvaluation.NotEvaluated(
                FreshnessEvaluation.Reason.NO_MATCHING_POLICY
            )

        val referenceTime = when (policy.timestampBasis) {
            TimestampBasis.OBSERVED_AT -> observedAtEpochMs
            TimestampBasis.RETRIEVED_AT -> retrievedAtEpochMs
        } ?: return FreshnessEvaluation.NotEvaluated(
            FreshnessEvaluation.Reason.REQUIRED_TIMESTAMP_MISSING
        )

        if (retrievedAtEpochMs > evaluatedAtEpochMs ||
            referenceTime > evaluatedAtEpochMs ||
            (policy.timestampBasis == TimestampBasis.OBSERVED_AT &&
                referenceTime > retrievedAtEpochMs)
        ) {
            return FreshnessEvaluation.NotEvaluated(
                FreshnessEvaluation.Reason.INVALID_TIME_ORDER
            )
        }

        val ageMs = evaluatedAtEpochMs - referenceTime
        val status = when {
            ageMs > policy.maxAgeMs -> FreshnessStatus.STALE
            policy.maxAgeMs - ageMs <= policy.approachingExpiryWindowMs ->
                FreshnessStatus.APPROACHING_EXPIRY
            else -> FreshnessStatus.FRESH
        }

        return FreshnessEvaluation.Evaluated(
            FreshnessAssessment(
                status = status,
                policyId = policy.policyId,
                policyVersion = policy.version,
                intendedUse = policy.intendedUse,
                evaluatedAtEpochMs = evaluatedAtEpochMs,
                referenceTimestampEpochMs = referenceTime,
                ageMs = ageMs
            )
        )
    }

    private data class PolicyKey(
        val sourceId: String,
        val observationType: String,
        val intendedUse: String
    )
}
