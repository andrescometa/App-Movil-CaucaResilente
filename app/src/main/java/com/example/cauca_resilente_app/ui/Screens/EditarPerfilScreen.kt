package com.example.cauca_resilente_app.ui.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.User
import com.example.cauca_resilente_app.ui.ViewModels.UserViewModel
import com.example.cauca_resilente_app.ui.components.AppCard
import com.example.cauca_resilente_app.ui.components.AppPrimaryButton
import com.example.cauca_resilente_app.ui.components.AppScreenHeader
import com.example.cauca_resilente_app.ui.components.AppTextField
import com.example.cauca_resilente_app.ui.components.FotoPerfil
import com.example.cauca_resilente_app.ui.components.fotoDesdeUri
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun EditarPerfilScreen(
    user: User?,
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onGuardado: () -> Unit
) {
    val type = appTypography
    val context = LocalContext.current
    val editarPerfil by viewModel.editarPerfil.collectAsStateWithLifecycle()

    var nombres by rememberSaveable(user?.id) { mutableStateOf(user?.name.orEmpty()) }
    var apellidos by rememberSaveable(user?.id) { mutableStateOf(user?.apellidos.orEmpty()) }
    var telefono by rememberSaveable(user?.id) { mutableStateOf(user?.phone.orEmpty()) }
    var email by rememberSaveable(user?.id) { mutableStateOf(user?.email.orEmpty()) }
    var fotoSeleccionada by remember(user?.id) { mutableStateOf<String?>(null) }
    var errorLocal by rememberSaveable { mutableStateOf<String?>(null) }

    val iniciales = listOf(nombres, apellidos)
        .joinToString(" ")
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "UA" }

    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val dataUrl = fotoDesdeUri(context, uri)
        if (dataUrl == null) {
            errorLocal = "No se pudo leer la imagen seleccionada"
        } else {
            errorLocal = null
            fotoSeleccionada = dataUrl
        }
    }

    LaunchedEffect(editarPerfil.perfilActualizado) {
        if (editarPerfil.perfilActualizado) {
            viewModel.consumirResultadoEdicion()
            onGuardado()
        }
    }

    Scaffold(containerColor = AppDesign.Background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            AppScreenHeader(
                title = "Editar perfil",
                subtitle = "Actualiza tus datos personales",
                onBack = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                AppCard(padding = 18.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            FotoPerfil(
                                foto = fotoSeleccionada ?: user?.foto,
                                iniciales = iniciales,
                                tamano = 104.dp
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AppDesign.Primary)
                                    .border(2.dp, AppDesign.Surface, CircleShape)
                                    .clickable {
                                        selectorFoto.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Cambiar foto de perfil",
                            color = AppDesign.Primary,
                            fontSize = type.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppDesign.ChipRadius))
                                .clickable {
                                    selectorFoto.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "La foto se guarda en tu cuenta y se muestra a los socorristas en el mapa.",
                            color = AppDesign.TextGray,
                            fontSize = type.caption,
                            lineHeight = type.captionLineHeight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AppCard(padding = 18.dp) {
                    Text(
                        text = "Datos personales",
                        color = AppDesign.Heading,
                        fontSize = type.label,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    AppTextField(
                        label = "Nombres",
                        value = nombres,
                        onValueChange = { nombres = it },
                        placeholder = "Tus nombres",
                        icon = Icons.Filled.Person
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AppTextField(
                        label = "Apellidos",
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        placeholder = "Tus apellidos",
                        icon = Icons.Filled.Badge
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AppTextField(
                        label = "Número de teléfono",
                        value = telefono,
                        onValueChange = { telefono = it },
                        placeholder = "300 000 0000",
                        icon = Icons.Filled.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AppTextField(
                        label = "Correo institucional (Gmail)",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "usuario@uniautonoma.edu.co",
                        icon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email,
                        supportingText = "Con este correo iniciarás sesión la próxima vez."
                    )
                }

                val error = errorLocal ?: editarPerfil.errorMessage
                if (error != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppDesign.CardRadius))
                            .background(AppDesign.DangerSoft)
                            .border(
                                1.dp,
                                AppDesign.DangerBorder,
                                RoundedCornerShape(AppDesign.CardRadius)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = AppDesign.DangerDark,
                            fontSize = type.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (editarPerfil.guardando) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppDesign.Primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Guardando cambios...",
                            color = AppDesign.TextSoft,
                            fontSize = type.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    AppPrimaryButton(
                        text = "Guardar cambios",
                        icon = Icons.Filled.Save,
                        onClick = {
                            viewModel.guardarPerfil(
                                nombres = nombres,
                                apellidos = apellidos,
                                telefono = telefono,
                                email = email,
                                foto = fotoSeleccionada
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
