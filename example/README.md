

# react-native-nitro-fused-location 🚀

A blazing-fast, cross-platform location module for React Native built using **Nitro Modules**. It leverages Android's `FusedLocationProviderClient` and iOS's `CoreLocation` to fetch highly accurate GPS coordinates along with reverse geocoding (Address, City, State, Pincode) instantly.

---

## ✨ Features

*   **Ultra Fast:** Built with React Native Nitro Modules (C++ bindings) for zero-bridge overhead.
*   **Cross-Platform:** Works seamlessly on both Android and iOS.
*   **Reverse Geocoding:** Automatically converts coordinates into human-readable addresses (City, State, Country, Pincode).
*   **Main Thread Safe:** Optimized iOS background/main thread execution to prevent UI freezes.
*   **TypeScript Ready:** Fully typed API for a great Developer Experience (DX).

---

## 📦 Installation

```bash
# Using npm
npm install react-native-nitro-fused-location

# OR using Yarn
yarn add react-native-nitro-fused-location 


⚙️ Setup & Permissions
You need to request location permissions on both platforms before fetching the location. 

Android
Add the following permissions to your android/app/src/main/AndroidManifest.xml file: 

<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> 


iOS
Add the following key-value pair to your ios/YourProjectName/Info.plist file to explain why you need the user's location: 

<key>NSLocationWhenInUseUsageDescription</key>
<string>We need your location to fetch the current address.</string> 

💻 Usage
Here is a complete example of how to request permissions and fetch the location in your app. 

import React, { useEffect, useState } from 'react';
import { View, Text, Alert, PermissionsAndroid, Platform } from 'react-native';
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

export default function App() {
  const [location, setLocation] = useState(null);

  const requestLocationPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          fetchLocation();
        } else {
          Alert.alert("Permission Denied", "Location access is required.");
        }
      } catch (err) {
        console.warn(err);
      }
    } else {
      // iOS handles permissions natively via Info.plist
      fetchLocation();
    }
  };

  const fetchLocation = async () => {
    try {
      const loc = await NitroFusedLocation.getCurrentLocation();
      setLocation(loc);
    } catch (err) {
      console.error(err);
      Alert.alert("Error", "Could not fetch location data.");
    }
  };

  useEffect(() => {
    requestLocationPermission();
  }, []);

  return (
    <View 'center' 'center', 1, alignItems: flex: justifyContent: style="{{" }}>
      <Text>Nitro Fused Location 🚀</Text>
      
      {location && (
        <View 20 marginTop: style="{{" }}>
          <Text>📍 Address: {location.address}</Text>
          <Text>🏙 City: {location.city}</Text>
          <Text>🗺 State: {location.state}</Text>
          <Text>🇮🇳 Country: {location.country}</Text>
          <Text>📮 Pincode: {location.pincode}</Text>
          <Text>Lat: {location.latitude} | Lng: {location.longitude}</Text>
        </View>
      )}
    </View>
  );
} 

📖 API Reference
LocationData Object
When you call NitroFusedLocation.getCurrentLocation(), it returns a Promise that resolves to the following object: 

PropertyTypeDescriptionlatitudenumberThe latitude coordinate.longitudenumberThe longitude coordinate.accuracynumberThe accuracy of the location in meters.addressstringFormatted street address (Reverse Geocoded).citystringCity/Locality name.statestringState/Administrative area.countrystringCountry name.pincodestringPostal code / ZIP code.distancenumberDistance moved (if tracking continuous updates). 


🛠️ Local Development App 

If you want to run the example app locally or contribute to this module, follow these steps:

Note: Make sure you have completed the Set Up Your Environment guide before proceeding.

Step 1: Start Metro
First, run Metro, the JavaScript build tool for React Native. 

npm start
# OR using Yarn
yarn start 

npm run android
# OR using Yarn
yarn android 


For iOS:
Remember to install CocoaPods dependencies first: 

npm run ios
# OR using Yarn
yarn ios 


Step 3: Modify the app
Open App.tsx and make some changes. When you save, your app will automatically update via Fast Refresh.

Android: Press R twice to reload.

iOS: Press R in iOS Simulator to reload.

📄 License
MIT 

