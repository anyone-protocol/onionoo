/* Copyright 2025 The Anyone-Protocol Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApiGeoLocationServiceTest {

  @Test
  public void testGeolocationDataCreation() {
    String hexId = "test-hex-id";
    double latitude = 37.7749;
    double longitude = -122.4194;
    String countryCode = "US";
    String countryName = "United States";
    String regionName = "California";
    String cityName = "San Francisco";
    String asNumber = "7018";
    String asName = "ATT-INTERNET4";
    
    ApiGeoLocationService.GeolocationData geoData = 
        new ApiGeoLocationService.GeolocationData(hexId, latitude, longitude,
            countryCode, countryName, regionName, cityName, asNumber, asName);
    
    assertEquals(hexId, geoData.getHexId());
    assertEquals(latitude, geoData.getLatitude(), 0.0001);
    assertEquals(longitude, geoData.getLongitude(), 0.0001);
    assertEquals(countryCode, geoData.getCountryCode());
    assertEquals(countryName, geoData.getCountryName());
    assertEquals(regionName, geoData.getRegionName());
    assertEquals(cityName, geoData.getCityName());
    assertEquals(asNumber, geoData.getAsNumber());
    assertEquals(asName, geoData.getAsName());
  }

  @Test
  public void testApiGeoLocationServiceCreation() {
    String apiBaseUrl = "https://api.example.com";
    ApiGeoLocationService service = new ApiGeoLocationService(apiBaseUrl);
    
    assertNotNull(service);
    assertEquals(0, service.getCachedFingerprintCount());
    assertTrue(service.isCacheStale()); // Should be stale initially
  }

  @Test
  public void testApiGeoLocationServiceWithTrailingSlash() {
    String apiBaseUrl = "https://api.example.com/";
    ApiGeoLocationService service = new ApiGeoLocationService(apiBaseUrl);
    
    assertNotNull(service);
  }

  @Test
  public void testLookupWithEmptyCache() {
    String apiBaseUrl = "https://api.example.com";
    ApiGeoLocationService service = new ApiGeoLocationService(apiBaseUrl);
    
    ApiGeoLocationService.GeolocationData result = 
        service.getGeolocationData("test-fingerprint");
    
    assertNull(result);
  }

  @Test
  public void testStatsString() {
    String apiBaseUrl = "https://api.example.com";
    ApiGeoLocationService service = new ApiGeoLocationService(apiBaseUrl);
    
    String stats = service.getStatsString();
    assertNotNull(stats);
    assertTrue(stats.contains("0 fingerprint geolocations cached"));
    assertTrue(stats.contains("Cache is STALE"));
  }

  @Test
  public void testToLookupResult() {
    String hexId = "test-hex-id";
    double latitude = 37.7749;
    double longitude = -122.4194;
    String countryCode = "US";
    String countryName = "United States";
    String regionName = "California";
    String cityName = "San Francisco";
    String asNumber = "7018";
    String asName = "ATT-INTERNET4";
    
    ApiGeoLocationService.GeolocationData geoData = 
        new ApiGeoLocationService.GeolocationData(hexId, latitude, longitude,
            countryCode, countryName, regionName, cityName, asNumber, asName);
    
    LookupResult result = ApiGeoLocationService.toLookupResult(geoData);
    
    assertNotNull(result);
    assertEquals((float) latitude, result.getLatitude(), 0.0001);
    assertEquals((float) longitude, result.getLongitude(), 0.0001);
    assertEquals(countryCode, result.getCountryCode());
    assertEquals(countryName, result.getCountryName());
    assertEquals(regionName, result.getRegionName());
    assertEquals(cityName, result.getCityName());
    assertEquals(asNumber, result.getAsNumber());
    assertEquals(asName, result.getAsName());
  }

  @Test
  public void testToLookupResultWithNullInput() {
    LookupResult result = ApiGeoLocationService.toLookupResult(null);
    assertNull(result);
  }
}
