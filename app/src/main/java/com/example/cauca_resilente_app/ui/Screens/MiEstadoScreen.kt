package com.example.cauca_resilente_app.ui.Screens

// ui/screens/MiEstadoScreen.kt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.ui.ViewModels.EstadoViewModel
import com.example.cauca_resilente_app.ui.components.*
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

// Enum para tipar los estados (evita strings mágicos)
enum class EstadoUsuario(
    val titulo: String,
    val valorApi : String,
    val color: Color,
    val colorFondo: Color,
    val icono: ImageVector,

) {


    A_SALVO("A salvo", "A salvo", AppDesign.Success, AppDesign.SuccessSoft, Icons.Filled.Check),
    EN_RIESGO("En riesgo", "En riesgo", AppDesign.Warning, AppDesign.WarningSoft, Icons.Filled.Warning),
    ATRAPADO("Atrapado", "Atrapado", AppDesign.Danger, AppDesign.DangerSoft, Icons.Filled.Warning),
    DESAPARECIDO("Desaparecido", "Desaparecido", AppDesign.TextMuted, AppDesign.SoftBg, Icons.Filled.Close);
}

private data class EstadoVisual(
    val etiqueta: String,
    val subtitulo: String,
    val heroTitulo: String,
    val badge: String,
    val icono: ImageVector,
    val color: Color,
    val fondo: Color,
    val texto: Color
)

private fun EstadoUsuario.visual(): EstadoVisual = when (this) {
    EstadoUsuario.A_SALVO -> EstadoVisual(
        etiqueta = "Estoy bien",
        subtitulo = "Tranquilo y seguro",
        heroTitulo = "Estoy a salvo y bien",
        badge = "Estado activo",
        icono = Icons.Filled.CheckCircle,
        color = AppDesign.Success,
        fondo = AppDesign.SuccessSoft,
        texto = AppDesign.SuccessDark
    )

    EstadoUsuario.EN_RIESGO -> EstadoVisual(
        etiqueta = "En riesgo",
        subtitulo = "Peligro o amenaza",
        heroTitulo = "Estoy en riesgo",
        badge = "Estado en riesgo",
        icono = Icons.Filled.Warning,
        color = AppDesign.Warning,
        fondo = AppDesign.WarningSoft,
        texto = AppDesign.WarningDark
    )

    EstadoUsuario.ATRAPADO -> EstadoVisual(
        etiqueta = "Emergencia",
        subtitulo = "Ayuda inmediata",
        heroTitulo = "Necesito ayuda urgente",
        badge = "Emergencia",
        icono = Icons.Filled.Report,
        color = AppDesign.Danger,
        fondo = AppDesign.DangerSoft,
        texto = AppDesign.DangerDark
    )

    EstadoUsuario.DESAPARECIDO -> EstadoVisual(
        etiqueta = "Inactivo",
        subtitulo = "Sin monitoreo / pausado",
        heroTitulo = "Estado inactivo",
        badge = "Sin monitoreo",
        icono = Icons.Filled.Block,
        color = AppDesign.TextMuted,
        fondo = AppDesign.SoftBg,
        texto = AppDesign.TextSoft
    )
}

