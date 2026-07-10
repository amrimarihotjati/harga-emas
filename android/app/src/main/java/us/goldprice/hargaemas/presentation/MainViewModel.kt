package us.goldprice.hargaemas.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import us.goldprice.hargaemas.data.GoldRepository
import us.goldprice.hargaemas.domain.GoldData

sealed interface MainUiState {
    object Loading : MainUiState
    data class Success(val data: GoldData, val adConfig: us.goldprice.hargaemas.data.AdConfig? = null) : MainUiState
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
            _uiState.value = MainUiState.Loading
            try {
                val result = repository.fetchGoldPrices()
                val adConfig = try { repository.fetchAdConfig() } catch (e: Exception) { null }
                _uiState.value = MainUiState.Success(result, adConfig)
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
