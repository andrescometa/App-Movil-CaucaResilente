package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.AsignacionSocorrista
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SolicitudesAsignadasUiState(
    val asignaciones: List<AsignacionSocorrista> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SolicitudesAsignadasViewModel(
    private val api: CaucaResilienteApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudesAsignadasUiState())
    val uiState: StateFlow<SolicitudesAsignadasUiState> = _uiState.asStateFlow()

    fun cargarAsignaciones() {
        val socorristaId = SessionManager.getUserId()
        if (socorristaId.isNullOrBlank()) {
            _uiState.value = SolicitudesAsignadasUiState(
                errorMessage = "Debes iniciar sesión para ver tus solicitudes asignadas"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = api.getAsignacionesSocorrista(socorristaId)
                if (response.isSuccessful) {
                    _uiState.value = SolicitudesAsignadasUiState(
                        asignaciones = response.body()?.asignaciones.orEmpty()
                    )
                } else {
                    _uiState.value = SolicitudesAsignadasUiState(
                        errorMessage = "No se pudieron cargar las solicitudes (HTTP ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SolicitudesAsignadasUiState(
                    errorMessage = e.message ?: "Error de red"
                )
            }
        }
    }
}
