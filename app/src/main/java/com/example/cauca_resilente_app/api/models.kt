package com.example.cauca_resilente_app.api

import com.google.gson.annotations.SerializedName

// --- Modelo para el Login (Interfaz 02) ---
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: User
)

// --- Edición del perfil (Interfaz Perfil / Editar perfil) ---
data class ActualizarPerfilRequest(
    @SerializedName("nombres") val nombres: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("email") val email: String
)

data class ActualizarFotoRequest(
    @SerializedName("foto") val foto: String?
)

data class PerfilResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("usuario") val usuario: User? = null
)

// --- Modelo para el Usuario (Interfaz 09 - Perfil) ---
data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("FullName") val FullName : String? = null,
    @SerializedName("phone") val phone: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("apellidos") val apellidos: String? = null,
    @SerializedName("tipo_documento") val tipoDocumento: String? = null,
    @SerializedName("identificacion") val identificacion: String? = null,
    @SerializedName("genero") val genero: String? = null,
    @SerializedName("edad") val edad: Int? = null,
    @SerializedName("ciudad") val ciudad: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("tipo_sangre") val tipoSangre: String? = null,
    @SerializedName("foto") val foto: String? = null,
    @SerializedName("rol") val rol: String? = null,
    @SerializedName("fecha_registro") val fechaRegistro: String? = null,
    @SerializedName("contacto_emergencia") val contactoEmergencia: EmergencyContact? = null
) {

    val nombreCompleto: String
        get() = listOf(name, apellidos.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" ")

    val esSocorrista: Boolean
        get() = rol?.trim()?.lowercase() == "socorrista"
}

// --- Modelo para el Estado del Usuario (Interfaz 05 - Mi Estado) ---
data class StatusRequest(
    @SerializedName("estado") val estado: String, // "A salvo", "En riesgo", "Atrapado", "Desaparecido"
    @SerializedName("mensaje_estado") val mensajeEstado: String? = null
)

data class StatusResponse(
    @SerializedName("mensaje") val mensaje: String = "",
    @SerializedName("estado") val estado: EstadoSeguridad? = null
)

data class EstadoSeguridad(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("mensaje_estado") val mensajeEstado: String? = null,
    @SerializedName("fecha_ultima_actualizacion") val fechaUltimaActualizacion: String? = null
)

// --- Modelo para Solicitar Ayuda (Interfaz 06) ---
data class HelpRequest(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("tipo_ayuda") val tipoAyuda: String,
    @SerializedName("descripcion_emergencia") val descripcionEmergencia: String,
    @SerializedName("nivel_prioridad") val nivelPrioridad: String,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("personas_afectadas") val personasAfectadas: Int? = null,
    @SerializedName("requiere_ambulancia") val requiereAmbulancia: Boolean? = null
)

data class HelpResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("solicitud") val solicitud: SolicitudCreada? = null
)

data class SolicitudCreada(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("tipo_ayuda") val tipoAyuda: String? = null,
    @SerializedName("estado_solicitud") val estadoSolicitud: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null
)

// --- Modelo para el Mapa (Interfaz 04) ---
data class MapPoint(
    @SerializedName("id") val id: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("type") val type: String, // "usuario", "ayuda", "alerta"
    @SerializedName("intensity") val intensity: Int, // Para los círculos de colores (1-10)
    @SerializedName("name") val name: String? = null,
    @SerializedName("photo") val foto: String? = null
)

// --- Modelo para Alertas y Noticias (Interfaz 08) ---
// Coincide con el documento real de la colección "alertas" del backend.
data class AlertItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("tipo_alerta") val tipoAlerta: String? = null,
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("categoria") val categoria: String? = null,
    @SerializedName("nivel_severidad") val nivelSeveridad: String? = null,
    @SerializedName("zona_afectada") val zonaAfectada: CoordenadasGeo? = null,
    @SerializedName("radio_metros") val radioMetros: Double? = null,
    @SerializedName("fuente") val fuente: String? = null,
    @SerializedName("url_oficial") val urlOficial: String? = null,
    @SerializedName("activa") val activa: Boolean? = null,
    @SerializedName("fecha_emision") val fechaEmision: String? = null,
    @SerializedName("fecha_expiracion") val fechaExpiracion: String? = null,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: String? = null
) {
    // Propiedades que usa la interfaz, derivadas del esquema del backend.
    val title: String get() = tipoAlerta?.takeIf { it.isNotBlank() } ?: "Alerta"
    val description: String get() = mensaje.orEmpty()

    // La categoría la define el administrador; si falta, se deduce de la severidad.
    val category: String
        get() = categoria?.takeIf { it.isNotBlank() } ?: when (nivelSeveridad?.trim()?.lowercase()) {
            "crítica", "critica", "urgente" -> "Alerta"
            "preventiva" -> "Recomendación"
            else -> "Noticia"
        }

    val date: String get() = fechaEmision?.substringBefore("T").orEmpty()
    val time: String get() = fechaEmision?.substringAfter("T", "")?.take(5).orEmpty()
}


