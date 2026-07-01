/*
 * NitroFusedLocation - Fire OS Compatible Location Library
 * Copyright (c) 2026 Upendra Singh
 * All rights reserved.
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
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class HybridNitroFusedLocation : HybridNitroFusedLocationSpec() {
    private val context = NitroModules.applicationContext?: throw Exception("Context is null")
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private val activeWatchers = ConcurrentHashMap<String, LocationListener>()

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
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun resetDistance(): Promise<Unit> = Promise.async {
        totalDistance = 0.0
        lastLocation = null
    }

    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): Promise<LocationData> = Promise.async {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            throw Exception("Location permission not granted")
        }

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        var bestLocation: Location? = null

        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc!= null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                    bestLocation = loc
                }
            }
        }

        val loc = bestLocation?: throw Exception("No location available. Enable GPS.")
        processLocation(loc)
    }

    @SuppressLint("MissingPermission")
    override fun watchPosition(callback: (LocationData) -> Unit): Promise<String> = Promise.async {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            throw Exception("Location permission not granted")
        }

        val watchId = UUID.randomUUID().toString()
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                callback(processLocation(loc))
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 1f, listener, Looper.getMainLooper())
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 1f, listener, Looper.getMainLooper())
        }

        activeWatchers[watchId] = listener
        watchId
    }

    override fun clearWatch(watchId: String): Promise<Unit> = Promise.async {
        activeWatchers[watchId]?.let {
            locationManager.removeUpdates(it)
            activeWatchers.remove(watchId)
        }
    }

    private fun processLocation(loc: Location): LocationData {
        lastLocation?.let { last ->
            val res = FloatArray(1)
            Location.distanceBetween(last.latitude, last.longitude, loc.latitude, loc.longitude, res)
            if (res[0] > 1.0f) {
                totalDistance += res[0].toDouble()
                lastLocation = loc
            }
        }?: run { lastLocation = loc }

        val geoRes = FloatArray(1)
        Location.distanceBetween(loc.latitude, loc.longitude, geofenceLat, geofenceLng, geoRes)
        val isInside = geoRes[0] <= geofenceRadius

        val geo = Geocoder(context, Locale.getDefault())
        var addr = "Unknown"; var city = "Unknown"; var state = "Unknown"; var country = "Unknown"; var pin = "Unknown"
        try {
            val addresses = geo.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                addr = a.getAddressLine(0)?: "Unknown"
                city = a.locality?: a.subAdminArea?: "Unknown"
                state = a.adminArea?: "Unknown"
                country = a.countryName?: "Unknown"
                pin = a.postalCode?: "Unknown"
            }
        } catch (e: Exception) {}

        val speedKmh = if (loc.hasSpeed()) (loc.speed * 3.6) else 0.0 // m/s to km/h

        return LocationData(
            loc.latitude, loc.longitude, loc.accuracy.toDouble(),
            addr, city, state, country, pin,
            totalDistance, // meter me - JS me format karna
            speedKmh, // km/h me
            isInside
        )
    }
}