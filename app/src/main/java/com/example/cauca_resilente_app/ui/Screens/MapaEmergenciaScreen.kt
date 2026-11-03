package com.example.cauca_resilente_app.ui.Screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.location.obtenerUbicacionActual
import com.example.cauca_resilente_app.location.tienePermisoUbicacion
import com.example.cauca_resilente_app.ui.ViewModels.FiltroMapa
import com.example.cauca_resilente_app.ui.ViewModels.MapaViewModel
import com.example.cauca_resilente_app.ui.components.MapaLibreView
import com.example.cauca_resilente_app.ui.components.PuntoMapa
import com.example.cauca_resilente_app.ui.components.TipoPuntoMapa
import com.example.cauca_resilente_app.ui.theme.AzulOscuro
import com.example.cauca_resilente_app.ui.theme.FondoClaro
import com.example.cauca_resilente_app.ui.theme.TextoBlanco
import com.example.cauca_resilente_app.ui.theme.TextoOscuro
import org.maplibre.android.geometry.LatLng

private const val LATITUD_POR_DEFECTO = 2.4448
private const val LONGITUD_POR_DEFECTO = -76.6147

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaEmergenciaScreen(
    onMenuClick: () -> Unit,
    onNavItemSelected: (Int) -> Unit,
    onSosClick: () -> Unit,
    viewModel: MapaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                            tipo = TipoPuntoMapa.ZONA
                        )
                    )
                }
            }
        }

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
                            tipo = TipoPuntoMapa.SOLICITUD
                        )
                    )
                }
            }
        }

        if (mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.USUARIOS) {
            uiState.usuarios.forEach { usuario ->
                add(
                    PuntoMapa(
                        latitud = usuario.latitude,
                        longitud = usuario.longitude,
                        titulo = usuario.name ?: "Usuario",
                        descripcion = "Ubicación reportada",
                        tipo = TipoPuntoMapa.USUARIO
                    )
                )
            }
        }

        val ubicacion = ubicacionActual
        if ((mostrarTodo || uiState.filtroSeleccionado == FiltroMapa.USUARIOS) && ubicacion != null) {
            add(
                PuntoMapa(
                    latitud = ubicacion.latitude,
                    longitud = ubicacion.longitude,
                    titulo = "Mi ubicación",
                    descripcion = "Tu posición actual",
                    tipo = TipoPuntoMapa.USUARIO
                )
            )
        }
    }

    Scaffold(
        containerColor = FondoClaro,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selectedItem = 1,
                onItemSelected = onNavItemSelected,
                onSosClick = onSosClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapaLibreView(
                puntos = puntos,
                centroInicial = LatLng(LATITUD_POR_DEFECTO, LONGITUD_POR_DEFECTO),
                zoomInicial = 13.5,
                centrarEn = ubicacionActual,
                solicitudCentrado = centrarEnUbicacion,
                modifier = Modifier.fillMaxSize()
            )

            // ===== ENCABEZADO SUPERPUESTO =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AzulOscuro, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú", tint = TextoBlanco)
                    }
                    Text(
                        "Mapa de emergencia",
                        color = TextoBlanco,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TextoBlanco,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = { viewModel.cargarDatos() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = TextoBlanco)
                    }
                    IconButton(onClick = { /* TODO: opciones de filtro avanzado */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtros", tint = TextoBlanco)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FiltroMapa.entries.forEach { filtro ->
                        FiltroChip(
                            filtro = filtro,
                            seleccionado = uiState.filtroSeleccionado == filtro,
                            onClick = { viewModel.cambiarFiltro(filtro) }
                        )
                    }
                }
            }

            // ===== BOTÓN DE UBICACIÓN ACTUAL =====
            FloatingActionButton(
                onClick = { centrarEnUbicacion++ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(52.dp),
                containerColor = AzulOscuro,
                contentColor = TextoBlanco,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Mi ubicación")
            }
        }
    }
}

@Composable
private fun FiltroChip(
    filtro: FiltroMapa,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (seleccionado) AzulOscuro else TextoBlanco,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = filtro.titulo,
            fontSize = 13.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
            color = if (seleccionado) TextoBlanco else TextoOscuro.copy(alpha = 0.7f)
        )
    }
}
