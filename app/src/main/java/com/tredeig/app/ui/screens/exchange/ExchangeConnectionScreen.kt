package com.tredeig.app.ui.screens.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.common.Resource
import com.tredeig.app.core.theme.*

data class ExchangeInfo(
    val id: String,
    val name: String,
    val badgeColor: Color,
    val textColor: Color,
    val symbolLetter: String
)

val GLOBAL_EXCHANGES = listOf(
    ExchangeInfo("bybit", "Bybit", Color(0xFFF7A600), Color.Black, "B"),
    ExchangeInfo("binance", "Binance", Color(0xFFF0B90B), Color.Black, "❖"),
    ExchangeInfo("okx", "OKX", Color(0xFF000000), Color.White, "O"),
    ExchangeInfo("kucoin", "KuCoin", Color(0xFF24AE8F), Color.White, "K"),
    ExchangeInfo("gateio", "Gate.io", Color(0xFF00A2D0), Color.White, "G"),
    ExchangeInfo("bitget", "Bitget", Color(0xFF00F0FF), Color.Black, "B"),
    ExchangeInfo("mexc", "MEXC", Color(0xFF2B62F6), Color.White, "M"),
    ExchangeInfo("exness", "Exness", Color(0xFFFFD300), Color.Black, "E")
)

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
            title = { Text("تأكيد حذف وفصل المفتاح", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت تأكد من رغبتك في حذف مفاتيح منصة ${keyToDelete?.uppercase()} من خادم Tredeng المشفر؟\n\nتنبيه: حذف المفتاح من التطبيق لا يلغيه من المنصة نفسها. لإلغائه تماماً اذهب إلى إعدادات API في المنصة.") },
            confirmButton = {
                Button(
                    onClick = {
                        keyToDelete?.let { viewModel.deleteKeys(it) }
                        keyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("فصل وحذف المفتاح")
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
            Text("إدارة ورابط المنصات العالمية (API Keys)", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ربط وإدارة مفاتيح API الخاصة بالمنصات العالمية المعتمدة مع تمييز المنصات النشطة باللون الأخضر المميز.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // List of Connected Exchanges & Their Real Status
        item {
            Text("المنصات المرتبطة والحالة المباشرة:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        when (val statuses = allStatusesState) {
            is Resource.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGold)
                    }
                }
            }
            is Resource.Success -> {
                val list = statuses.data ?: emptyList()
                items(list) { ex ->
                    val isConnected = ex.isConnected && ex.apiKeyMasked != "غير مرتبط"
                    val exchangeObj = GLOBAL_EXCHANGES.find { it.id == ex.exchangeName.lowercase() }

                    // Card with VIBRANT GREEN HIGHLIGHT for connected exchanges!
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConnected) AccentGreen.copy(alpha = 0.15f) else CardBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isConnected) 2.dp else 1.dp,
                                color = if (isConnected) AccentGreen else BorderColor,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Official Brand Logo Badge
                                    Surface(
                                        color = exchangeObj?.badgeColor ?: PrimaryGold,
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = exchangeObj?.symbolLetter ?: ex.exchangeName.take(1).uppercase(),
                                                color = exchangeObj?.textColor ?: DarkBackground,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = exchangeObj?.name ?: ex.exchangeName.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Text("المفتاح: ${ex.apiKeyMasked}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }

                                Surface(
                                    color = if (isConnected) AccentGreen else Color.DarkGray,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isConnected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = if (isConnected) "نشط ومتصل" else "غير مرتبط",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (isConnected) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { keyToDelete = ex.exchangeName },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فصل وإلغاء المفتاح", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
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
                    Text("اختر المنصة لإضافة مفتاح جديد:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // Horizontal Scrolling Brand Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(GLOBAL_EXCHANGES) { ex ->
                            val isSel = selectedExchange == ex.id
                            Surface(
                                color = if (isSel) ex.badgeColor else CardBackground,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) ex.badgeColor else BorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedExchange = ex.id; viewModel.clearState() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isSel) Color.White else ex.badgeColor,
                                        shape = CircleShape,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = ex.symbolLetter,
                                                color = if (isSel) Color.Black else ex.textColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = ex.name,
                                        color = if (isSel) ex.textColor else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
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
                        Text("تم التشفير والحفظ بنجاح بـ AES-256 وتفعيل المفتاح!", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            Text("تشفير وتفعيل المفتاح الموحد", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
