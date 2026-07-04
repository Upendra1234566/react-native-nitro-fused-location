
/**
 * Created by Upendra Singh
 * MIT License
 */
 
package com.margelo.nitro.nitrofusedlocation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.util.Locale

class NitroLocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        var geofenceLat: Double = 0.0
        var geofenceLng: Double = 0.0
        var geofenceRadius: Double = 100.0
        var totalDistance = 0.0
        var lastLocation: Location? = null
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        createNotificationChannel()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NitroLocationService::WakeLock"
        )
        wakeLock?.acquire(10*60*1000L)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == HybridNitroFusedLocation.ACTION_STOP_SERVICE) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        wakeLock?.let {
            if (!it.isHeld) it.acquire(10*60*1000L)
        }

        val notification = createNotification("Location tracking active", "Waiting for location...")
        startForeground(HybridNitroFusedLocation.NOTIFICATION_ID, notification)

        val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

        locationManager.requestLocationUpdates(provider, 5000L, 1f, this, Looper.getMainLooper())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLocationChanged(loc: Location) {
        val data = processLocation(loc)
        updateNotification(data)
        HybridNitroFusedLocation.emitLocation(data)

        wakeLock?.let {
            if (!it.isHeld) it.acquire(10*60*1000L)
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Deprecated in API 29")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

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

        val geo = Geocoder(this, Locale.getDefault())
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

        val speedKmh = if (loc.hasSpeed()) (loc.speed * 3.6) else 0.0

        return LocationData(
            loc.latitude, loc.longitude, loc.accuracy.toDouble(),
            addr, city, state, country, pin,
            totalDistance,
            speedKmh,
            isInside
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HybridNitroFusedLocation.NOTIFICATION_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows notification when location is tracked in background"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val stopIntent = Intent(this, NitroLocationService::class.java).apply {
            action = HybridNitroFusedLocation.ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, HybridNitroFusedLocation.NOTIFICATION_CHANNEL_ID)
           .setContentTitle(title)
           .setContentText(content)
           .setSmallIcon(android.R.drawable.ic_menu_mylocation)
           .setOngoing(true)
           .setCategory(NotificationCompat.CATEGORY_SERVICE)
           .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
           .addAction(android.R.drawable.ic_delete, "Stop Tracking", stopPendingIntent)
           .setPriority(NotificationCompat.PRIORITY_LOW)
           .build()
    }

    private fun updateNotification(data: LocationData) {
        val content = "Lat: ${"%.4f".format(data.latitude)}, Speed: ${data.speed.toInt()} km/h"
        val notification = createNotification("Location tracking active", content)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(HybridNitroFusedLocation.NOTIFICATION_ID, notification)
    }
}