package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.CrearAsignacionRequest
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.api.SolicitudPendienteSocorrista
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

data class NotificacionSocorristaUiState(
    val solicitud: SolicitudPendienteSocorrista? = null,
    val asignando: Boolean = false,
    val asignada: Boolean = false,
    val errorAsignacion: String? = null
)

class NotificacionSocorristaViewModel(
    private val api: CaucaResilienteApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionSocorristaUiState())
    val uiState: StateFlow<NotificacionSocorristaUiState> = _uiState.asStateFlow()

    private val solicitudesIgnoradas = mutableSetOf<String>()
    private var pollingJob: Job? = null
    private var usuarioActual: String? = null

    fun iniciar() {
        if (pollingJob?.isActive == true) return
        if (SessionManager.getUser()?.esSocorrista != true) return
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) return

        if (usuarioActual != usuarioId) {
            usuarioActual = usuarioId
            solicitudesIgnoradas.clear()
            _uiState.value = NotificacionSocorristaUiState()
        } else if (_uiState.value.asignada) {
            _uiState.value = NotificacionSocorristaUiState()
        }

        pollingJob = viewModelScope.launch {
            while (isActive) {
                val estado = _uiState.value
                if (!estado.asignada && !estado.asignando) {
                    consultarPendientes()
                }
                delay(INTERVALO_ACTUALIZACION_MS)
            }
        }
    }

    fun detener() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun asignarme() {
        val solicitud = _uiState.value.solicitud ?: return
        val solicitudId = solicitud.id
        val socorristaId = SessionManager.getUserId()

        if (solicitudId.isNullOrBlank() || socorristaId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorAsignacion = "No se pudo identificar la solicitud o tu sesión")
            }
            return
        }

        _uiState.update { it.copy(asignando = true, errorAsignacion = null) }
        viewModelScope.launch {
            try {
                val response = api.crearAsignacion(
                    CrearAsignacionRequest(
                        solicitudId = solicitudId,
                        socorristaUsuarioId = socorristaId
                    )
                )
                if (response.isSuccessful) {
                    _uiState.update { it.copy(asignando = false, asignada = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            asignando = false,
                            errorAsignacion = mensajeDeError(response.code(), response.errorBody()?.string())
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        asignando = false,
                        errorAsignacion = e.message ?: "No se pudo asignar la solicitud"
                    )
                }
            }
        }
    }

    fun descartarActual() {
        val estado = _uiState.value
        val id = estado.solicitud?.id

        if (!estado.asignada && !id.isNullOrBlank()) {
            solicitudesIgnoradas.add(id)
        }

        _uiState.update {
            it.copy(
                solicitud = null,
                asignando = false,
                asignada = false,
                errorAsignacion = null
            )
        }

        if (!estado.asignada) {
            viewModelScope.launch { consultarPendientes() }
        }
    }

    private suspend fun consultarPendientes() {
        try {
            val response = api.getSolicitudesPendientes()
            if (!response.isSuccessful) return

            val pendientes = response.body()?.solicitudes.orEmpty()
                .filterNot { it.id != null && solicitudesIgnoradas.contains(it.id) }

            val actual = _uiState.value.solicitud
            val actualizado = actual?.id?.let { id ->
                pendientes.firstOrNull { it.id == id }
            }

            _uiState.update { estado ->
                when {
                    actualizado != null -> estado.copy(solicitud = actualizado)
                    pendientes.isNotEmpty() -> estado.copy(solicitud = pendientes.first())
                    else -> estado.copy(solicitud = null)
                }
            }
        } catch (_: Exception) {
            // Mantenemos la última solicitud conocida si falla la red.
        }
    }

    private fun mensajeDeError(codigo: Int, cuerpo: String?): String {
        val mensajeBackend = runCatching {
            cuerpo?.let { JSONObject(it).optString("mensaje").takeIf { m -> m.isNotBlank() } }
        }.getOrNull()

        return mensajeBackend ?: "No se pudo asignar la solicitud (HTTP $codigo)"
    }

    private companion object {
        const val INTERVALO_ACTUALIZACION_MS = 8_000L
    }
}
