# Onionoo API Service Integration

This document describes the integration of the Onionoo project with the Anyone-Protocol API service for geo IP data lookups.

## Overview

The Onionoo project has been updated to fetch geolocation data from the Anyone-Protocol API service instead of relying solely on local geo IP files. This provides more accurate and up-to-date geolocation information for relay fingerprints.

## Changes Made

### 1. New API Geolocation Service

- **File**: `src/main/java/org/torproject/metrics/onionoo/updater/ApiGeoLocationService.java`
- **Purpose**: Handles communication with the Anyone-Protocol API service to fetch fingerprint-based geolocation data
- **Features**: 
  - Fetches data from `/fingerprint-map/` endpoint
  - Caches data for 1 hour to reduce API calls
  - Provides statistics on cache usage

### 2. Enhanced Lookup Service

- **File**: `src/main/java/org/torproject/metrics/onionoo/updater/LookupService.java`
- **Changes**:
  - Added constructor parameter for API service URL
  - New method `lookupByFingerprint()` for API-based lookups
  - Added `updateGeolocationData()` method to refresh API cache
  - Enhanced statistics to include API lookup metrics
  - Maintains backward compatibility with file-based lookups

### 3. Updated Status Update Runner

- **File**: `src/main/java/org/torproject/metrics/onionoo/updater/StatusUpdateRunner.java`
- **Changes**:
  - Reads `API_SERVICE_URL` environment variable or `api.service.url` system property
  - Passes API URL to LookupService constructor
  - Added `updateGeolocationData()` method to trigger API cache refresh

### 4. Enhanced Node Details Status Updater

- **File**: `src/main/java/org/torproject/metrics/onionoo/updater/NodeDetailsStatusUpdater.java`
- **Changes**:
  - Modified `lookUpCitiesAndASes()` to try fingerprint-based lookups first
  - Falls back to IP-based lookups for fingerprints not found via API
  - Improved logging to show lookup statistics

### 5. Updated Cron Main

- **File**: `src/main/java/org/torproject/metrics/onionoo/cron/Main.java`
- **Changes**:
  - Added geolocation data update as part of the regular update cycle
  - API cache is refreshed before processing descriptors

## Configuration

### Environment Variable

Set the `API_SERVICE_URL` environment variable to point to your Anyone-Protocol API service:

```bash
export API_SERVICE_URL="https://api.ec.anyone.tech"
```

### System Property (Alternative)

Alternatively, you can use a system property:

```bash
java -Dapi.service.url="https://api.ec.anyone.tech" -jar onionoo.jar
```

### Deployment Configuration

The deployment files have been updated to automatically discover and configure the API service URL:

- **Live**: `operations/deploy-live.hcl` - Uses service discovery for `api-service-live`
- **Stage**: `operations/deploy-stage.hcl` - Uses service discovery for `api-service-stage`
- **Docker**: `docker/docker-compose.yml` - Uses `host.docker.internal:3000` for local development

## API Data Format

The API service provides geolocation data in this format:

```json
{
  "relay_fingerprint": {
    "hexId": "h3_hexagon_id",
    "coordinates": [latitude, longitude]
  }
}
```

## Behavior

1. **API First**: The system first attempts to get geolocation data for relay fingerprints using the API service
2. **Fallback**: For fingerprints not found via the API, it falls back to traditional IP-based lookups using local geo IP files
3. **Caching**: API data is cached for 1 hour to improve performance and reduce API load
4. **Statistics**: Enhanced logging provides visibility into lookup success rates for both methods

## Benefits

- **More Accurate**: Fingerprint-based lookups provide more precise geolocation data
- **Up-to-date**: API service can provide fresher data than local files
- **Hybrid Approach**: Maintains compatibility with existing IP-based lookups
- **Performance**: Caching reduces API calls while providing fresh data

## Backward Compatibility

The system remains fully backward compatible:
- If `API_SERVICE_URL` is not set, only file-based lookups are used
- If the API service is unavailable, it falls back to IP-based lookups
- Existing geo IP files in `/srv/onionoo/geoip/` are still used as fallback

## Monitoring

The API service integration includes comprehensive monitoring capabilities:

### Cache Health Monitoring
- **Cache Duration**: 1 hour (considered fresh)
- **Warning Threshold**: 70 minutes (getting stale)
- **Critical Threshold**: 2 hours (service degradation)

### Statistics and Logging
- Cache size (number of fingerprints)
- Cache age with status (FRESH/STALE/WARNING/CRITICAL)
- Last update timestamp
- Detailed error logging for different cache states

### Update Schedule
- **Onionoo Updates**: Every 60 minutes (configurable)
- **MaxMind Database Updates**: Weekly (recommended)
- **Cache Refresh**: Automatic during Onionoo update cycles

### Health Checks
Monitor the following for service health:
- Cache age and status in application logs
- API service response time and availability
- MaxMind database freshness (should be updated weekly)

For detailed monitoring setup and troubleshooting, see `GEOLOCATION_MONITORING.md`.

## Future Enhancements

1. **Country Data**: Enhance the API to provide country code and country name data
2. **ASN Data**: Add ASN (Autonomous System Number) information to the API response  
3. **Retry Logic**: Implement exponential backoff for API failures
4. **Health Checks**: Add endpoints to monitor API service health
