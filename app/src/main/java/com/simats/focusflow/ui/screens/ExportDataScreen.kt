package com.simats.focusflow.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.utils.ExportDataHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ExportDataScreen(onBack: () -> Unit = {}) {
    val colors = FocusFlowTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager.getInstance(context) }

    var incSessions by remember { mutableStateOf(true) }
    var incTasks by remember { mutableStateOf(true) }
    var incStreaks by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
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
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
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

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "DATA MANAGEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Export Productivity Data",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Export your verified focus logs, task records, and habit consistency into structured Markdown and CSV files for external analysis, reporting, or backup.",
                fontSize = 13.sp,
                color = colors.textMuted,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "INCLUDE RECORD TYPES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { incSessions = !incSessions }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = incSessions,
                            onCheckedChange = { incSessions = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.textMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Focus Sessions & Duration Logs", fontSize = 14.sp, color = colors.textPrimary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { incTasks = !incTasks }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = incTasks,
                            onCheckedChange = { incTasks = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.textMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Task History & Completions", fontSize = 14.sp, color = colors.textPrimary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { incStreaks = !incStreaks }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = incStreaks,
                            onCheckedChange = { incStreaks = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.textMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Habit Streaks & Daily Routine Metrics", fontSize = 14.sp, color = colors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Structured Formats Included", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted)
                        Text(".csv & .txt / .md", fontSize = 12.sp, color = colors.primaryLight, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("SECTION 1: FOCUS SESSIONS (Date | Time | Duration | Tag | Status)", fontSize = 11.sp, color = colors.primaryLight, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SECTION 2: HABIT CONSISTENCY (Name | Frequency | Streaks | Count)", fontSize = 11.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SECTION 3: TASKS COMPLETED (Title | Priority | Completed Date)", fontSize = 11.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (!isExporting) {
                        scope.launch {
                            isExporting = true
                            val session = sessionManager.userSessionFlow.first()
                            val uid = session.userId ?: 1
                            val uname = session.userName ?: "FocusFlow User"
                            ExportDataHelper.generateAndShareExport(
                                context = context,
                                userId = uid,
                                userName = uname,
                                incSessions = incSessions,
                                incTasks = incTasks,
                                incStreaks = incStreaks
                            )
                            isExporting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(16.dp),
                enabled = !isExporting && (incSessions || incTasks || incStreaks)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Preparing Export...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Share Structured Data", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
