package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.RetrofitInstance
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.api.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _user = MutableStateFlow(SessionManager.getUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

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

    fun logout() {
        SessionManager.logout()
        _user.value = null
    }

    fun consumirSesionExpirada() {
        _sessionExpired.value = false
    }
}
