package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.AppLogo
import com.simats.focusflow.ui.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(onBack: () -> Unit = {}, onContinue: () -> Unit = {}, onLogin: () -> Unit = {}) {
    var phone by remember { mutableStateOf("+91  98765 43210") }
    var agreed by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("Create your account", "A FOCUSED START", onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color(0xFF1E1E24), RoundedCornerShape(2.dp)))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo(size = 32.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("FocusFlow AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Start with your mobile number. Make room for better focus.", fontSize = 16.sp, color = Color(0xFF6B7280), lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Phone number", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF7158E2))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Sample number · no OTP will be sent.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { agreed = !agreed }) {
            Checkbox(checked = agreed, onCheckedChange = { agreed = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
            Text("I agree to the Terms & Privacy Policy", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Already have an account? Log in", color = Color(0xFF7158E2), fontSize = 16.sp, modifier = Modifier.clickable { onLogin() })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Demo only. No account is created and no message is sent.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
        }
    }
}