package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.data.TokenSyncHelper
import com.simats.focusflow.service.NotificationHelper
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    var notificationsEnabled by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("09:00 AM") }
    var showTimeDropdown by remember { mutableStateOf(false) }
    var deadlineReminder by remember { mutableStateOf(true) }
    var breakReminder by remember { mutableStateOf(true) }
    var aiReportReminder by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val timeOptions = listOf("07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "06:00 PM", "08:00 PM")

    // Automatically synchronize the active FCM token when navigating to notifications
    LaunchedEffect(Unit) {
        TokenSyncHelper.syncForCurrentUser(context)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Sleek Header with Circular Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Notifications & Reminders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Mindful focus alerts and scheduled nudges",
                        fontSize = 12.5.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. MASTER TOGGLE CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Master toggle for focus reminders and push alerts",
                            fontSize = 12.5.sp,
                            color = colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.textMuted,
                            uncheckedTrackColor = colors.surfaceVariant,
                            uncheckedBorderColor = colors.border
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DAILY FOCUS REMINDER CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Focus Reminder",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "A prompt to begin your first focus interval",
                                fontSize = 12.5.sp,
                                color = colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = dailyReminder && notificationsEnabled,
                            enabled = notificationsEnabled,
                            onCheckedChange = { dailyReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant,
                                uncheckedBorderColor = colors.border
                            )
                        )
                    }

                    if (dailyReminder && notificationsEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = colors.border, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Reminder Time",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Styled Dropdown / Picker
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceVariant,
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimeDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reminderTime,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Change",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryLight
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showTimeDropdown,
                                onDismissRequest = { showTimeDropdown = false },
                                modifier = Modifier
                                    .background(colors.surface)
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            ) {
                                timeOptions.forEach { time ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = time,
                                                fontSize = 13.5.sp,
                                                color = if (reminderTime == time) colors.primaryLight else colors.textPrimary,
                                                fontWeight = if (reminderTime == time) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            reminderTime = time
                                            showTimeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick selection pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("08:00 AM", "09:00 AM", "10:00 AM", "08:00 PM").forEach { quickTime ->
                                val isSelected = reminderTime == quickTime
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) colors.primary.copy(alpha = 0.16f) else colors.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) colors.primary else colors.border
                                    ),
                                    modifier = Modifier
                                        .bounceClick()
                                        .clickable { reminderTime = quickTime }
                                ) {
                                    Text(
                                        text = quickTime,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) colors.primaryLight else colors.textMuted,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TARGETED ALERTS SECTION
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Deadline Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Deadline Reminders",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "30 minutes before a scheduled task is due",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = deadlineReminder && notificationsEnabled,
                            enabled = notificationsEnabled,
                            onCheckedChange = { deadlineReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant,
                                uncheckedBorderColor = colors.border
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Break Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Breather Reminders",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gentle alert when a timer interval finishes",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = breakReminder && notificationsEnabled,
                            enabled = notificationsEnabled,
                            onCheckedChange = { breakReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant,
                                uncheckedBorderColor = colors.border
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekly Groot AI Report
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weekly Groot AI Insights",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sunday productivity summary and suggestions",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = aiReportReminder && notificationsEnabled,
                            enabled = notificationsEnabled,
                            onCheckedChange = { aiReportReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant,
                                uncheckedBorderColor = colors.border
                            )
                        )
                    }
                }
            }

            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "NOTIFICATION PREVIEW",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Elegant Notification Card Preview
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alert",
                                        tint = colors.primaryLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FocusFlow",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = if (dailyReminder) reminderTime else "Off",
                                fontSize = 11.5.sp,
                                color = colors.primaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Time for your deep focus interval",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Your intention is set. Tap here to begin flow.",
                            fontSize = 12.5.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Send Test Notification Button
                Surface(
                    onClick = {
                        NotificationHelper.sendTestNotification(context, "timer")
                        scope.launch {
                            TokenSyncHelper.syncForCurrentUser(context)
                            snackbarHostState.showSnackbar("Test notification sent & FCM token synced.")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colors.primaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Send Test Notification",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Preferences Button
            Button(
                onClick = {
                    scope.launch {
                        TokenSyncHelper.syncForCurrentUser(context)
                        snackbarHostState.showSnackbar(
                            if (notificationsEnabled) "Preferences saved. Reminder set for $reminderTime"
                            else "Notifications disabled."
                        )
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Preferences",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}
