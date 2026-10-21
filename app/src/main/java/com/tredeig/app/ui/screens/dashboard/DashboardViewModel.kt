package com.tredeig.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.remote.dto.KlineDto
import com.tredeig.app.data.remote.dto.UserSettingsDto
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import com.tredeig.app.data.repository.MarketRepositoryImpl
import com.tredeig.app.domain.model.MarketTicker
import com.tredeig.app.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val portfolioSummary: PortfolioSummary? = null,
    val ticker: MarketTicker? = null,
    val userSettings: UserSettingsDto? = null,
    val klines: List<KlineDto> = emptyList(),
    val selectedInterval: String = "15m",
    val activeSymbol: String = "BTCUSDT",
    val selectedExchange: String = "bybit",
    val isKillSwitchActive: Boolean = false,
    val isWsConnected: Boolean = true,
    val isApiConnected: Boolean = true,
    val aiStatusText: String = "Gemini 1.5 Flash API - متصل ومستقر",
    val aiIsConnected: Boolean = true,
    val aiLastAnalysisTime: String = "منذ دقيقة",
    val aiSignalSummary: String = "تحليل خوارزمي محايد - في انتظار ظهور دلالات الارتفاع",
    val isLoading: Boolean = false,
    val isChartLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val exchangeRepository: ExchangeRepositoryImpl,
    private val marketRepository: MarketRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val settingsRes = exchangeRepository.getUserSettings()
            val settings = if (settingsRes is Resource.Success) settingsRes.data else null
            val exchange = settings?.selectedExchange ?: "bybit"

            val portfolioRes = exchangeRepository.getPortfolioSummary()
            val tickerRes = marketRepository.getTicker(_uiState.value.activeSymbol, exchange)

            val portfolio = if (portfolioRes is Resource.Success) portfolioRes.data else null
            val ticker = if (tickerRes is Resource.Success) tickerRes.data else null

            _uiState.value = _uiState.value.copy(
                portfolioSummary = portfolio,
                ticker = ticker,
                userSettings = settings,
                selectedExchange = exchange,
                isLoading = false,
                errorMessage = if (portfolioRes is Resource.Error) portfolioRes.message else null
            )

            loadKlines(_uiState.value.activeSymbol, _uiState.value.selectedInterval, exchange)
        }
    }

    fun setInterval(interval: String) {
        _uiState.value = _uiState.value.copy(selectedInterval = interval)
        loadKlines(_uiState.value.activeSymbol, interval, _uiState.value.selectedExchange)
    }

    fun loadKlines(symbol: String, interval: String, exchangeName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChartLoading = true)
            val klineRes = marketRepository.getKlines(symbol, interval, exchangeName)
            val klines = if (klineRes is Resource.Success) klineRes.data ?: emptyList() else emptyList()

            _uiState.value = _uiState.value.copy(
                klines = klines,
                isChartLoading = false
            )
        }
    }

    fun toggleKillSwitch(active: Boolean) {
        viewModelScope.launch {
            val res = exchangeRepository.toggleKillSwitch(active)
            if (res is Resource.Success) {
                _uiState.value = _uiState.value.copy(
                    isKillSwitchActive = res.data ?: active
                )
            }
        }
    }
}
