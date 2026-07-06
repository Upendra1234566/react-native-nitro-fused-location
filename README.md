# react-native-nitro-fused-location 🚀

**Offline GPS tracking for React Native** - Fire OS compatible. Works without Google Play Services. Background location for Android & iOS.
`react-native-nitro-fused-location` is a blazing-fast, cross-platform location module for React Native built using **Nitro Modules** for zero-bridge overhead. 

It provides ultra-fast location fetching, continuous background tracking, and native reverse geocoding without relying on external paid APIs like Google Maps.

[![Version](https://img.shields.io/npm/v/react-native-nitro-fused-location.svg)](https://www.npmjs.com/package/react-native-nitro-fused-location)
[![Downloads](https://img.shields.io/npm/dm/react-native-nitro-fused-location.svg)](https://www.npmjs.com/package/react-native-nitro-fused-location)

---

## 📋 Requirements

- React Native v0.76.0 or higher
- Node 18.0.0 or higher

## ✨ Features 

*   **🔥 Fire OS Compatible:** Works on Amazon Fire OS tablets & devices without Google Play Services. Uses native `android.location.LocationManager`.
*   **📴 Offline GPS:** Get coordinates, speed, and distance using device GPS without internet or Google Maps API. *Note: Sending data to server requires internet.*
*   **🆓 Zero Google Dependencies:** No Fused Location Provider, no Play Services. Pure AOSP `LocationManager` - works on de-Googled devices.
*   **⚡️ Ultra Fast:** Built with React Native Nitro Modules (C++ bindings) for zero-bridge overhead.
*   **📏 Native Distance Tracking:** Calculates distance in meters directly on Android/iOS background threads.
*   **🚀 Real-time Speed Monitoring:** Get live speed in `m/s` and `km/h` without extra calculations.
*   **🔄 Live Tracking:** Continuously watch user location updates with configurable intervals.
*   **🛡️ Kill-Proof Background Mode:** Service auto-restarts after app kill, swipe, or device reboot. Works on Xiaomi, Vivo, Oppo, Samsung.
*   **🔔 Persistent Foreground Notification:** Shows live location status in notification bar. Cannot be dismissed by user.
*   **🔄 Auto-Restart on Boot:** Automatically resumes tracking after device restart if kill-mode was active.
*   **⚙️ Auto-Start Settings Helper:** One-tap method to open OEM auto-start settings for Chinese phones.
*   **🔋 Battery Optimization Bypass:** Built-in method to request "Ignore Battery Optimizations" for reliable background tracking.
*   **📱 Cross-Platform:** Android uses `LocationManager`, iOS uses `CoreLocation`. No Google APIs on either platform.
*   **📍 Native Geofencing:** Define custom geofence zones and monitor enter/exit events.
*   **🛡️ Main Thread Safe:** All GPS/DB operations run on background threads to prevent UI freezes.
*   **✅ TypeScript Ready:** Fully typed API for a great Developer Experience (DX).
*   **🔋 Battery Efficient:** Uses smart GPS throttling + distance-based updates to reduce battery drain by up to 40%. 
*   **🎯 Configurable Accuracy:** Supports `GPS_PROVIDER`, `NETWORK_PROVIDER`, and criteria-based accuracy levels.
*   **🔐 Permission Handling Built-in:** Handles Android 13+ `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS` and iOS `Always/WhenInUse` permissions.
*   **📦 Tiny Bundle Size:** Native module is <50KB with zero impact on your JS bundle size.

## 🗺️ Roadmap

<!-- ### **v0.2.0 - Coming Soon**
We're working on enterprise-grade features based on community feedback:

*   **📦 Offline Queue:** Store GPS points locally when device is offline
*   **🔄 Auto Sync:** Automatically upload queued data when internet returns  
*   **📊 Analytics:** Track offline duration, battery impact, and sync stats -->

> Star the repo to get notified on release.
 

 <p align="center">
  <img src="./assets/demo.gif" width="360" alt="Nitro Fused Location Demo" />
  <br>
  <sub>Android Release Build • Cold start to 12m tracking • First fix in ~2s</sub>
</p>
> 

---

## 📦 Installation

```bash
npm install react-native-nitro-fused-location react-native-nitro-modules
# or
yarn add react-native-nitro-fused-location react-native-nitro-modules
```
### Android Setup
Supports Android 14 (SDK 36). No extra setup needed!

### ⚙️ Setup & Permissions

#### Android
Add these permissions to your `android/app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<application ...>
  <service 
    android:name=".LocationForegroundService" 
    android:foregroundServiceType="location"
    android:exported="false" />
    
  <receiver 
    android:name=".BootReceiver" 
    android:enabled="true" 
    android:exported="true">
    <intent-filter>
      <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
  </receiver>
</application>
```

#### 1. `android/gradle.properties`
```properties
newArchEnabled=true
hermesEnabled=true
```

### iOS
Add this to your `ios/YourProjectName/Info.plist`:
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>We need your location to fetch the current address.</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>We need your location to track distance in the background.</string>
<key>NSLocationAlwaysUsageDescription</key>
<string>We need your location to track distance in the background.</string>
<key>UIBackgroundModes</key>
<array>
  <string>location</string>
</array>
```

## 📖 How to Use
### Step 1: Request Permissions
For Android 10+ and Android 13+, request all required permissions.
```tsx
import { NitroFusedLocation } from 'react-native-nitro-fused-location';
import { PermissionsAndroid, Platform } from 'react-native';
const requestPermissions = async () => {
  await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
  // 2. Background Location - Required for Android 10+
  if (Platform.Version >= 29) {
    await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION);
  }
  // 3. Notification Permission - Required for Android 13+
  if (Platform.Version >= 33) {
    await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
  }
};
```

### Step 2: Start Live Tracking
Use `watchPosition` to get real-time location updates.
```tsx
import { NitroFusedLocation } from 'react-native-nitro-fused-location';
...

const startTracking = async () => {
  // Start watching
  const watchId = await NitroFusedLocation.watchPosition((location) => {
    console.log('Latitude:', location.latitude);
    console.log('Longitude:', location.longitude);
    console.log('Address:', location.address);
    console.log('Distance:', location.distance, 'meters');
    console.log('Speed:', location.speed * 3.6, 'km/h'); // Convert m/s to km/h
    console.log('Inside Geofence:', location.isInsideGeofence);
  });

  // Stop watching after 10 seconds 
  setTimeout(async () => {
    await NitroFusedLocation.clearWatch(watchId);
  }, 10000);
};

startTracking();
```tsx
Step 3: Enable Kill-Proof Background Mode 
Keeps tracking alive even after app is killed or device is rebooted.
```tsx
// Start background service with persistent notification
await NitroFusedLocation.startKillProofMode();
// Stop background service
await NitroFusedLocation.stopKillProofMode();
// Alias for stopKillProofMode
await NitroFusedLocation.killMode(); 
```tsx

Step 4: OEM Settings for Chinese Devices
Required for Xiaomi, Vivo, Oppo, OnePlus to allow auto-start after reboot.
``` tsx 
// Opens Auto-start settings screen
await NitroFusedLocation.openAutoStartSettings();
// Requests "Ignore Battery Optimizations" permission
await NitroFusedLocation.requestBatteryOptimizationExemption(); 
```
Step 5: Other Useful Methods
``` tsx
// Check if GPS is enabled
const isGpsOn = await NitroFusedLocation.isGpsEnabled();
// Set Geofence - parameters: latitude, longitude, radius in meters
await NitroFusedLocation.setGeofence(28.6139, 77.2090, 500);
// Reset native distance counter to 0
await NitroFusedLocation.resetDistance(); 
```
[!IMPORTANT]
For Kill-Proof mode, users must manually enable "Auto-start" in device settings on Xiaomi, Vivo, Oppo devices.
// Get single location update 
``

## 🛠️ Local Development
1. **Start Metro:** `npm start`
2. **Build Android:** `npm run android`
3. **Build iOS:**
   ```bash
   bundle install
   bundle exec pod install
   npm run ios

   📖 API Reference
   ```API

### Methods

| Method |            Returns |                         Description |
| :--- |                :--- |                              :--- |
| `isGpsEnabled()` | `Promise<boolean>` | Checks if location services are enabled on the device. |
| `getCurrentLocation()` | `Promise<LocationData>` | Fetches the current location once. |
| `watchPosition(callback)` | `Promise<string>` | Subscribes to location updates and native distance calculation. Returns a `watchId`. |
| `clearWatch(watchId)` | `Promise<void>` | Stops watching location updates for the given ID. |
| `resetDistance()` | `Promise<void>` | Resets the natively calculated distance tracker to 0.00m. |
| `setGeofence(lat, lng, radius)` | `Promise<void>` | Sets a target geofence (in meters) to track proximity. |
| `startKillProofMode()` | `Promise<void>` | Starts background service that auto-restarts after app kill. |
| `stopKillProofMode()` | `Promise<void>` | Stops the kill-proof background service. |
| `killMode()` | `Promise<void>` | Alias of `stopKillProofMode()` - Stops kill-proof mode. |
| `openAutoStartSettings()` | `Promise<void>` | Opens Auto-start/Battery settings for Xiaomi, Vivo, Oppo etc. |
| `requestBatteryOptimizationExemption()` | `Promise<void>` | Requests battery optimization exemption for background location. |

###  LocationData  Object (Return Type)
 ``
| Property    | Type     | Description                                      |
| :---------- | :------- | :----------------------------------------------- |
| `latitude`  | `number` | GPS Latitude.                                    |
| `longitude` | `number` | GPS Longitude.                                   |
| `accuracy`  | `number` | Location accuracy in meters.                     |
| `address`   | `string` | Full formatted address (Reverse Geocoded).       |
| `city`      | `string` | City name.                                       |
| `state`     | `string` | State or administrative area.                    |
| `country`   | `string` | Country name.                                    |
| `pincode`   | `string` | Postal code / Zip code.                          |
| `distance`  | `number` | Total distance traveled in meters (since start). |
| `speed`     | `number` | Current speed in meters per second (m/s).        |
| `isInsideGeofence` | `boolean` | Returns true if user is within the defined geofence radius. |
```

Credits
Bootstrapped with create-nitro-module. 

💖 Support My WorkIf this library helped you save time or you find it useful, please consider sponsoring me. Your support helps me maintain this library and build more open-source tools!
```
## License

MIT © [Upendra Singh]

