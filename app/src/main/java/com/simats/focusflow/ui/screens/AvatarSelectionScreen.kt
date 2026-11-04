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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
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
import com.simats.focusflow.ui.theme.FocusFlowTheme
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
    val colors = FocusFlowTheme.colors
    var localSelectedAvatar by remember { mutableStateOf("avatar:person") }
    var isLoading by remember { mutableStateOf(false) }

    if (avatarViewModel != null) {
        val vmAvatar by avatarViewModel.selectedAvatar.collectAsState()
        val uiState by avatarViewModel.uiState.collectAsState()
        localSelectedAvatar = vmAvatar
        isLoading = uiState is AvatarSelectionUiState.Loading
    }

    val presetAvatars = listOf("avatar:person", "avatar:school", "avatar:code", "avatar:palette", "avatar:rocket", "avatar:bolt", "avatar:brain", "avatar:coffee")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Choose Your Avatar", null, onBack)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Pick an avatar or upload a picture for your workspace profile.",
            fontSize = 14.sp,
            color = colors.textMuted,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Large Circular Preview
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(colors.primary.copy(alpha = 0.15f), CircleShape)
                    .border(3.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    resolveAvatarIcon(localSelectedAvatar),
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Two Action Buttons: Take Photo & Choose Gallery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    localSelectedAvatar = "avatar:camera"
                    avatarViewModel?.selectAvatar("avatar:camera")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryLight)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Take Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            OutlinedButton(
                onClick = {
                    localSelectedAvatar = "avatar:gallery"
                    avatarViewModel?.selectAvatar("avatar:gallery")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryLight)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Choose Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Or choose a preset avatar:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Preset Avatar Icons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                                .background(
                                    if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.background,
                                    CircleShape
                                )
                                .border(2.dp, if (isSelected) colors.primary else Color.Transparent, CircleShape)
                                .clickable {
                                    localSelectedAvatar = avatar
                                    avatarViewModel?.selectAvatar(avatar)
                                }
                                .bounceClick(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                resolveAvatarIcon(avatar),
                                null,
                                tint = if (isSelected) colors.primaryLight else colors.textMuted,
                                modifier = Modifier.size(26.dp)
                            )
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
                                .background(
                                    if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.background,
                                    CircleShape
                                )
                                .border(2.dp, if (isSelected) colors.primary else Color.Transparent, CircleShape)
                                .clickable {
                                    localSelectedAvatar = avatar
                                    avatarViewModel?.selectAvatar(avatar)
                                }
                                .bounceClick(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                resolveAvatarIcon(avatar),
                                null,
                                tint = if (isSelected) colors.primaryLight else colors.textMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

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
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Save & Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Skip for now Text Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Skip for now",
                color = colors.primaryLight,
                fontSize = 14.sp,
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
