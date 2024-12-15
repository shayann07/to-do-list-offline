package com.shayan.remindersios.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.data.repository.Repository
import com.shayan.remindersios.utils.AlarmManagerHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)

    // Task counts as LiveData
    val todayTaskCount: LiveData<Int>
    val scheduledTasksCount: LiveData<Int>
    val flaggedTasksCount: LiveData<Int>
    val incompleteTasksCount: LiveData<Int>
    val completedTasksCount: LiveData<Int>
    val totalTaskCount: LiveData<Int>

    init {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 12)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        todayTaskCount = repository.getTodayTaskCountFlow(todayDate).asLiveData()
        scheduledTasksCount = repository.getScheduledTasksCountFlow(todayDate, endDate).asLiveData()
        flaggedTasksCount = repository.getFlaggedTaskCountFlow().asLiveData()
        incompleteTasksCount = repository.getIncompleteTasksCountFlow().asLiveData()
        completedTasksCount = repository.getCompletedTasksCountFlow().asLiveData()
        totalTaskCount = repository.getTotalTasksCountFlow().asLiveData()
    }

    // Task lists
    val tasksList = MutableLiveData<List<Tasks>?>()
    val searchQueryResult = MutableLiveData<List<Tasks>>()
    val taskCreationStatus = MutableLiveData<Boolean>()
    val morningTasksLiveData = MutableLiveData<List<Tasks>>()
    val afternoonTasksLiveData = MutableLiveData<List<Tasks>>()
    val tonightTasksLiveData = MutableLiveData<List<Tasks>>()
    val tasksByMonth = MutableLiveData<Map<String, List<Tasks>>>()
    val flaggedTasks = MutableLiveData<List<Tasks>>()
    val incompleteTasks = MutableLiveData<List<Tasks>>()
    val completedTasks = MutableLiveData<List<Tasks>>()
    val totalTasks = MutableLiveData<List<Tasks>>()
    val taskDeletionStatus = MutableLiveData<Boolean>()
    private var currentSearchQuery: String = ""

    fun fetchTasksByTitle(title: String) {
        currentSearchQuery = title
        viewModelScope.launch {
            try {
                val tasks = repository.getTasksByTitle(title)
                searchQueryResult.postValue(tasks)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch tasks: ${e.message}")
            }
        }
    }

    fun fetchTodayTasks() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            try {
                val tasks = repository.getTasksForToday(todayDate)
                tasksList.postValue(tasks)

                val (morning, afternoon, tonight) = categorizeTasksByTime (tasks)
                morningTasksLiveData.postValue(morning)
                afternoonTasksLiveData.postValue(afternoon)
                tonightTasksLiveData.postValue(tonight)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch tasks for today: ${e.message}")
            }
        }
    }

    fun fetchScheduledTasks() {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val startYear = calendar.get(Calendar.YEAR)
                val startMonth = calendar.get(Calendar.MONTH)

                val startDate = "${startYear}-${String.format("%02d", startMonth + 1)}-01"
                calendar.add(Calendar.MONTH, 12)
                val endDate = "${calendar.get(Calendar.YEAR)}-${
                    String.format(
                        "%02d", calendar.get(Calendar.MONTH) + 1
                    )
                }-01"

                val tasks = repository.getScheduledTasks(startDate, endDate)

                // Group tasks by "MMMM yyyy" for the next 12 months
                val groupedTasks = tasks.groupBy { task ->
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                            task.date ?: ""
                        )!!
                    )
                }
                tasksByMonth.postValue(groupedTasks)
            } catch (e: Exception) {
                Log.e(
                    "ViewModel", "Failed to fetch tasks for the next 12 months: ${e.message}"
                )
            }
        }
    }

    fun fetchFlaggedTasks() {
        viewModelScope.launch {
            try {
                val tasks = repository.getFlaggedTasks()
                flaggedTasks.postValue(tasks)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch flagged tasks: ${e.message}")
            }
        }
    }

    fun fetchIncompleteTasks() {
        viewModelScope.launch {
            try {
                val tasks = repository.getIncompleteTasks()
                incompleteTasks.postValue(tasks)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch incomplete tasks: ${e.message}")
            }
        }
    }

    fun fetchCompletedTasks() {
        viewModelScope.launch {
            try {
                val tasks = repository.getCompletedTasks()
                completedTasks.postValue(tasks)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch completed tasks: ${e.message}")
            }
        }
    }

    fun fetchTotalTasks() {
        viewModelScope.launch {
            try {
                val tasks = repository.getTotalTasks()
                totalTasks.postValue(tasks)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch completed tasks: ${e.message}")
            }
        }
    }

    fun toggleTaskCompletion(
        roomTaskId: Int, isCompleted: Boolean, onCompletion: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateLocalTaskCompletion(roomTaskId, isCompleted)
                fetchTodayTasks()
                fetchScheduledTasks()
                fetchIncompleteTasks()
                fetchFlaggedTasks()
                fetchTotalTasks()
                fetchCompletedTasks()
                onCompletion(true, "Task successfully updated!")
            } catch (e: Exception) {
                onCompletion(false, "Unexpected error: ${e.message}")
            }
        }
    }

    fun saveTask(task: Tasks) {
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(timestamp = System.currentTimeMillis())
                repository.saveTasksToRoom(updatedTask)
                taskCreationStatus.postValue(true)
                AlarmManagerHelper.scheduleTaskReminder(getApplication(), updatedTask)
            } catch (e: Exception) {
                taskCreationStatus.postValue(false)
                Log.e("ViewModel", "Failed to save task: ${e.message}")
            }
        }
    }

    fun deleteTask(roomTaskId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTaskFromRoom(roomTaskId)
                fetchTodayTasks()
                fetchScheduledTasks()
                fetchIncompleteTasks()
                fetchFlaggedTasks()
                fetchTotalTasks()
                fetchCompletedTasks()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to delete task: ${e.message}")
            }
        }
    }

    fun undoDeleteTask(task: Tasks) {
        viewModelScope.launch {
            try {
                repository.saveTasksToRoom(task)
                fetchTodayTasks()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to restore task: ${e.message}")
            }
        }
    }

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

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            try {
                repository.clearAllCompletedTasks()
                fetchCompletedTasks()
                taskDeletionStatus.postValue(true)
            } catch (e: Exception) {
                taskDeletionStatus.postValue(false)
                Log.e("ViewModel", "Failed to delete completed tasks: ${e.message}")
            }
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
        }
    }

}
