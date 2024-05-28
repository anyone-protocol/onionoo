/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Define details documents files. These are saved in
 * /out/details and served via the /details endpoint.
 *
 * <p>DetailsDocument does not extend the Document methods:
 * {@see Document#setFromDocumentString(String documentString)} and
 * {@see Document#toDocumentString()}(String documentString)</p>
 *
 * <p>The document is instead writte to file via the
 * {@code objectMapper.writeValueAsString(document}
 * methond in {@see DocumentStore}.</p>
 *
 * <p>DetailsDocument do not support JSON in order not to break
 * compatibility with older document formats.</p>
 */

public class DetailsDocument extends Document {

  private String nickname;

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getNickname() {
    return this.nickname;
  }

  private String fingerprint;

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  public String getFingerprint() {
    return this.fingerprint;
  }

  private String hashedFingerprint;

  public void setHashedFingerprint(String hashedFingerprint) {
    this.hashedFingerprint = hashedFingerprint;
  }

  public String getHashedFingerprint() {
    return this.hashedFingerprint;
  }

  private List<String> orAddresses;

  public void setOrAddresses(List<String> orAddresses) {
    this.orAddresses = orAddresses;
  }

  public List<String> getOrAddresses() {
    return this.orAddresses;
  }

  private List<String> exitAddresses;

  public void setExitAddresses(List<String> exitAddresses) {
    this.exitAddresses = !exitAddresses.isEmpty() ? exitAddresses : null;
  }

  public List<String> getExitAddresses() {
    return this.exitAddresses == null ? new ArrayList<>()
        : this.exitAddresses;
  }

  private String dirAddress;

  public void setDirAddress(String dirAddress) {
    this.dirAddress = dirAddress;
  }

  public String getDirAddress() {
    return this.dirAddress;
  }

  private String lastSeen;

  public void setLastSeen(long lastSeen) {
    this.lastSeen = DateTimeHelper.format(lastSeen);
  }

  public long getLastSeen() {
    return DateTimeHelper.parse(this.lastSeen);
  }

  private String lastChangedAddressOrPort;

  public void setLastChangedAddressOrPort(
      long lastChangedAddressOrPort) {
    this.lastChangedAddressOrPort = DateTimeHelper.format(
        lastChangedAddressOrPort);
  }

  public long getLastChangedAddressOrPort() {
    return DateTimeHelper.parse(this.lastChangedAddressOrPort);
  }

  private String firstSeen;

  public void setFirstSeen(long firstSeen) {
    this.firstSeen = DateTimeHelper.format(firstSeen);
  }

  public long getFirstSeen() {
    return DateTimeHelper.parse(this.firstSeen);
  }

  private Boolean running;

  public void setRunning(Boolean running) {
    this.running = running;
  }

  public Boolean isRunning() {
    return this.running;
  }

  private SortedSet<String> flags;

  public void setFlags(SortedSet<String> flags) {
    this.flags = flags;
  }

  public SortedSet<String> getFlags() {
    return this.flags;
  }

  private String country;

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCountry() {
    return this.country;
  }

  private String countryName;

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getCountryName() {
    return this.countryName;
  }

  private String regionName;

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getRegionName() {
    return this.regionName;
  }

  private String cityName;

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public String getCityName() {
    return this.cityName;
  }

  private Float latitude;

  public void setLatitude(Float latitude) {
    this.latitude = latitude;
  }

  public Float getLatitude() {
    return this.latitude;
  }

  private Float longitude;

  public void setLongitude(Float longitude) {
    this.longitude = longitude;
  }

  public Float getLongitude() {
    return this.longitude;
  }

  private String as;

  public void setAs(String as) {
    this.as = as;
  }

  public String getAs() {
    return this.as;
  }

  private String asName;

  public void setAsName(String asName) {
    this.asName = asName;
  }

  public String getAsName() {
    return this.asName;
  }

  private Long consensusWeight;

  public void setConsensusWeight(Long consensusWeight) {
    this.consensusWeight = consensusWeight;
  }

  public Long getConsensusWeight() {
    return this.consensusWeight;
  }

  private String hostName;

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getHostName() {
    return this.hostName;
  }

  private SortedSet<String> verifiedHostNames;

  public void setVerifiedHostNames(SortedSet<String> verifiedHostNames) {
    this.verifiedHostNames = verifiedHostNames;
  }

  public SortedSet<String> getVerifiedHostNames() {
    return this.verifiedHostNames;
  }

  private SortedSet<String> unverifiedHostNames;

  public void setUnverifiedHostNames(SortedSet<String> unverifiedHostNames) {
    this.unverifiedHostNames = unverifiedHostNames;
  }

  public SortedSet<String> getUnverifiedHostNames() {
    return this.unverifiedHostNames;
  }

  private String lastRestarted;

  public void setLastRestarted(Long lastRestarted) {
    this.lastRestarted = (lastRestarted == null ? null
        : DateTimeHelper.format(lastRestarted));
  }

  public Long getLastRestarted() {
    return this.lastRestarted == null ? null :
        DateTimeHelper.parse(this.lastRestarted);
  }

  private Integer bandwidthRate;

  public void setBandwidthRate(Integer bandwidthRate) {
    this.bandwidthRate = bandwidthRate;
  }

  public Integer getBandwidthRate() {
    return this.bandwidthRate;
  }

  private Integer bandwidthBurst;

  public void setBandwidthBurst(Integer bandwidthBurst) {
    this.bandwidthBurst = bandwidthBurst;
  }

  public Integer getBandwidthBurst() {
    return this.bandwidthBurst;
  }

  private Integer observedBandwidth;

  public void setObservedBandwidth(Integer observedBandwidth) {
    this.observedBandwidth = observedBandwidth;
  }

  public Integer getObservedBandwidth() {
    return this.observedBandwidth;
  }

  private Integer advertisedBandwidth;

  public void setAdvertisedBandwidth(Integer advertisedBandwidth) {
    this.advertisedBandwidth = advertisedBandwidth;
  }

  public Integer getAdvertisedBandwidth() {
    return this.advertisedBandwidth;
  }

  private Long overloadGeneralTimestamp;

  public void setOverloadGeneralTimestamp(Long overloadGeneralTimestamp) {
    this.overloadGeneralTimestamp = overloadGeneralTimestamp;
  }

  public Long getOverloadGeneralTimestamp() {
    return this.overloadGeneralTimestamp;
  }

  private List<String> exitPolicy;

  public void setExitPolicy(List<String> exitPolicy) {
    this.exitPolicy = exitPolicy;
  }

  public List<String> getExitPolicy() {
    return this.exitPolicy;
  }

  private Map<String, List<String>> exitPolicySummary;

  public void setExitPolicySummary(
      Map<String, List<String>> exitPolicySummary) {
    this.exitPolicySummary = exitPolicySummary;
  }

  public Map<String, List<String>> getExitPolicySummary() {
    return this.exitPolicySummary;
  }

  private Map<String, List<String>> exitPolicyV6Summary;

  public void setExitPolicyV6Summary(
      Map<String, List<String>> exitPolicyV6Summary) {
    this.exitPolicyV6Summary = exitPolicyV6Summary;
  }

  public Map<String, List<String>> getExitPolicyV6Summary() {
    return this.exitPolicyV6Summary;
  }

  private String contact;

  /**
   * Santize the contact field if it contains a percent char by removing it.
   */
  public void setContact(String contact) {
    if ((contact != null) && (contact.indexOf('%') != -1)) {
      contact = contact.replace("%", "");
    }
    this.contact = contact;
  }

  public String getContact() {
    return this.contact;
  }

  private String platform;

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getPlatform() {
    return this.platform;
  }

  private String version;

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return this.version;
  }

  private String versionStatus;

  public void setVersionStatus(String versionStatus) {
    this.versionStatus = versionStatus;
  }

  public String getVersionStatus() {
    return this.versionStatus;
  }

  private SortedSet<String> allegedFamily;

  public void setAllegedFamily(SortedSet<String> allegedFamily) {
    this.allegedFamily = allegedFamily;
  }

  public SortedSet<String> getAllegedFamily() {
    return this.allegedFamily;
  }

  private SortedSet<String> effectiveFamily;

  public void setEffectiveFamily(SortedSet<String> effectiveFamily) {
    this.effectiveFamily = effectiveFamily;
  }

  public SortedSet<String> getEffectiveFamily() {
    return this.effectiveFamily;
  }

  private SortedSet<String> indirectFamily;

  public void setIndirectFamily(SortedSet<String> indirectFamily) {
    this.indirectFamily = indirectFamily;
  }

  public SortedSet<String> getIndirectFamily() {
    return this.indirectFamily;
  }

  private Float consensusWeightFraction;

  /** Sets the consensus weight fraction to the given value, but only if
   * that value is neither null nor negative. */
  public void setConsensusWeightFraction(Float consensusWeightFraction) {
    if (consensusWeightFraction == null
        || consensusWeightFraction >= 0.0) {
      this.consensusWeightFraction = consensusWeightFraction;
    }
  }

  public Float getConsensusWeightFraction() {
    return this.consensusWeightFraction;
  }

  private Float guardProbability;

  /** Sets the guard probability to the given value, but only if that
   * value is neither null nor negative. */
  public void setGuardProbability(Float guardProbability) {
    if (guardProbability == null || guardProbability >= 0.0) {
      this.guardProbability = guardProbability;
    }
  }

  public Float getGuardProbability() {
    return this.guardProbability;
  }

  private Float middleProbability;

  /** Sets the middle probability to the given value, but only if that
   * value is neither null nor negative. */
  public void setMiddleProbability(Float middleProbability) {
    if (middleProbability == null || middleProbability >= 0.0) {
      this.middleProbability = middleProbability;
    }
  }

  public Float getMiddleProbability() {
    return this.middleProbability;
  }

  private Float exitProbability;

  /** Sets the exit probability to the given value, but only if that
   * value is neither null nor negative. */
  public void setExitProbability(Float exitProbability) {
    if (exitProbability == null || exitProbability >= 0.0) {
      this.exitProbability = exitProbability;
    }
  }

  public Float getExitProbability() {
    return this.exitProbability;
  }

  private Boolean recommendedVersion;

  public void setRecommendedVersion(Boolean recommendedVersion) {
    this.recommendedVersion = recommendedVersion;
  }

  public Boolean isRecommendedVersion() {
    return this.recommendedVersion;
  }

  private Boolean hibernating;

  public void setHibernating(Boolean hibernating) {
    this.hibernating = hibernating;
  }

  public Boolean isHibernating() {
    return this.hibernating;
  }

  private List<String> transports;

  public void setTransports(List<String> transports) {
    this.transports = (transports != null && !transports.isEmpty())
        ? transports : null;
  }

  public List<String> getTransports() {
    return this.transports;
  }

  private Boolean measured;

  public void setMeasured(Boolean measured) {
    this.measured = measured;
  }

  public Boolean isMeasured() {
    return this.measured;
  }

  private List<String> unreachableOrAddresses;

  public void setUnreachableOrAddresses(List<String> unreachableOrAddresses) {
    this.unreachableOrAddresses = unreachableOrAddresses;
  }

  public List<String> getUnreachableOrAddresses() {
    return this.unreachableOrAddresses;
  }

  private String bridgedbDistributor;

  public void setBridgedbDistributor(String bridgedbDistributor) {
    this.bridgedbDistributor = bridgedbDistributor;
  }

  public String getBridgedbDistributor() {
    return this.bridgedbDistributor;
  }

  private List<String> blocklist;

  public void setBlocklist(List<String> blocklist) {
    this.blocklist = (blocklist != null && !blocklist.isEmpty())
        ? blocklist : null;
  }

  public List<String> getBlocklist() {
    return this.blocklist;
  }

  private HardwareInfoDocument hardwareInfo;

  public HardwareInfoDocument getHardwareInfo() {
    return hardwareInfo;
  }

  public void setHardwareInfo(HardwareInfoDocument hardwareInfoDocument) {
    this.hardwareInfo = hardwareInfoDocument;
  }
}
