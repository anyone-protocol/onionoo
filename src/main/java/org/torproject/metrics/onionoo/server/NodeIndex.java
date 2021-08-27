/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.server;

import org.torproject.metrics.onionoo.docs.SummaryDocument;
import org.torproject.metrics.onionoo.updater.TorVersion;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

class NodeIndex {

  private String relaysPublishedString;

  public void setRelaysPublishedMillis(long relaysPublishedMillis) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    this.relaysPublishedString =
        dateTimeFormat.format(relaysPublishedMillis);
  }

  public String getRelaysPublishedString() {
    return relaysPublishedString;
  }

  private String bridgesPublishedString;

  public void setBridgesPublishedMillis(long bridgesPublishedMillis) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    this.bridgesPublishedString =
        dateTimeFormat.format(bridgesPublishedMillis);
  }

  public String getBridgesPublishedString() {
    return bridgesPublishedString;
  }

  private Map<String, SummaryDocument> relayFingerprintSummaryLines;

  public void setRelayFingerprintSummaryLines(
      Map<String, SummaryDocument> relayFingerprintSummaryLines) {
    this.relayFingerprintSummaryLines = relayFingerprintSummaryLines;
  }

  public Map<String, SummaryDocument> getRelayFingerprintSummaryLines() {
    return this.relayFingerprintSummaryLines;
  }

  private Map<String, SummaryDocument> bridgeFingerprintSummaryLines;

  public void setBridgeFingerprintSummaryLines(
      Map<String, SummaryDocument> bridgeFingerprintSummaryLines) {
    this.bridgeFingerprintSummaryLines = bridgeFingerprintSummaryLines;
  }

  public Map<String, SummaryDocument> getBridgeFingerprintSummaryLines() {
    return this.bridgeFingerprintSummaryLines;
  }

  private Map<String, Set<String>> relaysByCountryCode = null;

  public void setRelaysByCountryCode(
      Map<String, Set<String>> relaysByCountryCode) {
    this.relaysByCountryCode = relaysByCountryCode;
  }

  public Map<String, Set<String>> getRelaysByCountryCode() {
    return relaysByCountryCode;
  }

  private Map<String, Set<String>> relaysByAsNumber = null;

  public void setRelaysByAsNumber(
      Map<String, Set<String>> relaysByAsNumber) {
    this.relaysByAsNumber = relaysByAsNumber;
  }

  public Map<String, Set<String>> getRelaysByAsNumber() {
    return relaysByAsNumber;
  }

  private Map<String, Set<String>> relaysByAsName = null;

  public void setRelaysByAsName(Map<String, Set<String>> relaysByAsName) {
    this.relaysByAsName = relaysByAsName;
  }

  public Map<String, Set<String>> getRelaysByAsName() {
    return relaysByAsName;
  }

  private Map<String, Set<String>> relaysByFlag = null;

  public void setRelaysByFlag(Map<String, Set<String>> relaysByFlag) {
    this.relaysByFlag = relaysByFlag;
  }

  public Map<String, Set<String>> getRelaysByFlag() {
    return relaysByFlag;
  }

  private Map<String, Set<String>> bridgesByFlag = null;

  public void setBridgesByFlag(Map<String, Set<String>> bridgesByFlag) {
    this.bridgesByFlag = bridgesByFlag;
  }

  public Map<String, Set<String>> getBridgesByFlag() {
    return bridgesByFlag;
  }

  private Map<String, Set<String>> relaysByContact = null;

  public void setRelaysByContact(
      Map<String, Set<String>> relaysByContact) {
    this.relaysByContact = relaysByContact;
  }

  public Map<String, Set<String>> getRelaysByContact() {
    return relaysByContact;
  }

  private Map<String, Set<String>> relaysByFamily = null;

  public void setRelaysByFamily(Map<String, Set<String>> relaysByFamily) {
    this.relaysByFamily = relaysByFamily;
  }

  public Map<String, Set<String>> getRelaysByFamily() {
    return this.relaysByFamily;
  }

  private SortedMap<Integer, Set<String>> relaysByFirstSeenDays;

  public void setRelaysByFirstSeenDays(
      SortedMap<Integer, Set<String>> relaysByFirstSeenDays) {
    this.relaysByFirstSeenDays = relaysByFirstSeenDays;
  }

  public SortedMap<Integer, Set<String>> getRelaysByFirstSeenDays() {
    return relaysByFirstSeenDays;
  }

  private SortedMap<Integer, Set<String>> bridgesByFirstSeenDays;

  public void setBridgesByFirstSeenDays(
      SortedMap<Integer, Set<String>> bridgesByFirstSeenDays) {
    this.bridgesByFirstSeenDays = bridgesByFirstSeenDays;
  }

  public SortedMap<Integer, Set<String>> getBridgesByFirstSeenDays() {
    return bridgesByFirstSeenDays;
  }

  private SortedMap<Integer, Set<String>> relaysByLastSeenDays;

  public void setRelaysByLastSeenDays(
      SortedMap<Integer, Set<String>> relaysByLastSeenDays) {
    this.relaysByLastSeenDays = relaysByLastSeenDays;
  }

  public SortedMap<Integer, Set<String>> getRelaysByLastSeenDays() {
    return relaysByLastSeenDays;
  }

  private SortedMap<Integer, Set<String>> bridgesByLastSeenDays;

  public void setBridgesByLastSeenDays(
      SortedMap<Integer, Set<String>> bridgesByLastSeenDays) {
    this.bridgesByLastSeenDays = bridgesByLastSeenDays;
  }

  public SortedMap<Integer, Set<String>> getBridgesByLastSeenDays() {
    return bridgesByLastSeenDays;
  }

  private Map<TorVersion, Set<String>> relaysByVersion;

  public void setRelaysByVersion(Map<TorVersion, Set<String>> relaysByVersion) {
    this.relaysByVersion = relaysByVersion;
  }

  public Map<TorVersion, Set<String>> getRelaysByVersion() {
    return this.relaysByVersion;
  }

  private Map<TorVersion, Set<String>> bridgesByVersion;

  public void setBridgesByVersion(Map<TorVersion,
      Set<String>> bridgesByVersion) {
    this.bridgesByVersion = bridgesByVersion;
  }

  public Map<TorVersion, Set<String>> getBridgesByVersion() {
    return this.bridgesByVersion;
  }

  private Map<String, Set<String>> relaysByOperatingSystem;

  public void setRelaysByOperatingSystem(
      Map<String, Set<String>> relaysByOperatingSystem) {
    this.relaysByOperatingSystem = relaysByOperatingSystem;
  }

  public Map<String, Set<String>> getRelaysByOperatingSystem() {
    return this.relaysByOperatingSystem;
  }

  private Map<String, Set<String>> bridgesByOperatingSystem;

  public void setBridgesByOperatingSystem(
      Map<String, Set<String>> bridgesByOperatingSystem) {
    this.bridgesByOperatingSystem = bridgesByOperatingSystem;
  }

  public Map<String, Set<String>> getBridgesByOperatingSystem() {
    return this.bridgesByOperatingSystem;
  }

  private Map<String, Set<String>> relaysByHostName;

  public void setRelaysByHostName(Map<String, Set<String>> relaysByHostName) {
    this.relaysByHostName = relaysByHostName;
  }

  public Map<String, Set<String>> getRelaysByHostName() {
    return this.relaysByHostName;
  }

  private Map<Boolean, Set<String>> relaysByRecommendedVersion;

  public void setRelaysByRecommendedVersion(
      Map<Boolean, Set<String>> relaysByRecommendedVersion) {
    this.relaysByRecommendedVersion = relaysByRecommendedVersion;
  }

  public Map<Boolean, Set<String>> getRelaysByRecommendedVersion() {
    return this.relaysByRecommendedVersion;
  }

  private Map<Boolean, Set<String>> bridgesByRecommendedVersion;

  public void setBridgesByRecommendedVersion(
      Map<Boolean, Set<String>> bridgesByRecommendedVersion) {
    this.bridgesByRecommendedVersion = bridgesByRecommendedVersion;
  }

  public Map<Boolean, Set<String>> getBridgesByRecommendedVersion() {
    return this.bridgesByRecommendedVersion;
  }

  private Map<Boolean, Set<String>> relaysByOverloadStatus;

  public void setRelaysByOverloadStatus(
      Map<Boolean, Set<String>> relaysByOverloadStatus) {
    this.relaysByOverloadStatus = relaysByOverloadStatus;
  }

  public Map<Boolean, Set<String>> getRelaysByOverloadStatus() {
    return this.relaysByOverloadStatus;
  }

  private Map<Boolean, Set<String>> bridgesByOverloadStatus;

  public void setBridgesByOverloadStatus(
      Map<Boolean, Set<String>> bridgesByOverloadStatus) {
    this.bridgesByOverloadStatus = bridgesByOverloadStatus;
  }

  public Map<Boolean, Set<String>> getBridgesByOverloadStatus() {
    return this.bridgesByOverloadStatus;
  }
}
