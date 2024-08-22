/* Copyright 2014--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.writer;

import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.NodeStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentWriterRunner {

  private static final Logger logger = LoggerFactory.getLogger(
      DocumentWriterRunner.class);

  private DocumentWriter[] documentWriters;

  /** Instantiates a new document writer runner with newly created
   * instances of all known document writer implementations. */
  public DocumentWriterRunner() {
    SummaryDocumentWriter sdw = new SummaryDocumentWriter();
    DetailsDocumentWriter ddw = new DetailsDocumentWriter();
    BandwidthDocumentWriter bdw = new BandwidthDocumentWriter();
    WeightsDocumentWriter wdw = new WeightsDocumentWriter();
    ClientsDocumentWriter cdw = new ClientsDocumentWriter();
    UptimeDocumentWriter udw = new UptimeDocumentWriter();
    OnionperfDocumentWriter opdw = new OnionperfDocumentWriter();
    this.documentWriters = new DocumentWriter[] { sdw, ddw, bdw, wdw, cdw, udw, opdw };
  }

  /** Lets each configured document writer write its documents. */
  public void writeDocuments() {
    long mostRecentStatusMillis = retrieveMostRecentStatusMillis();
    for (DocumentWriter dw : this.documentWriters) {
      logger.debug("Writing {}", dw.getClass().getSimpleName());
      dw.writeDocuments(mostRecentStatusMillis);
    }
  }

  private long retrieveMostRecentStatusMillis() {
    DocumentStore documentStore = DocumentStoreFactory.getDocumentStore();
    long mostRecentStatusMillis = -1L;
    for (String fingerprint : documentStore.list(NodeStatus.class)) {
      NodeStatus nodeStatus = documentStore.retrieve(
          NodeStatus.class, true, fingerprint);
      mostRecentStatusMillis = Math.max(mostRecentStatusMillis,
          nodeStatus.getLastSeenMillis());
    }
    return mostRecentStatusMillis;
  }

  /** Logs statistics of all configured document writers. */
  public void logStatistics() {
    for (DocumentWriter dw : this.documentWriters) {
      String statsString = dw.getStatsString();
      if (statsString != null) {
        logger.info("{}\n{}", dw.getClass().getSimpleName(), statsString);
      }
    }
  }
}

