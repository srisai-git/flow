package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.simats.focusflow.ui.components.AmbientMeshGlowHeader
import com.simats.focusflow.ui.components.FlowLoadingIndicator
import com.simats.focusflow.ui.components.TasksSkeleton
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
    onNavigateToTimer: () -> Unit = {},
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

    val state by viewModel.uiState.collectAsState()
    val filteredTasks = remember(state.tasks, state.searchQuery, state.selectedFilter) {
        state.filteredTasks
    }
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateTask() },
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 8.dp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            AmbientMeshGlowHeader(height = 240.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

            if (state.isLoading) {
                TasksSkeleton()
            } else if (filteredTasks.isEmpty()) {
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
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            currentTimeMillis = currentTimeMillis,
                            onToggle = { viewModel.toggleTask(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onEdit = { viewModel.openEditTask(task) }
                        )
                    }
                }
            }
        }
        }

        if (state.isBottomSheetOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.setBottomSheetOpen(false) },
                sheetState = sheetState,
                containerColor = colors.surface,
                scrimColor = Color.Black.copy(alpha = 0.55f),
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
                    taskToEdit = state.editingTask,
                    onDismiss = { viewModel.setBottomSheetOpen(false) },
                    onCreateTask = { title, desc, priority, dueDate, category, scheduledTime, deadlineTimestamp ->
                        viewModel.createTask(title, desc, priority, dueDate, category, scheduledTime, deadlineTimestamp)
                    },
                    onUpdateTask = { taskId, title, desc, priority, dueDate, category, scheduledTime, deadlineTimestamp, clientId ->
                        viewModel.updateTask(taskId, title, desc, priority, dueDate, category, scheduledTime, deadlineTimestamp, clientId)
                    }
                )
            }
        }
    }
}

private fun resolveLegacyDeadlineToMillis(dueDate: String, scheduledTime: String?): Long? {
    if (scheduledTime.isNullOrBlank()) return null
    return try {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val parsedTime = timeFormat.parse(scheduledTime.trim()) ?: return null
        val timeCal = Calendar.getInstance().apply { time = parsedTime }
        val targetCal = Calendar.getInstance()

        if (dueDate.equals("Tomorrow", ignoreCase = true)) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
        } else if (!dueDate.equals("Today", ignoreCase = true) && dueDate.isNotBlank()) {
            val dateFormats = listOf(
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )
            for (df in dateFormats) {
                try {
                    val d = df.parse(dueDate)
                    if (d != null) {
                        targetCal.time = d
                        break
                    }
                } catch (_: Exception) {}
            }
        }
        targetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
        targetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
        targetCal.set(Calendar.SECOND, 0)
        targetCal.set(Calendar.MILLISECOND, 0)
        targetCal.timeInMillis
    } catch (_: Exception) {
        null
    }
}

private fun formatDeadlineTime(timestamp: Long): String {
    val deadlineCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCal = Calendar.getInstance()
    val isToday = deadlineCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            deadlineCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = deadlineCal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
            deadlineCal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)

    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    return when {
        isToday -> "Today, $timeStr"
        isTomorrow -> "Tomorrow, $timeStr"
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun TaskItemCard(
    task: TaskItem,
    currentTimeMillis: Long,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val colors = FocusFlowTheme.colors
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val hairlineBorder = colors.cardBorderBrush

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, hairlineBorder)
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
                    checkedColor = Color(0xFF8B5CF6),
                    uncheckedColor = colors.textMuted,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEdit() }
            ) {
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
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (task.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = task.category,
                                fontSize = 10.5.sp,
                                color = if (colors.isDark) Color(0xFFC4B5FD) else colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    val resolvedDeadline = task.deadlineTimestamp?.takeIf { it > 0 }
                        ?: resolveLegacyDeadlineToMillis(task.dueDate, task.scheduledTime)

                    if (resolvedDeadline != null) {
                        val deadlineStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(resolvedDeadline))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = deadlineStr,
                                    fontSize = 10.5.sp,
                                    color = colors.textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        val diffMillis = resolvedDeadline - currentTimeMillis
                        if (!task.isCompleted && diffMillis <= 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF5A5F).copy(alpha = 0.14f))
                                    .border(1.dp, Color(0xFFFF5A5F).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5A5F),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Overdue",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFFF5A5F),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            val deadlineText = formatDeadlineTime(resolvedDeadline)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.14f))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = deadlineText,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        if (task.dueDate.isNotBlank() && task.dueDate != "0000-00-00") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
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
                                        fontSize = 10.5.sp,
                                        color = colors.textMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (!task.scheduledTime.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.12f))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = task.scheduledTime,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityColor.copy(alpha = 0.14f))
                            .border(1.dp, priorityColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.priority.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    if (!task.isSynced) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.14f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Syncing",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sync pending",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task",
                        tint = colors.textMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp)
                    )
                }

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
}

