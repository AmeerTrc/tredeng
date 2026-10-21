package com.tredeig.app.ui.screens.market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.data.remote.dto.KlineDto
import com.tredeig.app.domain.model.MarketTicker

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
fun ChartScreen(
    symbol: String,
    exchangeName: String,
    ticker: MarketTicker?,
    klines: List<KlineDto>,
    selectedInterval: String,
    isLoading: Boolean,
    errorMessage: String?,
    onIntervalSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit = {}
) {
    val intervals = listOf("1m", "5m", "15m", "30m", "1h", "4h", "1d", "1w", "1M")

    // AI Strategy & Risk Parameters
    var aiLevel by remember { mutableStateOf("MEDIUM") } // "LOW", "MEDIUM", "HIGH"
    var tradeAmountText by remember { mutableStateOf("100") }
    var takeProfitText by remember { mutableStateOf("4.0") }
    var stopLossText by remember { mutableStateOf("2.0") }
    var isAiActive by remember { mutableStateOf(true) }
    var saveSuccessMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${symbol.take(3)} / ${symbol.drop(3)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = BrandPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = exchangeName.uppercase(),
                                color = BrandPurpleLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Toggle favorite */ }) {
                        Icon(Icons.Default.StarOutline, contentDescription = "Favorite", tint = BrandPurpleLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgMain)
            )
        },
        containerColor = BgMain
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price & 24h Stats Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val price = ticker?.price ?: 67432.10
                    Text(
                        text = "$${String.format("%,.2f", price)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    val pct = ticker?.priceChangePercent ?: 2.45
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (pct >= 0) Icons.Default.NorthEast else Icons.Default.SouthEast,
                            contentDescription = null,
                            tint = if (pct >= 0) BullishGreen else BearishRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (pct >= 0) "+" else ""}${String.format("%.2f", pct)}% (24h)",
                            color = if (pct >= 0) BullishGreen else BearishRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                ticker?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("أعلى 24h: $${String.format("%,.2f", it.high24h)}", color = TextSecondary, fontSize = 11.sp)
                        Text("أدنى 24h: $${String.format("%,.2f", it.low24h)}", color = TextSecondary, fontSize = 11.sp)
                        Text("حجم التداول: ${String.format("%.2fM", it.volume24h / 1000000.0)}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Timeframe Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                intervals.forEach { interval ->
                    Surface(
                        color = if (selectedInterval == interval) BrandPurple else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onIntervalSelected(interval) }
                    ) {
                        Text(
                            text = interval,
                            color = if (selectedInterval == interval) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (selectedInterval == interval) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Real Candlestick Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(CardBg, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandPurple
                    )
                } else if (errorMessage != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = errorMessage, color = BearishRed, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRefresh) { Text("إعادة التحميل") }
                    }
                } else if (klines.isEmpty()) {
                    Text("جاري جلب بيانات الشموع اليابانية...", color = TextSecondary, modifier = Modifier.align(Alignment.Center))
                } else {
                    CandlestickCanvas(klines = klines)
                }
            }

            // AI Trading Controls Card (لوحة تشغيل الصندوق والذكاء الاصطناعي)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = BrandPurpleLight, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("إعدادات صندوق الذكاء الاصطناعي", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(if (isAiActive) BullishGreen else BearishRed, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isAiActive) "الصندوق نشط - التداول الذكي مفعل" else "الصندوق متوقف حالياً", color = if (isAiActive) BullishGreen else BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Switch(
                            checked = isAiActive,
                            onCheckedChange = { isAiActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandPurple)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // 3 AI Experience Levels Selector Chips (منخفض / متوسط / عالي)
                    Text("مستوى خبرة ومخاطرة الذكاء الاصطناعي:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = aiLevel == "LOW",
                            onClick = { aiLevel = "LOW" },
                            label = { Text("منخفض (متحفظ)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = aiLevel == "MEDIUM",
                            onClick = { aiLevel = "MEDIUM" },
                            label = { Text("متوسط (متوازن)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = aiLevel == "HIGH",
                            onClick = { aiLevel = "HIGH" },
                            label = { Text("عالي (مغامرة)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Risk & Entry Parameters (نسبة الأخذ والخسارة ومبلغ الدخول)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = takeProfitText,
                            onValueChange = { takeProfitText = it },
                            label = { Text("جني الأرباح %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BullishGreen, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )

                        OutlinedTextField(
                            value = stopLossText,
                            onValueChange = { stopLossText = it },
                            label = { Text("وقف الخسارة %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BearishRed, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    OutlinedTextField(
                        value = tradeAmountText,
                        onValueChange = { tradeAmountText = it },
                        label = { Text("مبلغ دخول الصفقة (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple, unfocusedBorderColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )

                    saveSuccessMsg?.let { msg ->
                        Text(msg, color = BullishGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            saveSuccessMsg = "تم حفظ وتفعيل استراتيجية الذكاء الاصطناعي لـ $symbol بنجاح!"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                    ) {
                        Text("تطبيق وتفعيل استراتيجية الصندوق", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CandlestickCanvas(klines: List<KlineDto>) {
    val minPrice = klines.minOfOrNull { it.low } ?: 0.0
    val maxPrice = klines.maxOfOrNull { it.high } ?: 1.0
    val maxVolume = klines.maxOfOrNull { it.volume } ?: 1.0
    val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val candleCount = klines.size
        val candleWidth = width / candleCount.coerceAtLeast(1)

        val chartHeight = height * 0.75f
        val volumeHeight = height * 0.20f

        klines.forEachIndexed { index, kline ->
            val x = index * candleWidth + (candleWidth / 2f)

            val openY = chartHeight - ((kline.open - minPrice) / priceRange * chartHeight).toFloat()
            val closeY = chartHeight - ((kline.close - minPrice) / priceRange * chartHeight).toFloat()
            val highY = chartHeight - ((kline.high - minPrice) / priceRange * chartHeight).toFloat()
            val lowY = chartHeight - ((kline.low - minPrice) / priceRange * chartHeight).toFloat()

            val isGreen = kline.close >= kline.open
            val color = if (isGreen) BullishGreen else BearishRed

            // Draw Wick
            drawLine(
                color = color,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 2f
            )

            // Draw Body
            val bodyTop = minOf(openY, closeY)
            val bodyHeight = maxOf(Math.abs(closeY - openY), 3f)
            val bodyWidth = (candleWidth * 0.7f).coerceAtLeast(2f)

            drawRect(
                color = color,
                topLeft = Offset(x - (bodyWidth / 2f), bodyTop),
                size = Size(bodyWidth, bodyHeight)
            )

            // Draw Volume Bar
            val volHeight = ((kline.volume / maxVolume) * volumeHeight).toFloat()
            drawRect(
                color = color.copy(alpha = 0.4f),
                topLeft = Offset(x - (bodyWidth / 2f), height - volHeight),
                size = Size(bodyWidth, volHeight)
            )
        }
    }
}
