// ui/screens/SolicitarAyudaScreen.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.ui.ViewModels.SolicitarAyudaViewModel
import com.example.cauca_resilente_app.ui.theme.*

private val tiposDeAyuda = listOf(
    "Médica", "Rescate", "Alimento y agua", "Refugio", "Apoyo psicológico", "Otro"
)

private val opcionesGravedad = listOf("Baja", "Media", "Alta")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarAyudaScreen(
    onBackClick: () -> Unit,
    onSolicitudEnviada: () -> Unit,
    viewModel: SolicitarAyudaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dropdownExpandido by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Confirmación de envío exitoso
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
                    tint = VerdeSeguro,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Solicitud enviada", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tu solicitud fue enviada correctamente. El personal autorizado se pondrá en contacto contigo.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetEstado()
                    onSolicitudEnviada()
                }) {
                    Text("Aceptar", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Mostrar errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Confirmar solicitud SOS") },
            text = {
                Text("Se enviará una solicitud de tipo \"${uiState.tipoAyuda}\" con gravedad ${uiState.gravedad}. ¿Deseas continuar?")
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoConfirmacion = false
                    viewModel.enviarSolicitud()
                }) {
                    Text("Enviar", color = RojoEmergencia, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = TextoBlanco,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Solicitar ayuda", color = TextoBlanco, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoBlanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text("Cuéntanos qué necesitas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tu solicitud será enviada al personal autorizado más cercano.",
                fontSize = 14.sp,
                color = TextoOscuro.copy(alpha = 0.6f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== DROPDOWN TIPO =====
            Text("Tipo de ayuda", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpandido,
                onExpandedChange = { if (!uiState.isLoading) dropdownExpandido = !dropdownExpandido }
            ) {
                OutlinedTextField(
                    value = uiState.tipoAyuda,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !uiState.isLoading,
                    placeholder = { Text("Seleccione", color = TextoOscuro.copy(alpha = 0.4f)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = AzulOscuro) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulOscuro,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpandido,
                    onDismissRequest = { dropdownExpandido = false }
                ) {
                    tiposDeAyuda.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.onTipoAyudaChange(tipo)
                                dropdownExpandido = false
                            },
                            trailingIcon = {
                                if (uiState.tipoAyuda == tipo) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeSeguro)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== DESCRIPCIÓN =====
            Text("Descripción", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                placeholder = { Text("Describe tu situación...", color = TextoOscuro.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6,
                enabled = !uiState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulOscuro,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== GRAVEDAD =====
            Text("Gravedad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                opcionesGravedad.forEach { gravedad ->
                    GravedadButton(
                        gravedad = gravedad,
                        seleccionada = uiState.gravedad == gravedad,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        onClick = { viewModel.onGravedadChange(gravedad) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== BOTÓN SOS =====
            Button(
                onClick = { mostrarDialogoConfirmacion = true },
                enabled = !uiState.isLoading && uiState.tipoAyuda.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RojoEmergencia,
                    disabledContainerColor = RojoEmergencia.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextoBlanco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = TextoBlanco, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar solicitud SOS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoBlanco)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GravedadButton(
    gravedad: String,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val color = when (gravedad) {
        "Baja" -> VerdeSeguro
        "Media" -> Color(0xFFF9A825)
        else -> RojoEmergencia
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = if (seleccionada) color.copy(alpha = 0.15f) else TextoBlanco,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = if (seleccionada) 2.dp else 1.dp,
                color = if (seleccionada) color else Color.LightGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = gravedad,
            fontSize = 15.sp,
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium,
            color = if (seleccionada) color else TextoOscuro.copy(alpha = 0.7f)
        )
    }
}