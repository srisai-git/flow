package com.simats.focusflow.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.simats.focusflow.data.models.ProfileData
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val DangerRed = Color(0xFFEF4444)

fun resolveAvatarIcon(avatarStr: String): ImageVector = when (avatarStr.removePrefix("avatar:").lowercase()) {
    "school" -> Icons.Default.School
    "code", "computer" -> Icons.Default.Computer
    "palette", "art" -> Icons.Default.Palette
    "rocket" -> Icons.Default.RocketLaunch
    "bolt", "energy" -> Icons.Default.FlashOn
    "brain", "psychology" -> Icons.Default.Psychology
    "coffee" -> Icons.Default.Coffee
    "sparkle", "star" -> Icons.Default.AutoAwesome
    "camera" -> Icons.Default.PhotoCamera
    "gallery" -> Icons.Default.Image
    else -> Icons.Default.Person
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDataAi: () -> Unit = {},
    onNavigateToDailyGoal: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToAboutFlow: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
        viewModel.loadProfile()
    }

    val colors = FocusFlowTheme.colors
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPreferencesSheet by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. TOP HEADER & TITLE
            Text(
                text = "PROFILE & ACCOUNT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your Workspace",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. USER PROFILE HERO CARD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEditProfile() }
                    .bounceClick(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Avatar with edit pen badge
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.18f))
                                .border(1.5.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarStr = profile.avatar
                            val isImageUri = avatarStr.startsWith("content:") ||
                                    avatarStr.startsWith("file:") ||
                                    avatarStr.startsWith("http:") ||
                                    avatarStr.startsWith("https:")

                            if (isImageUri) {
                                AsyncImage(
                                    model = avatarStr,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else if (avatarStr.startsWith("avatar:")) {
                                Icon(
                                    imageVector = resolveAvatarIcon(avatarStr),
                                    contentDescription = "User",
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(38.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User",
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        // Edit pen badge
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                                .border(1.5.dp, colors.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name.ifBlank { "Focus User" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val bioText = profile.bio.ifBlank { "Dedicated FocusFlow Explorer" }
                        Text(
                            text = bioText,
                            fontSize = 13.sp,
                            color = colors.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PRO MEMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryLight,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Edit",
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. STATS SUMMARY ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Focus Hours Stat
                StatSummaryCard(
                    icon = Icons.Default.Timer,
                    value = String.format(Locale.US, "%.1fh", profile.totalFocusHours),
                    label = "Focus Time",
                    iconTint = colors.primaryLight,
                    modifier = Modifier.weight(1f)
                )

                // Tasks Done Stat
                StatSummaryCard(
                    icon = Icons.Default.CheckCircle,
                    value = "${profile.completedTasksCount}",
                    label = "Tasks Done",
                    iconTint = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                // Current Streak Stat
                StatSummaryCard(
                    icon = Icons.Default.Star,
                    value = "${profile.currentStreak}d",
                    label = "Streak",
                    iconTint = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. MENU SECTION: PREFERENCES & FOCUS SETTINGS
            Text(
                text = "PREFERENCES & FOCUS SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column {
                    val goalHours = (profile.dailyFocusGoalMinutes / 60).coerceAtLeast(1)
                    val rhythmBlock = if (profile.preferredFocusDuration > 0) profile.preferredFocusDuration else 45
                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        title = "Focus Target & Preferences",
                        subtitle = "${goalHours}h daily goal • ${rhythmBlock}m intervals",
                        onClick = { showPreferencesSheet = true }
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)

                    // Dynamic Theme Selector Row
                    ProfileMenuRow(
                        icon = Icons.Default.Palette,
                        title = "Appearance & Theme",
                        subtitle = if (state.themeMode == "light") "Clean Light" else "Default Dark",
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)

                    // Daily Focus Reminders Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colors.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Daily Focus Reminders",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (profile.remindersEnabled) "Alerts scheduled for morning focus" else "Reminders muted",
                                    fontSize = 12.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                        Switch(
                            checked = profile.remindersEnabled,
                            onCheckedChange = { viewModel.toggleReminders() },
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

            Spacer(modifier = Modifier.height(24.dp))

            // 5. MENU SECTION: DATA & SYSTEM
            Text(
                text = "DATA & SYSTEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Share,
                        title = "Export Productivity Data",
                        subtitle = "Generate & share CSV summary",
                        onClick = {
                            shareProductivityCsv(context, profile)
                        }
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)

                    ProfileMenuRow(
                        icon = Icons.Default.Refresh,
                        title = "Clear App Data / Reset Stats",
                        subtitle = "Reset focus history, sessions & streaks",
                        onClick = { viewModel.setResetDialogOpen(true) },
                        titleColor = Color(0xFFF59E0B),
                        iconTint = Color(0xFFF59E0B)
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)

                    ProfileMenuRow(
                        icon = Icons.Default.Info,
                        title = "About FocusFlow",
                        subtitle = "Architecture & version 1.0.0",
                        onClick = onNavigateToAboutFlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. LOG OUT BUTTON
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed.copy(alpha = 0.12f),
                    contentColor = DangerRed
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log Out",
                    tint = DangerRed,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Log Out of Session",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }

        // 7. FOCUS TARGET & PREFERENCES BOTTOM SHEET
        if (showPreferencesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPreferencesSheet = false },
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
                FocusPreferencesSheetContent(
                    initialGoalHours = (profile.dailyFocusGoalMinutes / 60).coerceIn(1, 12),
                    initialRhythmBlock = if (profile.preferredFocusDuration in listOf(25, 45, 60)) profile.preferredFocusDuration else 45,
                    onDismiss = { showPreferencesSheet = false },
                    onSave = { goalHours, rhythmBlock ->
                        viewModel.updateFocusPreferences(goalHours, rhythmBlock)
                        showPreferencesSheet = false
                    }
                )
            }
        }

        // 8. THEME SELECTION DIALOG
        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = state.themeMode,
                onSelectTheme = { selectedMode ->
                    viewModel.setThemeMode(selectedMode)
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        // 9. RESET STATS CONFIRMATION DIALOG
        if (state.showResetDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setResetDialogOpen(false) },
                containerColor = colors.surface,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Reset Productivity Stats?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to clear your focus sessions, habit streaks, and task completions? Your account will remain active, but stats will reset to zero.",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetStats() },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Yes, Reset All", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setResetDialogOpen(false) }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                }
            )
        }

        // 10. LOGOUT CONFIRMATION DIALOG
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = colors.surface,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Log Out of FocusFlow?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = "Your local session will be terminated and you will return to the sign in screen.",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: String,
    onSelectTheme: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Choose Appearance",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select your preferred visual style across FocusFlow.",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
                // Default Dark Option Card
                Surface(
                    onClick = {
                        onSelectTheme("dark")
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (currentTheme == "dark") colors.primary.copy(alpha = 0.15f) else colors.surfaceVariant,
                    border = BorderStroke(
                        width = if (currentTheme == "dark") 2.dp else 1.dp,
                        color = if (currentTheme == "dark") colors.primary else colors.border
                    ),
                    modifier = Modifier.fillMaxWidth().bounceClick()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F0F14)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8B5CF6))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Default Dark",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Deep midnight canvas with violet accents",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                        if (currentTheme == "dark") {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Clean Light Option Card
                Surface(
                    onClick = {
                        onSelectTheme("light")
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (currentTheme == "light") colors.primary.copy(alpha = 0.15f) else colors.surfaceVariant,
                    border = BorderStroke(
                        width = if (currentTheme == "light") 2.dp else 1.dp,
                        color = if (currentTheme == "light") colors.primary else colors.border
                    ),
                    modifier = Modifier.fillMaxWidth().bounceClick()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF8F9FA))
                                    .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7C3AED))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Clean Light",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Crisp daylight aesthetic with bright surfaces",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                        if (currentTheme == "light") {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun FocusPreferencesSheetContent(
    initialGoalHours: Int,
    initialRhythmBlock: Int,
    onDismiss: () -> Unit,
    onSave: (goalHours: Int, rhythmBlock: Int) -> Unit
) {
    val colors = FocusFlowTheme.colors
    var goalHoursVal by remember { mutableFloatStateOf(initialGoalHours.toFloat()) }
    var selectedRhythm by remember { mutableIntStateOf(initialRhythmBlock) }
    val rhythmOptions = listOf(25, 45, 60)

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
            Text(
                text = "Focus Target & Preferences",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DAILY TARGET SLIDER (1 - 12 HOURS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Target Goal",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = "${goalHoursVal.roundToInt()} hours / day",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = goalHoursVal,
            onValueChange = { goalHoursVal = it },
            valueRange = 1f..12f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            )
        )
        Text(
            text = "Recommended for sustainable deep work: 3 - 5 hours daily",
            fontSize = 11.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PREFERRED SESSION RHYTHM (25, 45, 60 MINS)
        Text(
            text = "PREFERRED SESSION RHYTHM",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryLight,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rhythmOptions.forEach { rhythm ->
                val isSelected = selectedRhythm == rhythm
                Surface(
                    onClick = { selectedRhythm = rhythm },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) colors.primary else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border),
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${rhythm}m",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (rhythm) {
                                25 -> "Pomodoro"
                                45 -> "Standard"
                                else -> "Deep Block"
                            },
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                onSave(goalHoursVal.roundToInt(), selectedRhythm)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Preferences",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun shareProductivityCsv(context: Context, profile: ProfileData) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    val csv = buildString {
        appendLine("FOCUSFLOW PRODUCTIVITY EXPORT")
        appendLine("Exported At,$timestamp")
        appendLine("User Name,${profile.name}")
        appendLine("User Email,${profile.email}")
        appendLine("Total Focus Hours,${profile.totalFocusHours}")
        appendLine("Total Focus Minutes,${profile.totalFocusMinutes}")
        appendLine("Tasks Completed,${profile.completedTasksCount}")
        appendLine("Current Streak Days,${profile.currentStreak}")
        appendLine("Daily Focus Goal (Minutes),${profile.dailyFocusGoalMinutes}")
        appendLine("Preferred Session Duration (Minutes),${profile.preferredFocusDuration}")
        appendLine("Daily Reminders Enabled,${profile.remindersEnabled}")
        appendLine("")
        appendLine("Date,Metric,Unit,Value")
        appendLine("$dateStr,Focus Duration,Hours,${profile.totalFocusHours}")
        appendLine("$dateStr,Focus Duration,Minutes,${profile.totalFocusMinutes}")
        appendLine("$dateStr,Completed Tasks,Count,${profile.completedTasksCount}")
        appendLine("$dateStr,Active Streak,Days,${profile.currentStreak}")
        appendLine("$dateStr,Daily Target,Hours,${(profile.dailyFocusGoalMinutes / 60).coerceAtLeast(1)}")
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "FocusFlow Productivity Export - $dateStr")
        putExtra(Intent.EXTRA_TEXT, csv)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export Productivity Data (CSV)"))
}

@Composable
private fun StatSummaryCard(
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = FocusFlowTheme.colors
    Surface(
        modifier = modifier.bounceClick(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: colors.primaryLight,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    titleColor: Color? = null,
    iconTint: Color? = null
) {
    val colors = FocusFlowTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: colors.primaryLight,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor ?: colors.textPrimary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
