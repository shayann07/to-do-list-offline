package com.shayan.remindersios.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.data.repository.Repository
import com.shayan.remindersios.utils.AlarmManagerHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * A ViewModel for managing task data and counts, bridging between the Repository and the UI.
 */
class ViewModel(application: Application) : AndroidViewModel(application) {

    // region Repository & Initialization
    private val repository = Repository(application)

    // region LiveData - Task Counts
    val todayTaskCount: LiveData<Int>
    val scheduledTasksCount: LiveData<Int>
    val flaggedTasksCount: LiveData<Int>
    val incompleteTasksCount: LiveData<Int>
    val completedTasksCount: LiveData<Int>
    val totalTaskCount: LiveData<Int>
    // endregion

    init {
        val todayDate = getCurrentDate()
        val endDate = getFutureDate(12) // 12 months ahead
        // Trigger initial LiveData with flows
        todayTaskCount = repository.getTodayTaskCountFlow(todayDate).asLiveData()
        scheduledTasksCount = repository.getScheduledTasksCountFlow(todayDate, endDate).asLiveData()
        flaggedTasksCount = repository.getFlaggedTaskCountFlow().asLiveData()
        incompleteTasksCount = repository.getIncompleteTasksCountFlow().asLiveData()
        completedTasksCount = repository.getCompletedTasksCountFlow().asLiveData()
        totalTaskCount = repository.getTotalTasksCountFlow().asLiveData()
    }
    // endregion

    // region LiveData - Task Lists
    val tasksList = MutableLiveData<List<Tasks>?>()
    val searchQueryResult = MutableLiveData<List<Tasks>>()
    val taskCreationStatus = MutableLiveData<Boolean>()
    val morningTasksLiveData = MutableLiveData<List<Tasks>>()
    val afternoonTasksLiveData = MutableLiveData<List<Tasks>>()
    val tonightTasksLiveData = MutableLiveData<List<Tasks>>()
    val tasksByMonth = MutableLiveData<Map<String, List<Tasks>>>(emptyMap())
    val flaggedTasks = MutableLiveData<List<Tasks>>()
    val incompleteTasks = MutableLiveData<List<Tasks>>()
    val completedTasks = MutableLiveData<List<Tasks>>()
    val totalTasks = MutableLiveData<List<Tasks>>()
    val taskDeletionStatus = MutableLiveData<Boolean>()
    // endregion

    // region State
    private var currentSearchQuery: String = ""
    // endregion

    // region Fetch Methods
    /**
     * Fetch tasks by matching the [title]. Populates [searchQueryResult].
     */
    fun fetchTasksByTitle(title: String) {
        currentSearchQuery = title
        viewModelScope.launch {
            handleRepositoryCall(call = { repository.getTasksByTitle(title) },
                onSuccess = { searchQueryResult.postValue(it) })
        }
    }

    /**
     * Fetch tasks specifically for today's date and categorize them.
     */
    fun fetchTodayTasks() {
        val todayDate = getCurrentDate()
        viewModelScope.launch {
            handleRepositoryCall(call = { repository.getTasksForToday(todayDate) },
                onSuccess = { tasks ->
                    tasksList.postValue(tasks)
                    val (morning, afternoon, tonight) = categorizeTasksByTime(tasks)
                    morningTasksLiveData.postValue(morning)
                    afternoonTasksLiveData.postValue(afternoon)
                    tonightTasksLiveData.postValue(tonight)
                })
        }
    }

    /**
     * Fetch tasks scheduled between today and 12 months from now,
     * grouping them by "Month Year" keys in [tasksByMonth].
     */
    fun fetchScheduledTasks() {
        viewModelScope.launch {
            val startDate = getCurrentDate()
            val endDate = getFutureDate(12)
            repository.getScheduledTasks(startDate, endDate).catch { e ->
                logError(
                    "Error fetching scheduled tasks", e as Exception
                )
            }.collect { tasks ->
                val groupedTasks = tasks.groupBy { task ->
                    try {
                        task.date?.let {
                            // Format e.g., "October 2024"
                            val parsedDate =
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                            SimpleDateFormat(
                                "MMMM yyyy", Locale.getDefault()
                            ).format(parsedDate!!)
                        } ?: "Unknown"
                    } catch (e: Exception) {
                        logError("Error parsing date for task: ${task.roomTaskId}", e)
                        "Unknown"
                    }
                }
                tasksByMonth.value = groupedTasks
            }
        }
    }

    /**
     * Fetch flagged tasks and store them in [flaggedTasks].
     */
    fun fetchFlaggedTasks() {
        fetchTaskData(
            fetcher = { repository.getFlaggedTasks() }, liveData = flaggedTasks, label = "flagged"
        )
    }

    /**
     * Fetch incomplete tasks and store them in [incompleteTasks].
     */
    fun fetchIncompleteTasks() {
        fetchTaskData(
            fetcher = { repository.getIncompleteTasks() },
            liveData = incompleteTasks,
            label = "incomplete"
        )
    }

