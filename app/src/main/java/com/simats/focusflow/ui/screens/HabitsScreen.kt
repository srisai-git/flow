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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.HabitMonthlyChartItem
import com.simats.focusflow.data.models.HabitUserStats
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.FlowLoadingIndicator
import com.simats.focusflow.ui.components.GrootAvatar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.HabitInsightTimeframe
import com.simats.focusflow.ui.viewmodel.HabitReportData
import com.simats.focusflow.ui.viewmodel.HabitTab
import com.simats.focusflow.ui.viewmodel.HabitUiState
import com.simats.focusflow.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

fun resolveHabitIcon(iconKey: String): ImageVector = when (iconKey.lowercase()) {
    "star", "default", "general" -> Icons.Default.Star
    "check", "checkcircle" -> Icons.Rounded.CheckCircle
    "bolt", "flash" -> Icons.Default.FlashOn
    "book", "read" -> Icons.AutoMirrored.Filled.MenuBook
    "code", "laptop" -> Icons.Default.Code
    "water" -> Icons.Default.WaterDrop
    "fitness", "run" -> Icons.Default.FitnessCenter
    "edit", "write" -> Icons.Default.Edit
    "target", "focus" -> Icons.Default.TrackChanges
    "night", "sleep" -> Icons.Default.Bedtime
    else -> Icons.Default.Star
}

