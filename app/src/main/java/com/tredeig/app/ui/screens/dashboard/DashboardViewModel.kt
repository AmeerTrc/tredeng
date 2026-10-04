package com.tredeig.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import com.tredeig.app.data.repository.MarketRepositoryImpl
import com.tredeig.app.domain.model.MarketTicker
import com.tredeig.app.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val portfolio: PortfolioSummary? = null,
    val ticker: MarketTicker? = null,
    val isKillSwitchActive: Boolean = false,
    val isWsConnected: Boolean = true,
    val isApiConnected: Boolean = true,
    val tradingStatusText: String = "توقف التداول (المرحلة الأولى)",
    val dailyLossUsdt: Double = 0.0,
    val dailyTradesCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val exchangeRepository: ExchangeRepositoryImpl,
    private val marketRepository: MarketRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val portfolioRes = exchangeRepository.getPortfolioSummary()
            val tickerRes = marketRepository.getTicker("BTCUSDT")

            val portfolio = if (portfolioRes is Resource.Success) portfolioRes.data else null
            val ticker = if (tickerRes is Resource.Success) tickerRes.data else null

            _uiState.value = _uiState.value.copy(
                portfolio = portfolio,
                ticker = ticker,
                isLoading = false,
                errorMessage = if (portfolioRes is Resource.Error) portfolioRes.message else null
            )
        }
    }

    fun toggleKillSwitch(active: Boolean) {
        viewModelScope.launch {
            val res = exchangeRepository.toggleKillSwitch(active)
            if (res is Resource.Success) {
                _uiState.value = _uiState.value.copy(
                    isKillSwitchActive = res.data ?: active,
                    tradingStatusText = if (res.data == true) "إيقاف الطوارئ (Kill Switch)" else "توقف التداول (المرحلة الأولى)"
                )
            }
        }
    }
}
