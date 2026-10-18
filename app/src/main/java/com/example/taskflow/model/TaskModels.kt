package com.example.taskflow.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Priority(val label: String, val colorHex: Long, val order: Int) {
    LOW("Low", 0xFF0284C7, 1),        // Sky Blue
    MEDIUM("Medium", 0xFFD97706, 2),    // Amber / Orange
    HIGH("High", 0xFFDC2626, 3)         // Red
}

enum class TaskCategory(val label: String, val colorHex: Long) {
    ALL("All", 0xFF475569),
    STUDY("Study", 0xFF7C3AED),        // Purple
    WORK("Work", 0xFF2563EB),          // Blue
    PERSONAL("Personal", 0xFF059669),    // Emerald Green
    HEALTH("Health", 0xFFE11D48),      // Rose
    FINANCE("Finance", 0xFFD97706),    // Amber
    OTHER("Other", 0xFF64748B)         // Slate
}

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean = false,
    val dueDate: String = "Today",
    val dueDateMillis: Long? = null,
    val dueTimeString: String = "12:00 PM",
    val hasReminder: Boolean = false,
    val reminderTimeMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDueDateTime(): String {
        return if (dueDateMillis != null) {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(dueDateMillis))
            "$formattedDate at $dueTimeString"
        } else {
            "$dueDate at $dueTimeString"
        }
    }
}

data class User(
    val name: String,
    val email: String,
    val isLoggedIn: Boolean = true
)

enum class SortOption(val label: String) {
    DATE_CREATED("Date Created"),
    PRIORITY("Priority"),
    DUE_DATE("Due Date"),
    TITLE("Alphabetical")
}
