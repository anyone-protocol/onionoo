/* Copyright 2025 The Anyone-Protocol Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to fetch geolocation data from the Anyone-Protocol API service.
 * This replaces the old file-based geo IP lookup with API-based lookups.
 */
public class ApiGeoLocationService {

  private static final Logger logger = LoggerFactory.getLogger(
      ApiGeoLocationService.class);

  private final String apiBaseUrl;
  private final ObjectMapper objectMapper;
  private Map<String, GeolocationData> fingerprintLocationMap;
  private long lastUpdateTime = 0;
  private static final long CACHE_DURATION_MS = 60 * 60 * 1000; // 1 hour
  private static final long STALE_WARNING_THRESHOLD_MS = 70 * 60 * 1000; // 70 minutes
  private static final long CRITICAL_THRESHOLD_MS = 120 * 60 * 1000; // 2 hours

  public static class GeolocationData {
    private final String hexId;
    private final double latitude;
    private final double longitude;
    private final String countryCode;
    private final String countryName;
    private final String regionName;
    private final String cityName;
    private final String asNumber;
    private final String asName;

    public GeolocationData(String hexId, double latitude, double longitude,
                          String countryCode, String countryName, String regionName,
                          String cityName, String asNumber, String asName) {
      this.hexId = hexId;
      this.latitude = latitude;
      this.longitude = longitude;
      this.countryCode = countryCode;
      this.countryName = countryName;
      this.regionName = regionName;
      this.cityName = cityName;
      this.asNumber = asNumber;
      this.asName = asName;
    }

    public String getHexId() {
      return hexId;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    public String getCountryCode() {
      return countryCode;
    }

    public String getCountryName() {
      return countryName;
    }

    public String getRegionName() {
      return regionName;
    }

    public String getCityName() {
      return cityName;
    }

    public String getAsNumber() {
      return asNumber;
    }

    public String getAsName() {
      return asName;
    }
  }

  public ApiGeoLocationService(String apiBaseUrl) {
    this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
    this.objectMapper = new ObjectMapper();
    this.fingerprintLocationMap = new ConcurrentHashMap<>();
  }

  /**
   * Fetches fresh geolocation data from the API service.
   * This method should be called by the cron job to update the cache.
   */
  public void updateGeolocationData() throws IOException {
    logger.info("Fetching geolocation data from API service: {}", apiBaseUrl);
    
    String url = apiBaseUrl + "/fingerprint-map/";
    HttpURLConnection connection = null;
    
    try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(30000); // 30 seconds
      connection.setReadTimeout(60000); // 60 seconds
      connection.setRequestProperty("Accept", "application/json");

      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("API request failed with response code: " + responseCode);
      }

      StringBuilder response = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
      }

      JsonNode rootNode = objectMapper.readTree(response.toString());
      Map<String, GeolocationData> newLocationMap = new ConcurrentHashMap<>();

      rootNode.fields().forEachRemaining(entry -> {
        String fingerprint = entry.getKey();
        JsonNode locationData = entry.getValue();
        
        if (locationData.has("hexId") && locationData.has("coordinates")) {
          String hexId = locationData.get("hexId").asText();
          JsonNode coordinates = locationData.get("coordinates");
          
          if (coordinates.isArray() && coordinates.size() >= 2) {
            double latitude = coordinates.get(0).asDouble();
            double longitude = coordinates.get(1).asDouble();
            
            // Extract enhanced geolocation data
            String countryCode = locationData.has("countryCode") ? 
                locationData.get("countryCode").asText(null) : null;
            String countryName = locationData.has("countryName") ? 
                locationData.get("countryName").asText(null) : null;
            String regionName = locationData.has("regionName") ? 
                locationData.get("regionName").asText(null) : null;
            String cityName = locationData.has("cityName") ? 
                locationData.get("cityName").asText(null) : null;
            String asNumber = locationData.has("asNumber") ? 
                locationData.get("asNumber").asText(null) : null;
            String asName = locationData.has("asName") ? 
                locationData.get("asName").asText(null) : null;
            
            newLocationMap.put(fingerprint.toUpperCase(), 
                new GeolocationData(hexId, latitude, longitude, countryCode, 
                    countryName, regionName, cityName, asNumber, asName));
          }
        }
      });

