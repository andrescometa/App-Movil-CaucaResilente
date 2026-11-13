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

    // ===== MAPA REAL =====
    @GET("api/zonas-riesgo")
    suspend fun getZonasRiesgo(): Response<ZonasRiesgoResponse>

    @GET("api/solicitud")
    suspend fun getSolicitudesMapa(): Response<SolicitudesMapaResponse>

    // ===== SOLICITUDES ASIGNADAS AL SOCORRISTA =====
    @GET("api/asignaciones/socorrista/{socorristaId}")
    suspend fun getAsignacionesSocorrista(
        @Path("socorristaId") socorristaId: String
    ): Response<AsignacionesResponse>

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

}