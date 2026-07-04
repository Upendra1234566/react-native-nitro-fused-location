
/**
 * Created by Upendra Singh
 * MIT License
 */
 
package com.margelo.nitro.nitrofusedlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("nitro_location_prefs", Context.MODE_PRIVATE)
        val wasKillModeActive = prefs.getBoolean("kill_mode_active", false)

        if (wasKillModeActive) {
            val serviceIntent = Intent(context, NitroLocationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d("RestartReceiver", "Service restarted by AlarmManager")
            } catch (e: Exception) {
                Log.e("RestartReceiver", "Failed to restart service", e)
            }
        }
    }
}