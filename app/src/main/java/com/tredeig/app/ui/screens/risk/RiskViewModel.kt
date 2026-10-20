package com.tredeig.app.ui.screens.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.remote.dto.UserSettingsDto
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RiskUiState(
    val settings: UserSettingsDto = UserSettingsDto(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class RiskViewModel(
    private val exchangeRepository: ExchangeRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiskUiState())
    val uiState: StateFlow<RiskUiState> = _uiState

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val res = exchangeRepository.getUserSettings()
            if (res is Resource.Success) {
                _uiState.value = _uiState.value.copy(isLoading = false, settings = res.data!!)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
            }
        }
    }

    fun saveSettings(settings: UserSettingsDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val res = exchangeRepository.saveUserSettings(settings)
            if (res is Resource.Success) {
                _uiState.value = _uiState.value.copy(isLoading = false, settings = res.data!!, successMessage = "تم حفظ جميع الإعدادات والمبالغ بنجاح")
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
