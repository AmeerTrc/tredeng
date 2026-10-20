package com.tredeig.app.ui.screens.risk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*
import com.tredeig.app.data.remote.dto.UserSettingsDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskSettingsScreen(viewModel: RiskViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    var totalCapitalUsdt by remember(uiState.settings.totalCapitalUsdt) { mutableStateOf(uiState.settings.totalCapitalUsdt.toString()) }
    var tradeAmountUsdt by remember(uiState.settings.tradeAmountUsdt) { mutableStateOf(uiState.settings.tradeAmountUsdt.toString()) }
    var entryPrice by remember(uiState.settings.entryPrice) { mutableStateOf(uiState.settings.entryPrice?.toString() ?: "") }
    var exitPrice by remember(uiState.settings.exitPrice) { mutableStateOf(uiState.settings.exitPrice?.toString() ?: "") }
    var stopLossPricePct by remember(uiState.settings.stopLossPricePct) { mutableStateOf(uiState.settings.stopLossPricePct.toString()) }
    var takeProfitPricePct by remember(uiState.settings.takeProfitPricePct) { mutableStateOf(uiState.settings.takeProfitPricePct.toString()) }
    var maxDailyLossPct by remember(uiState.settings.maxDailyLossPct) { mutableStateOf(uiState.settings.maxDailyLossPct.toString()) }
    var maxOpenPositions by remember(uiState.settings.maxOpenPositions) { mutableStateOf(uiState.settings.maxOpenPositions.toString()) }
    var tradingMode by remember(uiState.settings.tradingMode) { mutableStateOf(uiState.settings.tradingMode) }
    var selectedExchange by remember(uiState.settings.selectedExchange) { mutableStateOf(uiState.settings.selectedExchange) }
    var isPaperTrading by remember(uiState.settings.isPaperTrading) { mutableStateOf(uiState.settings.isPaperTrading) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("إعدادات رأس المال والمبالغ والمخاطرة", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text("أدخل القيم والمبالغ يدوياً (يدعم الأرقام الصحيحة والعشرية):", color = TextSecondary, fontSize = 12.sp)

            // Capital & Trade Amounts
            OutlinedTextField(
                value = totalCapitalUsdt,
                onValueChange = { totalCapitalUsdt = it },
                label = { Text("رأس المال الإجمالي المخصص ($)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            OutlinedTextField(
                value = tradeAmountUsdt,
                onValueChange = { tradeAmountUsdt = it },
                label = { Text("مبلغ كل صفقة ($)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            // Target Entry & Exit Prices (Optional)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = entryPrice,
                    onValueChange = { entryPrice = it },
                    label = { Text("سعر الشراء الهدف ($)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
                )

                OutlinedTextField(
                    value = exitPrice,
                    onValueChange = { exitPrice = it },
                    label = { Text("سعر البيع الهدف ($)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
                )
            }

            // Stop Loss & Take Profit Percentages
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = stopLossPricePct,
                    onValueChange = { stopLossPricePct = it },
                    label = { Text("وقف الخسارة (%)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
                )

                OutlinedTextField(
                    value = takeProfitPricePct,
                    onValueChange = { takeProfitPricePct = it },
                    label = { Text("جني الأرباح (%)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
                )
            }

            // Limits
            OutlinedTextField(
                value = maxDailyLossPct,
                onValueChange = { maxDailyLossPct = it },
                label = { Text("الحد الأقصى للخسارة اليومية (%)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            OutlinedTextField(
                value = maxOpenPositions,
                onValueChange = { maxOpenPositions = it },
                label = { Text("الحد الأقصى لعدد الصفقات المفتوحة بالتوازي", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            // Mode Selector: AI Strategy vs Manual Target Prices
            Text("نمط استراتيجية التداول:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = tradingMode == "AUTOMATED_AI",
                    onClick = { tradingMode = "AUTOMATED_AI" },
                    label = { Text("ذكاء اصطناعي (Gemini)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tradingMode == "MANUAL",
                    onClick = { tradingMode = "MANUAL" },
                    label = { Text("أسعار يدوية") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Exchange Selector
            Text("المنصة المحددة:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = selectedExchange == "binance", onClick = { selectedExchange = "binance" }, label = { Text("Binance") })
                FilterChip(selected = selectedExchange == "okx", onClick = { selectedExchange = "okx" }, label = { Text("OKX") })
                FilterChip(selected = selectedExchange == "bybit", onClick = { selectedExchange = "bybit" }, label = { Text("Bybit") })
            }

            // Paper Trading Toggle (Safety Default)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Switch(checked = isPaperTrading, onCheckedChange = { isPaperTrading = it }, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGold))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("وضع التداول التجريبي (Paper Trading)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (isPaperTrading) "مفعل (بدون أموال حقيقية - آمن)" else "تداول بأموال حقيقية", color = if (isPaperTrading) AccentGreen else AccentRed, fontSize = 11.sp)
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryGold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (uiState.successMessage != null) {
                Text(uiState.successMessage!!, color = AccentGreen, fontSize = 13.sp)
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = AccentRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveSettings(
                        UserSettingsDto(
                            totalCapitalUsdt = totalCapitalUsdt.toDoubleOrNull() ?: 1000.0,
                            tradeAmountUsdt = tradeAmountUsdt.toDoubleOrNull() ?: 100.0,
                            entryPrice = entryPrice.toDoubleOrNull(),
                            exitPrice = exitPrice.toDoubleOrNull(),
                            stopLossPricePct = stopLossPricePct.toDoubleOrNull() ?: 2.0,
                            takeProfitPricePct = takeProfitPricePct.toDoubleOrNull() ?: 4.0,
                            maxDailyLossPct = maxDailyLossPct.toDoubleOrNull() ?: 1.0,
                            maxOpenPositions = maxOpenPositions.toIntOrNull() ?: 3,
                            tradingMode = tradingMode,
                            selectedExchange = selectedExchange,
                            isPaperTrading = isPaperTrading
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ الإعدادات والمبالغ", color = DarkBackground, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("العودة", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