      this.fingerprintLocationMap = newLocationMap;
      this.lastUpdateTime = System.currentTimeMillis();
      
      logger.info("Successfully updated geolocation data for {} fingerprints", 
          newLocationMap.size());
      
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * Gets geolocation data for a specific fingerprint.
   * 
   * @param fingerprint The relay fingerprint (case-insensitive)
   * @return GeolocationData if found, null otherwise
   */
  public GeolocationData getGeolocationData(String fingerprint) {
    if (fingerprint == null) {
      return null;
    }
    
    // Check if cache needs refresh
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastUpdateTime > CACHE_DURATION_MS) {
      if (currentTime - lastUpdateTime > CRITICAL_THRESHOLD_MS) {
        logger.error("Geolocation cache is critically stale! Last update: {} ms ago", 
            currentTime - lastUpdateTime);
      } else if (currentTime - lastUpdateTime > STALE_WARNING_THRESHOLD_MS) {
        logger.warn("Geolocation cache is getting very stale. Last update: {} ms ago", 
            currentTime - lastUpdateTime);
      } else {
        logger.warn("Geolocation cache is stale. Last update: {} ms ago", 
            currentTime - lastUpdateTime);
      }
    }
    
    return fingerprintLocationMap.get(fingerprint.toUpperCase());
  }

  /**
   * Returns the number of fingerprints currently cached.
   */
  public int getCachedFingerprintCount() {
    return fingerprintLocationMap.size();
  }

  /**
   * Returns the time when the cache was last updated.
   */
  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  /**
   * Checks if the cache is stale and needs updating.
   */
  public boolean isCacheStale() {
    return System.currentTimeMillis() - lastUpdateTime > CACHE_DURATION_MS;
  }

  /**
   * Checks if the cache is critically stale (over 2 hours old).
   */
  public boolean isCacheCriticallyStale() {
    return System.currentTimeMillis() - lastUpdateTime > CRITICAL_THRESHOLD_MS;
  }

  /**
   * Gets the cache age category for monitoring purposes.
   */
  public String getCacheAgeCategory() {
    long cacheAge = System.currentTimeMillis() - lastUpdateTime;
    if (cacheAge < CACHE_DURATION_MS) {
      return "FRESH";
    } else if (cacheAge < STALE_WARNING_THRESHOLD_MS) {
      return "STALE";
    } else if (cacheAge < CRITICAL_THRESHOLD_MS) {
      return "WARNING";
    } else {
      return "CRITICAL";
    }
  }

  /**
   * Returns statistics about the geolocation service.
   */
  public String getStatsString() {
    long currentTime = System.currentTimeMillis();
    long cacheAge = currentTime - lastUpdateTime;
    String ageCategory = getCacheAgeCategory();
    
    return String.format(
        "    %d fingerprint geolocations cached\n"
        + "    Cache age: %d ms (%d minutes)\n"
        + "    Cache status: %s\n"
        + "    Last update: %s\n",
        fingerprintLocationMap.size(),
        cacheAge,
        cacheAge / (60 * 1000),
        ageCategory,
        lastUpdateTime > 0 ? new java.util.Date(lastUpdateTime).toString() : "Never");
  }

  /**
   * Converts GeolocationData to a LookupResult with all geolocation fields populated.
   * This provides compatibility with the existing lookup infrastructure.
   * 
   * @param geoData The GeolocationData to convert
   * @return A populated LookupResult, or null if geoData is null
   */
  public static LookupResult toLookupResult(GeolocationData geoData) {
    if (geoData == null) {
      return null;
    }

    LookupResult result = new LookupResult();
    result.setLatitude((float) geoData.getLatitude());
    result.setLongitude((float) geoData.getLongitude());
    result.setCountryCode(geoData.getCountryCode());
    result.setCountryName(geoData.getCountryName());
    result.setRegionName(geoData.getRegionName());
    result.setCityName(geoData.getCityName());
    result.setAsNumber(geoData.getAsNumber());
    result.setAsName(geoData.getAsName());
    
    return result;
  }
}
