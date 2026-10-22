package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataScreen(onBack: () -> Unit = {}) {
    var dataType by remember { mutableStateOf("Sample records") }
    var incSessions by remember { mutableStateOf(true) }
    var incTasks by remember { mutableStateOf(true) }
    var incInterrupts by remember { mutableStateOf(true) }
    var incRatings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Export data", "DEMO CSV PREVIEW", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        SegmentedControl(options = listOf("Sample records", "Actual records · 0"), selectedOption = dataType, onOptionSelected = { dataType = it })
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sample data · fictional records, not your history", fontSize = 14.sp, color = Color(0xFF0D9488))
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(0.4f)) {
                Text("Format", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "CSV", onValueChange = {}, readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                )
            }
            Column(modifier = Modifier.weight(0.6f)) {
                Text("Date range", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "05-11 Oct 2026", onValueChange = {}, readOnly = true,
                    shape = RoundedCornerShape(12.dp), trailingIcon = { Text("v", color = Color(0xFF6B7280), modifier = Modifier.padding(end=8.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Include record types", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { incSessions = !incSessions }) {
                    Checkbox(checked = incSessions, onCheckedChange = { incSessions = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
                    Text("Focus sessions · 20 samples", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { incTasks = !incTasks }) {
                    Checkbox(checked = incTasks, onCheckedChange = { incTasks = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
                    Text("Tasks · 24 samples", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { incInterrupts = !incInterrupts }) {
                    Checkbox(checked = incInterrupts, onCheckedChange = { incInterrupts = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
                    Text("Interruption logs · 12 samples", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { incRatings = !incRatings }) {
                    Checkbox(checked = incRatings, onCheckedChange = { incRatings = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
                    Text("Ratings · optional, not selected", fontSize = 16.sp, color = Color(0xFF6B7280))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CSV preview", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("2 sample rows", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("date, record_type, task, focused_min", fontSize = 14.sp, color = Color(0xFF0D9488), fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                Text("2026-10-11, session, Study ML, 30", fontSize = 14.sp, color = Color(0xFF6B7280), fontFamily = FontFamily.Monospace)
                Text("2026-10-10, session, Practice Python, 45", fontSize = 14.sp, color = Color(0xFF6B7280), fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Abbreviated task names for preview only.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Preparing a demo CSV is simulated. No file is generated, downloaded or shared.", fontSize = 12.sp, color = Color(0xFF6B7280))
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Prepare demo CSV", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}