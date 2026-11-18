package com.example.cauca_resilente_app.ui.ViewModels

// ui/viewmodel/AuthViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.RetrofitInstance
import com.example.cauca_resilente_app.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado de la UI de autenticación
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val tipoDocumento: String = "CC",
    val identificacion: String = "",
    val ciudad: String = "",
    val tipoSangre: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val usuarioLogueado: String? = null,
    val entidad : String? = null
)



class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ===== ACTUALIZACIÓN DE CAMPOS =====
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, errorMessage = null)
    }

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre, errorMessage = null)
    }

    fun onTelefonoChange(telefono: String) {
        _uiState.value = _uiState.value.copy(telefono = telefono, errorMessage = null)
    }

    fun onTipoDocumentoChange(tipoDocumento: String) {
        _uiState.value = _uiState.value.copy(tipoDocumento = tipoDocumento, errorMessage = null)
    }

    fun onIdentificacionChange(identificacion: String) {
        _uiState.value = _uiState.value.copy(identificacion = identificacion, errorMessage = null)
    }

    fun onCiudadChange(ciudad: String) {
        _uiState.value = _uiState.value.copy(ciudad = ciudad, errorMessage = null)
    }

    fun onTipoSangreChange(tipoSangre: String) {
        _uiState.value = _uiState.value.copy(tipoSangre = tipoSangre, errorMessage = null)
    }

    fun onEntidadChange(entidad: String) {
        _uiState.value = _uiState.value.copy(entidad = entidad, errorMessage = null)
    }

    // ===== VALIDACIONES =====
    private fun esEmailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mensajeError(response: retrofit2.Response<*>): String? {
        val cuerpo = response.errorBody()?.string() ?: return null
        return runCatching {
            val json = org.json.JSONObject(cuerpo)
            val mensaje = json.optString("mensaje").takeIf { it.isNotBlank() }
            val detalle = json.optString("error").takeIf { it.isNotBlank() }
            when {
                mensaje != null && detalle != null -> "$mensaje ($detalle)"
                mensaje != null -> mensaje
                detalle != null -> detalle
                else -> null
            }
        }.getOrNull()
    }

    private fun nombreNormalizado(): String =
        _uiState.value.nombre.trim().replace(Regex("\\s+"), " ")

    private fun emailNormalizado(): String =
        _uiState.value.email.trim().lowercase()

    private fun telefonoNormalizado(): String =
        _uiState.value.telefono.replace(Regex("[^0-9+]"), "")

    private fun tipoDocumentoCodigo(): String =
        _uiState.value.tipoDocumento
            .substringBefore("-")
            .trim()
            .uppercase()
            .takeIf { it.isNotBlank() }
            ?: "CC"

    private fun validarLogin(): String? {
        val state = _uiState.value
        return when {
            state.email.isBlank() -> "El correo electrónico es obligatorio"
            !esEmailValido(state.email.trim()) -> "El correo electrónico no es válido"
            state.password.isBlank() -> "La contraseña es obligatoria"
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    private fun validarRegistro(): String? {
        val state = _uiState.value
        return when {
            state.nombre.isBlank() -> "El nombre es obligatorio"
            state.telefono.isBlank() -> "El teléfono es obligatorio"
            telefonoNormalizado().length < 10 -> "El teléfono debe tener al menos 10 dígitos"
            state.email.isBlank() -> "El correo electrónico es obligatorio"
            !esEmailValido(state.email.trim()) -> "El correo electrónico no es válido"
            state.password.isBlank() -> "La contraseña es obligatoria"
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            state.password != state.confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    private fun validarRegistroSocorrista(): String? {
        val state = _uiState.value
        return when {
            state.nombre.isBlank() -> "El nombre es obligatorio"
            state.tipoDocumento.isBlank() -> "El tipo de documento es obligatorio"
            state.identificacion.isBlank() -> "La identificación es obligatoria"
            state.identificacion.length < 6 -> "La identificación no es válida"
            state.ciudad.isBlank() -> "La ciudad es obligatoria"
            state.tipoSangre.isBlank() -> "El tipo de sangre es obligatorio"
            state.telefono.isBlank() -> "El teléfono es obligatorio"
            telefonoNormalizado().length < 10 -> "El teléfono debe tener al menos 10 dígitos"
            state.email.isBlank() -> "El correo electrónico es obligatorio"
            !esEmailValido(state.email.trim()) -> "El correo electrónico no es válido"
            state.password.isBlank() -> "La contraseña es obligatoria"
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            state.password != state.confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    // ===== LOGIN =====
    fun login() {
        val error = validarLogin()
        if (error != null) {
            _uiState.value = _uiState.value.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = RetrofitInstance.apiService.login(
                    com.example.cauca_resilente_app.api.LoginRequest(
                        emailNormalizado(), _uiState.value.password
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    SessionManager.saveSession(body.token, body.user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isAuthenticated = true,
                        usuarioLogueado = body.user.name
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, errorMessage = "Credenciales inválidas"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, errorMessage = e.message ?: "Error de red"
                )
            }
        }
    }

    // ===== REGISTRO =====
    fun registrar() {
        val error = validarRegistro()
        if (error != null) {
            _uiState.value = _uiState.value.copy(errorMessage = error)
            return
        }

        enviarRegistro(
            com.example.cauca_resilente_app.api.RegisterRequest(
                name = nombreNormalizado(),
                email = emailNormalizado(),
                phone = telefonoNormalizado(),
                password = _uiState.value.password,
                rol = "usuario"
            )
        )
    }

    fun registrarSocorrista() {
        val error = validarRegistroSocorrista()
        if (error != null) {
            _uiState.value = _uiState.value.copy(errorMessage = error)
            return
        }

        enviarRegistro(
            com.example.cauca_resilente_app.api.RegisterRequest(
                name = nombreNormalizado(),
                email = emailNormalizado(),
                phone = telefonoNormalizado(),
                password = _uiState.value.password,
                rol = "socorrista",
                tipoDocumento = tipoDocumentoCodigo(),
                identificacion = _uiState.value.identificacion.filter { it.isDigit() },
                ciudad = _uiState.value.ciudad.trim(),
                tipoSangre = _uiState.value.tipoSangre.trim().uppercase()
            )
        )
    }

    private fun enviarRegistro(request: com.example.cauca_resilente_app.api.RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = RetrofitInstance.apiService.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    SessionManager.saveSession(body.token, body.user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isAuthenticated = true,
                        usuarioLogueado = body.user.name
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = mensajeError(response) ?: "Error al registrarse"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, errorMessage = e.message ?: "Error de red"
                )
            }
        }
    }

    // ===== LIMPIAR =====
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetEstado() {
        _uiState.value = AuthUiState()
    }


}