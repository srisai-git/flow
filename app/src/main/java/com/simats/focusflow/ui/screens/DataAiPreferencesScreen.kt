package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun DataAiPreferencesScreen(onBack: () -> Unit = {}, onNavigateToExport: () -> Unit = {}) {
    var personalizedAnalysis by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Data & Groot AI preferences", null, onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🛡", color = Color(0xFF4FD1C5), fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Privacy first. You choose what to keep.", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Your actual records", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Focus sessions · tasks · interruption logs · ratings", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personalized analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Groot AI · uses your recorded history", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Switch(checked = personalizedAnalysis, onCheckedChange = { personalizedAnalysis = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Groot AI insights enabled. Disable anytime.", fontSize = 14.sp, color = Color(0xFF9CA3AF))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        Text("🛢", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Groot AI Analysis status", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Groot AI Assistant is active and analyzing your focus routines.", fontSize = 16.sp, color = Color(0xFF1E1E24))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onNavigateToExport, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE5E7EB)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))) {
                Text("Export CSV", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1.3f).height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFFCA5A5)), colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFB91C1C))) {
                Text("Delete recorded history", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("CONFIRMATION STATE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Delete recorded history?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Delete all four record types permanently? This cannot be undone. Sample data stays.", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))) {
                        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5), contentColor = Color(0xFFB91C1C))) {
                        Text("Delete history", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}