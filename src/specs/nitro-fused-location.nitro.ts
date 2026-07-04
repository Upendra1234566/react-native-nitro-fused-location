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

export interface NitroFusedLocation
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  
  // Core Location
  getCurrentLocation(): Promise<LocationData>
  watchPosition(): Promise<string>
  clearWatch(watchId: string): Promise<void>
  
  // GPS & Distance
  isGpsEnabled(): Promise<boolean>
  resetDistance(): Promise<void>
  
  // Geofence
  setGeofence(lat: number, lng: number, radius: number): Promise<void>
  
  // Kill-Proof Mode - Android Only
  requestBatteryOptimizationExemption(): Promise<void>
  startKillProofMode(): Promise<void>
  stopKillProofMode(): Promise<void>
  
  // Auto-start Settings - Reboot ke liye
  openAutoStartSettings(): Promise<void>  // <-- Yaha Promise<void> kar
  
  // Event Listeners - Foreground service se data lene ke liye
  addLocationListener(listener: (data: LocationData) => void): void
  removeLocationListener(listener: (data: LocationData) => void): void 
}