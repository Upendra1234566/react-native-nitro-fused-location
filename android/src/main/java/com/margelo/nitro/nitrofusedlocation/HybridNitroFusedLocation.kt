
/**
 * Created by Upendra Singh
 * MIT License
 */

package com.margelo.nitro.nitrofusedlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class HybridNitroFusedLocation : HybridNitroFusedLocationSpec() {
    private val context =
        NitroModules.applicationContext
            ?: throw Exception("Context is null")
    
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private val activeWatchers = ConcurrentHashMap<String, LocationListener>()

    companion object {
        private val locationListeners = mutableListOf<(LocationData) -> Unit>()
        fun emitLocation(data: LocationData) {
            locationListeners.forEach { it(data) }
        }
    }

    override fun addLocationListener(listener: (LocationData) -> Unit) {
        locationListeners.add(listener)
    }

    override fun removeLocationListener(listener: (LocationData) -> Unit) {
        locationListeners.remove(listener)
    }

    override fun isGpsEnabled(): Promise<Boolean> = Promise.async {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun resetDistance(): Promise<Unit> = Promise.async {
        totalDistance = 0.0
        lastLocation = null
    }

    // ================= LOCATION =================
    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): Promise<LocationData> = Promise.async {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw Exception("Location permission not granted")
        }
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: throw Exception("No location available")
        processLocation(location)
    }

    @SuppressLint("MissingPermission")
    override fun watchPosition(): Promise<String> = Promise.async {
        val id = UUID.randomUUID().toString()
        val listener = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                val data = processLocation(location)
                emitLocation(data)
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            1f,
            listener,
            Looper.getMainLooper()
        )
        activeWatchers[id] = listener
        id
    }

    override fun clearWatch(watchId: String): Promise<Unit> = Promise.async {
        activeWatchers[watchId]?.let {
            locationManager.removeUpdates(it)
            activeWatchers.remove(watchId)
        }
    }

    private fun processLocation(loc: Location): LocationData {
        lastLocation?.let {
            val result = FloatArray(1)
            Location.distanceBetween(it.latitude, it.longitude, loc.latitude, loc.longitude, result)
            totalDistance += result[0]
        }
        lastLocation = loc

        var address = "Unknown"
        var city = "Unknown"
        var state = "Unknown"
        var country = "Unknown"
        var pincode = "Unknown"

        // ================= GEO CODER =================
        try {
            val geo = Geocoder(context, Locale.getDefault())
            val list = geo.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!list.isNullOrEmpty()) {
                val a = list[0]
                address = a.getAddressLine(0) ?: "Unknown"
                city = a.locality ?: "Unknown"
                state = a.adminArea ?: "Unknown"
                country = a.countryName ?: "Unknown"
                pincode = a.postalCode ?: "Unknown"
            }
        } catch (e: Exception) {
            android.util.Log.e("GEOCODER", e.message ?: "error")
        }

        return LocationData(
            loc.latitude,
            loc.longitude,
            loc.accuracy.toDouble(),
            address,
            city,
            state,
            country,
            pincode,
            totalDistance,
            loc.speed.toDouble(),
            false // isInsideGeofence flag (kept false for free version)
        )
    }
}