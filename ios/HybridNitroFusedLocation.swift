import Foundation
import NitroModules
import CoreLocation

// MARK: - Main Nitro Class
class HybridNitroFusedLocation: HybridNitroFusedLocationSpec {
    private var fetcher = LocationFetcher()
    
    func setGeofence(lat: Double, lng: Double, radius: Double) throws -> Promise<Void> {
        return Promise.async {
            self.fetcher.setGeofence(lat: lat, lng: lng, radius: radius)
        }
    }
    
    func getCurrentLocation() throws -> Promise<LocationData> {
        return Promise.async {
            return try await withCheckedThrowingContinuation { [weak self] continuation in
                guard let self = self else { return }
                DispatchQueue.main.async {
                    self.fetcher.fetchLocation { result in
                        switch result {
                        case .success(let data): continuation.resume(returning: data)
                        case .failure(let error): continuation.resume(throwing: error)
                        }
                    }
                }
            }
        }
    }

    func isGpsEnabled() throws -> Promise<Bool> {
        return Promise.async { CLLocationManager.locationServicesEnabled() }
    }

    func watchPosition(callback: @escaping (LocationData) -> Void) throws -> Promise<String> {
        return Promise.async {
            return self.fetcher.startWatching(callback: callback)
        }
    }

    func clearWatch(watchId: String) throws -> Promise<Void> {
        return Promise.async {
            DispatchQueue.main.async { self.fetcher.stopWatching() }
        }
    }

    func resetDistance() throws -> Promise<Void> {
        return Promise.async {
            DispatchQueue.main.async { self.fetcher.resetDistance() }
        }
    }

    func sum(num1: Double, num2: Double) throws -> Double { return num1 + num2 }
}

// MARK: - Location Fetcher Logic
class LocationFetcher: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    private var watchCallback: ((LocationData) -> Void)?
    
    private var totalDistance: Double = 0.0
    private var lastLocation: CLLocation?
    private var geofenceLat: Double = 0.0
    private var geofenceLng: Double = 0.0
    private var geofenceRadius: Double = 100.0

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.distanceFilter = 5 
        locationManager.requestAlwaysAuthorization() 
    }

    func setGeofence(lat: Double, lng: Double, radius: Double) {
        self.geofenceLat = lat
        self.geofenceLng = lng
        self.geofenceRadius = radius
    }

    func fetchLocation(completion: @escaping (Result<LocationData, Error>) -> Void) {
        locationManager.requestLocation()
    }
    
    func startWatching(callback: @escaping (LocationData) -> Void) -> String {
        self.watchCallback = callback
        locationManager.startUpdatingLocation()
        return "ios_watch_id_1"
    }
    
    func stopWatching() {
        locationManager.stopUpdatingLocation()
    }
    
    func resetDistance() {
        self.totalDistance = 0.0
        self.lastLocation = nil
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
        if let previousLoc = lastLocation {
            totalDistance += location.distance(from: previousLoc)
        }
        lastLocation = location
        
        let speed = location.speed > 0 ? location.speed : 0.0
        let targetLoc = CLLocation(latitude: geofenceLat, longitude: geofenceLng)
        let isInside = location.distance(from: targetLoc) <= geofenceRadius
        
        if geocoder.isGeocoding { return }
        
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, _ in
            guard let self = self else { return }
            let p = placemarks?.first
            let data = LocationData(
                latitude: location.coordinate.latitude,
                longitude: location.coordinate.longitude,
                accuracy: location.horizontalAccuracy,
                address: p?.name ?? "Unknown",
                city: p?.locality ?? "Unknown",
                state: p?.administrativeArea ?? "Unknown",
                country: p?.country ?? "Unknown",
                pincode: p?.postalCode ?? "Unknown",
                distance: self.totalDistance,
                speed: speed,
                isInsideGeofence: isInside
            )
            self.watchCallback?(data)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location Error: \(error.localizedDescription)")
    }
}