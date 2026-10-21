package com.tredeig.app.ui.screens.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.remote.dto.ExchangeStatusResponse
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import com.tredeig.app.domain.model.ExchangeKeyInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExchangeViewModel(private val repository: ExchangeRepositoryImpl) : ViewModel() {

    private val _saveState = MutableStateFlow<Resource<ExchangeKeyInfo>?>(null)
    val saveState: StateFlow<Resource<ExchangeKeyInfo>?> = _saveState

    private val _allStatuses = MutableStateFlow<Resource<List<ExchangeStatusResponse>>?>(null)
    val allStatuses: StateFlow<Resource<List<ExchangeStatusResponse>>?> = _allStatuses

    init {
        loadAllStatuses()
    }

    fun loadAllStatuses() {
        viewModelScope.launch {
            _allStatuses.value = Resource.Loading()
            _allStatuses.value = repository.getAllExchangeStatus()
        }
    }

    fun connectExchange(exchangeName: String, apiKey: String, apiSecret: String) {
        val cleanExchange = exchangeName.trim().lowercase()
        val cleanKey = apiKey.trim()
        val cleanSecret = apiSecret.trim()

        if (cleanKey.isBlank() || cleanSecret.isBlank()) {
            _saveState.value = Resource.Error("يرجى إدخال مفتاح API والمفتاح السري (Secret Key)")
            return
        }

        if (cleanKey.length < 8 || cleanSecret.length < 8) {
            _saveState.value = Resource.Error("طول المفتاح غير مكتمل. يرجى التأكد من نسخ المفتاح كاملاً بدون نقص.")
            return
        }

        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = repository.saveKeys(cleanExchange, cleanKey, cleanSecret)
            _saveState.value = result
            loadAllStatuses()
        }
    }

    fun deleteKeys(exchangeName: String) {
        viewModelScope.launch {
            repository.deleteKeys(exchangeName)
            loadAllStatuses()
        }
    }

    fun clearState() {
        _saveState.value = null
    }
}
