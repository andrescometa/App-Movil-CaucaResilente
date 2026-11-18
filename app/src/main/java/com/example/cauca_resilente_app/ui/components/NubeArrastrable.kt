package com.example.cauca_resilente_app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Contenedor flotante tipo "nube": el usuario puede arrastrarlo libremente por
 * la pantalla y se mantiene dentro de los límites visibles.
 *
 * Se agranda ligeramente mientras se arrastra y, si su contenido cambia de
 * tamaño (por ejemplo al minimizarse), se reajusta para no salirse por abajo.
 */
@Composable
fun NubeArrastrable(
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    content: @Composable () -> Unit
) {
    val densidad = LocalDensity.current
    val ventana = LocalWindowInfo.current
    val margenPx = with(densidad) { 8.dp.toPx() }
    val insetSuperior = WindowInsets.statusBars.getTop(densidad).toFloat()
    val insetInferior = WindowInsets.navigationBars.getBottom(densidad).toFloat()

    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    var tamano by remember { mutableStateOf(IntSize.Zero) }
    var posicionActual by remember { mutableStateOf(Offset.Zero) }
    var baseArrastre by remember { mutableStateOf(Offset.Zero) }
    var arrastrando by remember { mutableStateOf(false) }

    val escala by animateFloatAsState(
        targetValue = if (arrastrando) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "escalaNube"
    )

    LaunchedEffect(tamano, ventana.containerSize) {
        if (tamano == IntSize.Zero || posicionActual == Offset.Zero) return@LaunchedEffect

        val rango = rangoPermitido(
            tamano = tamano,
            anchoVentana = ventana.containerSize.width.toFloat(),
            altoVentana = ventana.containerSize.height.toFloat(),
            margenPx = margenPx,
            insetSuperior = insetSuperior,
            insetInferior = insetInferior
        ) ?: return@LaunchedEffect

        val base = posicionActual - desplazamiento
        val x = (base.x + desplazamiento.x).coerceIn(rango.left, rango.right)
        val y = (base.y + desplazamiento.y).coerceIn(rango.top, rango.bottom)
        val ajustado = Offset(x - base.x, y - base.y)

        if (abs(ajustado.x - desplazamiento.x) > 0.5f || abs(ajustado.y - desplazamiento.y) > 0.5f) {
            desplazamiento = ajustado
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .onSizeChanged { tamano = it }
            .onGloballyPositioned { posicionActual = it.positionInRoot() }
            .offset {
                IntOffset(
                    desplazamiento.x.roundToInt(),
                    desplazamiento.y.roundToInt()
                )
            }
            .pointerInput(habilitado, ventana.containerSize) {
                if (!habilitado) return@pointerInput

                detectDragGestures(
                    onDragStart = {
                        arrastrando = true
                        baseArrastre = posicionActual - desplazamiento
                    },
                    onDragEnd = { arrastrando = false },
                    onDragCancel = { arrastrando = false }
                ) { cambio, arrastre ->
                    cambio.consume()

                    val rango = rangoPermitido(
                        tamano = tamano,
                        anchoVentana = ventana.containerSize.width.toFloat(),
                        altoVentana = ventana.containerSize.height.toFloat(),
                        margenPx = margenPx,
                        insetSuperior = insetSuperior,
                        insetInferior = insetInferior
                    ) ?: return@detectDragGestures

                    val x = (baseArrastre.x + desplazamiento.x + arrastre.x)
                        .coerceIn(rango.left, rango.right)
                    val y = (baseArrastre.y + desplazamiento.y + arrastre.y)
                        .coerceIn(rango.top, rango.bottom)

                    desplazamiento = Offset(x - baseArrastre.x, y - baseArrastre.y)
                }
            }
    ) {
        content()
    }
}

private fun rangoPermitido(
    tamano: IntSize,
    anchoVentana: Float,
    altoVentana: Float,
    margenPx: Float,
    insetSuperior: Float,
    insetInferior: Float
): Rect? {
    val ancho = tamano.width.toFloat()
    val alto = tamano.height.toFloat()
    if (ancho <= 0f || alto <= 0f || anchoVentana <= 0f || altoVentana <= 0f) return null

    val minX = margenPx
    val maxX = (anchoVentana - ancho - margenPx).coerceAtLeast(minX)
    val minY = maxOf(insetSuperior, margenPx)
    val maxY = (altoVentana - alto - insetInferior - margenPx).coerceAtLeast(minY)

    return Rect(left = minX, top = minY, right = maxX, bottom = maxY)
}
