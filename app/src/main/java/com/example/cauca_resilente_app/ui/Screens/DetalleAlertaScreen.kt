package com.example.cauca_resilente_app.ui.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.AlertItem
import com.example.cauca_resilente_app.ui.ViewModels.AlertasViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppOutlineButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DetalleAlertaScreen(
    alertaId: String,
    viewModel: AlertasViewModel,
    onBackClick: () -> Unit,
    onVerEnMapa: (AlertItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val alerta = uiState.alertaSeleccionada

    LaunchedEffect(alertaId) {
        viewModel.cargarAlerta(alertaId)
    }

    Scaffold(containerColor = AppDesign.Background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            AppScreenHeader(
                title = alerta?.title ?: "Detalle de la alerta",
                subtitle = alerta?.let { "Categoría: ${it.category}" } ?: "Información oficial",
                onBack = onBackClick
            )

            when {
                uiState.cargandoDetalle -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppDesign.Primary)
                }

                alerta == null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorDetalle ?: "No se encontró la alerta",
                            color = AppDesign.TextGray,
                            fontSize = appTypography.body,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AppOutlineButton(
                            text = "Reintentar",
                            icon = Icons.Filled.Refresh,
                            onClick = {
                                viewModel.limpiarDetalle()
                                viewModel.cargarAlerta(alertaId)
                            }
                        )
                    }
                }

                else -> DetalleContenido(
                    alerta = alerta,
                    onVerEnMapa = onVerEnMapa,
                    onRecargar = { viewModel.cargarAlerta(alertaId) }
                )
            }
        }
    }
}

