import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, SafeAreaView, Alert, PermissionsAndroid, Platform } from 'react-native';
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

function App(): React.JSX.Element {
  const [location, setLocation] = useState<{latitude: number, longitude: number} | null>(null)
  const [status, setStatus] = useState('Requesting...')

  const requestLocationPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
        );
        
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log("Permission Granted");
          setStatus('Granted')
          fetchLocation();
        } else {
          setStatus('Denied')
          Alert.alert("Permission Denied", "Bhai, location access chahiye.");
        }
      } catch (err) {
        console.warn(err);
        setStatus('Error')
      }
    } else {
      fetchLocation();
    }
  };

  const fetchLocation = async () => {
    try {
      setStatus('Fetching...')
      const loc = await NitroFusedLocation.getCurrentLocation();
      setLocation(loc)
      setStatus('Success')
      Alert.alert("📍 Location Milee!", `Lat: ${loc.latitude.toFixed(4)}\nLng: ${loc.longitude.toFixed(4)}\nAccuracy: ${loc.accuracy}m`);
    } catch (err) {
      console.error("❌ Location Error:", err);
      setStatus('Failed')
      Alert.alert("Error", "Location nahi mil payi.");
    }
  };

  useEffect(() => {
    requestLocationPermission();
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.text}>Nitro Fused Location Welcome Upendra Singh 🚀</Text>
        <Text style={styles.subtext}>Status: {status}</Text>
        {location && (
          <View style={styles.locationBox}>
            <Text style={styles.coords}>Lat: {location.latitude.toFixed(6)}</Text>
            <Text style={styles.coords}>Lng: {location.longitude.toFixed(6)}</Text>
          </View>
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#111' },
  content: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  text: { fontSize: 22, color: 'white', fontWeight: 'bold' },
  subtext: { fontSize: 16, color: '#00ffcc', marginTop: 10 },
  locationBox: { marginTop: 20, padding: 15, backgroundColor: '#222', borderRadius: 8 },
  coords: { fontSize: 14, color: 'white', marginVertical: 2 },
});

export default App;