@Composable
fun EmptyHabitsCard(
    modifier: Modifier = Modifier,
    onCreateHabit: () -> Unit = {}
) {
    val colors = FocusFlowTheme.colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.cardBorderBrush),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No habits created yet.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap '+' to create your first habit.",
                fontSize = 13.sp,
                color = colors.textMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateHabit,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.bounceClick()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Create Habit",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGrootAi: (String?) -> Unit = {},
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (state.selectedTab == HabitTab.ROUTINE) {
                FloatingActionButton(
                    onClick = { viewModel.setAddHabitDialogOpen(true) },
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 12.dp, end = 8.dp)
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
            Spacer(modifier = Modifier.height(10.dp))

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
                if (state.habits.isNotEmpty()) {
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

                        val dynamicInsight = remember(state.habits, state.userStats) {
                            viewModel.computeDynamicHabitInsight(state.habits, state.userStats)
                        }

                        Text(
                            text = dynamicInsight,
                            fontSize = 12.sp,
                            color = if (state.totalCount > 0 && state.completedCount == state.totalCount) colors.primaryLight else colors.textMuted
                        )

                        val topStreakHabit = remember(state.habits) { state.habits.maxByOrNull { it.streakCount } }
                        if (topStreakHabit != null && topStreakHabit.streakCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.primary.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Top Habit: ${topStreakHabit.title} with ${topStreakHabit.streakCount} consecutive days",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primaryLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ACTIVE HABITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FlowLoadingIndicator(size = 36.dp)
                    }
                } else if (state.habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyHabitsCard(onCreateHabit = { viewModel.setAddHabitDialogOpen(true) })
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
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
                    },
                    habitReport = state.habitReport,
                    isGeneratingReport = state.isGeneratingReport,
                    onGenerateReport = { viewModel.generateHabitReport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        if (state.isAddHabitDialogOpen) {
            AddHabitDialog(
                onDismiss = { viewModel.setAddHabitDialogOpen(false) },
                onConfirm = { title, icon, frequency ->
                    viewModel.createHabit(title, icon, frequency)
                }
            )
        }

        if (isStreakModalOpen || state.selectedHabitForStreak != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    isStreakModalOpen = false
                    viewModel.selectHabitForStreak(null)
                },
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
    val isDue = HabitUiState.isHabitDueToday(habit.frequency)
    val freqDisplay = if (habit.frequency.equals("daily", ignoreCase = true)) {
        "Daily"
    } else if (habit.frequency.equals("weekdays", ignoreCase = true)) {
        "Weekdays"
    } else {
        habit.frequency.split(",").joinToString(", ") { it.trim().take(3).replaceFirstChar { c -> c.uppercase() } }
    }
    val frequencyLabel = freqDisplay
    val cardBg by animateColorAsState(
        targetValue = if (isDone) colors.primary.copy(alpha = 0.16f) else colors.surface,
        label = "habitBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDone) colors.primary.copy(alpha = 0.45f) else colors.border,
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.14f))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
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
                        text = "•  $frequencyLabel",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

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
    selectedTimeframe: HabitInsightTimeframe = HabitInsightTimeframe.WEEK,
    onSelectTimeframe: (HabitInsightTimeframe) -> Unit = {},
    onRefreshAi: () -> Unit,
    onNavigateToGrootAi: (String?) -> Unit,
    onHabitClick: (HabitItem) -> Unit,
    habitReport: HabitReportData? = null,
    isGeneratingReport: Boolean = false,
    onGenerateReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FocusFlowTheme.colors

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        GrootAiHabitInsightsCard(
            insight = aiInsight,
            isLoading = isAiLoading,
            onRefresh = onRefreshAi,
            habitReport = habitReport,
            isGeneratingReport = isGeneratingReport,
            onGenerateReport = onGenerateReport,
            userStats = userStats,
            habits = habits
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                            text = "Habit Momentum Trend",
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
                            text = "${habits.count { it.isCompletedToday }}/${habits.size} Done Today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                HabitMomentumDottedGraph(userStats = userStats)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            habits.forEach { h ->
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
private fun GrootAiHabitInsightsCard(
    insight: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    habitReport: HabitReportData?,
    isGeneratingReport: Boolean,
    onGenerateReport: () -> Unit,
    userStats: HabitUserStats,
    habits: List<HabitItem> = emptyList()
) {
    val colors = FocusFlowTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(colors.primary.copy(alpha = 0.5f), colors.border)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GrootAvatar(
                        size = 32.dp,
                        containerColor = colors.primary.copy(alpha = 0.16f),
                        borderColor = colors.primary.copy(alpha = 0.35f),
                        tint = colors.primaryLight
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "GROOT AI HABIT INSIGHTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Consistency & Performance Audit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val score = habitReport?.consistencyScore ?: userStats.avgConsistencyPercent.toInt()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Score $score%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                            .bounceClick(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            FlowLoadingIndicator(size = 16.dp, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Insight",
                                tint = colors.primaryLight,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
                val alphaAnimState = infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Column(
                    modifier = Modifier.graphicsLayer { alpha = alphaAnimState.value },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.80f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.60f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant)
                    )
                }
            } else {
                val isChatFallback = !insight.isNullOrBlank() && (
                    insight.contains("lock in—want to", ignoreCase = true) ||
                    insight.contains("start a focus timer", ignoreCase = true) ||
                    insight.contains("unable to connect", ignoreCase = true) ||
                    insight.contains("offline", ignoreCase = true) ||
                    insight.contains("I am Groot", ignoreCase = true)
                )

                val defaultInsight = run {
                    val comp = habits.count { it.isCompletedToday }
                    val consistency = if (userStats.avgConsistencyPercent > 0) userStats.avgConsistencyPercent.toInt() else if (habits.isNotEmpty()) ((comp * 100) / habits.size) else 0
                    if (habits.isNotEmpty() && habits.all { it.isCompletedToday }) {
                        "Flawless $consistency% consistency! All ${habits.size} habits completed today with an active ${maxOf(1, userStats.currentStreak)}-day streak."
                    } else if (comp > 0) {
                        "Maintaining $consistency% consistency with $comp of ${habits.size} habits completed today. Strong momentum across daily routines."
                    } else {
                        "Consistency currently at $consistency%. Complete your scheduled habits today to sustain your ${maxOf(1, userStats.currentStreak)}-day streak."
                    }
                }

                val displayText = if (!insight.isNullOrBlank() && !isChatFallback) {
                    insight
                } else {
                    habitReport?.recommendation ?: defaultInsight
                }
                Text(
                    text = displayText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Normal
                )

                val strongestTitle = habitReport?.strongestHabit?.ifBlank { null }
                    ?: habits.maxByOrNull { it.streakCount }?.title ?: "Daily Habit"
                val strongestStreak = habitReport?.strongestHabitStreak
                    ?: maxOf(habits.maxByOrNull { it.streakCount }?.streakCount ?: 0, userStats.currentStreak)

                val allHabitsCompleted = habits.isNotEmpty() && habits.all { it.isCompletedToday }
                val topHabit = habits.maxByOrNull { it.streakCount }
                val attentionTitle = when {
                    habits.isEmpty() || allHabitsCompleted -> "All habits on track!"
                    !habitReport?.needsAttentionHabit.isNullOrBlank() -> habitReport!!.needsAttentionHabit
                    habits.size > 1 -> {
                        val otherHabits = habits.filter { it.id != topHabit?.id }
                        val uncompletedOther = otherHabits.filter { !it.isCompletedToday }
                        if (uncompletedOther.isNotEmpty()) {
                            val target = uncompletedOther.minByOrNull { it.streakCount }!!
                            "${target.title} (${target.streakCount}d streak)"
                        } else {
                            "All habits on track!"
                        }
                    }
                    else -> {
                        val single = habits.first()
                        if (single.isCompletedToday) "All habits on track!" else "${single.title} (${single.streakCount}d streak)"
                    }
                }

                val defaultNextAction = run {
                    val uncompleted = habits.count { !it.isCompletedToday }
                    val nextStreak = userStats.currentStreak + 1
                    when {
                        habits.isEmpty() -> "Add your first habit to start tracking your daily consistency."
                        uncompleted == 0 -> "All ${habits.size} habits completed today! Keep this momentum tomorrow to push your streak to $nextStreak days."
                        uncompleted == 1 -> "Complete your remaining 1 habit today to push your streak to $nextStreak days."
                        else -> "Complete all $uncompleted habits today to push your streak to $nextStreak days."
                    }
                }

                val nextActionText = habitReport?.nextAction?.ifBlank { null }
                    ?: defaultNextAction

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STRONGEST STREAK",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strongestTitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${strongestStreak}d Streak",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NEEDS ATTENTION",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = attentionTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Focus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "NEXT ACTION",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextActionText,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = colors.textPrimary
                    )
                }

                if (habitReport == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onGenerateReport,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            enabled = !isGeneratingReport
                        ) {
                            if (isGeneratingReport) {
                                FlowLoadingIndicator(size = 14.dp, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Analyzing Habits...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Generate Habit Report",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitMomentumDottedGraph(
    userStats: HabitUserStats,
    modifier: Modifier = Modifier
) {
    val colors = FocusFlowTheme.colors
    val weeklyData = userStats.weeklyChartData
    if (weeklyData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No weekly habit history yet", fontSize = 12.sp, color = colors.textMuted)
        }
        return
    }

    val primaryColor = colors.primary
    val primaryLight = colors.primaryLight
    val textMuted = colors.textMuted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val count = weeklyData.size
            if (count < 2) return@Canvas

            val bottomLabelHeight = 22.dp.toPx()
            val graphHeight = size.height - bottomLabelHeight
            val spacing = size.width / (count - 1).coerceAtLeast(1)

            val points = weeklyData.mapIndexed { index, item ->
                val x = index * spacing
                val rate = item.completionRate.coerceIn(0f, 1f)
                val y = graphHeight - (rate * (graphHeight - 16.dp.toPx())) - 8.dp.toPx()
                Offset(x, y)
            }

            val path = Path()
            val fillPath = Path()

            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, graphHeight)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, graphHeight)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = graphHeight
                )
            )

            drawPath(
                path = path,
                color = primaryLight,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )

            points.forEach { pt ->
                drawCircle(
                    color = primaryLight,
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = pt
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weeklyData.forEach { item ->
                Text(
                    text = item.day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted,
                    textAlign = TextAlign.Center
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
                    color = colors.textPrimary
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

        StreakMetricRow(
            icon = Icons.Rounded.LocalFireDepartment,
            iconTint = Color(0xFFF59E0B),
            iconBg = Color(0xFFF59E0B).copy(alpha = 0.14f),
            title = "$currentStreak days",
            subtitle = "Current streak"
        )

        Spacer(modifier = Modifier.height(10.dp))

        StreakMetricRow(
            icon = Icons.Rounded.CalendarToday,
            iconTint = Color(0xFF3B82F6),
            iconBg = Color(0xFF3B82F6).copy(alpha = 0.14f),
            title = "$longestStreak days",
            subtitle = "Longest streak"
        )

        Spacer(modifier = Modifier.height(10.dp))

        StreakMetricRow(
            icon = Icons.Rounded.CheckCircle,
            iconTint = Color(0xFF10B981),
            iconBg = Color(0xFF10B981).copy(alpha = 0.14f),
            title = "$habitsCompleted",
            subtitle = "Habits completed"
        )

        Spacer(modifier = Modifier.height(10.dp))

        StreakMetricRow(
            icon = Icons.Rounded.HourglassBottom,
            iconTint = colors.primaryLight,
            iconBg = colors.primary.copy(alpha = 0.14f),
            title = "$focusHours hrs $focusMins min",
            subtitle = "Total focus time"
        )

        Spacer(modifier = Modifier.height(24.dp))

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
    var selectedIcon by remember { mutableStateOf("star") }

    val iconOptions = listOf("star", "check", "bolt", "book", "code", "water", "fitness", "edit", "target", "night", "repeat")

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

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedIcon, "daily")
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6), contentColor = Color.White),
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
