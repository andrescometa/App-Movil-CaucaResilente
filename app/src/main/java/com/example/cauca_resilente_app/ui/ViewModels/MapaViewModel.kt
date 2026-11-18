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

data class FocoSolicitudMapa(
    val latitud: Double,
    val longitud: Double,
    val titulo: String,
    val descripcion: String,
    val foto: String? = null,
    val iniciales: String? = null
)

data class MapaUiState(
    val filtroSeleccionado: FiltroMapa = FiltroMapa.TODOS,
    val zonas: List<ZonaRiesgoMapa> = emptyList(),
    val solicitudes: List<SolicitudMapa> = emptyList(),
    val usuarios: List<MapPoint> = emptyList(),
    val alertas: List<MapPoint> = emptyList(),  // ← nuevo
    val focoSolicitud: FocoSolicitudMapa? = null,
    val focoSolicitudVersion: Int = 0,
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

    fun enfocarSolicitud(
        latitud: Double,
        longitud: Double,
        titulo: String,
        descripcion: String,
        foto: String? = null,
        iniciales: String? = null
    ) {
        val estado = _uiState.value
        _uiState.value = estado.copy(
            filtroSeleccionado = FiltroMapa.TODOS,
            focoSolicitud = FocoSolicitudMapa(
                latitud = latitud,
                longitud = longitud,
                titulo = titulo,
                descripcion = descripcion,
                foto = foto,
                iniciales = iniciales
            ),
            focoSolicitudVersion = estado.focoSolicitudVersion + 1
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
                    val usuariosDeferred = async { api.getMapPoints() }
                    val alertasDeferred = async { api.getAlertasMapa() }
                    val solicitudesDeferred = if (esSocorrista) {
                        async { api.getSolicitudesMapa() }
                    } else null

                    val zonasResp = zonasDeferred.await()
                    val usuariosResp = usuariosDeferred.await()
                    val alertasResp = alertasDeferred.await()
                    val solicitudesResp = solicitudesDeferred?.await()

                    val falloSolicitudes = solicitudesResp != null && !solicitudesResp.isSuccessful
                    if (!zonasResp.isSuccessful || !usuariosResp.isSuccessful ||
                        !alertasResp.isSuccessful || falloSolicitudes) {
                        val errores = listOfNotNull(
                            if (!zonasResp.isSuccessful) "zonas (${zonasResp.code()})" else null,
                            if (!usuariosResp.isSuccessful) "usuarios (${usuariosResp.code()})" else null,
                            if (!alertasResp.isSuccessful) "alertas (${alertasResp.code()})" else null,
                            if (falloSolicitudes) "solicitudes (${solicitudesResp?.code()})" else null
                        ).joinToString(", ")

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No se pudieron cargar los datos del mapa: $errores"
                        )
                    } else {
                        // /api/map/points devuelve alertas, zonas y usuarios en una
                        // sola lista; solo los puntos de tipo "usuario" deben ir a
                        // los contactos del mapa para no pintar zonas como personas.
                        val soloUsuarios = usuariosResp.body().orEmpty()
                            .filter { punto -> punto.type.trim().equals("usuario", ignoreCase = true) }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            zonas = zonasResp.body()?.zonas.orEmpty(),
                            solicitudes = solicitudesResp?.body()?.solicitudes.orEmpty(),
                            usuarios = soloUsuarios,
                            alertas = alertasResp.body().orEmpty()   // ← nuevo
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
