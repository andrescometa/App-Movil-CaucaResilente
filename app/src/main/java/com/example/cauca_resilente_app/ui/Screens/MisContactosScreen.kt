package com.example.cauca_resilente_app.ui.Screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.ContactoEmergencia
import com.example.cauca_resilente_app.ui.ViewModels.ContactosViewModel
import com.example.cauca_resilente_app.ui.components.AppBanner
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.AppSectionHeader
import com.example.cauca_resilente_app.ui.components.AppTextField
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun MisContactosScreen(
    onBackClick: () -> Unit,
    viewModel: ContactosViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var mostrarDialogo by remember { mutableStateOf(false) }
    var contactoAEliminar by remember { mutableStateOf<ContactoEmergencia?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarContactos()
    }

    LaunchedEffect(uiState.mensajeExito) {
        if (uiState.mensajeExito != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.consumirMensajes()
        }
    }

    fun llamar(numero: String) {
        val limpio = numero.filter { it.isDigit() || it == '+' }
        if (limpio.isBlank()) return
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$limpio")))
        }
    }

    if (mostrarDialogo) {
        AgregarContactoDialog(
            guardando = uiState.guardando,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nombre, telefono, parentesco ->
                viewModel.agregarContacto(nombre, telefono, parentesco) {
                    mostrarDialogo = false
                }
            }
        )
    }

    contactoAEliminar?.let { contacto ->
        AlertDialog(
            onDismissRequest = { contactoAEliminar = null },
            title = { Text("Eliminar contacto") },
            text = {
                Text("¿Quitar a ${contacto.nombre ?: "este contacto"} de tu red de emergencia?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarContacto(contacto.id.orEmpty()) {
                        contactoAEliminar = null
                    }
                }) {
                    Text("Eliminar", color = AppDesign.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactoAEliminar = null }) { Text("Cancelar") }
            }
        )
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
            onAction = {
                viewModel.consumirMensajes()
                mostrarDialogo = true
            }
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
                onAction = {
                    viewModel.consumirMensajes()
                    mostrarDialogo = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            uiState.mensajeExito?.let { mensaje ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(AppDesign.CardRadius))
                        .background(AppDesign.SuccessSoft)
                        .border(1.dp, AppDesign.Success, RoundedCornerShape(AppDesign.CardRadius))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppDesign.Success,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        mensaje,
                        fontSize = appTypography.bodySmall,
                        color = AppDesign.Success
                    )
                }
            }

            uiState.errorMessage?.let { mensaje ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
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
                        mensaje,
                        fontSize = appTypography.bodySmall,
                        color = AppDesign.DangerDark
                    )
                }
            }

            if (uiState.isLoading && uiState.contactos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppDesign.Primary)
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
                        phone = contacto.telefono ?: "",
                        onCall = { llamar(contacto.telefono.orEmpty()) },
                        onDelete = { contactoAEliminar = contacto }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Agregar Nuevo Contacto
            AppPrimaryButton(
                text = "Agregar nuevo contacto de confianza",
                icon = Icons.Default.PersonAdd,
                onClick = {
                    viewModel.consumirMensajes()
                    mostrarDialogo = true
                }
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
                btnText = "Llamar",
                phone = "+573210000000",
                onCall = { llamar("+573210000000") }
            )
            AuxilioCard(
                title = "Bomberos Popayán",
                desc = "Emergencias de fuego, sismos y rescate • Línea directa nacional: 119",
                bgColor = AppDesign.DangerSoft,
                iconTint = AppDesign.Danger,
                btnText = "",
                phone = "119",
                onCall = { llamar("119") }
            )
            AuxilioCard(
                title = "Cruz Roja Seccional Cauca",
                desc = "Ambulancias, trauma y primeros auxilios • Marcación rápida: 132",
                bgColor = AppDesign.SoftBg,
                iconTint = AppDesign.Danger,
                btnText = "",
                phone = "132",
                onCall = { llamar("132") }
            )
            AuxilioCard(
                title = "Policía Nacional / CAI Mototo",
                desc = "Atención inmediata de orden público • Marcación de auxilio: 123",
                bgColor = AppDesign.PrimarySoft,
                iconTint = AppDesign.Primary,
                btnText = "",
                phone = "123",
                onCall = { llamar("123") }
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
private fun ContactCard(
    name: String,
    sub: String,
    phone: String,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
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
                        .clickable { onCall() }
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Eliminar contacto",
                tint = AppDesign.TextGray,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onDelete() }
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
    btnText: String,
    phone: String,
    onCall: () -> Unit
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
                onClick = onCall,
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
                    .clickable { onCall() }
            )
        }
    }
}


@Composable
private fun AgregarContactoDialog(
    guardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (nombre: String, telefono: String, parentesco: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var parentesco by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text("Vincular contacto de emergencia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "Esta persona recibirá tu alerta SOS con tu ubicación en tiempo real.",
                    fontSize = appTypography.bodySmall,
                    color = AppDesign.TextGray
                )
                AppTextField(
                    label = "Nombre completo",
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = "Ej. María Fernanda López",
                    icon = Icons.Default.Person,
                    enabled = !guardando
                )
                AppTextField(
                    label = "Teléfono",
                    value = telefono,
                    onValueChange = { telefono = it },
                    placeholder = "Ej. 312 456 7890",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    enabled = !guardando
                )
                AppTextField(
                    label = "Parentesco (opcional)",
                    value = parentesco,
                    onValueChange = { parentesco = it },
                    placeholder = "Ej. Madre, hermano, vecino",
                    icon = Icons.Default.FamilyRestroom,
                    enabled = !guardando
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, telefono, parentesco) },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = AppDesign.Primary)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (guardando) "Guardando..." else "Vincular")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
        },
        containerColor = AppDesign.Surface
    )
}
