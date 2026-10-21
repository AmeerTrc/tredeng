package com.tredeig.app.ui.screens.market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.data.remote.dto.KlineDto
import com.tredeig.app.domain.model.MarketTicker

val DarkBg = Color(0xFF0F172A)
val CardBg = Color(0xFF1E293B)
val BullishGreen = Color(0xFF10B981)
val BearishRed = Color(0xFFEF4444)

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
    onRefresh: () -> Unit
) {
    val intervals = listOf("1m", "5m", "15m", "30m", "1h", "4h", "1d", "1w")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Header Ticker Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$symbol (${exchangeName.uppercase()})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                ticker?.let {
                    Text(
                        text = "$${String.format("%.2f", it.price)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (it.priceChangePercent >= 0) BullishGreen else BearishRed
                    )
                }
            }

            ticker?.let {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (it.priceChangePercent >= 0) "+" else ""}${String.format("%.2f", it.priceChangePercent)}%",
                        color = if (it.priceChangePercent >= 0) BullishGreen else BearishRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أعلى 24h: $${String.format("%.2f", it.high24h)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "أدنى 24h: $${String.format("%.2f", it.low24h)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeframe Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            intervals.forEach { interval ->
                FilterChip(
                    selected = selectedInterval == interval,
                    onClick = { onIntervalSelected(interval) },
                    label = { Text(interval, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = CardBg,
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Candlestick Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CardBg, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3B82F6)
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorMessage, color = BearishRed, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRefresh) {
                        Text("إعادة المحاولة")
                    }
                }
            } else if (klines.isEmpty()) {
                Text(
                    text = "لا تتوفر بيانات شموع حالياً",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                CandlestickCanvas(klines = klines)
            }
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
        val volumeTop = height * 0.80f

        klines.forEachIndexed { index, kline ->
            val x = index * candleWidth + (candleWidth / 2f)

            // Price Scaling Y
            val openY = chartHeight - ((kline.open - minPrice) / priceRange * chartHeight).toFloat()
            val closeY = chartHeight - ((kline.close - minPrice) / priceRange * chartHeight).toFloat()
            val highY = chartHeight - ((kline.high - minPrice) / priceRange * chartHeight).toFloat()
            val lowY = chartHeight - ((kline.low - minPrice) / priceRange * chartHeight).toFloat()

            val isGreen = kline.close >= kline.open
            val color = if (isGreen) BullishGreen else BearishRed

            // Draw Wick (Line from High to Low)
            drawLine(
                color = color,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 2f
            )

            // Draw Body (Rect from Open to Close)
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
