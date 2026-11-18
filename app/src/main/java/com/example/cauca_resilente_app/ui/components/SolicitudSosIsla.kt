package com.example.cauca_resilente_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.DragIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
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
    var expandida by remember(solicitudId) { mutableStateOf(false) }
    var minimizada by remember(solicitudId) { mutableStateOf(false) }

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

    val radio by animateDpAsState(
        targetValue = if (minimizada) 40.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "radioNube"
    )
    val colorFondo by animateColorAsState(
        targetValue = if (minimizada) AppDesign.Danger else AppDesign.Heading,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "fondoNube"
    )
    val elevacion by animateDpAsState(
        targetValue = if (minimizada) 10.dp else 20.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "elevacionNube"
    )

    val forma = RoundedCornerShape(radio)

    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(forma)
            .clickable(enabled = !minimizada) { expandida = !expandida },
        shape = forma,
        color = colorFondo,
        shadowElevation = elevacion
    ) {
        if (minimizada) {
            NubeMinimizada(
                colorEstado = colorEstado,
                onExpandir = {
                    expandida = true
                    minimizada = false
                },
                onDescartar = onDismiss
            )
        } else {
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
                                    .clickable {
                                        expandida = false
                                        minimizada = true
                                    }
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
}

/**
 * Versión compacta de la nube: solo el icono de emergencia dentro de un círculo.
 * Un toque la expande de nuevo; una pulsación larga descarta el aviso.
 */
@Composable
private fun NubeMinimizada(
    colorEstado: Color,
    onExpandir: () -> Unit,
    onDescartar: () -> Unit
) {
    val pulso = rememberInfiniteTransition(label = "pulsoNube")
    val escala by pulso.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaIcono"
    )
    val halo by pulso.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloIcono"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .combinedClickable(
                onClickLabel = "Mostrar solicitud SOS",
                onLongClickLabel = "Descartar aviso",
                onLongClick = onDescartar,
                onClick = onExpandir
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = halo
                    scaleY = halo
                    alpha = (1.35f - halo) / 0.45f * 0.28f
                }
                .background(colorEstado, CircleShape)
        )
        Icon(
            imageVector = Icons.Filled.Emergency,
            contentDescription = "Mostrar solicitud SOS",
            tint = Color.White,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                }
        )
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
            StatusDot(colorEstado, size = 6.dp)
        }
    }
}
