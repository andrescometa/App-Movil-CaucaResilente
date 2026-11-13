package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.MapPoint
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.api.SolicitudMapa
import com.example.cauca_resilente_app.api.ZonaRiesgoMapa
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

enum class FiltroMapa(val titulo: String) {
    TODOS("Todos"),
    USUARIOS("Usuarios"),
    AYUDA("Ayuda"),
    ALERTAS("Alertas")
}

data class MapaUiState(
    val filtroSeleccionado: FiltroMapa = FiltroMapa.TODOS,
    val zonas: List<ZonaRiesgoMapa> = emptyList(),
    val solicitudes: List<SolicitudMapa> = emptyList(),
    val usuarios: List<MapPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val latitudActual: Double = 2.4448,
    val longitudActual: Double = -76.6147
)

class MapaViewModel(private val api: CaucaResilienteApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    fun cambiarFiltro(filtro: FiltroMapa) {
        _uiState.value = _uiState.value.copy(filtroSeleccionado = filtro)
    }

    fun actualizarUbicacion(latitud: Double, longitud: Double) {
        _uiState.value = _uiState.value.copy(
            latitudActual = latitud,
            longitudActual = longitud
        )
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // El usuario común no debe ver las solicitudes de ayuda de otras
            // personas; su mapa solo muestra zonas y contactos de emergencia.
            val esSocorrista = SessionManager.getUser()?.esSocorrista == true

            try {
                supervisorScope {
                    val zonasDeferred = async { api.getZonasRiesgo() }
                    val usuariosDeferred = async {
                        api.getMapPoints()
                    }
                    val solicitudesDeferred = if (esSocorrista) {
                        async { api.getSolicitudesMapa() }
                    } else {
                        null
                    }

                    val zonasResp = zonasDeferred.await()
                    val usuariosResp = usuariosDeferred.await()
                    val solicitudesResp = solicitudesDeferred?.await()

                    val falloSolicitudes = solicitudesResp != null && !solicitudesResp.isSuccessful
                    if (!zonasResp.isSuccessful || !usuariosResp.isSuccessful || falloSolicitudes) {
                        val errores = listOfNotNull(
                            if (!zonasResp.isSuccessful) "zonas (${zonasResp.code()})" else null,
                            if (!usuariosResp.isSuccessful) "usuarios (${usuariosResp.code()})" else null,
                            if (falloSolicitudes) "solicitudes (${solicitudesResp?.code()})" else null
                        ).joinToString(", ")

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No se pudieron cargar los datos del mapa: $errores"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            zonas = zonasResp.body()?.zonas.orEmpty(),
                            solicitudes = solicitudesResp?.body()?.solicitudes.orEmpty(),
                            usuarios = usuariosResp.body().orEmpty()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el mapa: ${e.message ?: "sin conexión"}"
                )
            }
        }
    }
}
