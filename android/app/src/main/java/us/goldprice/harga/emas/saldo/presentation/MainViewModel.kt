package us.goldprice.harga.emas.saldo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import us.goldprice.harga.emas.saldo.data.GoldRepository
import us.goldprice.harga.emas.saldo.domain.GoldData

sealed interface MainUiState {
    object Loading : MainUiState
    data class Success(
        val data: GoldData, 
        val adConfig: us.goldprice.harga.emas.saldo.data.AdConfig? = null,
        val historyData: List<us.goldprice.harga.emas.saldo.domain.HistoryItem> = emptyList()
    ) : MainUiState
    data class Error(val message: String) : MainUiState
}

class MainViewModel(private val repository: GoldRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            if (_uiState.value !is MainUiState.Success) {
                _uiState.value = MainUiState.Loading
            }
            repository.fetchGoldPricesFlow().collect { result ->
                result.fold(
                    onSuccess = { data ->
                        val adConfig = try { repository.fetchAdConfig() } catch (e: Exception) { null }
                        val history = try { repository.fetchHistory() } catch (e: Exception) { emptyList() }
                        _uiState.value = MainUiState.Success(data, adConfig, history)
                    },
                    onFailure = { error ->
                        // If we already have Success state (from cache), don't overwrite with Error
                        if (_uiState.value !is MainUiState.Success) {
                            _uiState.value = MainUiState.Error(error.message ?: "Unknown error")
                        }
                    }
                )
            }
        }
    }
}
