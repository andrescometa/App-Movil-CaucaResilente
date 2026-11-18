package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.CrearUbicacionRequest
import com.example.cauca_resilente_app.api.HelpRequest
import com.example.cauca_resilente_app.api.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class EstadoSosIsla {
    EN_ESPERA,
    EN_CAMINO,
    EN_EL_SITIO,
    FINALIZADO
}

data class SolicitarAyudaUiState(
    val tipoAyuda: String = "",
    val descripcion: String = "",
    val gravedad: String = "Media",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val solicitudEnviada: Boolean = false,
    val idSolicitud: String? = null,
    val enviandoSosRapido: Boolean = false,
    val mensajeSosRapido: String? = null,
    val sosRapidoEnviado: Boolean? = null,
    val sosRapidoEnCamino: Boolean = false,
    val sosSolicitudId: String? = null,
    val estadoSosIsla: EstadoSosIsla = EstadoSosIsla.EN_ESPERA,
    val sosConOrganismo: Boolean = false
)

class SolicitarAyudaViewModel(private val api: CaucaResilienteApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitarAyudaUiState())
    val uiState: StateFlow<SolicitarAyudaUiState> = _uiState.asStateFlow()
    private var seguimientoJob: Job? = null

    fun onTipoAyudaChange(tipo: String) {
        _uiState.value = _uiState.value.copy(tipoAyuda = tipo, errorMessage = null)
    }

    fun onDescripcionChange(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcion = descripcion, errorMessage = null)
    }

    fun onGravedadChange(gravedad: String) {
        _uiState.value = _uiState.value.copy(gravedad = gravedad)
    }

    private fun validar(): String? {
        val state = _uiState.value
        return when {
            state.tipoAyuda.isBlank() -> "Selecciona el tipo de ayuda"
            state.descripcion.isBlank() -> "Describe tu situación"
            state.descripcion.length < 10 -> "La descripción es demasiado corta"
            SessionManager.getUserId().isNullOrBlank() -> "Debes iniciar sesión para enviar la solicitud"
            else -> null
        }
    }

    fun enviarSolicitud() {
        val error = validar()
        if (error != null) {
            _uiState.value = _uiState.value.copy(errorMessage = error)
            return
        }

        val usuarioId = SessionManager.getUserId() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = api.helpRequest(
                    HelpRequest(
                        usuarioId = usuarioId,
                        tipoAyuda = mapearTipoAyuda(_uiState.value.tipoAyuda),
                        descripcionEmergencia = _uiState.value.descripcion.trim(),
                        nivelPrioridad = _uiState.value.gravedad,
                        latitud = LATITUD_POR_DEFECTO,
                        longitud = LONGITUD_POR_DEFECTO
                    )
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        solicitudEnviada = true,
                        idSolicitud = response.body()?.solicitud?.id
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = leerMensajeError(response.code(), response.errorBody()?.string())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudo enviar la solicitud: ${e.message}"
                )
            }
        }
    }

    fun enviarSosRapido(
        latitud: Double,
        longitud: Double,
        precisionMetros: Double? = null
    ) {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                mensajeSosRapido = "Debes iniciar sesión para enviar el SOS",
                sosRapidoEnviado = false
            )
            return
        }
        if (_uiState.value.enviandoSosRapido) return

        guardarUbicacionEnHistorial(usuarioId, latitud, longitud, precisionMetros)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                enviandoSosRapido = true,
                mensajeSosRapido = null,
                sosRapidoEnviado = null,
                sosRapidoEnCamino = false
            )
            try {
                val response = api.helpRequest(
                    HelpRequest(
                        usuarioId = usuarioId,
                        tipoAyuda = "Rescate",
                        descripcionEmergencia = "SOS automático enviado desde el botón de emergencia.",
                        nivelPrioridad = "Alta",
                        latitud = latitud,
                        longitud = longitud
                    )
                )

                if (response.isSuccessful) {
                    val solicitud = response.body()?.solicitud
                    val estado = solicitud?.estadoSolicitud.orEmpty()
                    val enCamino = estaEnCamino(estado)
                    val mensaje = if (enCamino) {
                        "Tu solicitud está en camino"
                    } else {
                        "Tu solicitud está en espera de ser atendida"
                    }
                    _uiState.value = _uiState.value.copy(
                        enviandoSosRapido = false,
                        mensajeSosRapido = mensaje,
                        sosRapidoEnviado = true,
                        sosRapidoEnCamino = enCamino,
                        sosSolicitudId = solicitud?.id,
                        estadoSosIsla = if (enCamino) EstadoSosIsla.EN_CAMINO else EstadoSosIsla.EN_ESPERA,
                        sosConOrganismo = false
                    )
                    solicitud?.id?.let { iniciarSeguimientoSos(it) }
                } else {
                    _uiState.value = _uiState.value.copy(
                        enviandoSosRapido = false,
                        mensajeSosRapido = leerMensajeError(response.code(), response.errorBody()?.string()),
                        sosRapidoEnviado = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    enviandoSosRapido = false,
                    mensajeSosRapido = "No se pudo enviar el SOS: ${e.message}",
                    sosRapidoEnviado = false
                )
            }
        }
    }

    fun actualizarUbicacionSos(
        latitud: Double,
        longitud: Double,
        precisionMetros: Double? = null
    ) {
        val usuarioId = SessionManager.getUserId() ?: return
        guardarUbicacionEnHistorial(usuarioId, latitud, longitud, precisionMetros)
    }

    private fun guardarUbicacionEnHistorial(
        usuarioId: String,
        latitud: Double,
        longitud: Double,
        precisionMetros: Double?
    ) {
        viewModelScope.launch {
            runCatching {
                api.crearUbicacion(
                    CrearUbicacionRequest(
                        usuarioId = usuarioId,
                        latitud = latitud,
                        longitud = longitud,
                        direccionAproximada = String.format(
                            Locale.getDefault(),
                            "%.5f, %.5f",
                            latitud,
                            longitud
                        ),
                        precisionMetros = precisionMetros
                    )
                )
            }
        }
    }

    private fun iniciarSeguimientoSos(solicitudId: String) {
        seguimientoJob?.cancel()
        seguimientoJob = viewModelScope.launch {
            while (isActive) {
                val finalizado = consultarSeguimientoSos(solicitudId)
                if (finalizado) break
                delay(10_000)
            }
        }
    }

    private suspend fun consultarSeguimientoSos(solicitudId: String): Boolean {
        val response = runCatching { api.getSeguimientoSolicitud(solicitudId) }.getOrNull()
            ?: return false
        if (!response.isSuccessful) return false

        val body = response.body() ?: return false
        val asignacion = body.asignacion
        val estadoSolicitud = body.solicitud?.estadoSolicitud
        val nuevoEstado = when {
            asignacion?.estadoAtencion.esFinalizado() == true -> EstadoSosIsla.FINALIZADO
            asignacion?.estadoAtencion.esEnElSitio() == true -> EstadoSosIsla.EN_EL_SITIO
            asignacion != null -> EstadoSosIsla.EN_CAMINO
            estadoSolicitud.esFinalizado() == true -> EstadoSosIsla.FINALIZADO
            estadoSolicitud.esEnCamino() == true -> EstadoSosIsla.EN_CAMINO
            else -> EstadoSosIsla.EN_ESPERA
        }

        val finalizado = nuevoEstado == EstadoSosIsla.FINALIZADO
        if (finalizado) {
            _uiState.value = _uiState.value.copy(
                sosSolicitudId = null,
                estadoSosIsla = EstadoSosIsla.EN_ESPERA,
                sosConOrganismo = false,
                sosRapidoEnCamino = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                estadoSosIsla = nuevoEstado,
                sosRapidoEnCamino = nuevoEstado == EstadoSosIsla.EN_CAMINO,
                sosConOrganismo = !asignacion?.organismoId.isNullOrBlank()
            )
        }
        return finalizado
    }

    fun descartarIslaSos() {
        seguimientoJob?.cancel()
        seguimientoJob = null
        _uiState.value = _uiState.value.copy(
            sosSolicitudId = null,
            estadoSosIsla = EstadoSosIsla.EN_ESPERA,
            sosConOrganismo = false,
            sosRapidoEnCamino = false
        )
    }

    fun notificarSos(mensaje: String) {
        _uiState.value = _uiState.value.copy(
            mensajeSosRapido = mensaje,
            sosRapidoEnviado = false
        )
    }

    fun consumirMensajeSos() {
        _uiState.value = _uiState.value.copy(
            mensajeSosRapido = null,
            sosRapidoEnviado = null,
            sosRapidoEnCamino = false
        )
    }

    private fun estaEnCamino(estado: String): Boolean {
        val estadosEnCamino = listOf("camino", "asignad", "atencion", "atiende", "proceso")
        return estadosEnCamino.any { estado.contains(it, ignoreCase = true) }
    }

    private fun mapearTipoAyuda(tipo: String): String = when (tipo) {
        "Médica" -> "Atención Médica"
        "Alimento y agua" -> "Víveres/Agua"
        "Refugio" -> "Albergue"
        "Apoyo psicológico" -> "Apoyo Logístico"
        "Otro" -> "Apoyo Logístico"
        else -> tipo
    }

    private fun leerMensajeError(codigo: Int, cuerpo: String?): String {
        if (cuerpo.isNullOrBlank()) return "No se pudo enviar la solicitud (HTTP $codigo)"
        return runCatching {
            org.json.JSONObject(cuerpo).optString("mensaje").ifBlank { cuerpo }
        }.getOrDefault(cuerpo)
    }

    fun resetEstado() {
        _uiState.value = SolicitarAyudaUiState()
    }

    companion object {
        private const val LATITUD_POR_DEFECTO = 2.4448
        private const val LONGITUD_POR_DEFECTO = -76.6147
    }
}

private fun String?.normalizado(): String = this?.trim()?.lowercase().orEmpty()

private fun String?.esFinalizado(): Boolean =
    normalizado().let { it.contains("final") || it.contains("atendid") || it.contains("cerrad") }

private fun String?.esEnElSitio(): Boolean = normalizado().contains("sitio")

private fun String?.esEnCamino(): Boolean =
    normalizado().let { it.contains("camino") || it.contains("asignad") || it.contains("proceso") }
