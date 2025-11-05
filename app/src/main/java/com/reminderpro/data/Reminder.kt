package com.reminderpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model danych dla przypomnienia.
 * Reprezentuje pojedyncze przypomnienie przechowywane w bazie danych.
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Tytuł przypomnienia */
    val title: String,

    /** Treść/opis przypomnienia */
    val message: String,

    /** Interwał w minutach między przypomnieniami */
    val intervalMinutes: Int,

    /** Czy przypomnienie jest aktywne */
    val isEnabled: Boolean = true,

    /** Timestamp utworzenia przypomnienia */
    val createdAt: Long = System.currentTimeMillis(),

    /** Timestamp ostatniego wywołania przypomnienia (null jeśli jeszcze nie było) */
    val lastTriggered: Long? = null,

    /** ID kategorii/ikony dla szybkiego wyboru */
    val categoryIcon: String? = null
) {
    /**
     * Zwraca sformatowany opis interwału (np. "co 30 minut", "co 2 godziny").
     */
    fun getFormattedInterval(): String {
        return when {
            intervalMinutes < 60 -> "co $intervalMinutes min"
            intervalMinutes % 1440 == 0 -> {
                val days = intervalMinutes / 1440
                "co $days ${if (days == 1) "dzień" else if (days < 5) "dni" else "dni"}"
            }
            intervalMinutes % 60 == 0 -> {
                val hours = intervalMinutes / 60
                "co $hours ${if (hours == 1) "godzinę" else if (hours < 5) "godziny" else "godzin"}"
            }
            else -> {
                val hours = intervalMinutes / 60
                val minutes = intervalMinutes % 60
                "co ${hours}h ${minutes}min"
            }
        }
    }

    /**
     * Sprawdza czy nadszedł czas na następne przypomnienie.
     */
    fun shouldTrigger(): Boolean {
        if (!isEnabled) return false

        val lastTrigger = lastTriggered ?: createdAt
        val now = System.currentTimeMillis()
        val intervalMillis = intervalMinutes * 60 * 1000L

        return (now - lastTrigger) >= intervalMillis
    }

    /**
     * Zwraca czas do następnego przypomnienia w milisekundach.
     */
    fun getTimeUntilNextTrigger(): Long {
        val lastTrigger = lastTriggered ?: createdAt
        val intervalMillis = intervalMinutes * 60 * 1000L
        val nextTriggerTime = lastTrigger + intervalMillis
        val now = System.currentTimeMillis()

        return maxOf(0, nextTriggerTime - now)
    }
}
