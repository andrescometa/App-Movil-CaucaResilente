package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.ActualizarFotoRequest
import com.example.cauca_resilente_app.api.ActualizarPerfilRequest
import com.example.cauca_resilente_app.api.RetrofitInstance
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.api.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class EditarPerfilUiState(
    val guardando: Boolean = false,
    val errorMessage: String? = null,
    val perfilActualizado: Boolean = false
)

class UserViewModel : ViewModel() {

    private val _user = MutableStateFlow(SessionManager.getUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    private val _editarPerfil = MutableStateFlow(EditarPerfilUiState())
    val editarPerfil: StateFlow<EditarPerfilUiState> = _editarPerfil.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        _user.value = SessionManager.getUser()

        if (!SessionManager.isLoggedIn()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.getUserProfile(
                    SessionManager.getToken().orEmpty()
                )
                if (response.isSuccessful) {
                    response.body()?.let { usuario ->
                        SessionManager.saveUser(usuario)
                        _user.value = usuario
                    }
                } else if (response.code() in 400..499) {
                    logout()
                    _sessionExpired.value = true
                }
            } catch (_: Exception) {
                // Sin conexión: se conserva el usuario guardado en caché
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarPerfil(
        nombres: String,
        apellidos: String,
        telefono: String,
        email: String,
        foto: String? = null
    ) {
        val usuarioId = SessionManager.getUserId() ?: return
        val token = SessionManager.getToken() ?: return
        if (_editarPerfil.value.guardando) return

        validatePerfil(nombres, apellidos, telefono, email)?.let { error ->
            _editarPerfil.value = _editarPerfil.value.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            _editarPerfil.value = EditarPerfilUiState(guardando = true)
            try {
                val response = RetrofitInstance.apiService.actualizarPerfil(
                    token = token,
                    usuarioId = usuarioId,
                    request = ActualizarPerfilRequest(
                        nombres = nombres.trim(),
                        apellidos = apellidos.trim(),
                        telefono = telefono.trim(),
                        email = email.trim().lowercase()
                    )
                )
                val perfil = response.body()?.usuario
                if (!response.isSuccessful || perfil == null) {
                    _editarPerfil.value = EditarPerfilUiState(
                        errorMessage = mensajeDeError(response.code(), response.errorBody()?.string())
                    )
                    return@launch
                }

                var usuarioFinal = perfil
                if (foto != null) {
                    val fotoResponse = RetrofitInstance.apiService.actualizarFoto(
                        token = token,
                        usuarioId = usuarioId,
                        request = ActualizarFotoRequest(foto)
                    )
                    val conFoto = fotoResponse.body()?.usuario
                    if (!fotoResponse.isSuccessful || conFoto == null) {
                        _editarPerfil.value = EditarPerfilUiState(
                            errorMessage = mensajeDeError(
                                fotoResponse.code(),
                                fotoResponse.errorBody()?.string()
                            )
                        )
                        return@launch
                    }
                    usuarioFinal = conFoto
                }

                SessionManager.saveUser(usuarioFinal)
                _user.value = usuarioFinal
                _editarPerfil.value = EditarPerfilUiState(perfilActualizado = true)
            } catch (e: Exception) {
                _editarPerfil.value = EditarPerfilUiState(
                    errorMessage = e.message ?: "No se pudo actualizar el perfil"
                )
            }
        }
    }

    fun consumirResultadoEdicion() {
        _editarPerfil.value = EditarPerfilUiState()
    }

    fun logout() {
        SessionManager.logout()
        _user.value = null
    }

    fun consumirSesionExpirada() {
        _sessionExpired.value = false
    }

    private fun validatePerfil(
        nombres: String,
        apellidos: String,
        telefono: String,
        email: String
    ): String? {
        if (nombres.trim().length < 2) return "El nombre es obligatorio"
        if (telefono.isNotBlank() && !Regex("^[0-9+\\s-]{7,20}$").matches(telefono.trim())) {
            return "El número de teléfono no es válido"
        }
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) {
            return "El correo electrónico no es válido"
        }
        return null
    }

    private fun mensajeDeError(codigo: Int, cuerpo: String?): String {
        val mensajeBackend = runCatching {
            cuerpo?.let { JSONObject(it).optString("mensaje").takeIf { m -> m.isNotBlank() } }
        }.getOrNull()

        return mensajeBackend ?: "No se pudo actualizar (HTTP $codigo)"
    }
}
