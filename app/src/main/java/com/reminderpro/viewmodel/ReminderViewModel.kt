package com.reminderpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.reminderpro.data.Reminder
import com.reminderpro.data.ReminderDatabase
import com.reminderpro.data.ReminderRepository
import com.reminderpro.workers.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel dla zarządzania przypomnieniami.
 * Używa AndroidViewModel aby mieć dostęp do Application context.
 */
class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository
    val allReminders: LiveData<List<Reminder>>

    init {
        val reminderDao = ReminderDatabase.getDatabase(application).reminderDao()
        repository = ReminderRepository(reminderDao)
        allReminders = repository.allReminders
    }

    /**
     * Dodaje nowe przypomnienie.
     * Uruchamia scheduler dla tego przypomnienia.
     */
    fun addReminder(reminder: Reminder, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            onComplete(id)
        }
    }

    /**
     * Aktualizuje istniejące przypomnienie.
     */
    fun updateReminder(reminder: Reminder, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            onComplete()
        }
    }

    /**
     * Usuwa przypomnienie.
     * Anuluje scheduler dla tego przypomnienia.
     */
    fun deleteReminder(reminder: Reminder, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            onComplete()
        }
    }

    /**
     * Usuwa przypomnienie po ID.
     */
    fun deleteReminderById(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteReminderById(id)
            onComplete()
        }
    }

    /**
     * Przełącza stan aktywności przypomnienia (włączone/wyłączone).
     */
    fun toggleReminderEnabled(id: Long, enabled: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.toggleReminderEnabled(id, enabled)
            onComplete()
        }
    }

    /**
     * Aktualizuje timestamp ostatniego wywołania przypomnienia.
     */
    fun updateLastTriggered(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateLastTriggered(id)
            onComplete()
        }
    }

    /**
     * Pobiera przypomnienie po ID.
     */
    fun getReminderById(id: Long, onResult: (Reminder?) -> Unit) {
        viewModelScope.launch {
            val reminder = repository.getReminderById(id)
            onResult(reminder)
        }
    }

    /**
     * Pobiera liczbę wszystkich przypomnień.
     */
    fun getRemindersCount(onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.getRemindersCount()
            onResult(count)
        }
    }

    /**
     * Przeplanowuje wszystkie aktywne przypomnienia.
     * Używane po zmianie ustawień godzin ciszy, aby zakolejkowane alarmy
     * respektowały nowe okno (ReminderScheduler.scheduleReminder uwzględnia ciszę).
     */
    fun rescheduleAll(scheduler: ReminderScheduler) {
        viewModelScope.launch(Dispatchers.IO) {
            scheduler.rescheduleAllReminders(repository.getActiveReminders())
        }
    }

    /**
     * Usuwa wszystkie przypomnienia (pomocne przy testowaniu).
     */
    fun deleteAllReminders(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllReminders()
            onComplete()
        }
    }
}
