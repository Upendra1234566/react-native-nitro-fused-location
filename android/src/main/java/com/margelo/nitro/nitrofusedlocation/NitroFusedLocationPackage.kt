
/**
 * Created by Upendra Singh
 * MIT License
 */
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
                // Library load karna zaroori hai taki C++ bridge initialize ho sake
                System.loadLibrary("NitroFusedLocation")
                Log.i("NITRO_LOCATION", "Library Loaded Successfully!")
            } catch (e: Throwable) {
                Log.e("NITRO_LOCATION", "Failed to load library: ${e.message}")
            }
        }
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        // Nitro modules HybridObjects hote hain, isliye yahan koi NativeModule return nahi karna
        return emptyList()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        // Agar aap koi UI component nahi bana rahe, toh khali list
        return emptyList()
    }
}