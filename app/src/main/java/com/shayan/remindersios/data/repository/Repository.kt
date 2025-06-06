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











//    private val TAG = "FirebaseHelper"
//
//    /**
//     * Deletes all user documents whose "uid" is in ["U1660", "U1732"]
//     * and all account documents whose "userId" is in ["U1660", "U1732"].
//     */
//    private suspend fun deleteSpecificUsersAndAccounts() {
////        val uidsToDelete = listOf("U1660", "U1732")
//        val uidsToDelete = listOf("U9879")
//
//        try {
//            // 1) Fetch the matching user docs
//            val usersToDelete = usersCollection
//                .whereIn("uid", uidsToDelete)
//                .get()
//                .await()
//                .documents
//
//            // 2) Fetch the matching account docs
//            val accountsToDelete = accountsCollection
//                .whereIn("userId", uidsToDelete)
//                .get()
//                .await()
//                .documents
//
//            // 3) If there’s nothing to delete, bail out early
//            if (usersToDelete.isEmpty() && accountsToDelete.isEmpty()) {
//                Log.d(TAG, "No matching users or accounts found for deletion.")
//                return
//            }
//
//            // 4) Batch‐delete them
//            val batch = firestore.batch()
//            usersToDelete.forEach { batch.delete(it.reference) }
//            accountsToDelete.forEach { batch.delete(it.reference) }
//            batch.commit().await()
//
//            Log.d(
//                TAG,
//                "Deleted ${usersToDelete.size} user(s) and ${accountsToDelete.size} account(s) for UIDs $uidsToDelete"
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Error deleting specific users/accounts: ${e.message}", e)
//        }
//    }


//    private val TAG = "FirebaseHelper"
//
//    /**
//     * Finds all emails shared by 2+ users, and for each duplicate group
//     * logs the email plus every field/value of each user doc.
//     */
//    private suspend fun logDuplicateUsersFullData() {
//        try {
//            // 1) load every user doc
//            val allUsers = usersCollection
//                .get()
//                .await()
//                .documents
//
//            // 2) group by lowercase-trimmed email (treat null/blank as "")
//            val byEmail = allUsers.groupBy { doc ->
//                doc.getString("email")?.trim()?.lowercase() ?: ""
//            }
//
//            // 3) for each group of size >= 2, dump full data
//            byEmail.forEach { (email, docs) ->
//                if (email.isBlank() || docs.size < 2) return@forEach
//
//                Log.d(TAG, "=== Found ${docs.size} users sharing email \"$email\" ===")
//                docs.forEach { doc ->
//                    val uid = doc.getString("uid") ?: "<no-uid>"
//                    Log.d(TAG, "-- User uid=$uid, docId=${doc.id} --")
//                    doc.data?.forEach { (field, value) ->
//                        Log.d(TAG, "   $field = $value")
//                    } ?: Log.d(TAG, "   <no data fields>")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error logging duplicate users full data: ${e.message}", e)
//        }
//    }


//    private val TAG = "FirebaseHelper"
//
//    /**
//     * Queries for the user whose "uid" equals [uid], and logs all of their fields.
//     */
//    private suspend fun logUserDataByUid(uid: String) {
//        try {
//            val snapshot = usersCollection
//                .whereEqualTo("uid", uid)
//                .limit(1)
//                .get()
//                .await()
//
//            val doc = snapshot.documents.firstOrNull()
//            if (doc == null) {
//                Log.d(TAG, "No user found with uid=$uid")
//                return
//            }
//
//            val data = doc.data
//            if (data.isNullOrEmpty()) {
//                Log.d(TAG, "User document is empty for uid=$uid")
//                return
//            }
//
//            Log.d(TAG, "User data for uid=$uid:")
//            data.forEach { (field, value) ->
//                Log.d(TAG, "  $field = $value")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error fetching user data for uid=$uid: ${e.message}", e)
//        }
//    }
//
//
//
//    /**
//     * Deletes all documents in `users` where uid ∉ whitelist,
//     * and in `accounts` where userId ∉ whitelist.
//     */
//    private suspend fun deleteNonWhitelistedUsersAndAccounts() {
//        // 1. Define the UIDs to keep
//        val whitelist = listOf("U2752", "U3324", "U4008", "U5684")
//
//        try {
//            // 2. Query for all user docs not in whitelist
//            val usersToDelete = usersCollection
//                .whereNotIn("uid", whitelist)
//                .get()
//                .await()
//                .documents
//
//            // 3. Query for all account docs not in whitelist
//            val accountsToDelete = accountsCollection
//                .whereNotIn("userId", whitelist)
//                .get()
//                .await()
//                .documents
//
//            // 4. If there’s anything to delete, batch it
//            if (usersToDelete.isNotEmpty() || accountsToDelete.isNotEmpty()) {
//                val batch = firestore.batch()
//                usersToDelete.forEach { batch.delete(it.reference) }
//                accountsToDelete.forEach { batch.delete(it.reference) }
//
//                // 5. Commit in one go
//                batch.commit().await()
//                Log.d(
//                    "FirebaseHelper",
//                    "Deleted ${usersToDelete.size} users and ${accountsToDelete.size} accounts not in whitelist"
//                )
//            } else {
//                Log.d("FirebaseHelper", "No users or accounts to delete")
//            }
//
//        } catch (e: Exception) {
//            Log.e("FirebaseHelper", "Error deleting non-whitelisted docs: ${e.message}", e)
//        }
//    }
//





