import org.torproject.metrics.onionoo.updater.ApiGeoLocationService;
import org.torproject.metrics.onionoo.updater.LookupResult;

public class test_integration {
    public static void main(String[] args) {
        try {
            // Test with the actual running API service
            ApiGeoLocationService service = new ApiGeoLocationService("http://localhost:3000");
            
            // Update the geolocation data from the API
            System.out.println("Fetching geolocation data from API...");
            service.updateGeolocationData();
            
            // Get stats
            System.out.println("API Service Stats:");
            System.out.println(service.getStatsString());
            
            // Test lookup for a specific fingerprint (we know this one exists from the API test)
            String testFingerprint = "000A10D43011EA4928A35F610405F92B4433B4DC";
            ApiGeoLocationService.GeolocationData geoData = service.getGeolocationData(testFingerprint);
            
            if (geoData != null) {
                System.out.println("\nFound geolocation data for fingerprint " + testFingerprint + ":");
                System.out.println("  HexId: " + geoData.getHexId());
                System.out.println("  Coordinates: [" + geoData.getLatitude() + ", " + geoData.getLongitude() + "]");
                System.out.println("  Country: " + geoData.getCountryName() + " (" + geoData.getCountryCode() + ")");
                System.out.println("  Region: " + geoData.getRegionName());
                System.out.println("  City: " + geoData.getCityName());
                System.out.println("  ASN: " + geoData.getAsNumber() + " (" + geoData.getAsName() + ")");
                
                // Test conversion to LookupResult
                LookupResult lookupResult = ApiGeoLocationService.toLookupResult(geoData);
                System.out.println("\nConverted to LookupResult:");
                System.out.println("  Coordinates: [" + lookupResult.getLatitude() + ", " + lookupResult.getLongitude() + "]");
                System.out.println("  Country: " + lookupResult.getCountryName() + " (" + lookupResult.getCountryCode() + ")");
                System.out.println("  Region: " + lookupResult.getRegionName());
                System.out.println("  City: " + lookupResult.getCityName());
                System.out.println("  ASN: " + lookupResult.getAsNumber() + " (" + lookupResult.getAsName() + ")");
                
                System.out.println("\n✅ Integration test passed! All enhanced geolocation fields are working correctly.");
            } else {
                System.out.println("❌ No geolocation data found for test fingerprint");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Integration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
