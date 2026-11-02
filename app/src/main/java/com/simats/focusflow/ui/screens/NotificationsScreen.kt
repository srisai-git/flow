package com.simats.focusflow.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.TokenSyncHelper
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.SavePreferencesRequest
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.NotificationHelper
import com.simats.focusflow.service.ReminderScheduler
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("09:00 AM") }
    var deadlineReminder by remember { mutableStateOf(true) }
    var breakReminder by remember { mutableStateOf(true) }
    var aiReportReminder by remember { mutableStateOf(false) }

    val presetTimes = listOf("07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "06:00 PM", "08:00 PM")

    // Helper to launch Android's native custom time picker dialog
    val openTimePicker = {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val parsedDate = sdf.parse(reminderTime)
            if (parsedDate != null) calendar.time = parsedDate
        } catch (_: Exception) {}

        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val isPm = hourOfDay >= 12
                val formattedHour = when {
                    hourOfDay == 0 -> 12
                    hourOfDay > 12 -> hourOfDay - 12
                    else -> hourOfDay
                }
                val amPm = if (isPm) "PM" else "AM"
                val customFormatted = String.format(Locale.US, "%02d:%02d %s", formattedHour, minute, amPm)
                reminderTime = customFormatted

                // Schedule alarm immediately at the selected custom time
                ReminderScheduler.scheduleDailyReminder(
                    context,
                    customFormatted,
                    notificationsEnabled && dailyReminder
                )

                // Persist custom time immediately to session and backend
                scope.launch(Dispatchers.IO) {
                    val sessionManager = SessionManager.getInstance(context)
                    sessionManager.saveNotificationPreferences(
                        notificationsEnabled = notificationsEnabled,
                        dailyReminder = dailyReminder,
                        reminderTime = customFormatted,
                        deadlineReminder = deadlineReminder,
                        breakReminder = breakReminder,
                        aiReportReminder = aiReportReminder
                    )
                    val uid = sessionManager.userSessionFlow.first().userId ?: 1
                    try {
                        RetrofitClient.authApiService.savePreferences(
                            SavePreferencesRequest(
                                userId = uid,
                                primaryGoal = "Study",
                                preferredFocusDuration = 45,
                                dailyFocusGoalMinutes = 210,
                                remindersEnabled = if (notificationsEnabled && dailyReminder) 1 else 0,
                                reminderTime = customFormatted
                            )
                        )
                    } catch (_: Exception) {}
                }
                scope.launch {
                    snackbarHostState.showSnackbar("Daily reminder scheduled for $customFormatted")
                }
            },
            initialHour,
            initialMinute,
            false // 12-hour AM/PM format
        ).show()
    }

    // Load persisted preferences and verify FCM token sync
    LaunchedEffect(Unit) {
        val sessionManager = SessionManager.getInstance(context)
        try {
            notificationsEnabled = sessionManager.notificationsEnabledFlow.first()
            dailyReminder = sessionManager.dailyReminderEnabledFlow.first()
            reminderTime = sessionManager.reminderTimeFlow.first()
            deadlineReminder = sessionManager.deadlineReminderEnabledFlow.first()
            breakReminder = sessionManager.breakReminderEnabledFlow.first()
            aiReportReminder = sessionManager.aiReportReminderEnabledFlow.first()

            if (notificationsEnabled && dailyReminder) {
                ReminderScheduler.scheduleDailyReminder(context, reminderTime, true)
            }
        } catch (_: Exception) {}

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
            // Top Bar with Sleek Circular Back Button
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
                        onCheckedChange = { isChecked ->
                            notificationsEnabled = isChecked
                            ReminderScheduler.scheduleDailyReminder(
                                context,
                                reminderTime,
                                isChecked && dailyReminder
                            )
                            // Save master toggle immediately
                            scope.launch(Dispatchers.IO) {
                                val sessionManager = SessionManager.getInstance(context)
                                sessionManager.saveNotificationPreferences(
                                    notificationsEnabled = isChecked,
                                    dailyReminder = dailyReminder,
                                    reminderTime = reminderTime,
                                    deadlineReminder = deadlineReminder,
                                    breakReminder = breakReminder,
                                    aiReportReminder = aiReportReminder
                                )
                                val uid = sessionManager.userSessionFlow.first().userId ?: 1
                                try {
                                    RetrofitClient.authApiService.savePreferences(
                                        SavePreferencesRequest(
                                            userId = uid,
                                            primaryGoal = "Study",
                                            preferredFocusDuration = 45,
                                            dailyFocusGoalMinutes = 210,
                                            remindersEnabled = if (isChecked && dailyReminder) 1 else 0,
                                            reminderTime = reminderTime
                                        )
                                    )
                                } catch (_: Exception) {}
                            }
                        },
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

            // WHEN NOTIFICATIONS ARE OFF: NOTHING SHOWS UP BELOW
            AnimatedVisibility(
                visible = notificationsEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. DAILY FOCUS REMINDER CARD WITH CUSTOM TIME PICKER
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
                                    checked = dailyReminder,
                                    onCheckedChange = { isDaily ->
                                        dailyReminder = isDaily
                                        ReminderScheduler.scheduleDailyReminder(
                                            context,
                                            reminderTime,
                                            isDaily && notificationsEnabled
                                        )
                                        scope.launch(Dispatchers.IO) {
                                            val sessionManager = SessionManager.getInstance(context)
                                            sessionManager.saveNotificationPreferences(
                                                notificationsEnabled = notificationsEnabled,
                                                dailyReminder = isDaily,
                                                reminderTime = reminderTime,
                                                deadlineReminder = deadlineReminder,
                                                breakReminder = breakReminder,
                                                aiReportReminder = aiReportReminder
                                            )
                                            val uid = sessionManager.userSessionFlow.first().userId ?: 1
                                            try {
                                                RetrofitClient.authApiService.savePreferences(
                                                    SavePreferencesRequest(
                                                        userId = uid,
                                                        primaryGoal = "Study",
                                                        preferredFocusDuration = 45,
                                                        dailyFocusGoalMinutes = 210,
                                                        remindersEnabled = if (notificationsEnabled && isDaily) 1 else 0,
                                                        reminderTime = reminderTime
                                                    )
                                                )
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.primary,
                                        uncheckedThumbColor = colors.textMuted,
                                        uncheckedTrackColor = colors.surfaceVariant,
                                        uncheckedBorderColor = colors.border
                                    )
                                )
                            }

                            if (dailyReminder) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = colors.border, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Reminder Time",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Custom schedule enabled",
                                        fontSize = 11.5.sp,
                                        color = colors.primaryLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Interactive Custom Time Box (Tapping opens full native TimePickerDialog)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = colors.surfaceVariant,
                                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bounceClick()
                                        .clickable { openTimePicker() }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = colors.primaryLight,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = reminderTime,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = "Tap to pick any custom hour & minute",
                                                    fontSize = 11.5.sp,
                                                    color = colors.textMuted
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = colors.primary.copy(alpha = 0.16f),
                                            border = BorderStroke(1.dp, colors.primary)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Time",
                                                    tint = colors.primaryLight,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Set Custom",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.primaryLight
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Presets and Custom Chip Row
                                Text(
                                    text = "Quick Presets",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("08:00 AM", "09:00 AM", "10:00 AM", "08:00 PM").forEach { quickTime ->
                                        val isSelected = reminderTime == quickTime
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surfaceVariant,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) colors.primary else colors.border
                                            ),
                                            modifier = Modifier
                                                .bounceClick()
                                                .clickable {
                                                    reminderTime = quickTime
                                                    ReminderScheduler.scheduleDailyReminder(
                                                        context,
                                                        quickTime,
                                                        notificationsEnabled && dailyReminder
                                                    )
                                                    scope.launch(Dispatchers.IO) {
                                                        val sessionManager = SessionManager.getInstance(context)
                                                        sessionManager.saveNotificationPreferences(
                                                            notificationsEnabled = notificationsEnabled,
                                                            dailyReminder = dailyReminder,
                                                            reminderTime = quickTime,
                                                            deadlineReminder = deadlineReminder,
                                                            breakReminder = breakReminder,
                                                            aiReportReminder = aiReportReminder
                                                        )
                                                    }
                                                }
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
                                    checked = deadlineReminder,
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
                                    checked = breakReminder,
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
                                    checked = aiReportReminder,
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
                                snackbarHostState.showSnackbar("Test notification dispatched & token verified.")
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Preferences Button
                    Button(
                        onClick = {
                            ReminderScheduler.scheduleDailyReminder(
                                context,
                                reminderTime,
                                notificationsEnabled && dailyReminder
                            )
                            scope.launch {
                                val sessionManager = SessionManager.getInstance(context)
                                sessionManager.saveNotificationPreferences(
                                    notificationsEnabled = notificationsEnabled,
                                    dailyReminder = dailyReminder,
                                    reminderTime = reminderTime,
                                    deadlineReminder = deadlineReminder,
                                    breakReminder = breakReminder,
                                    aiReportReminder = aiReportReminder
                                )
                                val uid = sessionManager.userSessionFlow.first().userId ?: 1
                                try {
                                    RetrofitClient.authApiService.savePreferences(
                                        SavePreferencesRequest(
                                            userId = uid,
                                            primaryGoal = "Study",
                                            preferredFocusDuration = 45,
                                            dailyFocusGoalMinutes = 210,
                                            remindersEnabled = if (notificationsEnabled) 1 else 0,
                                            reminderTime = reminderTime
                                        )
                                    )
                                } catch (_: Exception) {}

                                TokenSyncHelper.syncForCurrentUser(context)

                                snackbarHostState.showSnackbar("Preferences saved. Reminder set for $reminderTime")
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
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}
