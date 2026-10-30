package com.simats.focusflow.ui.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.HabitUserStats
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.GrootAvatar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.HabitInsightTimeframe
import com.simats.focusflow.ui.viewmodel.HabitTab
import com.simats.focusflow.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

fun resolveHabitIcon(iconKey: String): ImageVector = when (iconKey.lowercase()) {
    "bolt", "flash" -> Icons.Default.FlashOn
    "book", "read" -> Icons.Default.MenuBook
    "code", "laptop" -> Icons.Default.Code
    "water" -> Icons.Default.WaterDrop
    "fitness", "run" -> Icons.Default.FitnessCenter
    "edit", "write" -> Icons.Default.Edit
    "target", "focus" -> Icons.Default.TrackChanges
    "night", "sleep" -> Icons.Default.Bedtime
    "star" -> Icons.Default.Star
    else -> Icons.Default.Repeat
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGrootAi: () -> Unit = {},
    viewModel: HabitViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isStreakModalOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val animatedProgress by animateFloatAsState(
        targetValue = state.completionRate,
        animationSpec = tween(durationMillis = 600),
        label = "habitProgress"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        floatingActionButton = {
            if (state.selectedTab == HabitTab.ROUTINE) {
                FloatingActionButton(
                    onClick = { viewModel.setAddHabitDialogOpen(true) },
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 80.dp, end = 12.dp)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit",
                        modifier = Modifier.size(28.dp)
                    )
                }
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

            // 1. TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABIT TRACKER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Build Consistency",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = currentDateStr,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. TAB SWITCHER (Today's Routine vs Insights & Trends)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf(
                    HabitTab.ROUTINE to "Today's Routine",
                    HabitTab.INSIGHTS to "Insights & Trends"
                ).forEach { (tab, label) ->
                    val isSelected = state.selectedTab == tab
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) colors.primary else Color.Transparent,
                        label = "tabBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else colors.textMuted,
                        label = "textColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tabBg)
                            .clickable { viewModel.selectTab(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (state.selectedTab == HabitTab.ROUTINE) {
                // DAILY PROGRESS CARD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S COMPLETION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${state.completedCount} / ${state.totalCount} Done",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.primary,
                            trackColor = colors.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (state.totalCount > 0 && state.completedCount == state.totalCount) {
                                "Outstanding! All daily habits completed today."
                            } else {
                                "Keep your momentum going. Tap a habit to complete."
                            },
                            fontSize = 12.sp,
                            color = if (state.totalCount > 0 && state.completedCount == state.totalCount) colors.primaryLight else colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // HABIT LIST SECTION
                Text(
                    text = "ACTIVE HABITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(36.dp))
                    }
                } else if (state.habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = colors.primaryLight,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No habits tracked yet!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+' below to start building your daily routine.",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(state.habits, key = { it.id }) { habit ->
                            HabitCard(
                                habit = habit,
                                onToggle = { viewModel.toggleHabit(habit.id) },
                                onStreakClick = {
                                    viewModel.selectHabitForStreak(habit)
                                    isStreakModalOpen = true
                                }
                            )
                        }
                    }
                }
            } else {
                // INSIGHTS & TRENDS VIEW (Powered by real MySQL user stats & chart history)
                HabitsInsightsView(
                    habits = state.habits,
                    userStats = state.userStats,
                    aiInsight = state.aiInsight,
                    isAiLoading = state.isAiLoading,
                    selectedTimeframe = state.selectedTimeframe,
                    onSelectTimeframe = { viewModel.selectTimeframe(it) },
                    onRefreshAi = { viewModel.loadAiInsight(forceRefresh = true) },
                    onNavigateToGrootAi = onNavigateToGrootAi,
                    onHabitClick = {
                        viewModel.selectHabitForStreak(it)
                        isStreakModalOpen = true
                    }
                )
            }
        }

        // ADD HABIT DIALOG
        if (state.isAddHabitDialogOpen) {
            AddHabitDialog(
                onDismiss = { viewModel.setAddHabitDialogOpen(false) },
                onConfirm = { title, icon, frequency ->
                    viewModel.createHabit(title, icon, frequency)
                }
            )
        }

        // INTERACTIVE STREAK DETAILS BOTTOM SHEET (Elevated Material3 matching reference card design)
        if (isStreakModalOpen || state.selectedHabitForStreak != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    isStreakModalOpen = false
                    viewModel.selectHabitForStreak(null)
                },
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
                StreakDetailsSheetContent(
                    habit = state.selectedHabitForStreak,
                    userStats = state.userStats,
                    onDismiss = {
                        isStreakModalOpen = false
                        viewModel.selectHabitForStreak(null)
                    },
                    onDelete = { habitId ->
                        viewModel.deleteHabit(habitId)
                        isStreakModalOpen = false
                        viewModel.selectHabitForStreak(null)
                    }
                )
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: HabitItem,
    onToggle: () -> Unit,
    onStreakClick: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    val isDone = habit.isCompletedToday
    val cardBg by animateColorAsState(
        targetValue = if (isDone) colors.primary.copy(alpha = 0.12f) else colors.surface,
        label = "habitBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDone) colors.primary.copy(alpha = 0.5f) else colors.border,
        label = "habitBorder"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .bounceClick(),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveHabitIcon(habit.icon),
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Habit Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Interactive Streak Badge
                    Surface(
                        onClick = onStreakClick,
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                        modifier = Modifier.bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${habit.streakCount}d Streak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "•  ${habit.frequency.replaceFirstChar { it.uppercase() }}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Completion Toggle Checkbox Circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDone) colors.primary else colors.surfaceVariant)
                    .border(1.5.dp, if (isDone) colors.primary else colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitsInsightsView(
    habits: List<HabitItem>,
    userStats: HabitUserStats,
    aiInsight: String?,
    isAiLoading: Boolean,
    selectedTimeframe: HabitInsightTimeframe,
    onSelectTimeframe: (HabitInsightTimeframe) -> Unit,
    onRefreshAi: () -> Unit,
    onNavigateToGrootAi: () -> Unit,
    onHabitClick: (HabitItem) -> Unit
) {
    val colors = FocusFlowTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 100.dp)
    ) {
        // AI Productivity Insights Card
        AiProductivityInsightsCard(
            insight = aiInsight,
            isLoading = isAiLoading,
            onRefresh = onRefreshAi,
            onOpenGrootAssistant = onNavigateToGrootAi
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Timeframe Selector: Day, Week, Month
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            HabitInsightTimeframe.values().forEach { tf ->
                val isSelected = selectedTimeframe == tf
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .clickable { onSelectTimeframe(tf) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Canvas Chart Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (selectedTimeframe) {
                                HabitInsightTimeframe.DAY -> "Today's Habit Distribution"
                                HabitInsightTimeframe.WEEK -> "7-Day Completion Rate"
                                HabitInsightTimeframe.MONTH -> "4-Week Consistency Trend"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (selectedTimeframe) {
                                HabitInsightTimeframe.DAY -> "${habits.count { it.isCompletedToday }}/${habits.size} Done"
                                HabitInsightTimeframe.WEEK -> "Avg ${userStats.avgConsistencyPercent.toInt()}%"
                                HabitInsightTimeframe.MONTH -> "${userStats.currentStreak}d Streak"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Jetpack Compose Canvas Chart
                when (selectedTimeframe) {
                    HabitInsightTimeframe.DAY -> {
                        DayHabitProgressList(habits = habits)
                    }
                    HabitInsightTimeframe.WEEK -> {
                        WeekCompletionBarChart(userStats = userStats)
                    }
                    HabitInsightTimeframe.MONTH -> {
                        MonthConsistencyBarChart(userStats = userStats)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Habit Performance List
        Text(
            text = "TAP HABIT FOR STREAK & MATRIX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryLight,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habits, key = { it.id }) { h ->
                Surface(
                    onClick = { onHabitClick(h) },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth().bounceClick()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = resolveHabitIcon(h.icon),
                                    contentDescription = null,
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = h.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${h.streakCount}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiProductivityInsightsCard(
    insight: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onOpenGrootAssistant: () -> Unit
) {
    val colors = FocusFlowTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GrootAvatar(
                        size = 32.dp,
                        containerColor = colors.primary.copy(alpha = 0.16f),
                        borderColor = colors.primary.copy(alpha = 0.35f),
                        tint = colors.primaryLight
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI PRODUCTIVITY COACH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Groot Habit Analysis",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .bounceClick(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Insight",
                            tint = colors.primaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
                val alphaAnim by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant.copy(alpha = alphaAnim))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.80f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant.copy(alpha = alphaAnim))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.60f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant.copy(alpha = alphaAnim))
                    )
                }
            } else {
                Text(
                    text = insight?.ifBlank { "You have active habits ready to compound. Complete your primary routine to build lasting streak momentum." }
                        ?: "Tap refresh to analyze your current habit streak and consistency with Groot AI.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onOpenGrootAssistant,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ask Groot Assistant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHabitProgressList(habits: List<HabitItem>) {
    val colors = FocusFlowTheme.colors

    if (habits.isEmpty()) {
        Text("No habits active today.", fontSize = 12.sp, color = colors.textMuted)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        habits.forEach { h ->
            val rate = if (h.isCompletedToday) 1.0f else 0.0f
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(h.title, fontSize = 12.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                    Text(
                        if (h.isCompletedToday) "Completed" else "Pending",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (h.isCompletedToday) Color(0xFF10B981) else colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { rate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (h.isCompletedToday) colors.primary else colors.surfaceVariant,
                    trackColor = colors.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeekCompletionBarChart(userStats: HabitUserStats) {
    val colors = FocusFlowTheme.colors
    val primaryColor = colors.primary
    val primaryLight = colors.primaryLight
    val surfaceVariant = colors.surfaceVariant

    val weeklyData = userStats.weeklyChartData

    if (weeklyData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No weekly habit history yet",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
        return
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 22.dp.toPx()
                val totalBars = weeklyData.size
                val spacing = (size.width - (barWidth * totalBars)) / (totalBars + 1)
                val chartHeight = size.height - 20.dp.toPx()

                weeklyData.forEachIndexed { index, item ->
                    val x = spacing + index * (barWidth + spacing)
                    val rate = item.completionRate.coerceIn(0f, 1f)
                    val barH = chartHeight * rate
                    val y = chartHeight - barH

                    // Background track
                    drawRoundRect(
                        color = surfaceVariant,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Active filled bar
                    if (rate > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryLight, primaryColor),
                                startY = y,
                                endY = chartHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, maxOf(barH, 4.dp.toPx())),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weeklyData.forEachIndexed { idx, item ->
                val isLast = idx == weeklyData.size - 1
                Text(
                    text = item.day,
                    fontSize = 10.sp,
                    color = if (isLast) colors.primaryLight else colors.textMuted,
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun MonthConsistencyBarChart(userStats: HabitUserStats) {
    val colors = FocusFlowTheme.colors
    val primaryColor = colors.primary
    val primaryLight = colors.primaryLight
    val surfaceVariant = colors.surfaceVariant

    val monthlyData = userStats.monthlyChartData

    if (monthlyData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No 4-week habit history yet",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
        return
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 42.dp.toPx()
                val totalBars = monthlyData.size
                val spacing = (size.width - (barWidth * totalBars)) / (totalBars + 1)
                val chartHeight = size.height - 20.dp.toPx()

                monthlyData.forEachIndexed { index, item ->
                    val x = spacing + index * (barWidth + spacing)
                    val rate = item.completionRate.coerceIn(0f, 1f)
                    val barH = chartHeight * rate
                    val y = chartHeight - barH

                    // Background track
                    drawRoundRect(
                        color = surfaceVariant,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )

                    // Active filled bar
                    if (rate > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryLight, primaryColor),
                                startY = y,
                                endY = chartHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, maxOf(barH, 4.dp.toPx())),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            monthlyData.forEach { item ->
                val pct = (item.completionRate * 100).toInt()
                Text(
                    text = "${item.weekLabel} ($pct%)",
                    fontSize = 10.sp,
                    color = colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StreakMetricRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String
) {
    val colors = FocusFlowTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun StreakDetailsSheetContent(
    habit: HabitItem?,
    userStats: HabitUserStats,
    onDismiss: () -> Unit,
    onDelete: (Int) -> Unit
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val currentStreak = if (habit != null) {
        if (habit.streakCount > 0) habit.streakCount else userStats.currentStreak
    } else {
        userStats.currentStreak
    }

    val longestStreak = if (habit != null) {
        maxOf(habit.longestStreak, habit.streakCount, userStats.longestStreak)
    } else {
        maxOf(userStats.longestStreak, userStats.currentStreak)
    }

    val habitsCompleted = if (habit != null && habit.totalCompletions > 0) {
        habit.totalCompletions
    } else {
        userStats.totalHabitsCompleted
    }

    val totalMinutes = userStats.totalFocusMinutes
    val focusHours = totalMinutes / 60
    val focusMins = totalMinutes % 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (habit != null) resolveHabitIcon(habit.icon) else Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (habit != null) colors.primaryLight else Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = habit?.title ?: "Focus & Streak Metrics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (habit != null) "${habit.frequency.replaceFirstChar { it.uppercase() }} Habit" else "Your Overall Consistency",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Elevated Metric Rows Matching Reference Card Design
        // Row 1: Vector flame icon -> "[X] days" (Bold Title) | "Current streak" (Subtext)
        StreakMetricRow(
            icon = Icons.Rounded.LocalFireDepartment,
            iconTint = Color(0xFFF59E0B),
            iconBg = Color(0xFFF59E0B).copy(alpha = 0.14f),
            title = "$currentStreak days",
            subtitle = "Current streak"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Vector calendar icon -> "[X] days" (Bold Title) | "Longest streak" (Subtext)
        StreakMetricRow(
            icon = Icons.Rounded.CalendarToday,
            iconTint = Color(0xFF3B82F6),
            iconBg = Color(0xFF3B82F6).copy(alpha = 0.14f),
            title = "$longestStreak days",
            subtitle = "Longest streak"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: Vector check/task icon -> "[X]" (Bold Title) | "Habits completed" (Subtext)
        StreakMetricRow(
            icon = Icons.Rounded.CheckCircle,
            iconTint = Color(0xFF10B981),
            iconBg = Color(0xFF10B981).copy(alpha = 0.14f),
            title = "$habitsCompleted",
            subtitle = "Habits completed"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row 4: Vector hourglass/timer icon -> "[X] hrs [Y] min" (Bold Title) | "Total focus time" (Subtext)
        StreakMetricRow(
            icon = Icons.Rounded.HourglassBottom,
            iconTint = colors.primaryLight,
            iconBg = colors.primary.copy(alpha = 0.14f),
            title = "$focusHours hrs $focusMins min",
            subtitle = "Total focus time"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Action: A clean "Share" button using Android's native share intent (ACTION_SEND)
        Button(
            onClick = {
                val shareText = "I'm on a $currentStreak-day focus streak on FOCUSFLOW! Longest streak: $longestStreak days, $habitsCompleted habits completed, and $focusHours hrs $focusMins min of deep focus logged."
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val chooser = Intent.createChooser(sendIntent, "Share your streak")
                context.startActivity(chooser)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bounceClick(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (habit != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Habit", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }

    if (showDeleteConfirm && habit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Delete Habit?", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text("Are you sure you want to remove '${habit.title}' and its logged streaks?", color = colors.textMuted, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(habit.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, icon: String, frequency: String) -> Unit
) {
    val colors = FocusFlowTheme.colors
    var title by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("bolt") }
    var selectedFrequency by remember { mutableStateOf("daily") }

    val iconOptions = listOf("bolt", "book", "code", "water", "fitness", "star", "edit", "target", "night", "repeat")
    val frequencyOptions = listOf("daily", "weekdays", "weekly")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "New Habit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Habit Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title *", color = colors.textMuted) },
                    placeholder = { Text("e.g. Read 20 pages", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Picker
                Text(
                    text = "CHOOSE ICON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconOptions, key = { it }) { icon ->
                        val isSelected = selectedIcon == icon
                        Surface(
                            onClick = { selectedIcon = icon },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primary else colors.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = resolveHabitIcon(icon),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else colors.primaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Selector
                Text(
                    text = "FREQUENCY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequencyOptions.forEach { freq ->
                        val isSelected = selectedFrequency == freq
                        Surface(
                            onClick = { selectedFrequency = freq },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.primary else colors.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = freq.replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedIcon, selectedFrequency)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Habit", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HabitsScreenPreview() {
    HabitsScreen()
}
