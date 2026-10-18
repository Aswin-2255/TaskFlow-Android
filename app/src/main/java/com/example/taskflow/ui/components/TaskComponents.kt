package com.example.taskflow.ui.components

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.taskflow.model.Priority
import com.example.taskflow.model.SortOption
import com.example.taskflow.model.Task
import com.example.taskflow.model.TaskCategory
import com.example.taskflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val PrimaryTeal = Color(0xFF0F766E)
private val PrimaryMint = Color(0xFFCCFBF1)
private val TextSlate = Color(0xFF0F172A)
private val TextMutedSlate = Color(0xFF64748B)

@Composable
fun ProgressSummaryCard(
    totalCount: Int,
    completedCount: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnimation")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(1.dp, Color(0xFFDCFCE7))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Completion",
                        color = TextSlate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedCount of $totalCount tasks completed",
                        color = TextMutedSlate,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = PrimaryTeal,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryTeal,
                trackColor = Color(0xFFDCFCE7),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(label = "Done", count = completedCount, color = PrimaryTeal)
                StatBadge(label = "Pending", count = pendingCount, color = Color(0xFFD97706))
                StatBadge(label = "Total", count = totalCount, color = Color(0xFF0284C7))
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            color = TextMutedSlate,
            fontSize = 12.sp
        )
        Text(
            text = "$count",
            color = TextSlate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg by animateColorAsState(
        targetValue = if (task.completed) Color(0xFFFAFAFA) else Color.White,
        label = "cardBgColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(
            1.dp,
            if (task.completed) Color(0xFFF1F5F9) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Circular Checkbox
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (task.completed) PrimaryTeal else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (task.completed) PrimaryTeal else Color(0xFFCBD5E1),
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.completed) TextMutedSlate else TextSlate,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        color = TextMutedSlate,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tags row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryTag(category = task.category)
                    PriorityTag(priority = task.priority)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = TextMutedSlate,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = task.getFormattedDueDateTime(),
                            color = TextMutedSlate,
                            fontSize = 11.sp
                        )
                    }

                    if (task.hasReminder) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Reminder Active",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Minimal Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTag(category: TaskCategory) {
    val categoryColor = Color(category.colorHex)
    Surface(
        color = categoryColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = category.label,
                color = categoryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PriorityTag(priority: Priority) {
    val (bgColor, textColor) = when (priority) {
        Priority.HIGH -> PriorityHighBg to PriorityHighText
        Priority.MEDIUM -> PriorityMedBg to PriorityMedText
        Priority.LOW -> PriorityLowBg to PriorityLowText
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = priority.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: TaskCategory,
    onCategorySelect: (TaskCategory) -> Unit,
    selectedPriority: Priority?,
    onPrioritySelect: (Priority?) -> Unit,
    selectedStatus: String,
    onStatusSelect: (String) -> Unit,
    sortBy: SortOption,
    onSortSelect: (SortOption) -> Unit
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Search & Sort bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search tasks...", fontSize = 13.sp, color = TextMutedSlate) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMutedSlate,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMutedSlate,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Menu Button
            Box {
                IconButton(
                    onClick = { isSortMenuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort tasks",
                        tint = TextSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.label,
                                    fontWeight = if (sortBy == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sortBy == option) PrimaryTeal else TextSlate
                                )
                            },
                            onClick = {
                                onSortSelect(option)
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Status Segmented Control ("All", "Pending", "Completed")
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                listOf("All", "Pending", "Completed").forEach { status ->
                    val isSelected = selectedStatus == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { onStatusSelect(status) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryTeal else TextMutedSlate
                        )
                    }
                }
            }
        }

        // Priority Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Priority:", fontSize = 11.sp, color = TextMutedSlate, fontWeight = FontWeight.SemiBold)
            FilterChip(
                selected = selectedPriority == null,
                onClick = { onPrioritySelect(null) },
                label = { Text("All", fontSize = 11.sp) },
                shape = RoundedCornerShape(8.dp)
            )
            Priority.entries.forEach { priority ->
                FilterChip(
                    selected = selectedPriority == priority,
                    onClick = { onPrioritySelect(priority) },
                    label = { Text(priority.label, fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Categories Scrollable Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(TaskCategory.entries) { category ->
                val isSelected = selectedCategory == category

                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = { Text(category.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTeal,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: Task?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: TaskCategory,
        priority: Priority,
        dueDate: String,
        dueDateMillis: Long?,
        dueTimeString: String,
        hasReminder: Boolean,
        reminderTimeMillis: Long?
    ) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember {
        Calendar.getInstance().apply {
            if (taskToEdit?.dueDateMillis != null) {
                timeInMillis = taskToEdit.dueDateMillis
            }
        }
    }

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: TaskCategory.STUDY) }
    var selectedPriority by remember { mutableStateOf(taskToEdit?.priority ?: Priority.MEDIUM) }

    var selectedDateMillis by remember { mutableStateOf(taskToEdit?.dueDateMillis ?: calendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var hasReminder by remember { mutableStateOf(taskToEdit?.hasReminder ?: false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val dateDisplayString = dateFormat.format(Date(selectedDateMillis))

    fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(cal.time)
    }

    val timeDisplayString = formatTime(selectedHour, selectedMinute)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            hasReminder = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (taskToEdit != null) "Edit Task" else "New Task",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    cursorColor = PrimaryTeal
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes / Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    cursorColor = PrimaryTeal
                )
            )

            // Category choice chips
            Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlate)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(TaskCategory.entries.filterNot { it == TaskCategory.ALL }) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        leadingIcon = {
                            Icon(getCategoryIcon(category), contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text(category.label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Priority choice chips
            Text("Priority", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlate)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Date & Time pickers
            Text("Schedule & Alarm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val updatedCal = Calendar.getInstance()
                                updatedCal.set(year, month, dayOfMonth, selectedHour, selectedMinute)
                                selectedDateMillis = updatedCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(dateDisplayString, fontSize = 12.sp, color = TextSlate)
                }

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                selectedHour = hourOfDay
                                selectedMinute = minute
                                val updatedCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                updatedCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                updatedCal.set(Calendar.MINUTE, minute)
                                selectedDateMillis = updatedCal.timeInMillis
                            },
                            selectedHour,
                            selectedMinute,
                            false
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(timeDisplayString, fontSize = 12.sp, color = TextSlate)
                }
            }

            // Notification Reminder Toggle
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Notify me at $timeDisplayString",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSlate
                        )
                    }

                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { isChecked ->
                            hasReminder = isChecked
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryTeal,
                            checkedTrackColor = PrimaryMint
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Action
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalReminderCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                        }
                        val reminderMillis = if (hasReminder) finalReminderCal.timeInMillis else null

                        onSave(
                            title,
                            description,
                            selectedCategory,
                            selectedPriority,
                            dateDisplayString,
                            selectedDateMillis,
                            timeDisplayString,
                            hasReminder,
                            reminderMillis
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text(
                    text = if (taskToEdit != null) "Update Task" else "Save Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun getCategoryIcon(category: TaskCategory): ImageVector = when (category) {
    TaskCategory.ALL -> Icons.Default.FilterList
    TaskCategory.STUDY -> Icons.Outlined.School
    TaskCategory.WORK -> Icons.Outlined.Work
    TaskCategory.PERSONAL -> Icons.Outlined.Home
    TaskCategory.HEALTH -> Icons.Outlined.FitnessCenter
    TaskCategory.FINANCE -> Icons.Outlined.Payments
    TaskCategory.OTHER -> Icons.Outlined.Bookmark
}
