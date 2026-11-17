package com.example.cauca_resilente_app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography
import java.io.ByteArrayOutputStream

/**
 * Decodifica una foto almacenada como data URL ("data:image/jpeg;base64,...").
 */
fun fotoABitmap(foto: String?, maxDimension: Int = 512): Bitmap? {
    if (foto.isNullOrBlank()) return null

    val base64 = foto.substringAfter(',', foto)
    val bytes = runCatching { Base64.decode(base64, Base64.DEFAULT) }.getOrNull() ?: return null

    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
    if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null

    var escala = 1
    while (opciones.outWidth / (escala * 2) >= maxDimension &&
        opciones.outHeight / (escala * 2) >= maxDimension
    ) {
        escala *= 2
    }

    val opcionesDecodificacion = BitmapFactory.Options().apply { inSampleSize = escala }
    return runCatching {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opcionesDecodificacion)
    }.getOrNull()
}

/**
 * Recorta la foto en un círculo con un borde blanco, útil para los marcadores
 * del mapa y para el avatar del perfil.
 */
fun bitmapCircular(origen: Bitmap, tamano: Int = 144, borde: Int = 8): Bitmap {
    val salida = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(salida)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val centro = tamano / 2f
    val radioExterior = centro
    val radioFoto = centro - borde

    canvas.drawARGB(0, 0, 0, 0)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centro, centro, radioExterior, paint)

    val escala = maxOf(
        (radioFoto * 2) / origen.width.toFloat(),
        (radioFoto * 2) / origen.height.toFloat()
    )
    val ancho = origen.width * escala
    val alto = origen.height * escala
    val destino = RectF(
        centro - ancho / 2f,
        centro - alto / 2f,
        centro + ancho / 2f,
        centro + alto / 2f
    )

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(
        origen,
        Rect(0, 0, origen.width, origen.height),
        destino,
        paint
    )
    paint.xfermode = null

    return salida
}

/** Iniciales para mostrar en un avatar cuando no hay foto. */
fun inicialesDe(nombre: String?): String = nombre
    .orEmpty()
    .trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "UA" }

/**
 * Genera un marcador circular con las iniciales del usuario, usado cuando
 * todavía no ha subido una foto de perfil.
 */
fun bitmapIniciales(
    iniciales: String,
    colorFondo: Int,
    tamano: Int = 144,
    borde: Int = 8
): Bitmap {
    val salida = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(salida)
    val centro = tamano / 2f

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centro, centro, centro, paint)

    paint.color = colorFondo
    canvas.drawCircle(centro, centro, centro - borde, paint)

    val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = tamano * 0.36f
    }
    val metrics = texto.fontMetrics
    val y = centro - (metrics.ascent + metrics.descent) / 2f
    canvas.drawText(iniciales.take(2).uppercase(), centro, y, texto)

    return salida
}

/**
 * Convierte la imagen elegida por el usuario en un data URL JPEG comprimido.
 */
fun fotoDesdeUri(context: Context, uri: Uri, maxDimension: Int = 720): String? {
    return runCatching {
        val resolver = context.contentResolver

        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null

        var escala = 1
        while (opciones.outWidth / (escala * 2) >= maxDimension &&
            opciones.outHeight / (escala * 2) >= maxDimension
        ) {
            escala *= 2
        }

        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = escala })
        } ?: return null

        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, salida)
        val bytes = salida.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }.getOrNull()
}

@Composable
fun FotoPerfil(
    foto: String?,
    iniciales: String,
    tamano: Dp,
    modifier: Modifier = Modifier
) {
    val type = appTypography
    val bitmap = remember(foto) { fotoABitmap(foto, maxDimension = 512) }

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(AppDesign.PrimarySoft),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Text(
                text = iniciales,
                color = AppDesign.Primary,
                fontSize = type.title,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
