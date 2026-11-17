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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.AlertItem
import com.example.cauca_resilente_app.ui.AlertRefreshBus
import com.example.cauca_resilente_app.ui.ViewModels.AlertasViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.StatusDot
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

private val tabsInfo = listOf("Noticias", "Alertas", "Consejos")
private val categorias = listOf("Noticia", "Alerta", "Recomendación")

@Composable
fun InformacionAlertasScreen(
    onBackClick: () -> Unit,
    onVerMasClick: (AlertItem) -> Unit,
    viewModel: AlertasViewModel,
    tabInicial: Int = 0,
    onNavItemSelected: (Int) -> Unit = {},
    onSosClick: () -> Unit = {}
) {
    val type = appTypography
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tabSeleccionada by remember { mutableIntStateOf(tabInicial) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    // Refresco periódico para mostrar alertas nuevas mientras la pantalla está abierta.
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
            viewModel.cargarAlertas()
        }
    }

    LaunchedEffect(Unit) {
        AlertRefreshBus.events.collect {
            viewModel.cargarAlertas()
        }
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
                title = "Información y alertas",
                subtitle = "UniAutónoma Segura",
                onBack = onBackClick,
                actionIcon = Icons.Filled.FilterList,
                onAction = { viewModel.cargarAlertas() },
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

                // Tabs segmentados
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(AppDesign.ChipBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabsInfo.forEachIndexed { index, titulo ->
                        val selected = tabSeleccionada == index
                        val count = uiState.alertas.count { it.category.equals(categorias[index], ignoreCase = true) }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) AppDesign.Surface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (selected) AppDesign.Border else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { tabSeleccionada = index }
                                .padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                titulo,
                                color = if (selected) AppDesign.Primary else AppDesign.TextGray,
                                fontSize = type.caption,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (index == 1 && count > 0) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(AppDesign.ChipRadius))
                                        .background(AppDesign.Danger)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        "$count",
                                        color = Color.White,
                                        fontSize = type.micro,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.isLoading && uiState.alertas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppDesign.Primary)
                    }
                } else {
                    val lista = uiState.alertas.filter {
                        it.category.equals(categorias[tabSeleccionada], ignoreCase = true)
                    }

                    if (lista.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val plural = when (categorias[tabSeleccionada]) {
                                    "Noticia" -> "noticias"
                                    "Alerta" -> "alertas"
                                    else -> "recomendaciones"
                                }
                                Text(
                                    text = "No hay $plural activas",
                                    color = AppDesign.TextGray,
                                    fontSize = type.body,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Actualiza para consultar los últimos reportes.",
                                    color = AppDesign.TextMuted,
                                    fontSize = type.caption
                                )
                            }
                        }
                    } else {
                        lista.forEach { alerta ->
                            val card = alerta.toCard(tabSeleccionada)
                            TarjetaInformativa(card = card, onClick = { onVerMasClick(alerta) })
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AppPrimaryButton(
                    text = "Ver más información y reportes",
                    icon = Icons.Filled.ChevronRight,
                    trailing = true,
                    onClick = {
                        if (tabSeleccionada < tabsInfo.lastIndex) tabSeleccionada++ else viewModel.cargarAlertas()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

private data class CardInfo(
    val etiqueta: String,
    val subtitulo: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val accion: String,
    val accent: Color,
    val tagBg: Color,
    val tagText: Color,
    val iconBg: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val original: AlertItem? = null
)

private fun AlertItem.toCard(tab: Int): CardInfo = when (tab) {
    1 -> CardInfo(
        etiqueta = "Alerta",
        subtitulo = nivelSeveridad?.trim()?.replaceFirstChar { it.uppercase() }?.let { "Riesgo $it" }
            ?: "Riesgo Alto",
        titulo = title,
        descripcion = description.ifBlank { "Consulta los canales oficiales de prevención." },
        fecha = listOf(date, time).filter { it.isNotBlank() }.joinToString(" • "),
        accion = "Protocolo",
        accent = AppDesign.Danger,
        tagBg = AppDesign.Danger,
        tagText = Color.White,
        iconBg = AppDesign.DangerSoft,
        iconTint = AppDesign.Danger,
        icon = Icons.Filled.Warning,
        original = this
    )
    2 -> CardInfo(
        etiqueta = "Prevención",
        subtitulo = "Ruta habilitada",
        titulo = title,
        descripcion = description.ifBlank { "Sigue las recomendaciones de las brigadas de emergencia." },
        fecha = listOf(date, time).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Hoy • 9:00 a. m." },
        accion = "Ver mapa",
        accent = AppDesign.Success,
        tagBg = AppDesign.Success,
        tagText = Color.White,
        iconBg = AppDesign.SuccessSoft,
        iconTint = AppDesign.SuccessDark,
        icon = Icons.Filled.Explore,
        original = this
    )
    else -> CardInfo(
        etiqueta = "Aviso",
        subtitulo = "Departamental",
        titulo = title,
        descripcion = description.ifBlank { "Mantente atento a los comunicados institucionales." },
        fecha = listOf(date, time).filter { it.isNotBlank() }.joinToString(" • "),
        accion = "Leer",
        accent = AppDesign.Warning,
        tagBg = AppDesign.Warning,
        tagText = Color(0xFF0F172A),
        iconBg = AppDesign.WarningSoft,
        iconTint = AppDesign.WarningDark,
        icon = Icons.Filled.Campaign,
        original = this
    )
}

@Composable
private fun TarjetaInformativa(card: CardInfo, onClick: () -> Unit) {
    val type = appTypography
    AppCard(padding = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(card.accent)
        )
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppChip(
                        text = card.etiqueta.uppercase(),
                        color = card.tagText,
                        background = card.tagBg,
                        dotColor = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppChip(
                        text = card.subtitulo,
                        color = card.tagBg,
                        background = card.tagBg.copy(alpha = 0.10f)
                    )
                }
                AppIconTile(
                    icon = card.icon,
                    tint = card.iconTint,
                    background = card.iconBg,
                    size = 44.dp,
                    iconSize = 22.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                card.titulo,
                color = AppDesign.Heading,
                fontSize = type.label,
                fontWeight = FontWeight.Bold,
                lineHeight = type.labelLineHeight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                card.descripcion,
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
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = AppDesign.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    card.fecha.ifBlank { "Ahora" },
                    color = AppDesign.TextMuted,
                    fontSize = type.caption,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    card.accion,
                    color = card.accent,
                    fontSize = type.caption,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = card.accent,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
