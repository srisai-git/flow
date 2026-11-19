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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.AmbientMeshGlowHeader
import com.simats.focusflow.ui.components.GrootAvatar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.ChatMessage
import com.simats.focusflow.ui.viewmodel.GrootAiViewModel

private val AccentGreen = Color(0xFF10B981)

data class QuickPrompt(val label: String, val category: String)

@OptIn(ExperimentalLayoutApi::class)
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
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(state.messages.size, state.isAiThinking) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(isImeVisible) {
        if (isImeVisible && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val quickActionPrompts = remember {
        listOf(
            QuickPrompt("Start a 25 min timer", "Action"),
            QuickPrompt("Add task Finish ML Report with high priority", "Action"),
            QuickPrompt("Create habit Read 20 pages daily", "Action"),
            QuickPrompt("My stats today", "Insights"),
            QuickPrompt("How to use the Pomodoro technique?", "Productivity"),
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
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .size(38.dp)
                            .bounceClick()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(contentAlignment = Alignment.Center) {
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

                    Surface(
                        onClick = { viewModel.clearChat() },
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .size(38.dp)
                            .bounceClick()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    AnimatedVisibility(visible = !isImeVisible) {
                        Column {
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
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                                tint = if (colors.isDark) Color(0xFFC4B5FD) else colors.primary,
                                                modifier = Modifier.size(12.dp)
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.inputText,
                            onValueChange = { viewModel.onInputTextChanged(it) },
                            placeholder = {
                                Text(
                                    text = "Ask Groot AI...",
                                    fontSize = 13.5.sp,
                                    color = colors.textMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp, max = 110.dp),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            minLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = colors.border,
                                focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.7f),
                                unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = Color(0xFF8B5CF6)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (state.inputText.isNotBlank() && !state.isAiThinking) {
                                        viewModel.sendMessage()
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val sendEnabled = state.inputText.isNotBlank() && !state.isAiThinking
                        IconButton(
                            onClick = {
                                if (sendEnabled) {
                                    viewModel.sendMessage()
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (sendEnabled)
                                        Color(0xFF8B5CF6)
                                    else
                                        colors.surfaceVariant
                                )
                                .bounceClick(),
                            enabled = sendEnabled
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (sendEnabled) Color.White else colors.textMuted,
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
                .consumeWindowInsets(paddingValues)
                .background(colors.background)
        ) {
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
                    .consumeWindowInsets(paddingValues)
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
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

                SelectionContainer {
                    MarkdownChatMessage(
                        text = message.text,
                        isFromUser = message.isFromUser,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (message.action != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ActionCard(action = message.action, isExecuted = message.actionExecuted, onClick = { onActionClick(message.action) })
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (!message.isFromUser) Arrangement.SpaceBetween else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!message.isFromUser) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp).bounceClick()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = colors.textMuted.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = message.timestamp,
                        fontSize = 10.sp,
                        color = if (message.isFromUser) Color.White.copy(alpha = 0.7f) else colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    action: AiAction,
    isExecuted: Boolean = false,
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
                            isTimer -> "${action.duration} min session active"
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (isExecuted) {
                    Box(
                        modifier = Modifier
                            .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Done",
                                tint = AccentGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Done",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary
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

@Composable
fun MarkdownChatMessage(
    text: String,
    isFromUser: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = FocusFlowTheme.colors
    val textColor = if (isFromUser) Color.White else colors.textPrimary

    val parts = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier) {
        parts.forEach { part ->
            when (part) {
                is MarkdownBlock.Code -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (colors.isDark) Color(0xFF0D0B18) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (part.language.isNotBlank()) {
                                Text(
                                    text = part.language.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryLight,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = part.code,
                                fontSize = 12.5.sp,
                                color = if (colors.isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                is MarkdownBlock.Paragraph -> {
                    val annotated = remember(part.content, textColor) {
                        formatInlineMarkdown(part.content, textColor, colors.primaryLight)
                    }
                    Text(
                        text = annotated,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = textColor,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val content: String) : MarkdownBlock()
    data class Code(val language: String, val code: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\n?([\\s\\S]*?)```")
    var lastIndex = 0

    codeBlockRegex.findAll(text).forEach { match ->
        val range = match.range
        if (range.first > lastIndex) {
            val textBefore = text.substring(lastIndex, range.first).trim()
            if (textBefore.isNotEmpty()) {
                blocks.add(MarkdownBlock.Paragraph(textBefore))
            }
        }
        val lang = match.groupValues[1].trim()
        val code = match.groupValues[2].trimEnd()
        blocks.add(MarkdownBlock.Code(lang, code))
        lastIndex = range.last + 1
    }

    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(remaining))
        }
    }

    if (blocks.isEmpty() && text.isNotBlank()) {
        blocks.add(MarkdownBlock.Paragraph(text))
    }

    return blocks
}

fun formatInlineMarkdown(text: String, defaultColor: Color, accentColor: Color): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            var currentLine = line
            if (currentLine.startsWith("* ") || currentLine.startsWith("- ")) {
                currentLine = "• " + currentLine.substring(2)
            }

            var i = 0
            while (i < currentLine.length) {
                if (currentLine.startsWith("**", i)) {
                    val endBold = currentLine.indexOf("**", i + 2)
                    if (endBold != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(currentLine.substring(i + 2, endBold))
                        }
                        i = endBold + 2
                        continue
                    }
                }
                if (currentLine[i] == '`') {
                    val endCode = currentLine.indexOf('`', i + 1)
                    if (endCode != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = accentColor
                            )
                        ) {
                            append(currentLine.substring(i + 1, endCode))
                        }
                        i = endCode + 1
                        continue
                    }
                }
                append(currentLine[i])
                i++
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}

