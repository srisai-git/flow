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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(onBack: () -> Unit = {}, onSearch: () -> Unit = {}, onSessionClick: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf("Week") }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Session history", "SAMPLE DATA · 05–11 OCT 2026", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        SegmentedControl(options = listOf("Week", "Month"), selectedOption = selectedTab, onOptionSelected = { selectedTab = it })
        
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search sessions", color = Color(0xFF6B7280)) },
            leadingIcon = { Text("🔍", color = Color(0xFF6B7280), fontSize = 16.sp) },
            trailingIcon = { Text("≑", color = Color(0xFF7158E2), fontSize = 24.sp, modifier = Modifier.clickable { onSearch() }.padding(12.dp)) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFFE5E7EB), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("20 sample sessions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("12h 30m", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Archived sample week · 750 focused minutes", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Latest preview · outside this week", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp)).clickable { onSessionClick() }.padding(24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("Unsaved preview", color = Color(0xFF7158E2), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("12 Oct · 25 min", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Study machine learning", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("1 interruption · Focus 4/5 · View details →", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Not included in the 20 sessions or weekly totals.", fontSize = 12.sp, color = Color(0xFF0D9488))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent archived samples", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(16.dp))
        
        val archived = listOf(
            Triple("Sun 11 Oct · 09:00", "Study machine learning", "30 min"),
            Triple("Sat 10 Oct · 10:00", "Practice Python", "45 min"),
            Triple("Fri 09 Oct · 14:00", "Finish project UI", "35 min")
        )
        
        archived.forEach { item ->
            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp).padding(bottom = 8.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.first, fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(item.third, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.second, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("0 interruptions · Focus 4/5", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("3 of 20 archived samples shown.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
    }
}