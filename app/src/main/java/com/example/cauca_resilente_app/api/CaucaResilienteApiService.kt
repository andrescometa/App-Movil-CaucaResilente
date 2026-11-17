package com.example.cauca_resilente_app.api



import retrofit2.Response
import retrofit2.http.*

interface CaucaResilienteApiService {
    // Autenticación
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    // Usuario

    // endpoint para actualizar el estado del usuario
    @PUT("api/estados/{usuarioId}")
    suspend fun updateStatus(
        @Path("usuarioId") usuarioId: String,
        @Body request: StatusRequest
    ): StatusResponse

    @GET("api/user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<User>

    // ===== EDITAR PERFIL =====
    @PUT("api/usuarios/{id}/perfil")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Path("id") usuarioId: String,
        @Body request: ActualizarPerfilRequest
    ): Response<PerfilResponse>

    @PUT("api/usuarios/{id}/foto")
    suspend fun actualizarFoto(
        @Header("Authorization") token: String,
        @Path("id") usuarioId: String,
        @Body request: ActualizarFotoRequest
    ): Response<PerfilResponse>

    @POST("api/user/location")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body request: LocationRequest
    ): Response<LocationResponse>

    // Emergencias


    // Mapa
    @GET("api/map/points")
    suspend fun getMapPoints(
        @Query("filter") filter: String = "all"
    ): Response<List<MapPoint>>

    // Alertas


    //*********************************************************************************

    // ===== SOLICITAR AYUDA =====
    @POST("api/solicitud")
    suspend fun helpRequest(@Body request: HelpRequest): Response<HelpResponse>

    // ===== ALERTAS E INFORMACIÓN =====
    @GET("api/alerts")
    suspend fun getAlerts(): Response<List<AlertItem>>

    @GET("api/alerts/{id}")
    suspend fun getAlert(@Path("id") id: String): Response<AlertItem>

    // ===== MAPA REAL =====
    @GET("api/zonas-riesgo")
    suspend fun getZonasRiesgo(): Response<ZonasRiesgoResponse>

    @GET("api/solicitud")
    suspend fun getSolicitudesMapa(): Response<SolicitudesMapaResponse>

    @GET("api/solicitud/pendientes")
    suspend fun getSolicitudesPendientes(): Response<SolicitudesPendientesResponse>

    // ===== SOLICITUDES ASIGNADAS AL SOCORRISTA =====
    @POST("api/asignaciones")
    suspend fun crearAsignacion(
        @Body request: CrearAsignacionRequest
    ): Response<CrearAsignacionResponse>

    @GET("api/asignaciones/socorrista/{socorristaId}")
    suspend fun getAsignacionesSocorrista(
        @Path("socorristaId") socorristaId: String
    ): Response<AsignacionesResponse>

    @GET("api/asignaciones/solicitud/{solicitudId}")
    suspend fun getSeguimientoSolicitud(
        @Path("solicitudId") solicitudId: String
    ): Response<SeguimientoSolicitudResponse>

    // ===== CONTACTOS DE EMERGENCIA =====
    @GET("api/contactos/{usuarioId}")
    suspend fun getContactosEmergencia(
        @Path("usuarioId") usuarioId: String
    ): Response<ContactosEmergenciaResponse>

    // ===== HISTORIAL DE UBICACIONES =====
    @POST("api/ubicaciones")
    suspend fun crearUbicacion(@Body request: CrearUbicacionRequest): Response<CrearUbicacionResponse>

    @GET("api/ubicaciones/{usuarioId}")
    suspend fun getHistorialUbicaciones(
        @Path("usuarioId") usuarioId: String
    ): Response<HistorialUbicacionesResponse>



    @POST("api/devices")
    suspend fun registrarToken(@Body request: DeviceTokenRequest): Response<Unit>

    data class DeviceTokenRequest(
        val token: String,
        val topics: List<String>
    )



}