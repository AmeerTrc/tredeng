package com.tredeig.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.ui.screens.market.CandlestickCanvas
import kotlinx.coroutines.launch

// Professional Deep Purple Identity Theme Colors
val DeepPurpleDark = Color(0xFF0F0C20)
val DarkCanvasBg = Color(0xFF070510)
val CardPurpleBg = Color(0xFF181330)
val PrimaryPurple = Color(0xFF8B5CF6)
val BrightPurple = Color(0xFFA78BFA)
val BullishGreen = Color(0xFF10B981)
val BearishRed = Color(0xFFEF4444)

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardPurpleBg,
                drawerContentColor = Color.White,
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryPurple,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tredeng AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("نظام التداول الآلي الحقيقي", color = BrightPurple, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                NavigationDrawerItem(
                    label = { Text("🏠 الرئيسية (الرصيد والحركة)", color = Color.White) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📊 الأسواق والعملات", color = Color.White) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToMarket() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📈 الصفقات المفتوحة", color = Color.White) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToPositions() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📜 سجل التداول", color = Color.White) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToHistory() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("🔑 ربط المنصات والـ API", color = Color.White) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToExchange() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("🛡️ إدارة المخاطر ورأس المال", color = Color.White) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToRisk() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("🚨 زر الطوارئ (Kill Switch)", color = BearishRed) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToEmergency() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = PrimaryPurple,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tredeng",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToAI() }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = BrightPurple)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCanvasBg)
                )
            },
            containerColor = DarkCanvasBg
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // 1. Real Balance Card Section (الرصيد الحقيقي)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardPurpleBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = PrimaryPurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = state.selectedExchange.uppercase(),
                                        color = BrightPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الرصيد المتاح (USDT)",
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.loadDashboard() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Balance", tint = BrightPurple)
                            }
                        }

                        val availableBalance = state.portfolioSummary?.availableBalanceUsdt
                        val totalBalance = state.portfolioSummary?.totalBalanceUsdt

                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = PrimaryPurple,
                                modifier = Modifier.size(28.dp).padding(vertical = 4.dp)
                            )
                        } else if (availableBalance != null) {
                            Text(
                                text = "$${String.format("%.2f", availableBalance)} USDT",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "الإجمالي بالمنصة: $${String.format("%.2f", totalBalance ?: availableBalance)} USDT",
                                color = BullishGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Column {
                                Text(
                                    text = "اربط منصة التداول لعرض رصيدك",
                                    color = Color.Yellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(onClick = onNavigateToExchange) {
                                    Text("اضغط هنا لربط API المنصة", color = BrightPurple, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 2. Candlestick Kline Chart Section (مخطط الشموع اليابانية)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardPurpleBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${state.activeSymbol} (${state.selectedExchange.uppercase()})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                state.ticker?.let { t ->
                                    Text(
                                        text = "$${String.format("%.2f", t.price)}",
                                        color = if (t.priceChangePercent >= 0) BullishGreen else BearishRed,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            state.ticker?.let { t ->
                                Surface(
                                    color = if (t.priceChangePercent >= 0) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${if (t.priceChangePercent >= 0) "+" else ""}${String.format("%.2f", t.priceChangePercent)}%",
                                        color = if (t.priceChangePercent >= 0) BullishGreen else BearishRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Timeframe interval selector chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("1m", "5m", "15m", "1h", "4h", "1d").forEach { interval ->
                                FilterChip(
                                    selected = state.selectedInterval == interval,
                                    onClick = { viewModel.setInterval(interval) },
                                    label = { Text(interval, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryPurple,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkCanvasBg,
                                        labelColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        // Real Candlestick Chart Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(DarkCanvasBg, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            if (state.isChartLoading) {
                                CircularProgressIndicator(
                                    color = PrimaryPurple,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else if (state.klines.isNotEmpty()) {
                                CandlestickCanvas(klines = state.klines)
                            } else {
                                Text(
                                    text = "جاري جلب شموع ${state.activeSymbol} من ${state.selectedExchange.uppercase()}...",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                // 3. AI System Status Card (حالة الذكاء الاصطناعي)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardPurpleBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(BullishGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حالة الذكاء الاصطناعي (Gemini AI)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Text(
                                text = state.aiLastAnalysisTime,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        Text(
                            text = state.aiStatusText,
                            color = BrightPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Text(
                            text = state.aiSignalSummary,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
