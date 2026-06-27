package com.margelo.nitro.nitrofusedlocation

import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import kotlinx.coroutines.tasks.await
import java.util.Locale

class HybridNitroFusedLocation : HybridNitroFusedLocationSpec() {
    private val context = NitroModules.applicationContext 
        ?: throw Exception("NitroModules.applicationContext is null")
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location? = null 

    override fun getCurrentLocation(): Promise<LocationData> {
        return Promise.async {
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            
            val cancellationToken = CancellationTokenSource().token
            val location = fusedLocationClient.getCurrentLocation(locationRequest, cancellationToken).await()
                ?: throw Exception("Location not found. Ensure GPS is enabled.")

            // 1. Distance Calculation
            var distanceInMeters = 0.0
            lastLocation?.let { last ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    last.latitude, last.longitude,
                    location.latitude, location.longitude,
                    results
                )
                distanceInMeters = results[0].toDouble()
            }
            lastLocation = location 

            // 2. Geocoding (Address) - Wrapped in try-catch
            var addr: android.location.Address? = null
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                // 'getFromLocation' internet access karta hai
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addr = addresses[0]
                }
            } catch (e: Exception) {
                Log.e("NITRO_LOCATION", "Geocoding failed: ${e.message}")
            }

            // 3. Return Final Object
            LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy.toDouble(),
                address = addr?.getAddressLine(0) ?: "Unknown",
                city = addr?.locality ?: addr?.subAdminArea ?: "Unknown",
                state = addr?.adminArea ?: "Unknown",
                country = addr?.countryName ?: "Unknown",
                pincode = addr?.postalCode ?: "Unknown",
                distance = distanceInMeters
            )
        }
    }
}