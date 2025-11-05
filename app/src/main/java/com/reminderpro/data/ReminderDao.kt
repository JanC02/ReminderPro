package com.reminderpro.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object dla operacji na przypomnieniach w bazie danych.
 */
@Dao
interface ReminderDao {

    /**
     * Pobiera wszystkie przypomnienia jako LiveData.
     * Automatycznie aktualizuje UI gdy dane się zmienią.
     */
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): LiveData<List<Reminder>>

    /**
     * Pobiera wszystkie aktywne przypomnienia.
     */
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getActiveReminders(): List<Reminder>

    /**
     * Pobiera przypomnienie po ID.
     */
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Long): Reminder?

    /**
     * Wstawia nowe przypomnienie do bazy.
     * Zwraca ID nowo utworzonego przypomnienia.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    /**
     * Aktualizuje istniejące przypomnienie.
     */
    @Update
    suspend fun updateReminder(reminder: Reminder)

    /**
     * Usuwa przypomnienie z bazy.
     */
    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    /**
     * Usuwa przypomnienie po ID.
     */
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)

    /**
     * Przełącza stan aktywności przypomnienia (włączone/wyłączone).
     */
    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :reminderId")
    suspend fun toggleReminderEnabled(reminderId: Long, enabled: Boolean)

    /**
     * Aktualizuje timestamp ostatniego wywołania przypomnienia.
     */
    @Query("UPDATE reminders SET lastTriggered = :timestamp WHERE id = :reminderId")
    suspend fun updateLastTriggered(reminderId: Long, timestamp: Long)

    /**
     * Pobiera liczbę wszystkich przypomnień.
     */
    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun getRemindersCount(): Int

    /**
     * Usuwa wszystkie przypomnienia (pomocne przy czyszczeniu/testowaniu).
     */
    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
