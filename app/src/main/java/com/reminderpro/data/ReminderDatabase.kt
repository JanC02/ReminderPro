package com.reminderpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Główna baza danych Room dla aplikacji ReminderPro.
 * Używa wzorca Singleton aby zapewnić jedną instancję bazy.
 */
@Database(
    entities = [Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {

    /**
     * Dostęp do DAO dla operacji na przypomnieniach.
     */
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        /**
         * Pobiera instancję bazy danych (Singleton).
         * Jeśli instancja nie istnieje, tworzy nową.
         */
        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .fallbackToDestructiveMigration() // W produkcji użyj migracji
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Pomocnicza metoda do testowania - pozwala wyczyścić instancję bazy.
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
