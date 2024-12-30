/* Copyright 2014--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.metrics.onionoo.docs.ClientsHistory;
import org.torproject.metrics.onionoo.docs.ClientsStatus;
import org.torproject.metrics.onionoo.docs.DateTimeHelper;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.NodeStatus;
import org.torproject.metrics.onionoo.util.FormattingUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * Example extra-info descriptor used as input:
 *
 * extra-info ndnop2 DE6397A047ABE5F78B4C87AF725047831B221AAB
 * dirreq-stats-end 2014-02-16 16:42:11 (86400 s)
 * dirreq-v3-resp ok=856,not-enough-sigs=0,unavailable=0,not-found=0,
 *   not-modified=40,busy=0
 * bridge-stats-end 2014-02-16 16:42:17 (86400 s)
 * bridge-ips ??=8,in=8,se=8
 * bridge-ip-versions v4=8,v6=0
 *
 * Clients status file produced as intermediate output:
 *
 * 2014-02-15 16:42:11 2014-02-16 00:00:00
 *   259.042 in=86.347,se=86.347  v4=259.042
 * 2014-02-16 00:00:00 2014-02-16 16:42:11
 *   592.958 in=197.653,se=197.653  v4=592.958
 */
public class ClientsStatusUpdater implements DescriptorListener,
    StatusUpdater {

  private DescriptorSource descriptorSource;

  private DocumentStore documentStore;

  /** Initializes a new status updater, obtains references to all relevant
   * singleton instances, and registers as listener at the (singleton)
   * descriptor source. */
  public ClientsStatusUpdater() {
    this.descriptorSource = DescriptorSourceFactory.getDescriptorSource();
    this.documentStore = DocumentStoreFactory.getDocumentStore();
    this.registerDescriptorListeners();
  }

  private void registerDescriptorListeners() {
    // disable useless module
//    this.descriptorSource.registerDescriptorListener(this,
//        DescriptorType.BRIDGE_EXTRA_INFOS);
  }

  @Override
  public void processDescriptor(Descriptor descriptor, boolean relay) {
    if (descriptor instanceof ExtraInfoDescriptor && !relay) {
      this.processBridgeExtraInfoDescriptor(
          (ExtraInfoDescriptor) descriptor);
    }
  }

  private SortedMap<String, SortedSet<ClientsHistory>> newResponses =
      new TreeMap<>();

  private void processBridgeExtraInfoDescriptor(
      ExtraInfoDescriptor descriptor) {
    long dirreqStatsEndMillis = descriptor.getDirreqStatsEndMillis();
    long dirreqStatsIntervalLengthMillis =
        descriptor.getDirreqStatsIntervalLength()
        * DateTimeHelper.ONE_SECOND;
    SortedMap<String, Integer> responses = descriptor.getDirreqV3Resp();
    if (dirreqStatsEndMillis < 0L
        || dirreqStatsIntervalLengthMillis != DateTimeHelper.ONE_DAY
        || responses == null || !responses.containsKey("ok")) {
      /* No directory request responses in the descriptor that we would
       * include in a clients document. */
      return;
    }
    double okResponses = responses.get("ok") - 4;
    if (okResponses < 0.0) {
      /* Response numbers are not supposed to be negative.  Skipping. */
      return;
    }
    String hashedFingerprint = descriptor.getFingerprint().toUpperCase();
    long dirreqStatsStartMillis = dirreqStatsEndMillis
        - dirreqStatsIntervalLengthMillis;
    long utcBreakMillis = (dirreqStatsEndMillis / DateTimeHelper.ONE_DAY)
        * DateTimeHelper.ONE_DAY;
    for (int i = 0; i < 2; i++) {
      long startMillis = i == 0 ? dirreqStatsStartMillis : utcBreakMillis;
      long endMillis = i == 0 ? utcBreakMillis : dirreqStatsEndMillis;
      if (startMillis >= endMillis) {
        continue;
      }
      double totalResponses = okResponses
          * ((double) (endMillis - startMillis))
          / ((double) DateTimeHelper.ONE_DAY);
      SortedMap<String, Double> responsesByCountry =
          this.weightResponsesWithUniqueIps(totalResponses,
          descriptor.getBridgeIps(), "??");
      SortedMap<String, Double> responsesByTransport =
          this.weightResponsesWithUniqueIps(totalResponses,
          descriptor.getBridgeIpTransports(), "<??>");
      SortedMap<String, Double> responsesByVersion =
          this.weightResponsesWithUniqueIps(totalResponses,
          descriptor.getBridgeIpVersions(), "");
      ClientsHistory newResponseHistory = new ClientsHistory(
          startMillis, endMillis, totalResponses, responsesByCountry,
          responsesByTransport, responsesByVersion); 
      this.newResponses.putIfAbsent(hashedFingerprint, new TreeSet<>());
      this.newResponses.get(hashedFingerprint).add(newResponseHistory);
    }
  }

  private SortedMap<String, Double> weightResponsesWithUniqueIps(
      double totalResponses, SortedMap<String, Integer> uniqueIps,
      String omitString) {
    SortedMap<String, Double> weightedResponses = new TreeMap<>();
    int totalUniqueIps = 0;
    if (uniqueIps != null) {
      for (Map.Entry<String, Integer> e : uniqueIps.entrySet()) {
        if (e.getValue() > 4) {
          totalUniqueIps += e.getValue() - 4;
        }
      }
    }
    if (totalUniqueIps > 0) {
      for (Map.Entry<String, Integer> e : uniqueIps.entrySet()) {
        if (!e.getKey().equals(omitString) && e.getValue() > 4) {
          weightedResponses.put(e.getKey(),
              (((double) (e.getValue() - 4)) * totalResponses)
              / ((double) totalUniqueIps));
        }
      }
    }
    return weightedResponses;
  }

  @Override
  public void updateStatuses() {
    for (Map.Entry<String, SortedSet<ClientsHistory>> e :
        this.newResponses.entrySet()) {
      String hashedFingerprint = e.getKey();
      ClientsStatus clientsStatus = this.documentStore.retrieve(
          ClientsStatus.class, true, hashedFingerprint);
      if (clientsStatus == null) {
        clientsStatus = new ClientsStatus();
      }
      clientsStatus.addToHistory(e.getValue());
      if (clientsStatus.isDirty()) {
        NodeStatus nodeStatus = this.documentStore.retrieve(NodeStatus.class,
            true, hashedFingerprint);
        if (null != nodeStatus) {
          clientsStatus.compressHistory(nodeStatus.getLastSeenMillis());
        }
        this.documentStore.store(clientsStatus, hashedFingerprint);
        clientsStatus.clearDirty();
      }
    }
  }

  @Override
  public String getStatsString() {
    int newIntervals = 0;
    for (SortedSet<ClientsHistory> hist : this.newResponses.values()) {
      newIntervals += hist.size();
    }
    return String.format(
        "    %s client statistics processed from extra-info descriptors\n"
        + "    %s client status files updated\n",
        FormattingUtils.formatDecimalNumber(newIntervals / 2),
        FormattingUtils.formatDecimalNumber(this.newResponses.size()));
  }
}

