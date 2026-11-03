package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.CrearUbicacionRequest
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.api.UbicacionRegistro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RegistroUbicacion(
    val hora: String,
    val fecha: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

data class UbicacionUiState(
    val direccionActual: String = "Obteniendo ubicación...",
    val fechaActual: String = "",
    val latitudActual: Double? = null,
    val longitudActual: Double? = null,
    val historial: List<RegistroUbicacion> = emptyList(),
    val isLoading: Boolean = false,
    val guardando: Boolean = false,
    val errorMessage: String? = null
)

class UbicacionViewModel(private val api: CaucaResilienteApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(UbicacionUiState())
    val uiState: StateFlow<UbicacionUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    fun actualizarUbicacionActual(
        latitud: Double,
        longitud: Double,
        direccion: String,
        precisionMetros: Double? = null,
        bateria: Int? = null,
        guardarEnBackend: Boolean = true
    ) {
        val (fecha, hora) = formatearFechaHora(Instant.now().toString())
        _uiState.update {
            it.copy(
                direccionActual = direccion,
                fechaActual = listOf(hora, fecha).filter { valor -> valor.isNotBlank() }.joinToString(" • "),
                latitudActual = latitud,
                longitudActual = longitud,
                errorMessage = null
            )
        }

        if (guardarEnBackend) {
            registrarUbicacion(latitud, longitud, direccion, precisionMetros, bateria)
        }
    }

    fun registrarUbicacion(
        latitud: Double,
        longitud: Double,
        direccion: String,
        precisionMetros: Double? = null,
        bateria: Int? = null
    ) {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Debes iniciar sesión para guardar tu ubicación")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorMessage = null) }
            try {
                val response = api.crearUbicacion(
                    CrearUbicacionRequest(
                        usuarioId = usuarioId,
                        latitud = latitud,
                        longitud = longitud,
                        direccionAproximada = direccion,
                        precisionMetros = precisionMetros,
                        bateria = bateria
                    )
                )

                if (response.isSuccessful) {
                    val registro = response.body()?.ubicacion?.toRegistro()
                    _uiState.update { state ->
                        state.copy(
                            guardando = false,
                            historial = if (registro != null) {
                                listOf(registro) + state.historial
                            } else {
                                state.historial
                            }
                        )
                    }
                    if (registro == null) cargarHistorial()
                } else {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errorMessage = "No se pudo guardar la ubicación (HTTP ${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorMessage = "No se pudo guardar la ubicación: ${e.message ?: "sin conexión"}"
                    )
                }
            }
        }
    }

    fun cargarHistorial() {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = api.getHistorialUbicaciones(usuarioId)
                when {
                    response.isSuccessful -> {
                        val historial = response.body()?.historial.orEmpty().map { it.toRegistro() }
                        _uiState.update { it.copy(isLoading = false, historial = historial) }
                    }
                    response.code() == 404 -> {
                        _uiState.update { it.copy(isLoading = false, historial = emptyList()) }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No se pudo cargar el historial (HTTP ${response.code()})"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el historial: ${e.message ?: "sin conexión"}"
                    )
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

private fun UbicacionRegistro.toRegistro(): RegistroUbicacion {
    val (fecha, hora) = formatearFechaHora(fechaHora)
    val latitudReal = latitud ?: coordenadas?.latitud ?: 0.0
    val longitudReal = longitud ?: coordenadas?.longitud ?: 0.0
    val direccion = direccionAproximada?.takeIf { it.isNotBlank() }
        ?: String.format(Locale.getDefault(), "%.5f, %.5f", latitudReal, longitudReal)
    return RegistroUbicacion(
        hora = hora.ifBlank { fechaHora.orEmpty() },
        fecha = fecha,
        direccion = direccion,
        latitud = latitudReal,
        longitud = longitudReal
    )
}

private fun formatearFechaHora(iso: String?): Pair<String, String> {
    if (iso.isNullOrBlank()) return "" to ""
    return runCatching {
        val zoned = Instant.parse(iso).atZone(ZoneId.systemDefault())
        val locale = Locale("es", "CO")
        val fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale).format(zoned)
        val hora = DateTimeFormatter.ofPattern("h:mm a", locale).format(zoned)
        fecha to hora
    }.getOrDefault(iso to "")
}
