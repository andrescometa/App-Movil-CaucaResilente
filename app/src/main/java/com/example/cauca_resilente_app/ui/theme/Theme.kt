package com.example.cauca_resilente_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val CaucaColorScheme = lightColorScheme(
    primary = AppColors.Cobalt,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = AppColors.Navy,
    secondary = AppColors.Amber,
    onSecondary = AppColors.NavyDeep,
    secondaryContainer = AppColors.AmberLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = AppColors.Emerald,
    onTertiary = Color.White,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceSoft,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Divider,
    error = AppColors.Red,
    onError = Color.White
)

@Composable
fun Cauca_Resilente_AppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val appType = rememberAppTypography()

    // Escala tipográfica global: multiplica el `fontScale` del sistema para que
    // TODO el texto de la app (incluidos los `sp` fijos de cada pantalla) se lea
    // más grande. Se aplica sobre la densidad base, sin tocar la conversión dp.
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * APP_FONT_SCALE
        )
    }

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalAppTypography provides appType
    ) {
        MaterialTheme(
            colorScheme = CaucaColorScheme,
            typography = Typography,
            content = content
        )
    }
}
