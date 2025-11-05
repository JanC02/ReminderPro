package com.reminderpro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminderpro.data.ReminderDatabase
import com.reminderpro.workers.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver odbierający event restartu systemu.
 * Restartuje wszystkie aktywne przypomnienia.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Reschedule wszystkich aktywnych przypomnień
            CoroutineScope(Dispatchers.IO).launch {
                val database = ReminderDatabase.getDatabase(context)
                val dao = database.reminderDao()
                val activeReminders = dao.getActiveReminders()

                val scheduler = ReminderScheduler.getInstance(context)
                scheduler.rescheduleAllReminders(activeReminders)
            }
        }
    }
}
