// DateUtil.kt — Mandarin Learn
// Epoch-day helpers per ARCHITECTURE.md §5.6.
// "today" = LocalDate.now(ZoneId.systemDefault()).toEpochDay() — all date math in epoch-days.

package com.mandarinlearn.util

import java.time.LocalDate
import java.time.ZoneId

/**
 * Utility functions for epoch-day date arithmetic.
 * All SRS review dates and reading completion dates are stored as epoch days.
 */
object DateUtil {

    /**
     * Returns today's epoch day in the device's default time zone.
     * This is the canonical "today" used for SRS due-date comparisons.
     */
    fun today(): Long = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    /** Converts an epoch day to a [LocalDate] for display formatting. */
    fun epochDayToLocalDate(epochDay: Long): LocalDate =
        LocalDate.ofEpochDay(epochDay)

    /** Returns true if the given epoch day is today. */
    fun isToday(epochDay: Long): Boolean = epochDay == today()

    /** Returns true if the given epoch day is yesterday. */
    fun isYesterday(epochDay: Long): Boolean = epochDay == today() - 1
}
