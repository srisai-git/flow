package com.simats.focusflow.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val isBottomSheetOpen: Boolean = false
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
                    !it.isCompleted && (it.dueDate.equals("Today", ignoreCase = true) || it.dueDate.isBlank())
                }
                TaskFilter.UPCOMING -> list.filter {
                    !it.isCompleted && !it.dueDate.equals("Today", ignoreCase = true) && it.dueDate.isNotBlank()
                }
                TaskFilter.COMPLETED -> list.filter { it.isCompleted }
                TaskFilter.ALL -> list
            }
        }

    val todayCount: Int
        get() = tasks.count { !it.isCompleted && (it.dueDate.equals("Today", ignoreCase = true) || it.dueDate.isBlank()) }

    val upcomingCount: Int
        get() = tasks.count { !it.isCompleted && !it.dueDate.equals("Today", ignoreCase = true) && it.dueDate.isNotBlank() }

    val completedCount: Int
        get() = tasks.count { it.isCompleted }
}

class TaskViewModel(
    private val repository: ProductivityRepository = ProductivityRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState(isLoading = true))
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            sessionManager?.let { sm ->
                val session = sm.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
            }

            val result = repository.getTasks(currentUserId)
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
        _uiState.update { it.copy(isBottomSheetOpen = isOpen) }
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
        category: String = "General"
    ) {
        if (title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isBottomSheetOpen = false) }
            }

            val result = repository.createTask(
                userId = currentUserId,
                title = title.trim(),
                description = description.trim(),
                priority = priority.lowercase(),
                dueDate = dueDate.trim(),
                category = category.trim()
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
}
