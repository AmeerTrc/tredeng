package com.tredeig.app.ui.screens.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.common.Resource
import com.tredeig.app.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeConnectionScreen(
    viewModel: ExchangeViewModel,
    onBack: () -> Unit
) {
    var selectedExchange by remember { mutableStateOf("binance") }
    var apiKey by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }
    var isSecretVisible by remember { mutableStateOf(false) }

    val saveState by viewModel.saveState.collectAsState()

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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("ربط منصة التداول الآمن (API Keys)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "اختر المنصة وأدخل المفتاح العام (API Key) والمفتاح السري (Secret Key) المولد من المنصة. تحذير أمان: تأكد من تفعيل تداول Spot وعدم تفعيل صلاحيات السحب (Withdrawals).",
                color = TextSecondary,
                fontSize = 12.sp
            )

            // Standard Exchange Selector Chips
            Text("اختر منصة التداول:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedExchange == "binance",
                    onClick = { selectedExchange = "binance"; viewModel.clearState() },
                    label = { Text("Binance") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedExchange == "bybit",
                    onClick = { selectedExchange = "bybit"; viewModel.clearState() },
                    label = { Text("Bybit") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedExchange == "okx",
                    onClick = { selectedExchange = "okx"; viewModel.clearState() },
                    label = { Text("OKX") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key (المفتاح العام)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            OutlinedTextField(
                value = apiSecret,
                onValueChange = { apiSecret = it },
                label = { Text("API Secret / Secret Key (المفتاح السري)") },
                singleLine = true,
                visualTransformation = if (isSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isSecretVisible = !isSecretVisible }) {
                        Icon(
                            imageVector = if (isSecretVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Secret Visibility",
                            tint = PrimaryGold
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (saveState is Resource.Error) {
                Text((saveState as Resource.Error).message ?: "فشل حفظ المفاتيح", color = AccentRed, fontSize = 13.sp)
            }

            if (saveState is Resource.Success) {
                Text("تم التحقق من المفاتيح وحفظها مشفرة بـ AES-256 بنجاح على الخادم!", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.connectExchange(selectedExchange, apiKey, apiSecret) },
                enabled = saveState !is Resource.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
            ) {
                if (saveState is Resource.Loading) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp))
                } else {
                    Text("حفظ ومصادقة المفاتيح في Backend", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("العودة للوحة التحكم", color = TextSecondary)
            }
        }
    }
}
