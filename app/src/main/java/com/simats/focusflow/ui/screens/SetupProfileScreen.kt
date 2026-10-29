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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.SetupProfileUiState
import com.simats.focusflow.ui.viewmodel.SetupProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    userId: Int = 1,
    initialName: String = "Sri Sai",
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: SetupProfileViewModel? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var bio by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("avatar:person") }
    var isLoading by remember { mutableStateOf(false) }

    if (viewModel != null) {
        val uiState by viewModel.uiState.collectAsState()
        isLoading = uiState is SetupProfileUiState.Loading
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Setup Your Profile", null, onBack)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose your profile photo and tell us a bit about yourself.",
            fontSize = 16.sp,
            color = Color(0xFF6B7280),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Avatar Section Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF0EDFF), CircleShape)
                        .border(3.dp, Color(0xFF7158E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedAvatar, fontSize = 50.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { selectedAvatar = "avatar:camera" },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2)),
                        modifier = Modifier.bounceClick()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Take Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }

                    OutlinedButton(
                        onClick = { selectedAvatar = "avatar:gallery" },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2)),
                        modifier = Modifier.bounceClick()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Choose Gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("PROFILE DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
        Spacer(modifier = Modifier.height(12.dp))

        // Profile Details Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Full Name
                Column {
                    Text("Full Name", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF7158E2)
                        ),
                        singleLine = true
                    )
                }

                // Bio / Status
                Column {
                    Text("Bio / Status", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = {
                            Text(
                                "e.g., B.Sc. AI & Data Science student passionate about focus routines.",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF7158E2)
                        ),
                        maxLines = 3
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Primary Action Button
        Button(
            onClick = {
                if (viewModel != null) {
                    viewModel.setupProfile(
                        userId = userId,
                        name = name,
                        avatar = selectedAvatar,
                        bio = bio,
                        onSuccess = onContinue
                    )
                } else {
                    onContinue()
                }
            },
            enabled = !isLoading && name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Save & Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Text Button: Skip for now
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Skip for now",
                color = Color(0xFF7158E2),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { onSkip() }
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupProfileScreenPreview() {
    SetupProfileScreen()
}