@Composable
private fun DetalleContenido(
    alerta: AlertItem,
    onVerEnMapa: (AlertItem) -> Unit,
    onRecargar: () -> Unit
) {
    val type = appTypography
    val estilo = estiloDeAlerta(alerta)
    val uriHandler = LocalUriHandler.current
    val coordenadas = alerta.zonaAfectada
    val latitud = coordenadas?.latitud
    val longitud = coordenadas?.longitud
    val expirada = estaExpirada(alerta.fechaExpiracion)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Encabezado de la alerta con el color según su categoría.
        AppCard(padding = 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(estilo.accent)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppChip(
                            text = alerta.category.uppercase(),
                            color = Color.White,
                            background = estilo.accent,
                            dotColor = Color.White
                        )
                        AppChip(
                            text = if (expirada) "Expirada"
                            else if (alerta.activa == false) "Inactiva"
                            else "Activa",
                            color = if (expirada || alerta.activa == false) AppDesign.Danger
                            else AppDesign.SuccessDark,
                            background = if (expirada || alerta.activa == false) AppDesign.DangerSoft
                            else AppDesign.SuccessSoft
                        )
                    }
                    AppIconTile(
                        icon = estilo.icon,
                        tint = estilo.accent,
                        background = estilo.soft,
                        size = 44.dp,
                        iconSize = 22.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = alerta.title,
                    color = AppDesign.Heading,
                    fontSize = type.title,
                    lineHeight = type.titleLineHeight,
                    fontWeight = FontWeight.Bold
                )

                if (!alerta.nivelSeveridad.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AppChip(
                        text = "Severidad ${alerta.nivelSeveridad}",
                        color = estilo.accent,
                        background = estilo.soft
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = alerta.mensaje
                        ?.takeIf { it.isNotBlank() }
                        ?: "Sin descripción adicional.",
                    color = AppDesign.TextSoft,
                    fontSize = type.body,
                    lineHeight = type.bodyLineHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Datos de vigencia y fuente.
        AppCard {
            Text(
                text = "Detalles de la alerta",
                color = AppDesign.Heading,
                fontSize = type.sectionTitle,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FilaDetalle(
                icon = Icons.Filled.Schedule,
                etiqueta = "Emitida",
                valor = formatearFecha(alerta.fechaEmision)
            )

            if (!alerta.fechaExpiracion.isNullOrBlank()) {
                FilaDetalle(
                    icon = Icons.Filled.Schedule,
                    etiqueta = "Expira",
                    valor = formatearFecha(alerta.fechaExpiracion)
                )
            }

            if (!alerta.fechaActualizacion.isNullOrBlank()) {
                FilaDetalle(
                    icon = Icons.Filled.Refresh,
                    etiqueta = "Última actualización",
                    valor = formatearFecha(alerta.fechaActualizacion)
                )
            }

            if (!alerta.fuente.isNullOrBlank()) {
                FilaDetalle(
                    icon = Icons.Filled.Campaign,
                    etiqueta = "Fuente",
                    valor = alerta.fuente
                )
            }

            if (alerta.radioMetros != null) {
                FilaDetalle(
                    icon = Icons.Filled.Place,
                    etiqueta = "Radio de influencia",
                    valor = "${alerta.radioMetros.toInt()} metros"
                )
            }
        }

        if (latitud != null && longitud != null) {
            Spacer(modifier = Modifier.height(14.dp))
            AppCard {
                Text(
                    text = "Zona afectada",
                    color = AppDesign.Heading,
                    fontSize = type.sectionTitle,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Latitud ${"%.5f".format(latitud)} · Longitud ${"%.5f".format(longitud)}",
                    color = AppDesign.TextGray,
                    fontSize = type.bodySmall,
                    lineHeight = type.bodySmallLineHeight
                )

                if (alerta.radioMetros != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cobertura aproximada de ${alerta.radioMetros.toInt()} metros.",
                        color = AppDesign.TextGray,
                        fontSize = type.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                AppOutlineButton(
                    text = "Ver en el mapa",
                    icon = Icons.Filled.Explore,
                    onClick = { onVerEnMapa(alerta) }
                )
            }
        }

        val url = alerta.urlOficial?.trim().orEmpty()
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Spacer(modifier = Modifier.height(14.dp))
            AppCard {
                Text(
                    text = "Enlace oficial",
                    color = AppDesign.Heading,
                    fontSize = type.sectionTitle,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = url,
                    color = AppDesign.TextGray,
                    fontSize = type.bodySmall,
                    lineHeight = type.bodySmallLineHeight
                )
                Spacer(modifier = Modifier.height(14.dp))
                AppOutlineButton(
                    text = "Abrir fuente oficial",
                    icon = Icons.Filled.OpenInNew,
                    onClick = { runCatching { uriHandler.openUri(url) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        AppOutlineButton(
            text = "Actualizar información",
            icon = Icons.Filled.Refresh,
            onClick = onRecargar
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FilaDetalle(
    icon: ImageVector,
    etiqueta: String,
    valor: String
) {
    val type = appTypography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(AppDesign.TileRadius))
                .background(AppDesign.SoftBg)
                .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.TileRadius)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppDesign.TextSoft,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = etiqueta,
                color = AppDesign.TextMuted,
                fontSize = type.caption,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valor,
                color = AppDesign.Heading,
                fontSize = type.bodySmall,
                fontWeight = FontWeight.SemiBold,
                lineHeight = type.bodySmallLineHeight
            )
        }
    }
}

private data class EstiloAlerta(
    val accent: Color,
    val soft: Color,
    val icon: ImageVector
)

private fun estiloDeAlerta(alerta: AlertItem): EstiloAlerta = when (alerta.category.lowercase()) {
    "alerta" -> EstiloAlerta(
        accent = AppDesign.Danger,
        soft = AppDesign.DangerSoft,
        icon = Icons.Filled.Warning
    )
    "recomendación", "recomendacion" -> EstiloAlerta(
        accent = AppDesign.SuccessDark,
        soft = AppDesign.SuccessSoft,
        icon = Icons.Filled.Explore
    )
    else -> EstiloAlerta(
        accent = AppDesign.WarningDark,
        soft = AppDesign.WarningSoft,
        icon = Icons.Filled.Campaign
    )
}

private val formatoFecha: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, h:mm a", Locale("es", "CO"))

private fun formatearFecha(valor: String?): String {
    if (valor.isNullOrBlank()) return "Sin fecha"
    return runCatching {
        OffsetDateTime.parse(valor).atZoneSameInstant(ZoneId.systemDefault()).format(formatoFecha)
    }.recoverCatching {
        Instant.parse(valor).atZone(ZoneId.systemDefault()).format(formatoFecha)
    }.recoverCatching {
        LocalDateTime.parse(valor).format(formatoFecha)
    }.getOrDefault(valor)
}

private fun estaExpirada(valor: String?): Boolean {
    if (valor.isNullOrBlank()) return false
    val fecha = runCatching { OffsetDateTime.parse(valor).toInstant() }
        .recoverCatching { Instant.parse(valor) }
        .recoverCatching { LocalDateTime.parse(valor).atZone(ZoneId.systemDefault()).toInstant() }
        .getOrNull() ?: return false
    return fecha.isBefore(Instant.now())
}
