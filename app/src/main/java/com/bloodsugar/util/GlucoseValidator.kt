package com.bloodsugar.util

import androidx.annotation.StringRes
import com.bloodsugar.R

object GlucoseValidator {

    const val MIN_VALUE = 1.0f
    const val MAX_VALUE = 33.3f

    sealed class ValidationResult {
        data class Success(val value: Float) : ValidationResult()
        data class Error(@StringRes val messageResId: Int, val formatArgs: List<Float> = emptyList()) : ValidationResult()
    }

    fun validate(input: String): ValidationResult {
        val value = input.toFloatOrNull()
            ?: return ValidationResult.Error(R.string.error_invalid_number)

        return when {
            value < MIN_VALUE -> ValidationResult.Error(R.string.error_below_min, listOf(MIN_VALUE))
            value > MAX_VALUE -> ValidationResult.Error(R.string.error_above_max, listOf(MAX_VALUE))
            else -> ValidationResult.Success(value)
        }
    }
}
