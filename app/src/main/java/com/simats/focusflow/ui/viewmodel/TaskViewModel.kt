package com.simats.focusflow.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.utils.AcademicCalendarHelper
import com.simats.focusflow.utils.AcademicEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
enum class TaskFilter(val title: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    ALL("All")
}

@Immutable
data class TaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskItem> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.TODAY,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isBottomSheetOpen: Boolean = false,
    val editingTask: TaskItem? = null,
    val academicEvents: List<AcademicEvent> = emptyList(),
    val isSyncingCalendar: Boolean = false
) {
    val filteredTasks: List<TaskItem>
        get() {
            var list = tasks
            if (searchQuery.isNotBlank()) {
                list = list.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
                }
            }
            return when (selectedFilter) {
                TaskFilter.TODAY -> list.filter {
                    !it.isCompleted && isTaskForToday(it)
                }
                TaskFilter.UPCOMING -> list.filter {
                    !it.isCompleted && isTaskUpcoming(it)
                }
                TaskFilter.COMPLETED -> list.filter { it.isCompleted }
                TaskFilter.ALL -> list
            }
        }

    val todayCount: Int
        get() = tasks.count { !it.isCompleted && isTaskForToday(it) }

    val upcomingCount: Int
        get() = tasks.count { !it.isCompleted && isTaskUpcoming(it) }

    val completedCount: Int
        get() = tasks.count { it.isCompleted }

    companion object {
        fun isTaskForToday(task: TaskItem): Boolean {
            if (task.dueDate.equals("Today", ignoreCase = true)) return true
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)

            val timestamp = task.deadlineTimestamp
            if (timestamp != null && timestamp > 0L) {
                val taskDate = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
                if (taskDate == today || taskDate.isBefore(today)) return true
            }

            if (task.dueDate.isNotBlank() && !task.dueDate.equals("Tomorrow", ignoreCase = true)) {
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ISO_LOCAL_DATE,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
                )
                for (fmt in formatters) {
                    try {
                        val parsed = LocalDate.parse(task.dueDate.trim(), fmt)
                        if (parsed == today || parsed.isBefore(today)) return true
                    } catch (_: Exception) {}
                }
            }

            if (task.dueDate.isBlank() && (timestamp == null || timestamp <= 0L)) {
                return true
            }

            return false
        }

        fun isTaskUpcoming(task: TaskItem): Boolean {
            if (isTaskForToday(task)) return false
            if (task.dueDate.equals("Tomorrow", ignoreCase = true)) return true
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)

            val timestamp = task.deadlineTimestamp
            if (timestamp != null && timestamp > 0L) {
                val taskDate = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
                if (taskDate.isAfter(today)) return true
            }

            if (task.dueDate.isNotBlank()) {
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ISO_LOCAL_DATE,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
                )
                for (fmt in formatters) {
                    try {
                        val parsed = LocalDate.parse(task.dueDate.trim(), fmt)
                        if (parsed.isAfter(today)) return true
                    } catch (_: Exception) {}
                }
                return true
            }

            return false
        }
    }
}

class TaskViewModel(
    private val repository: ProductivityRepository = ProductivityRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState(isLoading = true))
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1

    init {
        viewModelScope.launch {
            com.simats.focusflow.data.local.FocusSessionDatabase.taskTableChangedFlow.collect {
                loadTasksSilently()
            }
        }
    }

    private fun loadTasksSilently() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                repository.getTasks(currentUserId)
            } catch (e: Exception) {
                Result.failure(e)
            }
            withContext(Dispatchers.Main) {
                result.onSuccess { list ->
                    _uiState.update { it.copy(tasks = list) }
                }
            }
        }
    }

    fun initSessionManager(context: android.content.Context, manager: SessionManager) {
        initSessionManager(manager)
    }

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                sessionManager?.let { sm ->
                    val session = sm.userSessionFlow.first()
                    if (session.userId != null && session.userId > 0) {
                        currentUserId = session.userId
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskViewModel", "Failed to read session: ${e.message}")
            }

            val result = try {
                repository.getTasks(currentUserId)
            } catch (e: Exception) {
                android.util.Log.e("TaskViewModel", "Failed to get tasks: ${e.message}")
                Result.failure(e)
            }
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(isLoading = false, tasks = list) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
                    }
                )
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setBottomSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isBottomSheetOpen = isOpen, editingTask = if (!isOpen) null else it.editingTask) }
    }

    fun openCreateTask() {
        _uiState.update { it.copy(isBottomSheetOpen = true, editingTask = null) }
    }

    fun openEditTask(task: TaskItem) {
        _uiState.update { it.copy(isBottomSheetOpen = true, editingTask = task) }
    }

    fun toggleTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = _uiState.value.tasks.toMutableList()
            val index = currentList.indexOfFirst { it.id == taskId }
            if (index != -1) {
                val updatedStatus = !currentList[index].isCompleted
                currentList[index] = currentList[index].copy(isCompleted = updatedStatus)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(tasks = currentList) }
                }

                repository.toggleTask(currentUserId, taskId, updatedStatus)
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = _uiState.value.tasks.filter { it.id != taskId }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(tasks = currentList) }
            }

            repository.deleteTask(currentUserId, taskId)
        }
    }

    fun createTask(
        title: String,
        description: String = "",
        priority: String = "medium",
        dueDate: String = "Today",
        category: String = "General",
        scheduledTime: String? = null,
        deadlineTimestamp: Long? = null
    ) {
        if (title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isBottomSheetOpen = false, editingTask = null) }
            }

            val result = repository.createTask(
                userId = currentUserId,
                title = title.trim(),
                description = description.trim(),
                priority = priority.lowercase(),
                dueDate = dueDate.trim(),
                category = category.trim(),
                scheduledTime = scheduledTime?.trim(),
                deadlineTimestamp = deadlineTimestamp
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { createdTask ->
                        val updated = _uiState.value.tasks.toMutableList().apply { add(0, createdTask) }
                        _uiState.update { it.copy(tasks = updated) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(errorMessage = err.localizedMessage) }
                    }
                )
            }
        }
    }

    fun updateTask(
        taskId: Int,
        title: String,
        description: String = "",
        priority: String = "medium",
        dueDate: String = "Today",
        category: String = "General",
        scheduledTime: String? = null,
        deadlineTimestamp: Long? = null,
        clientId: String = ""
    ) {
        if (title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isBottomSheetOpen = false, editingTask = null) }
            }

            val result = repository.updateTask(
                userId = currentUserId,
                taskId = taskId,
                title = title.trim(),
                description = description.trim(),
                priority = priority.lowercase(),
                dueDate = dueDate.trim(),
                category = category.trim(),
                scheduledTime = scheduledTime?.trim(),
                deadlineTimestamp = deadlineTimestamp,
                clientId = clientId
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { updatedTask ->
                        val updated = _uiState.value.tasks.map { if (it.id == taskId) updatedTask else it }
                        _uiState.update { it.copy(tasks = updated) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(errorMessage = err.localizedMessage) }
                    }
                )
            }
        }
    }

    fun syncAcademicCalendar(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isSyncingCalendar = true) }
            }
            val events = AcademicCalendarHelper.fetchAcademicEvents(context)
            val currentTasks = _uiState.value.tasks
            val created = AcademicCalendarHelper.autoSchedulePrepTasks(
                events = events,
                existingTasks = currentTasks,
                userId = currentUserId,
                repository = repository
            )
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        academicEvents = events,
                        isSyncingCalendar = false
                    )
                }
                if (created) {
                    loadTasks()
                }
            }
        }
    }
}
