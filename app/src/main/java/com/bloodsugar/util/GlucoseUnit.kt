package com.bloodsugar.util

enum class GlucoseUnit(val label: String, val factor: Float) {
    MMOL("mmol/L", 1f),
    MGDL("mg/dL", 18.0182f);

    companion object {
        const val PREF_KEY = "glucose_unit"

        fun fromMmol(mmol: Float, unit: GlucoseUnit): Float {
            return when (unit) {
                MMOL -> mmol
                MGDL -> mmol * MGDL.factor
            }
        }

        fun toMmol(value: Float, unit: GlucoseUnit): Float {
            return when (unit) {
                MMOL -> value
                MGDL -> value / MGDL.factor
            }
        }

        fun formatValue(mmol: Float, unit: GlucoseUnit): String {
            return when (unit) {
                MMOL -> "%.1f".format(mmol)
                MGDL -> "%.0f".format(mmol * MGDL.factor)
            }
        }

        fun validationRange(unit: GlucoseUnit): Pair<Float, Float> {
            return when (unit) {
                MMOL -> Pair(1.0f, 33.3f)
                MGDL -> Pair(18f, 600f)
            }
        }

        fun normalRange(unit: GlucoseUnit): Pair<Float, Float> {
            return when (unit) {
                MMOL -> Pair(4.4f, 7.8f)
                MGDL -> Pair(79f, 140f)
            }
        }
    }
}
