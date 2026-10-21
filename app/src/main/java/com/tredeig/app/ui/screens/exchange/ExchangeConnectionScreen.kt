package com.tredeig.app.ui.screens.exchange

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    var selectedExchangeId by remember { mutableStateOf("bybit") }
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
            text = { Text("هل أنت تأكد من رغبتك في حذف مفاتيح منصة ${keyToDelete?.uppercase()} من خادم Tredeng المشفر؟") },
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
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("إدارة ورابط المنصات العالمية (API Keys)", color = PrimaryGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "اضغط على المنصة المحددة لإدخال وتفعيل المفتاح الموحد. المنصات المرتبطة تظهر باللون الأخضر المميز.",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // Slim & Compact List of Global Exchanges
        val statusesList = (allStatusesState as? Resource.Success)?.data ?: emptyList()

        items(GLOBAL_EXCHANGES) { ex ->
            val statusObj = statusesList.find { it.exchangeName.lowercase() == ex.id }
            val isConnected = statusObj?.isConnected == true && statusObj.apiKeyMasked != "غير مرتبط"
            val isExpanded = selectedExchangeId == ex.id

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) AccentGreen.copy(alpha = 0.12f) else CardBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isConnected) 1.5.dp else 1.dp,
                        color = if (isConnected) AccentGreen else BorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Slim Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedExchangeId = if (isExpanded) "" else ex.id
                                viewModel.clearState()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Official Brand Badge Logo
                            Surface(
                                color = ex.badgeColor,
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = ex.symbolLetter,
                                        color = ex.textColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(ex.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                if (isConnected) {
                                    Text("المفتاح: ${statusObj?.apiKeyMasked}", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Single Word Status Badge: ONLY "نشط" in Green if connected
                            Surface(
                                color = if (isConnected) AccentGreen else Color.DarkGray,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isConnected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = if (isConnected) "نشط" else "غير مرتبط",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            if (isConnected) {
                                IconButton(
                                    onClick = { keyToDelete = ex.id },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Fasl", tint = AccentRed, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Expandable Key Input Form inside the platform card
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                label = { Text("API Key ($ex.name المفتاح العام)") },
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
                                label = { Text("API Secret ($ex.name المفتاح السري)") },
                                singleLine = true,
                                visualTransformation = if (isSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isSecretVisible = !isSecretVisible }) {
                                        Icon(
                                            imageVector = if (isSecretVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Secret",
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
                                Text((saveState as Resource.Error).message ?: "فشل حفظ المفاتيح", color = AccentRed, fontSize = 12.sp)
                            }

                            if (saveState is Resource.Success) {
                                Text("تم التشفير والتفعيل بنجاح بـ AES-256!", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.connectExchange(ex.id, apiKey, apiSecret) },
                                enabled = saveState !is Resource.Loading,
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                            ) {
                                if (saveState is Resource.Loading) {
                                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("تشفير وتفعيل مفتاح ${ex.name}", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("العودة للوحة التحكم", color = TextSecondary)
            }
        }
    }
}
