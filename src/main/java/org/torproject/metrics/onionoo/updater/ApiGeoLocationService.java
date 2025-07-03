/* Copyright 2025 The ATOR Project
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
 * Service to fetch geolocation data from the ATOR API service.
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

  public static class GeolocationData {
    private final String hexId;
    private final double latitude;
    private final double longitude;

    public GeolocationData(String hexId, double latitude, double longitude) {
      this.hexId = hexId;
      this.latitude = latitude;
      this.longitude = longitude;
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
            
            newLocationMap.put(fingerprint.toUpperCase(), 
                new GeolocationData(hexId, latitude, longitude));
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
      logger.warn("Geolocation cache is stale. Last update: {} ms ago", 
          currentTime - lastUpdateTime);
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
   * Returns statistics about the geolocation service.
   */
  public String getStatsString() {
    long currentTime = System.currentTimeMillis();
    long cacheAge = currentTime - lastUpdateTime;
    
    return String.format(
        "    %d fingerprint geolocations cached\n"
        + "    Cache age: %d ms\n"
        + "    Cache is %s\n",
        fingerprintLocationMap.size(),
        cacheAge,
        isCacheStale() ? "STALE" : "fresh");
  }
}
