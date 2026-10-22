package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.BottomNavBar

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToDataAi: () -> Unit = {},
    onNavigateToDailyGoal: () -> Unit = {},
    onNavigateToExport: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("Alex Morgan") }
    var userRole by remember { mutableStateOf("B.Sc. AI & Data Science · demo profile") }
    var selectedTheme by remember { mutableStateOf("Light · default") }
    var selectedDuration by remember { mutableStateOf("45 minutes") }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        bottomBar = { 
            BottomNavBar(selectedItem = "Profile", onNavigate = {
                when(it) {
                    "Home" -> onNavigateToHome()
                    "Insights" -> onNavigateToInsights()
                    "Tasks" -> onNavigateToTasks()
                }
            }) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your workspace", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("PROFILE & SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                }
                IconButton(onClick = { showEditProfileDialog = true }) {
                    Text("•••", fontSize = 24.sp, color = Color(0xFF1E1E24))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEditProfileDialog = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFF3E8FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 32.sp, color = Color(0xFF7158E2))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(userRole, fontSize = 14.sp, color = Color(0xFF6B7280))
                }
                Text("Edit", fontSize = 14.sp, color = Color(0xFF7158E2), fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SettingsBlock(
                items = listOf(
                    SettingItemData("⛶", "Daily focus goal", "3h 30m · progress rounded on Home", onNavigateToDailyGoal),
                    SettingItemData("☾", "Theme", selectedTheme) { showThemeDialog = true },
                    SettingItemData("⏱", "Default duration", selectedDuration) { showDurationDialog = true }
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsBlock(
                items = listOf(
                    SettingItemData("🔔", "Notifications & Reminders", "Manage alerts & prompts", onNavigateToNotifications),
                    SettingItemData("⚏", "Data & AI Preferences", "Privacy & analytics settings", onNavigateToDataAi),
                    SettingItemData("🛡", "Privacy", "Your records, your control.") { showPrivacyDialog = true }
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsBlock(
                items = listOf(
                    SettingItemData("↓", "Export productivity data", "CSV · choose records to include", onNavigateToExport),
                    SettingItemData("↺", "Reset sample data", "Restore fictional demo records") { showResetDialog = true },
                    SettingItemData("ⓘ", "About FocusFlow AI", "Academic design concept · v1.0") { showAboutDialog = true }
                )
            )
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempRole by remember { mutableStateOf(userRole) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Update your profile information.", fontSize = 14.sp, color = Color(0xFF6B7280))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempRole,
                        onValueChange = { tempRole = it },
                        label = { Text("Role / Major") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) userName = tempName
                        if (tempRole.isNotBlank()) userRole = tempRole
                        showEditProfileDialog = false
                        snackbarMessage = "Profile updated successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (showThemeDialog) {
        val themes = listOf("Light · default", "Dark mode", "System default")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = theme
                                    showThemeDialog = false
                                    snackbarMessage = "Theme set to $theme"
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == selectedTheme),
                                onClick = {
                                    selectedTheme = theme
                                    showThemeDialog = false
                                    snackbarMessage = "Theme set to $theme"
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7158E2))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme, fontSize = 16.sp, color = Color(0xFF1E1E24))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (showDurationDialog) {
        val durations = listOf("25 minutes (Pomodoro)", "45 minutes (Standard)", "60 minutes (Deep Focus)", "90 minutes (Extended)")
        AlertDialog(
            onDismissRequest = { showDurationDialog = false },
            title = { Text("Default Focus Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    durations.forEach { duration ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDuration = duration.split(" ")[0] + " " + duration.split(" ")[1]
                                    showDurationDialog = false
                                    snackbarMessage = "Default duration updated to $selectedDuration"
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDuration.startsWith(duration.split(" ")[0]),
                                onClick = {
                                    selectedDuration = duration.split(" ")[0] + " " + duration.split(" ")[1]
                                    showDurationDialog = false
                                    snackbarMessage = "Default duration updated to $selectedDuration"
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7158E2))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(duration, fontSize = 16.sp, color = Color(0xFF1E1E24))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDurationDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your privacy is guaranteed:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("• All session logs, tasks, and notes are stored locally on your device.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("• No personal data is transmitted to external servers without explicit consent.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("• AI recommendations run locally using private parameters.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("• You can export or delete your history at any time.", fontSize = 14.sp, color = Color(0xFF4B5563))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2))
                ) {
                    Text("Understood")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Sample Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action will restore all fictional sample focus records, demo tasks, and weekly stats to their initial state.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        snackbarMessage = "Sample data reset successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About FocusFlow AI", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FocusFlow AI v1.0.0", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF7158E2))
                    Text("Smart Productivity & Focus Assistant", fontSize = 14.sp, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Designed as an academic project for focus session tracking, intelligent distraction management, and pattern analysis.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Built with Kotlin & Jetpack Compose.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2))
                ) {
                    Text("Close")
                }
            }
        )
    }
}

data class SettingItemData(
    val icon: String,
    val title: String,
    val subtitle: String? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun SettingsBlock(items: List<SettingItemData>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.onClick != null) { item.onClick?.invoke() }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(item.icon, fontSize = 20.sp, color = Color(0xFF6B7280), modifier = Modifier.width(32.dp))
                        Column {
                            Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                            if (item.subtitle != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.subtitle, fontSize = 14.sp, color = Color(0xFF6B7280))
                            }
                        }
                    }
                    Text(">", fontSize = 18.sp, color = Color(0xFF9CA3AF))
                }
                if (index < items.size - 1) {
                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
        }
    }
}