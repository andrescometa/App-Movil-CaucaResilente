package com.example.cauca_resilente_app.ui.Screens

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.ui.ViewModels.SolicitarAyudaViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.StatusDot
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

private data class TipoAyuda(
    val nombre: String,
    val icono: ImageVector,
    val fondo: Color,
    val tint: Color
)

private val tiposDeAyuda = listOf(
    TipoAyuda("Médica", Icons.Filled.MedicalServices, AppDesign.DangerSoft, AppDesign.Danger),
    TipoAyuda("Rescate", Icons.Filled.Emergency, AppDesign.WarningSoft, AppDesign.WarningDark),
    TipoAyuda("Alimento y agua", Icons.Filled.Restaurant, AppDesign.SuccessSoft, AppDesign.SuccessDark),
    TipoAyuda("Refugio", Icons.Filled.Home, AppDesign.PrimarySoft, AppDesign.Primary),
    TipoAyuda("Apoyo psicológico", Icons.Filled.Psychology, AppDesign.InfoSoft, AppDesign.InfoDark),
    TipoAyuda("Otro", Icons.Filled.MoreHoriz, AppDesign.SoftBg, AppDesign.TextSoft)
)

@Composable
fun SolicitarAyudaScreen(
    onBackClick: () -> Unit,
    onSolicitudEnviada: () -> Unit,
    viewModel: SolicitarAyudaViewModel,
    onNavItemSelected: (Int) -> Unit = {},
    onSosClick: () -> Unit = {}
) {
    val type = appTypography
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState.solicitudEnviada) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetEstado()
                onSolicitudEnviada()
            },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = AppDesign.Success,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Solicitud enviada", fontWeight = FontWeight.Bold) },
            text = { Text("Tu solicitud fue enviada correctamente. El personal autorizado se pondrá en contacto contigo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetEstado()
                    onSolicitudEnviada()
                }) {
                    Text("Aceptar", color = AppDesign.Primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Confirmar solicitud SOS") },
            text = { Text("Se enviará una solicitud de tipo \"${uiState.tipoAyuda}\" con gravedad ${uiState.gravedad}. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoConfirmacion = false
                    viewModel.enviarSolicitud()
                }) {
                    Text("Enviar", color = AppDesign.Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = AppDesign.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selectedItem = -1,
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
                title = "Solicitar ayuda",
                subtitle = "UniAutónoma Segura",
                onBack = onBackClick,
                actionIcon = Icons.Filled.Call,
                onAction = { },
                actionBackground = AppDesign.DangerSoft,
                actionTint = AppDesign.Danger
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ===== ENCABEZADO =====
                AppCard(padding = 16.dp) {
                    Text(
                        "Cuéntanos qué necesitas",
                        color = AppDesign.Heading,
                        fontSize = type.title,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Tu solicitud será enviada al personal autorizado y brigadistas más cercanos de forma inmediata.",
                        color = AppDesign.TextGray,
                        fontSize = type.bodySmall,
                        lineHeight = type.bodySmallLineHeight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppDesign.Border)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(AppDesign.Success, size = 7.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = AppDesign.Danger,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "Ubicación actual detectada",
                            color = AppDesign.TextSoft,
                            fontSize = type.caption,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        AppChip(
                            text = "GPS activo",
                            color = AppDesign.SuccessDark,
                            background = AppDesign.SuccessSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Tipo de ayuda",
                        color = AppDesign.Heading,
                        fontSize = type.sectionTitle,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("*", color = AppDesign.Danger, fontSize = type.sectionTitle, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Selección rápida",
                        color = AppDesign.TextMuted,
                        fontSize = type.micro,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                tiposDeAyuda.chunked(2).forEach { fila ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        fila.forEach { tipo ->
                            val seleccionado = uiState.tipoAyuda == tipo.nombre
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(AppDesign.CardRadius))
                                    .background(if (seleccionado) tipo.fondo else AppDesign.Surface)
                                    .border(
                                        if (seleccionado) 2.dp else 1.dp,
                                        if (seleccionado) tipo.tint.copy(alpha = 0.6f) else AppDesign.Border,
                                        RoundedCornerShape(AppDesign.CardRadius)
                                    )
                                    .clickable(enabled = !uiState.isLoading) { viewModel.onTipoAyudaChange(tipo.nombre) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIconTile(
                                    icon = tipo.icono,
                                    tint = tipo.tint,
                                    background = tipo.fondo,
                                    size = 34.dp,
                                    iconSize = 18.dp,
                                    shape = RoundedCornerShape(11.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    tipo.nombre,
                                    color = if (seleccionado) tipo.tint else AppDesign.TextSoft,
                                    fontSize = type.caption,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                    lineHeight = type.captionLineHeight
                                )
                            }
                        }
                        if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Descripción del incidente",
                        color = AppDesign.Heading,
                        fontSize = type.sectionTitle,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("(opcional)", color = AppDesign.TextMuted, fontSize = type.caption)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "${uiState.descripcion.length}/300",
                        color = if (uiState.descripcion.length >= 280) AppDesign.Danger else AppDesign.TextMuted,
                        fontSize = type.caption,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(AppDesign.Surface)
                        .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = uiState.descripcion,
                        onValueChange = viewModel::onDescripcionChange,
                        enabled = !uiState.isLoading,
                        textStyle = TextStyle(
                            color = AppDesign.Heading,
                            fontSize = type.bodySmall,
                            fontWeight = FontWeight.Medium,
                            lineHeight = type.bodySmallLineHeight
                        ),
                        cursorBrush = SolidColor(AppDesign.Primary),
                        modifier = Modifier.fillMaxWidth().height(78.dp),
                        decorationBox = { inner ->
                            if (uiState.descripcion.isEmpty()) {
                                Text(
                                    "Describe brevemente tu situación, referencias visuales o detalles para los rescatistas...",
                                    color = AppDesign.TextMuted,
                                    fontSize = type.bodySmall,
                                    lineHeight = type.bodySmallLineHeight
                                )
                            }
                            inner()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppDesign.Border)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppDesign.SoftBg)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = null,
                                tint = AppDesign.Danger,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Grabar audio",
                                color = AppDesign.TextSoft,
                                fontSize = type.micro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppDesign.SoftBg)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = AppDesign.Primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Foto",
                                color = AppDesign.TextSoft,
                                fontSize = type.micro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = AppDesign.Success,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "Listo",
                            color = AppDesign.SuccessDark,
                            fontSize = type.micro,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Nivel de gravedad",
                        color = AppDesign.Heading,
                        fontSize = type.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    AppChip(
                        text = "Prioridad de respuesta",
                        color = AppDesign.DangerDark,
                        background = AppDesign.DangerSoft
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GravedadCard(
                        gravedad = "Baja",
                        subtitulo = "Preventiva",
                        icono = Icons.Filled.CheckCircle,
                        color = AppDesign.Success,
                        fondo = AppDesign.SuccessSoft,
                        seleccionada = uiState.gravedad == "Baja",
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        onClick = { viewModel.onGravedadChange("Baja") }
                    )
                    GravedadCard(
                        gravedad = "Media",
                        subtitulo = "Urgente",
                        icono = Icons.Filled.Warning,
                        color = AppDesign.Warning,
                        fondo = AppDesign.WarningSoft,
                        seleccionada = uiState.gravedad == "Media",
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        onClick = { viewModel.onGravedadChange("Media") }
                    )
                    GravedadCard(
                        gravedad = "Alta",
                        subtitulo = "Crítica",
                        icono = Icons.Filled.Error,
                        color = AppDesign.Danger,
                        fondo = AppDesign.DangerSoft,
                        seleccionada = uiState.gravedad == "Alta",
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        onClick = { viewModel.onGravedadChange("Alta") }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                AppPrimaryButton(
                    text = "Enviar solicitud SOS",
                    icon = Icons.AutoMirrored.Filled.Send,
                    enabled = !uiState.isLoading && uiState.tipoAyuda.isNotBlank(),
                    containerColor = AppDesign.Danger,
                    onClick = { mostrarDialogoConfirmacion = true }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = AppDesign.Success,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Transmisión cifrada al Centro de Control UA",
                        color = AppDesign.TextMuted,
                        fontSize = type.micro,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun GravedadCard(
    gravedad: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    fondo: Color,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val type = appTypography
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppDesign.CardRadius))
            .background(if (seleccionada) fondo else AppDesign.Surface)
            .border(
                if (seleccionada) 2.dp else 1.dp,
                if (seleccionada) color.copy(alpha = 0.5f) else AppDesign.Border,
                RoundedCornerShape(AppDesign.CardRadius)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            gravedad,
            color = if (seleccionada) color else AppDesign.TextSoft,
            fontSize = type.caption,
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium
        )
        Text(
            subtitulo,
            color = AppDesign.TextMuted,
            fontSize = type.micro,
            fontWeight = FontWeight.Medium
        )
    }
}
