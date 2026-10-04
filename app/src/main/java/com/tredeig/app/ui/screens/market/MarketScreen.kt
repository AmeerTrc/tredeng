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
fun MarketScreen(onBack: () -> Unit) {
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
            Text("أسواق التداول (Spot / Gold)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            MarketItem("BTC / USDT", "$65,420.00", "+2.45%")
            MarketItem("XAU / USD (Gold - Exness)", "$2,648.50", "+0.85%")

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

@Composable
fun MarketItem(symbol: String, price: String, change: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, color = TextPrimary, fontWeight = FontWeight.Bold)
        Column(horizontalAlignment = Alignment.End) {
            Text(price, color = PrimaryGold, fontWeight = FontWeight.Bold)
            Text(change, color = AccentGreen, fontSize = 12.sp)
        }
    }
}
