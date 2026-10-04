package com.tredeig.app.ui.screens.risk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@Composable
fun RiskSettingsScreen(onBack: () -> Unit) {
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
            Text("قواعد وإدارة المخاطر الصارمة", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text("هذه القواعد مفروضة برمجياً من الخادم الخفي ولا يمكن للذكاء الاصطناعي تعديلها أو تجاوزها:", color = TextSecondary, fontSize = 13.sp)

            RiskRuleRow("الحد الأقصى للمخاطرة بالصفقة:", "0.25% من الرصيد")
            RiskRuleRow("الحد الأقصى للخسارة اليومية:", "1.00% من الرصيد")
            RiskRuleRow("الحد الأدنى لنسبة ثقة AI:", "0.70 (70%)")
            RiskRuleRow("الحد الأقصى لعدد الصفقات اليومية:", "3 صفقات")
            RiskRuleRow("شرط إلغاء الصفقة:", "عدم وجود Stop Loss")
            RiskRuleRow("عدد الصفقات المفتوحة بالتوازي:", "صفقة واحدة فقط لزوج BTCUSDT")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ والعودة", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RiskRuleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 13.sp)
        Text(value, color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
