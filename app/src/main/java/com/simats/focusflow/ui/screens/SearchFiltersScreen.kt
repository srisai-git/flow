package com.simats.focusflow.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFiltersScreen(onBack: () -> Unit = {}, onApply: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("machine learning") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Search and filters", "TASKS & SESSION HISTORY", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Search tasks and sessions", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFF7158E2), focusedBorderColor = Color(0xFF7158E2), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Category", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "Study", onValueChange = {}, readOnly = true,
                    shape = RoundedCornerShape(12.dp), trailingIcon = { Text("v", color = Color(0xFF6B7280), modifier = Modifier.padding(end=8.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), unfocusedContainerColor = Color.White)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Task status", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "Not started", onValueChange = {}, readOnly = true,
                    shape = RoundedCornerShape(12.dp), trailingIcon = { Text("v", color = Color(0xFF6B7280), modifier = Modifier.padding(end=8.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), unfocusedContainerColor = Color.White)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Priority", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "High", onValueChange = {}, readOnly = true,
                    shape = RoundedCornerShape(12.dp), trailingIcon = { Text("v", color = Color(0xFF6B7280), modifier = Modifier.padding(end=8.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), unfocusedContainerColor = Color.White)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Date range", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "05-12 Oct", onValueChange = {}, readOnly = true,
                    shape = RoundedCornerShape(12.dp), trailingIcon = { Text("v", color = Color(0xFF6B7280), modifier = Modifier.padding(end=8.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), unfocusedContainerColor = Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Status and priority apply to tasks only.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Matching results · 2", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Column {
                Text("TASK", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(20.dp).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Study machine learning", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                        Text("Study · 45 min · 12 Oct 10:00", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Box(modifier = Modifier.background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("High", color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Column {
                Text("SESSION PREVIEW · NOT COUNTED", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Study machine learning", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(4.dp))
                Text("12 Oct 2026 · 25 min · Focus 4/5", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                Text("View details →", fontSize = 14.sp, color = Color(0xFF7158E2))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Apply filters", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Clear selections", color = Color(0xFF7158E2), fontSize = 14.sp)
        }
    }
}