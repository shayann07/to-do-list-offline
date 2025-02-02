package com.shayan.remindersios.data.repository

import android.content.Context
import android.util.Log
import com.shayan.remindersios.data.local.AppDatabase
import com.shayan.remindersios.data.models.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * A Repository responsible for all data operations involving [Tasks],
 * including retrieval, insertion, updating, and deletion in the local database.
 */
class Repository(context: Context) {

    // region DAO Reference
    private val taskDao = AppDatabase.getInstance(context).tasksDao()
    // endregion

    // region Fetch by Title
    /**
     * Retrieves a list of [Tasks] that match the given [title], ignoring case.
     */
    suspend fun getTasksByTitle(title: String): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getTasksByTitle(title)
    }
    // endregion

    // region Save to Database
    /**
     * Saves a [task] to the local database, returning a [Result] indicating success or failure.
     * If a task with the same [roomTaskId] already exists, it fails.
     */
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
    // endregion

    // region Today Tasks
    /**
     * Retrieves a list of tasks scheduled for today's date ([todayDate]).
     */
    suspend fun getTasksForToday(todayDate: String): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getTasksForToday(todayDate)
    }

    /**
     * Returns a [Flow] of the count of today's tasks.
     */
    fun getTodayTaskCountFlow(todayDate: String): Flow<Int> = taskDao.getTodayTaskCount(todayDate)
    // endregion

    // region Scheduled Tasks
    /**
     * Returns a [Flow] of tasks scheduled between [startDate] and [endDate].
     */
    fun getScheduledTasks(startDate: String, endDate: String): Flow<List<Tasks>> {
        return taskDao.getTasksForDateRange(startDate, endDate)
    }

    /**
     * Returns a [Flow] of the count of tasks scheduled between [startDate] and [endDate].
     */
    fun getScheduledTasksCountFlow(startDate: String, endDate: String): Flow<Int> {
        return taskDao.getTasksCountForDateRange(startDate, endDate)
    }
    // endregion

    // region Flagged Tasks
    /**
     * Retrieves a list of tasks flagged by the user.
     */
    suspend fun getFlaggedTasks(): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getFlaggedTasks()
    }

    /**
     * Returns a [Flow] of the count of flagged tasks.
     */
    fun getFlaggedTaskCountFlow(): Flow<Int> = taskDao.getFlaggedTaskCount()
    // endregion

    // region Update Completion
    /**
     * Updates the [isCompleted] status of a local task identified by [roomTaskId].
     * If [isCompleted] is true, sets [dateCompleted] to current date/time; otherwise null.
     */
    suspend fun updateLocalTaskCompletion(roomTaskId: Int, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            val dateCompleted = if (isCompleted) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            } else {
                null
            }
            taskDao.updateTaskCompletion(roomTaskId, isCompleted, dateCompleted)
            Log.d(
                "Repository",
                "Local task status updated: ID=$roomTaskId, " + "isCompleted=$isCompleted, dateCompleted=$dateCompleted"
            )
        }
    }
    // endregion

    // region Incomplete Tasks
    /**
     * Retrieves a list of tasks that are not yet completed.
     */
    suspend fun getIncompleteTasks(): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getIncompleteTasks()
    }

    /**
     * Returns a [Flow] of the count of incomplete tasks.
     */
    fun getIncompleteTasksCountFlow(): Flow<Int> = taskDao.getIncompleteTaskCount()
    // endregion

    // region Completed Tasks
    /**
     * Retrieves a list of tasks that have been completed.
     */
    suspend fun getCompletedTasks(): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getCompletedTasks()
    }

    /**
     * Returns a [Flow] of the count of completed tasks.
     */
    fun getCompletedTasksCountFlow(): Flow<Int> = taskDao.getCompletedTaskCount()
    // endregion

    // region All Tasks
    /**
     * Retrieves all tasks from the database.
     */
    suspend fun getTotalTasks(): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getTotalTasks()
    }

    /**
     * Returns a [Flow] of the total task count in the database.
     */
    fun getTotalTasksCountFlow(): Flow<Int> = taskDao.getTotalTaskCount()
    // endregion

    // region Deletion
    /**
     * Deletes a task from the local database by its [roomTaskId].
     */
    suspend fun deleteTaskFromRoom(roomTaskId: Int) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskByRoomTaskId(roomTaskId)
    }

    /**
     * Removes all completed tasks from the local database.
     */
    suspend fun clearAllCompletedTasks() = withContext(Dispatchers.IO) {
        taskDao.clearAllCompletedTasks()
    }

    /**
     * Clears all tasks from the local database, irrespective of completion status.
     */
    suspend fun clearAllTasks() = withContext(Dispatchers.IO) {
        taskDao.clearAllTasks()
    }
    // endregion
}