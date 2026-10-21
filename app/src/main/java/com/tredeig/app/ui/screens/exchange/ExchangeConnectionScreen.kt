package com.tredeig.app.ui.screens.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var selectedExchange by remember { mutableStateOf("bybit") }
    var apiKey by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }
    var isSecretVisible by remember { mutableStateOf(false) }

    var keyToDelete by remember { mutableStateOf<String?>(null) }

    val saveState by viewModel.saveState.collectAsState()
    val allStatusesState by viewModel.allStatuses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllStatuses()
    }

    // Confirmation Dialog for Key Deletion
    if (keyToDelete != null) {
        AlertDialog(
            onDismissRequest = { keyToDelete = null },
            title = { Text("تأكيد حذف المفتاح المشفر", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت تأكد من رغبتك في حذف مفاتيح منصة ${keyToDelete?.uppercase()} من خادم Tredeng المشفر؟\n\nتنبيه: حذف المفتاح من التطبيق لا يلغيه من المنصة نفسها. لإلغائه تماماً اذهب إلى إعدادات API في المنصة.") },
            confirmButton = {
                Button(
                    onClick = {
                        keyToDelete?.let { viewModel.deleteKeys(it) }
                        keyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { keyToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("إدارة المنصات والاتصالات (API Keys)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ربط وإدارة مفاتيح API الخاصة بمنصات Bybit وBinance وOKX لتفعيل التداول الآمن عبر الخادم المشفر.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Add / Update Keys Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة / تحديث مفتاح منصة:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedExchange == "bybit",
                            onClick = { selectedExchange = "bybit"; viewModel.clearState() },
                            label = { Text("Bybit") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedExchange == "binance",
                            onClick = { selectedExchange = "binance"; viewModel.clearState() },
                            label = { Text("Binance") },
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
                        label = { Text("API Secret (المفتاح السري)") },
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
                        Text("تم التشفير والحفظ بنجاح بـ AES-256 في قاعدة البيانات!", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            Text("تشفير وحفظ المفتاح الموحد", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // List of Connected Exchanges & Their Real Status
        item {
            Text("حالة الاتصال والمنصات المسجلة:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        when (val statuses = allStatusesState) {
            is Resource.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGold)
                    }
                }
            }
            is Resource.Success -> {
                val list = statuses.data ?: emptyList()
                items(list) { ex ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(ex.exchangeName.uppercase(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("المفتاح: ${ex.apiKeyMasked}", fontSize = 12.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (ex.isConnected) AccentGreen.copy(alpha = 0.2f) else AccentRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (ex.isConnected) "متصل بنجاح" else "غير مرتبط",
                                            color = if (ex.isConnected) AccentGreen else AccentRed,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(onClick = { viewModel.loadAllStatuses() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Test Connection", tint = PrimaryGold)
                                }
                                if (ex.apiKeyMasked != "غير مرتبط") {
                                    IconButton(onClick = { keyToDelete = ex.exchangeName }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Keys", tint = AccentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }

        item {
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("العودة للوحة التحكم", color = TextSecondary)
            }
        }
    }
}
