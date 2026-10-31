package com.simats.focusflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.GrootAvatar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.ChatMessage
import com.simats.focusflow.ui.viewmodel.GrootAiViewModel

private val AccentGreen = Color(0xFF10B981)

data class QuickPrompt(val label: String, val category: String)

@Composable
fun GrootAiScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: GrootAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    LaunchedEffect(Unit) {
        viewModel.initContext(context, SessionManager.getInstance(context))
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            onNavigate(route)
        }
    }

    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(state.messages.size, state.isAiThinking) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val quickActionPrompts = remember {
        listOf(
            QuickPrompt("Start a 25 min timer", "Action"),
            QuickPrompt("Add task Finish ML Report with high priority", "Action"),
            QuickPrompt("Create habit Read 20 pages daily", "Action"),
            QuickPrompt("My stats today", "Insights"),
            QuickPrompt("Explain quicksort vs mergesort", "Coding"),
            QuickPrompt("How do streaks work in FocusFlow?", "App Guide"),
            QuickPrompt("Export my productivity data", "Action")
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(contentAlignment = Alignment.Center) {
                        // Ambient subtle glow ring around Groot Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f))
                        )
                        GrootAvatar(size = 38.dp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Groot AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.primary.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "AGENT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryLight,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Autonomous Actions & General Q&A",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Quick Action Starter Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickActionPrompts, key = { it.label }) { item ->
                            Surface(
                                onClick = { viewModel.sendMessage(item.label) },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceVariant,
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier.bounceClick(),
                                enabled = !state.isAiThinking
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (item.category) {
                                            "Action" -> Icons.Outlined.Timer
                                            "Insights" -> Icons.Outlined.TrackChanges
                                            "Coding" -> Icons.Outlined.AutoAwesome
                                            else -> Icons.Outlined.Star
                                        },
                                        contentDescription = null,
                                        tint = colors.primaryLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Multiline Input Field & Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = state.inputText,
                            onValueChange = { viewModel.onInputTextChanged(it) },
                            placeholder = {
                                Text(
                                    text = "Ask anything or command actions (start timer, add task)...",
                                    fontSize = 13.sp,
                                    color = colors.textMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp, max = 120.dp),
                            shape = RoundedCornerShape(16.dp),
                            maxLines = 4,
                            minLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedContainerColor = colors.surfaceVariant,
                                unfocusedContainerColor = colors.surfaceVariant,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() })
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = { viewModel.sendMessage() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (state.inputText.isNotBlank() && !state.isAiThinking)
                                        Brush.horizontalGradient(listOf(colors.primary, colors.primaryLight))
                                    else
                                        Brush.horizontalGradient(listOf(colors.border, colors.border))
                                )
                                .bounceClick(),
                            enabled = state.inputText.isNotBlank() && !state.isAiThinking
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (state.inputText.isNotBlank() && !state.isAiThinking) Color.White else colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            // Ambient Atmospheric Gradients (adds modern depth)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(colors.primary.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(700f, -50f),
                            radius = 600f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .height(220.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(colors.primary.copy(alpha = 0.04f), Color.Transparent),
                            center = Offset(100f, 300f),
                            radius = 500f
                        )
                    )
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onActionClick = { action ->
                            viewModel.handleActionClick(action, onNavigate)
                        }
                    )
                }

                if (state.isAiThinking) {
                    item(key = "thinking_indicator") {
                        AiThinkingBubble()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onActionClick: (AiAction) -> Unit
) {
    val colors = FocusFlowTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            GrootAvatar(size = 30.dp)
            Spacer(modifier = Modifier.width(10.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromUser) 18.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 18.dp
            ),
            color = if (message.isFromUser) colors.primary else colors.surface,
            border = BorderStroke(
                1.dp,
                if (message.isFromUser) colors.primary else colors.border
            ),
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!message.isFromUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Groot",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = if (message.isFromUser) Color.White else colors.textPrimary,
                    fontWeight = FontWeight.Normal
                )

                // Render Action Confirmation Card if Groot performed an in-app action!
                if (message.action != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ActionCard(action = message.action, onClick = { onActionClick(message.action) })
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = if (message.isFromUser) Color.White.copy(alpha = 0.7f) else colors.textMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    action: AiAction,
    onClick: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    val actionType = action.type.uppercase()
    val isTimer = actionType == "START_TIMER"
    val isTask = actionType == "CREATE_TASK"
    val isHabit = actionType == "CREATE_HABIT"
    val isNav = actionType == "NAVIGATE"
    val isGoal = actionType == "SET_GOAL"

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isTimer -> Icons.Outlined.Timer
                            isTask -> Icons.Outlined.TaskAlt
                            isHabit -> Icons.Outlined.CheckCircle
                            isGoal -> Icons.Outlined.TrackChanges
                            else -> Icons.Outlined.NearMe
                        },
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = when {
                            isTimer -> "Focus Timer Active"
                            isTask -> "Task Scheduled"
                            isHabit -> "Daily Habit Created"
                            isGoal -> "Focus Target Saved"
                            else -> "In-App Shortcut"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = when {
                            isTimer -> "${action.duration} Minutes • Running"
                            isTask -> "${action.title} (${action.priority.uppercase()})"
                            isHabit -> "${action.title} • Daily Streak"
                            isGoal -> "${action.dailyMinutes / 60.0} Hours / Day"
                            else -> action.screenName.ifBlank { action.route.uppercase() }
                        },
                        fontSize = 11.sp,
                        color = colors.textMuted,
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.primary,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isTimer -> "Open Timer"
                            isTask -> "View Tasks"
                            isHabit -> "View Habits"
                            else -> "Open"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiThinkingBubble() {
    val colors = FocusFlowTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GrootAvatar(size = 30.dp)

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, if (colors.isDark) colors.primary.copy(alpha = 0.45f) else colors.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = alphaAnim))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = (alphaAnim * 0.8f).coerceIn(0.2f, 1f)))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = (alphaAnim * 0.6f).coerceIn(0.2f, 1f)))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Groot is thinking & preparing actions...",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}
