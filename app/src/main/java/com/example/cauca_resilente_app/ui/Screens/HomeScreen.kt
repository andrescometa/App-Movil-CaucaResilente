package com.example.cauca_resilente_app.ui.Screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cauca_resilente_app.api.ContactoEmergencia
import com.example.cauca_resilente_app.api.User
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppSectionHeader
import com.example.cauca_resilente_app.ui.components.MapaGoogleView
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CentroPopayan = LatLng(2.4448, -76.6147)
private val FormatoHora = DateTimeFormatter.ofPattern("h:mm a", Locale("es", "CO"))
private val FormatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "CO"))

private data class EstadoVisualHome(
    val badge: String,
    val titulo: String,
    val icono: ImageVector,
    val color: Color,
    val colorOscuro: Color,
    val fondo: Color,
    val borde: Color
)

private fun EstadoUsuario.visualHome(): EstadoVisualHome = when (this) {
    EstadoUsuario.A_SALVO -> EstadoVisualHome(
        badge = "Estado activo",
        titulo = "Estoy a salvo y bien",
        icono = Icons.Filled.VerifiedUser,
        color = AppDesign.Success,
        colorOscuro = AppDesign.SuccessDark,
        fondo = AppDesign.SuccessSoft,
        borde = AppDesign.SuccessBorder
    )

    EstadoUsuario.EN_RIESGO -> EstadoVisualHome(
        badge = "Estado en riesgo",
        titulo = "Estoy en riesgo",
        icono = Icons.Filled.Warning,
        color = AppDesign.Warning,
        colorOscuro = AppDesign.WarningDark,
        fondo = AppDesign.WarningSoft,
        borde = AppDesign.WarningBorder
    )

    EstadoUsuario.ATRAPADO -> EstadoVisualHome(
        badge = "Emergencia",
        titulo = "Necesito ayuda urgente",
        icono = Icons.Filled.Emergency,
        color = AppDesign.Danger,
        colorOscuro = AppDesign.DangerDark,
        fondo = AppDesign.DangerSoft,
        borde = AppDesign.DangerBorder
    )

    EstadoUsuario.DESAPARECIDO -> EstadoVisualHome(
        badge = "Sin monitoreo",
        titulo = "Estado inactivo",
        icono = Icons.Filled.Block,
        color = AppDesign.TextMuted,
        colorOscuro = AppDesign.TextSoft,
        fondo = AppDesign.SoftBg,
        borde = AppDesign.Border
    )
}

