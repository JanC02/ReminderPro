package com.reminderpro.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.reminderpro.MainActivity
import com.reminderpro.R

/**
 * Klasa pomocnicza do zarządzania powiadomieniami.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "reminder_channel"
        private const val CHANNEL_NAME = "Przypomnienia"
        private const val CHANNEL_DESCRIPTION = "Powiadomienia o przypomnieniach"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Tworzy kanał powiadomień (wymagane dla Android 8.0+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Wyświetla powiadomienie przypomnienia.
     *
     * @param reminderId ID przypomnienia
     * @param title Tytuł powiadomienia
     * @param message Treść powiadomienia
     */
    fun showReminderNotification(reminderId: Long, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }

    /**
     * Anuluje powiadomienie o określonym ID.
     */
    fun cancelNotification(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }

    /**
     * Anuluje wszystkie powiadomienia.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * Sprawdza czy powiadomienia są włączone.
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }
}
