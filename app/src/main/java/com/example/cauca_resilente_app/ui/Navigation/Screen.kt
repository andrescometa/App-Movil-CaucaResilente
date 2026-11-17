package com.example.cauca_resilente_app.ui.Navigation

// ui/navigation/Screen.kt
sealed class Screen(val route: String) {
    object Bienvenida : Screen("bienvenida")
    object Login : Screen("login")
    object Registro : Screen("registro")
    object RegistroSocorrista : Screen("registro_socorrista")
    object Home : Screen("home")
    object Mapa : Screen("mapa")

    object Alertas : Screen("alertas")
    object DetalleAlerta : Screen("detalle_alerta/{alertaId}") {
        fun crearRuta(alertaId: String): String = "detalle_alerta/$alertaId"
    }

    object Perfil : Screen("perfil")
    object EditarPerfil : Screen("editar_perfil")
    object MisContactos : Screen("mis_contactos")

    object MiEstado : Screen("mi_estado")
    object SolicitarAyuda : Screen("solicitar_ayuda")
    object SolicitudesAsignadas : Screen("solicitudes_asignadas")
    object MiUbicacion : Screen("mi_ubicacion")
    object Informacion : Screen("informacion")


}