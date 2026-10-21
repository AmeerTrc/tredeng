package com.tredeig.app.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.remote.dto.KlineDto
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import com.tredeig.app.data.repository.MarketRepositoryImpl
import com.tredeig.app.domain.model.MarketTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MarketUiState(
    val selectedExchange: String = "bybit",
    val availableSymbols: List<String> = emptyList(),
    val selectedCoins: Map<String, Boolean> = emptyMap(),
    val favorites: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Chart / Kline details
    val activeSymbol: String = "BTCUSDT",
    val selectedInterval: String = "15m",
    val klines: List<KlineDto> = emptyList(),
    val ticker: MarketTicker? = null,
    val isChartLoading: Boolean = false,
    val chartError: String? = null
)

class MarketViewModel(
    private val exchangeRepository: ExchangeRepositoryImpl,
    private val marketRepository: MarketRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState

    init {
        loadData("bybit")
        loadChartData("BTCUSDT", "15m", "bybit")
    }

    fun setExchange(exchangeName: String) {
        _uiState.value = _uiState.value.copy(selectedExchange = exchangeName)
        loadData(exchangeName)
        loadChartData(_uiState.value.activeSymbol, _uiState.value.selectedInterval, exchangeName)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectSymbolForChart(symbol: String) {
        _uiState.value = _uiState.value.copy(activeSymbol = symbol)
        loadChartData(symbol, _uiState.value.selectedInterval, _uiState.value.selectedExchange)
    }

    fun setIntervalForChart(interval: String) {
        _uiState.value = _uiState.value.copy(selectedInterval = interval)
        loadChartData(_uiState.value.activeSymbol, interval, _uiState.value.selectedExchange)
    }

    fun loadChartData(symbol: String, interval: String, exchangeName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChartLoading = true, chartError = null)

            val tickerRes = marketRepository.getTicker(symbol, exchangeName)
            val klineRes = marketRepository.getKlines(symbol, interval, exchangeName)

            val ticker = if (tickerRes is Resource.Success) tickerRes.data else null
            val klines = if (klineRes is Resource.Success) klineRes.data ?: emptyList() else emptyList()
            val chartErr = if (klineRes is Resource.Error) klineRes.message else null

            _uiState.value = _uiState.value.copy(
                isChartLoading = false,
                ticker = ticker,
                klines = klines,
                chartError = chartErr
            )
        }
    }

    fun loadData(exchangeName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val symbolsRes = exchangeRepository.getAvailableSymbols(exchangeName)
            val selectedRes = exchangeRepository.getSelectedCoins(exchangeName)

            val symbols = if (symbolsRes is Resource.Success) symbolsRes.data ?: emptyList() else listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT")
            val selectedMap = mutableMapOf<String, Boolean>()
            val favs = mutableSetOf<String>()

            if (selectedRes is Resource.Success) {
                selectedRes.data?.forEach {
                    selectedMap[it.symbol] = it.isActive
                    if (it.isFavorite) favs.add(it.symbol)
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                availableSymbols = symbols,
                selectedCoins = selectedMap,
                favorites = favs
            )
        }
    }

    fun toggleCoinActive(symbol: String, isActive: Boolean) {
        viewModelScope.launch {
            val currentMap = _uiState.value.selectedCoins.toMutableMap()
            currentMap[symbol] = isActive
            _uiState.value = _uiState.value.copy(selectedCoins = currentMap)

            val isFav = _uiState.value.favorites.contains(symbol)
            exchangeRepository.saveSelectedCoin(_uiState.value.selectedExchange, symbol, isActive, isFav)
        }
    }

    fun toggleFavorite(symbol: String) {
        viewModelScope.launch {
            val currentFavs = _uiState.value.favorites.toMutableSet()
            val isNowFav = !currentFavs.contains(symbol)
            if (isNowFav) currentFavs.add(symbol) else currentFavs.remove(symbol)
            _uiState.value = _uiState.value.copy(favorites = currentFavs)

            val isActive = _uiState.value.selectedCoins[symbol] ?: false
            exchangeRepository.saveSelectedCoin(_uiState.value.selectedExchange, symbol, isActive, isNowFav)
        }
    }
}
