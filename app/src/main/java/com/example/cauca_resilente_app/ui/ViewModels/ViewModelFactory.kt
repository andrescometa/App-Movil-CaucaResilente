package com.example.cauca_resilente_app.ui.ViewModels

// ui/viewmodel/ViewModelFactory.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cauca_resilente_app.api.CaucaResilienteApiService

class ViewModelFactory(private  val api: CaucaResilienteApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EstadoViewModel::class.java) ->
                EstadoViewModel(api) as T
            modelClass.isAssignableFrom(SolicitarAyudaViewModel::class.java) ->
                SolicitarAyudaViewModel(api) as T
            modelClass.isAssignableFrom(AlertasViewModel::class.java) ->
                AlertasViewModel(api) as T
            modelClass.isAssignableFrom(MapaViewModel::class.java) ->
                MapaViewModel(api) as T
            modelClass.isAssignableFrom(UbicacionViewModel::class.java) ->
                UbicacionViewModel(api) as T
            modelClass.isAssignableFrom(ContactosViewModel::class.java) ->
                ContactosViewModel(api) as T
            modelClass.isAssignableFrom(SolicitudesAsignadasViewModel::class.java) ->
                SolicitudesAsignadasViewModel(api) as T
            modelClass.isAssignableFrom(NotificacionSocorristaViewModel::class.java) ->
                NotificacionSocorristaViewModel(api) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}