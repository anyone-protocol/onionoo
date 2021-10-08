/* Copyright 2011--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.writer;

import org.torproject.metrics.onionoo.docs.BandwidthDocument;
import org.torproject.metrics.onionoo.docs.BandwidthStatus;
import org.torproject.metrics.onionoo.docs.DateTimeHelper;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.GraphHistory;
import org.torproject.metrics.onionoo.docs.UpdateStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Period;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public class BandwidthDocumentWriter implements DocumentWriter {

  private static final Logger logger = LoggerFactory.getLogger(
      BandwidthDocumentWriter.class);

  private DocumentStore documentStore;

  public BandwidthDocumentWriter() {
    this.documentStore = DocumentStoreFactory.getDocumentStore();
  }

  @Override
  public void writeDocuments(long mostRecentStatusMillis) {
    UpdateStatus updateStatus = this.documentStore.retrieve(
        UpdateStatus.class, true);
    long updatedMillis = updateStatus != null
        ? updateStatus.getUpdatedMillis() : 0L;
    SortedSet<String> updateBandwidthDocuments = this.documentStore.list(
        BandwidthStatus.class, updatedMillis);
    for (String fingerprint : updateBandwidthDocuments) {
      BandwidthStatus bandwidthStatus = this.documentStore.retrieve(
          BandwidthStatus.class, true, fingerprint);
      if (bandwidthStatus == null) {
        continue;
      }
      BandwidthDocument bandwidthDocument = this.compileBandwidthDocument(
          fingerprint, mostRecentStatusMillis, bandwidthStatus);
      this.documentStore.store(bandwidthDocument, fingerprint);
    }
    logger.info("Wrote bandwidth document files");
  }

  private BandwidthDocument compileBandwidthDocument(String fingerprint,
      long mostRecentStatusMillis, BandwidthStatus bandwidthStatus) {
    BandwidthDocument bandwidthDocument = new BandwidthDocument();
    bandwidthDocument.setFingerprint(fingerprint);
    if (bandwidthStatus.getOverloadRatelimitsTimestamp() != -1L) {
      Map<String, Long> overloadRatelimits =
          bandwidthStatus.compileOverloadRatelimits();
      bandwidthDocument.setOverloadRatelimits(overloadRatelimits);
    }
    else {
      bandwidthDocument.setOverloadRatelimits(null);
    }
    if (bandwidthStatus.getOverloadFdExhaustedTimestamp() != -1L) {
      Map<String, Long> overloadFdExhausted =
          bandwidthStatus.compileOverloadFdExhausted();
      bandwidthDocument.setOverloadFdExhausted(overloadFdExhausted);
    }
    else {
      bandwidthDocument.setOverloadFdExhausted(null);
    }
    bandwidthDocument.setWriteHistory(this.compileGraphType(
        mostRecentStatusMillis, bandwidthStatus.getWriteHistory()));
    bandwidthDocument.setReadHistory(this.compileGraphType(
        mostRecentStatusMillis, bandwidthStatus.getReadHistory()));
    return bandwidthDocument;
  }

  private String[] graphNames = new String[] {
      "1_month",
      "6_months",
      "1_year",
      "5_years" };

  private Period[] graphIntervals = new Period[] {
      Period.ofMonths(1),
      Period.ofMonths(6),
      Period.ofYears(1),
      Period.ofYears(5) };

  private long[] dataPointIntervals = new long[] {
      DateTimeHelper.ONE_DAY,
      DateTimeHelper.ONE_DAY,
      DateTimeHelper.TWO_DAYS,
      DateTimeHelper.TEN_DAYS };

  private Map<String, GraphHistory> compileGraphType(
      long mostRecentStatusMillis, SortedMap<Long, long[]> history) {
    GraphHistoryCompiler ghc = new GraphHistoryCompiler(
        mostRecentStatusMillis + DateTimeHelper.ONE_HOUR);
    for (int i = 0; i < this.graphIntervals.length; i++) {
      ghc.addGraphType(this.graphNames[i], this.graphIntervals[i],
          this.dataPointIntervals[i]);
    }
    for (long[] v : history.values()) {
      ghc.addHistoryEntry(v[0], v[1],
          (double) (v[2] * DateTimeHelper.ONE_SECOND));
    }
    return ghc.compileGraphHistories();
  }

  @Override
  public String getStatsString() {
    /* TODO Add statistics string. */
    return null;
  }
}
