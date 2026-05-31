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
        /**
         * Infer meal segment from hour of day.
         * 5:00-8:59  → Before Breakfast
         * 9:00-10:59 → After Breakfast
         * 11:00-12:59 → Before Lunch
         * 13:00-15:59 → After Lunch
         * 16:00-17:59 → Before Dinner
         * 18:00-23:59 / 0:00-4:59 → After Dinner
         */
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
