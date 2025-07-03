/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.GeoipFile;
import org.torproject.descriptor.GeoipNamesFile;
import org.torproject.descriptor.impl.DescriptorReaderImpl;
import org.torproject.metrics.onionoo.util.FormattingUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.regex.Pattern;

// TODO: GeoIP feature updated to use API service for fingerprint-based lookups, fallback to file-based for IPv4 addresses.
public class LookupService {

  private static final Logger logger = LoggerFactory.getLogger(
      LookupService.class);

  private final File geoipDir;
  private final ApiGeoLocationService apiGeoLocationService;

  private GeoipFile geoip;

//  private GeoipFile geoip6;

  private GeoipNamesFile countries;

//  private GeoipNamesFile asns;

  private boolean hasAllFiles = false;

  public LookupService(File geoipDir) {
    this(geoipDir, null);
  }

  public LookupService(File geoipDir, String apiBaseUrl) {
    this.geoipDir = geoipDir;
    this.apiGeoLocationService = apiBaseUrl != null ? 
        new ApiGeoLocationService(apiBaseUrl) : null;
    this.findRequiredCsvFiles();
  }

  /* Make sure we have all required .csv files. */
  private void findRequiredCsvFiles() {
    File geoip = new File(this.geoipDir,
        "geoip");
    for (Descriptor g : new DescriptorReaderImpl().readDescriptors(geoip)) {
      if (g instanceof GeoipFile) {
        this.geoip = (GeoipFile) g;
      }
    }
//    File geoip6 = new File(this.geoipDir,
//        "geoip6");
//    for (Descriptor g : new DescriptorReaderImpl().readDescriptors(geoip6)) {
//      if (g instanceof GeoipFile) {
//        this.geoip6 = (GeoipFile) g;
//      }
//    }
    File countries = new File(this.geoipDir,
            "countries.txt");
    for (Descriptor g : new DescriptorReaderImpl().readDescriptors(countries)) {
      if (g instanceof GeoipNamesFile) {
        this.countries = (GeoipNamesFile) g;
      }
    }
//    File asns = new File(this.geoipDir,
//            "asn.txt");
//    for (Descriptor g : new DescriptorReaderImpl().readDescriptors(asns)) {
//      if (g instanceof GeoipNamesFile) {
//        this.asns = (GeoipNamesFile) g;
//      }
//    }
    this.hasAllFiles = (
            null != this.geoip
//            && null != this.geoip6
            && null != this.countries
//            && null != this.asns
    );
  }

  private final Pattern ipv4Pattern = Pattern.compile("^[0-9.]{7,15}$");

  /** Looks up address strings in the configured
   * files and returns all lookup results. */
  public SortedMap<String, LookupResult> lookup(
      SortedSet<String> addressStrings) {

    SortedMap<String, LookupResult> lookupResults = new TreeMap<>();

    if (!this.hasAllFiles) {
      return lookupResults;
    }

    /* Obtain a map from relay IP address strings to numbers. */
    Map<String, String> addressCountries = new HashMap<>();
    Map<String, String> addressAsn = new HashMap<>();
    for (String addressString : addressStrings) {
      GeoipFile geoipFile;
      if (ipv4Pattern.matcher(addressString).matches()) {
        geoipFile = this.geoip;
      } else {
        // geoip6 is not supported yet
//        geoipFile = this.geoip6;
        continue;
      }
      try {
        Optional<GeoipFile.GeoipEntry> entry =
            geoipFile.getEntry(InetAddress.getByName(addressString));
        if (entry.isPresent()) {
          String countryCode = entry.get().getCountryCode();
          if (null != countryCode) {
            addressCountries.put(addressString, countryCode);
          }
          String asn = entry.get().getAutonomousSystemNumber();
          if (null != asn) {
            addressAsn.put(addressString, asn);
          }
        }
      } catch (UnknownHostException e) {
        logger.error("Tried to look up " + addressString
                + " which is not an IP address.");
      }
    }

    /* Finally, put together lookup results. */
    for (String addressString : addressStrings) {
      LookupResult lookupResult = new LookupResult();
      String countryCode = addressCountries.getOrDefault(addressString, null);
      String asn = addressAsn.getOrDefault(addressString, null);
      if (null == countryCode && null == asn) {
        continue;
      }
      if (null != countryCode) {
        lookupResult.setCountryCode(countryCode.toLowerCase());
        lookupResult.setCountryName(this.countries.get(
                countryCode.toUpperCase()));
      }
//      if (null != asn) {
//        lookupResult.setAsNumber("AS" + asn);
//        lookupResult.setAsName(this.asns.get(asn));
//      }
      lookupResults.put(addressString, lookupResult);
    }

    /* Keep statistics. */
    this.addressesLookedUp += addressStrings.size();
    this.addressesCountryResolved += addressCountries.size();
    this.addressesAsnResolved += addressAsn.size();
    this.addressesResolved += lookupResults.size();

    return lookupResults;
  }

