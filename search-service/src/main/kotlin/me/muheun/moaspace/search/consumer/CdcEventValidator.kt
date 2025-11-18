package me.muheun.moaspace.search.consumer

import org.springframework.stereotype.Component

@Component
class CdcEventValidator {

    fun validate(event: CdcEvent): ValidationResult {
        val id = event.payload["id"]
        if (id == null) {
            return ValidationResult.invalid("missing_id")
        }

        if (id is String && id.isBlank()) {
            return ValidationResult.invalid("blank_id")
        }

        return ValidationResult.valid()
    }

    data class ValidationResult(val isValid: Boolean, val reason: String? = null) {
        companion object {
            fun valid() = ValidationResult(true)
            fun invalid(reason: String) = ValidationResult(false, reason)
        }
    }
}
