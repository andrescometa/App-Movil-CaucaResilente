package com.example.cauca_resilente_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cauca_resilente_app.api.SolicitudPendienteSocorrista
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SolicitudSocorristaIsla(
    solicitud: SolicitudPendienteSocorrista,
    asignando: Boolean,
    asignada: Boolean,
    marcandoSalvo: Boolean = false,
    errorAsignacion: String?,
    onAsignarme: () -> Unit,
    onMarcarSalvo: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val type = appTypography
    var expandida by remember(solicitud.id) { mutableStateOf(false) }

    val prioridad = solicitud.nivelPrioridad.orEmpty()
    val colorPrioridad = when (prioridad.lowercase()) {
        "crítica", "critica" -> AppDesign.Danger
        "alta" -> AppDesign.Warning
        else -> AppDesign.Info
    }
    val nombreUsuario = solicitud.usuario?.nombreCompleto ?: "Usuario"
    val tipoAyuda = solicitud.tipoAyuda?.takeIf { it.isNotBlank() } ?: "Solicitud de ayuda"
    val titulo = if (asignada) "ASIGNACIÓN CONFIRMADA" else "NUEVA SOLICITUD SOS"
    val detalle = if (asignada) {
        "Estás en camino a atender la emergencia"
    } else {
        "$nombreUsuario · $tipoAyuda"
    }

    Surface(
        modifier = modifier
            .widthIn(max = 372.dp)
            .clip(RoundedCornerShape(30.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(30.dp),
        color = AppDesign.Heading,
        shadowElevation = 20.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandida = !expandida },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (asignada) AppDesign.Success else AppDesign.Danger),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (asignada) {
                            Icons.Filled.AssignmentTurnedIn
                        } else {
                            Icons.Filled.Emergency
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(
                            color = if (asignada) AppDesign.Success else colorPrioridad,
                            size = 6.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = titulo,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = type.micro,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = detalle,
                        color = Color.White,
                        fontSize = type.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Arrastrar aviso",
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(2.dp))

                Icon(
                    imageVector = if (expandida) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = if (expandida) "Contraer" else "Expandir",
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(visible = expandida) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nombreUsuario,
                                color = Color.White,
                                fontSize = type.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            solicitud.usuario?.telefono?.takeIf { it.isNotBlank() }?.let { telefono ->
                                Text(
                                    text = telefono,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = type.caption
                                )
                            }
                        }
                        Text(
                            text = prioridad.ifBlank { "Prioridad" },
                            color = colorPrioridad,
                            fontSize = type.micro,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDesign.ChipRadius))
                                .background(colorPrioridad.copy(alpha = 0.16f))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = AppDesign.InfoBorder,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ubicación en tiempo real",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = type.micro,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = textoUbicacion(solicitud),
                                color = Color.White,
                                fontSize = type.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            solicitud.ubicacion?.direccionAproximada
                                ?.takeIf { it.isNotBlank() && it != textoUbicacion(solicitud) }
                                ?.let { direccion ->
                                    Text(
                                        text = direccion,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = type.caption,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            horaUbicacion(solicitud.ubicacion?.fechaHora)?.let { hora ->
                                Text(
                                    text = "Actualizada $hora",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = type.caption
                                )
                            }
                        }
                    }

                    solicitud.descripcionEmergencia?.takeIf { it.isNotBlank() }?.let { descripcion ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = descripcion,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = type.caption,
                            lineHeight = type.captionLineHeight,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (asignada) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppDesign.ButtonRadius))
                                .background(AppDesign.Success.copy(alpha = 0.18f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = AppDesign.Success,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "La solicitud quedó a tu cargo",
                                color = Color.White,
                                fontSize = type.caption,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppDesign.ButtonRadius))
                                .background(AppDesign.Success)
                                .clickable(enabled = !marcandoSalvo) { onMarcarSalvo() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (marcandoSalvo) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (marcandoSalvo) {
                                        "Marcando a salvo..."
                                    } else {
                                        "Usuario a salvo"
                                    },
                                    color = Color.White,
                                    fontSize = type.action,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppDesign.ButtonRadius))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable(enabled = !marcandoSalvo) { onDismiss() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cerrar aviso",
                                color = Color.White,
                                fontSize = type.action,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAsignarme,
                            enabled = !asignando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(AppDesign.ButtonRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AppDesign.Heading,
                                disabledContainerColor = Color.White.copy(alpha = 0.7f),
                                disabledContentColor = AppDesign.Heading.copy(alpha = 0.6f)
                            )
                        ) {
                            if (asignando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = AppDesign.Heading
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Asignando...",
                                    fontSize = type.label,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Asignarme a esta solicitud",
                                    fontSize = type.label,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        errorAsignacion?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = AppDesign.DangerBorder,
                                fontSize = type.caption,
                                lineHeight = type.captionLineHeight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Ahora no",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = type.action,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppDesign.ChipRadius))
                                    .clickable { onDismiss() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun textoUbicacion(solicitud: SolicitudPendienteSocorrista): String {
    val latitud = solicitud.latitudReal
    val longitud = solicitud.longitudReal
    if (latitud == null || longitud == null) return "Ubicación no disponible"

    val direccion = solicitud.ubicacion?.direccionAproximada?.takeIf { it.isNotBlank() }
    return direccion ?: String.format(Locale.getDefault(), "%.5f, %.5f", latitud, longitud)
}

private fun horaUbicacion(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        Instant.parse(iso)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale("es", "CO")))
    }.getOrNull()
}
