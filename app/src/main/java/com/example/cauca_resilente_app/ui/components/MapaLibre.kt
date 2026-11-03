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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textAnchor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textFont
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.textIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.textMaxWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

const val ESTILO_OPENFREEMAP = "https://tiles.openfreemap.org/styles/liberty"

enum class TipoPuntoMapa {
    ZONA,
    SOLICITUD,
    USUARIO
}

data class PuntoMapa(
    val latitud: Double,
    val longitud: Double,
    val titulo: String,
    val descripcion: String = "",
    val tipo: TipoPuntoMapa = TipoPuntoMapa.ZONA
)

private const val FUENTE_ZONAS = "cauca-fuente-zonas"
private const val FUENTE_SOLICITUDES = "cauca-fuente-solicitudes"
private const val FUENTE_USUARIO = "cauca-fuente-usuario"

private const val CAPA_ZONAS = "cauca-capa-zonas"
private const val CAPA_SOLICITUDES = "cauca-capa-solicitudes"
private const val CAPA_USUARIO = "cauca-capa-usuario"

private const val CAPA_TEXTO_ZONAS = "cauca-capa-texto-zonas"
private const val CAPA_TEXTO_SOLICITUDES = "cauca-capa-texto-solicitudes"
private const val CAPA_TEXTO_USUARIO = "cauca-capa-texto-usuario"

private const val COLOR_ZONA = "#D32F2F"
private const val COLOR_SOLICITUD = "#F9A825"
private const val COLOR_USUARIO = "#1565C0"

