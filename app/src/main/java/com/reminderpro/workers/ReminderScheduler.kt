package com.reminderpro.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import com.reminderpro.data.Reminder
import com.reminderpro.receivers.AlarmReceiver
import java.util.concurrent.TimeUnit

/**
 * Klasa odpowiedzialna za schedulowanie przypomnień.
 * Używa AlarmManager dla precyzyjnych alarmów.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Scheduluje przypomnienie używając AlarmManager.
     */
    fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(AlarmReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(AlarmReceiver.EXTRA_REMINDER_MESSAGE, reminder.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + reminder.getTimeUntilNextTrigger()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Dokładne alarmy tylko gdy mamy uprawnienie; inaczej fallback na niedokładny,
            // który nie wymaga SCHEDULE_EXACT_ALARM i nigdy nie rzuca SecurityException.
            // Dzięki temu łańcuch przypomnień nigdy nie ginie po cichu.
            if (canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    // Wyścig: uprawnienie cofnięte tuż po sprawdzeniu -> niedokładny alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Anuluje schedulowane przypomnienie.
     */
    fun cancelReminder(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Reschedule wszystkich aktywnych przypomnień.
     * Używane po restarcie urządzenia.
     */
    fun rescheduleAllReminders(reminders: List<Reminder>) {
        reminders.filter { it.isEnabled }.forEach { reminder ->
            scheduleReminder(reminder)
        }
    }

    /**
     * Sprawdza czy aplikacja ma uprawnienie do schedulowania dokładnych alarmów.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    companion object {
        /**
         * Pomocnicza metoda do utworzenia instancji schedulera.
         */
        fun getInstance(context: Context): ReminderScheduler {
            return ReminderScheduler(context.applicationContext)
        }
    }
}
