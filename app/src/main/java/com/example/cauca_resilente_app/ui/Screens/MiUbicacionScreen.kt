package com.example.cauca_resilente_app.ui.Screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.location.obtenerDireccion
import com.example.cauca_resilente_app.location.obtenerUbicacionActual
import com.example.cauca_resilente_app.location.tienePermisoUbicacion
import com.example.cauca_resilente_app.ui.ViewModels.RegistroUbicacion
import com.example.cauca_resilente_app.ui.ViewModels.UbicacionViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.MapaGoogleView
import com.example.cauca_resilente_app.ui.components.PuntoMapa
import com.example.cauca_resilente_app.ui.components.StatusDot
import com.example.cauca_resilente_app.ui.components.TipoPuntoMapa
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
import com.google.android.gms.maps.model.LatLng

private const val LATITUD_POR_DEFECTO = 2.4448
private const val LONGITUD_POR_DEFECTO = -76.6147

@Composable
fun MiUbicacionScreen(
    onBackClick: () -> Unit,
    onVerEnMapaClick: () -> Unit,
    onVerHistorialCompletoClick: () -> Unit,
    viewModel: UbicacionViewModel,
    onNavItemSelected: (Int) -> Unit = {},
    onSosClick: () -> Unit = {}
) {
    val type = appTypography
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
        containerColor = AppDesign.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selectedItem = 1,
                onItemSelected = onNavItemSelected,
                onSosClick = onSosClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            AppScreenHeader(
                title = "Mi ubicación",
                subtitle = "UniAutónoma Segura • En vivo",
                onBack = onBackClick,
                actionIcon = Icons.Filled.Share,
                onAction = { viewModel.cargarHistorial() },
                actionBackground = AppDesign.SoftBg,
                actionTint = AppDesign.TextSoft
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                AppCard(padding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppChip(
                            text = "Última ubicación",
                            color = AppDesign.WarningDark,
                            background = AppDesign.WarningSoft,
                            dotColor = AppDesign.Warning,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = AppDesign.Primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            AppChip(
                                text = "Precisión GPS",
                                color = AppDesign.SuccessDark,
                                background = AppDesign.SuccessSoft,
                                dotColor = AppDesign.Success
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        uiState.direccionActual,
                        color = AppDesign.Heading,
                        fontSize = type.title,
                        fontWeight = FontWeight.Bold,
                        lineHeight = type.titleLineHeight
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = AppDesign.TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            uiState.fechaActual.ifBlank { "Calculando..." },
                            color = AppDesign.TextMuted,
                            fontSize = type.caption,
                            fontWeight = FontWeight.Medium
                        )
                        Text("  •  ", color = AppDesign.TextMuted, fontSize = type.caption)
                        Text(
                            "Sector Histórico",
                            color = AppDesign.Primary,
                            fontSize = type.caption,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .clip(RoundedCornerShape(AppDesign.CardRadius))
                            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                    ) {
                        MapaPreview(
                            latitud = uiState.latitudActual,
                            longitud = uiState.longitudActual,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(30.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(AppDesign.Surface)
                                .border(1.dp, AppDesign.Border, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = AppDesign.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppDesign.Surface)
                                .border(1.dp, AppDesign.Border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusDot(AppDesign.Primary, pulsing = false, size = 6.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "Campus La Casona (350 m)",
                                color = AppDesign.TextSoft,
                                fontSize = type.micro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AppPrimaryButton(
                        text = "Ver en el mapa",
                        icon = Icons.Filled.Map,
                        onClick = onVerEnMapaClick
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Historial de ubicaciones",
                        color = AppDesign.Heading,
                        fontSize = type.sectionTitle,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppChip(
                        text = "Hoy",
                        color = AppDesign.Primary,
                        background = AppDesign.PrimarySoft
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = null,
                        tint = AppDesign.TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Sincronizado",
                        color = AppDesign.TextMuted,
                        fontSize = type.micro,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when {
                    uiState.isLoading && uiState.historial.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppDesign.Primary)
                        }
                    }
                    uiState.historial.isEmpty() -> {
                        AppCard(padding = 16.dp) {
                            Text(
                                "Todavía no hay ubicaciones registradas.",
                                color = AppDesign.TextMuted,
                                fontSize = type.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    else -> {
                        AppCard(padding = 16.dp) {
                            uiState.historial.forEachIndexed { index, registro ->
                                FilaHistorial(registro, index == 0)
                                if (index != uiState.historial.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(AppDesign.Border)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onVerHistorialCompletoClick() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ver historial completo",
                        color = AppDesign.Primary,
                        fontSize = type.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppDesign.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
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

    MapaGoogleView(
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
        modifier = modifier.background(Color(0xFFE8F0E3))
    )
}

@Composable
private fun FilaHistorial(registro: RegistroUbicacion, actual: Boolean) {
    val type = appTypography
    val colorFondo = if (actual) AppDesign.PrimarySoft else AppDesign.SoftBg
    val colorIcono = if (actual) AppDesign.Primary else AppDesign.WarningDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorFondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (actual) Icons.Filled.LocationOn else Icons.Filled.Business,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    registro.hora.ifBlank { registro.fecha },
                    color = AppDesign.TextMuted,
                    fontSize = type.caption,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                AppChip(
                    text = if (actual) "Parada actual" else "En tránsito",
                    color = if (actual) AppDesign.SuccessDark else AppDesign.WarningDark,
                    background = if (actual) AppDesign.SuccessSoft else AppDesign.WarningSoft
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                registro.direccion,
                color = AppDesign.Heading,
                fontSize = type.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppDesign.TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
