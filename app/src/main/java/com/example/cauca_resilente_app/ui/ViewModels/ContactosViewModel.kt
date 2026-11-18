package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.ContactoEmergencia
import com.example.cauca_resilente_app.api.CrearContactoRequest
import com.example.cauca_resilente_app.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class ContactosUiState(
    val contactos: List<ContactoEmergencia> = emptyList(),
    val isLoading: Boolean = false,
    val guardando: Boolean = false,
    val errorMessage: String? = null,
    val mensajeExito: String? = null
)

class ContactosViewModel(private val api: CaucaResilienteApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactosUiState())
    val uiState: StateFlow<ContactosUiState> = _uiState.asStateFlow()

    fun cargarContactos() {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) {
            _uiState.value = ContactosUiState(
                errorMessage = "Debes iniciar sesión para ver tus contactos"
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = api.getContactosEmergencia(usuarioId)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            contactos = response.body()?.contactos.orEmpty()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudieron cargar los contactos (HTTP ${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Error de red")
                }
            }
        }
    }

    fun agregarContacto(
        nombre: String,
        telefono: String,
        parentesco: String,
        onCreado: () -> Unit = {}
    ) {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Debes iniciar sesión para vincular contactos") }
            return
        }

        val nombreLimpio = nombre.trim().replace(Regex("\\s+"), " ")
        val telefonoLimpio = telefono.replace(Regex("[^0-9+]"), "")

        val errorLocal = when {
            nombreLimpio.length < 3 -> "Escribe el nombre completo del contacto"
            telefonoLimpio.replace("+", "").length < 7 -> "Escribe un teléfono válido"
            _uiState.value.contactos.size >= MAX_CONTACTOS -> "Puedes vincular máximo $MAX_CONTACTOS contactos"
            _uiState.value.guardando -> "Espera un momento, estamos guardando"
            else -> null
        }
        if (errorLocal != null) {
            _uiState.update { it.copy(errorMessage = errorLocal) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorMessage = null, mensajeExito = null) }
            try {
                val response = api.crearContacto(
                    CrearContactoRequest(
                        usuarioId = usuarioId,
                        nombre = nombreLimpio,
                        telefono = telefonoLimpio,
                        parentesco = parentesco.trim().ifBlank { null },
                        prioridad = _uiState.value.contactos.size + 1
                    )
                )

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensajeExito = "Contacto vinculado correctamente"
                        )
                    }
                    onCreado()
                    cargarContactos()
                } else {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errorMessage = mensajeDeError(response.code(), response.errorBody()?.string())
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorMessage = e.message ?: "No se pudo vincular el contacto"
                    )
                }
            }
        }
    }

    fun eliminarContacto(contactoId: String, onEliminado: () -> Unit = {}) {
        val usuarioId = SessionManager.getUserId()
        if (usuarioId.isNullOrBlank() || contactoId.isBlank()) return
        if (_uiState.value.guardando) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorMessage = null, mensajeExito = null) }
            try {
                val response = api.eliminarContacto(contactoId, usuarioId)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensajeExito = "Contacto eliminado"
                        )
                    }
                    onEliminado()
                    cargarContactos()
                } else {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errorMessage = mensajeDeError(response.code(), response.errorBody()?.string())
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorMessage = e.message ?: "No se pudo eliminar el contacto"
                    )
                }
            }
        }
    }

    fun consumirMensajes() {
        _uiState.update { it.copy(errorMessage = null, mensajeExito = null) }
    }

    private fun mensajeDeError(codigo: Int, cuerpo: String?): String {
        val mensaje = cuerpo?.let {
            runCatching {
                JSONObject(it).optString("mensaje").takeIf { m -> m.isNotBlank() }
            }.getOrNull()
        }
        return mensaje ?: "No se pudo completar la operación (HTTP $codigo)"
    }

    private companion object {
        const val MAX_CONTACTOS = 5
    }
}