@Composable
fun MapaLibreView(
    puntos: List<PuntoMapa>,
    centroInicial: LatLng,
    zoomInicial: Double = 13.5,
    interactivo: Boolean = true,
    centrarEn: LatLng? = null,
    solicitudCentrado: Int = 0,
    actualizarCentroConDatos: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }

    var mapa by remember { mutableStateOf<MapLibreMap?>(null) }
    // CORREGIDO: antes era Notification.Style?, ahora es Style? de MapLibre
    var estilo by remember { mutableStateOf<Style?>(null) }

    val puntosActuales by rememberUpdatedState(puntos)
    val centroActual by rememberUpdatedState(centroInicial)
    val centrarActual by rememberUpdatedState(centrarEn)
    val interactivoActual by rememberUpdatedState(interactivo)

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

            val ui = map.uiSettings
            ui.isAttributionEnabled = true
            ui.isLogoEnabled = true
            ui.isCompassEnabled = interactivoActual
            if (!interactivoActual) {
                ui.setAllGesturesEnabled(false)
            }

            // CORREGIDO: CameraUpdateFactory de MapLibre
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(centroActual, zoomInicial)
            )

            // CORREGIDO: Style.Builder de MapLibre (no Notification.Style.Builder)
            map.setStyle(Style.Builder().fromUri(ESTILO_OPENFREEMAP)) { estiloCargado ->
                configurarCapas(estiloCargado)
                estilo = estiloCargado
                actualizarFuentes(estiloCargado, puntosActuales)
            }
        }
    }

    LaunchedEffect(estilo, puntos) {
        estilo?.let { actualizarFuentes(it, puntos) }
    }

    LaunchedEffect(actualizarCentroConDatos, centroInicial, zoomInicial, mapa) {
        if (actualizarCentroConDatos) {
            mapa?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(centroInicial, zoomInicial),
                800
            )
        }
    }

    LaunchedEffect(solicitudCentrado) {
        if (solicitudCentrado > 0) {
            val destino = centrarActual ?: centroActual
            mapa?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(destino, 16.0),
                800
            )
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun configurarCapas(style: Style) {
    if (style.getSource(FUENTE_ZONAS) == null) {
        style.addSource(GeoJsonSource(FUENTE_ZONAS, coleccionVacia()))
    }
    if (style.getSource(FUENTE_SOLICITUDES) == null) {
        style.addSource(GeoJsonSource(FUENTE_SOLICITUDES, coleccionVacia()))
    }
    if (style.getSource(FUENTE_USUARIO) == null) {
        style.addSource(GeoJsonSource(FUENTE_USUARIO, coleccionVacia()))
    }

    if (style.getLayer(CAPA_ZONAS) == null) {
        style.addLayer(
            CircleLayer(CAPA_ZONAS, FUENTE_ZONAS).withProperties(
                circleColor(COLOR_ZONA),
                circleRadius(8f),
                circleOpacity(0.92f),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2f)
            )
        )
    }
    if (style.getLayer(CAPA_SOLICITUDES) == null) {
        style.addLayer(
            CircleLayer(CAPA_SOLICITUDES, FUENTE_SOLICITUDES).withProperties(
                circleColor(COLOR_SOLICITUD),
                circleRadius(8f),
                circleOpacity(0.92f),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2f)
            )
        )
    }
    if (style.getLayer(CAPA_USUARIO) == null) {
        style.addLayer(
            CircleLayer(CAPA_USUARIO, FUENTE_USUARIO).withProperties(
                circleColor(COLOR_USUARIO),
                circleRadius(9f),
                circleOpacity(0.95f),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(3f)
            )
        )
    }

    if (style.getLayer(CAPA_TEXTO_ZONAS) == null) {
        style.addLayer(
            SymbolLayer(CAPA_TEXTO_ZONAS, FUENTE_ZONAS).withProperties(
                textField(Expression.get("titulo")),
                textFont(arrayOf("Noto Sans Regular")),
                textSize(11f),
                textColor("#7F0000"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.3f)),
                textAllowOverlap(false),
                textIgnorePlacement(false),
                textMaxWidth(9f)
            )
        )
    }
    if (style.getLayer(CAPA_TEXTO_SOLICITUDES) == null) {
        style.addLayer(
            SymbolLayer(CAPA_TEXTO_SOLICITUDES, FUENTE_SOLICITUDES).withProperties(
                textField(Expression.get("titulo")),
                textFont(arrayOf("Noto Sans Regular")),
                textSize(11f),
                textColor("#7A4F00"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.3f)),
                textAllowOverlap(false),
                textIgnorePlacement(false),
                textMaxWidth(9f)
            )
        )
    }
    if (style.getLayer(CAPA_TEXTO_USUARIO) == null) {
        style.addLayer(
            SymbolLayer(CAPA_TEXTO_USUARIO, FUENTE_USUARIO).withProperties(
                textField(Expression.get("titulo")),
                textFont(arrayOf("Noto Sans Regular")),
                textSize(11f),
                textColor("#0D47A1"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.3f)),
                textAllowOverlap(false),
                textIgnorePlacement(false),
                textMaxWidth(9f)
            )
        )
    }
}

private fun actualizarFuentes(style: Style, puntos: List<PuntoMapa>) {
    val agrupados = puntos.groupBy { it.tipo }
    actualizarFuente(style, FUENTE_ZONAS, agrupados[TipoPuntoMapa.ZONA].orEmpty())
    actualizarFuente(style, FUENTE_SOLICITUDES, agrupados[TipoPuntoMapa.SOLICITUD].orEmpty())
    actualizarFuente(style, FUENTE_USUARIO, agrupados[TipoPuntoMapa.USUARIO].orEmpty())
}

private fun actualizarFuente(style: Style, idFuente: String, puntos: List<PuntoMapa>) {
    val features = puntos.map { punto ->
        Feature.fromGeometry(Point.fromLngLat(punto.longitud, punto.latitud)).apply {
            addStringProperty("titulo", punto.titulo)
            addStringProperty("descripcion", punto.descripcion)
        }
    }
    style.getSourceAs<GeoJsonSource>(idFuente)
        ?.setGeoJson(FeatureCollection.fromFeatures(features))
}

private fun coleccionVacia(): FeatureCollection =
    FeatureCollection.fromFeatures(emptyList<Feature>())