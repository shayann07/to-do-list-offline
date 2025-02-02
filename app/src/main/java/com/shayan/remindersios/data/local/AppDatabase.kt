package com.shayan.remindersios.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shayan.remindersios.data.local.dao.TasksDao
import com.shayan.remindersios.data.models.Tasks

/**
 * The main Room database for the application.
 * Stores and manages all [Tasks] entities.
 */
@Database(
    entities = [Tasks::class], version = 2, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // region DAO Accessors
    /**
     * Provides access to the [TasksDao] for performing database operations on [Tasks].
     */
    abstract fun tasksDao(): TasksDao
    // endregion

    // region Companion Object (Singleton)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retrieves the singleton instance of the database, creating it if necessary.
         * Ensures only one instance of the database is created across the entire application.
         *
         * @param context The [Context] used to build or retrieve the [AppDatabase].
         * @return The singleton [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase {
            // Double-checked locking to prevent multiple instances
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Builds a new [AppDatabase] instance with the required configuration.
         * Uses `fallbackToDestructiveMigration()` to reset the DB if version mismatches occur.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "app_database"
            )
                // Clears and rebuilds the database on version mismatch.
                // In production, consider using proper migration strategies.
                .fallbackToDestructiveMigration().build()
        }
    }
    // endregion
}