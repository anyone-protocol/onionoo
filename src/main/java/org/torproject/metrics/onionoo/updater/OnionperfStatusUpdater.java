package org.torproject.metrics.onionoo.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.TorperfResult;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.OnionperfStatus;
import org.torproject.metrics.onionoo.onionperf.Measurement;
import org.torproject.metrics.onionoo.onionperf.TorperfResultConverter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class OnionperfStatusUpdater implements DescriptorListener, StatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(OnionperfStatusUpdater.class);

    private final TorperfResultConverter torperfResultConverter = new TorperfResultConverter();

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

    private List<Measurement> measurements = new ArrayList<>();

    @Override
    public void processDescriptor(Descriptor descriptor, boolean relay) {
        if (descriptor instanceof TorperfResult) {
            long threshold = LocalDateTime.now().atOffset(ZoneOffset.UTC).minusDays(2).toEpochSecond();
            if (!(((TorperfResult) descriptor).getStartMillis() / 1000 < threshold)) {
                this.processTorPerfResult((TorperfResult) descriptor);
            }
        }
    }

    private void processTorPerfResult(TorperfResult descriptor) {
        measurements.add(torperfResultConverter.toMeasurement(descriptor));
    }

    @Override
    public void updateStatuses() {
        logger.info("Updating Onionperf statuses. Size: {}", measurements.size());
        OnionperfStatus status = new OnionperfStatus(measurements);
        documentStore.store(status);
        measurements.clear();
    }

    @Override
    public String getStatsString() {
        /* TODO Add statistics string. */
        return null;
    }
}
