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
import com.tredeig.app.data.remote.dto.RiskSettingsDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskSettingsScreen(viewModel: RiskViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    var maxTradeRiskPct by remember(uiState.settings.maxTradeRiskPct) { mutableStateOf(uiState.settings.maxTradeRiskPct.toString()) }
    var maxDailyLossPct by remember(uiState.settings.maxDailyLossPct) { mutableStateOf(uiState.settings.maxDailyLossPct.toString()) }
    var minConfidence by remember(uiState.settings.minConfidence) { mutableStateOf(uiState.settings.minConfidence.toString()) }
    var maxDailyTrades by remember(uiState.settings.maxDailyTrades) { mutableStateOf(uiState.settings.maxDailyTrades.toString()) }
    var stopLossRequired by remember(uiState.settings.stopLossRequired) { mutableStateOf(uiState.settings.stopLossRequired) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("قواعد وإدارة المخاطر الصارمة", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text("هذه القواعد مفروضة برمجياً من الخادم الخفي ولا يمكن تجاوزها:", color = TextSecondary, fontSize = 13.sp)

            OutlinedTextField(
                value = maxTradeRiskPct,
                onValueChange = { maxTradeRiskPct = it },
                label = { Text("الحد الأقصى للمخاطرة بالصفقة (%)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            OutlinedTextField(
                value = maxDailyLossPct,
                onValueChange = { maxDailyLossPct = it },
                label = { Text("الحد الأقصى للخسارة اليومية (%)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            OutlinedTextField(
                value = maxDailyTrades,
                onValueChange = { maxDailyTrades = it },
                label = { Text("الحد الأقصى لعدد الصفقات اليومية", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = PrimaryGold, cursorColor = PrimaryGold)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = stopLossRequired, onCheckedChange = { stopLossRequired = it }, colors = CheckboxDefaults.colors(checkedColor = PrimaryGold))
                Text("إلغاء الصفقة عند عدم وجود Stop Loss", color = TextPrimary, fontSize = 14.sp)
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryGold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (uiState.successMessage != null) {
                Text(uiState.successMessage!!, color = AccentGreen, fontSize = 14.sp)
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = AccentRed, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveSettings(RiskSettingsDto(
                        maxTradeRiskPct = maxTradeRiskPct.toDoubleOrNull() ?: 0.25,
                        maxDailyLossPct = maxDailyLossPct.toDoubleOrNull() ?: 1.0,
                        minConfidence = minConfidence.toDoubleOrNull() ?: 0.70,
                        maxDailyTrades = maxDailyTrades.toIntOrNull() ?: 3,
                        stopLossRequired = stopLossRequired
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ الإعدادات", color = DarkBackground, fontWeight = FontWeight.Bold)
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
