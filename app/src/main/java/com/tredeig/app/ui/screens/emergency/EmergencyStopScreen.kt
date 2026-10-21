package com.tredeig.app.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@Composable
fun EmergencyStopScreen(
    isKillSwitchActive: Boolean,
    onToggleKillSwitch: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showActivateConfirmDialog by remember { mutableStateOf(false) }
    var showDeactivateConfirmDialog by remember { mutableStateOf(false) }

    // Confirmation Dialog for Activation
    if (showActivateConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showActivateConfirmDialog = false },
            title = { Text("تأكيد تفعيل إيقاف الطوارئ (Kill Switch)", fontWeight = FontWeight.Bold, color = AccentRed) },
            text = {
                Text(
                    "عند التفعيل:\n\n" +
                    "1. سيتم حظر الخادم فوراً من فتح أي صفقات جديدة آلياً أو يدوياً.\n" +
                    "2. الصفقات المفتوحة حالياً ستبقى كما هي وستحتاج لإغلاقها يدوياً إذا رغبت.\n" +
                    "3. لن يتم حذف مفاتيح API الخاصة بك."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showActivateConfirmDialog = false
                        onToggleKillSwitch(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("تأكيد التفعيل الفوري")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivateConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Confirmation Dialog for Deactivation
    if (showDeactivateConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateConfirmDialog = false },
            title = { Text("تأكيد إعادة تشغيل التداول", fontWeight = FontWeight.Bold, color = PrimaryGold) },
            text = { Text("هل أنت متاكد من استئناف السماح لربوت التداول التلقائي بفتح صفقات جديدة وفق حدود إعدادات المخاطر؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeactivateConfirmDialog = false
                        onToggleKillSwitch(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text("استئناف التداول", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("زر الطوارئ الموحد (Kill Switch)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Current Status Badge
            Surface(
                color = if (isKillSwitchActive) AccentRed.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isKillSwitchActive) "حالة النظام: مُعَطّل / إيقاف طوارئ نَشِط" else "حالة النظام: طبيعي / التداول متاح",
                    color = if (isKillSwitchActive) AccentRed else AccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("شرح آلية العمل الصريحة:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("• تفعيل الزر يمنع الخادم فوراً من فتح أي صفقات جديدة.", color = TextSecondary, fontSize = 12.sp)
                    Text("• الصفقات المفتوحة حالياً لا تُغلق تلقائياً لحماية رأس المال، بل تظل مراقبة بواسطة Stop Loss ويمكنك إغلاقها يدوياً.", color = TextSecondary, fontSize = 12.sp)
                    Text("• التفعيل لا يحذف مفاتيح API الخاصة بك من السيرفر.", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isKillSwitchActive) {
                Button(
                    onClick = { showActivateConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تفعيل إيقاف الطوارئ الكلي (EMERGENCY STOP)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Button(
                    onClick = { showDeactivateConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("استئناف وإعادة تشغيل التداول", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            TextButton(onClick = onBack) {
                Text("العودة للوحة التحكم", color = TextSecondary)
            }
        }
    }
}
