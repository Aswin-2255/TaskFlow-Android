package com.example.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskflow.model.SortOption
import com.example.taskflow.model.TaskCategory
import com.example.taskflow.ui.components.AddEditTaskDialog
import com.example.taskflow.ui.components.FilterSection
import com.example.taskflow.ui.components.ProgressSummaryCard
import com.example.taskflow.ui.components.TaskCard
import com.example.taskflow.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryTeal = Color(0xFF0F766E)
private val BackgroundColor = Color(0xFFF8FAFC)
private val TextDark = Color(0xFF0F172A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var isProfileMenuExpanded by remember { mutableStateOf(false) }

    val todayString = remember {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        sdf.format(Date())
    }

    // Filter & Sort logic
    val filteredTasks = remember(
        uiState.tasks,
        uiState.searchQuery,
        uiState.selectedCategory,
        uiState.selectedPriority,
        uiState.selectedFilter,
        uiState.sortBy
    ) {
        uiState.tasks.filter { task ->
            val matchesSearch = uiState.searchQuery.isBlank() ||
                    task.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    task.description.contains(uiState.searchQuery, ignoreCase = true)

            val matchesCategory = uiState.selectedCategory == TaskCategory.ALL ||
                    task.category == uiState.selectedCategory

            val matchesPriority = uiState.selectedPriority == null ||
                    task.priority == uiState.selectedPriority

            val matchesStatus = when (uiState.selectedFilter) {
                "Pending" -> !task.completed
                "Completed" -> task.completed
                else -> true
            }

            matchesSearch && matchesCategory && matchesPriority && matchesStatus
        }.sortedWith { task1, task2 ->
            when (uiState.sortBy) {
                SortOption.DATE_CREATED -> task2.createdAt.compareTo(task1.createdAt)
                SortOption.PRIORITY -> task2.priority.order.compareTo(task1.priority.order)
                SortOption.DUE_DATE -> task1.dueDate.compareTo(task2.dueDate)
                SortOption.TITLE -> task1.title.compareTo(task2.title, ignoreCase = true)
            }
        }
    }

    val totalCount = uiState.tasks.size
    val completedCount = uiState.tasks.count { it.completed }
    val pendingCount = totalCount - completedCount

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = todayString.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Hello, ${uiState.user?.name?.substringBefore(" ") ?: "Guest"} 👋",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                },
                actions = {
                    Box {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(38.dp)
                                .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape)
                                .clickable { isProfileMenuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.user?.name?.take(2)?.uppercase() ?: "GU",
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        DropdownMenu(
                            expanded = isProfileMenuExpanded,
                            onDismissRequest = { isProfileMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(uiState.user?.email ?: "Guest User") },
                                onClick = { },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                enabled = false
                            )
                            HorizontalDivider()
                            if (uiState.user == null) {
                                DropdownMenuItem(
                                    text = { Text("Sign In") },
                                    onClick = {
                                        isProfileMenuExpanded = false
                                        viewModel.showAuthScreen()
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Log Out", color = Color(0xFFEF4444)) },
                                    onClick = {
                                        isProfileMenuExpanded = false
                                        viewModel.logout()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddTaskSheet() },
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Task", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
        ) {
            // Daily Progress Summary Card
            ProgressSummaryCard(
                totalCount = totalCount,
                completedCount = completedCount,
                pendingCount = pendingCount,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Filter Section
            FilterSection(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedCategory = uiState.selectedCategory,
                onCategorySelect = { viewModel.setSelectedCategory(it) },
                selectedPriority = uiState.selectedPriority,
                onPrioritySelect = { viewModel.setSelectedPriority(it) },
                selectedStatus = uiState.selectedFilter,
                onStatusSelect = { viewModel.setSelectedFilter(it) },
                sortBy = uiState.sortBy,
                onSortSelect = { viewModel.setSortBy(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No tasks found",
                            color = Color(0xFF64748B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap '+ New Task' to add your first goal.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                val context = LocalContext.current
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 85.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task.id, context) },
                            onEdit = { viewModel.openAddTaskSheet(taskToEdit = task) },
                            onDelete = { viewModel.deleteTask(task.id, context) }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add/Edit Task
    if (uiState.isAddSheetOpen) {
        val context = LocalContext.current
        AddEditTaskDialog(
            taskToEdit = uiState.taskToEdit,
            onDismiss = { viewModel.closeAddTaskSheet() },
            onSave = { title, description, category, priority, dueDate, dueDateMillis, dueTimeString, hasReminder, reminderTimeMillis ->
                viewModel.saveTask(
                    context = context,
                    id = uiState.taskToEdit?.id,
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    dueDateMillis = dueDateMillis,
                    dueTimeString = dueTimeString,
                    hasReminder = hasReminder,
                    reminderTimeMillis = reminderTimeMillis
                )
            }
        )
    }
}
