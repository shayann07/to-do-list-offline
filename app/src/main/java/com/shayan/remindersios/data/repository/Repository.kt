package com.shayan.remindersios.data.repository

import android.content.Context
import android.util.Log
import com.shayan.remindersios.data.local.AppDatabase
import com.shayan.remindersios.data.models.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(context: Context) {

    private val taskDao = AppDatabase.getInstance(context).tasksDao()

    // ROOM OPERATIONS

    suspend fun getTasksByTitle(title: String): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTasksByTitle(title) }

    // Save Task to Room
    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val existingTask = taskDao.getTaskByRoomTaskId(task.roomTaskId)
            if (existingTask == null) {
                taskDao.insertTask(task)
                Result.success(true)
            } else {
                Result.failure(Exception("Task with ID ${task.roomTaskId} already exists"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch Tasks for Today
    suspend fun getTasksForToday(todayDate: String): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTasksForToday(todayDate) }

    fun getTodayTaskCountFlow(todayDate: String): Flow<Int> = taskDao.getTodayTaskCount(todayDate)

    suspend fun getScheduledTasks(startDate: String, endDate: String): List<Tasks> =
        withContext(Dispatchers.IO) {
            taskDao.getTasksForDateRange(startDate, endDate)
        }

    fun getScheduledTasksCountFlow(startDate: String, endDate: String): Flow<Int> =
        taskDao.getTasksCountForDateRange(startDate, endDate)

    // Fetch Flagged Tasks
    suspend fun getFlaggedTasks(): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getFlaggedTasks()
    }

    fun getFlaggedTaskCountFlow(): Flow<Int> = taskDao.getFlaggedTaskCount()

    // Update Task Completion Locally
    suspend fun updateLocalTaskCompletion(
        roomTaskId: Int, isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {

        val dateCompleted = if (isCompleted) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        } else {
            null
        }

        taskDao.updateTaskCompletion(roomTaskId, isCompleted, dateCompleted)
        Log.d(
            "Repository",
            "Local task status updated: ID=$roomTaskId, isCompleted=$isCompleted, dateCompleted=$dateCompleted"
        )
    }

    // Fetch incomplete tasks from Room and their count
    suspend fun getIncompleteTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getIncompleteTasks() }

    fun getIncompleteTasksCountFlow(): Flow<Int> = taskDao.getIncompleteTaskCount()

    // Fetch completed tasks from Room and their count
    suspend fun getCompletedTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getCompletedTasks() }

    fun getCompletedTasksCountFlow(): Flow<Int> = taskDao.getCompletedTaskCount()

    // Fetch all tasks from Room and their count
    suspend fun getTotalTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTotalTasks() }

    fun getTotalTasksCountFlow(): Flow<Int> = taskDao.getTotalTaskCount()

    // Update a task
    suspend fun updateTask(task: Tasks) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    // Delete task by ID
    suspend fun deleteTaskFromRoom(roomTaskId: Int) =
        withContext(Dispatchers.IO) { taskDao.deleteTaskByRoomTaskId(roomTaskId) }

    // Clear Completed Tasks Locally
    suspend fun clearAllCompletedTasks() =
        withContext(Dispatchers.IO) { taskDao.clearAllCompletedTasks() }

    suspend fun clearAllTasks() = withContext(Dispatchers.IO) { taskDao.clearAllTasks() }
}
