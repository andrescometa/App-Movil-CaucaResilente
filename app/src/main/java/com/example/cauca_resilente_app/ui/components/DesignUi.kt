package com.example.cauca_resilente_app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cauca_resilente_app.ui.theme.AppColors

/** Fondo con degradado vertical suave y halos ambientales. */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF), Color(0xFFF8FAFC))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .padding(0.dp)
                .background(Color(0xFFDBEAFE).copy(alpha = 0.45f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .background(Color(0xFFFEF3C7).copy(alpha = 0.40f), CircleShape)
        )
        content()
    }
}

/** Tarjeta blanca "glass" con borde y sombra suave. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    color: Color = Color.White,
    borderColor: Color = AppColors.BorderSoft,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.shadow(12.dp, RoundedCornerShape(corner), clip = false),
        shape = RoundedCornerShape(corner),
        color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

/** Botón principal con degradado y relieve. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(AppColors.Amber, AppColors.AmberDark),
    contentColor: Color = AppColors.NavyDeep,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    leading: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(if (enabled) 10.dp else 0.dp, shape)
            .clip(shape)
            .background(
                if (enabled) Brush.linearGradient(colors)
                else Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/** Punto de estado con pulso opcional. */
@Composable
fun StatusDot(color: Color, pulsing: Boolean = true, size: Dp = 8.dp) {
    Box(contentAlignment = Alignment.Center) {
        if (pulsing) {
            val transition = rememberInfiniteTransition(label = "dot")
            val alpha by transition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.05f,
                animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
                label = "dotAlpha"
            )
            Box(
                modifier = Modifier
                    .size(size * 2.1f)
                    .background(color.copy(alpha = alpha), CircleShape)
            )
        }
        Box(modifier = Modifier.size(size).background(color, CircleShape))
    }
}

/** Pequeña píldora/chip de estado. */
@Composable
fun StatusPill(
    text: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    dotColor: Color? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dotColor != null) {
            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(text = text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** Cabecera tipo app con botón volver, título centrado y acción. */
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionTint: Color = AppColors.Accent,
    actionBackground: Color = Color(0xFFEFF6FF),
    showLiveDot: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(6.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, AppColors.BorderSoft, RoundedCornerShape(14.dp))
                .clickable { onBack?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = AppColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showLiveDot) {
                        StatusDot(AppColors.Emerald, size = 6.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = subtitle,
                        color = AppColors.EmeraldDark,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (onAction != null) actionBackground else Color.Transparent)
                .border(
                    if (onAction != null) 1.dp else 0.dp,
                    if (onAction != null) actionTint.copy(alpha = 0.18f) else Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
                .clickable(enabled = onAction != null) { onAction?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            if (actionIcon != null && onAction != null) {
                Icon(actionIcon, contentDescription = null, tint = actionTint, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Escudo institucional volumétrico dibujado con Canvas.
 * Aproxima el emblema 3D de las interfaces (marco dorado, cordillera, comunidad y pin).
 */
@Composable
fun ShieldLogo(
    modifier: Modifier = Modifier,
    glow: Boolean = true
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (glow) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x55FBBF24), Color(0x221E3A8A), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            fun shield(insetX: Float, insetY: Float): Path {
                val p = Path()
                p.moveTo(cx, h * 0.04f + insetY)
                p.lineTo(w * 0.88f - insetX, h * 0.20f + insetY)
                p.cubicTo(
                    w * 0.88f - insetX, h * 0.56f,
                    w * 0.72f - insetX, h * 0.80f,
                    cx, h * 0.95f - insetY
                )
                p.cubicTo(
                    w * 0.28f + insetX, h * 0.80f,
                    w * 0.12f + insetX, h * 0.56f,
                    w * 0.12f + insetX, h * 0.20f + insetY
                )
                p.close()
                return p
            }

            val gold = Brush.linearGradient(
                listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFB45309)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            val inner = Brush.verticalGradient(
                listOf(Color.White, Color(0xFFF0F7FF), Color(0xFFDCEBFE))
            )

            drawPath(shield(0f, 0f), gold)
            drawPath(shield(w * 0.06f, h * 0.03f), inner)
            drawPath(
                shield(w * 0.06f, h * 0.03f),
                Color(0xFFF59E0B),
                style = Stroke(width = w * 0.02f)
            )

            // Cordillera
            val mountains = Path().apply {
                moveTo(w * 0.28f, h * 0.44f)
                lineTo(w * 0.40f, h * 0.33f)
                lineTo(w * 0.49f, h * 0.39f)
                lineTo(w * 0.60f, h * 0.27f)
                lineTo(w * 0.72f, h * 0.44f)
                close()
            }
            drawPath(
                mountains,
                Brush.verticalGradient(listOf(Color(0xFFFCD34D), Color(0xFFB45309)))
            )

            // Comunidad
            val peopleColor = Color(0xFFD97706)
            drawCircle(peopleColor, radius = w * 0.035f, center = Offset(w * 0.33f, h * 0.56f))
            drawRoundRectShape(
                color = peopleColor,
                left = w * 0.27f, top = h * 0.60f, right = w * 0.39f, bottom = h * 0.70f
            )
            drawCircle(peopleColor, radius = w * 0.045f, center = Offset(cx, h * 0.53f))
            drawRoundRectShape(
                color = peopleColor,
                left = w * 0.41f, top = h * 0.60f, right = w * 0.59f, bottom = h * 0.72f
            )

            // Pin de geolocalización
            drawCircle(Color(0xFFFDE68A), radius = w * 0.055f, center = Offset(w * 0.64f, h * 0.50f))
            drawCircle(Color(0xFF1E3A8A), radius = w * 0.025f, center = Offset(w * 0.64f, h * 0.50f))

            // Estrellas
            drawCircle(Color(0xFFF59E0B), radius = w * 0.012f, center = Offset(cx, h * 0.14f))
            drawCircle(Color(0xFFFBBF24), radius = w * 0.008f, center = Offset(w * 0.44f, h * 0.15f))
            drawCircle(Color(0xFFFBBF24), radius = w * 0.008f, center = Offset(w * 0.56f, h * 0.15f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRectShape(
    color: Color,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )
}
