package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.bounceClick

private val CanvasDark = Color(0xFF0F0F14)
private val CardDark = Color(0xFF181722)
private val BorderDark = Color(0xFF262433)
private val VioletPrimary = Color(0xFF8B5CF6)
private val VioletLight = Color(0xFFC4B5FD)
private val TextWhite = Color(0xFFF1F1F5)
private val TextMuted = Color(0xFF9CA3AF)

@Composable
fun AboutFlowScreen(
    onBack: () -> Unit = {}
) {
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

            // 1. TOP HEADER WITH BACK BUTTON
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
                        text = "ABOUT FOCUSFLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Architecture & Engine",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. HERO APP BRANDING CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.18f))
                            .border(1.5.dp, VioletPrimary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "FocusFlow Logo",
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "FOCUSFLOW",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VioletPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "Version 1.0.0 (Production)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VioletLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mindful, distraction-free productivity engine designed for deep work sessions and deliberate habit formation.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. CORE PHILOSOPHY CARD
            InfoSectionCard(
                icon = Icons.Default.Psychology,
                title = "Core Philosophy",
                content = "FocusFlow is built around the science of deep work and cognitive rhythm. We reject superficial gamification in favor of minimal, intentional user workflows that cultivate unbroken concentration and genuine progress."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. ARCHITECTURE OVERVIEW CARD
            InfoSectionCard(
                icon = Icons.Default.Layers,
                title = "Architecture & Engineering",
                content = "Strict Clean MVVM Architecture featuring unidirectional data flow (UDF). Presentation layer powered entirely by Jetpack Compose. State synchronization handled via StateFlow and Coroutines, paired with offline-first persistence."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. TECH STACK SPECIFICATION CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = VioletLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Technology Stack",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TechSpecItem(label = "Language", value = "Kotlin 1.9 & Coroutines")
                    TechSpecItem(label = "UI Toolkit", value = "Jetpack Compose + Material 3")
                    TechSpecItem(label = "Session Layer", value = "Jetpack DataStore Preferences")
                    TechSpecItem(label = "Network Layer", value = "Retrofit 2.9 + OkHttp3 + Gson")
                    TechSpecItem(label = "Image Engine", value = "Coil Compose 2.6")
                    TechSpecItem(label = "Backend & DB", value = "PHP 8.2 & MySQL REST Engine")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. PRIVACY & DATA GUARANTEE CARD
            InfoSectionCard(
                icon = Icons.Default.Security,
                title = "Privacy & Data Sovereignity",
                content = "All user credentials, session history, and task logs are secured in isolated DataStore storage with complete support for on-demand CSV data export and instant local stats wipe."
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun InfoSectionCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VioletLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun TechSpecItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextWhite,
            fontWeight = FontWeight.SemiBold
        )
    }
}
