
/**
 * Created by Upendra Singh
 * MIT License
 */
 
package com.margelo.nitro.nitrofusedlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class HybridNitroFusedLocation : HybridNitroFusedLocationSpec() {
    private val context = NitroModules.applicationContext?: throw Exception("Context is null")
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val prefs: SharedPreferences = context.getSharedPreferences("nitro_location_prefs", Context.MODE_PRIVATE)

    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private val activeWatchers = ConcurrentHashMap<String, LocationListener>()

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "nitro_location_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_SERVICE = "STOP_NITRO_LOCATION_SERVICE"

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

    override fun setGeofence(lat: Double, lng: Double, radius: Double): Promise<Unit> {
        return Promise.async {
            NitroLocationService.geofenceLat = lat
            NitroLocationService.geofenceLng = lng
            NitroLocationService.geofenceRadius = radius
        }
    }

    override fun isGpsEnabled(): Promise<Boolean> = Promise.async {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun resetDistance(): Promise<Unit> = Promise.async {
        NitroLocationService.totalDistance = 0.0
        NitroLocationService.lastLocation = null
    }

    override fun requestBatteryOptimizationExemption(): Promise<Unit> = Promise.async {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = context.packageName
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }

    // Naya method - Auto-start settings kholne ke liye
    override fun openAutoStartSettings(): Promise<Unit> = Promise.async {
        try {
            val intent = Intent()
            val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)

            when {
                manufacturer.contains("xiaomi") -> {
                    intent.component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                    intent.component = ComponentName(
                        "com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                    )
                }
                manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                    intent.component = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                }
                manufacturer.contains("oneplus") -> {
                    intent.component = ComponentName(
                        "com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                    )
                }
                manufacturer.contains("huawei") -> {
                    intent.component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                }
                else -> {
                    // Samsung, Stock Android, Others
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", context.packageName, null)
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        } catch (e: Exception) {
            // Fallback - Normal app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", context.packageName, null)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun startKillProofMode(): Promise<Unit> = Promise.async {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            throw Exception("Location permission not granted")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                throw Exception("Notification permission not granted for Android 13+")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                throw Exception("Background location permission not granted")
            }
        }

        // FIX 1: commit() use kar apply() ki jagah - Vivo me reboot pe lost ho jata hai
        prefs.edit().putBoolean("kill_mode_active", true).commit()

        val serviceIntent = Intent(context, NitroLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // AlarmManager setup - Android 12+ ke liye exact alarm check
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val restartIntent = Intent(context, RestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // FIX 2: Android 12+ me exact alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 60000,
                    15 * 60 * 1000,
                    pendingIntent
                )
            } else {
                // Fallback to inexact
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 60000,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 60000,
                15 * 60 * 1000,
                pendingIntent
            )
        }
    }

    override fun stopKillProofMode(): Promise<Unit> = Promise.async {
        // FIX 3: commit() yaha bhi
        prefs.edit().putBoolean("kill_mode_active", false).commit()

        val serviceIntent = Intent(context, NitroLocationService::class.java)
        context.stopService(serviceIntent)

        // AlarmManager cancel
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val restartIntent = Intent(context, RestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
 
 // YE WALA NAYA FUNCTION ADD KIYA HAI
    override fun killMode(): Promise<Unit> = Promise.async {
        stopKillProofMode()
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
    override fun watchPosition(): Promise<String> = Promise.async {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            throw Exception("Location permission not granted")
        }

        val watchId = UUID.randomUUID().toString()
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                val data = processLocation(loc)
                emitLocation(data)
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
        Location.distanceBetween(loc.latitude, loc.longitude, NitroLocationService.geofenceLat, NitroLocationService.geofenceLng, geoRes)
        val isInside = geoRes[0] <= NitroLocationService.geofenceRadius

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

        val speedKmh = if (loc.hasSpeed()) (loc.speed * 3.6) else 0.0

        return LocationData(
            loc.latitude, loc.longitude, loc.accuracy.toDouble(),
            addr, city, state, country, pin,
            totalDistance,
            speedKmh,
            isInside
        )
    }
}