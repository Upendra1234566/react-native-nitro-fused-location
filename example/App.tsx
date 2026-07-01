
import React, { useEffect, useState, useRef, useCallback } from 'react';
import { 
  View, Text, StyleSheet, SafeAreaView, Alert, PermissionsAndroid, 
  Platform, ScrollView, RefreshControl, TouchableOpacity, Linking
} from 'react-native';
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

interface LocationInfo {
  latitude: number; longitude: number; accuracy: number; address: string;
  city: string; state: string; country: string; pincode: string; distance: number;
  speed: number;           
  isInsideGeofence: boolean; 
}

type StatusType = 'Requesting...' | 'Checking GPS...' | 'Watching...' | 'Success' | 'Failed' | 'Denied' | 'Stopped';

function App(): React.JSX.Element {
  const [location, setLocation] = useState<LocationInfo | null>(null);
  const [status, setStatus] = useState<StatusType>('Requesting...');
  const [refreshing, setRefreshing] = useState(false);
  const watchIdRef = useRef<string | null>(null);

  // CHANGE #1: Distance ko km/m me format karne ka function
  const formatDistance = (meters: number): string => {
    return meters < 1000 
      ? `${meters.toFixed(0)} m` 
      : `${(meters / 1000).toFixed(2)} km`;
  };

  const checkGpsAndStart = async () => {
    setStatus('Checking GPS...');
    const isEnabled = await NitroFusedLocation.isGpsEnabled();
    if (!isEnabled) {
      setStatus('Failed');
      // CHANGE #2: GPS band hai to popup dikhao
      Alert.alert(
        'GPS Band Hai',
        'Location ke liye GPS on karo',
        [
          { text: 'Cancel', style: 'cancel' },
          { text: 'Settings Kholo', onPress: () => Linking.openSettings() }
        ]
      );
      return false;
    }
    startWatching();
    return true;
  };

  const startWatching = async () => {
    setStatus('Watching...');
    try {
      const id = await NitroFusedLocation.watchPosition((loc) => {
        setLocation(loc);
        setStatus('Success');
      });
      watchIdRef.current = id;
    } catch (err: any) {
      setStatus('Failed');
      // CHANGE #3: Permission error handle karo
      if (err.message.includes('permission')) {
        Alert.alert(
          'Permission Chahiye',
          'Location permission allow karo',
          [
            { text: 'Cancel' },
            { text: 'Settings', onPress: () => Linking.openSettings() }
          ]
        );
      }
    }
  };

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    if (watchIdRef.current) {
        await NitroFusedLocation.clearWatch(watchIdRef.current);
        watchIdRef.current = null;
    }
    await checkGpsAndStart();
    setRefreshing(false);
  }, []);

  useEffect(() => {
    // Geofence setup
    NitroFusedLocation.setGeofence(28.8314, 78.7660, 500);

    // Permission and Start
    if (Platform.OS === 'android') {
        PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION)
          .then((granted) => {
            if (granted === PermissionsAndroid.RESULTS.GRANTED) {
              checkGpsAndStart();
            } else {
              setStatus('Denied');
              Alert.alert('Permission Denied', 'App ko location chahiye');
            }
          });
    } else {
        checkGpsAndStart();
    }

    return () => {
      if (watchIdRef.current) NitroFusedLocation.clearWatch(watchIdRef.current);
    };
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView 
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#00ffcc" />}
      >
        <Text style={styles.title}>Nitro Fused Location 🚀</Text>
        <Text style={[styles.status, { color: status === 'Success' ? '#00ffcc' : '#ff4444' }]}>
          Status: {status}
        </Text>
        
        {location && (
          <View style={styles.locationBox}>
            <Text style={styles.label}>📍 Address: {location.address}</Text>
            <Text style={styles.label}>🏙 City: {location.city}</Text>
            <Text style={styles.label}>🗺 State: {location.state}</Text>
            <Text style={styles.label}>🇮🇳 Country: {location.country}</Text>
            <Text style={styles.label}>📮 Pincode: {location.pincode}</Text>
            
            <View style={styles.divider} />
            
            {/* CHANGE #4: Distance ab km/m 
            */}
            <Text style={styles.distanceLabel}>📏 Total Distance: {formatDistance(location.distance)}</Text>
            
            {/* CHANGE #5: Speed km/h me , m/s */}
            <Text style={styles.speedLabel}>⚡ Speed: {location.speed.toFixed(1)} km/h</Text>
            
            <Text style={[styles.geoLabel, { color: location.isInsideGeofence ? '#00ffcc' : '#ffaa00' }]}>
              🏠 Geofence: {location.isInsideGeofence ? 'Inside' : 'Outside'}
            </Text>
            
            <View style={styles.divider} />
            <Text style={styles.coords}>Lat: {location.latitude.toFixed(6)} | Lng: {location.longitude.toFixed(6)}</Text>
            
            <View style={styles.buttonRow}>
              <TouchableOpacity style={styles.resetButton} onPress={async () => {
                await NitroFusedLocation.resetDistance();
                setLocation(prev => prev ? { ...prev, distance: 0 } : null);
              }}><Text style={styles.buttonText}>Reset Distance</Text></TouchableOpacity>
              
              <TouchableOpacity style={styles.stopButton} onPress={async () => {
                if (watchIdRef.current) {
                  await NitroFusedLocation.clearWatch(watchIdRef.current);
                  watchIdRef.current = null;
                  setStatus('Stopped');
                }
              }}><Text style={styles.buttonText}>Stop Tracking</Text></TouchableOpacity>
            </View>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#111' },
  content: { flexGrow: 1, justifyContent: 'center', padding: 20 },
  title: { fontSize: 24, color: 'white', fontWeight: 'bold', textAlign: 'center' },
  status: { fontSize: 16, textAlign: 'center', marginTop: 10, fontWeight: '600' },
  locationBox: { marginTop: 25, padding: 20, backgroundColor: '#1e1e1e', borderRadius: 12, borderWidth: 1, borderColor: '#333' },
  label: { fontSize: 15, color: '#e0e0e0', marginVertical: 4 },
  distanceLabel: { fontSize: 18, color: '#00ffcc', fontWeight: 'bold', marginVertical: 5, textAlign: 'center' },
  speedLabel: { fontSize: 16, color: '#ffcc00', fontWeight: 'bold', marginVertical: 5, textAlign: 'center' },
  geoLabel: { fontSize: 16, fontWeight: 'bold', marginVertical: 5, textAlign: 'center' },
  divider: { height: 1, backgroundColor: '#333', marginVertical: 15 },
  coords: { fontSize: 12, color: '#777', textAlign: 'center' },
  buttonRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 20 },
  resetButton: { backgroundColor: '#4488ff', paddingVertical: 12, borderRadius: 8, flex: 0.48 },
  stopButton: { backgroundColor: '#ff4444', paddingVertical: 12, borderRadius: 8, flex: 0.48 },
  buttonText: { color: 'white', fontSize: 14, fontWeight: 'bold', textAlign: 'center' },
});

export default App;