private data class AccionRapida(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val color: Color,
    val fondoTinte: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    user: User?,
    estadoActual: EstadoUsuario,
    contactos: List<ContactoEmergencia> = emptyList(),
    esSocorrista: Boolean = false,
    onSolicitudesAsignadasClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit,
    onMiEstadoClick: () -> Unit,
    onSolicitarAyudaClick: () -> Unit,
    onMiUbicacionClick: () -> Unit,
    onInformacionClick: () -> Unit,
    onVerEnMapaClick: () -> Unit,
    onCirculoProtectorClick: () -> Unit,
    onNavItemSelected: (Int) -> Unit,
    onSosClick: () -> Unit
) {
    val horaActual = remember { LocalTime.now().format(FormatoHora) }
    val fechaActual = remember { LocalDate.now().format(FormatoFecha) }
    val direccion = user?.direccion?.takeIf { it.isNotBlank() }
        ?: "Calle 5 # 3-15, Popayán"

    val acciones = listOf(
        AccionRapida(
            titulo = "Mi estado",
            subtitulo = "Actualizar cómo te encuentras",
            icono = Icons.Filled.HealthAndSafety,
            color = AppDesign.SuccessDark,
            fondoTinte = AppDesign.SuccessSoft,
            onClick = onMiEstadoClick
        ),
        AccionRapida(
            titulo = "Solicitar ayuda",
            subtitulo = "Reporta una emergencia",
            icono = Icons.Filled.NotificationsActive,
            color = AppDesign.Danger,
            fondoTinte = AppDesign.DangerSoft,
            onClick = onSolicitarAyudaClick
        ),
        AccionRapida(
            titulo = "Mi ubicación",
            subtitulo = "Ver tu última posición",
            icono = Icons.Filled.LocationOn,
            color = AppDesign.InfoDark,
            fondoTinte = AppDesign.InfoSoft,
            onClick = onMiUbicacionClick
        ),
        AccionRapida(
            titulo = "Información",
            subtitulo = "Noticias y alertas clave",
            icono = Icons.Filled.Campaign,
            color = AppDesign.WarningDark,
            fondoTinte = AppDesign.WarningSoft,
            onClick = onInformacionClick
        )
    )

    Scaffold(
        containerColor = AppDesign.Background,
        topBar = {
            HomeHeader(
                onNotificacionesClick = onNotificacionesClick,
                onPerfilClick = { onNavItemSelected(4) }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = 0,
                onItemSelected = onNavItemSelected,
                onSosClick = onSosClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            EstadoHeroCard(
                estado = estadoActual,
                direccion = direccion,
                hora = horaActual,
                onCambiarEstadoClick = onMiEstadoClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppSectionHeader(
                title = "Acciones principales",
                badge = "Modo continuo",
                badgeColor = AppDesign.SuccessDark,
                badgeBackground = AppDesign.SuccessSoft
            )

            Spacer(modifier = Modifier.height(12.dp))

            acciones.chunked(2).forEachIndexed { index, fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { accion ->
                        QuickActionCard(
                            accion = accion,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (fila.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (index < acciones.chunked(2).lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (esSocorrista) {
                Spacer(modifier = Modifier.height(16.dp))
                SolicitudesAsignadasCard(onClick = onSolicitudesAsignadasClick)
            }

            Spacer(modifier = Modifier.height(24.dp))

            UbicacionCard(
                direccion = direccion,
                hora = horaActual,
                fecha = fechaActual,
                onVerEnMapaClick = onVerEnMapaClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            CirculoProtectorCard(
                contactos = contactos,
                contactoUsuario = user?.contactoEmergencia?.nombre,
                onManageClick = onCirculoProtectorClick
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    onNotificacionesClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val type = appTypography
    Surface(color = AppDesign.Surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inicio",
                    fontSize = type.screenTitle,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading
                )
                Text(
                    text = "Estamos aquí para apoyarte",
                    fontSize = type.bodySmall,
                    color = AppDesign.TextGray
                )
            }

            Box {
                IconButton(onClick = onNotificacionesClick) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = AppDesign.TextSoft,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(8.dp)
                        .background(AppDesign.Warning, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppDesign.PrimarySoft)
                    .border(1.dp, AppDesign.PrimaryBorder, CircleShape)
                    .clickable { onPerfilClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Perfil",
                    tint = AppDesign.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EstadoHeroCard(
    estado: EstadoUsuario,
    direccion: String,
    hora: String,
    onCambiarEstadoClick: () -> Unit
) {
    val type = appTypography
    val visual = estado.visualHome()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDesign.BannerRadius),
        color = visual.fondo,
        border = androidx.compose.foundation.BorderStroke(1.dp, visual.borde)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AppIconTile(
                    icon = visual.icono,
                    tint = Color.White,
                    background = visual.color,
                    size = 56.dp,
                    iconSize = 28.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PuntoPulsante(visual.color)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = visual.badge.uppercase(),
                            fontSize = type.overline,
                            fontWeight = FontWeight.Bold,
                            color = visual.colorOscuro
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = visual.titulo,
                        fontSize = type.title,
                        fontWeight = FontWeight.Bold,
                        color = AppDesign.Heading
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = visual.color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "Actualizado a las $hora",
                    fontSize = type.bodySmall,
                    color = AppDesign.TextSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PinDrop,
                    contentDescription = null,
                    tint = AppDesign.Warning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = direccion,
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDesign.CardRadius))
                    .background(AppDesign.Surface)
                    .border(1.dp, visual.borde, RoundedCornerShape(AppDesign.CardRadius))
                    .clickable { onCambiarEstadoClick() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = visual.colorOscuro,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Toca para cambiar estado",
                    modifier = Modifier.weight(1f),
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = AppDesign.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PuntoPulsante(color: Color, size: Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "estado-pulso")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "estado-alfa"
    )
    Box(
        modifier = Modifier
            .size(size)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

@Composable
private fun QuickActionCard(
    accion: AccionRapida,
    modifier: Modifier = Modifier
) {
    val type = appTypography
    AppCard(modifier = modifier.clickable { accion.onClick() }, padding = 14.dp) {
        AppIconTile(
            icon = accion.icono,
            tint = accion.color,
            background = accion.fondoTinte,
            size = 44.dp,
            iconSize = 22.dp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = accion.titulo,
            fontSize = type.label,
            fontWeight = FontWeight.Bold,
            color = AppDesign.Heading,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = accion.subtitulo,
            fontSize = type.caption,
            color = AppDesign.TextGray,
            lineHeight = type.captionLineHeight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SolicitudesAsignadasCard(onClick: () -> Unit) {
    val type = appTypography
    AppCard(modifier = Modifier.clickable { onClick() }, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconTile(
                icon = Icons.Filled.Groups,
                tint = AppDesign.SuccessDark,
                background = AppDesign.SuccessSoft,
                size = 44.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Solicitudes asignadas",
                    fontSize = type.label,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading
                )
                Text(
                    text = "Atiende las emergencias que tienes a cargo",
                    fontSize = type.caption,
                    color = AppDesign.TextGray,
                    lineHeight = type.captionLineHeight
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AppDesign.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UbicacionCard(
    direccion: String,
    hora: String,
    fecha: String,
    onVerEnMapaClick: () -> Unit
) {
    val type = appTypography
    AppCard(padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppChip(
                text = "Última ubicación registrada",
                color = AppDesign.WarningDark,
                background = AppDesign.WarningSoft,
                dotColor = AppDesign.Warning
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.ShareLocation,
                contentDescription = null,
                tint = AppDesign.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(134.dp)
                .clip(RoundedCornerShape(AppDesign.CardRadius))
                .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
        ) {
            MapaGoogleView(
                puntos = emptyList(),
                centroInicial = CentroPopayan,
                zoomInicial = 14.0,
                interactivo = false,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
                    .background(AppDesign.Success.copy(alpha = 0.22f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
                    .background(AppDesign.Success, CircleShape)
                    .border(3.dp, AppDesign.Surface, CircleShape)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = CircleShape,
                color = AppDesign.Surface
            ) {
                Text(
                    text = "Precisión GPS ± 4 m",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    fontSize = type.micro,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = direccion,
            fontSize = type.label,
            fontWeight = FontWeight.Bold,
            color = AppDesign.Heading,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$hora • $fecha",
            fontSize = type.caption,
            color = AppDesign.TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppPrimaryButton(
            text = "Ver en el mapa",
            icon = Icons.Filled.Map,
            onClick = onVerEnMapaClick
        )
    }
}

@Composable
private fun CirculoProtectorCard(
    contactos: List<ContactoEmergencia>,
    contactoUsuario: String?,
    onManageClick: () -> Unit
) {
    val type = appTypography
    val nombres = contactos.mapNotNull { it.nombre?.takeIf { nombre -> nombre.isNotBlank() } }
        .ifEmpty { listOfNotNull(contactoUsuario?.takeIf { it.isNotBlank() }) }
    val total = contactos.size.takeIf { it > 0 } ?: nombres.size

    AppCard(padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconTile(
                icon = Icons.Filled.Groups,
                tint = AppDesign.SuccessDark,
                background = AppDesign.SuccessSoft,
                size = 34.dp,
                iconSize = 18.dp,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Círculo protector",
                fontSize = type.label,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading,
                modifier = Modifier.weight(1f)
            )
            AppChip(
                text = if (total == 1) "1 receptor" else "$total receptores",
                color = AppDesign.SuccessDark,
                background = AppDesign.SuccessSoft
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarStack(nombres = nombres)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombres.take(3).joinToString(", ")
                        .ifBlank { "Sin contactos aún" },
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (nombres.isEmpty()) AppDesign.TextMuted else AppDesign.Success,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (nombres.isEmpty()) {
                            "Agrega a tu círculo"
                        } else {
                            "Notificaciones sincronizadas"
                        },
                        fontSize = type.caption,
                        fontWeight = FontWeight.Bold,
                        color = if (nombres.isEmpty()) AppDesign.TextGray else AppDesign.SuccessDark
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppDesign.SoftBg)
                    .border(1.dp, AppDesign.Border, CircleShape)
                    .clickable { onManageClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Gestionar círculo protector",
                    tint = AppDesign.TextSoft,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AvatarStack(nombres: List<String>) {
    val visibles = nombres.take(3)
    if (visibles.isEmpty()) {
        AvatarContacto(nombre = "?", indice = 0)
        return
    }

    Box(
        modifier = Modifier
            .width((40 + (visibles.size - 1) * 28).dp)
            .height(40.dp)
    ) {
        visibles.forEachIndexed { index, nombre ->
            Box(modifier = Modifier.padding(start = (index * 28).dp)) {
                AvatarContacto(nombre = nombre, indice = index)
            }
        }
    }
}

@Composable
private fun AvatarContacto(nombre: String, indice: Int) {
    val type = appTypography
    val fondos = listOf(
        AppDesign.SuccessSoft,
        AppDesign.PrimarySoft,
        AppDesign.WarningSoft
    )
    val tintes = listOf(
        AppDesign.SuccessDark,
        AppDesign.Primary,
        AppDesign.WarningDark
    )

    Box(modifier = Modifier.size(40.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(fondos[indice % 3])
                .border(1.dp, AppDesign.Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicialesDe(nombre),
                fontSize = type.micro,
                fontWeight = FontWeight.Bold,
                color = tintes[indice % 3]
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(11.dp)
                .background(AppDesign.Success, CircleShape)
                .border(2.dp, AppDesign.Surface, CircleShape)
        )
    }
}

private fun inicialesDe(nombre: String): String {
    val partes = nombre.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)

    if (partes.isEmpty()) return "?"

    return partes.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }
}
