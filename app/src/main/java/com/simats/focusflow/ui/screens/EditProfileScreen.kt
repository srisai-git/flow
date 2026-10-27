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
import com.simats.focusflow.data.api.UserProfile
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.ProfileUiState
import com.simats.focusflow.ui.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    onBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    profileViewModel: ProfileViewModel? = null
) {
    var name by remember { mutableStateOf("Sri Sai") }
    var role by remember { mutableStateOf("Student") }
    var bio by remember { mutableStateOf("B.Sc. AI & Data Science student passionate about focus routines.") }
    var location by remember { mutableStateOf("Chennai, India") }
    var email by remember { mutableStateOf("sri.sai@example.com") }
    var selectedDp by remember { mutableStateOf("👤") }

    if (profileViewModel != null) {
        val uiState by profileViewModel.uiState.collectAsState()
        LaunchedEffect(uiState) {
            if (uiState is ProfileUiState.Success) {
                val p = (uiState as ProfileUiState.Success).profile
                name = p.name
                role = p.role
                bio = p.bio ?: ""
                location = p.location ?: ""
                email = p.email
                selectedDp = p.avatar ?: "👤"
            }
        }
    }

    val roleOptions = listOf("Student", "Scholar", "Employee", "Researcher")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Edit Profile", null, onBack)
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Photo Header
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
                        .size(80.dp)
                        .background(Color(0xFFF0EDFF), CircleShape)
                        .border(2.dp, Color(0xFF7158E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedDp, fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { selectedDp = "📷" },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2)),
                        modifier = Modifier.bounceClick()
                    ) {
                        Text("📷 Take Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { selectedDp = "🖼️" },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2)),
                        modifier = Modifier.bounceClick()
                    ) {
                        Text("🖼️ Choose Gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("PUBLIC PROFILE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
        Spacer(modifier = Modifier.height(12.dp))

        // Public Profile Fields Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Name
                Column {
                    Text("Name", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
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

                // Role
                Column {
                    Text("Role", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF7158E2)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        roleOptions.forEach { option ->
                            val isSelected = role == option
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) Color(0xFFF0EDFF) else Color(0xFFF3F4F6),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF7158E2) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { role = option }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color(0xFF7158E2) else Color(0xFF4B5563),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Bio
                Column {
                    Text("Bio", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF7158E2)
                        ),
                        maxLines = 3
                    )
                }

                // Location
                Column {
                    Text("Location", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF7158E2)
                        ),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("ACCOUNT SECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
        Spacer(modifier = Modifier.height(12.dp))

        // Account Section Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text("Email Address", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF7158E2)
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val updated = UserProfile(
                    id = 1,
                    name = name,
                    avatar = selectedDp,
                    role = role,
                    bio = bio,
                    location = location,
                    email = email
                )
                if (profileViewModel != null) {
                    profileViewModel.updateProfile(updated) {
                        onSaveSuccess()
                    }
                } else {
                    onSaveSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Profile Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen()
}