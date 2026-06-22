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
                Log.i("NITRO_LOCATION", "Loading Manual C++ library...")
                System.loadLibrary("NitroFusedLocation")
                Log.i("NITRO_LOCATION", "Library Loaded Successfully!")
            } catch (e: Throwable) {
                Log.e("NITRO_LOCATION", "Failed to load library", e)
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