// --- Request para actualizar ubicación ---
data class LocationRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String
)

data class LocationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: User?
)

// --- Request para registro ---
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String,
    @SerializedName("rol") val rol: String? = null,
    @SerializedName("apellidos") val apellidos: String? = null,
    @SerializedName("tipo_documento") val tipoDocumento: String? = null,
    @SerializedName("identificacion") val identificacion: String? = null,
    @SerializedName("genero") val genero: String? = null,
    @SerializedName("edad") val edad: Int? = null,
    @SerializedName("ciudad") val ciudad: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("tipo_sangre") val tipoSangre: String? = null
)

// --- Contacto de emergencia ---
data class EmergencyContact(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null
)

// --- Contactos de emergencia (colección contactos_emergencia) ---
data class ContactoEmergencia(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("nombre_contacto") val nombre: String? = null,
    @SerializedName("telefono_contacto") val telefono: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null,
    @SerializedName("prioridad") val prioridad: Int? = null,
    @SerializedName("notificado") val notificado: Boolean? = null
)

data class ContactosEmergenciaResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("contactos") val contactos: List<ContactoEmergencia>? = null
)

// --- Modelos para el mapa real (OpenStreetMap) ---
data class CoordenadasGeo(
    @SerializedName("type") val type: String? = null,
    @SerializedName("coordinates") val coordinates: List<Double>? = null
) {
    val latitud: Double? get() = coordinates?.getOrNull(1)
    val longitud: Double? get() = coordinates?.getOrNull(0)
}

data class ZonaRiesgoMapa(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("nombre_zona") val nombreZona: String? = null,
    @SerializedName("tipo_riesgo") val tipoRiesgo: String? = null,
    @SerializedName("nivel_peligro") val nivelPeligro: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("coordenadas_centro") val coordenadasCentro: CoordenadasGeo? = null,
    @SerializedName("radio_metros") val radioMetros: Double? = null,
    @SerializedName("estado_zona") val estadoZona: String? = null,
    @SerializedName("poblacion_estimada") val poblacionEstimada: Int? = null,
    @SerializedName("activa") val activa: Boolean? = null
) {
    val latitud: Double? get() = coordenadasCentro?.latitud
    val longitud: Double? get() = coordenadasCentro?.longitud
}

data class ZonasRiesgoResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("zonas") val zonas: List<ZonaRiesgoMapa>? = null
)

data class SolicitudMapa(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("tipo_ayuda") val tipoAyuda: String? = null,
    @SerializedName("descripcion_emergencia") val descripcionEmergencia: String? = null,
    @SerializedName("nivel_prioridad") val nivelPrioridad: String? = null,
    @SerializedName("estado_solicitud") val estadoSolicitud: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("personas_afectadas") val personasAfectadas: Int? = null,
    @SerializedName("requiere_ambulancia") val requiereAmbulancia: Boolean? = null,
    @SerializedName("fecha_hora") val fechaHora: String? = null,
    @SerializedName("usuario") val usuario: UsuarioSolicitud? = null
)

// --- Solicitudes asignadas al socorrista ---
data class AsignacionSocorrista(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("solicitud_id") val solicitud: SolicitudMapa? = null,
    @SerializedName("estado_atencion") val estadoAtencion: String? = null,
    @SerializedName("fecha_hora_asignacion") val fechaAsignacion: String? = null,
    @SerializedName("fecha_hora_llegada") val fechaLlegada: String? = null,
    @SerializedName("fecha_hora_cierre") val fechaCierre: String? = null,
    @SerializedName("observaciones") val observaciones: String? = null,
    @SerializedName("recursos_utilizados") val recursosUtilizados: List<String>? = null
)

