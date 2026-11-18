package com.example.cauca_resilente_app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

fun tienePermisoUbicacion(context: Context): Boolean {
    val fino = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val grueso = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fino || grueso
}

@SuppressLint("MissingPermission")
suspend fun obtenerUbicacionActual(context: Context): Location? {
    if (!tienePermisoUbicacion(context)) return null

    val cliente = LocationServices.getFusedLocationProviderClient(context)

    val actual = suspendCancellableCoroutine<Location?> { cont ->
        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location -> if (cont.isActive) cont.resume(location) }
            .addOnFailureListener { if (cont.isActive) cont.resume(null) }
    }
    if (actual != null) return actual

    return suspendCancellableCoroutine { cont ->
        cliente.lastLocation
            .addOnSuccessListener { location -> if (cont.isActive) cont.resume(location) }
            .addOnFailureListener { if (cont.isActive) cont.resume(null) }
    }
}

suspend fun obtenerDireccion(context: Context, latitud: Double, longitud: Double): String {
    val porDefecto = String.format(Locale.getDefault(), "%.5f, %.5f", latitud, longitud)

    return withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val direccion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<String?> { cont ->
                    geocoder.getFromLocation(
                        latitud,
                        longitud,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) {
                                    cont.resume(addresses.firstOrNull()?.getAddressLine(0))
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        }
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitud, longitud, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
            }

            direccion?.takeIf { it.isNotBlank() } ?: porDefecto
        }.getOrDefault(porDefecto)
    }
}


/**
 * Emite la ubicación del usuario de forma continua mientras haya alguien recolectando.
 * Se usa en Home para mostrar la posición en tiempo real.
 */
@SuppressLint("MissingPermission")
fun flujoUbicacionActual(
    context: Context,
    intervaloMs: Long = 20_000L
): Flow<Location> = callbackFlow {
    if (!tienePermisoUbicacion(context)) {
        close()
        return@callbackFlow
    }

    val cliente = LocationServices.getFusedLocationProviderClient(context)
    val solicitud = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervaloMs)
        .setMinUpdateIntervalMillis(intervaloMs / 2)
        .setMinUpdateDistanceMeters(10f)
        .build()

    val callback = object : LocationCallback() {
        override fun onLocationResult(resultado: LocationResult) {
            resultado.lastLocation?.let { ubicacion ->
                trySend(ubicacion)
            }
        }
    }

    cliente.requestLocationUpdates(solicitud, callback, Looper.getMainLooper())

    awaitClose {
        cliente.removeLocationUpdates(callback)
    }
}
