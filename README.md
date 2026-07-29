# react-native-nitro-fused-location 🚀

**Offline GPS tracking for React Native** - Fire OS compatible. Works without Google Play Services. 
`react-native-nitro-fused-location` is a blazing-fast, lightweight location module for React Native built using **Nitro Modules** for zero-bridge overhead.

[![Version](https://img.shields.io/npm/v/react-native-nitro-fused-location.svg)](https://www.npmjs.com/package/react-native-nitro-fused-location)
[![Downloads](https://img.shields.io/npm/dm/react-native-nitro-fused-location.svg)](https://www.npmjs.com/package/react-native-nitro-fused-location)

---

## 🟢 What You Get in FREE Version:
- **🔥 Fire OS Compatible:** Works on Amazon Fire OS tablets & devices without Google Play Services (`LocationManager`).
- **📴 Offline GPS:** Get coordinates, speed, and distance using device GPS without internet or Google Maps API.
- **🆓 Zero Google Dependencies:** No Fused Location Provider, no Play Services. Pure AOSP.
- **⚡️ Ultra Fast C++ Bindings:** Built with React Native Nitro Modules for zero-bridge overhead.
- **📏 Native Distance & Speed:** Real-time distance tracking in meters and speed monitoring in `km/h`.
- **🎯 Core Methods:** `getCurrentLocation()`, `watchPosition()`, `clearWatch()`, `isGpsEnabled()`, `resetDistance()`.

---

## 🚀 Looking for PRO Features? (Paid Version)
If you need enterprise-grade, background-persistent features, check out our **PRO Version** (`react-native-nitro-fused-location-pro`):
- **🛡️ Kill-Proof Background Mode:** Guaranteed foreground service tracking that never gets killed by Android/iOS system.
- **📍 Native Geofencing:** Instant entry/exit alerts for custom zones.
- **🔄 Auto-Start on Boot & Auto-Sync:** Resumes tracking automatically after reboot and syncs offline data via local Room DB.
👉 Get PRO Version: Contact via email at nitrofusedlocationsupport@gmail.com for licensing.
---

## 📋 Requirements
- React Native v0.76.0 or higher
- Node 18.0.0 or higher

---
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
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

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
<string>We need your location to track distance.</string>
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
import { PermissionsAndroid } from 'react-native';

const requestPermissions = async () => {
  await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
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
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

const startTracking = async () => {
  NitroFusedLocation.addLocationListener((location) => {
    console.log('Latitude:', location.latitude);
    console.log('Longitude:', location.longitude);
    console.log('Address:', location.address);
    console.log('Distance:', location.distance, 'meters');
    console.log('Speed:', location.speed, 'm/s');
  });

  const watchId = await NitroFusedLocation.watchPosition();
};
```

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

