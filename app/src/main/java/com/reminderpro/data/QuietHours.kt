package com.reminderpro.data

import android.content.Context
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Globalne "godziny ciszy" — przedział, w którym przypomnienia nie są wysyłane
 * (np. 22:00–06:00). Czysta logika czasu, bez zależności od Androida — łatwa do testów.
 *
 * @param enabled czy wyciszanie jest aktywne
 * @param startMinutes początek okna jako minuty od północy (0..1439)
 * @param endMinutes koniec okna jako minuty od północy (0..1439), wyłączający
 */
data class QuietHours(
    val enabled: Boolean,
    val startMinutes: Int,
    val endMinutes: Int
) {
    /**
     * Czy podany moment (timestamp w ms) wypada w oknie ciszy.
     * Obsługuje okno zawinięte przez północ (start > end).
     * Gdy start == end traktujemy okno jako puste (false), żeby nie zablokować całej doby.
     */
    fun isWithin(timeMillis: Long): Boolean {
        if (!enabled || startMinutes == endMinutes) return false

        val minuteOfDay = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .let { it.hour * 60 + it.minute }

        return if (startMinutes < endMinutes) {
            // Okno w obrębie jednej doby, np. 13:00–14:00
            minuteOfDay in startMinutes until endMinutes
        } else {
            // Okno zawinięte przez północ, np. 22:00–06:00
            minuteOfDay >= startMinutes || minuteOfDay < endMinutes
        }
    }

    /**
     * Koryguje czas wyzwolenia alarmu względem okna ciszy.
     * - poza oknem (lub wyłączone) → zwraca [triggerMillis] bez zmian,
     * - w oknie → zwraca najbliższy "koniec ciszy" (endMinutes) po [triggerMillis].
     *
     * Dzięki temu żaden alarm nie ląduje wewnątrz okna — dostarczenie jest przesuwane
     * na koniec okna (np. 06:00), po czym wraca normalny interwał.
     */
    fun adjustTriggerTime(triggerMillis: Long): Long {
        if (!isWithin(triggerMillis)) return triggerMillis

        val zone = ZoneId.systemDefault()
        val triggerDateTime = Instant.ofEpochMilli(triggerMillis).atZone(zone)
        val endTime = LocalTime.of(endMinutes / 60, endMinutes % 60)

        // Najbliższe wystąpienie endTime nie wcześniej niż trigger = wyjście z okna ciszy.
        var end = triggerDateTime.with(endTime)
        if (!end.toInstant().isAfter(triggerDateTime.toInstant())) {
            end = end.plusDays(1)
        }
        return end.toInstant().toEpochMilli()
    }

    companion object {
        const val DEFAULT_START_MINUTES = 22 * 60 // 22:00
        const val DEFAULT_END_MINUTES = 6 * 60    // 06:00
    }
}

/**
 * Odczyt/zapis ustawień godzin ciszy w SharedPreferences.
 * Nie dotyka bazy Room — brak potrzeby migracji.
 */
class QuietHoursStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): QuietHours = QuietHours(
        enabled = prefs.getBoolean(KEY_ENABLED, false),
        startMinutes = prefs.getInt(KEY_START, QuietHours.DEFAULT_START_MINUTES),
        endMinutes = prefs.getInt(KEY_END, QuietHours.DEFAULT_END_MINUTES)
    )

    fun save(quietHours: QuietHours) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, quietHours.enabled)
            .putInt(KEY_START, quietHours.startMinutes)
            .putInt(KEY_END, quietHours.endMinutes)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "reminderpro_settings"
        private const val KEY_ENABLED = "quiet_enabled"
        private const val KEY_START = "quiet_start_minutes"
        private const val KEY_END = "quiet_end_minutes"
    }
}
