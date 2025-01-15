package com.shayan.remindersios.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shayan.remindersios.data.local.dao.TasksDao
import com.shayan.remindersios.data.models.Tasks

@Database(entities = [Tasks::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tasksDao(): TasksDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retrieves the singleton instance of the database.
         * Ensures only one instance of the database is created across the application.
         *
         * @param context Application context to initialize the database.
         * @return The [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create the database instance if it doesn't already exist
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Builds the database with the specified configuration.
         * Configurations include handling version mismatches and enabling migrations.
         *
         * @param context Application context.
         * @return A new [AppDatabase] instance.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "app_database"
            )
                .fallbackToDestructiveMigration() // Clears and rebuilds the database on version mismatch
                .build()
        }
    }
}