import Foundation
import NitroModules
import CoreLocation

// Main Nitro Class
class HybridNitroFusedLocation: HybridNitroFusedLocationSpec {
    
    // Memory mein hold rakhne ke liye variable
    private var fetcher: LocationFetcher?
    
    // JS se call hone wala main function
    func getCurrentLocation() throws -> Promise<LocationData> {
        return Promise.async {
            return try await withCheckedThrowingContinuation { continuation in
                // 👇 iOS ko bol rahe hain ki yeh kaam Main Thread par karo
                DispatchQueue.main.async {
                    self.fetcher = LocationFetcher()
                    self.fetcher?.fetchLocation { result in
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

    // Protocol ke liye zaroori function
    func sum(num1: Double, num2: Double) throws -> Double {
        return num1 + num2
    }
}

// iOS Location Fetcher Logic
class LocationFetcher: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private var completion: ((Result<LocationData, Error>) -> Void)?

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }

    func fetchLocation(completion: @escaping (Result<LocationData, Error>) -> Void) {
        self.completion = completion
        locationManager.requestWhenInUseAuthorization()
        locationManager.requestLocation() // Location mangna shuru
    }

    // Success: Jab location mil jaye
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
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
                distance: 0.0
            )
            
            self.completion?(.success(data))
            self.completion = nil
        }
    }

    // Failed: Agar koi error aaye
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        self.completion?(.failure(error))
        self.completion = nil
    }
}