package com.tredeig.app.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@Composable
fun EmergencyStopScreen(
    onToggleKillSwitch: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var isEmergencyActive by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("زر إيقاف التداول الكلي (Kill Switch)", color = AccentRed, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "عند تفعيل الإيقاف الطارئ، سيتم منع فتح أي صفقات جديدة فوراً من الخادم وتجميد كل الأوامر القادمة من الذكاء الاصطناعي.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isEmergencyActive = true
                    onToggleKillSwitch(true)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("تفعيل إيقاف الطوارئ الكلي (EMERGENCY STOP)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = {
                    isEmergencyActive = false
                    onToggleKillSwitch(false)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGold)
            ) {
                Text("إلغاء التجميد وإعادة الوضع العادي", color = PrimaryGold, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBack) {
                Text("العودة", color = TextSecondary)
            }
        }
    }
}
