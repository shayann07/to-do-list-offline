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

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)

    // LiveData for task counts
    val todayTaskCount: LiveData<Int>
    val scheduledTasksCount: LiveData<Int>
    val flaggedTasksCount: LiveData<Int>
    val incompleteTasksCount: LiveData<Int>
    val completedTasksCount: LiveData<Int>
    val totalTaskCount: LiveData<Int>

    init {
        val todayDate = getCurrentDate()
        val endDate = getFutureDate(12)

        todayTaskCount = repository.getTodayTaskCountFlow(todayDate).asLiveData()
        scheduledTasksCount = repository.getScheduledTasksCountFlow(todayDate, endDate).asLiveData()
        flaggedTasksCount = repository.getFlaggedTaskCountFlow().asLiveData()
        incompleteTasksCount = repository.getIncompleteTasksCountFlow().asLiveData()
        completedTasksCount = repository.getCompletedTasksCountFlow().asLiveData()
        totalTaskCount = repository.getTotalTasksCountFlow().asLiveData()
    }

    // LiveData for task details
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

    private var currentSearchQuery: String = ""

    // Fetch tasks by title
    fun fetchTasksByTitle(title: String) {
        currentSearchQuery = title
        viewModelScope.launch {
            handleRepositoryCall({ repository.getTasksByTitle(title) }) {
                searchQueryResult.postValue(it)
            }
        }
    }

    // Fetch today's tasks and categorize them by time
    fun fetchTodayTasks() {
        val todayDate = getCurrentDate()
        viewModelScope.launch {
            handleRepositoryCall({ repository.getTasksForToday(todayDate) }) { tasks ->
                tasksList.postValue(tasks)

                val (morning, afternoon, tonight) = categorizeTasksByTime(tasks)
                morningTasksLiveData.postValue(morning)
                afternoonTasksLiveData.postValue(afternoon)
                tonightTasksLiveData.postValue(tonight)
            }
        }
    }

    // Fetch scheduled tasks grouped by month
    fun fetchScheduledTasks() {
        viewModelScope.launch {
            val startDate = getCurrentDate()
            val endDate = getFutureDate(12)
            repository.getScheduledTasks(startDate, endDate).catch { e ->
                Log.e("TaskViewModel", "Error fetching tasks", e)
            }.collect { tasks ->
                val groupedTasks = tasks.groupBy { task ->
                    try {
                        task.date?.let {
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)!!
                            )
                        } ?: "Unknown"
                    } catch (e: Exception) {
                        Log.e(
                            "TaskViewModel", "Error parsing date for task: ${task.roomTaskId}", e
                        )
                        "Unknown"
                    }
                }
                Log.d("TaskViewModel", "Tasks fetched and grouped by month: $groupedTasks")
                tasksByMonth.value = groupedTasks
            }
        }
    }

    // Fetch flagged tasks
    fun fetchFlaggedTasks() {
        fetchTaskData(repository::getFlaggedTasks, flaggedTasks, "flagged")
    }

    // Fetch incomplete tasks
    fun fetchIncompleteTasks() {
        fetchTaskData(repository::getIncompleteTasks, incompleteTasks, "incomplete")
    }

    // Fetch completed tasks
    fun fetchCompletedTasks() {
        fetchTaskData(repository::getCompletedTasks, completedTasks, "completed")
    }

    // Fetch total tasks
    fun fetchTotalTasks() {
        fetchTaskData(repository::getTotalTasks, totalTasks, "total")
    }

    // Toggle task completion status
    fun toggleTaskCompletion(
        roomTaskId: Int, isCompleted: Boolean, onCompletion: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateLocalTaskCompletion(roomTaskId, isCompleted)
                fetchAllTaskData()
                onCompletion(true, "Task successfully updated!")
            } catch (e: Exception) {
                onCompletion(false, "Unexpected error: ${e.message}")
            }
        }
    }

    // Save a new task
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

    // Delete a task
    fun deleteTask(roomTaskId: Int) {
        viewModelScope.launch {
            handleRepositoryCall({ repository.deleteTaskFromRoom(roomTaskId) }) {
                fetchAllTaskData()
            }
        }
    }

    // Undo task deletion
    fun undoDeleteTask(task: Tasks) {
        viewModelScope.launch {
            handleRepositoryCall({ repository.saveTasksToRoom(task) }) {
                fetchTodayTasks()
            }
        }
    }

    // Clear all completed tasks
    fun deleteCompletedTasks() {
        viewModelScope.launch {
            handleRepositoryCall({ repository.clearAllCompletedTasks() }) {
                fetchCompletedTasks()
                taskDeletionStatus.postValue(true)
            }
        }
    }

    // Clear all tasks
    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
        }
    }

    // Categorize tasks by time (morning, afternoon, tonight)
    private fun categorizeTasksByTime(tasks: List<Tasks>): Triple<List<Tasks>, List<Tasks>, List<Tasks>> {
        val morning = mutableListOf<Tasks>()
        val afternoon = mutableListOf<Tasks>()
        val tonight = mutableListOf<Tasks>()

        tasks.forEach { task ->
            val time = task.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: return@forEach
            when (time) {
                in 5..11 -> morning.add(task)
                in 12..16 -> afternoon.add(task)
                else -> tonight.add(task)
            }
        }
        return Triple(morning, afternoon, tonight)
    }

    // Fetch all task-related data
    private fun fetchAllTaskData() {
        fetchTodayTasks()
        fetchScheduledTasks()
        fetchIncompleteTasks()
        fetchFlaggedTasks()
        fetchCompletedTasks()
        fetchTotalTasks()
    }

    // Fetch task data with error handling
    private fun <T> fetchTaskData(
        fetcher: suspend () -> T, liveData: MutableLiveData<T>, label: String
    ) {
        viewModelScope.launch {
            handleRepositoryCall(fetcher) {
                liveData.postValue(it)
            }
        }
    }

    // Handle repository calls with error handling
    private suspend fun <T> handleRepositoryCall(call: suspend () -> T, onSuccess: (T) -> Unit) {
        try {
            val result = call()
            onSuccess(result)
        } catch (e: Exception) {
            logError("Error during repository call", e)
        }
    }

    // Log errors with consistent format
    private fun logError(message: String, e: Exception) {
        Log.e("TaskViewModel", "$message: ${e.message}")
    }

    // Get the current date in yyyy-MM-dd format
    private fun getCurrentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Get a future date (months ahead) in yyyy-MM-dd format
    private fun getFutureDate(monthsAhead: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthsAhead) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}