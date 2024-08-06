package org.torproject.metrics.onionoo.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.TorperfResult;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;

public class OnionperfStatusUpdater implements DescriptorListener, StatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(OnionperfStatusUpdater.class);

    private DescriptorSource descriptorSource;
    private DocumentStore documentStore;

    /** Initializes a new status updater, obtains references to all relevant
     * singleton instances, and registers as listener at the (singleton)
     * descriptor source. */
    public OnionperfStatusUpdater() {
        this.descriptorSource = DescriptorSourceFactory.getDescriptorSource();
        this.documentStore = DocumentStoreFactory.getDocumentStore();
        this.registerDescriptorListeners();
    }

    private void registerDescriptorListeners() {
        this.descriptorSource.registerDescriptorListener(this, DescriptorType.ONIONPERF);
    }
    @Override
    public void processDescriptor(Descriptor descriptor, boolean relay) {
        if (descriptor instanceof TorperfResult) {
            this.processTorPerfResult((TorperfResult) descriptor);
        }
    }

    private void processTorPerfResult(TorperfResult descriptor) {
        logger.info("Processing Torperf result with cirId: {}", descriptor.getCircId());
    }

    @Override
    public void updateStatuses() {
        /* Nothing to do. */
    }

    @Override
    public String getStatsString() {
        /* TODO Add statistics string. */
        return null;
    }
}
