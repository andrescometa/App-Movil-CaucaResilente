package com.example.cauca_resilente_app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tokens de diseño de la app.
 *
 * Esta es la fuente única de verdad del estilo visual: todas las pantallas
 * deben usar estos colores, radios y tipografías para mantener la misma
 * identidad (piso, sin degradados ni sombras pesadas).
 */
object AppDesign {
    // Titulares y texto
    val Heading = Color(0xFF0F1E46)      // Títulos de pantalla y secciones
    val Title = Color(0xFF0F1E46)        // Títulos dentro de tarjetas
    val TextGray = Color(0xFF64748B)     // Textos secundarios y descriptivos
    val TextSoft = Color(0xFF475569)     // Textos secundarios más fuertes
    val TextMuted = Color(0xFF94A3B8)    // Textos terciarios / iconos inactivos

    // Marca
    val Primary = Color(0xFF1E3A8A)      // Cobalto: acciones principales
    val PrimarySoft = Color(0xFFEFF6FF)  // Fondo de banners y chips
    val PrimaryBorder = Color(0xFFDBEAFE)

    // Estados
    val Success = Color(0xFF10B981)
    val SuccessDark = Color(0xFF047857)
    val SuccessSoft = Color(0xFFECFDF5)
    val SuccessBorder = Color(0xFFA7F3D0)

    val Danger = Color(0xFFEF4444)
    val DangerDark = Color(0xFFB91C1C)
    val DangerSoft = Color(0xFFFEF2F2)
    val DangerBorder = Color(0xFFFECACA)

    val Warning = Color(0xFFF59E0B)
    val WarningDark = Color(0xFFB45309)
    val WarningSoft = Color(0xFFFFFBEB)
    val WarningBorder = Color(0xFFFDE68A)

    val Info = Color(0xFF0EA5E9)
    val InfoDark = Color(0xFF0369A1)
    val InfoSoft = Color(0xFFF0F9FF)
    val InfoBorder = Color(0xFFBAE6FD)

    // Superficies
    val Surface = Color(0xFFFFFFFF)
    val Background = Color(0xFFF8FAFC)
    val Border = Color(0xFFE2E8F0)
    val ChipBg = Color(0xFFE2E8F0)
    val SoftBg = Color(0xFFF1F5F9)

    // Formas
    val CardRadius = 16.dp
    val BannerRadius = 20.dp
    val TileRadius = 14.dp
    val ButtonRadius = 16.dp
    val ChipRadius = 50.dp
}
