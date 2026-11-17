package com.example.cauca_resilente_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun SosResultadoDialog(
    enviado: Boolean,
    enCamino: Boolean,
    mensaje: String,
    onDismiss: () -> Unit
) {
    val type = appTypography
    val acento = if (enviado) AppDesign.Success else AppDesign.Danger
    val acentoOscuro = if (enviado) AppDesign.SuccessDark else AppDesign.DangerDark
    val acentoSuave = if (enviado) AppDesign.SuccessSoft else AppDesign.DangerSoft
    val acentoBorde = if (enviado) AppDesign.SuccessBorder else AppDesign.DangerBorder

    val titulo = if (!enviado) {
        "No se pudo enviar"
    } else if (enCamino) {
        "¡Tu ayuda está en camino!"
    } else {
        "¡Solicitud enviada!"
    }

    val descripcion = when {
        !enviado -> mensaje
        enCamino -> "Un socorrista ya fue asignado y se dirige a tu ubicación. Mantén la calma y permanece en un lugar seguro."
        else -> "Recibimos tu solicitud de emergencia. Un socorrista la revisará y se pondrá en camino lo antes posible."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(30.dp),
            color = AppDesign.Surface,
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(acentoSuave)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(acento.copy(alpha = 0.14f))
                            .border(2.dp, acentoBorde, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (enviado) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = acento,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppDesign.ChipRadius))
                            .background(Color.White.copy(alpha = 0.75f))
                            .border(1.dp, acentoBorde, RoundedCornerShape(AppDesign.ChipRadius))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(acento, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (enviado) "SOLICITUD SOS" else "ERROR AL ENVIAR",
                            color = acentoOscuro,
                            fontSize = type.overline,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = titulo,
                        color = AppDesign.Heading,
                        fontSize = type.screenTitle,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (enviado) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDesign.ChipRadius))
                                .background(if (enCamino) AppDesign.InfoSoft else AppDesign.WarningSoft)
                                .border(
                                    1.dp,
                                    if (enCamino) AppDesign.InfoBorder else AppDesign.WarningBorder,
                                    RoundedCornerShape(AppDesign.ChipRadius)
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (enCamino) AppDesign.Info else AppDesign.Warning, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = if (enCamino) "EN CAMINO" else "EN ESPERA DE ATENCIÓN",
                                color = if (enCamino) AppDesign.InfoDark else AppDesign.WarningDark,
                                fontSize = type.micro,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = descripcion,
                        color = AppDesign.TextSoft,
                        fontSize = type.body,
                        lineHeight = type.bodyLineHeight,
                        textAlign = TextAlign.Center
                    )

                    if (enviado) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppDesign.CardRadius))
                                .background(AppDesign.SoftBg)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = AppDesign.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tu ubicación actual fue compartida",
                                color = AppDesign.Heading,
                                fontSize = type.bodySmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppPrimaryButton(
                        text = if (enviado) "Entendido" else "Cerrar",
                        onClick = onDismiss,
                        containerColor = if (enviado) AppDesign.Primary else AppDesign.Danger
                    )
                }
            }
        }
    }
}
