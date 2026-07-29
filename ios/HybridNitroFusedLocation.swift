/**
 * Created by Upendra Singh
 * MIT License
 */

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
}

// MARK: - Location Fetcher Logic
class LocationFetcher: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    
    // React Native ko data bhejne ke liye closure
    var onLocationUpdate: ((LocationData) -> Void)?

    private var totalDistance: Double = 0.0
    private var lastLocation: CLLocation?

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 5
        locationManager.requestWhenInUseAuthorization()
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

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        if let previousLoc = lastLocation {
            totalDistance += location.distance(from: previousLoc)
        }
        lastLocation = location

        let speed = location.speed >= 0 ? location.speed : 0.0

        if geocoder.isGeocoding { return }

        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, _ in
            guard let self = self else { return }
            let p = placemarks?.first
            
            let lat = location.coordinate.latitude
            let lng = location.coordinate.longitude
            let acc = location.horizontalAccuracy
            let name = p?.name ?? "Unknown"
            let city = p?.locality ?? "Unknown"
            let state = p?.administrativeArea ?? "Unknown"
            let country = p?.country ?? "Unknown"
            let pincode = p?.postalCode ?? "Unknown"
            let dist = self.totalDistance
            let spd = speed

            let data = LocationData(
                latitude: lat,
                longitude: lng,
                accuracy: acc,
                address: name,
                city: city,
                state: state,
                country: country,
                pincode: pincode,
                distance: dist,
                speed: spd,
                isInsideGeofence: false
            )
            
            // Listener ko trigger karein
            self.onLocationUpdate?(data)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location Error: \(error.localizedDescription)")
    }
}