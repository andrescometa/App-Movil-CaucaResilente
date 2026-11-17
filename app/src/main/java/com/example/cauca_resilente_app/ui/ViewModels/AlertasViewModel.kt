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
    val errorMessage: String? = null,
    val alertaSeleccionada: AlertItem? = null,
    val cargandoDetalle: Boolean = false,
    val errorDetalle: String? = null
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

    // Carga una alerta por id para la pantalla de detalle. Si la petición
    // falla, intenta reutilizar la alerta que ya está en la lista local.
    fun cargarAlerta(id: String) {
        if (id.isBlank()) {
            _uiState.value = _uiState.value.copy(
                cargandoDetalle = false,
                errorDetalle = "No se indicó la alerta a consultar"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargandoDetalle = true,
                errorDetalle = null,
                alertaSeleccionada = null
            )

            try {
                val response = api.getAlert(id)
                val alerta = response.body()
                if (response.isSuccessful && alerta != null) {
                    _uiState.value = _uiState.value.copy(
                        cargandoDetalle = false,
                        alertaSeleccionada = alerta
                    )
                    return@launch
                }

                val local = _uiState.value.alertas.firstOrNull { it.id == id }
                _uiState.value = _uiState.value.copy(
                    cargandoDetalle = false,
                    alertaSeleccionada = local,
                    errorDetalle = if (local == null) {
                        "No se pudo cargar la alerta (HTTP ${response.code()})"
                    } else {
                        null
                    }
                )
            } catch (e: Exception) {
                val local = _uiState.value.alertas.firstOrNull { it.id == id }
                _uiState.value = _uiState.value.copy(
                    cargandoDetalle = false,
                    alertaSeleccionada = local,
                    errorDetalle = if (local == null) {
                        "No se pudo cargar la alerta: ${e.message ?: "sin conexión"}"
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun limpiarDetalle() {
        _uiState.value = _uiState.value.copy(
            alertaSeleccionada = null,
            cargandoDetalle = false,
            errorDetalle = null
        )
    }

    // Filtro por categoría según la pestaña
    fun alertasPorCategoria(categoria: String): List<AlertItem> {
        return _uiState.value.alertas.filter {
            it.category.equals(categoria, ignoreCase = true)
        }
    }
}