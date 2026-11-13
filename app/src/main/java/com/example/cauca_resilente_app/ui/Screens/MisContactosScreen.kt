package com.example.cauca_resilente_app.ui.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.ui.ViewModels.ContactosViewModel
import com.example.cauca_resilente_app.ui.components.AppBanner
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.AppSectionHeader
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun MisContactosScreen(
    onBackClick: () -> Unit,
    viewModel: ContactosViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarContactos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppDesign.Background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        // ==========================================
        // 1. HEADER
        // ==========================================
        AppScreenHeader(
            title = "Contactos de emergencia",
            onBack = onBackClick,
            actionIcon = Icons.Default.Add,
            onAction = { }
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // ==========================================
            // 2. BANNER AZUL
            // ==========================================
            AppBanner(
                icon = Icons.Default.Security,
                iconBackground = AppDesign.Primary,
                background = AppDesign.PrimarySoft,
                borderColor = AppDesign.PrimaryBorder,
                overline = "Red Activa Cauca",
                overlineColor = AppDesign.Success,
                title = "Contactos de emergencia",
                description = "Personas e instituciones que recibirán tu alerta SOS y ubicación precisa en tiempo real."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 3. LISTA DE CONTACTOS (Familiares y amigos)
            // ==========================================
            AppSectionHeader(
                title = "Familiares y amigos",
                badge = "${uiState.contactos.size}/5",
                badgeColor = AppDesign.TextGray,
                badgeBackground = AppDesign.ChipBg,
                action = "+ Añadir",
                onAction = { }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppDesign.Primary)
                }
            } else if (uiState.errorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(AppDesign.DangerSoft)
                        .border(1.dp, AppDesign.DangerBorder, RoundedCornerShape(AppDesign.CardRadius))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = AppDesign.Danger,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "No pudimos sincronizar tus contactos. Revisa tu conexión e inténtalo de nuevo.",
                        fontSize = appTypography.bodySmall,
                        color = AppDesign.DangerDark
                    )
                }
            } else if (uiState.contactos.isEmpty()) {
                AppCard(padding = 16.dp) {
                    Text(
                        "No tienes contactos de emergencia registrados.",
                        fontSize = appTypography.body,
                        color = AppDesign.TextGray
                    )
                }
            } else {
                uiState.contactos.forEach { contacto ->
                    ContactCard(
                        name = contacto.nombre ?: "Contacto",
                        sub = listOfNotNull(contacto.parentesco).filter { it.isNotBlank() }.joinToString(" • "),
                        phone = contacto.telefono ?: ""
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Agregar Nuevo Contacto
            AppPrimaryButton(
                text = "Agregar nuevo contacto de confianza",
                icon = Icons.Default.PersonAdd,
                onClick = { }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. LÍNEAS DE AUXILIO (Estáticas)
            // ==========================================
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Líneas de auxilio",
                    fontSize = appTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "24/7",
                    fontSize = appTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Danger
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AppChip(
                text = "Oficial Popayán y Cauca",
                color = AppDesign.Danger,
                background = AppDesign.DangerSoft
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tarjetas de líneas de auxilio estáticas
            AuxilioCard(
                title = "Brigada UniAutónoma",
                desc = "Rescate, paramédicos y primeros auxilios • Ext. 104 • (+57) 321 000 0000",
                bgColor = AppDesign.PrimarySoft,
                iconTint = AppDesign.Primary,
                btnText = "Llamar"
            )
            AuxilioCard(
                title = "Bomberos Popayán",
                desc = "Emergencias de fuego, sismos y rescate • Línea directa nacional: 119",
                bgColor = AppDesign.DangerSoft,
                iconTint = AppDesign.Danger,
                btnText = ""
            )
            AuxilioCard(
                title = "Cruz Roja Seccional Cauca",
                desc = "Ambulancias, trauma y primeros auxilios • Marcación rápida: 132",
                bgColor = AppDesign.SoftBg,
                iconTint = AppDesign.Danger,
                btnText = ""
            )
            AuxilioCard(
                title = "Policía Nacional / CAI Mototo",
                desc = "Atención inmediata de orden público • Marcación de auxilio: 123",
                bgColor = AppDesign.PrimarySoft,
                iconTint = AppDesign.Primary,
                btnText = ""
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nota de privacidad inferior
            Text(
                "Tus datos se encuentran cifrados de extremo a extremo. Los contactos solo acceden a tu ubicación satelital en situaciones de emergencia declarada.",
                fontSize = appTypography.caption,
                color = AppDesign.TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

@Composable
private fun ContactCard(name: String, sub: String, phone: String) {
    val type = appTypography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDesign.CardRadius))
            .background(AppDesign.Surface)
            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(AppDesign.SoftBg)
                .border(1.dp, AppDesign.Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = AppDesign.TextMuted,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                fontSize = type.label,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            if (sub.isNotBlank()) {
                Text(
                    sub,
                    fontSize = type.bodySmall,
                    color = AppDesign.TextGray
                )
            }
            if (phone.isNotBlank()) {
                Text(
                    phone,
                    fontSize = type.bodySmall,
                    color = AppDesign.TextGray
                )
            }
        }

        // Botones de acción (Llamada y Menú)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (phone.isNotBlank()) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Llamar",
                    tint = AppDesign.Success,
                    modifier = Modifier
                        .size(40.dp)
                        .background(AppDesign.SuccessSoft, CircleShape)
                        .padding(10.dp)
                        .clickable { }
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                tint = AppDesign.TextGray,
                modifier = Modifier.size(26.dp).clickable { }
            )
        }
    }
}

@Composable
private fun AuxilioCard(
    title: String,
    desc: String,
    bgColor: Color,
    iconTint: Color,
    btnText: String
) {
    val type = appTypography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(AppDesign.CardRadius))
            .background(AppDesign.Surface)
            .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconTile(
            icon = Icons.Default.HealthAndSafety,
            tint = iconTint,
            background = bgColor,
            size = 48.dp,
            iconSize = 26.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = type.label,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            Text(
                desc,
                fontSize = type.caption,
                color = AppDesign.TextGray,
                lineHeight = type.captionLineHeight
            )
        }
        if (btnText.isNotEmpty()) {
            Button(
                onClick = { },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppDesign.Primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    btnText,
                    fontSize = type.action,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Icon(
                Icons.Default.Call,
                contentDescription = null,
                tint = AppDesign.Danger,
                modifier = Modifier
                    .size(40.dp)
                    .background(AppDesign.DangerSoft, CircleShape)
                    .padding(10.dp)
                    .clickable { }
            )
        }
    }
}
