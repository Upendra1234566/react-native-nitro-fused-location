package com.margelo.nitro.nitrofusedlocation

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class HybridNitroFusedLocation : HybridNitroFusedLocationSpec() {
    private val context = NitroModules.applicationContext ?: throw Exception("Context is null")
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private val activeWatchers = mutableMapOf<String, LocationCallback>()

    // Geofencing State
    private var geofenceLat: Double = 0.0
    private var geofenceLng: Double = 0.0
    private var geofenceRadius: Double = 100.0

    override fun setGeofence(lat: Double, lng: Double, radius: Double): Promise<Unit> {
        return Promise.async {
            geofenceLat = lat
            geofenceLng = lng
            geofenceRadius = radius
        }
    }

    override fun isGpsEnabled(): Promise<Boolean> = Promise.async {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun resetDistance(): Promise<Unit> = Promise.async {
        totalDistance = 0.0
        lastLocation = null
    }

    override fun getCurrentLocation(): Promise<LocationData> = Promise.async {
        val req = CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        val loc = fusedLocationClient.getCurrentLocation(req, null).await() ?: throw Exception("No location")
        processLocation(loc)
    }

    @SuppressLint("MissingPermission")
    override fun watchPosition(callback: (LocationData) -> Unit): Promise<String> = Promise.async {
        val watchId = UUID.randomUUID().toString()
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { callback(processLocation(it)) }
            }
        }
        fusedLocationClient.requestLocationUpdates(req, cb, Looper.getMainLooper())
        activeWatchers[watchId] = cb
        watchId
    }

    override fun clearWatch(watchId: String): Promise<Unit> = Promise.async {
        activeWatchers[watchId]?.let {
            fusedLocationClient.removeLocationUpdates(it).await()
            activeWatchers.remove(watchId)
        }
    }

    private fun processLocation(loc: Location): LocationData {
        // Distance
        lastLocation?.let { last ->
            val res = FloatArray(1)
            Location.distanceBetween(last.latitude, last.longitude, loc.latitude, loc.longitude, res)
            if (res[0] > 1.0f) {
                totalDistance += res[0].toDouble()
                lastLocation = loc
            }
        } ?: run { lastLocation = loc }

        // Geofencing Calculation
        val geoRes = FloatArray(1)
        Location.distanceBetween(loc.latitude, loc.longitude, geofenceLat, geofenceLng, geoRes)
        val isInside = geoRes[0] <= geofenceRadius

        // Geocoding
        val geo = Geocoder(context, Locale.getDefault())
        var addr = "Unknown"; var city = "Unknown"; var state = "Unknown"; var country = "Unknown"; var pin = "Unknown"
        try {
            val addresses = geo.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                addr = a.getAddressLine(0) ?: "Unknown"
                city = a.locality ?: a.subAdminArea ?: "Unknown"
                state = a.adminArea ?: "Unknown"
                country = a.countryName ?: "Unknown"
                pin = a.postalCode ?: "Unknown"
            }
        } catch (e: Exception) {}

        return LocationData(loc.latitude, loc.longitude, loc.accuracy.toDouble(), 
                            addr, city, state, country, pin, totalDistance, 
                            loc.speed.toDouble(), isInside)
    }
}