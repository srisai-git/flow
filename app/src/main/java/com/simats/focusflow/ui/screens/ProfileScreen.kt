package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.tooling.preview.Preview
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
    onNavigateToExport: () -> Unit = {},
    onNavigateToAboutFlow: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("Sri Sai") }
    var userRole by remember { mutableStateOf("B.Sc. AI & Data Science") }
    var userDp by remember { mutableStateOf("👤") }
    var selectedTheme by remember { mutableStateOf("Light · default") }
    var selectedDuration by remember { mutableStateOf("45 minutes") }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
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
                    Text(userDp, fontSize = 32.sp)
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
                    SettingItemData("⚏", "Data & Groot AI Preferences", "Privacy & analytics settings", onNavigateToDataAi),
                    SettingItemData("🛡", "Privacy", "Your records, your control.") { showPrivacyDialog = true }
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsBlock(
                items = listOf(
                    SettingItemData("↓", "Export productivity data", "CSV · choose records to include", onNavigateToExport),
                    SettingItemData("↺", "Reset sample data", "Restore initial sample records") { showResetDialog = true },
                    SettingItemData("ⓘ", "About Flow", "Productivity assistant · v1.0.0", onNavigateToAboutFlow)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEF2F2),
                    contentColor = Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempRole by remember { mutableStateOf(userRole) }
        var tempDp by remember { mutableStateOf(userDp) }
        val avatarOptions = listOf("👤", "🎓", "💻", "🎨", "🚀", "⚡", "🧠", "🔥")

        AlertDialog(
            containerColor = Color.White,
            titleContentColor = Color(0xFF1E1E24),
            textContentColor = Color(0xFF1E1E24),
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose Profile Picture (DP):", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E24))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        avatarOptions.take(4).forEach { avatar ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (tempDp == avatar) Color(0xFFF0EDFF) else Color(0xFFF3F4F6), CircleShape)
                                    .border(1.dp, if (tempDp == avatar) Color(0xFF7158E2) else Color.Transparent, CircleShape)
                                    .clickable { tempDp = avatar },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatar, fontSize = 22.sp)
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        avatarOptions.drop(4).forEach { avatar ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (tempDp == avatar) Color(0xFFF0EDFF) else Color(0xFFF3F4F6), CircleShape)
                                    .border(1.dp, if (tempDp == avatar) Color(0xFF7158E2) else Color.Transparent, CircleShape)
                                    .clickable { tempDp = avatar },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatar, fontSize = 22.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Name", color = Color(0xFF6B7280)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1E24),
                            unfocusedTextColor = Color(0xFF1E1E24)
                        )
                    )
                    OutlinedTextField(
                        value = tempRole,
                        onValueChange = { tempRole = it },
                        label = { Text("Role / Major", color = Color(0xFF6B7280)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E1E24),
                            unfocusedTextColor = Color(0xFF1E1E24)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) userName = tempName
                        if (tempRole.isNotBlank()) userRole = tempRole
                        userDp = tempDp
                        showEditProfileDialog = false
                        snackbarMessage = "Profile updated successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2))
                ) {
                    Text("Save", color = Color.White)
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
            containerColor = Color.White,
            titleContentColor = Color(0xFF1E1E24),
            textContentColor = Color(0xFF1E1E24),
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24)) },
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
            containerColor = Color.White,
            titleContentColor = Color(0xFF1E1E24),
            textContentColor = Color(0xFF1E1E24),
            onDismissRequest = { showDurationDialog = false },
            title = { Text("Default Focus Duration", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24)) },
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
            containerColor = Color.White,
            titleContentColor = Color(0xFF1E1E24),
            textContentColor = Color(0xFF1E1E24),
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Security", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your privacy is guaranteed:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E1E24))
                    Text("• All session logs, tasks, and notes are stored locally on your device.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("• No personal data is transmitted to external servers without explicit consent.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("• Groot AI Assistant runs private parameters locally.", fontSize = 14.sp, color = Color(0xFF4B5563))
                    Text("• You can export or delete your history at any time.", fontSize = 14.sp, color = Color(0xFF4B5563))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2))
                ) {
                    Text("Understood", color = Color.White)
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            containerColor = Color.White,
            titleContentColor = Color(0xFF1E1E24),
            textContentColor = Color(0xFF1E1E24),
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Sample Data?", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24)) },
            text = {
                Text(
                    "This action will restore all sample focus records, tasks, and weekly stats to their initial state.",
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
                    Text("Reset Data", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            containerColor = Color.White,
            titleContentColor = Color(0xFF1E1E24),
            textContentColor = Color(0xFF1E1E24),
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out?", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24)) },
            text = {
                Text(
                    "Are you sure you want to log out of your account?",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}