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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "app_database"
                )
                    .fallbackToDestructiveMigration() // Clears and rebuilds the database on version mismatch
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}