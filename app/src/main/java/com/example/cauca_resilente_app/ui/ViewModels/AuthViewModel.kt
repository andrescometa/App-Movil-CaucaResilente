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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val usuarioLogueado: String? = null
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

    // ===== VALIDACIONES =====
    private fun esEmailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validarLogin(): String? {
        val state = _uiState.value
        return when {
            state.email.isBlank() -> "El correo electrónico es obligatorio"
            !esEmailValido(state.email) -> "El correo electrónico no es válido"
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
            state.telefono.length < 10 -> "El teléfono debe tener al menos 10 dígitos"
            state.email.isBlank() -> "El correo electrónico es obligatorio"
            !esEmailValido(state.email) -> "El correo electrónico no es válido"
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
                        _uiState.value.email, _uiState.value.password
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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = RetrofitInstance.apiService.register(
                    com.example.cauca_resilente_app.api.RegisterRequest(
                        name = _uiState.value.nombre,
                        email = _uiState.value.email,
                        phone = _uiState.value.telefono,
                        password = _uiState.value.password
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
                        isLoading = false, errorMessage = "Error al registrarse"
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