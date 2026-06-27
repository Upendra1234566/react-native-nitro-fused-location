import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, SafeAreaView, Alert, PermissionsAndroid, Platform, ActivityIndicator } from 'react-native';
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

interface LocationInfo {
  latitude: number;
  longitude: number;
  accuracy: number;
  address: string;
  city: string;
  state: string;
  country: string;
  pincode: string;
  distance: number;
}

function App(): React.JSX.Element {
  const [location, setLocation] = useState<LocationInfo | null>(null);
  const [status, setStatus] = useState<'Requesting...' | 'Fetching...' | 'Success' | 'Failed' | 'Denied'>('Requesting...');

  const requestLocationPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          fetchLocation();
        } else {
          setStatus('Denied');
          Alert.alert("Permission Denied", "Location access is required to show your address.");
        }
      } catch (err) {
        setStatus('Failed');
      }
    } else {
      fetchLocation();
    }
  };

  const fetchLocation = async () => {
    setStatus('Fetching...');
    try {
      const loc = await NitroFusedLocation.getCurrentLocation();
      setLocation(loc);
      setStatus('Success');
    } catch (err) {
      console.error(err);
      setStatus('Failed');
      Alert.alert("Error", "Could not fetch location data.");
    }
  };

  useEffect(() => {
    requestLocationPermission();
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>Nitro Fused Location 🚀</Text>
        <Text style={[styles.status, { color: status === 'Success' ? '#00ffcc' : '#ff4444' }]}>
          Status: {status}
        </Text>
        
        {status === 'Fetching...' && <ActivityIndicator size="large" color="#00ffcc" style={{marginTop: 20}} />}

        {location && (
          <View style={styles.locationBox}>
            <Text style={styles.label}>📍 Address: {location.address}</Text>
            <Text style={styles.label}>🏙 City: {location.city}</Text>
            <Text style={styles.label}>🗺 State: {location.state}</Text>
            <Text style={styles.label}>🇮🇳 Country: {location.country}</Text>
            <Text style={styles.label}>📮 Pincode: {location.pincode}</Text>
            <Text style={styles.label}>📏 Distance Moved: {location.distance.toFixed(2)}m</Text>
            <View style={styles.divider} />
            <Text style={styles.coords}>Lat: {location.latitude.toFixed(6)} | Lng: {location.longitude.toFixed(6)}</Text>
          </View>
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#111' },
  content: { flex: 1, justifyContent: 'center', padding: 20 },
  title: { fontSize: 24, color: 'white', fontWeight: 'bold', textAlign: 'center' },
  status: { fontSize: 16, textAlign: 'center', marginTop: 10, fontWeight: '600' },
  locationBox: { marginTop: 25, padding: 20, backgroundColor: '#1e1e1e', borderRadius: 12, borderWidth: 1, borderColor: '#333' },
  label: { fontSize: 15, color: '#e0e0e0', marginVertical: 4 },
  divider: { height: 1, backgroundColor: '#333', marginVertical: 15 },
  coords: { fontSize: 12, color: '#777', textAlign: 'center' },
});

export default App;