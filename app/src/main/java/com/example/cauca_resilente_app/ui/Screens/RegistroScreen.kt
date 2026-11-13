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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cauca_resilente_app.ui.ViewModels.AuthViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppChip
import com.example.cauca_resilente_app.ui.components.AppIconTile
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.AppTextField
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun RegistroScreen(
    onBackClick: () -> Unit,
    onRegistroExitoso: (String) -> Unit,
    onYaTengoCuentaClick: () -> Unit,
    onRegistroSocorristaClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
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
            title = "Crear cuenta",
            subtitle = "UniAutónoma Segura",
            onBack = onBackClick,
            actionIcon = Icons.Default.Info,
            onAction = { },
            actionBackground = AppDesign.SoftBg,
            actionTint = AppDesign.TextSoft
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // ===== ENCABEZADO INSTITUCIONAL =====
            AppCard(padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(AppDesign.TileRadius))
                            .background(AppDesign.PrimarySoft)
                            .border(1.dp, AppDesign.PrimaryBorder, RoundedCornerShape(AppDesign.TileRadius)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = AppDesign.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Únete a Cauca Resiliente",
                            fontSize = type.title,
                            fontWeight = FontWeight.Bold,
                            color = AppDesign.Heading
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Red universitaria y comunitaria de respuesta rápida",
                            fontSize = type.bodySmall,
                            color = AppDesign.TextGray,
                            lineHeight = type.bodySmallLineHeight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== DATOS DE LA CUENTA =====
            Text(
                "Datos de la cuenta",
                fontSize = type.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                label = "Nombre completo",
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                placeholder = "Ej. Didier Felipe Valencia",
                icon = Icons.Default.Person,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Teléfono de contacto",
                value = uiState.telefono,
                onValueChange = viewModel::onTelefonoChange,
                placeholder = "Ej. 312 456 7890",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppTextField(
                label = "Correo institucional",
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "usuario@uniautonoma.edu.co",
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

            // ===== TÉRMINOS =====
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
                        .background(if (termsAccepted) AppDesign.Primary else AppDesign.Surface)
                        .border(
                            1.dp,
                            if (termsAccepted) AppDesign.Primary else AppDesign.Border,
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
                    "Acepto los Términos de Protección y la política de privacidad institucional.",
                    color = AppDesign.TextGray,
                    fontSize = type.bodySmall,
                    lineHeight = type.bodySmallLineHeight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppPrimaryButton(
                text = "Registrarme",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                trailing = true,
                onClick = { viewModel.registrar() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta? ",
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

            Spacer(modifier = Modifier.height(20.dp))

            // ===== REGISTRO DE SOCORRISTA =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDesign.CardRadius))
                    .background(AppDesign.Surface)
                    .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconTile(
                    icon = Icons.Default.HealthAndSafety,
                    tint = AppDesign.SuccessDark,
                    background = AppDesign.SuccessSoft,
                    size = 42.dp,
                    iconSize = 22.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "¿Eres socorrista o brigadista?",
                        color = AppDesign.Heading,
                        fontSize = type.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Recibe solicitudes de emergencia",
                        color = AppDesign.TextGray,
                        fontSize = type.caption
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { onRegistroSocorristaClick() },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppDesign.Primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        "Registrarse",
                        fontSize = type.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
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
