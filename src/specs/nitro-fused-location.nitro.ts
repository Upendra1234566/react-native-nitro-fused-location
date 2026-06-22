import type { HybridObject } from 'react-native-nitro-modules'

export interface LocationData {
  latitude: number
  longitude: number
  accuracy: number
}

export interface NitroFusedLocation extends HybridObject<{ ios: 'swift', android: 'kotlin' }> {
  getCurrentLocation(): Promise<LocationData>
}