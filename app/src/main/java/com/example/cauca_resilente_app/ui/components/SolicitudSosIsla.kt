package com.example.cauca_resilente_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.cauca_resilente_app.ui.ViewModels.EstadoSosIsla
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun SolicitudSosIsla(
    estado: EstadoSosIsla,
    conOrganismo: Boolean,
    solicitudId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val type = appTypography
    var expandida by remember { mutableStateOf(false) }

    val titulo = when (estado) {
        EstadoSosIsla.EN_ESPERA -> "SOLICITUD SOS ENVIADA"
        EstadoSosIsla.EN_CAMINO -> "SOCORRO EN CAMINO"
        EstadoSosIsla.EN_EL_SITIO -> "SOCORRO EN EL SITIO"
        EstadoSosIsla.FINALIZADO -> "SOLICITUD ATENDIDA"
    }
    val detalle = when (estado) {
        EstadoSosIsla.EN_ESPERA -> "Buscando socorrista u organismo"
        EstadoSosIsla.EN_CAMINO -> if (conOrganismo) {
            "Organismo de socorro en camino"
        } else {
            "Socorrista en camino"
        }
        EstadoSosIsla.EN_EL_SITIO -> "Un socorrista ya está en el lugar"
        EstadoSosIsla.FINALIZADO -> "La emergencia fue atendida"
    }
    val colorEstado = when (estado) {
        EstadoSosIsla.EN_ESPERA -> AppDesign.Warning
        EstadoSosIsla.EN_CAMINO -> AppDesign.Info
        EstadoSosIsla.EN_EL_SITIO -> AppDesign.Success
        EstadoSosIsla.FINALIZADO -> AppDesign.Success
    }

    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .clip(RoundedCornerShape(30.dp))
            .clickable { expandida = !expandida }
            .animateContentSize(),
        shape = RoundedCornerShape(30.dp),
        color = AppDesign.Heading,
        shadowElevation = 20.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppDesign.Danger),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Emergency,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(colorEstado, size = 6.dp)
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
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solicitud #$solicitudId",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = type.caption,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Ocultar",
                            color = AppDesign.InfoBorder,
                            fontSize = type.action,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDesign.ChipRadius))
                                .clickable { onDismiss() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "El estado se actualiza automáticamente mientras la solicitud esté activa.",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = type.caption,
                        lineHeight = type.captionLineHeight,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
