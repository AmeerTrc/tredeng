package com.tredeig.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExchange: () -> Unit,
    onNavigateToEmergency: () -> Unit
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
            // Header Warning Banner
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
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow)
                    Text(
                        text = "المرحلة الأولى: عرض البيانات والحساب فقط. لن يتم فتح أو تنفيذ أي صفقات حقيقية.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // System Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("حالة المنظومة والاتصال", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(label = "API Status", isOk = state.isApiConnected)
                        StatusChip(label = "WebSocket", isOk = state.isWsConnected)
                        StatusChip(
                            label = if (state.isKillSwitchActive) "EMERGENCY STOP" else "STOPPED (Phase 1)",
                            isOk = !state.isKillSwitchActive,
                            activeColor = WarningYellow
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onNavigateToExchange,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text("ربط API (Binance/Exness)", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToEmergency,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                        ) {
                            Text("زر الطوارئ Kill Switch", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
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
                        text = "$${String.format("%.2f", state.portfolio?.totalBalanceUsdt ?: 0.0)}",
                        color = PrimaryGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Divider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد المتاح", color = TextSecondary, fontSize = 12.sp)
                            Text("$${String.format("%.2f", state.portfolio?.availableBalanceUsdt ?: 0.0)}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("في الأوامر المعلقة", color = TextSecondary, fontSize = 12.sp)
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
                        Text("سعر التداول اللحظي", color = TextSecondary, fontSize = 12.sp)
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

            // Trading Activity & Stats Summary
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("إحصائيات اليوم وصفقات الـ Spot", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عدد الصفقات اليوم:", color = TextSecondary)
                        Text("${state.dailyTradesCount} / 3 (الحد الأقصى)", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الصفقات المفتوحة حالياً:", color = TextSecondary)
                        Text("لا يوجد (الصفقات معطلة في المرحلة 1)", color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("العمولة والانزلاق السعري:", color = TextSecondary)
                        Text("0.1% Spot Fee", color = TextPrimary)
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
