/* Copyright 2014--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import org.torproject.descriptor.BridgeNetworkStatus;
import org.torproject.descriptor.NetworkStatusEntry;

import java.io.File;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class DummyBridgeStatus implements BridgeNetworkStatus {

  public byte[] getRawDescriptorBytes() {
    return null;
  }

  public int getRawDescriptorLength() {
    return 0;
  }

  public List<String> getAnnotations() {
    return null;
  }

  public List<String> getUnrecognizedLines() {
    return null;
  }

  public File getDescriptorFile() {
    return null;
  }

  private long publishedMillis;

  public void setPublishedMillis(long publishedMillis) {
    this.publishedMillis = publishedMillis;
  }

  public long getPublishedMillis() {
    return this.publishedMillis;
  }

  private SortedMap<String, NetworkStatusEntry> statusEntries =
      new TreeMap<>();

  public void addStatusEntry(NetworkStatusEntry statusEntry) {
    this.statusEntries.put(statusEntry.getFingerprint(), statusEntry);
  }

  public SortedMap<String, NetworkStatusEntry> getStatusEntries() {
    return this.statusEntries;
  }

  @Override
  public int getEnoughMtbfInfo() {
    return 0;
  }

  @Override
  public long getFastBandwidth() {
    return 0;
  }

  @Override
  public long getGuardBandwidthExcludingExits() {
    return 0;
  }

  @Override
  public long getGuardBandwidthIncludingExits() {
    return 0;
  }

  @Override
  public long getGuardTk() {
    return 0;
  }

  @Override
  public double getGuardWfu() {
    return 0;
  }

  @Override
  public int getIgnoringAdvertisedBws() {
    return 0;
  }

  @Override
  public long getStableMtbf() {
    return 0;
  }

  @Override
  public long getStableUptime() {
    return 0;
  }

  @Override
  public String getFingerprint() {
    return null;
  }
}

