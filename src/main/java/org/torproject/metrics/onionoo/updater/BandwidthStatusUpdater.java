/* Copyright 2011--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.metrics.onionoo.docs.BandwidthStatus;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.NodeStatus;

public class BandwidthStatusUpdater implements DescriptorListener,
    StatusUpdater {

  private DescriptorSource descriptorSource;

  private DocumentStore documentStore;

  /** Initializes a new status updater, obtains references to all relevant
   * singleton instances, and registers as listener at the (singleton)
   * descriptor source. */
  public BandwidthStatusUpdater() {
    this.descriptorSource = DescriptorSourceFactory.getDescriptorSource();
    this.documentStore = DocumentStoreFactory.getDocumentStore();
    this.registerDescriptorListeners();
  }

  private void registerDescriptorListeners() {
    this.descriptorSource.registerDescriptorListener(this,
        DescriptorType.RELAY_EXTRA_INFOS);
    this.descriptorSource.registerDescriptorListener(this,
        DescriptorType.BRIDGE_EXTRA_INFOS);
  }

  @Override
  public void processDescriptor(Descriptor descriptor, boolean relay) {
    if (descriptor instanceof ExtraInfoDescriptor) {
      this.parseDescriptor((ExtraInfoDescriptor) descriptor);
    }
  }

  @Override
  public void updateStatuses() {
    /* Status files are already updated while processing descriptors. */
  }

  private void parseDescriptor(ExtraInfoDescriptor descriptor) {
    String fingerprint = descriptor.getFingerprint();
    BandwidthStatus bandwidthStatus = this.documentStore.retrieve(
        BandwidthStatus.class, true, fingerprint);
    if (bandwidthStatus == null) {
      bandwidthStatus = new BandwidthStatus();
    }
    if (descriptor.getWriteHistory() != null) {
      bandwidthStatus.addToWriteHistory(descriptor.getWriteHistory());
    }
    if (descriptor.getReadHistory() != null) {
      bandwidthStatus.addToReadHistory(descriptor.getReadHistory());
    }
    bandwidthStatus.setOverloadRatelimitsTimestamp(
        descriptor.getOverloadRatelimitsTimestamp()
    );
    bandwidthStatus.setOverloadRatelimitsRateLimit(
        descriptor.getOverloadRatelimitsRateLimit()
    );
    bandwidthStatus.setOverloadRatelimitsBurstLimit(
        descriptor.getOverloadRatelimitsBurstLimit()
    );

    bandwidthStatus.setOverloadRatelimitsReadCount(
        descriptor.getOverloadRatelimitsReadCount()
    );
    bandwidthStatus.setOverloadRatelimitsWriteCount(
        descriptor.getOverloadRatelimitsWriteCount()
    );
    bandwidthStatus.setOverloadFdExhaustedTimestamp(
        descriptor.getOverloadFdExhaustedTimestamp()
    );
    if (bandwidthStatus.isDirty()) {
      NodeStatus nodeStatus = this.documentStore.retrieve(NodeStatus.class,
          true, fingerprint);
      if (null != nodeStatus) {
        bandwidthStatus.compressHistory(nodeStatus.getLastSeenMillis());
      }
      this.documentStore.store(bandwidthStatus, fingerprint);
      bandwidthStatus.clearDirty();
    }
  }

  @Override
  public String getStatsString() {
    /* TODO Add statistics string. */
    return null;
  }
}
