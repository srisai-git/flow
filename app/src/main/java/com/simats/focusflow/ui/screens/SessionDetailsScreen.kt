package com.simats.focusflow.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun SessionDetailsScreen(onBack: () -> Unit = {}, onViewHistory: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Session details", "MONDAY, 12 OCT 2026", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("Sample session preview", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Study machine learning", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Unsaved preview · not in weekly or Home totals", fontSize = 14.sp, color = Color(0xFF6B7280))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Focused time", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("25 min", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("planned", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("25 min", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("actual focused", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total elapsed", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("25:42", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Interruption time is excluded from focused minutes.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Interruption log", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Social Media · 1 interruption", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("42 sec", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Opened Instagram after a notification.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Focus rating", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("4 / 5", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(4) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                    }
                    Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Session note", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Reviewed classification metrics, compared precision and recall, and solved 3 practice questions.", fontSize = 14.sp, color = Color(0xFF6B7280), lineHeight = 20.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF1E1E24)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("View Session History", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}