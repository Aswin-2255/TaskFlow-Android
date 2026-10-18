package com.example.taskflow.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.taskflow.model.Priority
import com.example.taskflow.model.SortOption
import com.example.taskflow.model.Task
import com.example.taskflow.model.TaskCategory
import com.example.taskflow.model.User
import com.example.taskflow.util.TaskReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val user: User? = User("Alex Johnson", "alex@taskflow.com", isLoggedIn = true),
    val searchQuery: String = "",
    val selectedCategory: TaskCategory = TaskCategory.ALL,
    val selectedPriority: Priority? = null,
    val selectedFilter: String = "All", // "All", "Pending", "Completed"
    val sortBy: SortOption = SortOption.DATE_CREATED,
    val isAuthScreenVisible: Boolean = false,
    val isAddSheetOpen: Boolean = false,
    val taskToEdit: Task? = null
)

class TaskViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        val initialTasks = listOf(
            Task(
                id = UUID.randomUUID().toString(),
                title = "Complete Mobile App Architecture",
                description = "Build clean ViewModel and Compose UI for Taskflow project",
                category = TaskCategory.WORK,
                priority = Priority.HIGH,
                completed = false,
                dueDate = "Today",
                dueTimeString = "05:00 PM",
                hasReminder = true,
                reminderTimeMillis = System.currentTimeMillis() + (1000 * 60 * 60 * 2) // 2 hours from now
            ),
            Task(
                id = UUID.randomUUID().toString(),
                title = "Review Machine Learning Notes",
                description = "Prepare summary slides for Chapter 4 - Neural Networks",
                category = TaskCategory.STUDY,
                priority = Priority.MEDIUM,
                completed = false,
                dueDate = "Tomorrow",
                dueTimeString = "10:00 AM",
                hasReminder = true,
                reminderTimeMillis = System.currentTimeMillis() + (1000 * 60 * 60 * 24)
            ),
            Task(
                id = UUID.randomUUID().toString(),
                title = "Gym Session - Leg & Core",
                description = "4 sets squats, leg press, and core workout",
                category = TaskCategory.HEALTH,
                priority = Priority.MEDIUM,
                completed = true,
                dueDate = "Today",
                dueTimeString = "07:00 AM"
            )
        )
        _uiState.update { it.copy(tasks = initialTasks) }
    }

    fun toggleTaskCompletion(taskId: String, context: Context? = null) {
        _uiState.update { currentState ->
            val updatedTasks = currentState.tasks.map { task ->
                if (task.id == taskId) {
                    val updated = task.copy(completed = !task.completed)
                    if (context != null) {
                        if (updated.completed) {
                            TaskReminderScheduler.cancelReminder(context, updated.id)
                        } else if (updated.hasReminder) {
                            TaskReminderScheduler.scheduleReminder(context, updated)
                        }
                    }
                    updated
                } else task
            }
            currentState.copy(tasks = updatedTasks)
        }
    }

    fun saveTask(
        context: Context? = null,
        id: String? = null,
        title: String,
        description: String,
        category: TaskCategory,
        priority: Priority,
        dueDate: String,
        dueDateMillis: Long? = null,
        dueTimeString: String = "12:00 PM",
        hasReminder: Boolean = false,
        reminderTimeMillis: Long? = null
    ) {
        if (title.isBlank()) return

        var savedTask: Task? = null

        _uiState.update { currentState ->
            val updatedTasks = if (id != null) {
                currentState.tasks.map { task ->
                    if (task.id == id) {
                        val updated = task.copy(
                            title = title.trim(),
                            description = description.trim(),
                            category = category,
                            priority = priority,
                            dueDate = dueDate,
                            dueDateMillis = dueDateMillis,
                            dueTimeString = dueTimeString,
                            hasReminder = hasReminder,
                            reminderTimeMillis = reminderTimeMillis
                        )
                        savedTask = updated
                        updated
                    } else task
                }
            } else {
                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    dueDateMillis = dueDateMillis,
                    dueTimeString = dueTimeString,
                    hasReminder = hasReminder,
                    reminderTimeMillis = reminderTimeMillis
                )
                savedTask = newTask
                listOf(newTask) + currentState.tasks
            }
            currentState.copy(
                tasks = updatedTasks,
                isAddSheetOpen = false,
                taskToEdit = null
            )
        }

        savedTask?.let { task ->
            if (context != null) {
                if (task.hasReminder && !task.completed) {
                    TaskReminderScheduler.scheduleReminder(context, task)
                } else {
                    TaskReminderScheduler.cancelReminder(context, task.id)
                }
            }
        }
    }

    fun addVoiceTask(parsed: com.example.taskflow.util.ParsedVoiceTask, context: Context? = null) {
        saveTask(
            context = context,
            id = null,
            title = parsed.title,
            description = "Created via Voice: \"${parsed.rawText}\"",
            category = parsed.category,
            priority = parsed.priority,
            dueDate = parsed.dueDateString,
            dueDateMillis = parsed.dueDateMillis,
            dueTimeString = parsed.dueTimeString,
            hasReminder = parsed.hasReminder,
            reminderTimeMillis = parsed.reminderTimeMillis
        )
    }

    fun deleteTask(taskId: String, context: Context? = null) {
        if (context != null) {
            TaskReminderScheduler.cancelReminder(context, taskId)
        }
        _uiState.update { currentState ->
            currentState.copy(tasks = currentState.tasks.filterNot { it.id == taskId })
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: TaskCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSelectedPriority(priority: Priority?) {
        _uiState.update { it.copy(selectedPriority = priority) }
    }

    fun setSelectedFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun setSortBy(sortOption: SortOption) {
        _uiState.update { it.copy(sortBy = sortOption) }
    }

    fun openAddTaskSheet(taskToEdit: Task? = null) {
        _uiState.update { it.copy(isAddSheetOpen = true, taskToEdit = taskToEdit) }
    }

    fun closeAddTaskSheet() {
        _uiState.update { it.copy(isAddSheetOpen = false, taskToEdit = null) }
    }

    fun login(email: String, name: String) {
        val user = User(
            name = if (name.isBlank()) email.substringBefore("@").replaceFirstChar { it.uppercase() } else name,
            email = email,
            isLoggedIn = true
        )
        _uiState.update { it.copy(user = user, isAuthScreenVisible = false) }
    }

    fun logout() {
        _uiState.update { it.copy(user = null, isAuthScreenVisible = true) }
    }

    fun showAuthScreen() {
        _uiState.update { it.copy(isAuthScreenVisible = true) }
    }

    fun hideAuthScreen() {
        _uiState.update { it.copy(isAuthScreenVisible = false) }
    }
}
