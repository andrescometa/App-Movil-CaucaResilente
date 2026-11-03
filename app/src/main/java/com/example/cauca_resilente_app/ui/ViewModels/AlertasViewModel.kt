package com.example.cauca_resilente_app.ui.ViewModels
// ui/viewmodel/AlertasViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.AlertItem
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertasUiState(
    val alertas: List<AlertItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AlertasViewModel(private val api: CaucaResilienteApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertasUiState())
    val uiState: StateFlow<AlertasUiState> = _uiState.asStateFlow()

    init {
        cargarAlertas()
    }

    fun cargarAlertas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = api.getAlerts()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        alertas = response.body().orEmpty()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las alertas (HTTP ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudieron cargar las alertas: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Filtro por categoría según la pestaña
    fun alertasPorCategoria(categoria: String): List<AlertItem> {
        return _uiState.value.alertas.filter {
            it.category.equals(categoria, ignoreCase = true)
        }
    }
}