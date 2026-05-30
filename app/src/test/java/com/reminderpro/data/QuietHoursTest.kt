package com.reminderpro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Testy czystej logiki godzin ciszy: [QuietHours.isWithin] i [QuietHours.adjustTriggerTime].
 */
class QuietHoursTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    /** Tworzy timestamp (ms) dla danej godziny/minuty dnia 2026-01-15 w strefie systemowej. */
    private fun millisAt(hour: Int, minute: Int): Long =
        LocalDateTime.of(2026, 1, 15, hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    private val wrapped = QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 6 * 60) // 22:00–06:00
    private val sameDay = QuietHours(enabled = true, startMinutes = 13 * 60, endMinutes = 14 * 60) // 13:00–14:00

    // --- isWithin: okno zawinięte przez północ ---

    @Test fun wrapped_lateNight_isWithin() = assertTrue(wrapped.isWithin(millisAt(23, 0)))
    @Test fun wrapped_afterMidnight_isWithin() = assertTrue(wrapped.isWithin(millisAt(5, 0)))
    @Test fun wrapped_start_isWithin() = assertTrue(wrapped.isWithin(millisAt(22, 0)))
    @Test fun wrapped_end_isExclusive() = assertFalse(wrapped.isWithin(millisAt(6, 0)))
    @Test fun wrapped_midday_isOutside() = assertFalse(wrapped.isWithin(millisAt(12, 0)))

    // --- isWithin: okno w obrębie doby ---

    @Test fun sameDay_inside_isWithin() = assertTrue(sameDay.isWithin(millisAt(13, 30)))
    @Test fun sameDay_end_isExclusive() = assertFalse(sameDay.isWithin(millisAt(14, 0)))
    @Test fun sameDay_before_isOutside() = assertFalse(sameDay.isWithin(millisAt(12, 0)))

    // --- isWithin: przypadki brzegowe ---

    @Test fun disabled_neverWithin() =
        assertFalse(QuietHours(false, 22 * 60, 6 * 60).isWithin(millisAt(23, 0)))

    @Test fun startEqualsEnd_neverWithin() =
        assertFalse(QuietHours(true, 6 * 60, 6 * 60).isWithin(millisAt(6, 0)))

    // --- adjustTriggerTime ---

    @Test fun adjust_insideWrapped_movesToNextEnd() {
        val trigger = millisAt(23, 0)
        val adjusted = wrapped.adjustTriggerTime(trigger)
        val expected = LocalDateTime.of(2026, 1, 16, 6, 0).atZone(zone).toInstant().toEpochMilli()
        assertEquals(expected, adjusted)
    }

    @Test fun adjust_afterMidnight_movesToSameDayEnd() {
        val trigger = millisAt(5, 0)
        val adjusted = wrapped.adjustTriggerTime(trigger)
        val expected = LocalDateTime.of(2026, 1, 15, 6, 0).atZone(zone).toInstant().toEpochMilli()
        assertEquals(expected, adjusted)
    }

    @Test fun adjust_outsideWindow_unchanged() {
        val trigger = millisAt(12, 0)
        assertEquals(trigger, wrapped.adjustTriggerTime(trigger))
    }

    @Test fun adjust_disabled_unchanged() {
        val disabled = QuietHours(false, 22 * 60, 6 * 60)
        val trigger = millisAt(23, 0)
        assertEquals(trigger, disabled.adjustTriggerTime(trigger))
    }

    @Test fun adjust_resultIsOutsideWindow() {
        val adjusted = wrapped.adjustTriggerTime(millisAt(23, 0))
        // Po korekcie czas nie może już wypadać w oknie ciszy.
        assertFalse(wrapped.isWithin(adjusted))
        // I powinien przypadać dokładnie na koniec okna (06:00).
        val t = java.time.Instant.ofEpochMilli(adjusted).atZone(zone).toLocalTime()
        assertEquals(LocalTime.of(6, 0), t)
    }
}