    /**
     * Fetch completed tasks and store them in [completedTasks].
     */
    fun fetchCompletedTasks() {
        fetchTaskData(
            fetcher = { repository.getCompletedTasks() },
            liveData = completedTasks,
            label = "completed"
        )
    }

    /**
     * Fetch all tasks and store them in [totalTasks].
     */
    fun fetchTotalTasks() {
        fetchTaskData(
            fetcher = { repository.getTotalTasks() }, liveData = totalTasks, label = "total"
        )
    }
    // endregion

    // region Task Updates / Creation / Deletion
    /**
     * Toggles the completion status of a task and updates the relevant data sets.
     */
    fun toggleTaskCompletion(
        roomTaskId: Int, isCompleted: Boolean, onCompletion: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateLocalTaskCompletion(roomTaskId, isCompleted)
                fetchAllTaskData() // Refresh all relevant lists
                onCompletion(true, "Task successfully updated!")
            } catch (e: Exception) {
                onCompletion(false, "Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Saves a new [task] to the database and schedules an alarm if needed.
     */
    fun saveTask(task: Tasks) {
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(timestamp = System.currentTimeMillis())
                repository.saveTasksToRoom(updatedTask)
                taskCreationStatus.postValue(true)
                AlarmManagerHelper.scheduleTaskReminder(getApplication(), updatedTask)
            } catch (e: Exception) {
                taskCreationStatus.postValue(false)
                logError("Failed to save task", e)
            }
        }
    }

    /**
     * Deletes a task based on its [roomTaskId] and refreshes data sets.
     */
    fun deleteTask(roomTaskId: Int) {
        viewModelScope.launch {
            handleRepositoryCall(call = { repository.deleteTaskFromRoom(roomTaskId) },
                onSuccess = { fetchAllTaskData() })
        }
    }

    /**
     * Restores a previously deleted task.
     */
    fun undoDeleteTask(task: Tasks) {
        viewModelScope.launch {
            handleRepositoryCall(call = { repository.saveTasksToRoom(task) },
                onSuccess = { fetchTodayTasks() } // Only re-fetch today's tasks here
            )
        }
    }

    /**
     * Removes all completed tasks from the database.
     */
    fun deleteCompletedTasks() {
        viewModelScope.launch {
            handleRepositoryCall(call = { repository.clearAllCompletedTasks() }, onSuccess = {
                fetchCompletedTasks()
                taskDeletionStatus.postValue(true)
            })
        }
    }

    /**
     * Clears all tasks in the database.
     */
    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
        }
    }
    // endregion

    // region Data Aggregation
    /**
     * Re-fetches multiple sets of data to ensure UI is up-to-date across different screens.
     */
    private fun fetchAllTaskData() {
        fetchTodayTasks()
        fetchScheduledTasks()
        fetchIncompleteTasks()
        fetchFlaggedTasks()
        fetchCompletedTasks()
        fetchTotalTasks()
    }
    // endregion

    // region Helper Methods
    /**
     * Categorizes a list of [tasks] into morning, afternoon, and tonight lists.
     */
    private fun categorizeTasksByTime(tasks: List<Tasks>): Triple<List<Tasks>, List<Tasks>, List<Tasks>> {
        val morning = mutableListOf<Tasks>()
        val afternoon = mutableListOf<Tasks>()
        val tonight = mutableListOf<Tasks>()

        tasks.forEach { task ->
            val hour = task.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: return@forEach
            when (hour) {
                in 5..11 -> morning.add(task)
                in 12..16 -> afternoon.add(task)
                else -> tonight.add(task)
            }
        }
        return Triple(morning, afternoon, tonight)
    }

    /**
     * A generic method for performing a repository call with error handling.
     */
    private suspend fun <T> handleRepositoryCall(call: suspend () -> T, onSuccess: (T) -> Unit) {
        try {
            val result = call()
            onSuccess(result)
        } catch (e: Exception) {
            logError("Error during repository call", e)
        }
    }

    /**
     * Fetches generic task data from a [fetcher] function and posts it to [liveData], with logging.
     */
    private fun <T> fetchTaskData(
        fetcher: suspend () -> T, liveData: MutableLiveData<T>, label: String
    ) {
        viewModelScope.launch {
            handleRepositoryCall(fetcher) {
                liveData.postValue(it)
            }
        }
    }

    /**
     * Logs an error with a standard tag and message format.
     */
    private fun logError(message: String, e: Exception) {
        Log.e("TaskViewModel", "$message: ${e.message}")
    }

    /**
     * Returns today's date in `yyyy-MM-dd` format.
     */
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Returns a future date [monthsAhead] in `yyyy-MM-dd` format.
     */
    private fun getFutureDate(monthsAhead: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthsAhead) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
    // endregion
}
