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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.ui.ViewModels.AuthViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppDropdownField
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.AppTextField
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

private val tiposDocumento = listOf("CC - Cédula de Ciudadanía", "CE - Cédula de Extranjería", "TI - Tarjeta de Identidad", "Pasaporte")
private val tiposSangre = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")
private val entidadesSocorro = listOf(
    "Brigada de Emergencias UniAutónoma",
    "Cruz Roja Colombiana",
    "Defensa Civil Colombiana",
    "Cuerpo de Bomberos Voluntarios",
    "Policía Nacional",
    "Otra entidad"
)

@Composable
fun RegistroSocorristaScreen(
    onBackClick: () -> Unit,
    onYaTengoCuentaClick: () -> Unit,
    onRegistroUsuarioClick: () -> Unit,
    viewModel: AuthViewModel
) {
    val type = appTypography
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppDesign.Background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppScreenHeader(
            title = "Registro de socorrista",
            subtitle = "UniAutónoma Segura",
            onBack = onBackClick,
            actionIcon = Icons.Default.Info,
            onAction = { },
            actionBackground = AppDesign.SoftBg,
            actionTint = AppDesign.TextSoft
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // ===== ENCABEZADO DE LA RED SOCORRISTAS =====
            AppCard(padding = 16.dp) {
                Row(verticalAlignment = Alignment.Top) {
                    AppIconTile(
                        icon = Icons.Default.HealthAndSafety,
                        tint = Color.White,
                        background = AppDesign.SuccessDark,
                        size = 52.dp,
                        iconSize = 26.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Únete a la red de socorristas",
                            fontSize = type.title,
                            fontWeight = FontWeight.Bold,
                            color = AppDesign.Heading
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Regístrate para atender emergencias y salvar vidas en Cauca Resiliente.",
                            fontSize = type.bodySmall,
                            color = AppDesign.TextGray,
                            lineHeight = type.bodySmallLineHeight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                AppChip(
                    text = "Red Socorristas UA • Activa",
                    color = AppDesign.SuccessDark,
                    background = AppDesign.SuccessSoft,
                    dotColor = AppDesign.Success
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== DATOS DEL SOCORRISTA =====
            Text(
                "Datos del socorrista",
                fontSize = type.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                label = "Nombre completo",
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                placeholder = "Ej. Andrés Camilo Morales",
                icon = Icons.Default.Person,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppDropdownField(
                label = "Tipo de documento",
                value = uiState.tipoDocumento,
                placeholder = "Selecciona el tipo",
                options = tiposDocumento,
                onSelect = { viewModel.onTipoDocumentoChange(it) },
                icon = Icons.Default.Badge,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Número de documento",
                value = uiState.identificacion,
                onValueChange = viewModel::onIdentificacionChange,
                placeholder = "1001789234",
                icon = Icons.Default.Numbers,
                keyboardType = KeyboardType.Number,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Ciudad",
                value = uiState.ciudad,
                onValueChange = viewModel::onCiudadChange,
                placeholder = "Popayán",
                icon = Icons.Default.LocationCity,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppDropdownField(
                label = "Tipo de sangre",
                value = uiState.tipoSangre,
                placeholder = "Selecciona",
                options = tiposSangre,
                onSelect = { viewModel.onTipoSangreChange(it) },
                icon = Icons.Default.Bloodtype,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Teléfono de contacto",
                value = uiState.telefono,
                onValueChange = viewModel::onTelefonoChange,
                placeholder = "+57 312 000 0000",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppDropdownField(
                label = "Entidad o brigada",
                value = uiState.entidad ?: "",
                placeholder = "Selecciona tu entidad",
                options = entidadesSocorro,
                onSelect = { viewModel.onEntidadChange(it) },
                icon = Icons.Default.Shield,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Correo electrónico",
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "socorrista@uniautonoma.edu.co",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Contraseña",
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Mínimo 8 caracteres",
                icon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Confirmar contraseña",
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = "Repite tu contraseña",
                icon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
                supportingText = if (uiState.confirmPassword.isNotBlank() &&
                    uiState.confirmPassword != uiState.password
                ) {
                    "Las contraseñas no coinciden"
                } else {
                    null
                },
                enabled = !uiState.isLoading
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
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
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = AppDesign.Danger,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        uiState.errorMessage!!,
                        color = AppDesign.DangerDark,
                        fontSize = type.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== CÓDIGO DE ÉTICA =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDesign.CardRadius))
                    .background(AppDesign.Surface)
                    .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                    .clickable { termsAccepted = !termsAccepted }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (termsAccepted) AppDesign.SuccessDark else AppDesign.Surface)
                        .border(
                            1.dp,
                            if (termsAccepted) AppDesign.SuccessDark else AppDesign.Border,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (termsAccepted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Acepto el código de ética de socorrismo y la política de tratamiento de datos de la Red de Emergencias.",
                    color = AppDesign.TextGray,
                    fontSize = type.bodySmall,
                    lineHeight = type.bodySmallLineHeight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppPrimaryButton(
                text = "Completar registro",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                trailing = true,
                containerColor = AppDesign.SuccessDark,
                onClick = { viewModel.registrarSocorrista() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta de socorrista? ",
                    color = AppDesign.TextGray,
                    fontSize = type.bodySmall
                )
                Text(
                    "Iniciar sesión",
                    color = AppDesign.Primary,
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) { onYaTengoCuentaClick() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Eres usuario civil? ",
                    color = AppDesign.TextGray,
                    fontSize = type.bodySmall
                )
                Text(
                    "Registro de ciudadano",
                    color = AppDesign.Primary,
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) { onRegistroUsuarioClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== AVISO INSTITUCIONAL =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDesign.ChipRadius))
                    .background(AppDesign.SuccessSoft)
                    .border(1.dp, AppDesign.SuccessBorder, RoundedCornerShape(AppDesign.ChipRadius))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.size(8.dp).background(AppDesign.Success, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sistema de Protección Activo • Red Resiliente",
                    color = AppDesign.SuccessDark,
                    fontSize = type.caption,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
