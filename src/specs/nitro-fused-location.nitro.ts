// import type { HybridObject } from 'react-native-nitro-modules'

// export interface LocationData {
//   latitude: number
//   longitude: number
//   accuracy: number
//   address: string
//   city: string
//   state: string
//   country: string
//   pincode: string
//   distance: number
//   speed: number
//   isInsideGeofence: boolean
// }

// export interface NitroFusedLocation
//   extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  
//   // Core Location
//   getCurrentLocation(): Promise<LocationData>
//   watchPosition(): Promise<string>
//   clearWatch(watchId: string): Promise<void>
  
//   // GPS & Distance
//   isGpsEnabled(): Promise<boolean>
//   resetDistance(): Promise<void>
  
//   // Geofence
//   setGeofence(lat: number, lng: number, radius: number): Promise<void>
  
//   // Kill-Proof Mode - Android Only
//   requestBatteryOptimizationExemption(): Promise<void>
//   startKillProofMode(): Promise<void>
//   stopKillProofMode(): Promise<void>
  
//   // Kill mode
//   killMode(): Promise<void>
  
//   // Auto-start Settings - Reboot 
//   openAutoStartSettings(): Promise<void>
  
//   // Auto Sync - DB wale
//   startAutoSync(): void           // <- NAYA
//   stopAutoSync(): void            // <- NAYA
//   getPendingCount(): number       // <- NAYA
  
//   // Event Listeners
//   addLocationListener(listener: (data: LocationData) => void): void
//   removeLocationListener(listener: (data: LocationData) => void): void 
// } 
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
  isInsideGeofence: boolean // (Ise rehne dijiye kyunki native se ab ye hamesha 'false' aayega)
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
  
  // Event Listeners
  addLocationListener(listener: (data: LocationData) => void): void
  removeLocationListener(listener: (data: LocationData) => void): void 
}