  /** Looks up geolocation for relay fingerprints using the API service.
   * Returns all lookup results. */
  public SortedMap<String, LookupResult> lookupByFingerprint(
      SortedSet<String> fingerprints) {

    SortedMap<String, LookupResult> lookupResults = new TreeMap<>();

    if (apiGeoLocationService == null) {
      logger.warn("API geolocation service not configured, skipping fingerprint lookups");
      return lookupResults;
    }

    /* Obtain geolocation data from API service for each fingerprint. */
    for (String fingerprint : fingerprints) {
      ApiGeoLocationService.GeolocationData geoData = 
          apiGeoLocationService.getGeolocationData(fingerprint);
      
      if (geoData != null) {
        LookupResult lookupResult = ApiGeoLocationService.toLookupResult(geoData);
        
        if (lookupResult != null) {
          lookupResults.put(fingerprint, lookupResult);
          this.addressesResolvedByApi++;
        }
      }
    }

    /* Keep statistics. */
    this.addressesLookedUpByApi += fingerprints.size();
    this.addressesResolvedByApiTotal += lookupResults.size();

    return lookupResults;
  }

  private BufferedReader createBufferedReaderFromUtf8File(File utf8File)
      throws FileNotFoundException {
    return this.createBufferedReaderFromFile(utf8File,
        StandardCharsets.UTF_8.newDecoder());
  }

  private BufferedReader createBufferedReaderFromFile(File file,
      CharsetDecoder dec) throws FileNotFoundException {
    dec.onMalformedInput(CodingErrorAction.REPORT);
    dec.onUnmappableCharacter(CodingErrorAction.REPORT);
    return new BufferedReader(new InputStreamReader(
        new FileInputStream(file), dec));
  }

  private int addressesLookedUp = 0;

  private int addressesResolved = 0;

  private int addressesCountryResolved = 0;

  private int addressesAsnResolved = 0;

  // New statistics for API-based lookups
  private int addressesLookedUpByApi = 0;

  private int addressesResolvedByApi = 0;

  private int addressesResolvedByApiTotal = 0;

  /** Updates geolocation data from the API service. Should be called by cron job. */
  public void updateGeolocationData() {
    if (apiGeoLocationService != null) {
      try {
        apiGeoLocationService.updateGeolocationData();
        logger.info("Successfully updated geolocation data from API service");
      } catch (Exception e) {
        logger.error("Failed to update geolocation data from API service", e);
      }
    } else {
      logger.warn("API geolocation service not configured, cannot update data");
    }
  }

  /** Returns a string with the number of addresses looked up and
   * resolved. */
  public String getStatsString() {
    StringBuilder stats = new StringBuilder();
    stats.append(String.format(
        "    %s addresses looked up (file-based)\n"
        + "    %s addresses resolved (any)\n"
        + "    %s addresses resolved (country)\n"
        + "    %s addresses resolved (ASN)\n",
        FormattingUtils.formatDecimalNumber(addressesLookedUp),
        FormattingUtils.formatDecimalNumber(addressesResolved),
        FormattingUtils.formatDecimalNumber(addressesCountryResolved),
        FormattingUtils.formatDecimalNumber(addressesAsnResolved)));
    
    if (apiGeoLocationService != null) {
      stats.append(String.format(
          "    %s fingerprints looked up (API-based)\n"
          + "    %s fingerprints resolved (API-based)\n"
          + "    API service stats:\n%s",
          FormattingUtils.formatDecimalNumber(addressesLookedUpByApi),
          FormattingUtils.formatDecimalNumber(addressesResolvedByApiTotal),
          apiGeoLocationService.getStatsString()));
    }
    
    return stats.toString();
  }
}

