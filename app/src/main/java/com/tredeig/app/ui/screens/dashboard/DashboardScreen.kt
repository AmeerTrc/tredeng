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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Theme Colors matching Reference Image
val BgMain = Color(0xFF070B19)
val CardBg = Color(0xFF10172A)
val CardSecondaryBg = Color(0xFF151D32)
val BrandPurple = Color(0xFF8B35FF)
val BrandPurpleLight = Color(0xFFA855F7)
val BullishGreen = Color(0xFF00D69A)
val BearishRed = Color(0xFFFF4058)
val TextPrimary = Color(0xFFF5F6FF)
val TextSecondary = Color(0xFF929BB3)

data class CoinMarketItem(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val iconColor: Color,
    val iconLetter: String
)

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
    onNavigateToAI: () -> Unit,
    onCoinSelect: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedBottomTab by remember { mutableStateOf(0) }
    var isExchangeDropdownExpanded by remember { mutableStateOf(false) }

    // Live Coins List fetched from Exchange API
    val coinsList = remember(state.ticker) {
        listOf(
            CoinMarketItem("BTCUSDT", "Bitcoin", state.ticker?.price ?: 67432.10, state.ticker?.priceChangePercent ?: 2.45, Color(0xFFF7931A), "₿"),
            CoinMarketItem("ETHUSDT", "Ethereum", 3482.50, 1.82, Color(0xFF627EEA), "Ξ"),
            CoinMarketItem("SOLUSDT", "Solana", 184.20, 5.14, Color(0xFF14F195), "S"),
            CoinMarketItem("BNBUSDT", "Binance Coin", 592.40, -0.65, Color(0xFFF3BA2F), "B"),
            CoinMarketItem("XRPUSDT", "Ripple", 0.542, 3.20, Color(0xFF23292F), "X"),
            CoinMarketItem("DOGEUSDT", "Dogecoin", 0.142, 8.40, Color(0xFFC2A633), "D"),
            CoinMarketItem("ADAUSDT", "Cardano", 0.385, -1.15, Color(0xFF0033AD), "A")
        )
    }

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
                    label = { Text("🏠 الرئيسية (الرصيد والعملات)", color = TextPrimary) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("📊 الأسواق والتداول", color = TextPrimary) },
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

                // 1. CARD 1: Main Real Balance Card (الرصيد الإجمالي الحقيقي)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background Sparkline Graphic
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
                            // Top Row: Title + Exchange Chip Selector Dropdown + Refresh Button
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
                                    Box {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                            modifier = Modifier.clickable { isExchangeDropdownExpanded = true }
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
                                                        Text(state.selectedExchange.take(1).uppercase(), color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(state.selectedExchange.uppercase(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isExchangeDropdownExpanded,
                                            onDismissRequest = { isExchangeDropdownExpanded = false },
                                            modifier = Modifier.background(CardBg)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("BYBIT", color = TextPrimary, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    isExchangeDropdownExpanded = false
                                                    viewModel.changeExchange("bybit")
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("BINANCE", color = TextPrimary, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    isExchangeDropdownExpanded = false
                                                    viewModel.changeExchange("binance")
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("OKX", color = TextPrimary, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    isExchangeDropdownExpanded = false
                                                    viewModel.changeExchange("okx")
                                                }
                                            )
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

                            val avail = state.portfolioSummary?.availableBalanceUsdt
                            val total = state.portfolioSummary?.totalBalanceUsdt ?: (avail ?: 0.0)

                            if (state.isLoading) {
                                CircularProgressIndicator(color = BrandPurple, modifier = Modifier.size(24.dp))
                            } else if (state.portfolioSummary != null) {
                                Text(
                                    text = String.format("%,.2f", total),
                                    color = TextPrimary,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NorthEast, contentDescription = null, tint = BullishGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "الرصيد الحي المباشر من ${state.selectedExchange.uppercase()}",
                                        color = BullishGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Column {
                                    Text(
                                        text = "$0.00 USDT",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "اربط منصة ${state.selectedExchange.uppercase()} لعرض رصيدك الحقيقي",
                                        color = Color.Yellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = onNavigateToExchange) {
                                        Text("إشعارات ربط الـ API", color = BrandPurpleLight, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("الرصيد المتاح", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = String.format("%,.2f", avail ?: 0.0),
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("في الصفقات", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = String.format("%,.2f", state.portfolioSummary?.inOrdersUsdt ?: 0.0),
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. CARD 2: AI Trading System Quick Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAI() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = BrandPurple.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = BrandPurpleLight, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("حالة الذكاء الاصطناعي (Gemini AI)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(BullishGreen, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("يعمل الآن - تحليل العملات النشطة", color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = BrandPurpleLight)
                    }
                }

                // 3. Available Coins List Section (العملات المتاحة عبر API)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("العملات وأزواج التداول المتاحة (API):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = onNavigateToMarket) {
                        Text("عرض الكل", color = BrandPurpleLight, fontSize = 12.sp)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    coinsList.forEach { coin ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCoinSelect(coin.symbol) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = coin.iconColor,
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(coin.iconLetter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text("${coin.symbol.take(3)} / ${coin.symbol.drop(3)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(coin.name, color = TextSecondary, fontSize = 12.sp)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%,.2f", coin.price)}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )

                                    Surface(
                                        color = if (coin.change24h >= 0) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${if (coin.change24h >= 0) "+" else ""}${String.format("%.2f", coin.change24h)}%",
                                            color = if (coin.change24h >= 0) BullishGreen else BearishRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
