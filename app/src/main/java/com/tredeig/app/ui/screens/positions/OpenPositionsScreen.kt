package com.tredeig.app.ui.screens.positions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*
import com.tredeig.app.data.remote.dto.PaperTradeDto
import java.util.Locale

@Composable
fun OpenPositionsScreen(viewModel: PositionsViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الصفقات المفتوحة (Open Positions)", color = PrimaryGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { viewModel.loadData() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("تحديث", color = PrimaryGold, fontSize = 12.sp)
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryGold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = AccentRed, fontSize = 12.sp)
            }

            if (uiState.successMessage != null) {
                Text(uiState.successMessage!!, color = AccentGreen, fontSize = 12.sp)
            }

            if (uiState.openPositions.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("لا توجد صفقات مفتوحة حالياً.", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.openPositions) { trade ->
                        OpenPositionCard(trade = trade, onClose = { viewModel.closePositionManually(trade.id) })
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("العودة", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OpenPositionCard(trade: PaperTradeDto, onClose: () -> Unit) {
    val livePrice = trade.currentPrice ?: trade.price
    val isProfit = trade.pnl >= 0
    val pnlColor = if (isProfit) AccentGreen else AccentRed
    val pnlPct = trade.pnlPct ?: (((livePrice - trade.price) / trade.price) * 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(trade.symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = CardBackground,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(trade.exchangeName.uppercase(), color = PrimaryGold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Text(
                text = String.format(Locale.US, "%+.2f USDT (%+.2f%%)", trade.pnl, pnlPct),
                color = pnlColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        HorizontalDivider(color = CardBackground, thickness = 1.dp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("سعر الدخول: $${String.format(Locale.US, "%.2f", trade.price)}", color = TextSecondary, fontSize = 12.sp)
                Text("السعر الحالي: $${String.format(Locale.US, "%.2f", livePrice)}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("حجم الصفقة: $${String.format(Locale.US, "%.2f", trade.amount)}", color = TextSecondary, fontSize = 12.sp)
                Text("نوع الأمر: ${trade.action}", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Stop Loss: $${String.format(Locale.US, "%.2f", trade.stopLoss)}", color = AccentRed, fontSize = 11.sp)
            Text("Take Profit: $${String.format(Locale.US, "%.2f", trade.takeProfit)}", color = AccentGreen, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            modifier = Modifier.fillMaxWidth().height(36.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("إغلاق الصفقة يدوياً", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
