package com.tredeig.app.ui.screens.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.repository.ExchangeRepositoryImpl
import com.tredeig.app.domain.model.ExchangeKeyInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExchangeViewModel(private val repository: ExchangeRepositoryImpl) : ViewModel() {

    private val _saveState = MutableStateFlow<Resource<ExchangeKeyInfo>?>(null)
    val saveState: StateFlow<Resource<ExchangeKeyInfo>?> = _saveState

    fun connectExchange(exchangeName: String, apiKey: String, apiSecret: String) {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            _saveState.value = Resource.Error("يرجى إدخال مفتاح API والتوقيع السرّي")
            return
        }

        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            _saveState.value = repository.saveKeys(exchangeName, apiKey, apiSecret)
        }
    }
}
