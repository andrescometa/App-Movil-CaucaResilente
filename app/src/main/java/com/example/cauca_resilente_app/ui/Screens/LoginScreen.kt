package com.example.cauca_resilente_app.ui.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cauca_resilente_app.ui.ViewModels.AuthViewModel
import com.example.cauca_resilente_app.ui.components.StatusDot
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
// Importación necesaria para el icono del pin
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.focus.onFocusChanged

// --- PALETA DE COLORES EXACTA (Extraída de la imagen) ---
private val ColorPrimaryBlue = AppDesign.Primary
private val ColorTextDark = AppDesign.Heading
private val ColorTextGray = AppDesign.TextGray
private val ColorBorderGray = AppDesign.Border
private val ColorPlaceholder = AppDesign.TextMuted
private val ColorIconBlue = AppDesign.Primary
private val ColorGreenSOS = AppDesign.Success
private val ColorBgGreen = AppDesign.SuccessSoft
private val ColorBorderGreen = AppDesign.SuccessBorder
private val ColorTextGreen = AppDesign.SuccessDark

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onRegistroSocorristaClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val type = appTypography

    // Usamos un fondo blanco puro para que coincida exactamente con la imagen
    Box(modifier = Modifier.fillMaxSize().background(AppDesign.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {

            // ==========================================
            // 1. HEADER SUPERIOR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Atrás (Cuadrado redondeado con sombra sutil)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Atrás",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Píldora Universidad (Borde azul claro, fondo blanco, texto azul)

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ==========================================
                // 2. ILUSTRACIÓN CENTRAL
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Halo de luz difuminado detrás del candado
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFEF3C7).copy(alpha = 0.6f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    // ⚠️ REEMPLAZA ESTO POR TU IMAGEN REAL DEL CANDADO 3D
                    // Image(painter = painterResource(id = R.drawable.tu_candado_3d), contentDescription = null, modifier = Modifier.size(110.dp))
                    // Placeholder temporal:
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppDesign.Warning,
                        modifier = Modifier.size(85.dp).offset(y = 10.dp)
                    )

                    // Cruz Médica (Izquierda)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-75).dp, y = (-45).dp)
                            .size(34.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = Color(0xFFEF4444).copy(alpha = 0.3f))
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppDesign.DangerSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = AppDesign.Danger, fontSize = type.glyph, fontWeight = FontWeight.Black)
                    }

                    // Pin Ubicación (Derecha)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 75.dp, y = (-35).dp)
                            .size(34.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = Color(0xFF2563EB).copy(alpha = 0.3f))
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorIconBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icono de ubicación simulado
                        Icon(
                            imageVector = Icons.Default.LocationOn, // Requiere import
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Badge GPS SOS
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-55).dp, y = 60.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ColorGreenSOS)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(Color.White, size = 5.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("GPS SOS", color = Color.White, fontSize = type.overline, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // 3. TÍTULOS
                // ==========================================
                Text(
                    "Bienvenido",
                    color = AppDesign.Heading,
                    fontSize = type.hero,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Inicia sesión para continuar en la red\ninstitucional y de asistencia",
                    color = ColorTextGray,
                    fontSize = type.subtitle,
                    textAlign = TextAlign.Center,
                    lineHeight = type.subtitleLineHeight
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ==========================================
                // 4. CAMPOS DE TEXTO
                // ==========================================
                Text(
                    "Correo institucional",
                    color = ColorTextDark,
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 6.dp)
                )
                ExactLoginField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    icon = Icons.Default.Email,
                    placeholder = "usuario@uniautonoma.edu.co",
                    keyboardType = KeyboardType.Email,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "Contraseña",
                    color = ColorTextDark,
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 6.dp)
                )
                ExactLoginField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    icon = Icons.Default.Lock,
                    placeholder = "••••••••••••",
                    keyboardType = KeyboardType.Password,
                    enabled = !uiState.isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Mostrar contraseña",
                                tint = ColorTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        uiState.errorMessage ?: "",
                        color = Color.Red,
                        fontSize = type.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Olvidé contraseña
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = ColorIconBlue,
                    fontSize = type.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(enabled = !uiState.isLoading) { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // 5. BOTÓN PRINCIPAL (Con sombra 3D inferior)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((54 * type.scale).dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorPrimaryBlue)
                        .clickable(enabled = !uiState.isLoading) { viewModel.login() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Iniciar sesión", color = Color.White, fontSize = type.label, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ==========================================
                // 6. SEPARADOR Y BOTONES SOCIALES
                // ==========================================
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(AppDesign.Border))
                    Text(
                        "O CONTINÚA CON",
                        color = ColorTextGray,
                        fontSize = type.overline,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(AppDesign.Border))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    SocialCircle("G", Color(0xFF4285F4))
                    SocialCircle("f", Color(0xFF1877F2))
                    // Icono de Apple (usando AccountCircle como placeholder)
                    SocialCircleIcon(Icons.Default.AccountCircle, Color.Black)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ==========================================
                // 7. FOOTER
                // ==========================================
                Row {
                    Text("¿No tienes cuenta? ", color = ColorTextGray, fontSize = type.bodySmall)
                    Text(
                        "Crear cuenta",
                        color = ColorIconBlue,
                        fontSize = type.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !uiState.isLoading) { onRegisterClick() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // 8. BANNER INFERIOR VERDE
                // ==========================================
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ColorBgGreen)
                        .border(1.dp, ColorBorderGreen, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Punto verde con borde blanco interno simulado
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ColorGreenSOS, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sistema de Protección Activo • Red Resiliente",
                        color = ColorTextGreen,
                        fontSize = type.caption,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES EXACTOS
// ==========================================


@Composable
private fun ExactLoginField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null
) {
    // 1. Estado para saber si el campo tiene el foco
    var isFocused by remember { mutableStateOf(false) }
    val type = appTypography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((52 * type.scale).dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            // 2. El borde cambia de color si está enfocado (opcional, pero mejora la UX)
            .border(
                width = 1.dp,
                color = if (isFocused) ColorPrimaryBlue else ColorBorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFocused) ColorPrimaryBlue else Color(0xFF94A3B8), // El icono se ilumina al enfocar
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(
                color = ColorTextDark,
                fontSize = type.body,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(ColorPrimaryBlue),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .weight(1f)
                // 3. Capturamos el estado del foco
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            decorationBox = { innerTextField ->
                // 4. Lógica del Placeholder: Solo se muestra si el valor está vacío
                // (No depende del foco, solo de si hay texto o no)
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = ColorPlaceholder,
                        fontSize = type.body
                    )
                }
                // 5. El campo de texto real siempre se dibuja encima
                innerTextField()
            }
        )
        trailing?.invoke()
    }
}

@Composable
private fun SocialCircle(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, AppDesign.Border, CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = appTypography.glyph, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SocialCircleIcon(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, AppDesign.Border, CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
    }
}

