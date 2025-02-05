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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
    private Long threshold = null;

    @Override
    public void processDescriptor(Descriptor descriptor, boolean relay) {
        if (descriptor instanceof TorperfResult) {
            if (threshold == null) {
                threshold = LocalDateTime.now().atOffset(ZoneOffset.UTC).minusDays(2).toEpochSecond();
            }
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
        if (threshold == null) {
            threshold = LocalDateTime.now().atOffset(ZoneOffset.UTC).minusDays(2).toEpochSecond();
        }
        logger.info("Updating Onionperf new measurements size: {}", measurements.size());
        OnionperfStatus statusOld = documentStore.retrieve(OnionperfStatus.class, true);
        if (statusOld != null) {
            logger.info("OnionperfStatus old measurements: {}", statusOld.getMeasurements().size());
            measurements.addAll(filter(statusOld.getMeasurements(), threshold));
            logger.info("OnionperfStatus merged measurements size: {}", measurements.size());
        }
        if (!measurements.isEmpty()) {
            OnionperfStatus status = new OnionperfStatus(measurements);
            documentStore.store(status);
        }

        logger.info("Measurements clearing");
        measurements.clear();
        threshold = null;
    }

    private Collection<? extends Measurement> filter(List<Measurement> measurements, Long threshold) {
        return measurements.stream()
                .filter(m -> m.getStart().toLocalDateTime().atZone(ZoneOffset.UTC).toEpochSecond() > threshold)
                .collect(Collectors.toList());
    }

    @Override
    public String getStatsString() {
        /* TODO Add statistics string. */
        return null;
    }
}
