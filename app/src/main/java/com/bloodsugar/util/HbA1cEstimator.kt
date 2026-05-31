package com.bloodsugar.util

import com.bloodsugar.data.Record
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Estimates HbA1c from recent blood glucose records using the Nathan formula:
 * HbA1c = (average_glucose + 2.59) / 1.59
 *
 * Based on: Nathan DM, et al. "Translating the A1C Assay Into Estimated Average Glucose Values."
 * Diabetes Care 2008;31(8):1473-1478.
 */
object HbA1cEstimator {

    /**
     * Estimate HbA1c from a list of records (in mmol/L).
     * Only uses records from the last [days] days.
     */
    fun estimate(records: List<Record>, days: Int = 90): Float? {
        val cutoff = Instant.now().minus(days.toLong(), ChronoUnit.DAYS).toEpochMilli()
        val recent = records.filter { it.timestamp >= cutoff }

        if (recent.isEmpty()) return null

        val avgMmol = recent.map { it.value }.average().toFloat()
        return (avgMmol + 2.59f) / 1.59f
    }

    /**
     * Get percentage of readings in normal range (4.4-7.8 mmol/L).
     */
    fun percentInRange(records: List<Record>, days: Int = 90): Float? {
        val cutoff = Instant.now().minus(days.toLong(), ChronoUnit.DAYS).toEpochMilli()
        val recent = records.filter { it.timestamp >= cutoff }

        if (recent.isEmpty()) return null

        val inRange = recent.count { it.value in 4.4f..7.8f }
        return (inRange.toFloat() / recent.size) * 100f
    }
}