@Composable
private fun AddTaskBottomSheetContent(
    taskToEdit: TaskItem? = null,
    onDismiss: () -> Unit,
    onCreateTask: (title: String, desc: String, priority: String, dueDate: String, category: String, scheduledTime: String?, deadlineTimestamp: Long?) -> Unit,
    onUpdateTask: (taskId: Int, title: String, desc: String, priority: String, dueDate: String, category: String, scheduledTime: String?, deadlineTimestamp: Long?, clientId: String) -> Unit
) {
    val colors = FocusFlowTheme.colors
    val context = LocalContext.current

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedPriority by remember {
        mutableStateOf(
            taskToEdit?.priority?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "Medium"
        )
    }
    var selectedDueDate by remember { mutableStateOf(taskToEdit?.dueDate?.ifBlank { "Today" } ?: "Today") }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category?.ifBlank { "Deep Work" } ?: "Deep Work") }
    var selectedScheduledTime by remember { mutableStateOf<String?>(taskToEdit?.scheduledTime) }
    var selectedDeadlineTimestamp by remember { mutableStateOf<Long?>(taskToEdit?.deadlineTimestamp) }

    val priorities = listOf("Low", "Medium", "High")
    val dueDates = listOf("Today", "Tomorrow", "Next Week")
    val categories = listOf("Deep Work", "Coding", "Study", "Creative", "General")

    var showDatePicker by remember { mutableStateOf(false) }
    val initialDateMillis = remember(selectedDeadlineTimestamp) {
        selectedDeadlineTimestamp ?: System.currentTimeMillis()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val pickedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
                        val targetCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, pickedCal.get(Calendar.YEAR))
                            set(Calendar.MONTH, pickedCal.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, pickedCal.get(Calendar.DAY_OF_MONTH))
                        }
                        selectedDueDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(targetCal.time)

                        val timeCal = Calendar.getInstance()
                        selectedDeadlineTimestamp?.let { timeCal.timeInMillis = it }
                        targetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                        targetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        targetCal.set(Calendar.SECOND, 0)
                        targetCal.set(Calendar.MILLISECOND, 0)
                        selectedDeadlineTimestamp = targetCal.timeInMillis
                        if (selectedScheduledTime == null) {
                            selectedScheduledTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(targetCal.time)
                        }
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val openTimePicker: () -> Unit = {
        val cal = Calendar.getInstance()
        selectedDeadlineTimestamp?.let { cal.timeInMillis = it }
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val targetCal = Calendar.getInstance()
                selectedDeadlineTimestamp?.let { targetCal.timeInMillis = it } ?: run {
                    if (selectedDueDate.equals("Tomorrow", ignoreCase = true)) {
                        targetCal.add(Calendar.DAY_OF_YEAR, 1)
                    } else if (!selectedDueDate.equals("Today", ignoreCase = true) && selectedDueDate.isNotBlank()) {
                        val dateFormats = listOf(
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()),
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        )
                        for (df in dateFormats) {
                            try {
                                val d = df.parse(selectedDueDate)
                                if (d != null) {
                                    targetCal.time = d
                                    break
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
                targetCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                targetCal.set(Calendar.MINUTE, minute)
                targetCal.set(Calendar.SECOND, 0)
                targetCal.set(Calendar.MILLISECOND, 0)

                selectedDeadlineTimestamp = targetCal.timeInMillis
                selectedScheduledTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(targetCal.time)
                if (selectedDueDate.isBlank()) {
                    selectedDueDate = "Today"
                }
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    val setOffsetHours: (Int) -> Unit = { hours ->
        val targetCal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDeadlineTimestamp = targetCal.timeInMillis
        selectedScheduledTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(targetCal.time)
        val nowCal = Calendar.getInstance()
        selectedDueDate = if (targetCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) "Today" else "Tomorrow"
    }

    val setTonight: () -> Unit = {
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        selectedDeadlineTimestamp = targetCal.timeInMillis
        selectedScheduledTime = "09:00 PM"
        val nowCal = Calendar.getInstance()
        selectedDueDate = if (targetCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) "Today" else "Tomorrow"
    }

    val setTomorrowMorning: () -> Unit = {
        val targetCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDeadlineTimestamp = targetCal.timeInMillis
        selectedScheduledTime = "09:00 AM"
        selectedDueDate = "Tomorrow"
    }

    val clearDeadline: () -> Unit = {
        selectedDeadlineTimestamp = null
        selectedScheduledTime = null
    }

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
                text = if (taskToEdit != null) "Edit Focus Task" else "New Focus Task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DUE DATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight, letterSpacing = 1.sp)
            TextButton(
                onClick = { showDatePicker = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick Date",
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedDueDate !in dueDates) selectedDueDate else "Select Date",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }
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

        Text("CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it }) { cat ->
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CUSTOM DEADLINE TIMER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight, letterSpacing = 1.sp)
            if (selectedScheduledTime != null) {
                Text(
                    text = selectedScheduledTime!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA78BFA)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .bounceClick()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pick Date",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Deadline Date", fontSize = 10.sp, color = colors.textMuted)
                        Text(
                            text = selectedDueDate.ifBlank { "Select Date" },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            Surface(
                onClick = openTimePicker,
                shape = RoundedCornerShape(12.dp),
                color = if (selectedScheduledTime != null) Color(0xFF8B5CF6).copy(alpha = 0.12f) else colors.surfaceVariant,
                border = BorderStroke(1.dp, if (selectedScheduledTime != null) Color(0xFF8B5CF6).copy(alpha = 0.40f) else colors.border),
                modifier = Modifier
                    .weight(1f)
                    .bounceClick()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Pick Time",
                        tint = if (selectedScheduledTime != null) Color(0xFF8B5CF6) else colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Deadline Time", fontSize = 10.sp, color = colors.textMuted)
                        Text(
                            text = selectedScheduledTime ?: "Set Time",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedScheduledTime != null) Color(0xFF8B5CF6) else colors.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { setOffsetHours(1) },
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("+1 Hour", fontSize = 11.sp, color = colors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Surface(
                onClick = { setOffsetHours(3) },
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("+3 Hours", fontSize = 11.sp, color = colors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Surface(
                onClick = setTonight,
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("Tonight 9 PM", fontSize = 11.sp, color = colors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            Surface(
                onClick = setTomorrowMorning,
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("Tomorrow 9 AM", fontSize = 11.sp, color = colors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
            if (selectedDeadlineTimestamp != null || selectedScheduledTime != null) {
                Surface(
                    onClick = clearDeadline,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF5A5F).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFF5A5F).copy(alpha = 0.35f))
                ) {
                    Text("Clear", fontSize = 11.sp, color = Color(0xFFFF5A5F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        if (selectedDeadlineTimestamp != null) {
            Spacer(modifier = Modifier.height(10.dp))
            val now = System.currentTimeMillis()
            val diff = selectedDeadlineTimestamp!! - now
            val remainingText = if (diff > 0) formatDeadlineTime(selectedDeadlineTimestamp!!) else "Overdue target"
            val badgeColor = if (diff > 0) Color(0xFF38BDF8) else Color(0xFFFF5A5F)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (diff > 0) Icons.Default.Schedule else Icons.Default.Warning,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Target: $selectedDueDate at ${selectedScheduledTime ?: ""} ($remainingText)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    if (taskToEdit != null) {
                        onUpdateTask(
                            taskToEdit.id,
                            title,
                            description,
                            selectedPriority,
                            selectedDueDate,
                            selectedCategory,
                            selectedScheduledTime,
                            selectedDeadlineTimestamp,
                            taskToEdit.clientId
                        )
                    } else {
                        onCreateTask(
                            title,
                            description,
                            selectedPriority,
                            selectedDueDate,
                            selectedCategory,
                            selectedScheduledTime,
                            selectedDeadlineTimestamp
                        )
                    }
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = if (taskToEdit != null) "Save Changes" else "Create Task",
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
