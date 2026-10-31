package com.iie.android.domain.quality

import com.iie.android.domain.model.ObservationPayloadContract
import com.iie.android.domain.model.PayloadField
import com.iie.android.domain.model.PayloadFieldType

/**
 * Explicit starter contracts shipped with the Android MVP.
 * These contracts describe local fixtures only; they are not market-data schemas.
 */
object ObservationPayloadContracts {
    fun initial(): List<ObservationPayloadContract> = listOf(
        ObservationPayloadContract(
            observationType = "IDENTITY_FIXTURE",
            schemaVersion = 1,
            fields = listOf(
                PayloadField("recordType", PayloadFieldType.STRING),
                PayloadField("marketData", PayloadFieldType.BOOLEAN)
            ),
            allowAdditionalFields = false
        )
    )
}
