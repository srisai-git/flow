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
import com.simats.focusflow.ui.components.bounceClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CanvasDark = Color(0xFF0F0F14)
private val CardDark = Color(0xFF181722)
private val BorderDark = Color(0xFF262433)
private val VioletPrimary = Color(0xFF8B5CF6)
private val VioletLight = Color(0xFFC4B5FD)
private val TextWhite = Color(0xFFF1F1F5)
private val TextMuted = Color(0xFF9CA3AF)

@Composable
fun ExportDataScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var incSessions by remember { mutableStateOf(true) }
    var incTasks by remember { mutableStateOf(true) }
    var incStreaks by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = CanvasDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CanvasDark)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, BorderDark, CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "DATA MANAGEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Export Productivity Data",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Export your local focus logs, task completions, and streak records into standard CSV format for external analysis or backup.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // RECORD TYPES CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "INCLUDE RECORD TYPES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletLight,
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
                            colors = CheckboxDefaults.colors(checkedColor = VioletPrimary, uncheckedColor = TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Focus Sessions & Duration Logs", fontSize = 14.sp, color = TextWhite)
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
                            colors = CheckboxDefaults.colors(checkedColor = VioletPrimary, uncheckedColor = TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Task History & Completions", fontSize = 14.sp, color = TextWhite)
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
                            colors = CheckboxDefaults.colors(checkedColor = VioletPrimary, uncheckedColor = TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Habit Streaks & Daily Routine Metrics", fontSize = 14.sp, color = TextWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CSV PREVIEW
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CSV Format Preview", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        Text("text/csv", fontSize = 12.sp, color = VioletLight, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF111019),
                        border = BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("date,record_type,metric,value", fontSize = 12.sp, color = VioletLight, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2026-10-29,focus_session,duration_min,45", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            Text("2026-10-29,task_completion,priority_task,1", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            Text("2026-10-29,habit_routine,streak_days,4", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // EXPORT BUTTON
            Button(
                onClick = {
                    shareCustomCsv(context, incSessions, incTasks, incStreaks)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export & Share CSV", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun shareCustomCsv(context: Context, incSessions: Boolean, incTasks: Boolean, incStreaks: Boolean) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    val csv = buildString {
        appendLine("FOCUSFLOW PRODUCTIVITY EXPORT")
        appendLine("Export Date,$timestamp")
        appendLine("")
        appendLine("date,record_type,metric,value")
        if (incSessions) {
            appendLine("$dateStr,focus_session,duration_minutes,45")
            appendLine("$dateStr,focus_session,duration_minutes,25")
        }
        if (incTasks) {
            appendLine("$dateStr,task,completed_tasks,14")
            appendLine("$dateStr,task,pending_priorities,2")
        }
        if (incStreaks) {
            appendLine("$dateStr,habit,active_streak_days,4")
            appendLine("$dateStr,habit,completion_rate_pct,100")
        }
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "FocusFlow Productivity Export - $dateStr")
        putExtra(Intent.EXTRA_TEXT, csv)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export Productivity Data (CSV)"))
}
