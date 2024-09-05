package org.torproject.metrics.onionoo.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.OnionperfStatus;
import org.torproject.metrics.onionoo.docs.onionperf.*;

public class OnionperfDocumentWriter implements DocumentWriter {

    private static final Logger logger = LoggerFactory.getLogger(OnionperfDocumentWriter.class);

    private final DocumentStore documentStore;

    public OnionperfDocumentWriter() {
        this.documentStore = DocumentStoreFactory.getDocumentStore();
    }

    @Override
    public void writeDocuments(long mostRecentStatusMillis) {
        logger.info("Writing onionperf documents time: {}", mostRecentStatusMillis);
        OnionperfStatus status = documentStore.retrieve(OnionperfStatus.class, true);
        if (status != null) {
            logger.info("OnionperfStatus: {}", status.getMeasurements().size());
            documentStore.store(new CircuitDocument(status.getMeasurements()));
            documentStore.store(new DownloadDocument(status.getMeasurements()));
            documentStore.store(new FailureDocument(status.getMeasurements()));
            documentStore.store(new LatencyDocument(status.getMeasurements()));
            documentStore.store(new ThroughputDocument(status.getMeasurements()));
            logger.info("Performance documents saved");
        } else {
            logger.info("No onionperf status found");
        }
    }

    @Override
    public String getStatsString() {
        return null;
    }
}
