package com.reminderpro.data

import androidx.lifecycle.LiveData

/**
 * Repository dla operacji na przypomnieniach.
 * Zapewnia czystą warstwę abstrakcji między ViewModel a bazą danych.
 */
class ReminderRepository(private val reminderDao: ReminderDao) {

    /**
     * LiveData ze wszystkimi przypomnieniami.
     * Automatycznie aktualizowana przy zmianach w bazie.
     */
    val allReminders: LiveData<List<Reminder>> = reminderDao.getAllReminders()

    /**
     * Pobiera wszystkie aktywne przypomnienia (synchronicznie).
     */
    fun getActiveReminders(): List<Reminder> {
        return reminderDao.getActiveReminders()
    }

    /**
     * Pobiera przypomnienie po ID.
     */
    suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)
    }

    /**
     * Dodaje nowe przypomnienie do bazy.
     * Zwraca ID nowo utworzonego przypomnienia.
     */
    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    /**
     * Aktualizuje istniejące przypomnienie.
     */
    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    /**
     * Usuwa przypomnienie.
     */
    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    /**
     * Usuwa przypomnienie po ID.
     */
    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    /**
     * Przełącza stan aktywności przypomnienia.
     */
    suspend fun toggleReminderEnabled(id: Long, enabled: Boolean) {
        reminderDao.toggleReminderEnabled(id, enabled)
    }

    /**
     * Aktualizuje timestamp ostatniego wywołania przypomnienia.
     */
    suspend fun updateLastTriggered(id: Long, timestamp: Long = System.currentTimeMillis()) {
        reminderDao.updateLastTriggered(id, timestamp)
    }

    /**
     * Pobiera liczbę wszystkich przypomnień.
     */
    suspend fun getRemindersCount(): Int {
        return reminderDao.getRemindersCount()
    }

    /**
     * Usuwa wszystkie przypomnienia.
     */
    suspend fun deleteAllReminders() {
        reminderDao.deleteAllReminders()
    }
}
