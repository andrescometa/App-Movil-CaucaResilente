package com.example.cauca_resilente_app.ui.Screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.location.obtenerDireccion
import com.example.cauca_resilente_app.location.obtenerUbicacionActual
import com.example.cauca_resilente_app.location.tienePermisoUbicacion
import com.example.cauca_resilente_app.ui.ViewModels.RegistroUbicacion
import com.example.cauca_resilente_app.ui.ViewModels.UbicacionViewModel
import com.example.cauca_resilente_app.ui.components.MapaLibreView
import com.example.cauca_resilente_app.ui.components.PuntoMapa
import com.example.cauca_resilente_app.ui.components.TipoPuntoMapa
import com.example.cauca_resilente_app.ui.theme.AzulInstitucional
import com.example.cauca_resilente_app.ui.theme.AzulOscuro
import com.example.cauca_resilente_app.ui.theme.FondoClaro
import com.example.cauca_resilente_app.ui.theme.TextoBlanco
import com.example.cauca_resilente_app.ui.theme.TextoOscuro
import org.maplibre.android.geometry.LatLng

private const val LATITUD_POR_DEFECTO = 2.4448
private const val LONGITUD_POR_DEFECTO = -76.6147

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiUbicacionScreen(
    onBackClick: () -> Unit,
    onVerEnMapaClick: () -> Unit,
    onVerHistorialCompletoClick: () -> Unit,
    viewModel: UbicacionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var permisoConcedido by remember { mutableStateOf(tienePermisoUbicacion(context)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoConcedido = resultado.values.any { it } || tienePermisoUbicacion(context)
    }

    LaunchedEffect(Unit) {
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
            val location = obtenerUbicacionActual(context) ?: return@LaunchedEffect
            val direccion = obtenerDireccion(context, location.latitude, location.longitude)
            viewModel.actualizarUbicacionActual(
                latitud = location.latitude,
                longitud = location.longitude,
                direccion = direccion,
                precisionMetros = location.accuracy.toDouble(),
                guardarEnBackend = true
            )
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        containerColor = TextoBlanco,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi ubicación",
                        color = TextoBlanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextoBlanco
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarHistorial() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar historial", tint = TextoBlanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FondoClaro)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Última ubicación registrada",
                            fontSize = 12.sp,
                            color = TextoOscuro.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AzulOscuro,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        uiState.direccionActual,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    if (uiState.fechaActual.isNotBlank()) {
                        Text(
                            uiState.fechaActual,
                            fontSize = 12.sp,
                            color = TextoOscuro.copy(alpha = 0.6f)
                        )
                    }
                    val latitudActual = uiState.latitudActual
                    val longitudActual = uiState.longitudActual
                    if (latitudActual != null && longitudActual != null) {
                        Text(
                            String.format(
                                java.util.Locale.getDefault(),
                                "Lat %.5f • Lng %.5f",
                                latitudActual,
                                longitudActual
                            ),
                            fontSize = 12.sp,
                            color = TextoOscuro.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    MapaPreview(
                        latitud = uiState.latitudActual,
                        longitud = uiState.longitudActual,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onVerEnMapaClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulOscuro)
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver en el mapa", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Historial de ubicaciones",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading && uiState.historial.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AzulOscuro)
                    }
                }

                uiState.historial.isEmpty() -> {
                    Text(
                        "Todavía no hay ubicaciones registradas.",
                        fontSize = 14.sp,
                        color = TextoOscuro.copy(alpha = 0.6f)
                    )
                }

                else -> {
                    uiState.historial.forEach { registro ->
                        FilaHistorial(registro)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onVerHistorialCompletoClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Ver historial completo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulInstitucional
                )
            }
        }
    }
}

@Composable
private fun MapaPreview(
    latitud: Double?,
    longitud: Double?,
    modifier: Modifier = Modifier
) {
    val lat = latitud ?: LATITUD_POR_DEFECTO
    val lng = longitud ?: LONGITUD_POR_DEFECTO

    MapaLibreView(
        puntos = listOf(
            PuntoMapa(
                latitud = lat,
                longitud = lng,
                titulo = "Mi ubicación",
                descripcion = "Tu posición actual",
                tipo = TipoPuntoMapa.USUARIO
            )
        ),
        centroInicial = LatLng(lat, lng),
        zoomInicial = 16.0,
        interactivo = false,
        actualizarCentroConDatos = true,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F0E3))
    )
}

@Composable
private fun FilaHistorial(registro: RegistroUbicacion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FondoClaro, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.LocationOn,
            contentDescription = null,
            tint = AzulOscuro,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                registro.direccion,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextoOscuro
            )
            Text(
                listOf(registro.hora, registro.fecha)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                fontSize = 12.sp,
                color = TextoOscuro.copy(alpha = 0.6f)
            )
        }
    }
}
