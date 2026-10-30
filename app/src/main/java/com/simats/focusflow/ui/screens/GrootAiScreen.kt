package com.simats.focusflow.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.GrootAvatar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.ChatMessage
import com.simats.focusflow.ui.viewmodel.GrootAiViewModel

private val CanvasDark = Color(0xFF0F0F14)
private val SurfaceDark = Color(0xFF181722)
private val BorderDark = Color(0xFF262433)
private val UserBubbleBg = Color(0xFF262433)
private val GrootBubbleBg = Color(0xFF181722)
private val VioletPrimary = Color(0xFF8B5CF6)
private val VioletBorder = Color(0xFF8B5CF6).copy(alpha = 0.45f)
private val VioletLight = Color(0xFFC4B5FD)
private val TextWhite = Color(0xFFF1F1F5)
private val TextMuted = Color(0xFF9CA3AF)

@Composable
fun GrootAiScreen(
    onBack: () -> Unit = {},
    viewModel: GrootAiViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
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
            "Analyze my productivity today",
            "Help me debug a Kotlin coroutine",
            "Draft a focus routine for exam prep",
            "Explain quicksort vs mergesort"
        )
    }

    Scaffold(
        containerColor = CanvasDark,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceDark,
                border = BorderStroke(1.dp, BorderDark)
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
                            .background(BorderDark)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    GrootAvatar(size = 38.dp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Groot AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Online / Ready",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BorderDark)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceDark,
                border = BorderStroke(1.dp, BorderDark)
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
                        items(quickActionPrompts, key = { it }) { promptText ->
                            Surface(
                                onClick = { viewModel.sendMessage(promptText) },
                                shape = RoundedCornerShape(12.dp),
                                color = BorderDark,
                                border = BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.bounceClick(),
                                enabled = !state.isAiThinking
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = VioletLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = promptText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextWhite
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
                                    text = "Ask Groot anything (coding, concepts, focus)...",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp, max = 120.dp),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 4,
                            minLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderDark,
                                focusedContainerColor = CanvasDark,
                                unfocusedContainerColor = CanvasDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() })
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = { viewModel.sendMessage() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (state.inputText.isNotBlank() && !state.isAiThinking) VioletPrimary else BorderDark)
                                .bounceClick(),
                            enabled = state.inputText.isNotBlank() && !state.isAiThinking
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (state.inputText.isNotBlank() && !state.isAiThinking) Color.White else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CanvasDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(state.messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (state.isAiThinking) {
                item(key = "thinking_indicator") {
                    AiThinkingBubble()
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            GrootAvatar(size = 28.dp)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            color = if (message.isFromUser) UserBubbleBg else GrootBubbleBg,
            border = BorderStroke(
                1.dp,
                if (message.isFromUser) BorderDark else VioletBorder
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!message.isFromUser) {
                    Text(
                        text = "Groot",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun AiThinkingBubble() {
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
        GrootAvatar(size = 28.dp)

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            color = GrootBubbleBg,
            border = BorderStroke(1.dp, VioletBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VioletPrimary.copy(alpha = alphaAnim))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VioletPrimary.copy(alpha = (alphaAnim * 0.8f).coerceIn(0.2f, 1f)))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VioletPrimary.copy(alpha = (alphaAnim * 0.6f).coerceIn(0.2f, 1f)))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Groot is thinking...",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}
