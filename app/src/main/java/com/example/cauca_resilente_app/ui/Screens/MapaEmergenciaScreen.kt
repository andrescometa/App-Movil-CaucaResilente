package com.example.cauca_resilente_app.ui.Screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.location.obtenerUbicacionActual
import com.example.cauca_resilente_app.location.tienePermisoUbicacion
import com.example.cauca_resilente_app.ui.ViewModels.FiltroMapa
import com.example.cauca_resilente_app.ui.ViewModels.MapaViewModel
import com.example.cauca_resilente_app.ui.components.MapaGoogleView
import com.example.cauca_resilente_app.ui.components.PuntoMapa
import com.example.cauca_resilente_app.ui.components.StatusDot
import com.example.cauca_resilente_app.ui.components.TipoPuntoMapa
import com.example.cauca_resilente_app.ui.components.inicialesDe
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay

private const val LATITUD_POR_DEFECTO = 2.4448
private const val LONGITUD_POR_DEFECTO = -76.6147

@Composable
fun MapaEmergenciaScreen(
    onMenuClick: () -> Unit,
    onNavItemSelected: (Int) -> Unit,
    onSosClick: () -> Unit,
    viewModel: MapaViewModel,
    mostrarSos: Boolean = true
) {
    val type = appTypography
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val esSocorrista = SessionManager.getUser()?.esSocorrista == true

    var ubicacionActual by remember { mutableStateOf<LatLng?>(null) }
    var permisoConcedido by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    var centrarEnUbicacion by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoConcedido = resultado.values.any { it } || tienePermisoUbicacion(context)
    }

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
        if (!tienePermisoUbicacion(context)) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Refresco periódico de zonas, alertas y solicitudes mientras el mapa está visible.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            viewModel.cargarDatos()
        }
    }

    LaunchedEffect(permisoConcedido) {
        if (permisoConcedido) {
            obtenerUbicacionActual(context)?.let { location ->
                val punto = LatLng(location.latitude, location.longitude)
                ubicacionActual = punto
                viewModel.actualizarUbicacion(location.latitude, location.longitude)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    val mostrarTodo = uiState.filtroSeleccionado == FiltroMapa.TODOS
    val puntos = buildList {
        // --- Zonas de riesgo ---
        if (mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.ALERTAS) {
            uiState.zonas.forEach { zona ->
                val latitud = zona.latitud
                val longitud = zona.longitud
                if (latitud != null && longitud != null) {
                    add(
                        PuntoMapa(
                            latitud = latitud,
                            longitud = longitud,
                            titulo = zona.nombreZona ?: "Zona de riesgo",
                            descripcion = listOfNotNull(
                                zona.tipoRiesgo,
                                zona.nivelPeligro?.let { "Peligro $it" },
                                zona.descripcion
                            ).joinToString(" • "),
                            tipo = TipoPuntoMapa.ZONA,
                            radioMetros = zona.radioMetros
                        )
                    )
                }
            }
        }

        // --- Alertas de los workers (IDEAM, USGS, Defensoría) ---
        if (mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.ALERTAS) {
            uiState.alertas.forEach { alerta ->
                add(
                    PuntoMapa(
                        latitud = alerta.latitude,
                        longitud = alerta.longitude,
                        titulo = alerta.name ?: "Alerta",
                        descripcion = listOfNotNull(
                            alerta.message,
                            alerta.severity?.let { "Severidad $it" },
                            alerta.source?.takeIf { it.isNotBlank() }?.let { "Fuente: $it" }
                        ).joinToString(" • "),
                        tipo = TipoPuntoMapa.ALERTA,
                        radioMetros = alerta.radius
                    )
                )
            }
        }

        // --- Solicitudes de ayuda (solo socorristas) ---
        if (mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.AYUDA) {
            uiState.solicitudes.forEach { solicitud ->
                val latitud = solicitud.latitud
                val longitud = solicitud.longitud
                if (latitud != null && longitud != null) {
                    add(
                        PuntoMapa(
                            latitud = latitud,
                            longitud = longitud,
                            titulo = "Solicitud: ${solicitud.tipoAyuda ?: "Ayuda"}",
                            descripcion = listOfNotNull(
                                solicitud.descripcionEmergencia,
                                solicitud.nivelPrioridad?.let { "Prioridad $it" },
                                solicitud.estadoSolicitud
                            ).joinToString(" • "),
                            tipo = TipoPuntoMapa.SOLICITUD,
                            foto = solicitud.usuario?.foto,
                            iniciales = inicialesDe(solicitud.usuario?.nombreCompleto)
                        )
                    )
                }
            }
        }

        // --- Usuarios / contactos ---
        if (mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.USUARIOS) {
            uiState.usuarios.forEach { usuario ->
                add(
                    PuntoMapa(
                        latitud = usuario.latitude,
                        longitud = usuario.longitude,
                        titulo = usuario.name ?: "Usuario",
                        descripcion = if (esSocorrista) "Ubicación reportada" else "Contacto de emergencia",
                        tipo = TipoPuntoMapa.USUARIO,
                        foto = usuario.foto,
                        iniciales = inicialesDe(usuario.name)
                    )
                )
            }
        }

        // --- Mi ubicación ---
        val ubicacion = ubicacionActual
        if ((mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.USUARIOS) && ubicacion != null) {
            add(
                PuntoMapa(
                    latitud = ubicacion.latitude,
                    longitud = ubicacion.longitude,
                    titulo = "Mi ubicación",
                    descripcion = "Tu posición actual",
                    tipo = TipoPuntoMapa.USUARIO,
                    foto = SessionManager.getUser()?.foto,
                    iniciales = inicialesDe(SessionManager.getUser()?.nombreCompleto)
                )
            )
        }

        // --- Foco de solicitud asignada (siempre visible para socorristas) ---
        uiState.focoSolicitud?.let { foco ->
            add(
                PuntoMapa(
                    latitud = foco.latitud,
                    longitud = foco.longitud,
                    titulo = foco.titulo,
                    descripcion = foco.descripcion,
                    tipo = TipoPuntoMapa.SOLICITUD,
                    foto = foco.foto,
                    iniciales = foco.iniciales ?: inicialesDe(foco.titulo)
                )
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            MapaGoogleView(
                puntos = puntos,
                centroInicial = LatLng(LATITUD_POR_DEFECTO, LONGITUD_POR_DEFECTO),
                zoomInicial = 13.5,
                mostrarControlesZoom = false,
                centrarEn = ubicacionActual,
                centrarEnSolicitud = uiState.focoSolicitud?.let {
                    LatLng(it.latitud, it.longitud)
                },
                solicitudCentrado = centrarEnUbicacion,
                versionFocoSolicitud = uiState.focoSolicitudVersion,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(AppDesign.CardRadius))
                            .background(AppDesign.Surface)
                            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                            .clickable { onMenuClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Menú",
                            tint = AppDesign.Heading,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDesign.CardRadius))
                                .background(AppDesign.Surface)
                                .border(
                                    1.dp,
                                    AppDesign.Border,
                                    RoundedCornerShape(AppDesign.CardRadius)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Mapa de emergencia",
                                color = AppDesign.Heading,
                                fontSize = type.sectionTitle,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusDot(AppDesign.Success, size = 6.dp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "EN VIVO • RED UA",
                                    color = AppDesign.SuccessDark,
                                    fontSize = type.micro,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(AppDesign.CardRadius))
                            .background(AppDesign.Surface)
                            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                            .clickable { viewModel.cargarDatos() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppDesign.Primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Filtros",
                                tint = AppDesign.Heading,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filtros = if (esSocorrista) FiltroMapa.entries else listOf(FiltroMapa.TODOS, FiltroMapa.USUARIOS, FiltroMapa.ALERTAS)
                    filtros.forEach { filtro ->
                        FiltroChip(
                            titulo = when {
                                !esSocorrista && filtro == FiltroMapa.USUARIOS -> "Contactos"
                                !esSocorrista && filtro == FiltroMapa.ALERTAS -> "Riesgos"
                                else -> filtro.titulo
                            },
                            seleccionado = uiState.filtroSeleccionado == filtro,
                            onClick = { viewModel.cambiarFiltro(filtro) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(AppDesign.ChipRadius))
                        .background(AppDesign.Surface)
                        .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.ChipRadius))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDot(AppDesign.Primary, size = 6.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${uiState.usuarios.size} contactos • ${uiState.zonas.size} zonas • ${uiState.alertas.size} alertas",
                        color = AppDesign.TextSoft,
                        fontSize = type.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Herramientas flotantes
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(8.dp, RoundedCornerShape(AppDesign.CardRadius), spotColor = Color.Black.copy(alpha = 0.18f))
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(AppDesign.CardRadius))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Layers,
                        contentDescription = "Capas",
                        tint = AppDesign.TextSoft,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(8.dp, RoundedCornerShape(AppDesign.CardRadius), spotColor = Color.Black.copy(alpha = 0.18f))
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, AppDesign.PrimaryBorder, RoundedCornerShape(AppDesign.CardRadius))
                        .clickable { centrarEnUbicacion++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = AppDesign.Primary,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            // Barra inferior flotante sobre el mapa
            BottomNavBar(
                selectedItem = 1,
                onItemSelected = onNavItemSelected,
                onSosClick = onSosClick,
                mostrarSos = mostrarSos,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun FiltroChip(
    titulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val type = appTypography
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppDesign.ChipRadius))
            .background(if (seleccionado) AppDesign.Primary else AppDesign.Surface)
            .border(
                1.dp,
                if (seleccionado) AppDesign.Primary else AppDesign.Border,
                RoundedCornerShape(AppDesign.ChipRadius)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = titulo,
            fontSize = type.caption,
            fontWeight = FontWeight.Bold,
            color = if (seleccionado) Color.White else AppDesign.TextSoft
        )
    }
}