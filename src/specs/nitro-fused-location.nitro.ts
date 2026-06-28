import type { HybridObject } from 'react-native-nitro-modules'

export interface LocationData {
  latitude: number
  longitude: number
  accuracy: number
  address: string
  city: string
  state: string
  country: string
  pincode: string
  distance: number
  speed: number           
  isInsideGeofence: boolean 
}

export interface NitroFusedLocation extends HybridObject<{ ios: 'swift', android: 'kotlin' }> {
  getCurrentLocation(): Promise<LocationData>
  watchPosition(callback: (data: LocationData) => void): Promise<string>
  clearWatch(watchId: string): Promise<void>
  isGpsEnabled(): Promise<boolean>
  resetDistance(): Promise<void>
  setGeofence(lat: number, lng: number, radius: number): Promise<void> // Naya
}