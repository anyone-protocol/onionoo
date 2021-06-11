/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import org.torproject.descriptor.*;
import org.torproject.descriptor.impl.DescriptorReaderImpl;
import org.torproject.descriptor.impl.GeoipFileImpl;
import org.torproject.descriptor.impl.GeoipNamesFileImpl;
import org.torproject.metrics.onionoo.util.FormattingUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.DescriptorRead;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class LookupService {

  private static final Logger logger = LoggerFactory.getLogger(
      LookupService.class);

  private final File geoipDir;

  private GeoipFile geoip;

  private GeoipFile geoip6;

  private GeoipNamesFile countries;

  private GeoipNamesFile asns;

  private boolean hasAllFiles = false;

  public LookupService(File geoipDir) {
    this.geoipDir = geoipDir;
    this.findRequiredCsvFiles();
  }

  /* Make sure we have all required .csv files. */
  private void findRequiredCsvFiles() {
    DescriptorReader dr = new DescriptorReaderImpl();
    File geoip = new File(this.geoipDir,
        "geoip");
    for (Descriptor g : dr.readDescriptors(geoip)) {
      if (g instanceof GeoipFile) {
        this.geoip = (GeoipFile) g;
      }
    }
    File geoip6 = new File(this.geoipDir,
        "geoip6");
    for (Descriptor g : dr.readDescriptors(geoip6)) {
      if (g instanceof GeoipFile) {
        this.geoip6 = (GeoipFile) g;
      }
    }
    File countries = new File(this.geoipDir,
            "countries.txt");
    for (Descriptor g : dr.readDescriptors(countries)) {
      if (g instanceof GeoipNamesFile) {
        this.countries = (GeoipNamesFile) g;
      }
    }
    File asns = new File(this.geoipDir,
            "asn.txt");
    for (Descriptor g : dr.readDescriptors(asns)) {
      if (g instanceof GeoipNamesFile) {
        this.asns = (GeoipNamesFile) g;
      }
    }
    this.hasAllFiles = true;
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
        geoipFile = this.geoip6;
      }
      try {
        Optional<GeoipFile.GeoipEntry> entry = geoipFile.getEntry(InetAddress.getByName(addressString));
        if (entry.isPresent()) {
          String countryCode = entry.get().getCountryCode();
          if (countryCode.length() > 1) {
            addressCountries.put(addressString, countryCode);
          }
          String asn = entry.get().getAutonomousSystemNumber();
          if (asn.length() > 1) {
            addressAsn.put(addressString, asn);
          }
        }
      } catch (UnknownHostException e) {
        logger.error("Tried to look up " + addressString + " which is not an IP address.");
      }
    }

    /* Finally, put together lookup results. */
    for (String addressString : addressStrings) {
      LookupResult lookupResult = new LookupResult();
      if (addressCountries.containsKey(addressString)) {
        String countryCode = addressCountries.get(addressString).toLowerCase();
        lookupResult.setCountryCode(countryCode);
        lookupResult.setCountryName(this.countries.get(countryCode));
      }
      if (addressAsn.containsKey(addressString)) {
        String asn = addressAsn.get(addressString);
        lookupResult.setAsNumber(asn);
        lookupResult.setAsName(this.asns.get(asn));
      }
      lookupResults.put(addressString, lookupResult);
    }

    /* Keep statistics. */
    this.addressesLookedUp += addressStrings.size();
    this.addressesResolved += lookupResults.size();

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

  /** Returns a string with the number of addresses looked up and
   * resolved. */
  public String getStatsString() {
    return String.format(
        "    %s addresses looked up\n"
        + "    %s addresses resolved\n",
        FormattingUtils.formatDecimalNumber(addressesLookedUp),
        FormattingUtils.formatDecimalNumber(addressesResolved));
  }
}

