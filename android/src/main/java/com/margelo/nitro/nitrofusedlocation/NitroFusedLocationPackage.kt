package com.margelo.nitro.nitrofusedlocation

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import android.util.Log

class NitroFusedLocationPackage : ReactPackage {

    companion object {
        init {
            try {
                Log.i("NITRO_LOCATION", "Step 1: Loading C++ library...")
                System.loadLibrary("NitroFusedLocation") // CMake wale naam se match hona chahiye
                
                Log.i("NITRO_LOCATION", "Step 2: C++ loaded! Initializing Nitrogen...")
                NitroFusedLocationOnLoad.initializeNative()
                
                Log.i("NITRO_LOCATION", "Step 3: All Success!")
            } catch (e: Throwable) { // <-- Exception ki jagah Throwable (Bahut Zaroori)
                Log.e("NITRO_LOCATION", "CRASH: Library load fail ho gayi!", e)
                throw RuntimeException("ASLI ERROR YAHAN HAI: Failed to load NitroFusedLocation C++ library! -> " + e.message, e)
            }
        }
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return emptyList()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}