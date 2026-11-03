package com.example.cauca_resilente_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cauca_resilente_app.api.CaucaResilienteApiService
import com.example.cauca_resilente_app.api.ContactoEmergencia
import com.example.cauca_resilente_app.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactosUiState(
    val contactos: List<ContactoEmergencia> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = api.getContactosEmergencia(usuarioId)
                if (response.isSuccessful) {
                    _uiState.value = ContactosUiState(
                        contactos = response.body()?.contactos.orEmpty()
                    )
                } else {
                    _uiState.value = ContactosUiState(
                        errorMessage = "No se pudieron cargar los contactos (HTTP ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ContactosUiState(
                    errorMessage = e.message ?: "Error de red"
                )
            }
        }
    }
}
