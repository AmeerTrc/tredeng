package com.tredeig.app.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.domain.model.PortfolioSummary

val DarkBg = Color(0xFF0F172A)
val CardBg = Color(0xFF1E293B)
val AccentBlue = Color(0xFF3B82F6)
val BullishGreen = Color(0xFF10B981)

@Composable
fun PortfolioScreen(
    portfolioSummary: PortfolioSummary?,
    isPaperTrading: Boolean,
    paperCapitalUsdt: Double,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = "المحفظة والأصول",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Type Chip Indicator
        Surface(
            color = if (isPaperTrading) Color(0xFF854D0E) else Color(0xFF065F46),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = if (isPaperTrading) "وضع التداول التجريبي (Paper Trading)" else "وضع الحساب الحقيقي (Real Exchange)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Balance Overview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isPaperTrading) "رأس المال التجريبي المتاح" else "إجمالي رصيد المنصة الحقيقي",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                val displayTotal = if (isPaperTrading) paperCapitalUsdt else (portfolioSummary?.totalBalanceUsdt ?: 0.0)
                Text(
                    text = "$${String.format("%.2f", displayTotal)} USDT",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "المتاح للتداول", fontSize = 12.sp, color = Color.Gray)
                        val displayAvail = if (isPaperTrading) paperCapitalUsdt else (portfolioSummary?.availableBalanceUsdt ?: 0.0)
                        Text(
                            text = "$${String.format("%.2f", displayAvail)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BullishGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "المحجوز في الصفقات", fontSize = 12.sp, color = Color.Gray)
                        val displayInOrders = if (isPaperTrading) 0.0 else (portfolioSummary?.inOrdersUsdt ?: 0.0)
                        Text(
                            text = "$${String.format("%.2f", displayInOrders)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Assets Breakdown Section
        Text(
            text = "توزيع الأصول بالمنصة",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text("تحديث البيانات") }
                }
            }
        } else if (portfolioSummary == null || portfolioSummary.balances.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "لا توجد أصول بالمنصة حالياً أو أنك في وضع Paper Trading",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(portfolioSummary.balances) { asset ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = asset.asset,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%.4f", asset.total),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "متاح: ${String.format("%.4f", asset.free)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
