package com.shayan.remindersios.data.local.dao

import androidx.room.*
import com.shayan.remindersios.data.models.Tasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Tasks)

    @Update
    suspend fun updateTask(task: Tasks)

    @Query("DELETE FROM tasks WHERE roomTaskId = :roomTaskId")
    suspend fun deleteTaskByRoomTaskId(roomTaskId: Int)

    @Query("SELECT * FROM tasks WHERE roomTaskId = :roomTaskId")
    suspend fun getTaskByRoomTaskId(roomTaskId: Int): Tasks?

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :title || '%' ORDER BY timestamp ASC")
    suspend fun getTasksByTitle(title: String): List<Tasks>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getIncompleteTasks(): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getIncompleteTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY timestamp ASC")
    suspend fun getCompletedTasks(): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks ORDER BY timestamp ASC")
    suspend fun getTotalTasks(): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE date = :todayDate AND isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getTasksForToday(todayDate: String): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :todayDate AND isCompleted = 0")
    fun getTodayTaskCount(todayDate: String): Flow<Int>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 0")
    suspend fun getTasksForDateRange(startDate: String, endDate: String): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 0")
    fun getTasksCountForDateRange(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT * FROM tasks WHERE flag = 1 AND isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getFlaggedTasks(): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks WHERE flag = 1 AND isCompleted = 0")
    fun getFlaggedTaskCount(): Flow<Int>

    @Query("UPDATE tasks SET isCompleted = :isCompleted, dateCompleted = :dateCompleted WHERE roomTaskId = :roomTaskId")
    suspend fun updateTaskCompletion(roomTaskId: Int, isCompleted: Boolean, dateCompleted: String?)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun clearAllCompletedTasks()

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}
