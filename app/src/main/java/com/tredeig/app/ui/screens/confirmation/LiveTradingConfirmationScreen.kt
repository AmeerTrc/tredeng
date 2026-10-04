package com.tredeig.app.ui.screens.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@Composable
fun LiveTradingConfirmationScreen(
    onConfirmSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val requiredPhrase = "أفهم أن التداول قد يسبب خسارة"
    var inputPhrase by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("تأكيد تفعيل التداول الحقيقي (Live Trading)", color = AccentRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "تحذير: التداول بالمال الحقيقي قد يؤدي إلى خسارة رأس المال جزئياً أو كلياً. يتطلب التفعيل كتابة النص التالي بدقة للتأكيد:",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text(
                    text = requiredPhrase,
                    color = PrimaryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally)
                )
            }

            OutlinedTextField(
                value = inputPhrase,
                onValueChange = { inputPhrase = it },
                label = { Text("اكتب العبارة أعلاه لتأكيد موافقتك") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (errorMessage != null) {
                Text(errorMessage!!, color = AccentRed, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (inputPhrase.trim() == requiredPhrase) {
                        onConfirmSuccess()
                    } else {
                        errorMessage = "العبارة غير مطابقة تماماً. يرجى كتابتها كما هي."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("تأكيد وتفعيل Live Trading", color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBack) {
                Text("إلغاء والعودة", color = TextSecondary)
            }
        }
    }
}
