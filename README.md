<img width="1200" height="300" alt="banner (1)" src="https://github.com/user-attachments/assets/4c1994c8-530c-434b-aa54-d49502b45e8e" />

# react-native-nitro-fused-location

Offline GPS tracking for React Native, including Fire OS compatibility. Works without Google Play Services.

`react-native-nitro-fused-location` is a lightweight location module for React Native built using Nitro Modules, providing zero-bridge overhead native performance.

[![Version](https://img.shields.io/npm/v/react-native-nitro-fused-location.svg)](https://www.npmjs.com/package/react-native-nitro-fused-location)
[![Downloads](https://img.shields.io/npm/dm/react-native-nitro-fused-location.svg)](https://www.npmjs.com/package/react-native-nitro-fused-location)

---

## Features (Free Version)

- **Fire OS Compatible** — Works on Amazon Fire OS tablets and devices without Google Play Services, using native `LocationManager`.
- **Offline GPS** — Retrieve coordinates, speed, and distance directly from the device GPS, with no internet or Google Maps API dependency.
- **Zero Google Dependencies** — No Fused Location Provider, no Play Services. Built purely on AOSP APIs.
- **Native C++ Bindings** — Built with React Native Nitro Modules for zero-bridge overhead and minimal JS-native latency.
- **Distance and Speed Tracking** — Real-time distance in meters and speed in km/h, computed natively.
- **Core API** — `getCurrentLocation()`, `watchPosition()`, `clearWatch()`, `isGpsEnabled()`, `resetDistance()`.
- **Platform** - Fully supported on Android & iOS.

---

## Pro Version

For enterprise-grade, background-persistent tracking, see `react-native-nitro-fused-location-pro`:

- **Kill-Proof Background Mode** — Guaranteed foreground service tracking that survives app kills on both Android and iOS.
- **Native Geofencing** — Real-time entry and exit alerts for custom zones.
- **Auto-Start on Boot & Auto-Sync** — Automatically resumes tracking after device reboot and syncs offline data through a local Room database.

For licensing, contact: nitrofusedlocationsupport@gmail.com

---

## Requirements

| Requirement | Minimum Version |
|---|---|
| React Native | 0.76.0+ |
| Node.js | 18.0.0+ |
| Android | SDK 21+ (tested up to SDK 36 / Android 14) |
| iOS | 13.0+ |

---

## Installation

```bash
npm install react-native-nitro-fused-location react-native-nitro-modules
```

or with Yarn:

```bash
yarn add react-native-nitro-fused-location react-native-nitro-modules
```

---

## Platform Support

### Android

- Fully supported on Android 5.0 (API 21) through Android 14 (SDK 36).
- No manual native linking required — autolinking is supported out of the box.
- Works on devices without Google Play Services, including Amazon Fire OS tablets.

**1. Add permissions** to `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

**2. Enable the New Architecture and Hermes** in `android/gradle.properties`:

```properties
newArchEnabled=true
hermesEnabled=true
```

### iOS

- Fully supported on iOS 13 and above.
- Requires CocoaPods installation (see Local Development below).

Add the following to `ios/YourProjectName/Info.plist`:

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

---

## Usage

### Step 1: Request Permissions

```tsx
import { PermissionsAndroid } from 'react-native';

const requestPermissions = async () => {
  await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
};
```

### Step 2: Start Live Tracking

Use `watchPosition` to receive real-time location updates.

```tsx
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

const startTracking = async () => {
  const watchId = await NitroFusedLocation.watchPosition((location) => {
    console.log('Latitude:', location.latitude);
    console.log('Longitude:', location.longitude);
    console.log('Address:', location.address);
    console.log('Distance:', location.distance, 'meters');
    console.log('Speed:', location.speed * 3.6, 'km/h'); // m/s to km/h
    console.log('Inside Geofence:', location.isInsideGeofence);
  });

  // Stop watching after 10 seconds
  setTimeout(async () => {
    await NitroFusedLocation.clearWatch(watchId);
  }, 10000);
};

startTracking();
```

### Step 3: Background Tracking (Pro)

Keeps tracking active even after the app is killed or the device is rebooted.

```tsx
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

const startBackgroundTracking = async () => {
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

---

## API Reference

### Methods

| Method | Returns | Description |
|---|---|---|
| `isGpsEnabled()` | `Promise<boolean>` | Checks if location services are enabled on the device. |
| `getCurrentLocation()` | `Promise<LocationData>` | Fetches the current location once. |
| `watchPosition(callback)` | `Promise<string>` | Subscribes to location updates and native distance calculation. Returns a `watchId`. |
| `clearWatch(watchId)` | `Promise<void>` | Stops watching location updates for the given ID. |
| `resetDistance()` | `Promise<void>` | Resets the natively calculated distance tracker to 0.00m. |
| `setGeofence(lat, lng, radius)` | `Promise<void>` | Sets a target geofence (in meters) to track proximity. |

### LocationData Object

| Property | Type | Description |
|---|---|---|
| `latitude` | `number` | GPS latitude. |
| `longitude` | `number` | GPS longitude. |
| `accuracy` | `number` | Location accuracy in meters. |
| `address` | `string` | Full formatted address (reverse geocoded). |
| `city` | `string` | City name. |
| `state` | `string` | State or administrative area. |
| `country` | `string` | Country name. |
| `pincode` | `string` | Postal code / ZIP code. |
| `distance` | `number` | Total distance traveled in meters since tracking started. |
| `speed` | `number` | Current speed in meters per second (m/s). |
| `isInsideGeofence` | `boolean` | `true` if the user is within the defined geofence radius. |

---

## Local Development

1. Start Metro:
   ```bash
   npm start
   ```
2. Build for Android:
   ```bash
   npm run android
   ```
3. Build for iOS:
   ```bash
   bundle install
   bundle exec pod install
   npm run ios
   ```

---

## Credits

Bootstrapped with `create-nitro-module`.

## Support

If this library saved you time or you find it useful, consider sponsoring the maintainer to support ongoing development.

## License

MIT © Upendra Singh