data class AsignacionesResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("asignaciones") val asignaciones: List<AsignacionSocorrista>? = null
)

// --- Seguimiento de una solicitud (para la isla de notificación SOS) ---
data class AsignacionSeguimiento(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("solicitud_id") val solicitudId: String? = null,
    @SerializedName("organismo_id") val organismoId: String? = null,
    @SerializedName("socorrista_usuario_id") val socorristaUsuarioId: String? = null,
    @SerializedName("socorrista_id") val socorristaId: String? = null,
    @SerializedName("estado_atencion") val estadoAtencion: String? = null,
    @SerializedName("fecha_hora_asignacion") val fechaAsignacion: String? = null,
    @SerializedName("fecha_hora_llegada") val fechaLlegada: String? = null,
    @SerializedName("fecha_hora_cierre") val fechaCierre: String? = null
)

data class SeguimientoSolicitudResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("solicitud") val solicitud: SolicitudMapa? = null,
    @SerializedName("asignacion") val asignacion: AsignacionSeguimiento? = null
)

data class SolicitudesMapaResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("solicitudes") val solicitudes: List<SolicitudMapa>? = null
)

// --- Notificación de solicitudes pendientes para el socorrista ---
data class UsuarioSolicitud(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("nombres") val nombres: String? = null,
    @SerializedName("apellidos") val apellidos: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("foto") val foto: String? = null
) {
    val nombreCompleto: String
        get() = listOf(nombres.orEmpty(), apellidos.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Usuario" }
}

data class UbicacionTiempoReal(
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("fecha_hora") val fechaHora: String? = null,
    @SerializedName("precision_metros") val precisionMetros: Double? = null,
    @SerializedName("direccion_aproximada") val direccionAproximada: String? = null
)

data class SolicitudPendienteSocorrista(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("tipo_ayuda") val tipoAyuda: String? = null,
    @SerializedName("descripcion_emergencia") val descripcionEmergencia: String? = null,
    @SerializedName("nivel_prioridad") val nivelPrioridad: String? = null,
    @SerializedName("estado_solicitud") val estadoSolicitud: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("personas_afectadas") val personasAfectadas: Int? = null,
    @SerializedName("requiere_ambulancia") val requiereAmbulancia: Boolean? = null,
    @SerializedName("fecha_hora") val fechaHora: String? = null,
    @SerializedName("usuario") val usuario: UsuarioSolicitud? = null,
    @SerializedName("ubicacion_tiempo_real") val ubicacion: UbicacionTiempoReal? = null
) {
    val latitudReal: Double? get() = ubicacion?.latitud ?: latitud
    val longitudReal: Double? get() = ubicacion?.longitud ?: longitud
}

data class SolicitudesPendientesResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("solicitudes") val solicitudes: List<SolicitudPendienteSocorrista>? = null
)

data class CrearAsignacionRequest(
    @SerializedName("solicitud_id") val solicitudId: String,
    @SerializedName("socorrista_usuario_id") val socorristaUsuarioId: String
)

data class CrearAsignacionResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("asignacion") val asignacion: AsignacionSeguimiento? = null
)

// --- Modelos para el historial de ubicaciones (Interfaz Mi ubicación) ---
data class CrearUbicacionRequest(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("direccion_aproximada") val direccionAproximada: String? = null,
    @SerializedName("precision_metros") val precisionMetros: Double? = null,
    @SerializedName("bateria") val bateria: Int? = null
)

data class UbicacionRegistro(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("usuario_id") val usuarioId: String? = null,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("coordenadas") val coordenadas: CoordenadasGeo? = null,
    @SerializedName("direccion_aproximada") val direccionAproximada: String? = null,
    @SerializedName("precision_metros") val precisionMetros: Double? = null,
    @SerializedName("bateria") val bateria: Int? = null,
    @SerializedName("fecha_hora") val fechaHora: String? = null
)

data class CrearUbicacionResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("ubicacion") val ubicacion: UbicacionRegistro? = null
)

data class HistorialUbicacionesResponse(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("historial") val historial: List<UbicacionRegistro>? = null
)