package com.nitrofusedlocationexample

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader
// Ensure kijiye ki ye import path aapki library ke native code se match kare
import com.margelo.nitro.nitrofusedlocation.NitroFusedLocationPackage 

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost =
    object : DefaultReactNativeHost(this) {
      override fun getPackages(): List<ReactPackage> {
        // Autolinked packages ki list
        val packages = PackageList(this).packages.toMutableList()
        
        // Nitro library ko explicitly register kiya
        packages.add(NitroFusedLocationPackage())
        
        return packages
      }

      override fun getJSMainModuleName(): String = "index"
      
      override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
      
      // Ye flags ab aapke app/build.gradle se aayenge
      override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
      override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
    }

  override val reactHost: ReactHost
    get() = getDefaultReactHost(applicationContext, reactNativeHost)

  override fun onCreate() {
    super.onCreate()
    // Nitro ke liye JSI bridge initialize karna zaruri hai
    SoLoader.init(this, OpenSourceMergedSoMapping)
    
    // New Architecture (TurboModules) load karna
    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
      load()
    }
  }
}