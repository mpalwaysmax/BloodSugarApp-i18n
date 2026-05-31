package com.bloodsugar.util

import org.junit.Assert.*
import org.junit.Test

class GlucoseUnitTest {

    @Test
    fun `mmol to mgdl conversion is correct`() {
        val mgdl = GlucoseUnit.fromMmol(5.5f, GlucoseUnit.MGDL)
        assertEquals(99.1f, mgdl, 0.5f)
    }

    @Test
    fun `mmol stays same in mmol unit`() {
        val mmol = GlucoseUnit.fromMmol(5.5f, GlucoseUnit.MMOL)
        assertEquals(5.5f, mmol, 0.01f)
    }

    @Test
    fun `mgdl to mmol conversion is correct`() {
        val mmol = GlucoseUnit.toMmol(100f, GlucoseUnit.MGDL)
        assertEquals(5.55f, mmol, 0.1f)
    }

    @Test
    fun `format mmol shows one decimal`() {
        val formatted = GlucoseUnit.formatValue(5.5f, GlucoseUnit.MMOL)
        assertEquals("5.5", formatted)
    }

    @Test
    fun `format mgdl shows no decimal`() {
        val formatted = GlucoseUnit.formatValue(5.5f, GlucoseUnit.MGDL)
        assertEquals("99", formatted)
    }

    @Test
    fun `normal range for mmol is 4_4 to 7_8`() {
        val (low, high) = GlucoseUnit.normalRange(GlucoseUnit.MMOL)
        assertEquals(4.4f, low)
        assertEquals(7.8f, high)
    }

    @Test
    fun `normal range for mgdl is 79 to 140`() {
        val (low, high) = GlucoseUnit.normalRange(GlucoseUnit.MGDL)
        assertEquals(79f, low)
        assertEquals(140f, high)
    }
}
