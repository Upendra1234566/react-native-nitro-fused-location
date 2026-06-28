import React, { useEffect, useState, useRef, useCallback } from 'react';
import { 
  View, Text, StyleSheet, SafeAreaView, Alert, PermissionsAndroid, 
  Platform, TouchableOpacity, ScrollView, AppState, RefreshControl 
} from 'react-native';
import { NitroFusedLocation } from 'react-native-nitro-fused-location';

interface LocationInfo {
  latitude: number; longitude: number; accuracy: number; address: string;
  city: string; state: string; country: string; pincode: string; distance: number;
}

type StatusType = 'Requesting...' | 'Checking GPS...' | 'Watching...' | 'Success' | 'Failed' | 'Denied' | 'Stopped';

function App(): React.JSX.Element {
  const [location, setLocation] = useState<LocationInfo | null>(null);
  const [status, setStatus] = useState<StatusType>('Requesting...');
  const [refreshing, setRefreshing] = useState(false);
  const watchIdRef = useRef<string | null>(null);
  const appState = useRef(AppState.currentState);

  const checkGpsAndStart = async () => {
    setStatus('Checking GPS...');
    const isEnabled = await NitroFusedLocation.isGpsEnabled();
    if (!isEnabled) {
      setStatus('Failed');
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
    } catch (err) {
      setStatus('Failed');
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
    PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION).then(() => {
        checkGpsAndStart();
    });

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
            <Text style={styles.distanceLabel}>📏 Total Distance: {location.distance.toFixed(2)}m</Text>
            
            <View style={styles.divider} />
            <Text style={styles.coords}>Lat: {location.latitude.toFixed(6)} | Lng: {location.longitude.toFixed(6)}</Text>
            
            <View style={styles.buttonRow}>
              <TouchableOpacity style={styles.resetButton} onPress={async () => {
                await NitroFusedLocation.resetDistance();
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
  distanceLabel: { fontSize: 18, color: '#00ffcc', fontWeight: 'bold', marginVertical: 10, textAlign: 'center' },
  divider: { height: 1, backgroundColor: '#333', marginVertical: 15 },
  coords: { fontSize: 12, color: '#777', textAlign: 'center' },
  buttonRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 20 },
  resetButton: { backgroundColor: '#4488ff', paddingVertical: 12, borderRadius: 8, flex: 0.48 },
  stopButton: { backgroundColor: '#ff4444', paddingVertical: 12, borderRadius: 8, flex: 0.48 },
  buttonText: { color: 'white', fontSize: 14, fontWeight: 'bold', textAlign: 'center' },
});

export default App;