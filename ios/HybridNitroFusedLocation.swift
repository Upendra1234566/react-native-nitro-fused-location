// import Foundation
// import NitroModules
// import CoreLocation

// // Main Nitro Class
// class HybridNitroFusedLocation: HybridNitroFusedLocationSpec {
    
//     // Memory mein hold rakhne ke liye variable
//     private var fetcher: LocationFetcher?
    
//     // JS se call hone wala main function
//     func getCurrentLocation() throws -> Promise<LocationData> {
//         return Promise.async {
//             return try await withCheckedThrowingContinuation { continuation in
//                 // 👇 iOS ko bol rahe hain ki yeh kaam Main Thread par karo
//                 DispatchQueue.main.async {
//                     self.fetcher = LocationFetcher()
//                     self.fetcher?.fetchLocation { result in
//                         switch result {
//                         case .success(let data):
//                             continuation.resume(returning: data)
//                         case .failure(let error):
//                             continuation.resume(throwing: error)
//                         }
//                     }
//                 }
//             }
//         }
//     }

//     // Protocol ke liye zaroori function
//     func sum(num1: Double, num2: Double) throws -> Double {
//         return num1 + num2
//     }
// }

// // iOS Location Fetcher Logic
// class LocationFetcher: NSObject, CLLocationManagerDelegate {
//     private let locationManager = CLLocationManager()
//     private var completion: ((Result<LocationData, Error>) -> Void)?

//     override init() {
//         super.init()
//         locationManager.delegate = self
//         locationManager.desiredAccuracy = kCLLocationAccuracyBest
//     }

//     func fetchLocation(completion: @escaping (Result<LocationData, Error>) -> Void) {
//         self.completion = completion
//         locationManager.requestWhenInUseAuthorization()
//         locationManager.requestLocation() // Location mangna shuru
//     }

//     // Success: Jab location mil jaye
//     func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
//         guard let location = locations.last else { return }
        
//         // Reverse Geocoding (Lat/Lng se Address nikalna)
//         let geocoder = CLGeocoder()
//         geocoder.reverseGeocodeLocation(location) { placemarks, error in
//             let placemark = placemarks?.first
            
//             let data = LocationData(
//                 latitude: location.coordinate.latitude,
//                 longitude: location.coordinate.longitude,
//                 accuracy: location.horizontalAccuracy,
//                 address: placemark?.name ?? "Unknown Address",
//                 city: placemark?.locality ?? "Unknown City",
//                 state: placemark?.administrativeArea ?? "Unknown State",
//                 country: placemark?.country ?? "Unknown Country",
//                 pincode: placemark?.postalCode ?? "Unknown",
//                 distance: 0.0
//             )
            
//             self.completion?(.success(data))
//             self.completion = nil
//         }
//     }

//     // Failed: Agar koi error aaye
//     func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
//         self.completion?(.failure(error))
//         self.completion = nil
//     }
// } 



import Foundation
import NitroModules
import CoreLocation

// Main Nitro Class
class HybridNitroFusedLocation: HybridNitroFusedLocationSpec {
    
    // Memory mein hold rakhne ke liye variable
    private var fetcher = LocationFetcher()
    
    // 1. One-time Location Fetch (Pehle wala)
    func getCurrentLocation() throws -> Promise<LocationData> {
        return Promise.async {
            return try await withCheckedThrowingContinuation { continuation in
                DispatchQueue.main.async {
                    self.fetcher.fetchLocation { result in
                        switch result {
                        case .success(let data):
                            continuation.resume(returning: data)
                        case .failure(let error):
                            continuation.resume(throwing: error)
                        }
                    }
                }
            }
        }
    }

    // 2. NEW: GPS Check karna
    func isGpsEnabled() throws -> Promise<Bool> {
        return Promise.async {
            // iOS mein location services check karne ka tarika
            let enabled = CLLocationManager.locationServicesEnabled()
            return enabled
        }
    }

