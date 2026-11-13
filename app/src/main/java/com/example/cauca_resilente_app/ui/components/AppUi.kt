package com.example.cauca_resilente_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

/**
 * Componentes compartidos por todas las pantallas.
 *
 * Estilo base: superficies blancas con borde de 1dp, radios contenidos,
 * tipografía de `appTypography` y cobalto como color de acción principal.
 */

/** Tarjeta blanca plana con borde suave. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    background: Color = AppDesign.Surface,
    borderColor: Color = AppDesign.Border,
    shape: RoundedCornerShape = RoundedCornerShape(AppDesign.CardRadius),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = background,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

/** Banner tintado (fondo suave + borde del mismo tono) con icono a la izquierda. */
@Composable
fun AppBanner(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = AppDesign.Primary,
    iconBackground: Color = AppDesign.Primary,
    background: Color = AppDesign.PrimarySoft,
    borderColor: Color = AppDesign.PrimaryBorder,
    overline: String? = null,
    overlineColor: Color = AppDesign.Success,
    title: String,
    description: String? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val type = appTypography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDesign.BannerRadius))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(AppDesign.BannerRadius))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(AppDesign.TileRadius))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (!overline.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(overlineColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = overline,
                        fontSize = type.overline,
                        fontWeight = FontWeight.Bold,
                        color = overlineColor
                    )
                }
            }
            Text(
                text = title,
                fontSize = type.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    fontSize = type.bodySmall,
                    color = AppDesign.TextGray,
                    lineHeight = type.bodySmallLineHeight
                )
            }
            content?.invoke(this)
        }
    }
}

/** Encabezado de sección: título, contador opcional y acción opcional. */
@Composable
fun AppSectionHeader(
    title: String,
    badge: String? = null,
    badgeColor: Color = AppDesign.TextGray,
    badgeBackground: Color = AppDesign.ChipBg,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    val type = appTypography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = type.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Heading
            )
            if (!badge.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppDesign.ChipRadius))
                        .background(badgeBackground)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = type.micro,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }
        if (!action.isNullOrBlank() && onAction != null) {
            Text(
                text = action,
                fontSize = type.action,
                fontWeight = FontWeight.Bold,
                color = AppDesign.Primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

/** Chip / etiqueta redondeada. */
@Composable
fun AppChip(
    text: String,
    color: Color = AppDesign.TextGray,
    background: Color = AppDesign.ChipBg,
    modifier: Modifier = Modifier,
    dotColor: Color? = null
) {
    val type = appTypography
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppDesign.ChipRadius))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dotColor != null) {
            Box(modifier = Modifier.size(7.dp).background(dotColor, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = type.action,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/** Cuadro de icono tintado (fondo suave + icono en color fuerte). */
@Composable
fun AppIconTile(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    shape: RoundedCornerShape = RoundedCornerShape(AppDesign.TileRadius)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Botón principal (cobalto). */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = AppDesign.Primary,
    trailing: Boolean = false
) {
    val type = appTypography
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height((60 * type.scale).dp)
            .clip(RoundedCornerShape(AppDesign.ButtonRadius))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(AppDesign.ButtonRadius),
        color = if (enabled) containerColor else AppDesign.ChipBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null && !trailing) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else AppDesign.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                fontSize = type.body,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else AppDesign.TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (icon != null && trailing) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else AppDesign.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Botón secundario delineado. */
@Composable
fun AppOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = AppDesign.Primary
) {
    val type = appTypography
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height((52 * type.scale).dp)
            .clip(RoundedCornerShape(AppDesign.ButtonRadius))
            .clickable { onClick() },
        shape = RoundedCornerShape(AppDesign.ButtonRadius),
        color = AppDesign.Surface,
        border = BorderStroke(1.dp, AppDesign.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = type.bodySmall,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

/** Encabezado estándar de pantalla: título, subtítulo, volver y acción. */
@Composable
fun AppScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    actionBackground: Color = AppDesign.Primary,
    actionTint: Color = Color.White
) {
    val type = appTypography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    fontSize = type.screenTitle,
                    fontWeight = FontWeight.Bold,
                    color = AppDesign.Heading,
                    maxLines = 2
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = type.bodySmall,
                        color = AppDesign.TextGray
                    )
                }
            }
        }
        if (actionIcon != null && onAction != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(actionBackground)
                    .clickable { onAction() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = actionTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Campo de texto con etiqueta superior, en el mismo estilo del sistema. */
@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    supportingText: String? = null
) {
    val type = appTypography
    var focused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = type.bodySmall,
            fontWeight = FontWeight.Bold,
            color = AppDesign.Heading
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((54 * type.scale).dp)
                .clip(RoundedCornerShape(AppDesign.CardRadius))
                .background(AppDesign.Surface)
                .border(
                    width = if (focused) 2.dp else 1.dp,
                    color = if (focused) AppDesign.Primary else AppDesign.Border,
                    shape = RoundedCornerShape(AppDesign.CardRadius)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (focused) AppDesign.Primary else AppDesign.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = AppDesign.Heading,
                    fontSize = type.body,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(AppDesign.Primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focused = it.isFocused },
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = AppDesign.TextMuted,
                            fontSize = type.body
                        )
                    }
                    inner()
                }
            )

            if (isPassword && onTogglePassword != null) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = AppDesign.TextMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onTogglePassword() }
                )
            }
        }

        if (!supportingText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = supportingText,
                fontSize = type.caption,
                color = AppDesign.TextGray
            )
        }
    }
}

/** Campo desplegable con el mismo estilo plano del sistema. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownField(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val type = appTypography
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = type.bodySmall,
            fontWeight = FontWeight.Bold,
            color = AppDesign.Heading
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height((54 * type.scale).dp)
                    .clip(RoundedCornerShape(AppDesign.CardRadius))
                    .background(AppDesign.Surface)
                    .border(1.dp, AppDesign.Border, RoundedCornerShape(AppDesign.CardRadius))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppDesign.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = value.ifBlank { placeholder },
                    fontSize = type.body,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color = if (value.isBlank()) AppDesign.TextMuted else AppDesign.Heading,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppDesign.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = AppDesign.Surface
            ) {
                options.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = opcion,
                                fontSize = type.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = AppDesign.Heading
                            )
                        },
                        onClick = {
                            onSelect(opcion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
