package com.bloodsugar.util

import androidx.annotation.StringRes
import com.bloodsugar.R

enum class MealSegment(@StringRes val labelResId: Int) {
    BEFORE_BREAKFAST(R.string.segment_before_breakfast),
    AFTER_BREAKFAST(R.string.segment_after_breakfast),
    BEFORE_LUNCH(R.string.segment_before_lunch),
    AFTER_LUNCH(R.string.segment_after_lunch),
    BEFORE_DINNER(R.string.segment_before_dinner),
    AFTER_DINNER(R.string.segment_after_dinner);

    companion object {
        fun safeValueOf(name: String): MealSegment? =
            values().find { it.name == name }

        fun inferSegment(hour: Int): MealSegment = when (hour) {
            in 5..8 -> BEFORE_BREAKFAST
            in 9..10 -> AFTER_BREAKFAST
            in 11..12 -> BEFORE_LUNCH
            in 13..15 -> AFTER_LUNCH
            in 16..17 -> BEFORE_DINNER
            else -> AFTER_DINNER
        }
    }
}
