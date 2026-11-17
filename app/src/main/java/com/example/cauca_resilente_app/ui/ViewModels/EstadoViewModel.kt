package com.example.cauca_resilente_app.ui.ViewModels

// ui/viewmodel/EstadoViewModel.kt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
// ui/viewmodel/EstadoViewModel.kt



import com.example.cauca_resilente_app.ui.Screens.EstadoUsuario  // ← importar el enum de la pantalla
import com.example.cauca_resilente_app.ui.theme.GrisInactivo
import com.example.cauca_resilente_app.ui.theme.RojoEmergencia
import com.example.cauca_resilente_app.ui.theme.VerdeSeguro

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class EstadoUiState(
    val estadoActual: EstadoUsuario = EstadoUsuario.A_SALVO,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val mensajeExito: String? = null
)

class EstadoViewModel(private val api: CaucaResilienteApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadoUiState())
    val uiState: StateFlow<EstadoUiState> = _uiState.asStateFlow()

    fun cambiarEstado(nuevoEstado: EstadoUsuario) {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesión no válida. Inicia sesión de nuevo."
            )
            return
        }

        val estadoAnterior = _uiState.value.estadoActual

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                mensajeExito = null,
                estadoActual = nuevoEstado
            )

            try {
                val response = api.updateStatus(
                    usuarioId,
                    StatusRequest(nuevoEstado.valorApi, nuevoEstado.titulo)
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    estadoActual = nuevoEstado,
                    mensajeExito = response.mensaje.ifBlank { "Estado actualizado correctamente" }
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    estadoActual = estadoAnterior,
                    errorMessage = "No se pudo actualizar el estado (HTTP ${e.code()})"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    estadoActual = estadoAnterior,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(errorMessage = null, mensajeExito = null)
    }
}