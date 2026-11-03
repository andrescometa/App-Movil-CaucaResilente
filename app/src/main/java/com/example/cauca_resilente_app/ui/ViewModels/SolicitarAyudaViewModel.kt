package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.HelpRequest
import com.example.cauca_resilente_app.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SolicitarAyudaUiState(
    val tipoAyuda: String = "",
    val descripcion: String = "",
    val gravedad: String = "Media",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val solicitudEnviada: Boolean = false,
    val idSolicitud: String? = null
)

class SolicitarAyudaViewModel(private val api: CaucaResilienteApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitarAyudaUiState())
    val uiState: StateFlow<SolicitarAyudaUiState> = _uiState.asStateFlow()

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
