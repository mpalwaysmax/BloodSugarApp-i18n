package com.bloodsugar.util

import org.junit.Assert.*
import org.junit.Test

class GlucoseValidatorTest {

    @Test
    fun `valid value returns Success`() {
        val result = GlucoseValidator.validate("5.5")
        assertTrue(result is GlucoseValidator.ValidationResult.Success)
        assertEquals(5.5f, (result as GlucoseValidator.ValidationResult.Success).value)
    }

    @Test
    fun `minimum boundary value returns Success`() {
        val result = GlucoseValidator.validate("1.0")
        assertTrue(result is GlucoseValidator.ValidationResult.Success)
    }

    @Test
    fun `maximum boundary value returns Success`() {
        val result = GlucoseValidator.validate("33.3")
        assertTrue(result is GlucoseValidator.ValidationResult.Success)
    }

    @Test
    fun `below minimum returns Error`() {
        val result = GlucoseValidator.validate("0.5")
        assertTrue(result is GlucoseValidator.ValidationResult.Error)
    }

    @Test
    fun `above maximum returns Error`() {
        val result = GlucoseValidator.validate("50.0")
        assertTrue(result is GlucoseValidator.ValidationResult.Error)
    }

    @Test
    fun `non-numeric input returns Error`() {
        val result = GlucoseValidator.validate("abc")
        assertTrue(result is GlucoseValidator.ValidationResult.Error)
    }

    @Test
    fun `empty string returns Error`() {
        val result = GlucoseValidator.validate("")
        assertTrue(result is GlucoseValidator.ValidationResult.Error)
    }

    @Test
    fun `integer input is valid`() {
        val result = GlucoseValidator.validate("7")
        assertTrue(result is GlucoseValidator.ValidationResult.Success)
        assertEquals(7.0f, (result as GlucoseValidator.ValidationResult.Success).value)
    }

    @Test
    fun `one decimal place is valid`() {
        val result = GlucoseValidator.validate("6.2")
        assertTrue(result is GlucoseValidator.ValidationResult.Success)
        assertEquals(6.2f, (result as GlucoseValidator.ValidationResult.Success).value)
    }
}
