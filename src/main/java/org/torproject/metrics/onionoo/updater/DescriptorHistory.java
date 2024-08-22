/* Copyright 2016--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

public enum DescriptorHistory {

  RELAY_CONSENSUS_HISTORY("relay-consensus-history"),
  RELAY_SERVER_HISTORY("relay-server-history"),
  RELAY_EXTRAINFO_HISTORY("relay-extrainfo-history"),
  EXIT_LIST_HISTORY("exit-list-history"),
  BRIDGE_STATUS_HISTORY("bridge-status-history"),
  BRIDGE_SERVER_HISTORY("bridge-server-history"),
  BRIDGE_EXTRAINFO_HISTORY("bridge-extrainfo-history"),
  BRIDGE_POOL_ASSIGNMENTS_HISTORY("bridge-pool-assignments-history"),
  BRIDGESTRAP_HISTORY("bridgestrap-history"),
  ARCHIVED_HISTORY("archived-history"),
  ONIONPERF_HISTORY("onionperf-history");

  private String fileName;

  DescriptorHistory(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return this.fileName;
  }

}

