package com.tredeig.app.ui.screens.positions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.remote.dto.PaperTradeDto
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PositionsUiState(
    val openPositions: List<PaperTradeDto> = emptyList(),
    val tradeHistory: List<PaperTradeDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PositionsViewModel(
    private val exchangeRepository: ExchangeRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(PositionsUiState())
    val uiState: StateFlow<PositionsUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val openRes = exchangeRepository.getOpenPositions()
            val historyRes = exchangeRepository.getTradeHistory()

            val openList = if (openRes is Resource.Success) openRes.data ?: emptyList() else emptyList()
            val historyList = if (historyRes is Resource.Success) historyRes.data ?: emptyList() else emptyList()

            val err = if (openRes is Resource.Error) openRes.message else if (historyRes is Resource.Error) historyRes.message else null

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                openPositions = openList,
                tradeHistory = historyList,
                errorMessage = err
            )
        }
    }

    fun closePositionManually(tradeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val res = exchangeRepository.closePosition(tradeId)
            if (res is Resource.Success) {
                _uiState.value = _uiState.value.copy(successMessage = "تم إغلاق الصفقة يدوياً بنجاح")
                loadData()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
            }
        }
    }
}
