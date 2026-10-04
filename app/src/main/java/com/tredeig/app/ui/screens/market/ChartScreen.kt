package com.tredeig.app.ui.screens.market

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
fun ChartScreen(onBack: () -> Unit) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("الرسم البياني والمؤشرات (Chart)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text("مؤشرات تحليلات السعر اللحظية:", color = TextSecondary)
            Text("• EMA (9, 21, 50, 200)\n• RSI (14)\n• MACD (12, 26, 9)\n• Bollinger Bands\n• ATR", color = TextPrimary)

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
