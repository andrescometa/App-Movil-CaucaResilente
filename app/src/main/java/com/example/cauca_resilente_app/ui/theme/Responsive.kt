package com.example.cauca_resilente_app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Multiplicador global de tipografía.
 *
 * Se aplica sobre el `fontScale` del sistema, por lo que:
 * - si el usuario tiene la fuente del sistema al 100%, la app renderiza ~1.15x
 * - si el usuario tiene la fuente al 130%, la app renderiza ~1.30 * 1.15
 *
 * Sirve para que TODO el texto de la app (incluidos los `sp` escritos a mano
 * en las pantallas) se vea más grande sin tener que editar pantalla por pantalla.
 * Súbelo o bájalo desde aquí para calibrar el tamaño global de letra.
 */
const val APP_FONT_SCALE: Float = 1.10f

/**
 * Clasificación del ancho disponible del dispositivo.
 */
enum class DeviceWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

/**
 * Escala tipográfica de la app. Todos los valores ya vienen escalados
 * según el tamaño del dispositivo, por lo que en las pantallas basta con
 * usar `appTypography.hero` (o `rememberAppTypography()`) en lugar
 * de escribir `32.sp` a mano.
 *
 * La base de diseño está calibrada para un móvil de 360dp de ancho con
 * fuente del sistema al 100%.
 */
@Immutable
data class AppTypography(
    val scale: Float,
    val hero: TextUnit,        // Título principal de pantalla (ej. "Bienvenido")
    val screenTitle: TextUnit, // Encabezado de pantalla (ej. "Contactos De Emergencia")
    val title: TextUnit,       // Títulos de sección y etiquetas de campo
    val sectionTitle: TextUnit,// Encabezado de bloque dentro de la pantalla
    val subtitle: TextUnit,    // Texto descriptivo bajo el título
    val body: TextUnit,        // Texto normal / cuerpo
    val bodySmall: TextUnit,   // Texto auxiliar, errores, descripciones de card
    val label: TextUnit,       // Botones y enlaces accionables
    val action: TextUnit,      // Acciones pequeñas ("+ Añadir")
    val caption: TextUnit,     // Texto del banner institucional
    val overline: TextUnit,    // Separadores y etiquetas en mayúsculas
    val micro: TextUnit,       // Badges diminutos ("Prioridad 1")
    val glyph: TextUnit        // Letras dentro de iconos circulares
) {
    val heroLineHeight: TextUnit get() = hero.scaledBy(1.15f)
    val screenTitleLineHeight: TextUnit get() = screenTitle.scaledBy(1.25f)
    val titleLineHeight: TextUnit get() = title.scaledBy(1.3f)
    val sectionTitleLineHeight: TextUnit get() = sectionTitle.scaledBy(1.3f)
    val subtitleLineHeight: TextUnit get() = subtitle.scaledBy(1.45f)
    val bodyLineHeight: TextUnit get() = body.scaledBy(1.5f)
    val bodySmallLineHeight: TextUnit get() = bodySmall.scaledBy(1.45f)
    val labelLineHeight: TextUnit get() = label.scaledBy(1.3f)
    val actionLineHeight: TextUnit get() = action.scaledBy(1.35f)
    val captionLineHeight: TextUnit get() = caption.scaledBy(1.4f)
}

private fun TextUnit.scaledBy(factor: Float): TextUnit =
    TextUnit(this.value * factor, this.type)

private fun scaled(base: Float, scale: Float): TextUnit = (base * scale).sp

/**
 * Calcula el factor de escala tipográfica según el tamaño de la pantalla.
 *
 * - Móviles estrechos (<360dp)          -> 0.90  (todo cabe mejor)
 * - Móviles estándar (360..399dp)       -> 1.00  (referencia de diseño)
 * - Móviles grandes (400..479dp)         -> 1.05
 * - Tablets verticales (480..599dp)      -> 1.12
 * - Tablets horizontales (600..839dp)    -> 1.20
 * - Tablets grandes (>=840dp)            -> 1.32
 *
 * En landscape (altura reducida) se limita el crecimiento para no romper
 * la legibilidad en pantallas cortas.
 */
internal fun calculateTypeScale(widthDp: Int, heightDp: Int): Float {
    val base = when {
        widthDp < 360 -> 0.95f
        widthDp < 400 -> 1.00f
        widthDp < 480 -> 1.05f
        widthDp < 600 -> 1.12f
        widthDp < 840 -> 1.20f
        else -> 1.32f
    }
    // Pantallas bajas (landscape en móvil): el alto manda, se topa el escalado.
    return if (heightDp in 1 until 480) base.coerceAtMost(1.05f) else base
}

internal fun widthClassOf(widthDp: Int): DeviceWidthClass = when {
    widthDp < 600 -> DeviceWidthClass.COMPACT
    widthDp < 840 -> DeviceWidthClass.MEDIUM
    else -> DeviceWidthClass.EXPANDED
}

/**
 * Construye la escala tipográfica sensible al tamaño del dispositivo.
 */
@Composable
fun rememberAppTypography(): AppTypography {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val scale = calculateTypeScale(widthDp, heightDp)

    return AppTypography(
        scale = scale,
        hero = scaled(34f, scale),
        screenTitle = scaled(21f, scale),
        title = scaled(19f, scale),
        sectionTitle = scaled(17f, scale),
        subtitle = scaled(15f, scale),
        body = scaled(15f, scale),
        bodySmall = scaled(14f, scale),
        label = scaled(17f, scale),
        action = scaled(14f, scale),
        caption = scaled(13f, scale),
        overline = scaled(12f, scale),
        micro = scaled(11f, scale),
        glyph = scaled(22f, scale)
    )
}

val LocalAppTypography: ProvidableCompositionLocal<AppTypography> =
    staticCompositionLocalOf {
        AppTypography(
            scale = 1f,
            hero = 34.sp,
            screenTitle = 21.sp,
            title = 19.sp,
            sectionTitle = 17.sp,
            subtitle = 15.sp,
            body = 15.sp,
            bodySmall = 14.sp,
            label = 17.sp,
            action = 14.sp,
            caption = 13.sp,
            overline = 12.sp,
            micro = 11.sp,
            glyph = 22.sp
        )
    }

/**
 * Acceso a la escala tipográfica responsive dentro de cualquier @Composable.
 */
val appTypography: AppTypography
    @Composable
    @ReadOnlyComposable
        get() = LocalAppTypography.current

// ---------------------------------------------------------------
// Estilos de texto listos para usar
// ---------------------------------------------------------------

@Composable
fun appHeroStyle(): TextStyle = TextStyle(
    fontSize = appTypography.hero,
    lineHeight = appTypography.heroLineHeight,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.5).sp
)

@Composable
fun appTitleStyle(): TextStyle = TextStyle(
    fontSize = appTypography.title,
    lineHeight = appTypography.titleLineHeight,
    fontWeight = FontWeight.SemiBold
)

@Composable
fun appSubtitleStyle(): TextStyle = TextStyle(
    fontSize = appTypography.subtitle,
    lineHeight = appTypography.subtitleLineHeight,
    fontWeight = FontWeight.Normal
)

@Composable
fun appBodyStyle(): TextStyle = TextStyle(
    fontSize = appTypography.body,
    lineHeight = appTypography.bodyLineHeight,
    fontWeight = FontWeight.Normal
)

@Composable
fun appLabelStyle(): TextStyle = TextStyle(
    fontSize = appTypography.label,
    lineHeight = appTypography.labelLineHeight,
    fontWeight = FontWeight.Medium
)
