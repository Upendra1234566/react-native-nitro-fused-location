import Foundation
import NitroModules
import CoreLocation

// MARK: - Main Nitro Class
class HybridNitroFusedLocation: HybridNitroFusedLocationSpec {
    private var fetcher = LocationFetcher()
    
    // Listener ko hold karne ke liye variable
    private var locationListener: ((LocationData) -> Void)?

    override init() {
        super.init()
        // Fetcher se data milte hi listener ko forward karein
        self.fetcher.onLocationUpdate = { [weak self] data in
            self?.locationListener?(data)
        }
    }

    func addLocationListener(listener: @escaping (LocationData) -> Void) {
        self.locationListener = listener
    }

    func removeLocationListener(listener: @escaping (LocationData) -> Void) {
        self.locationListener = nil
    }

    func setGeofence(lat: Double, lng: Double, radius: Double) throws -> Promise<Void> {
        return Promise.async { [weak self] in
            self?.fetcher.setGeofence(lat: lat, lng: lng, radius: radius)
        }
    }

    func getCurrentLocation() throws -> Promise<LocationData> {
        return Promise.async { [weak self] in
            guard let self = self else { throw NSError(domain: "Location", code: 0) }
            return try await withCheckedThrowingContinuation { continuation in
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

    func watchPosition() throws -> Promise<String> {
        return Promise.async { [weak self] in
            return self?.fetcher.startWatching() ?? ""
        }
    }

    func clearWatch(watchId: String) throws -> Promise<Void> {
        return Promise.async { [weak self] in
            DispatchQueue.main.async { self?.fetcher.stopWatching() }
        }
    }

    func resetDistance() throws -> Promise<Void> {
        return Promise.async { [weak self] in
            DispatchQueue.main.async { self?.fetcher.resetDistance() }
        }
    }

    func requestBatteryOptimizationExemption() throws -> Promise<Void> { return Promise.async { } }
    func startKillProofMode() throws -> Promise<Void> { return Promise.async { } }
    func stopKillProofMode() throws -> Promise<Void> { return Promise.async { } }

    func killMode() throws -> Promise<Void> {
        return Promise.async { [weak self] in
            DispatchQueue.main.async {
                self?.fetcher.stopWatching()
                self?.fetcher.resetDistance()
                self?.fetcher.disableBackground()
            }
        }
    }

    func openAutoStartSettings() throws -> Promise<Void> { return Promise.async { } }

    func sum(num1: Double, num2: Double) throws -> Double {
        return num1 + num2
    }
}

// MARK: - Location Fetcher Logic
class LocationFetcher: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    
    // React Native ko data bhejne ke liye closure
    var onLocationUpdate: ((LocationData) -> Void)?

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

    func startWatching() -> String {
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

    func disableBackground() {
        locationManager.allowsBackgroundLocationUpdates = false
        locationManager.pausesLocationUpdatesAutomatically = true
        locationManager.stopUpdatingLocation()
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        if let previousLoc = lastLocation {
            totalDistance += location.distance(from: previousLoc)
        }
        lastLocation = location

        let speed = location.speed >= 0 ? location.speed : 0.0
        let targetLoc = CLLocation(latitude: geofenceLat, longitude: geofenceLng)
        let isInside = location.distance(from: targetLoc) <= geofenceRadius

        if geocoder.isGeocoding { return }

        geocoder.reverseGeocodeLocation(location) { placemarks, _ in
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
            
            // Listener ko trigger karein
            self.onLocationUpdate?(data)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location Error: \(error.localizedDescription)")
    }
}