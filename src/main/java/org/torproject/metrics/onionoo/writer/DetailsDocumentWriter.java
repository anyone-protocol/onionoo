/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.writer;

import org.torproject.metrics.onionoo.docs.DetailsDocument;
import org.torproject.metrics.onionoo.docs.DetailsStatus;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.UpdateStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Write DetailsDocument files in {@code /out/details}
 * from updated status files.
 *
 * <p>Status files under {@code /status/details} are updated in
 * {@link NodeDetailsStatusUpdater}</p>
 *
 */
public class DetailsDocumentWriter implements DocumentWriter {

  private static final Logger logger = LoggerFactory.getLogger(
      DetailsDocumentWriter.class);

  private DocumentStore documentStore;

  public DetailsDocumentWriter() {
    this.documentStore = DocumentStoreFactory.getDocumentStore();
  }

  @Override
  public void writeDocuments(long mostRecentStatusMillis) {
    UpdateStatus updateStatus = this.documentStore.retrieve(
        UpdateStatus.class, true);
    long updatedMillis = updateStatus != null
        ? updateStatus.getUpdatedMillis() : 0L;
    SortedSet<String> updatedDetailsStatuses = this.documentStore.list(
        DetailsStatus.class, updatedMillis);
    for (String fingerprint : updatedDetailsStatuses) {
      DetailsStatus detailsStatus = this.documentStore.retrieve(
          DetailsStatus.class, true, fingerprint);
      if (detailsStatus.isRelay()) {
        this.updateRelayDetailsFile(fingerprint, detailsStatus);
      } else {
        this.updateBridgeDetailsFile(fingerprint, detailsStatus);
      }
    }
    logger.info("Wrote details document files");
  }

  /**
   * Update the relay details document file for a relay.
   *
   * @param fingerprint, a String
   * @param detailsStatus, a DetailsStatus
   */
  private void updateRelayDetailsFile(String fingerprint,
      DetailsStatus detailsStatus) {
    DetailsDocument detailsDocument = new DetailsDocument();
    detailsDocument.setNickname(detailsStatus.getNickname());
    detailsDocument.setFingerprint(fingerprint);
    List<String> orAddresses = new ArrayList<>();
    orAddresses.add(detailsStatus.getAddress() + ":"
        + detailsStatus.getOrPort());
    for (String orAddress : detailsStatus.getOrAddressesAndPorts()) {
      orAddresses.add(orAddress.toLowerCase());
    }
    detailsDocument.setOrAddresses(orAddresses);
    if (detailsStatus.getDirPort() != 0) {
      detailsDocument.setDirAddress(detailsStatus.getAddress() + ":"
          + detailsStatus.getDirPort());
    }
    detailsDocument.setLastSeen(detailsStatus.getLastSeenMillis());
    detailsDocument.setFirstSeen(detailsStatus.getFirstSeenMillis());
    detailsDocument.setLastChangedAddressOrPort(
        detailsStatus.getLastChangedOrAddressOrPort());
    detailsDocument.setRunning(detailsStatus.isRunning());
    detailsDocument.setFlags(detailsStatus.getRelayFlags());
    detailsDocument.setConsensusWeight(
        detailsStatus.getConsensusWeight());
    detailsDocument.setVerifiedHostNames(
        detailsStatus.getVerifiedHostNames());
    detailsDocument.setUnverifiedHostNames(
        detailsStatus.getUnverifiedHostNames());
    String defaultPolicy = detailsStatus.getDefaultPolicy();
    String portList = detailsStatus.getPortList();
    if (defaultPolicy != null && (defaultPolicy.equals("accept")
        || defaultPolicy.equals("reject")) && portList != null) {
      Map<String, List<String>> exitPolicySummary = new HashMap<>();
      List<String> portsOrPortRanges = Arrays.asList(portList.split(","));
      exitPolicySummary.put(defaultPolicy, portsOrPortRanges);
      detailsDocument.setExitPolicySummary(exitPolicySummary);
    }
    detailsDocument.setRecommendedVersion(
        detailsStatus.isRecommendedVersion());
    detailsDocument.setCountry(detailsStatus.getCountryCode());
    detailsDocument.setLatitude(detailsStatus.getLatitude());
    detailsDocument.setLongitude(detailsStatus.getLongitude());
    detailsDocument.setCountryName(detailsStatus.getCountryName());
    detailsDocument.setRegionName(detailsStatus.getRegionName());
    detailsDocument.setCityName(detailsStatus.getCityName());
    detailsDocument.setAs(detailsStatus.getAsNumber());
    detailsDocument.setAsName(detailsStatus.getAsName());
    if (detailsStatus.isRunning()) {
      detailsDocument.setConsensusWeightFraction(
          detailsStatus.getConsensusWeightFraction());
      detailsDocument.setGuardProbability(
          detailsStatus.getGuardProbability());
      detailsDocument.setMiddleProbability(
          detailsStatus.getMiddleProbability());
      detailsDocument.setExitProbability(
          detailsStatus.getExitProbability());
    }
    detailsDocument.setLastRestarted(detailsStatus.getLastRestarted());
    detailsDocument.setBandwidthRate(detailsStatus.getBandwidthRate());
    detailsDocument.setBandwidthBurst(detailsStatus.getBandwidthBurst());
    detailsDocument.setObservedBandwidth(
        detailsStatus.getObservedBandwidth());
    detailsDocument.setAdvertisedBandwidth(
        detailsStatus.getAdvertisedBandwidth());
    long overloadGeneralTimestamp = detailsStatus.getOverloadGeneralTimestamp();
    if (overloadGeneralTimestamp != -1L) {
      detailsDocument.setOverloadGeneralTimestamp(overloadGeneralTimestamp);
    } else {
      detailsDocument.setOverloadGeneralTimestamp(null);
    }
    detailsDocument.setExitPolicy(detailsStatus.getExitPolicy());
    detailsDocument.setContact(detailsStatus.getContact());
    detailsDocument.setPlatform(detailsStatus.getPlatform());
    if (detailsStatus.getAllegedFamily() != null
        && !detailsStatus.getAllegedFamily().isEmpty()) {
      detailsDocument.setAllegedFamily(
          new TreeSet<>(detailsStatus.getAllegedFamily()));
    }
    if (detailsStatus.getEffectiveFamily() != null
        && !detailsStatus.getEffectiveFamily().isEmpty()) {
      detailsDocument.setEffectiveFamily(
          new TreeSet<>(detailsStatus.getEffectiveFamily()));
    }
    if (detailsStatus.getIndirectFamily() != null
        && !detailsStatus.getIndirectFamily().isEmpty()) {
      detailsDocument.setIndirectFamily(
          new TreeSet<>(detailsStatus.getIndirectFamily()));
    }
    detailsDocument.setExitPolicyV6Summary(
        detailsStatus.getExitPolicyV6Summary());
    detailsDocument.setHibernating(detailsStatus.isHibernating());
    if (detailsStatus.getExitAddresses() != null) {
      List<String> exitAddresses = new ArrayList<>(new TreeSet<>(
          detailsStatus.getExitAddresses().keySet()));
      detailsDocument.setExitAddresses(exitAddresses);
    }
    detailsDocument.setMeasured(detailsStatus.isMeasured());
    List<String> unreachableOrAddresses = new ArrayList<>();
    if (null != detailsStatus.getAdvertisedOrAddresses()) {
      unreachableOrAddresses.addAll(detailsStatus.getAdvertisedOrAddresses());
    }
    if (null != detailsStatus.getOrAddressesAndPorts()) {
      unreachableOrAddresses.removeAll(detailsStatus.getOrAddressesAndPorts());
    }
    if (!unreachableOrAddresses.isEmpty()) {
      detailsDocument.setUnreachableOrAddresses(unreachableOrAddresses);
    }
    detailsDocument.setVersion(detailsStatus.getVersion());
    detailsDocument.setVersionStatus(detailsStatus.getVersionStatus());
    this.documentStore.store(detailsDocument, fingerprint);
  }