    // 3. NEW: Continuous Tracking (Watch)
    func watchPosition(callback: @escaping (LocationData) -> Void) throws -> Promise<String> {
        return Promise.async {
            return try await withCheckedThrowingContinuation { continuation in
                DispatchQueue.main.async {
                    let watchId = self.fetcher.startWatching(callback: callback)
                    continuation.resume(returning: watchId)
                }
            }
        }
    }

    // 4. NEW: Tracking Stop karna
    func clearWatch(watchId: String) throws -> Promise<Void> {
        return Promise.async {
            DispatchQueue.main.async {
                self.fetcher.stopWatching()
            }
        }
    }

    // 5. NEW: Distance 0.0 karna
    func resetDistance() throws -> Promise<Void> {
        return Promise.async {
            DispatchQueue.main.async {
                self.fetcher.resetDistance()
            }
        }
    }

    // Protocol ke liye zaroori function
    func sum(num1: Double, num2: Double) throws -> Double {
        return num1 + num2
    }
}

// iOS Location Fetcher Logic (Upgraded for Distance & Watching)
class LocationFetcher: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    
    // Callbacks
    private var singleFetchCompletion: ((Result<LocationData, Error>) -> Void)?
    private var watchCallback: ((LocationData) -> Void)?
    
    // Distance Tracking Variables
    private var isWatching = false
    private var totalDistance: Double = 0.0
    private var lastLocation: CLLocation?

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        // Background updates ke liye ye properties zaroori hain
        locationManager.allowsBackgroundLocationUpdates = true 
        locationManager.pausesLocationUpdatesAutomatically = false
    }

    // Single Fetch (Purana Logic)
    func fetchLocation(completion: @escaping (Result<LocationData, Error>) -> Void) {
        self.singleFetchCompletion = completion
        locationManager.requestWhenInUseAuthorization()
        locationManager.requestLocation()
    }
    
    // Continuous Fetch (Naya Watch Logic)
    func startWatching(callback: @escaping (LocationData) -> Void) -> String {
        self.watchCallback = callback
        self.isWatching = true
        self.totalDistance = 0.0
        self.lastLocation = nil
        
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation() // Lagaatar location maangna
        
        return "ios_watch_id_1" // Dummy ID, par kaam karegi
    }
    
    // Stop Watching
    func stopWatching() {
        self.isWatching = false
        locationManager.stopUpdatingLocation()
        self.watchCallback = nil
    }
    
    // Reset Distance
    func resetDistance() {
        self.totalDistance = 0.0
        self.lastLocation = nil
    }

    // Success: Jab location mil jaye
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
        // DISTANCE CALCULATE KARNA
        if self.isWatching {
            if let previousLoc = lastLocation {
                // iOS directly meters mein distance nikal kar deta hai
                totalDistance += location.distance(from: previousLoc)
            }
            lastLocation = location
        }
        
        // Reverse Geocoding (Lat/Lng se Address nikalna)
        let geocoder = CLGeocoder()
        geocoder.reverseGeocodeLocation(location) { placemarks, error in
            let placemark = placemarks?.first
            
            let data = LocationData(
                latitude: location.coordinate.latitude,
                longitude: location.coordinate.longitude,
                accuracy: location.horizontalAccuracy,
                address: placemark?.name ?? "Unknown Address",
                city: placemark?.locality ?? "Unknown City",
                state: placemark?.administrativeArea ?? "Unknown State",
                country: placemark?.country ?? "Unknown Country",
                pincode: placemark?.postalCode ?? "Unknown",
                distance: self.totalDistance // Added distance here
            )
            
            // Single fetch ke liye bhejein
            self.singleFetchCompletion?(.success(data))
            self.singleFetchCompletion = nil
            
            // Continuous watch ke liye bhejein
            if self.isWatching {
                self.watchCallback?(data)
            }
        }
    }

    // Failed: Agar koi error aaye
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        self.singleFetchCompletion?(.failure(error))
        self.singleFetchCompletion = nil
        // Continuous fetch fail ho toh use bhi handle kar sakte hain aage chal kar
    }
}