@Composable
fun MiEstadoScreen(
    onBackClick: () -> Unit,
    viewModel: EstadoViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val usuario = remember { SessionManager.getUser() }

    var nota by rememberSaveable { mutableStateOf("") }
    var compartir by rememberSaveable { mutableStateOf(true) }
    var ultimaActualizacion by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(uiState.mensajeExito, uiState.errorMessage) {
        uiState.mensajeExito?.let {
            ultimaActualizacion = System.currentTimeMillis()
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppDesign.Background,
        topBar = {
            CabeceraMiEstado(
                onBackClick = onBackClick,
                nombre = usuario?.name,
                apellidos = usuario?.apellidos
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            HeroEstado(
                estado = uiState.estadoActual,
                compartir = compartir,
                onCompartirChange = { compartir = it },
                ultimaActualizacion = ultimaActualizacion,
                direccion = usuario?.direccion
            )

            Spacer(modifier = Modifier.height(24.dp))

            SelectorEstado(
                estadoActual = uiState.estadoActual,
                isLoading = uiState.isLoading,
                onSeleccionar = { viewModel.cambiarEstado(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            CirculoProtector(nombreContacto = usuario?.contactoEmergencia?.nombre)

            Spacer(modifier = Modifier.height(24.dp))

            BotonActualizar(
                isLoading = uiState.isLoading,
                onClick = { viewModel.cambiarEstado(uiState.estadoActual) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CabeceraMiEstado(
    onBackClick: () -> Unit,
    nombre: String?,
    apellidos: String?
) {
    val type = appTypography
    val inicial = (nombre?.trim()?.firstOrNull() ?: apellidos?.trim()?.firstOrNull())
        ?.uppercaseChar()
        ?.toString()

    Surface(color = AppDesign.Surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = AppDesign.Heading,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Mi Estado",
                modifier = Modifier.weight(1f),
                fontSize = type.screenTitle,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )

            Box {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = AppDesign.TextSoft
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .size(8.dp)
                        .background(AppDesign.Warning, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppDesign.PrimarySoft)
                    .border(1.dp, AppDesign.PrimaryBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (inicial != null) {
                    Text(
                        text = inicial,
                        color = AppDesign.Primary,
                        fontSize = type.label,
                        fontWeight = FontWeight.Bold
                    )
                } else {
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
}

@Composable
private fun HeroEstado(
    estado: EstadoUsuario,
    compartir: Boolean,
    onCompartirChange: (Boolean) -> Unit,
    ultimaActualizacion: Long,
    direccion: String?
) {
    val type = appTypography
    val visual = estado.visual()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDesign.BannerRadius),
        color = visual.fondo,
        border = BorderStroke(1.dp, visual.color.copy(alpha = 0.35f))
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
                        Box(modifier = Modifier.size(7.dp).background(visual.color, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = visual.badge.uppercase(),
                            fontSize = type.overline,
                            fontWeight = FontWeight.Bold,
                            color = visual.texto
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = visual.heroTitulo,
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
                    text = "Última actualización: ${textoUltimaActualizacion(ultimaActualizacion)}",
                    fontSize = type.bodySmall,
                    color = AppDesign.TextSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = AppDesign.Warning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = direccion?.takeIf { it.isNotBlank() }
                        ?: "Ubicación compartida con tus contactos",
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            CompartirTiempoReal(
                compartir = compartir,
                onCompartirChange = onCompartirChange
            )
        }
    }
}

@Composable
private fun CompartirTiempoReal(
    compartir: Boolean,
    onCompartirChange: (Boolean) -> Unit
) {
    val type = appTypography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDesign.CardRadius))
            .background(AppDesign.Surface)
            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconTile(
            icon = Icons.Filled.Share,
            tint = AppDesign.SuccessDark,
            background = AppDesign.SuccessSoft,
            size = 40.dp,
            iconSize = 20.dp,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Compartir en tiempo real",
                fontSize = type.bodySmall,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            Text(
                text = "Con contactos de confianza",
                fontSize = type.caption,
                color = AppDesign.TextGray
            )
        }

        Switch(
            checked = compartir,
            onCheckedChange = onCompartirChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppDesign.Success,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SelectorEstado(
    estadoActual: EstadoUsuario,
    isLoading: Boolean,
    onSeleccionar: (EstadoUsuario) -> Unit
) {
    val type = appTypography
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seleccionar nuevo estado",
                fontSize = type.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AppDesign.Success
                )
            } else {
                Text(
                    text = "Toca para elegir",
                    fontSize = type.caption,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OpcionEstado(
                    estado = EstadoUsuario.A_SALVO,
                    seleccionado = estadoActual == EstadoUsuario.A_SALVO,
                    onSeleccionar = onSeleccionar,
                    modifier = Modifier.weight(1f)
                )
                OpcionEstado(
                    estado = EstadoUsuario.EN_RIESGO,
                    seleccionado = estadoActual == EstadoUsuario.EN_RIESGO,
                    onSeleccionar = onSeleccionar,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OpcionEstado(
                    estado = EstadoUsuario.ATRAPADO,
                    seleccionado = estadoActual == EstadoUsuario.ATRAPADO,
                    onSeleccionar = onSeleccionar,
                    modifier = Modifier.weight(1f)
                )
                OpcionEstado(
                    estado = EstadoUsuario.DESAPARECIDO,
                    seleccionado = estadoActual == EstadoUsuario.DESAPARECIDO,
                    onSeleccionar = onSeleccionar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OpcionEstado(
    estado: EstadoUsuario,
    seleccionado: Boolean,
    onSeleccionar: (EstadoUsuario) -> Unit,
    modifier: Modifier = Modifier
) {
    val type = appTypography
    val visual = estado.visual()

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(AppDesign.CardRadius))
            .clickable(role = Role.Button) { onSeleccionar(estado) },
        shape = RoundedCornerShape(AppDesign.CardRadius),
        color = if (seleccionado) visual.fondo else AppDesign.Surface,
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) visual.color else AppDesign.Border
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AppIconTile(
                    icon = visual.icono,
                    tint = visual.color,
                    background = if (seleccionado) AppDesign.Surface else visual.fondo,
                    size = 44.dp,
                    iconSize = 22.dp
                )

                Icon(
                    imageVector = if (seleccionado) {
                        Icons.Filled.RadioButtonChecked
                    } else {
                        Icons.Filled.RadioButtonUnchecked
                    },
                    contentDescription = if (seleccionado) "Seleccionado" else "No seleccionado",
                    tint = if (seleccionado) visual.color else AppDesign.TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = visual.etiqueta,
                fontSize = type.label,
                fontWeight = FontWeight.Bold,
                color = if (seleccionado) visual.texto else AppDesign.Heading
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = visual.subtitulo,
                fontSize = type.caption,
                color = if (seleccionado) visual.texto else AppDesign.TextGray,
                lineHeight = type.captionLineHeight
            )
        }
    }
}

@Composable
private fun CirculoProtector(nombreContacto: String?) {
    val type = appTypography
    val nombre = nombreContacto?.trim()?.takeIf { it.isNotBlank() }
    val inicial = nombre?.firstOrNull()?.uppercaseChar()?.toString()

    AppCard(padding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconTile(
                    icon = Icons.Filled.Group,
                    tint = AppDesign.WarningDark,
                    background = AppDesign.WarningSoft,
                    size = 40.dp,
                    iconSize = 20.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Círculo protector",
                    fontSize = type.label,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading
                )
            }

            AppChip(
                text = "Contactos",
                color = AppDesign.WarningDark,
                background = AppDesign.WarningSoft
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (nombre != null) {
                "Se notificará automáticamente a $nombre y tus contactos autorizados."
            } else {
                "Tus contactos autorizados serán notificados automáticamente al actualizar tu estado."
            },
            fontSize = type.bodySmall,
            color = AppDesign.TextGray,
            lineHeight = type.bodySmallLineHeight
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    AvatarContacto(inicial = if (index == 0) inicial else null)
                }
            }

            Text(
                text = "Contactos autorizados",
                fontSize = type.caption,
                fontWeight = FontWeight.Bold,
                color = AppDesign.TextMuted
            )
        }
    }
}

@Composable
private fun AvatarContacto(inicial: String?) {
    val type = appTypography
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(AppDesign.SoftBg, CircleShape)
            .border(1.dp, AppDesign.Border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (inicial != null) {
            Text(
                text = inicial,
                fontSize = type.bodySmall,
                fontWeight = FontWeight.Bold,
                color = AppDesign.TextSoft
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = AppDesign.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(10.dp)
                .background(AppDesign.Success, CircleShape)
                .border(1.5.dp, AppDesign.Surface, CircleShape)
        )
    }
}

@Composable
private fun BotonActualizar(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val type = appTypography
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AppPrimaryButton(
            text = if (isLoading) "Actualizando..." else "Actualizar mi estado",
            icon = if (isLoading) null else Icons.Filled.CheckCircle,
            onClick = onClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = AppDesign.Success,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Tu ubicación y estado están cifrados de extremo a extremo",
                fontSize = type.caption,
                color = AppDesign.TextGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tu estado se actualizará automáticamente cada 10 minutos.",
            fontSize = type.micro,
            color = AppDesign.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

private fun textoUltimaActualizacion(millis: Long): String {
    val minutos = ((System.currentTimeMillis() - millis) / 60000L).coerceAtLeast(0L)

    return when {
        minutos < 1L -> "Hace un momento"
        minutos == 1L -> "Hace 1 minuto"
        minutos < 60L -> "Hace $minutos minutos"
        else -> "Hoy"
    }
}
