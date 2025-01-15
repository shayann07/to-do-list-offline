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

    /**
     * Fetch tasks by title (case-insensitive search).
     */
    suspend fun getTasksByTitle(title: String): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTasksByTitle(title) }

    /**
     * Save a task to the local database.
     * If a task with the same ID already exists, the operation fails.
     */
    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
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

    /**
     * Fetch tasks scheduled for today.
     */
    suspend fun getTasksForToday(todayDate: String): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTasksForToday(todayDate) }

    /**
     * Observe the count of today's tasks as a Flow.
     */
    fun getTodayTaskCountFlow(todayDate: String): Flow<Int> = taskDao.getTodayTaskCount(todayDate)

    /**
     * Fetch tasks scheduled between a date range.
     */
    fun getScheduledTasks(startDate: String, endDate: String): Flow<List<Tasks>> =
        taskDao.getTasksForDateRange(startDate, endDate)

    /**
     * Observe the count of tasks scheduled between a date range.
     */
    fun getScheduledTasksCountFlow(startDate: String, endDate: String): Flow<Int> =
        taskDao.getTasksCountForDateRange(startDate, endDate)

    /**
     * Fetch flagged tasks from the database.
     */
    suspend fun getFlaggedTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getFlaggedTasks() }

    /**
     * Observe the count of flagged tasks as a Flow.
     */
    fun getFlaggedTaskCountFlow(): Flow<Int> = taskDao.getFlaggedTaskCount()

    /**
     * Update the completion status of a task locally.
     */
    suspend fun updateLocalTaskCompletion(roomTaskId: Int, isCompleted: Boolean) =
        withContext(Dispatchers.IO) {
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

    /**
     * Fetch all incomplete tasks.
     */
    suspend fun getIncompleteTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getIncompleteTasks() }

    /**
     * Observe the count of incomplete tasks as a Flow.
     */
    fun getIncompleteTasksCountFlow(): Flow<Int> = taskDao.getIncompleteTaskCount()

    /**
     * Fetch all completed tasks.
     */
    suspend fun getCompletedTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getCompletedTasks() }

    /**
     * Observe the count of completed tasks as a Flow.
     */
    fun getCompletedTasksCountFlow(): Flow<Int> = taskDao.getCompletedTaskCount()

    /**
     * Fetch all tasks from the database.
     */
    suspend fun getTotalTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTotalTasks() }

    /**
     * Observe the total task count as a Flow.
     */
    fun getTotalTasksCountFlow(): Flow<Int> = taskDao.getTotalTaskCount()

    /**
     * Delete a task by its ID.
     */
    suspend fun deleteTaskFromRoom(roomTaskId: Int) =
        withContext(Dispatchers.IO) { taskDao.deleteTaskByRoomTaskId(roomTaskId) }

    /**
     * Clear all completed tasks from the database.
     */
    suspend fun clearAllCompletedTasks() =
        withContext(Dispatchers.IO) { taskDao.clearAllCompletedTasks() }

    /**
     * Clear all tasks from the database.
     */
    suspend fun clearAllTasks() = withContext(Dispatchers.IO) { taskDao.clearAllTasks() }
}
