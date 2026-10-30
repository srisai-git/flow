package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.TaskFilter
import com.simats.focusflow.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProgressConsistency: () -> Unit = {},
    onNavigateToEditTask: () -> Unit = {},
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setBottomSheetOpen(true) },
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp, end = 12.dp)
                    .bounceClick()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. HEADER ROW & REFRESH ACTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK MANAGER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your Focus Tasks",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                }

                // Total pending badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${state.todayCount} due today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. SEARCH BAR
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search tasks by title or category...", color = colors.textMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = colors.textMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CATEGORY / FILTER TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TaskFilter.values().forEach { filter ->
                    val isSelected = state.selectedFilter == filter
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) colors.primary else Color.Transparent,
                        label = "filterTabBg"
                    )

                    val count = when (filter) {
                        TaskFilter.TODAY -> state.todayCount
                        TaskFilter.UPCOMING -> state.upcomingCount
                        TaskFilter.COMPLETED -> state.completedCount
                        TaskFilter.ALL -> state.tasks.size
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tabBg)
                            .clickable { viewModel.setFilter(filter) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.title} ($count)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. TASK LIST
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(36.dp))
                }
            } else if (state.filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = when (state.selectedFilter) {
                                TaskFilter.TODAY -> "No tasks for today."
                                TaskFilter.UPCOMING -> "No upcoming scheduled tasks."
                                TaskFilter.COMPLETED -> "No completed tasks yet."
                                TaskFilter.ALL -> "No tasks recorded."
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add your first priority.",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(state.filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }

        // 5. ADD TASK BOTTOM SHEET
        if (state.isBottomSheetOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.setBottomSheetOpen(false) },
                sheetState = sheetState,
                containerColor = colors.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(colors.border)
                    )
                }
            ) {
                AddTaskBottomSheetContent(
                    onDismiss = { viewModel.setBottomSheetOpen(false) },
                    onCreateTask = { title, desc, priority, dueDate, category ->
                        viewModel.createTask(title, desc, priority, dueDate, category)
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskItemCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    uncheckedColor = colors.textMuted,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) colors.textMuted else colors.textPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Tag
                    if (task.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category,
                                fontSize = 10.sp,
                                color = colors.primaryLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Due Date Tag
                    if (task.dueDate.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.dueDate,
                                    fontSize = 10.sp,
                                    color = colors.textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Priority Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Action Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = colors.textMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddTaskBottomSheetContent(
    onDismiss: () -> Unit,
    onCreateTask: (title: String, desc: String, priority: String, dueDate: String, category: String) -> Unit
) {
    val colors = FocusFlowTheme.colors
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDueDate by remember { mutableStateOf("Today") }
    var selectedCategory by remember { mutableStateOf("Deep Work") }

    val priorities = listOf("Low", "Medium", "High")
    val dueDates = listOf("Today", "Tomorrow", "Next Week")
    val categories = listOf("Deep Work", "Coding", "Study", "Creative", "General")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Focus Task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title *", color = colors.textMuted) },
            placeholder = { Text("e.g. Implement Compose state flow", color = colors.textMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)", color = colors.textMuted) },
            placeholder = { Text("Additional notes or sub-steps...", color = colors.textMuted) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Priority Selector
        Text("PRIORITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            priorities.forEach { p ->
                val isSelected = selectedPriority == p
                Surface(
                    onClick = { selectedPriority = p },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.primary else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = p,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Due Date Selector
        Text("DUE DATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dueDates.forEach { d ->
                val isSelected = selectedDueDate == d
                Surface(
                    onClick = { selectedDueDate = d },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.primary else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = d,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selector
        Text("CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.primaryLight else colors.textMuted,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onCreateTask(title, description, selectedPriority, selectedDueDate, selectedCategory)
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                disabledContainerColor = colors.primary.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = "Create Task",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    TasksScreen()
}
