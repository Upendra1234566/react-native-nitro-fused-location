package com.margelo.nitro.nitrofusedlocation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {

            try {
                val prefs = context.getSharedPreferences("nitro_location_prefs", Context.MODE_PRIVATE)
                val wasKillModeActive = prefs.getBoolean("kill_mode_active", false)

                if (wasKillModeActive) {
                    // 1. Service start karo
                    val serviceIntent = Intent(context, NitroLocationService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    // 2. AlarmManager se har 15 min me check karo
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val restartIntent = Intent(context, RestartReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 0, restartIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 60000,
                        15 * 60 * 1000,
                        pendingIntent
                    )
                    Log.d("BootReceiver", "NitroLocationService restarted after boot")
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start service after boot", e)
            }
        }
    }
}