package com.example.cauca_resilente_app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

private const val RADIO_ZONA_POR_DEFECTO = 300.0
private const val TAMANO_MARCADOR_PX = 88
private const val BORDE_MARCADOR_PX = 5
private const val FOTO_MARCADOR_MAX_PX = 120


enum class TipoPuntoMapa {
    ZONA,
    ALERTA,      // ← nuevo
    SOLICITUD,
    USUARIO
}
data class PuntoMapa(
    val latitud: Double,
    val longitud: Double,
    val titulo: String,
    val descripcion: String = "",
    val tipo: TipoPuntoMapa = TipoPuntoMapa.ZONA,
    val radioMetros: Double? = null,
    val foto: String? = null,
    val iniciales: String? = null
)

@Composable
fun MapaGoogleView(
    puntos: List<PuntoMapa>,
    centroInicial: LatLng,
    zoomInicial: Double = 13.5,
    interactivo: Boolean = true,
    mostrarControlesZoom: Boolean = true,
    centrarEn: LatLng? = null,
    centrarEnSolicitud: LatLng? = null,
    solicitudCentrado: Int = 0,
    versionFocoSolicitud: Int = 0,
    actualizarCentroConDatos: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }

    var mapa by remember { mutableStateOf<GoogleMap?>(null) }

    val puntosActuales by rememberUpdatedState(puntos)
    val centroActual by rememberUpdatedState(centroInicial)
    val centrarActual by rememberUpdatedState(centrarEn)
    val centrarSolicitudActual by rememberUpdatedState(centrarEnSolicitud)
    val cacheFotos = remember { mutableMapOf<String, BitmapDescriptor>() }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onStart()
            mapView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            mapa = map
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.uiSettings.isMapToolbarEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(centroActual, zoomInicial.toFloat())
            )
        }
    }

    LaunchedEffect(mapa, interactivo, mostrarControlesZoom) {
        val map = mapa ?: return@LaunchedEffect
        map.uiSettings.apply {
            isZoomControlsEnabled = interactivo && mostrarControlesZoom
            isCompassEnabled = interactivo && mostrarControlesZoom
            isScrollGesturesEnabled = interactivo
            isZoomGesturesEnabled = interactivo
            isRotateGesturesEnabled = interactivo
            isTiltGesturesEnabled = interactivo
        }
    }

    LaunchedEffect(mapa, puntos) {
        val map = mapa ?: return@LaunchedEffect
        map.clear()

        val marcadoresDeZona = mutableMapOf<Circle, Marker>()

        puntos.forEach { punto ->
            val posicion = LatLng(punto.latitud, punto.longitud)
            if (punto.tipo == TipoPuntoMapa.ZONA || punto.tipo == TipoPuntoMapa.ALERTA) {
                val marcador = map.addMarker(
                    MarkerOptions()
                        .position(posicion)
                        .title(punto.titulo)
                        .snippet(punto.descripcion)
                        .alpha(0f)
                )
                val circulo = map.addCircle(
                    CircleOptions()
                        .center(posicion)
                        .radius(punto.radioMetros?.takeIf { it > 0 } ?: RADIO_ZONA_POR_DEFECTO)
                        .strokeWidth(3f)
                        .strokeColor(colorBordeZona(punto.tipo))
                        .fillColor(colorRellenoZona(punto.tipo))
                )
                if (marcador != null) marcadoresDeZona[circulo] = marcador
            } else {
                val foto = punto.foto?.takeIf { it.isNotBlank() }
                val etiqueta = punto.iniciales ?: inicialesDe(punto.titulo)
                val clave = foto ?: "iniciales:${punto.tipo.name}:$etiqueta:$TAMANO_MARCADOR_PX"

                val icono = cacheFotos[clave] ?: run {
                    val bitmap = if (foto != null) {
                        fotoABitmap(foto, maxDimension = FOTO_MARCADOR_MAX_PX)?.let {
                            bitmapCircular(it, TAMANO_MARCADOR_PX, BORDE_MARCADOR_PX)
                        }
                    } else {
                        bitmapIniciales(
                            etiqueta,
                            colorCirculo(punto.tipo),
                            TAMANO_MARCADOR_PX,
                            BORDE_MARCADOR_PX
                        )
                    }
                    val descriptor = bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
                    if (descriptor != null) cacheFotos[clave] = descriptor
                    descriptor
                }

                val opciones = MarkerOptions()
                    .position(posicion)
                    .title(punto.titulo)
                    .snippet(punto.descripcion)
                    .icon(
                        icono ?: BitmapDescriptorFactory.defaultMarker(colorPara(punto.tipo))
                    )
                    .anchor(0.5f, 0.5f)

                map.addMarker(opciones)
            }
        }

        map.setOnCircleClickListener { circulo ->
            marcadoresDeZona[circulo]?.showInfoWindow()
        }
    }

    LaunchedEffect(actualizarCentroConDatos, centroInicial, zoomInicial, mapa) {
        if (actualizarCentroConDatos) {
            mapa?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(centroInicial, zoomInicial.toFloat()),
                800,
                null
            )
        }
    }

    LaunchedEffect(solicitudCentrado, mapa) {
        if (solicitudCentrado > 0) {
            val destino = centrarActual ?: centroActual
            mapa?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(destino, 16f),
                800,
                null
            )
        }
    }

    LaunchedEffect(versionFocoSolicitud, mapa) {
        if (versionFocoSolicitud > 0) {
            val destino = centrarSolicitudActual ?: return@LaunchedEffect
            mapa?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(destino, 16f),
                800,
                null
            )
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

// Las alertas se pintan como círculos amarillos y las zonas como círculos rojos.
private fun colorBordeZona(tipo: TipoPuntoMapa): Int = when (tipo) {
    TipoPuntoMapa.ALERTA -> android.graphics.Color.rgb(255, 193, 7)
    else -> AppDesign.Danger.toArgb()
}

private fun colorRellenoZona(tipo: TipoPuntoMapa): Int = when (tipo) {
    TipoPuntoMapa.ALERTA -> android.graphics.Color.argb(64, 255, 193, 7)
    else -> AppDesign.Danger.copy(alpha = 0.22f).toArgb()
}

private fun colorCirculo(tipo: TipoPuntoMapa): Int = when (tipo) {
    TipoPuntoMapa.SOLICITUD -> android.graphics.Color.rgb(198, 40, 40)
    TipoPuntoMapa.ALERTA -> android.graphics.Color.rgb(230, 81, 0)   // naranja
    TipoPuntoMapa.USUARIO -> android.graphics.Color.rgb(21, 72, 156)
    TipoPuntoMapa.ZONA -> android.graphics.Color.rgb(198, 40, 40)
}

private fun colorPara(tipo: TipoPuntoMapa): Float = when (tipo) {
    TipoPuntoMapa.ZONA -> BitmapDescriptorFactory.HUE_RED
    TipoPuntoMapa.ALERTA -> BitmapDescriptorFactory.HUE_ORANGE
    TipoPuntoMapa.SOLICITUD -> BitmapDescriptorFactory.HUE_ORANGE
    TipoPuntoMapa.USUARIO -> BitmapDescriptorFactory.HUE_AZURE
}