  /**
   * Update the relay details document file for a bridge.
   *
   * @param fingerprint, a String
   * @param detailsStatus, a DetailsStatus
   */
  private void updateBridgeDetailsFile(String fingerprint,
      DetailsStatus detailsStatus) {
    DetailsDocument detailsDocument = new DetailsDocument();
    detailsDocument.setNickname(detailsStatus.getNickname());
    detailsDocument.setHashedFingerprint(fingerprint);

    String address = detailsStatus.getAddress();
    List<String> orAddresses = new ArrayList<>();
    orAddresses.add(address + ":" + detailsStatus.getOrPort());
    SortedSet<String> orAddressesAndPorts =
        detailsStatus.getOrAddressesAndPorts();
    if (orAddressesAndPorts != null) {
      for (String orAddress : orAddressesAndPorts) {
        orAddresses.add(orAddress.toLowerCase());
      }
    }
    detailsDocument.setOrAddresses(orAddresses);
    detailsDocument.setLastSeen(detailsStatus.getLastSeenMillis());
    detailsDocument.setFirstSeen(detailsStatus.getFirstSeenMillis());
    detailsDocument.setRunning(detailsStatus.isRunning());
    detailsDocument.setFlags(detailsStatus.getRelayFlags());
    detailsDocument.setRecommendedVersion(
        detailsStatus.isRecommendedVersion());
    detailsDocument.setLastRestarted(detailsStatus.getLastRestarted());
    detailsDocument.setAdvertisedBandwidth(
        detailsStatus.getAdvertisedBandwidth());
    long overloadGeneralTimestamp = detailsStatus.getOverloadGeneralTimestamp();
    if (overloadGeneralTimestamp != -1L) {
      detailsDocument.setOverloadGeneralTimestamp(overloadGeneralTimestamp);
    } else {
      detailsDocument.setOverloadGeneralTimestamp(null);
    }
    detailsDocument.setPlatform(detailsStatus.getPlatform());
    detailsDocument.setTransports(detailsStatus.getTransports());
    detailsDocument.setVersion(detailsStatus.getVersion());
    detailsDocument.setVersionStatus(detailsStatus.getVersionStatus());
    detailsDocument.setBridgedbDistributor(
        detailsStatus.getBridgedbDistributor());
    detailsDocument.setBlocklist(
        detailsStatus.getBlocklist());
    this.documentStore.store(detailsDocument, fingerprint);
  }

  @Override
  public String getStatsString() {
    /* TODO Add statistics string. */
    return null;
  }
}
