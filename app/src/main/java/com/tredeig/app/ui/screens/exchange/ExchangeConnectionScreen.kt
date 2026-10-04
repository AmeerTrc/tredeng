package com.tredeig.app.ui.screens.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.common.Resource
import com.tredeig.app.core.theme.*

@Composable
fun ExchangeConnectionScreen(
    viewModel: ExchangeViewModel,
    onBack: () -> Unit
) {
    var exchangeName by remember { mutableStateOf("Binance Spot") }
    var apiKey by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }
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
            Text("ربط منصة التداول (Binance / Exness)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "تحذير أمان صارم: قم بإنشاء API Key بتصريح Spot Trading فقط. يُمُنع تماماً تفعيل صلاحيات السحب (Withdrawals) أو التحويل.",
                color = WarningYellow,
                fontSize = 12.sp
            )

            OutlinedTextField(
                value = exchangeName,
                onValueChange = { exchangeName = it },
                label = { Text("اسم المنصة (Binance / Exness)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
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
                label = { Text("API Secret") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (saveState is Resource.Error) {
                Text((saveState as Resource.Error).message ?: "خطأ في الاتصال", color = AccentRed, fontSize = 13.sp)
            }

            if (saveState is Resource.Success) {
                Text("تم حفظ المفاتيح مشفرة بـ AES-256 بنجاح على الخادم!", color = AccentGreen, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.connectExchange(exchangeName, apiKey, apiSecret) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
            ) {
                if (saveState is Resource.Loading) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp))
                } else {
                    Text("حفظ المفاتيح في Backend آمن", color = DarkBackground, fontWeight = FontWeight.Bold)
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
