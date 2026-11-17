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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cauca_resilente_app.api.User
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.AppSectionHeader
import com.example.cauca_resilente_app.ui.components.FotoPerfil
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun PerfilScreen(
    user: User?,
    onBackClick: () -> Unit,
    onMisContactosClick: () -> Unit,
    onEditarPerfilClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onPrivacidadClick: () -> Unit,
    onAcercaDeClick: () -> Unit,
    onCerrarSesionClick: () -> Unit,
    onNavItemSelected: (Int) -> Unit,
    onSosClick: () -> Unit,
    mostrarSos: Boolean = true
) {
    val type = appTypography
    val nombre = user?.nombreCompleto?.takeIf { it.isNotBlank() } ?: "Usuario"
    val correo = user?.email?.takeIf { it.isNotBlank() } ?: "Sin correo"
    val telefono = user?.phone?.takeIf { it.isNotBlank() } ?: "Sin teléfono"
    val iniciales = nombre.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "UA" }
    val carrera = user?.rol?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "Comunidad UA"
    val contactosActivos = if (user?.contactoEmergencia != null) "1 activo" else "0 activos"

    Scaffold(
        containerColor = AppDesign.Background,
        bottomBar = {
            BottomNavBar(
                selectedItem = 4,
                onItemSelected = onNavItemSelected,
                onSosClick = onSosClick,
                mostrarSos = mostrarSos
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
                title = "Perfil",
                subtitle = "UniAutónoma Segura",
                onBack = onBackClick,
                actionIcon = Icons.Filled.QrCode2,
                onAction = { },
                actionBackground = AppDesign.PrimarySoft,
                actionTint = AppDesign.Primary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ===== TARJETA DE PERFIL =====
                AppCard(padding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            FotoPerfil(
                                foto = user?.foto,
                                iniciales = iniciales,
                                tamano = 72.dp,
                                modifier = Modifier
                                    .border(1.dp, AppDesign.PrimaryBorder, CircleShape)
                                    .clickable { onEditarPerfilClick() }
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(AppDesign.Success)
                                    .border(2.dp, AppDesign.Surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    nombre,
                                    color = AppDesign.Heading,
                                    fontSize = type.label,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AppChip(
                                    text = "UA",
                                    color = AppDesign.Primary,
                                    background = AppDesign.PrimarySoft
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                correo,
                                color = AppDesign.TextGray,
                                fontSize = type.caption,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                telefono,
                                color = AppDesign.TextSoft,
                                fontSize = type.caption,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppChip(
                                    text = carrera,
                                    color = AppDesign.TextSoft,
                                    background = AppDesign.SoftBg
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = AppDesign.TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "Campus La Casona",
                                    color = AppDesign.TextMuted,
                                    fontSize = type.micro,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppDesign.Border)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat(
                            "Contactos",
                            contactosActivos,
                            AppDesign.Surface,
                            AppDesign.Heading,
                            Modifier.weight(1f)
                        )
                        MiniStat(
                            "GPS 100%",
                            "Monitoreado",
                            AppDesign.SuccessSoft,
                            AppDesign.SuccessDark,
                            Modifier.weight(1f)
                        )
                        MiniStat(
                            "Estado",
                            "Protegido",
                            AppDesign.PrimarySoft,
                            AppDesign.Primary,
                            Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppSectionHeader(title = "Gestión y preferencias")

                Spacer(modifier = Modifier.height(12.dp))

                OpcionPerfil(
                    icono = Icons.Filled.Edit,
                    titulo = "Editar perfil",
                    subtitulo = "Foto, nombre completo, teléfono y correo",
                    tint = AppDesign.Primary,
                    fondo = AppDesign.PrimarySoft,
                    onClick = onEditarPerfilClick
                )
                Spacer(modifier = Modifier.height(10.dp))
                OpcionPerfil(
                    icono = Icons.Filled.Groups,
                    titulo = "Mis contactos de emergencia",
                    subtitulo = "Gestionar y notificar en caso de SOS",
                    tint = AppDesign.SuccessDark,
                    fondo = AppDesign.SuccessSoft,
                    onClick = onMisContactosClick
                )
                Spacer(modifier = Modifier.height(10.dp))
                OpcionPerfil(
                    icono = Icons.Filled.Settings,
                    titulo = "Configuración y preferencias",
                    subtitulo = "Alertas, tono de alarma y vibración",
                    tint = AppDesign.WarningDark,
                    fondo = AppDesign.WarningSoft,
                    onClick = onConfiguracionClick
                )
                Spacer(modifier = Modifier.height(10.dp))
                OpcionPerfil(
                    icono = Icons.Filled.Shield,
                    titulo = "Privacidad y permisos",
                    subtitulo = "Ubicación en 2do plano y telemetría",
                    tint = AppDesign.Primary,
                    fondo = AppDesign.PrimarySoft,
                    onClick = onPrivacidadClick
                )
                Spacer(modifier = Modifier.height(10.dp))
                OpcionPerfil(
                    icono = Icons.Filled.Info,
                    titulo = "Acerca de la app y Soporte UA",
                    subtitulo = "Versión 2.4.0 • UniAutónoma del Cauca",
                    tint = AppDesign.TextSoft,
                    fondo = AppDesign.SoftBg,
                    onClick = onAcercaDeClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Cerrar sesión
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(AppDesign.DangerSoft)
                        .border(1.dp, AppDesign.DangerBorder, RoundedCornerShape(AppDesign.CardRadius))
                        .clickable { onCerrarSesionClick() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconTile(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        tint = AppDesign.Danger,
                        background = AppDesign.Surface,
                        size = 44.dp,
                        iconSize = 20.dp,
                        shape = RoundedCornerShape(13.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Cerrar sesión",
                            color = AppDesign.DangerDark,
                            fontSize = type.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Finalizar sesión en este dispositivo",
                            color = AppDesign.Danger,
                            fontSize = type.caption,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppDesign.Danger,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    bg: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val type = appTypography
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, AppDesign.Border, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label.uppercase(),
            color = AppDesign.TextMuted,
            fontSize = type.micro,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            color = valueColor,
            fontSize = type.caption,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OpcionPerfil(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    tint: Color,
    fondo: Color,
    onClick: () -> Unit
) {
    val type = appTypography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDesign.CardRadius))
            .background(AppDesign.Surface)
            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconTile(
            icon = icono,
            tint = tint,
            background = fondo,
            size = 44.dp,
            iconSize = 22.dp,
            shape = RoundedCornerShape(13.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                titulo,
                color = AppDesign.Heading,
                fontSize = type.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitulo,
                color = AppDesign.TextGray,
                fontSize = type.caption,
                fontWeight = FontWeight.Medium,
                lineHeight = type.captionLineHeight
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppDesign.TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
