# Geolocation Data Monitoring and Update Schedule

## Overview

This document describes the monitoring and update schedule for geolocation data in the Onionoo project. The system has been enhanced to provide comprehensive geolocation information including country, region, city, and ASN data from MaxMind GeoLite2 databases via the Anyone-Protocol API service.

## Update Schedule Architecture

### Current Configuration

#### Onionoo Service
- **Update Frequency**: Every 60 minutes (configurable via `updater.period.minutes`)
- **Geolocation Cache Check**: Every hour during main update cycle
- **Cache Duration**: 1 hour (considered fresh)
- **Warning Threshold**: 70 minutes (cache getting stale)
- **Critical Threshold**: 2 hours (cache critically stale)

#### API Service
- **MaxMind Database Updates**: Weekly (manual or scheduled via cron)
- **Live Data Serving**: Real-time from cached databases
- **Update Script**: `scripts/update-geolite-db.js`

## Update Frequency Recommendations

### Current Setup Assessment

Based on the analysis, the current update frequencies are:

1. **Onionoo to API Service**: Every 60 minutes ✅ **Optimal**
2. **API Service MaxMind Updates**: Weekly ✅ **Adequate**

The 60-minute update interval for Onionoo is appropriate because:
- Relay geolocation data doesn't change frequently
- Most geo IP data changes happen gradually over weeks/months
- 1-hour frequency balances freshness with resource usage
- MaxMind recommends weekly updates for GeoLite2 databases

## Monitoring Implementation

### Cache Health Monitoring

The `ApiGeoLocationService` now provides enhanced monitoring with the following states:

- **FRESH** (0-60 minutes): Normal operation
- **STALE** (60-70 minutes): Expected between updates
- **WARNING** (70-120 minutes): Potential update issues
- **CRITICAL** (>120 minutes): Service degradation

### Log Levels

```java
// Normal operation
logger.info("Successfully updated geolocation data for {} fingerprints", count);

// Stale cache (expected)
logger.warn("Geolocation cache is stale. Last update: {} ms ago", age);

// Getting concerning
logger.warn("Geolocation cache is getting very stale. Last update: {} ms ago", age);

// Critical issue
logger.error("Geolocation cache is critically stale! Last update: {} ms ago", age);
```

### Statistics Available

The service provides detailed statistics including:
- Number of cached fingerprints
- Cache age in milliseconds and minutes  
- Cache status (FRESH/STALE/WARNING/CRITICAL)
- Last update timestamp

## Configuration Files

### Docker Compose Configuration

```yaml
services:
  onionoo-jar:
    environment:
      UPDATER_PERIOD: 60        # Update every 60 minutes
      UPDATER_OFFSET: 5         # Start 5 minutes after the hour
      API_SERVICE_URL: "http://host.docker.internal:3000"
```

### Production Deployment Configuration

```hcl
# operations/deploy-live.hcl
env {
  ONIONOO_HOST      = "http://127.0.0.1:8080"
  INTERVAL_MINUTES  = "60"                    # Hourly updates
  API_SERVICE_URL   = "http://api-service:3000"
}
```

## API Service Database Update Schedule

### Recommended Weekly Update Schedule

For production deployments, set up a weekly cron job to update MaxMind databases:

```bash
# Add to crontab for weekly Sunday updates at 2 AM UTC
0 2 * * 0 cd /path/to/api-service && npm run update-geo-ip-db >> /var/log/geolite-update.log 2>&1
```

### Manual Update Process

To update MaxMind databases manually:

```bash
cd /path/to/api-service
npm run update-geo-ip-db
```

Requirements:
- `LICENSE_KEY` environment variable (MaxMind license key)
- `GEODATADIR` environment variable (database storage directory)
- Write permissions to the database directory

## Monitoring and Alerting

### Health Check Endpoints

Monitor the system health through:

1. **Onionoo Statistics**: Available in application logs during update cycles
2. **API Service Health**: Check `/fingerprint-map/` endpoint response time and data freshness
3. **Cache Age Monitoring**: Watch for WARNING and CRITICAL cache states

### Recommended Alerts

Set up monitoring alerts for:

1. **Critical Cache Age**: Alert when cache is >2 hours old
2. **Update Failures**: Alert on API service geolocation update failures
3. **Database Update Failures**: Alert on MaxMind database update failures
4. **API Service Downtime**: Alert when API service is unreachable

### Sample Alert Conditions

```bash
# Log monitoring for critical cache state
grep -i "critically stale" /srv/onionoo/data/logs/*.log

# API service health check
curl -f http://api-service:3000/fingerprint-map/ > /dev/null

# Database freshness check (databases should be updated weekly)
find /usr/local/share/GeoIP -name "*.mmdb" -mtime +10 -exec echo "Database {} is over 10 days old" \;
```

## Troubleshooting Guide

### Common Issues

#### 1. Cache Consistently Stale
**Symptoms**: Logs show "cache is stale" warnings every cycle  
**Causes**: API service unreachable, network issues, authentication problems  
**Resolution**: Check API service status, network connectivity, and credentials

#### 2. Critical Cache State
**Symptoms**: "critically stale" error messages  
**Causes**: Extended API service outage, configuration issues  
**Resolution**: 
- Restart API service
- Check MaxMind database files exist and are readable
- Verify API service configuration

#### 3. Empty Cache
**Symptoms**: 0 fingerprints cached  
**Causes**: API service returning empty data, parsing issues  
**Resolution**:
- Check `/fingerprint-map/` endpoint manually
- Verify Onionoo service has relay data
- Check API service logs for errors

### Performance Considerations

#### Resource Usage
- Memory: ~100MB for 10K+ fingerprint cache
- Network: ~10MB transfer per update cycle
- CPU: Minimal impact during update

#### Scaling Recommendations
- For >50K relays: Consider increasing cache duration to 90 minutes
- For high-availability setups: Deploy multiple API service instances
- For data persistence: Ensure database directory is properly backed up

## Migration Notes

### From File-Based to API-Based Lookups

The system maintains backward compatibility:
- File-based lookups still work for IP addresses
- API-based lookups are used for fingerprint-based queries
- Fallback mechanisms ensure service continuity

### Deployment Checklist

1. ✅ Update Onionoo configuration with `API_SERVICE_URL`
2. ✅ Deploy API service with MaxMind databases
3. ✅ Set up weekly database update schedule
4. ✅ Configure monitoring and alerting
5. ✅ Test end-to-end geolocation data flow
6. ✅ Verify cache statistics and health monitoring

## Future Enhancements

### Potential Improvements

1. **Real-time Database Updates**: Automatic daily MaxMind database updates
2. **Cache Persistence**: Persist geolocation cache between restarts
3. **Multiple API Sources**: Support for multiple geolocation data providers
4. **Advanced Metrics**: Prometheus/Grafana integration for detailed monitoring
5. **Geographic Redundancy**: Multiple API service instances in different regions

### Monitoring Enhancements

1. **Metrics Export**: Export cache statistics to monitoring systems
2. **Health Dashboard**: Web-based dashboard for geolocation service health
3. **Automated Recovery**: Automatic retry mechanisms for failed updates
4. **Data Quality Metrics**: Track accuracy and completeness of geolocation data
