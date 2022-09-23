/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.docs;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class DetailsStatus extends Document {

  /* From most recently published server descriptor: */

  private String descPublished;

  public void setDescPublished(Long descPublished) {
    this.descPublished = null == descPublished ? null
        : DateTimeHelper.format(descPublished);
  }

  public Long getDescPublished() {
    return DateTimeHelper.parse(this.descPublished);
  }

  private String lastRestarted;

  public void setLastRestarted(Long lastRestarted) {
    this.lastRestarted = null == lastRestarted ? null
        : DateTimeHelper.format(lastRestarted);
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

  private long overloadGeneralTimestamp;

  public void setOverloadGeneralTimestamp(long overloadGeneralTimestamp) {
    this.overloadGeneralTimestamp = overloadGeneralTimestamp;
  }

  public long getOverloadGeneralTimestamp() {
    return this.overloadGeneralTimestamp;
  }

  /**
   * Returns true if the node is overloaded.
   */
  public Boolean isOverloadStatus() {
    if (this.overloadGeneralTimestamp != -1L) {
      return true;
    } else {
      return false;
    }
  }

  private List<String> exitPolicy;

  public void setExitPolicy(List<String> exitPolicy) {
    this.exitPolicy = exitPolicy;
  }

  public List<String> getExitPolicy() {
    return this.exitPolicy;
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

  private Map<String, List<String>> exitPolicyV6Summary;

  public void setExitPolicyV6Summary(
      Map<String, List<String>> exitPolicyV6Summary) {
    this.exitPolicyV6Summary = exitPolicyV6Summary;
  }

  public Map<String, List<String>> getExitPolicyV6Summary() {
    return this.exitPolicyV6Summary;
  }

  private Boolean hibernating;

  public void setHibernating(Boolean hibernating) {
    this.hibernating = hibernating;
  }

  public Boolean isHibernating() {
    return this.hibernating;
  }

  /* From most recently published extra-info descriptor: */

  private Long extraInfoDescPublished;

  public void setExtraInfoDescPublished(Long extraInfoDescPublished) {
    this.extraInfoDescPublished = extraInfoDescPublished;
  }

  public Long getExtraInfoDescPublished() {
    return this.extraInfoDescPublished;
  }

  private List<String> transports;

  public void setTransports(List<String> transports) {
    this.transports = (transports != null && !transports.isEmpty())
        ? transports : null;
  }

  public List<String> getTransports() {
    return this.transports;
  }

  /* From network status entries: */

  private boolean isRelay;

  public void setRelay(boolean isRelay) {
    this.isRelay = isRelay;
  }

  public boolean isRelay() {
    return this.isRelay;
  }

  private boolean running;

  public void setRunning(boolean isRunning) {
    this.running = isRunning;
  }

  public boolean isRunning() {
    return this.running;
  }

  private String nickname;

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getNickname() {
    return this.nickname;
  }

  private String address;

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return this.address;
  }

  private SortedSet<String> orAddressesAndPorts;

  public void setOrAddressesAndPorts(
      SortedSet<String> orAddressesAndPorts) {
    this.orAddressesAndPorts = orAddressesAndPorts;
  }

  public SortedSet<String> getOrAddressesAndPorts() {
    return this.orAddressesAndPorts == null ? new TreeSet<>() :
        this.orAddressesAndPorts;
  }

  private Long bridgePoolAssignmentPublished;

  public void setBridgePoolAssignmentPublished(
      Long bridgePoolAssignmentPublished) {
    this.bridgePoolAssignmentPublished = bridgePoolAssignmentPublished;
  }

  public java.lang.Long getBridgePoolAssignmentPublished() {
    return bridgePoolAssignmentPublished;
  }

  private String bridgedbDistributor;

  public void setBridgedbDistributor(String bridgedbDistributor) {
    this.bridgedbDistributor = bridgedbDistributor;
  }

  public String getBridgedbDistributor() {
    return bridgedbDistributor;
  }

  private List<String> blocklist;

  public void setBlocklist(List<String> blocklist) {
    this.blocklist = (blocklist != null && !blocklist.isEmpty())
        ? blocklist : null;
  }

  public List<String> getBlocklist() {
    return this.blocklist;
  }

  /** Returns all addresses used for the onion-routing protocol which
   * includes the primary address and all additionally configured
   * onion-routing addresses. */
  public SortedSet<String> getOrAddresses() {
    SortedSet<String> orAddresses = new TreeSet<>();
    if (this.address != null) {
      orAddresses.add(this.address);
    }
    if (this.orAddressesAndPorts != null) {
      for (String orAddressAndPort : this.orAddressesAndPorts) {
        if (orAddressAndPort.contains(":")) {
          String orAddress = orAddressAndPort.substring(0,
              orAddressAndPort.lastIndexOf(':'));
          orAddresses.add(orAddress);
        }
      }
    }
    return orAddresses;
  }

  private long firstSeenMillis;

  public void setFirstSeenMillis(long firstSeenMillis) {
    this.firstSeenMillis = firstSeenMillis;
  }

  public long getFirstSeenMillis() {
    return this.firstSeenMillis;
  }

  private long lastSeenMillis;

  public void setLastSeenMillis(long lastSeenMillis) {
    this.lastSeenMillis = lastSeenMillis;
  }

  public long getLastSeenMillis() {
    return this.lastSeenMillis;
  }

  private int orPort;

  public void setOrPort(int orPort) {
    this.orPort = orPort;
  }

  public int getOrPort() {
    return this.orPort;
  }

  private int dirPort;

  public void setDirPort(int dirPort) {
    this.dirPort = dirPort;
  }

  public int getDirPort() {
    return this.dirPort;
  }

  private SortedSet<String> relayFlags;

  public void setRelayFlags(SortedSet<String> relayFlags) {
    this.relayFlags = relayFlags;
  }

  public SortedSet<String> getRelayFlags() {
    return this.relayFlags;
  }

  private long consensusWeight;

  public void setConsensusWeight(long consensusWeight) {
    this.consensusWeight = consensusWeight;
  }

  public long getConsensusWeight() {
    return this.consensusWeight;
  }

  private String defaultPolicy;

  public void setDefaultPolicy(String defaultPolicy) {
    this.defaultPolicy = defaultPolicy;
  }

  public String getDefaultPolicy() {
    return this.defaultPolicy;
  }

  private String portList;

  public void setPortList(String portList) {
    this.portList = portList;
  }

  public String getPortList() {
    return this.portList;
  }

  private long lastChangedOrAddressOrPort;

  public void setLastChangedOrAddressOrPort(
      long lastChangedOrAddressOrPort) {
    this.lastChangedOrAddressOrPort = lastChangedOrAddressOrPort;
  }

  public long getLastChangedOrAddressOrPort() {
    return this.lastChangedOrAddressOrPort;
  }

  private Boolean recommendedVersion;

  public void setRecommendedVersion(Boolean recommendedVersion) {
    this.recommendedVersion = recommendedVersion;
  }

  public Boolean isRecommendedVersion() {
    return this.recommendedVersion;
  }

  private Boolean measured;

  public void setMeasured(Boolean measured) {
    this.measured = measured;
  }

  public Boolean isMeasured() {
    return this.measured;
  }

  /* From exit lists: */

  private Map<String, Long> exitAddresses;

  public void setExitAddresses(Map<String, Long> exitAddresses) {
    this.exitAddresses = exitAddresses;
  }

  public Map<String, Long> getExitAddresses() {
    return this.exitAddresses;
  }

  /* Calculated path-selection probabilities: */

  private Float consensusWeightFraction;

  public void setConsensusWeightFraction(Float consensusWeightFraction) {
    this.consensusWeightFraction = consensusWeightFraction;
  }

  public Float getConsensusWeightFraction() {
    return this.consensusWeightFraction;
  }

  private Float guardProbability;

  public void setGuardProbability(Float guardProbability) {
    this.guardProbability = guardProbability;
  }

  public Float getGuardProbability() {
    return this.guardProbability;
  }

  private Float middleProbability;

  public void setMiddleProbability(Float middleProbability) {
    this.middleProbability = middleProbability;
  }

  public Float getMiddleProbability() {
    return this.middleProbability;
  }

  private Float exitProbability;

  public void setExitProbability(Float exitProbability) {
    this.exitProbability = exitProbability;
  }

  public Float getExitProbability() {
    return this.exitProbability;
  }

  /* GeoIP lookup results: */

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

  private String countryCode;

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getCountryCode() {
    return this.countryCode;
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

  private String asName;

  public void setAsName(String asName) {
    this.asName = asName;
  }

  public String getAsName() {
    return this.asName;
  }

  private String asNumber;

  public void setAsNumber(String asNumber) {
    this.asNumber = asNumber;
  }

  public String getAsNumber() {
    return this.asNumber;
  }

  /* Reverse DNS lookup result: */

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

  private List<String> advertisedOrAddresses;

  public void setAdvertisedOrAddresses(List<String> advertisedOrAddresses) {
    this.advertisedOrAddresses = advertisedOrAddresses;
  }

  public List<String> getAdvertisedOrAddresses() {
    return this.advertisedOrAddresses;
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
}
