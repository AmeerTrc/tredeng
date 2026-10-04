package com.tredeig.app.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@Composable
fun AIAssistantScreen(onBack: () -> Unit) {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("مساعد الذكاء الاصطناعي (AI Trading Assistant)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text("يعمل نموذج AI على تحليل مؤشرات الفني وتقديم توصيات بالصيغة:", color = TextSecondary, fontSize = 13.sp)

            Card(colors = CardDefaults.cardColors(containerColor = DarkBackground), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "{\n  \"action\": \"HOLD\",\n  \"confidence\": 0.85,\n  \"reason\": \"الماركت في مرحلة تذبذب عرضي، انتهاء تصحيح RSI.\"\n}",
                    color = PrimaryGold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
            ) {
                Text("العودة", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
