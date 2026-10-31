package com.iie.android.domain.quality

import com.iie.android.domain.model.ObservationPayloadContract
import com.iie.android.domain.model.ObservationPayloadValidation
import com.iie.android.domain.model.PayloadField
import com.iie.android.domain.model.PayloadFieldType
import com.iie.android.domain.model.PayloadValidationIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationPayloadValidatorTest {
    private val contract = ObservationPayloadContract(
        observationType = "IDENTITY_FIXTURE",
        schemaVersion = 1,
        fields = listOf(
            PayloadField("recordType", PayloadFieldType.STRING),
            PayloadField("marketData", PayloadFieldType.BOOLEAN)
        )
    )
    private val validator = ObservationPayloadValidator(listOf(contract))

    @Test
    fun acceptsKnownIdentityFixturePayload() {
        assertTrue(
            validator.validate(
                "IDENTITY_FIXTURE",
                1,
                """{"recordType":"synthetic_demo_fixture","marketData":false}"""
            ) is ObservationPayloadValidation.Valid
        )
    }

    @Test
    fun rejectsMalformedJson() {
        val result = validator.validate("IDENTITY_FIXTURE", 1, """{"recordType":""")
        assertIssue(result, PayloadValidationIssue.Code.MALFORMED_JSON)
    }

    @Test
    fun rejectsNonObjectRoot() {
        val result = validator.validate("IDENTITY_FIXTURE", 1, """["fixture"]""")
        assertIssue(result, PayloadValidationIssue.Code.ROOT_TYPE_MISMATCH)
    }

    @Test
    fun rejectsMissingRequiredField() {
        val result = validator.validate(
            "IDENTITY_FIXTURE",
            1,
            """{"recordType":"synthetic_demo_fixture"}"""
        )
        assertIssue(result, PayloadValidationIssue.Code.MISSING_REQUIRED_FIELD)
    }

    @Test
    fun rejectsWrongFieldType() {
        val result = validator.validate(
            "IDENTITY_FIXTURE",
            1,
            """{"recordType":"synthetic_demo_fixture","marketData":"false"}"""
        )
        assertIssue(result, PayloadValidationIssue.Code.FIELD_TYPE_MISMATCH)
    }

    @Test
    fun rejectsUndeclaredFieldsByDefault() {
        val result = validator.validate(
            "IDENTITY_FIXTURE",
            1,
            """{"recordType":"synthetic_demo_fixture","marketData":false,"price":1}"""
        )
        assertIssue(result, PayloadValidationIssue.Code.UNEXPECTED_FIELD)
    }

    @Test
    fun doesNotEvaluateUnknownTypeOrVersion() {
        val result = validator.validate("MARKET_PRICE", 1, """{"price":1}""")
        assertEquals(
            ObservationPayloadValidation.NotEvaluated.Reason.NO_MATCHING_CONTRACT,
            (result as ObservationPayloadValidation.NotEvaluated).reason
        )
    }

    @Test
    fun rejectsDuplicateContractScope() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            ObservationPayloadValidator(listOf(contract, contract))
        }
    }

    private fun assertIssue(
        result: ObservationPayloadValidation,
        expected: PayloadValidationIssue.Code
    ) {
        assertTrue(result is ObservationPayloadValidation.Invalid)
        assertTrue(
            (result as ObservationPayloadValidation.Invalid).issues.any { it.code == expected }
        )
    }
}
