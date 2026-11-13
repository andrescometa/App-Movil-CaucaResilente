package com.example.cauca_resilente_app.ui.Screens

// ui/screens/SolicitudesAsignadasScreen.kt
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.AsignacionSocorrista
import com.example.cauca_resilente_app.ui.ViewModels.SolicitudesAsignadasViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun SolicitudesAsignadasScreen(
    onBackClick: () -> Unit,
    viewModel: SolicitudesAsignadasViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarAsignaciones()
    }

    Scaffold(
        containerColor = AppDesign.Background,
        topBar = {
            AppScreenHeader(
                modifier = Modifier.statusBarsPadding(),
                title = "Solicitudes asignadas",
                subtitle = "UniAutónoma Segura",
                onBack = onBackClick,
                actionIcon = Icons.Filled.Refresh,
                onAction = { viewModel.cargarAsignaciones() },
                actionBackground = AppDesign.SoftBg,
                actionTint = AppDesign.TextSoft
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

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppDesign.Primary)
                    }
                }

                uiState.errorMessage != null -> {
                    Text(
                        uiState.errorMessage!!,
                        fontSize = appTypography.bodySmall,
                        color = AppDesign.Danger
                    )
                }

                uiState.asignaciones.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = AppDesign.TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No tienes solicitudes asignadas por ahora.",
                            fontSize = appTypography.bodySmall,
                            color = AppDesign.TextGray
                        )
                    }
                }

                else -> {
                    uiState.asignaciones.forEachIndexed { index, asignacion ->
                        if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                        AsignacionCard(asignacion)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AsignacionCard(asignacion: AsignacionSocorrista) {
    val type = appTypography
    val solicitud = asignacion.solicitud
    val colorEstado = when (asignacion.estadoAtencion?.lowercase()) {
        "en camino" -> AppDesign.WarningDark
        "en el sitio" -> AppDesign.Primary
        "finalizado" -> AppDesign.SuccessDark
        else -> AppDesign.TextSoft
    }
    val fondoEstado = when (asignacion.estadoAtencion?.lowercase()) {
        "en camino" -> AppDesign.WarningSoft
        "en el sitio" -> AppDesign.PrimarySoft
        "finalizado" -> AppDesign.SuccessSoft
        else -> AppDesign.SoftBg
    }

    AppCard(padding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                solicitud?.tipoAyuda ?: "Solicitud de ayuda",
                fontSize = type.label,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading,
                modifier = Modifier.weight(1f)
            )
            AppChip(
                text = asignacion.estadoAtencion ?: "Asignada",
                color = colorEstado,
                background = fondoEstado
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            solicitud?.descripcionEmergencia ?: "Sin descripción",
            fontSize = type.bodySmall,
            color = AppDesign.TextGray,
            lineHeight = type.bodySmallLineHeight
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            solicitud?.nivelPrioridad?.let { prioridad ->
                AppChip(
                    text = "Prioridad $prioridad",
                    color = when (prioridad.lowercase()) {
                        "crítica", "critica" -> AppDesign.DangerDark
                        "alta" -> AppDesign.WarningDark
                        else -> AppDesign.Primary
                    },
                    background = when (prioridad.lowercase()) {
                        "crítica", "critica" -> AppDesign.DangerSoft
                        "alta" -> AppDesign.WarningSoft
                        else -> AppDesign.PrimarySoft
                    }
                )
            }
            solicitud?.estadoSolicitud?.let { estado ->
                AppChip(
                    text = estado,
                    color = AppDesign.Primary,
                    background = AppDesign.PrimarySoft
                )
            }
        }

        val detalles = listOfNotNull(
            solicitud?.personasAfectadas?.let { "$it persona(s)" },
            solicitud?.requiereAmbulancia?.takeIf { it }?.let { "Requiere ambulancia" },
            asignacion.fechaAsignacion?.take(10)?.takeIf { it.isNotBlank() }
        )
        if (detalles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                detalles.joinToString(" · "),
                fontSize = type.caption,
                color = AppDesign.TextMuted
            )
        }

        asignacion.observaciones?.takeIf { it.isNotBlank() }?.let { observaciones ->
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = AppDesign.Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    observaciones,
                    fontSize = type.caption,
                    color = AppDesign.TextGray,
                    lineHeight = type.captionLineHeight
                )
            }
        }
    }
}
