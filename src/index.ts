import { NitroModules } from 'react-native-nitro-modules'
import type { NitroFusedLocation as NitroFusedLocationSpec } from './specs/nitro-fused-location.nitro'

export const NitroFusedLocation = NitroModules.createHybridObject<NitroFusedLocationSpec>('NitroFusedLocation')