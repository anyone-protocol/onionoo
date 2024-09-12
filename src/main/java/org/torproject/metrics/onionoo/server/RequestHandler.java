/* Copyright 2011--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.server;

import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.SummaryDocument;
import org.torproject.metrics.onionoo.updater.TorVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class RequestHandler {

  private NodeIndex nodeIndex;

  private DocumentStore documentStore;

  public RequestHandler(NodeIndex nodeIndex) {
    this.nodeIndex = nodeIndex;
    this.documentStore = DocumentStoreFactory.getDocumentStore();
  }

  private String resourceType;

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  private String type;

  public void setType(String type) {
    this.type = type;
  }

  private String running;

  public void setRunning(String running) {
    this.running = running;
  }

  private String[] search;

  public void setSearch(String[] search) {
    this.search = new String[search.length];
    System.arraycopy(search, 0, this.search, 0, search.length);
  }

  private String[] lookup;

  public void setLookup(String[] lookup) {
    this.lookup = lookup;
  }

  private String fingerprint;

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  private String country;

  public void setCountry(String country) {
    this.country = country;
  }

  private String[] as;

  public void setAs(String[] as) {
    this.as = as;
  }

  private String[] asName;

  public void setAsName(String[] asName) {
    this.asName = asName;
  }

  private String flag;

  public void setFlag(String flag) {
    this.flag = flag;
  }

  private String[] contact;

  public void setContact(String[] contact) {
    this.contact = new String[contact.length];
    System.arraycopy(contact, 0, this.contact, 0, contact.length);
  }

  private List<TorVersion[]> version;

  public void setVersion(List<TorVersion[]> version) {
    this.version = version;
  }

  private String operatingSystem;

  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

  private String hostName;

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  private Boolean recommendedVersion;

  public void setRecommendedVersion(Boolean recommendedVersion) {
    this.recommendedVersion = recommendedVersion;
  }

  private String[] order;

  public void setOrder(String[] order) {
    this.order = new String[order.length];
    System.arraycopy(order, 0, this.order, 0, order.length);
  }

  private String offset;

  public void setOffset(String offset) {
    this.offset = offset;
  }

  private String limit;

  public void setLimit(String limit) {
    this.limit = limit;
  }

  private int[] firstSeenDays;

  @SuppressWarnings("checkstyle:javadocmethod")
  public void setFirstSeenDays(int[] firstSeenDays) {
    this.firstSeenDays = new int[firstSeenDays.length];
    System.arraycopy(firstSeenDays, 0, this.firstSeenDays, 0,
        firstSeenDays.length);
  }

  private int[] lastSeenDays;

  @SuppressWarnings("checkstyle:javadocmethod")
  public void setLastSeenDays(int[] lastSeenDays) {
    this.lastSeenDays = new int[lastSeenDays.length];
    System.arraycopy(lastSeenDays, 0, this.lastSeenDays, 0,
        lastSeenDays.length);
  }

  private String family;

  public void setFamily(String family) {
    this.family = family;
  }

  private Boolean overloadStatus;

  public void setOverloadStatus(Boolean overloadStatus) {
    this.overloadStatus = overloadStatus;
  }

  private String transport;

  public void setTransport(String transport) {
    this.transport = transport;
  }

  private Map<String, SummaryDocument> filteredRelays = new HashMap<>();

  private Map<String, SummaryDocument> filteredBridges = new HashMap<>();

  /** Handles this request by filtering by all given parameters and then
   * possibly ordering, offsetting, and limiting results. */
  public void handleRequest() {
    this.filteredRelays.putAll(
        this.nodeIndex.getRelayFingerprintSummaryLines());
    this.filteredBridges.putAll(
        this.nodeIndex.getBridgeFingerprintSummaryLines());
    this.filterByResourceType();
    this.filterByType();
    this.filterByRunning();
    this.filterBySearchTerms();
    this.filterByLookup();
    this.filterByFingerprint();
    this.filterByCountryCode();
    this.filterByAsNumber();
    this.filterByAsName();
    this.filterByFlag();
    this.filterNodesByFirstSeenDays();
    this.filterNodesByLastSeenDays();
    this.filterByContact();
    this.filterByFamily();
    this.filterByVersion();
    this.filterByOperatingSystem();
    this.filterByHostName();
    this.filterByRecommendedVersion();
    this.filterByOverloadStatus();
    this.filterByTransport();
    this.order();
    this.offset();
    this.limit();
  }

  private void filterByResourceType() {
    if (this.resourceType.equals("clients")) {
      this.filteredRelays.clear();
      System.gc();
    }
    if (this.resourceType.equals("weights")) {
      this.filteredBridges.clear();
      System.gc();
    }
  }

  private void filterByType() {
    if (this.type == null) {
      /* Not filtering by type. */
    } else if (this.type.equals("relay")) {
      this.filteredBridges.clear();
      System.gc();
    } else {
      this.filteredRelays.clear();
      System.gc();
    }
  }

  private void filterByRunning() {
    if (this.running == null) {
      /* Not filtering by running or not. */
      return;
    }
    boolean runningRequested = this.running.equals("true");
    Set<String> removeRelays = new HashSet<>();
    for (Map.Entry<String, SummaryDocument> e
        : filteredRelays.entrySet()) {
      if (e.getValue().isRunning() != runningRequested) {
        removeRelays.add(e.getKey());
      }
    }
    for (String fingerprint : removeRelays) {
      this.filteredRelays.remove(fingerprint);
    }
    Set<String> removeBridges = new HashSet<>();
    for (Map.Entry<String, SummaryDocument> e
        : filteredBridges.entrySet()) {
      if (e.getValue().isRunning() != runningRequested) {
        removeBridges.add(e.getKey());
      }
    }
    for (String fingerprint : removeBridges) {
      this.filteredBridges.remove(fingerprint);
    }
  }

  private void filterBySearchTerms() {
    if (this.search == null) {
      /* Not filtering by search terms. */
      return;
    }
    for (String searchTerm : this.search) {
      filterBySearchTerm(searchTerm);
    }
  }

  private void filterBySearchTerm(String searchTerm) {
    Set<String> removeRelays = new HashSet<>();
    for (Map.Entry<String, SummaryDocument> e
        : filteredRelays.entrySet()) {
      String fingerprint = e.getKey();
      SummaryDocument entry = e.getValue();
      String base64Fingerprint = entry.isRelay()
          ? entry.getBase64Fingerprint() : null;
      String[] fingerprintSortedHexBlocks =
          entry.getFingerprintSortedHexBlocks();
      boolean lineMatches = false;
      String nickname = entry.getNickname() != null
          ? entry.getNickname().toLowerCase() : "unnamed";
      if (searchTerm.startsWith("$")) {
        /* Search is for $-prefixed fingerprint. */
        if (fingerprint.startsWith(
            searchTerm.substring(1).toUpperCase())) {
          /* $-prefixed fingerprint matches. */
          lineMatches = true;
        }
      } else if (nickname.contains(searchTerm.toLowerCase())) {
        /* Nickname matches. */
        lineMatches = true;
      } else if (fingerprint.startsWith(searchTerm.toUpperCase())) {
        /* Non-$-prefixed fingerprint matches. */
        lineMatches = true;
      } else if (base64Fingerprint != null
          && base64Fingerprint.startsWith(searchTerm)) {
        /* Base64-encoded fingerprint matches. */
        lineMatches = true;
      } else if (searchTerm.length() == 4
          && fingerprintSortedHexBlocks != null
          && Arrays.binarySearch(fingerprintSortedHexBlocks,
          searchTerm.toUpperCase()) >= 0) {
        /* 4-hex-character block of space-separated fingerprint
         * matches. */
        lineMatches = true;
      } else {
        List<String> addresses = entry.getAddresses();
        for (String address : addresses) {
          if (address.startsWith(searchTerm.toLowerCase())
              || address.startsWith("[" + searchTerm.toLowerCase())) {
            /* Address matches. */
            lineMatches = true;
            break;
          }
        }
      }
      if (!lineMatches) {
        removeRelays.add(e.getKey());
      }
    }
    for (String fingerprint : removeRelays) {
      this.filteredRelays.remove(fingerprint);
    }
    Set<String> removeBridges = new HashSet<>();
    for (Map.Entry<String, SummaryDocument> e :
        filteredBridges.entrySet()) {
      String hashedFingerprint = e.getKey();
      SummaryDocument entry = e.getValue();
      boolean lineMatches = false;
      String nickname = entry.getNickname() != null
          ? entry.getNickname().toLowerCase() : "unnamed";
      if (searchTerm.startsWith("$")) {
        /* Search is for $-prefixed hashed fingerprint. */
        if (hashedFingerprint.startsWith(
            searchTerm.substring(1).toUpperCase())) {
          /* $-prefixed hashed fingerprint matches. */
          lineMatches = true;
        }
      } else if (nickname.contains(searchTerm.toLowerCase())) {
        /* Nickname matches. */
        lineMatches = true;
      } else if (hashedFingerprint.startsWith(searchTerm.toUpperCase())) {
        /* Non-$-prefixed hashed fingerprint matches. */
        lineMatches = true;
      }
      if (!lineMatches) {
        removeBridges.add(e.getKey());
      }
    }
    for (String fingerprint : removeBridges) {
      this.filteredBridges.remove(fingerprint);
    }
  }

  private void filterByLookup() {
    if (this.lookup == null) {
      /* Not filtering by looking up relay or bridge. */
      return;
    }
    Map<String, SummaryDocument> matchingRelays = new HashMap<>();
    for (String fingerprint : this.lookup) {
      if (this.filteredRelays.containsKey(fingerprint)) {
        matchingRelays.put(fingerprint, this.filteredRelays.get(fingerprint));
      }
    }
    this.filteredRelays = matchingRelays;
    Map<String, SummaryDocument> matchingBridges = new HashMap<>();
    for (String fingerprint : this.lookup) {
      if (this.filteredBridges.containsKey(fingerprint)) {
        matchingBridges.put(fingerprint, this.filteredBridges.get(fingerprint));
      }
    }
    this.filteredBridges = matchingBridges;
  }

  private void filterByFingerprint() {
    if (this.fingerprint == null) {
      /* Not filtering by fingerprint. */
      return;
    }
    this.filteredRelays.clear();
    this.filteredBridges.clear();
    System.gc();
    String fingerprint = this.fingerprint;
    SummaryDocument entry = this.documentStore.retrieve(
        SummaryDocument.class, true, fingerprint);
    if (entry != null) {
      if (entry.isRelay()) {
        this.filteredRelays.put(fingerprint, entry);
      } else {
        this.filteredBridges.put(fingerprint, entry);
      }
    }
  }

  private void filterByCountryCode() {
    if (this.country == null) {
      /* Not filtering by country code. */
      return;
    }
    String countryCode = this.country.toLowerCase();
    if (!this.nodeIndex.getRelaysByCountryCode().containsKey(
        countryCode)) {
      this.filteredRelays.clear();
    } else {
      Set<String> relaysWithCountryCode =
          this.nodeIndex.getRelaysByCountryCode().get(countryCode);
      Set<String> removeRelays = new HashSet<>();
      for (String fingerprint : this.filteredRelays.keySet()) {
        if (!relaysWithCountryCode.contains(fingerprint)) {
          removeRelays.add(fingerprint);
        }
      }
      for (String fingerprint : removeRelays) {
        this.filteredRelays.remove(fingerprint);
      }
    }
    this.filteredBridges.clear();
    System.gc();
  }

  private void filterByAsNumber() {
    if (this.as == null || 0 == this.as.length) {
      /* Not filtering by AS number. */
      return;
    }
    Map<String, SummaryDocument> matchingRelays = new HashMap<>();
    for (String asNumber : this.as) {
      if (this.nodeIndex.getRelaysByAsNumber().containsKey(asNumber)) {
        for (String fingerprint
            : this.nodeIndex.getRelaysByAsNumber().get(asNumber)) {
          if (this.filteredRelays.containsKey(fingerprint)) {
            matchingRelays.put(fingerprint,
                this.filteredRelays.get(fingerprint));
          }
        }
      }
    }
    this.filteredRelays = matchingRelays;
    this.filteredBridges.clear();
    System.gc();
  }

  private void filterByAsName() {
    if (this.asName == null) {
      /* Not filtering by AS name. */
      return;
    }
    Set<String> removeRelays = new HashSet<>();
    for (Map.Entry<String, Set<String>> e :
        this.nodeIndex.getRelaysByAsName().entrySet()) {
      String asName = e.getKey();
      for (String asNamePart : this.asName) {
        if (asName == null || !asName.contains(asNamePart.toLowerCase())) {
          removeRelays.addAll(e.getValue());
          break;
        }
      }
    }
    for (String fingerprint : removeRelays) {
      this.filteredRelays.remove(fingerprint);
    }
    this.filteredBridges.clear();
    System.gc();
  }

  private void filterByFlag() {
    if (this.flag == null) {
      /* Not filtering by relay flag. */
      return;
    }
    String flag = this.flag.toLowerCase();
    if (!this.nodeIndex.getRelaysByFlag().containsKey(flag)) {
      this.filteredRelays.clear();
      System.gc();
    } else {
      Set<String> relaysWithFlag = this.nodeIndex.getRelaysByFlag().get(
          flag);
      Set<String> removeRelays = new HashSet<>();
      for (String fingerprint : this.filteredRelays.keySet()) {
        if (!relaysWithFlag.contains(fingerprint)) {
          removeRelays.add(fingerprint);
        }
      }
      for (String fingerprint : removeRelays) {
        this.filteredRelays.remove(fingerprint);
      }
    }
    if (!this.nodeIndex.getBridgesByFlag().containsKey(flag)) {
      this.filteredBridges.clear();
      System.gc();
    } else {
      Set<String> bridgesWithFlag = this.nodeIndex.getBridgesByFlag().get(
          flag);
      Set<String> removeBridges = new HashSet<>();
      for (String fingerprint : this.filteredBridges.keySet()) {
        if (!bridgesWithFlag.contains(fingerprint)) {
          removeBridges.add(fingerprint);
        }
      }
      for (String fingerprint : removeBridges) {
        this.filteredBridges.remove(fingerprint);
      }
    }
  }

  private void filterNodesByFirstSeenDays() {
    if (this.firstSeenDays == null) {
      /* Not filtering by first-seen days. */
      return;
    }
    filterNodesByDays(this.filteredRelays,
        this.nodeIndex.getRelaysByFirstSeenDays(), this.firstSeenDays);
    filterNodesByDays(this.filteredBridges,
        this.nodeIndex.getBridgesByFirstSeenDays(), this.firstSeenDays);
  }

  private void filterNodesByLastSeenDays() {
    if (this.lastSeenDays == null) {
      /* Not filtering by last-seen days. */
      return;
    }
    filterNodesByDays(this.filteredRelays,
        this.nodeIndex.getRelaysByLastSeenDays(), this.lastSeenDays);
    filterNodesByDays(this.filteredBridges,
        this.nodeIndex.getBridgesByLastSeenDays(), this.lastSeenDays);
  }

  private void filterNodesByDays(
      Map<String, SummaryDocument> filteredNodes,
      SortedMap<Integer, Set<String>> nodesByDays, int[] days) {
    Set<String> removeNodes = new HashSet<>();
    for (Set<String> nodes : nodesByDays.headMap(days[0]).values()) {
      removeNodes.addAll(nodes);
    }
    if (days[1] < Integer.MAX_VALUE) {
      for (Set<String> nodes :
          nodesByDays.tailMap(days[1] + 1).values()) {
        removeNodes.addAll(nodes);
      }
    }
    for (String fingerprint : removeNodes) {
      filteredNodes.remove(fingerprint);
    }
  }

  private void filterByContact() {
    if (this.contact == null) {
      /* Not filtering by contact information. */
      return;
    }
    Set<String> removeRelays = new HashSet<>();
    for (Map.Entry<String, Set<String>> e :
        this.nodeIndex.getRelaysByContact().entrySet()) {
      String contact = e.getKey();
      for (String contactPart : this.contact) {
        if (contact == null
            || !contact.contains(contactPart.toLowerCase())) {
          removeRelays.addAll(e.getValue());
          break;
        }
      }
    }
    for (String fingerprint : removeRelays) {
      this.filteredRelays.remove(fingerprint);
    }
    Set<String> removeBridges = new HashSet<>();
    for (Map.Entry<String, Set<String>> e :
        this.nodeIndex.getBridgesByContact().entrySet()) {
      String contact = e.getKey();
      for (String contactPart : this.contact) {
        if (contact == null
            || !contact.contains(contactPart.toLowerCase())) {
          removeBridges.addAll(e.getValue());
          break;
        }
      }
    }
    for (String fingerprint : removeBridges) {
      this.filteredBridges.remove(fingerprint);
    }
  }

  private void filterByFamily() {
    if (this.family == null) {
      /* Not filtering by relay family. */
      return;
    }
    Set<String> removeRelays = new HashSet<>(this.filteredRelays.keySet());
    removeRelays.remove(this.family);
    if (this.nodeIndex.getRelaysByFamily().containsKey(this.family)) {
      removeRelays.removeAll(this.nodeIndex.getRelaysByFamily()
          .get(this.family));
    }
    for (String fingerprint : removeRelays) {
      this.filteredRelays.remove(fingerprint);
    }
    this.filteredBridges.clear();
    System.gc();
  }

  private void filterByVersion() {
    if (null == this.version) {
      /* Not filtering by version. */
      return;
    }
    Set<String> keepRelays = new HashSet<>();
    for (Map.Entry<TorVersion, Set<String>> e
        : this.nodeIndex.getRelaysByVersion().entrySet()) {
      for (TorVersion[] versionRange : this.version) {
        if ((null == versionRange[0]
            || e.getKey().compareTo(versionRange[0]) >= 0)
            && (null == versionRange[1]
            || e.getKey().compareTo(versionRange[1]) <= 0
            || e.getKey().matchingPrefix(versionRange[1]))) {
          keepRelays.addAll(e.getValue());
        }
      }
    }
    this.filteredRelays.keySet().retainAll(keepRelays);
    Set<String> keepBridges = new HashSet<>();
    for (Map.Entry<TorVersion, Set<String>> e
        : this.nodeIndex.getBridgesByVersion().entrySet()) {
      for (TorVersion[] versionRange : this.version) {
        if ((null == versionRange[0]
            || e.getKey().compareTo(versionRange[0]) >= 0)
            && (null == versionRange[1]
            || e.getKey().compareTo(versionRange[1]) <= 0
            || e.getKey().matchingPrefix(versionRange[1]))) {
          keepBridges.addAll(e.getValue());
        }
      }
    }
    this.filteredBridges.keySet().retainAll(keepBridges);
  }

  private void filterByOperatingSystem() {
    if (null == this.operatingSystem) {
      /* Not filtering by operating system. */
      return;
    }
    Set<String> keepRelays = new HashSet<>();
    for (Map.Entry<String, Set<String>> e
        : this.nodeIndex.getRelaysByOperatingSystem().entrySet()) {
      if (e.getKey().startsWith(this.operatingSystem)) {
        keepRelays.addAll(e.getValue());
      }
    }
    this.filteredRelays.keySet().retainAll(keepRelays);
    Set<String> keepBridges = new HashSet<>();
    for (Map.Entry<String, Set<String>> e
        : this.nodeIndex.getBridgesByOperatingSystem().entrySet()) {
      if (e.getKey().startsWith(this.operatingSystem)) {
        keepBridges.addAll(e.getValue());
      }
    }
    this.filteredBridges.keySet().retainAll(keepBridges);
  }

  private void filterByHostName() {
    if (this.hostName == null) {
      /* Not filtering by host name. */
      return;
    }
    String hostName = this.hostName.toLowerCase();
    Set<String> removeRelays = new HashSet<>(this.filteredRelays.keySet());
    for (Map.Entry<String, Set<String>> e :
        this.nodeIndex.getRelaysByHostName().entrySet()) {
      if (e.getKey().endsWith(hostName)) {
        removeRelays.removeAll(e.getValue());
      }
    }
    for (String fingerprint : removeRelays) {
      this.filteredRelays.remove(fingerprint);
    }
    this.filteredBridges.clear();
    System.gc();
  }

  private void filterByRecommendedVersion() {
    if (null == this.recommendedVersion) {
      /* Not filtering by recommended version. */
      return;
    }
    Set<String> keepRelays = this.nodeIndex.getRelaysByRecommendedVersion()
        .get(this.recommendedVersion);
    this.filteredRelays.keySet().retainAll(keepRelays);
    Set<String> keepBridges = this.nodeIndex.getBridgesByRecommendedVersion()
        .get(this.recommendedVersion);
    this.filteredBridges.keySet().retainAll(keepBridges);
  }

  private void filterByOverloadStatus() {
    if (null == this.overloadStatus) {
      /* Not filtering by recommended version. */
      return;
    }
    Set<String> keepRelays = this.nodeIndex.getRelaysByOverloadStatus()
        .get(this.overloadStatus);
    this.filteredRelays.keySet().retainAll(keepRelays);
    Set<String> keepBridges = this.nodeIndex.getBridgesByOverloadStatus()
        .get(this.overloadStatus);
    this.filteredBridges.keySet().retainAll(keepBridges);
  }

  private void filterByTransport() {
    if (null == this.transport) {
      /* Not filtering by transport. */
      return;
    }
    this.filteredRelays.clear();
    System.gc();
    Set<String> keepBridges = new HashSet<>();
    for (Map.Entry<String, Set<String>> e
        : this.nodeIndex.getBridgesByTransport().entrySet()) {
      if (e.getKey().startsWith(this.transport)) {
        keepBridges.addAll(e.getValue());
      }
    }
    this.filteredBridges.keySet().retainAll(keepBridges);
  }

  private void order() {
    List<SummaryDocument> uniqueRelays = new ArrayList<>();
    List<SummaryDocument> uniqueBridges = new ArrayList<>();
    for (SummaryDocument relay : this.filteredRelays.values()) {
      if (!uniqueRelays.contains(relay)) {
        uniqueRelays.add(relay);
      }
    }
    for (SummaryDocument bridge : this.filteredBridges.values()) {
      if (!uniqueBridges.contains(bridge)) {
        uniqueBridges.add(bridge);
      }
    }
    Comparator<SummaryDocument> comparator
        = new SummaryDocumentComparator(this.order);
    uniqueRelays.sort(comparator);
    uniqueBridges.sort(comparator);
    this.orderedRelays.addAll(uniqueRelays);
    this.orderedBridges.addAll(uniqueBridges);
  }

  private int relaysSkipped = 0;

  public int getRelaysSkipped() {
    return this.relaysSkipped;
  }

  private int bridgesSkipped = 0;

  public int getBridgesSkipped() {
    return this.bridgesSkipped;
  }

  private void offset() {
    if (this.offset == null) {
      /* Not skipping first results. */
      return;
    }
    int offsetValue = Integer.parseInt(this.offset);
    while (offsetValue-- > 0
        && (!this.orderedRelays.isEmpty()
        || !this.orderedBridges.isEmpty())) {
      if (!this.orderedRelays.isEmpty()) {
        this.orderedRelays.remove(0);
        this.relaysSkipped++;
      } else {
        this.orderedBridges.remove(0);
        this.bridgesSkipped++;
      }
    }
  }

  private int relaysTruncated = 0;

  public int getRelaysTruncated() {
    return this.relaysTruncated;
  }

  private int bridgesTruncated = 0;

  public int getBridgesTruncated() {
    return this.bridgesTruncated;
  }

  private void limit() {
    if (this.limit == null) {
      /* Not limiting number of results. */
      return;
    }
    int limitValue = Integer.parseInt(this.limit);
    while (!this.orderedRelays.isEmpty()
        && limitValue < this.orderedRelays.size()) {
      this.orderedRelays.remove(this.orderedRelays.size() - 1);
      this.relaysTruncated++;
    }
    limitValue -= this.orderedRelays.size();
    while (!this.orderedBridges.isEmpty()
        && limitValue < this.orderedBridges.size()) {
      this.orderedBridges.remove(this.orderedBridges.size() - 1);
      this.bridgesTruncated++;
    }
  }

  private List<SummaryDocument> orderedRelays = new ArrayList<>();

  public List<SummaryDocument> getOrderedRelays() {
    return this.orderedRelays;
  }

  private List<SummaryDocument> orderedBridges = new ArrayList<>();

  public List<SummaryDocument> getOrderedBridges() {
    return this.orderedBridges;
  }

  public String getRelaysPublishedString() {
    return this.nodeIndex.getRelaysPublishedString();
  }

  public String getBridgesPublishedString() {
    return this.nodeIndex.getBridgesPublishedString();
  }
}
