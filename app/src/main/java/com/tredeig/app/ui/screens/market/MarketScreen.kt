package com.tredeig.app.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tredeig.app.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    onBack: () -> Unit,
    onCoinClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val filteredSymbols = uiState.availableSymbols.filter {
        it.contains(uiState.searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("اختيار العملات وسوق التداول", color = PrimaryGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text("حدد العملات المتاحة، واضغط على اسم العملة لفتح الشارت والشموع اليابانية:", color = TextSecondary, fontSize = 12.sp)

            // Exchange Selector Tabs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = uiState.selectedExchange == "bybit",
                    onClick = { viewModel.setExchange("bybit") },
                    label = { Text("Bybit") }
                )
                FilterChip(
                    selected = uiState.selectedExchange == "binance",
                    onClick = { viewModel.setExchange("binance") },
                    label = { Text("Binance") }
                )
                FilterChip(
                    selected = uiState.selectedExchange == "okx",
                    onClick = { viewModel.setExchange("okx") },
                    label = { Text("OKX") }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ابحث عن رمز العملة (مثال: BTCUSDT)...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGold) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryGold,
                    cursorColor = PrimaryGold
                )
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryGold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Coin List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSymbols) { symbol ->
                    val isActive = uiState.selectedCoins[symbol] ?: false
                    val isFav = uiState.favorites.contains(symbol)

                    CoinItemRow(
                        symbol = symbol,
                        isActive = isActive,
                        isFavorite = isFav,
                        onToggleActive = { active -> viewModel.toggleCoinActive(symbol, active) },
                        onToggleFavorite = { viewModel.toggleFavorite(symbol) },
                        onCoinClick = { onCoinClick(symbol) }
                    )
                }
            }

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
fun CoinItemRow(
    symbol: String,
    isActive: Boolean,
    isFavorite: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onToggleFavorite: () -> Unit,
    onCoinClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground, RoundedCornerShape(8.dp))
            .clickable { onCoinClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isActive,
                onCheckedChange = onToggleActive,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(symbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) PrimaryGold else TextSecondary
            )
        }
    }
}
