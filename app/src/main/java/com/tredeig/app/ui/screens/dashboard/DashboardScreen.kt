package com.tredeig.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExchange: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToPositions: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAI: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Active Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryGold)
                    Text(
                        text = "نظام Tredeng للتداول التجريبي والآلي (Paper & AI Trading System)",
                        color = PrimaryGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Navigation Action Buttons Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("لوحة التحكم والتنقل السريع", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToRisk,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("المبالغ والمخاطر", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onNavigateToMarket,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("اختيار العملات", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToPositions,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("الصفقات المفتوحة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("سجل التداول", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToExchange,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ربط المفاتيح (APIs)", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onNavigateToEmergency,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("زر الطوارئ", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // System Connection Status
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("حالة الاتصال بالخادم والمنصات", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(label = "Railway Backend", isOk = state.isApiConnected)
                        StatusChip(label = "Gemini AI", isOk = true)
                        StatusChip(
                            label = if (state.isKillSwitchActive) "EMERGENCY STOP" else "RUNNING",
                            isOk = !state.isKillSwitchActive,
                            activeColor = AccentGreen
                        )
                    }
                }
            }

            // Portfolio Balance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("الرصيد الإجمالي (USDT)", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "$${String.format("%.2f", state.portfolio?.totalBalanceUsdt ?: 1000.0)}",
                        color = PrimaryGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد المتاح", color = TextSecondary, fontSize = 12.sp)
                            Text("$${String.format("%.2f", state.portfolio?.availableBalanceUsdt ?: 1000.0)}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("في الصفقات", color = TextSecondary, fontSize = 12.sp)
                            Text("$${String.format("%.2f", state.portfolio?.inOrdersUsdt ?: 0.0)}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("الخسارة اليومية", color = TextSecondary, fontSize = 12.sp)
                            Text("$${String.format("%.2f", state.dailyLossUsdt)}", color = AccentRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // BTC Market Live Ticker Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("زوج BTC / USDT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("سعر التداول اللحظي من Binance", color = TextSecondary, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format("%.2f", state.ticker?.price ?: 0.0)}",
                            color = PrimaryGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val change = state.ticker?.priceChangePercent ?: 0.0
                        Text(
                            text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%",
                            color = if (change >= 0) AccentGreen else AccentRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, isOk: Boolean, activeColor: Color = AccentGreen) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(BorderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isOk) activeColor else AccentRed, CircleShape)
        )
        Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
