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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.AvatarSelectionUiState
import com.simats.focusflow.ui.viewmodel.AvatarSelectionViewModel

@Composable
fun AvatarSelectionScreen(
    userId: Int = 1,
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onSkip: () -> Unit = {},
    avatarViewModel: AvatarSelectionViewModel? = null
) {
    var localSelectedAvatar by remember { mutableStateOf("👤") }
    var isLoading by remember { mutableStateOf(false) }

    if (avatarViewModel != null) {
        val vmAvatar by avatarViewModel.selectedAvatar.collectAsState()
        val uiState by avatarViewModel.uiState.collectAsState()
        localSelectedAvatar = vmAvatar
        isLoading = uiState is AvatarSelectionUiState.Loading
    }

    val presetAvatars = listOf("👤", "🎓", "💻", "🎨", "🚀", "⚡", "🧠", "🔥")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Choose Your Avatar", null, onBack)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pick an avatar or upload a picture for your workspace profile.",
            fontSize = 16.sp,
            color = Color(0xFF6B7280),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Large Circular Preview
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFF0EDFF), CircleShape)
                    .border(3.dp, Color(0xFF7158E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(localSelectedAvatar, fontSize = 60.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Two Action Buttons: Take Photo & Choose Gallery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    localSelectedAvatar = "📷"
                    avatarViewModel?.selectAvatar("📷")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2))
            ) {
                Text("📷 Take Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = {
                    localSelectedAvatar = "🖼️"
                    avatarViewModel?.selectAvatar("🖼️")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2))
            ) {
                Text("🖼️ Choose Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Or choose a preset avatar:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E1E24)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid/Row of Preset Avatar Icons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetAvatars.take(4).forEach { avatar ->
                        val isSelected = localSelectedAvatar == avatar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(if (isSelected) Color(0xFFF0EDFF) else Color(0xFFF3F4F6), CircleShape)
                                .border(2.dp, if (isSelected) Color(0xFF7158E2) else Color.Transparent, CircleShape)
                                .clickable {
                                    localSelectedAvatar = avatar
                                    avatarViewModel?.selectAvatar(avatar)
                                }
                                .bounceClick(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar, fontSize = 28.sp)
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetAvatars.drop(4).forEach { avatar ->
                        val isSelected = localSelectedAvatar == avatar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(if (isSelected) Color(0xFFF0EDFF) else Color(0xFFF3F4F6), CircleShape)
                                .border(2.dp, if (isSelected) Color(0xFF7158E2) else Color.Transparent, CircleShape)
                                .clickable {
                                    localSelectedAvatar = avatar
                                    avatarViewModel?.selectAvatar(avatar)
                                }
                                .bounceClick(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar, fontSize = 28.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Save & Continue Action Button
        Button(
            onClick = {
                if (avatarViewModel != null) {
                    avatarViewModel.saveAvatar(userId = userId, onSuccess = onContinue)
                } else {
                    onContinue()
                }
            },
            enabled = !isLoading,
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

        // Skip for now Text Button
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
fun AvatarSelectionScreenPreview() {
    AvatarSelectionScreen()
}