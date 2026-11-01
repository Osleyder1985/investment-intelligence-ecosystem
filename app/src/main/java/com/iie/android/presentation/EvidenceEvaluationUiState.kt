package com.iie.android.presentation

import com.iie.android.domain.model.ObservationEvidenceEvaluation

data class EvidenceEvaluationUiState(
    val assetId: String,
    val isLoading: Boolean,
    val evaluation: ObservationEvidenceEvaluation? = null,
    val message: String? = null
)
