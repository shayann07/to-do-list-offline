package com.shayan.remindersios.data.local.dao

import androidx.room.*
import com.shayan.remindersios.data.models.Tasks
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for performing CRUD operations
 * and specific queries on the [Tasks] table in the Room database.
 */
@Dao
interface TasksDao {

    // region Insert / Update

    /**
     * Inserts a [task] into the database.
     * If there is a conflict (e.g., same primary key), it will replace the existing record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Tasks)

    /**
     * Updates the given [task] in the database, matching by primary key.
     */
    @Update
    suspend fun updateTask(task: Tasks)

    /**
     * Updates only the completion status and dateCompleted field of the task
     * identified by [roomTaskId].
     *
     * @param isCompleted Whether the task has been completed.
     * @param dateCompleted The date/time when the task was completed, if applicable.
     */
    @Query("UPDATE tasks SET isCompleted = :isCompleted, dateCompleted = :dateCompleted WHERE roomTaskId = :roomTaskId")
    suspend fun updateTaskCompletion(roomTaskId: Int, isCompleted: Boolean, dateCompleted: String?)
    // endregion

    // region Delete

    /**
     * Deletes a task from the database by its [roomTaskId].
     */
    @Query("DELETE FROM tasks WHERE roomTaskId = :roomTaskId")
    suspend fun deleteTaskByRoomTaskId(roomTaskId: Int)

    /**
     * Deletes all tasks that are marked as completed.
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun clearAllCompletedTasks()

    /**
     * Deletes all tasks in the database, irrespective of completion status.
     */
    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
    // endregion

    // region Single Task Retrieval

    /**
     * Retrieves a task by its unique [roomTaskId].
     *
     * @return The matching [Tasks] object, or null if not found.
     */
    @Query("SELECT * FROM tasks WHERE roomTaskId = :roomTaskId")
    suspend fun getTaskByRoomTaskId(roomTaskId: Int): Tasks?
    // endregion

    // region Queries: Title, Incomplete, Completed, All

    /**
     * Retrieves a list of tasks whose title contains [title] (case-insensitive),
     * ordered by [Tasks.timestamp] ascending.
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :title || '%' COLLATE NOCASE ORDER BY timestamp ASC")
    suspend fun getTasksByTitle(title: String): List<Tasks>

    /**
     * Retrieves all tasks that are incomplete (isCompleted = 0),
     * ordered by [Tasks.timestamp] ascending.
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getIncompleteTasks(): List<Tasks>

    /**
     * Retrieves all tasks that are completed (isCompleted = 1),
     * ordered by [Tasks.timestamp] ascending.
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY timestamp ASC")
    suspend fun getCompletedTasks(): List<Tasks>

    /**
     * Retrieves all tasks in the database, ordered by [Tasks.timestamp] ascending.
     */
    @Query("SELECT * FROM tasks ORDER BY timestamp ASC")
    suspend fun getTotalTasks(): List<Tasks>
    // endregion

    // region Queries: Flows & Counts

    /**
     * Returns a [Flow] that emits the count of incomplete tasks (isCompleted = 0).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getIncompleteTaskCount(): Flow<Int>

    /**
     * Returns a [Flow] that emits the count of completed tasks (isCompleted = 1).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    /**
     * Returns a [Flow] that emits the total number of tasks in the database.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTaskCount(): Flow<Int>
    // endregion

    // region Queries: Date-based

    /**
     * Retrieves tasks scheduled for [todayDate] (date = :todayDate) and not completed (isCompleted = 0),
     * ordered by [Tasks.timestamp] ascending.
     */
    @Query("SELECT * FROM tasks WHERE date = :todayDate AND isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getTasksForToday(todayDate: String): List<Tasks>

    /**
     * Returns a [Flow] that emits the count of today's tasks (date = :todayDate) that are not completed.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE date = :todayDate AND isCompleted = 0")
    fun getTodayTaskCount(todayDate: String): Flow<Int>

    /**
     * Returns a [Flow] of tasks whose [Tasks.date] is between [startDate] and [endDate]
     * (inclusive) and not completed, for scheduling scenarios.
     */
    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 0")
    fun getTasksForDateRange(startDate: String, endDate: String): Flow<List<Tasks>>

    /**
     * Returns a [Flow] of the count of tasks whose [Tasks.date] is between [startDate] and [endDate]
     * (inclusive) and not completed.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 0")
    fun getTasksCountForDateRange(startDate: String, endDate: String): Flow<Int>
    // endregion

    // region Queries: Flagged

    /**
     * Retrieves tasks that are flagged (flag = 1) and not completed (isCompleted = 0),
     * ordered by [Tasks.timestamp] ascending.
     */
    @Query("SELECT * FROM tasks WHERE flag = 1 AND isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getFlaggedTasks(): List<Tasks>

    /**
     * Returns a [Flow] that emits the count of tasks that are flagged (flag = 1) and not completed.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE flag = 1 AND isCompleted = 0")
    fun getFlaggedTaskCount(): Flow<Int>
    // endregion
}