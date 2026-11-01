package com.iie.android.domain.model

data class PayloadValidationIssue(
    val code: Code,
    val path: String,
    val detail: String
) {
    enum class Code {
        MALFORMED_JSON,
        ROOT_TYPE_MISMATCH,
        MISSING_REQUIRED_FIELD,
        FIELD_TYPE_MISMATCH,
        UNEXPECTED_FIELD
    }
}

sealed interface ObservationPayloadValidation {
    data class Valid(
        val observationType: String,
        val schemaVersion: Int
    ) : ObservationPayloadValidation

    data class Invalid(
        val observationType: String,
        val schemaVersion: Int,
        val issues: List<PayloadValidationIssue>
    ) : ObservationPayloadValidation

    data class NotEvaluated(
        val reason: Reason
    ) : ObservationPayloadValidation {
        enum class Reason {
            NO_MATCHING_CONTRACT
        }
    }
}
