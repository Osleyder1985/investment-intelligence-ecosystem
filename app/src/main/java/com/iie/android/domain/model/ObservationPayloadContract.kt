package com.iie.android.domain.model

/** JSON value kinds supported by the first payload-contract implementation. */
enum class PayloadFieldType {
    STRING,
    BOOLEAN,
    NUMBER,
    OBJECT,
    ARRAY
}

data class PayloadField(
    val name: String,
    val type: PayloadFieldType,
    val required: Boolean = true
) {
    init {
        require(name.isNotBlank())
    }
}

/**
 * A deliberately small, versioned contract for a normalized observation payload.
 * Nested field constraints are not implied by OBJECT/ARRAY in this initial version.
 */
data class ObservationPayloadContract(
    val observationType: String,
    val schemaVersion: Int,
    val requiredRootType: PayloadFieldType = PayloadFieldType.OBJECT,
    val fields: List<PayloadField>,
    val allowAdditionalFields: Boolean = false
) {
    init {
        require(observationType.isNotBlank())
        require(schemaVersion > 0)
        require(requiredRootType == PayloadFieldType.OBJECT) {
            "Root-object contracts are required while field-level validation is defined for objects only"
        }
        require(fields.map { it.name }.distinct().size == fields.size) {
            "Payload contract field names must be unique"
        }
    }
}
