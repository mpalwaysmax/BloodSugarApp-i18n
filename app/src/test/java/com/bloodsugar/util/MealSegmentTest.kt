package com.bloodsugar.util

import org.junit.Assert.*
import org.junit.Test

class MealSegmentTest {

    @Test
    fun `early morning maps to BEFORE_BREAKFAST`() {
        assertEquals(MealSegment.BEFORE_BREAKFAST, MealSegment.inferSegment(6))
        assertEquals(MealSegment.BEFORE_BREAKFAST, MealSegment.inferSegment(7))
        assertEquals(MealSegment.BEFORE_BREAKFAST, MealSegment.inferSegment(8))
    }

    @Test
    fun `morning maps to AFTER_BREAKFAST`() {
        assertEquals(MealSegment.AFTER_BREAKFAST, MealSegment.inferSegment(9))
        assertEquals(MealSegment.AFTER_BREAKFAST, MealSegment.inferSegment(10))
    }

    @Test
    fun `late morning maps to BEFORE_LUNCH`() {
        assertEquals(MealSegment.BEFORE_LUNCH, MealSegment.inferSegment(11))
        assertEquals(MealSegment.BEFORE_LUNCH, MealSegment.inferSegment(12))
    }

    @Test
    fun `afternoon maps to AFTER_LUNCH`() {
        assertEquals(MealSegment.AFTER_LUNCH, MealSegment.inferSegment(13))
        assertEquals(MealSegment.AFTER_LUNCH, MealSegment.inferSegment(14))
        assertEquals(MealSegment.AFTER_LUNCH, MealSegment.inferSegment(15))
    }

    @Test
    fun `late afternoon maps to BEFORE_DINNER`() {
        assertEquals(MealSegment.BEFORE_DINNER, MealSegment.inferSegment(16))
        assertEquals(MealSegment.BEFORE_DINNER, MealSegment.inferSegment(17))
    }

    @Test
    fun `evening and night maps to AFTER_DINNER`() {
        assertEquals(MealSegment.AFTER_DINNER, MealSegment.inferSegment(18))
        assertEquals(MealSegment.AFTER_DINNER, MealSegment.inferSegment(22))
        assertEquals(MealSegment.AFTER_DINNER, MealSegment.inferSegment(0))
        assertEquals(MealSegment.AFTER_DINNER, MealSegment.inferSegment(4))
    }

    @Test
    fun `all 6 segments exist`() {
        assertEquals(6, MealSegment.values().size)
    }
}
