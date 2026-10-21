package com.tredeig.app.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.data.remote.dto.KlineDto
import com.tredeig.app.ui.screens.market.CandlestickCanvas
import kotlinx.coroutines.launch

// Exact Theme Colors matching Reference Image
val BgMain = Color(0xFF070B19)
val CardBg = Color(0xFF10172A)
val CardSecondaryBg = Color(0xFF151D32)
val BrandPurple = Color(0xFF8B35FF)
val BrandPurpleLight = Color(0xFFA855F7)
val BullishGreen = Color(0xFF00D69A)
val BearishRed = Color(0xFFFF4058)
val TextPrimary = Color(0xFFF5F6FF)
val TextSecondary = Color(0xFF929BB3)

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
    var selectedBottomTab by remember { mutableStateOf(0) } // 0: الرئيسية, 1: الأسواق, 2: التداول, 3: الصفقات, 4: المزيد

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardBg,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = BrandPurple,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tredeng", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("منصة التداول الذكية", color = BrandPurpleLight, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                NavigationDrawerItem(
                    label = { Text("🏠 الرئيسية", color = TextPrimary) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📊 الأسواق والعملات", color = TextPrimary) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToMarket() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📈 الصفقات المفتوحة", color = TextPrimary) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToPositions() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📜 سجل التداول", color = TextPrimary) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToHistory() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("🔑 ربط المنصات و API", color = TextPrimary) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToExchange() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("🛡️ إدارة المخاطر ورأس المال", color = TextPrimary) },
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
                // Header Top Bar: Menu Button + Tredeng Logo + Notifications Bell
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = BrandPurple,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tredeng",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToAI() }) {
                            Box {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(BrandPurple, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BgMain)
                )
            },
            bottomBar = {
                // Bottom Navigation Bar 1:1 Match with Image
                NavigationBar(
                    containerColor = CardBg,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedBottomTab == 0,
                        onClick = { selectedBottomTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BrandPurple.copy(alpha = 0.15f)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedBottomTab == 1,
                        onClick = { selectedBottomTab = 1; onNavigateToMarket() },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "الأسواق") },
                        label = { Text("الأسواق", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BrandPurple.copy(alpha = 0.15f)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedBottomTab == 2,
                        onClick = { selectedBottomTab = 2; onNavigateToMarket() },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "التداول") },
                        label = { Text("التداول", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BrandPurple.copy(alpha = 0.15f)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedBottomTab == 3,
                        onClick = { selectedBottomTab = 3; onNavigateToPositions() },
                        icon = { Icon(Icons.Default.Description, contentDescription = "الصفقات") },
                        label = { Text("الصفقات", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BrandPurple.copy(alpha = 0.15f)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedBottomTab == 4,
                        onClick = { selectedBottomTab = 4; scope.launch { drawerState.open() } },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "المزيد") },
                        label = { Text("المزيد", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPurple,
                            selectedTextColor = BrandPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BrandPurple.copy(alpha = 0.15f)
                        )
                    )
                }
            },
            containerColor = BgMain
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // 1. CARD 1: Main Balance Card (الرصيد الإجمالي)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background Sparkline Decorative Chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            val path = Path().apply {
                                moveTo(size.width * 0.4f, size.height * 0.7f)
                                cubicTo(
                                    size.width * 0.55f, size.height * 0.9f,
                                    size.width * 0.7f, size.height * 0.3f,
                                    size.width, size.height * 0.2f
                                )
                            }
                            drawPath(
                                path = path,
                                color = BrandPurple.copy(alpha = 0.35f),
                                style = Stroke(width = 4f)
                            )
                        }

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row: Title + Exchange Chip Selector + Refresh Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("الرصيد الإجمالي (USDT)", color = TextSecondary, fontSize = 13.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                        modifier = Modifier.clickable { onNavigateToExchange() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.size(14.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("B", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(state.selectedExchange.replaceFirstChar { it.uppercase() }, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { viewModel.loadDashboard() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Big Balance Figure
                            val avail = state.portfolioSummary?.availableBalanceUsdt
                            val total = state.portfolioSummary?.totalBalanceUsdt ?: (avail ?: (state.userSettings?.totalCapitalUsdt ?: 1245.32))

                            if (state.isLoading) {
                                CircularProgressIndicator(color = BrandPurple, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = String.format("%,.2f", total),
                                    color = TextPrimary,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                // Daily Profit/Loss Indicator
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NorthEast, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(14.sp.value.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+52.14 (+4.37%) اليوم",
                                        color = BullishGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Bottom Sub-Balances Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("الرصيد المتاح", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = String.format("%,.2f", avail ?: (state.userSettings?.totalCapitalUsdt ?: 1180.50)),
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("في الصفقات", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = String.format("%,.2f", state.portfolioSummary?.inOrdersUsdt ?: 64.82),
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. CARD 2: Active Symbol Candlestick Chart (1:1 Match with Reference Image)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Symbol Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFF7931A), // BTC Orange
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("₿", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${state.activeSymbol.take(3)} / ${state.activeSymbol.drop(3)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val price = state.ticker?.price ?: 67432.10
                                val pct = state.ticker?.priceChangePercent ?: 2.45

                                Text(
                                    text = String.format("%,.2f", price),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (pct >= 0) Icons.Default.NorthEast else Icons.Default.SouthEast,
                                        contentDescription = null,
                                        tint = if (pct >= 0) BullishGreen else BearishRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "+1,608.32 (${if (pct >= 0) "+" else ""}${String.format("%.2f", pct)}%)",
                                        color = if (pct >= 0) BullishGreen else BearishRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.StarOutline, contentDescription = "Favorite", tint = BrandPurpleLight)
                            }
                        }

                        // Timeframe Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("1m", "5m", "15m", "1h", "4h", "1d", "1w", "1M").forEach { interval ->
                                Surface(
                                    color = if (state.selectedInterval == interval) BrandPurple else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setInterval(interval) }
                                ) {
                                    Text(
                                        text = interval,
                                        color = if (state.selectedInterval == interval) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (state.selectedInterval == interval) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Icon(Icons.Default.CropFree, contentDescription = "Fullscreen", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }

                        // Candlestick Chart Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(CardSecondaryBg, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            if (state.isChartLoading) {
                                CircularProgressIndicator(color = BrandPurple, modifier = Modifier.align(Alignment.Center))
                            } else if (state.klines.isNotEmpty()) {
                                CandlestickCanvas(klines = state.klines)
                            } else {
                                Text(
                                    text = "جاري تحميل الشموع اليابانية الحية...",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                // 3. CARD 3: AI Trading System Status Card (1:1 Match with Reference Image)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = BrandPurple.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = BrandPurpleLight,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text("حالة الذكاء الاصطناعي", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(BullishGreen, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("يعمل الآن", color = BullishGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تحليل: BTC", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }

                            Surface(
                                color = BrandPurple.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.clickable { onNavigateToAI() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("عرض التفاصيل", color = BrandPurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = BrandPurpleLight, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Inner Trend Prediction Box
                        Surface(
                            color = CardSecondaryBg,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("الاتجاه الحالي: ", color = TextSecondary, fontSize = 13.sp)
                                        Text("صاعد", color = BullishGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "فرصة محتملة للشراء ضمن الاتجاه العام.",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }

                                Surface(
                                    color = BullishGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BullishGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Bottom 24h Market Stats Row (حجم التداول | أدنى سعر | أعلى سعر | سعر 24 ساعة)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(label = "حجم التداول", value = "2.45B", modifier = Modifier.weight(1f))
                    StatBox(label = "أدنى سعر", value = String.format("%,.2f", state.ticker?.low24h ?: 65021.30), modifier = Modifier.weight(1f))
                    StatBox(label = "أعلى سعر", value = String.format("%,.2f", state.ticker?.high24h ?: 68120.50), modifier = Modifier.weight(1f))
                    StatBox(label = "سعر 24 ساعة", value = "+2.45%", isGreen = true, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, isGreen: Boolean = false, modifier: Modifier = Modifier) {
    Surface(
        color = CardBg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isGreen) BullishGreen else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
