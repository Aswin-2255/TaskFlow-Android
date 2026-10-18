package com.example.taskflow.util

import com.example.taskflow.model.Priority
import com.example.taskflow.model.TaskCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedVoiceTask(
    val rawText: String,
    val title: String,
    val category: TaskCategory,
    val priority: Priority,
    val dueDateString: String,
    val dueDateMillis: Long,
    val dueTimeString: String,
    val hasReminder: Boolean,
    val reminderTimeMillis: Long?
)

object VoiceTaskParser {

    fun parse(spokenText: String): ParsedVoiceTask {
        val originalText = spokenText.trim()
        var workingText = originalText.lowercase(Locale.getDefault())

        val calendar = Calendar.getInstance()
        var hour = 17 // Default to 5:00 PM if time specified without exact hour
        var minute = 0
        var hasExplicitTime = false
        var hasExplicitDate = false

        // 1. Time Parsing via Regex
        val timeRegex = Pattern.compile("(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)
        val timeMatcher = timeRegex.matcher(workingText)

        // Search for time patterns like "at 5 PM", "5:30 pm", "at 10 am"
        val timeMatchResults = mutableListOf<String>()
        while (timeMatcher.find()) {
            val fullMatch = timeMatcher.group(0) ?: ""
            val hourStr = timeMatcher.group(1)
            val minuteStr = timeMatcher.group(2)
            val amPmStr = timeMatcher.group(3)

            if (hourStr != null) {
                var parsedHour = hourStr.toIntOrNull() ?: 5
                val parsedMinute = minuteStr?.toIntOrNull() ?: 0

                if (amPmStr != null) {
                    val amPm = amPmStr.lowercase()
                    if (amPm == "pm" && parsedHour < 12) parsedHour += 12
                    if (amPm == "am" && parsedHour == 12) parsedHour = 0
                    hasExplicitTime = true
                    hour = parsedHour
                    minute = parsedMinute
                    timeMatchResults.add(fullMatch)
                } else if (fullMatch.contains("at ")) {
                    // "at 5" -> assume 5 PM unless 12
                    if (parsedHour in 1..11) parsedHour += 12
                    hasExplicitTime = true
                    hour = parsedHour
                    minute = parsedMinute
                    timeMatchResults.add(fullMatch)
                }
            }
        }

        // Remove matched time substrings from working text
        timeMatchResults.forEach { match ->
            workingText = workingText.replace(match, "").trim()
        }

        // 2. Date Parsing
        if (workingText.contains("day after tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 2)
            workingText = workingText.replace("day after tomorrow", "").trim()
            hasExplicitDate = true
        } else if (workingText.contains("tomorrow")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            workingText = workingText.replace("tomorrow", "").trim()
            hasExplicitDate = true
        } else if (workingText.contains("today")) {
            workingText = workingText.replace("today", "").trim()
            hasExplicitDate = true
        } else if (workingText.contains("next week")) {
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            workingText = workingText.replace("next week", "").trim()
            hasExplicitDate = true
        } else {
            // Check for weekdays: "next monday", "on tuesday"
            val weekdays = mapOf(
                "monday" to Calendar.MONDAY,
                "tuesday" to Calendar.TUESDAY,
                "wednesday" to Calendar.WEDNESDAY,
                "thursday" to Calendar.THURSDAY,
                "friday" to Calendar.FRIDAY,
                "saturday" to Calendar.SATURDAY,
                "sunday" to Calendar.SUNDAY
            )

            for ((dayName, dayOfWeek) in weekdays) {
                if (workingText.contains(dayName)) {
                    while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    if (workingText.contains("next $dayName")) {
                        calendar.add(Calendar.DAY_OF_YEAR, 7)
                        workingText = workingText.replace("next $dayName", "")
                    } else {
                        workingText = workingText.replace("on $dayName", "").replace(dayName, "")
                    }
                    hasExplicitDate = true
                    break
                }
            }
        }

        // 3. Priority Deduction
        val priority = when {
            workingText.contains("urgent") || workingText.contains("important") ||
                    workingText.contains("asap") || workingText.contains("high priority") -> {
                workingText = workingText.replace("urgent", "").replace("important", "").replace("asap", "").replace("high priority", "")
                Priority.HIGH
            }
            workingText.contains("low priority") -> {
                workingText = workingText.replace("low priority", "")
                Priority.LOW
            }
            else -> Priority.MEDIUM
        }

        // 4. Category Deduction
        val category = when {
            containsAny(workingText, "assignment", "homework", "study", "exam", "lecture", "chapter", "reading", "quiz", "paper", "school") -> TaskCategory.STUDY
            containsAny(workingText, "work", "project", "meeting", "client", "email", "report", "presentation", "office", "code", "bug") -> TaskCategory.WORK
            containsAny(workingText, "gym", "workout", "run", "exercise", "doctor", "medication", "walk", "health", "yoga", "fitness") -> TaskCategory.HEALTH
            containsAny(workingText, "pay", "bill", "grocery", "groceries", "buy", "rent", "bank", "tax", "subscription", "money", "purchase") -> TaskCategory.FINANCE
            containsAny(workingText, "clean", "home", "cook", "laundry", "call", "family", "birthday", "friend") -> TaskCategory.PERSONAL
            else -> TaskCategory.OTHER
        }

        // 5. Clean Title
        var cleanTitle = workingText
            .replace(Regex("(?i)\\b(at|on|for|by|to|the)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanTitle.isBlank()) {
            cleanTitle = originalText
        } else {
            cleanTitle = cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        // 6. Set Final Calendar Date & Time
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val dueDateString = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dueTimeString = timeFormat.format(calendar.time)

        val hasReminder = hasExplicitTime || hasExplicitDate
        val reminderTimeMillis = if (hasReminder) calendar.timeInMillis else null

        return ParsedVoiceTask(
            rawText = originalText,
            title = cleanTitle,
            category = category,
            priority = priority,
            dueDateString = dueDateString,
            dueDateMillis = calendar.timeInMillis,
            dueTimeString = dueTimeString,
            hasReminder = hasReminder,
            reminderTimeMillis = reminderTimeMillis
        )
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { keyword -> text.contains(keyword) }
    }
}
