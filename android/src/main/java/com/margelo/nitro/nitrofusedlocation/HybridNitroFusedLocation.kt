package com.margelo.nitro.nitrofusedlocation

import com.margelo.nitro.nitrofusedlocation.HybridNitroFusedLocationSpec
import com.margelo.nitro.nitrofusedlocation.LocationData
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules // <-- NAYA IMPORT
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

// Constructor se (private val context: Context) hata diya hai
class HybridNitroFusedLocation : HybridNitroFusedLocationSpec() {
    
    // Yahan hum directly globally context le rahe hain
    private val context = NitroModules.applicationContext 
        ?: throw Exception("NitroModules.applicationContext is null")

    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)

    override fun getCurrentLocation(): Promise<LocationData> {
        return Promise.async {
            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            
            val cancellationToken = CancellationTokenSource().token
            
            val location = fusedLocationClient.getCurrentLocation(locationRequest, cancellationToken).await()
            
            if (location != null) {
                LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy.toDouble()
                )
            } else {
                throw Exception("Location is null. Enable GPS & check permissions")
            }
        }
    }
}