package com.reminderpro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminderpro.data.ReminderDatabase
import com.reminderpro.data.ReminderRepository
import com.reminderpro.notifications.NotificationHelper
import com.reminderpro.workers.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver odbierający alarmy dla przypomnień.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_MESSAGE = "reminder_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: return
        val message = intent.getStringExtra(EXTRA_REMINDER_MESSAGE) ?: ""

        if (reminderId == -1L) return

        // Wyświetl powiadomienie
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminderNotification(reminderId, title, message)

        // Zaktualizuj timestamp w bazie i zaplanuj następne przypomnienie
        CoroutineScope(Dispatchers.IO).launch {
            val database = ReminderDatabase.getDatabase(context)
            val repository = ReminderRepository(database.reminderDao())

            // Aktualizuj lastTriggered
            repository.updateLastTriggered(reminderId)

            // Pobierz przypomnienie i zaplanuj następne
            val reminder = repository.getReminderById(reminderId)
            if (reminder != null && reminder.isEnabled) {
                val scheduler = ReminderScheduler.getInstance(context)
                scheduler.scheduleReminder(reminder)
            }
        }
    }
}
