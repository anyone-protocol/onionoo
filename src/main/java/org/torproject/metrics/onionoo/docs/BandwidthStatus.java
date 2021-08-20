/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.docs;

import org.torproject.descriptor.BandwidthHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class BandwidthStatus extends Document {

  private static final Logger logger = LoggerFactory.getLogger(
      BandwidthStatus.class);

  private transient boolean isDirty = false;

  public boolean isDirty() {
    return this.isDirty;
  }

  public void clearDirty() {
    this.isDirty = false;
  }

  private SortedMap<Long, long[]> writeHistory = new TreeMap<>();

  public void setWriteHistory(SortedMap<Long, long[]> writeHistory) {
    this.writeHistory = writeHistory;
  }

  public SortedMap<Long, long[]> getWriteHistory() {
    return this.writeHistory;
  }

  private SortedMap<Long, long[]> readHistory = new TreeMap<>();

  public void setReadHistory(SortedMap<Long, long[]> readHistory) {
    this.readHistory = readHistory;
  }

  public SortedMap<Long, long[]> getReadHistory() {
    return this.readHistory;
  }

  private long overloadRatelimitsTimestamp = -1L;

  public void setOverloadRatelimitsTimestamp(
      long overloadRatelimitsTimestamp
  ) {
    this.overloadRatelimitsTimestamp = overloadRatelimitsTimestamp;
  }

  public long getOverloadRatelimitsTimestamp() {
    return this.overloadRatelimitsTimestamp;
  }

  private long overloadRatelimitsBurstLimit = -1L;

  public void setOverloadRatelimitsBurstLimit(
      long overloadRatelimitsBurstLimit
  ) {
    this.overloadRatelimitsBurstLimit = overloadRatelimitsBurstLimit;
  }

  public long getOverloadRatelimitsBurstLimit() {
    return this.overloadRatelimitsBurstLimit;
  }

  private long overloadRatelimitsRateLimit = -1L;

  public void setOverloadRatelimitsRateLimit(
      long overloadRatelimitsRateLimit
  ) {
    this.overloadRatelimitsRateLimit = overloadRatelimitsRateLimit;
  }

  public long getOverloadRatelimitsRateLimit() {
    return this.overloadRatelimitsRateLimit;
  }

  private long overloadRatelimitsReadCount = -1L;

  public void setOverloadRatelimitsReadCount(
      long overloadRatelimitsReadCount
  ) {
    this.overloadRatelimitsReadCount = overloadRatelimitsReadCount;
  }

  public long getOverloadRatelimitsReadCount() {
    return this.overloadRatelimitsReadCount;
  }

  private long overloadRatelimitsWriteCount = -1L;

  public void setOverloadRatelimitsWriteCount(
      long overloadRatelimitsWriteCount
  ) {
    this.overloadRatelimitsWriteCount = overloadRatelimitsWriteCount;
  }

  public long getOverloadRatelimitsWriteCount() {
    return this.overloadRatelimitsWriteCount;
  }

  private long overloadFdExhaustedTimestamp = -1L;

  public void setOverloadFdExhaustedTimestamp(
      long overloadFdExhaustedTimestamp
  ) {
    this.overloadFdExhaustedTimestamp = overloadFdExhaustedTimestamp;
  }

  public long getOverloadFdExhaustedTimestamp() {
    return this.overloadFdExhaustedTimestamp;
  }

  /**
   * Compile a hash of the overload_rate_limits fields.
   *
   * @return overloadRatelimits object
   */
  public Map<String, Long> compileOverloadRatelimits() {
    Map<String, Long> overloadRatelimits = new HashMap();

    overloadRatelimits.put("timestamp",
        this.getOverloadRatelimitsTimestamp()
    );
    overloadRatelimits.put("rate-limit",
        this.getOverloadRatelimitsRateLimit()
    );
    overloadRatelimits.put("burst-limit",
        this.getOverloadRatelimitsBurstLimit()
    );
    overloadRatelimits.put("read-count",
        this.getOverloadRatelimitsReadCount()
    );
    overloadRatelimits.put("write-count",
        this.getOverloadRatelimitsWriteCount()
    );
    return overloadRatelimits;
  }

  /**
   * Compile a hash of the overload_fd_exhausted fields.
   *
   * @return overloadFdExhausted object
   */
  public Map<String, Long> compileOverloadFdExhausted() {
    Map<String, Long> overloadFdExhausted = new HashMap();

    overloadFdExhausted.put("timestamp",
        this.getOverloadFdExhaustedTimestamp()
    );
    return overloadFdExhausted;
  }

  @Override
  public void setFromDocumentString(String documentString) {
    try (Scanner s = new Scanner(documentString)) {
      while (s.hasNextLine()) {
        String line = s.nextLine();
        String[] parts = line.split(" ");
        if ((parts.length == 7) & (parts[0].equals("rl"))) {
          this.setOverloadRatelimitsTimestamp(
                  DateTimeHelper.parse(parts[1] + " " + parts[2])
          );
          this.setOverloadRatelimitsRateLimit(
                  Long.parseLong(parts[3])
          );
          this.setOverloadRatelimitsBurstLimit(
                  Long.parseLong(parts[4])
          );
          this.setOverloadRatelimitsReadCount(
                  Integer.parseInt(parts[5])
          );
          this.setOverloadRatelimitsWriteCount(
                  Integer.parseInt(parts[6])
          );
        } else if ((parts.length == 3) & (parts[0].equals("fd"))) {
          this.setOverloadFdExhaustedTimestamp(
                  DateTimeHelper.parse(parts[1] + " " + parts[2])
          );
        } else if (parts.length == 6) {
          SortedMap<Long, long[]> history = parts[0].equals("r")
                  ? readHistory : writeHistory;
          long startMillis = DateTimeHelper.parse(parts[1] + " " + parts[2]);
          long endMillis = DateTimeHelper.parse(parts[3] + " " + parts[4]);
          if (startMillis < 0L || endMillis < 0L) {
            logger.error("Could not parse timestamp while reading "
                    + "bandwidth history.  Skipping.");
            break;
          }
          long bandwidth = Long.parseLong(parts[5]);
          long previousEndMillis = history.headMap(startMillis).isEmpty()
                  ? startMillis
                  : history.get(history.headMap(startMillis).lastKey())[1];
          long nextStartMillis = history.tailMap(startMillis).isEmpty()
                  ? endMillis : history.tailMap(startMillis).firstKey();
          if (previousEndMillis <= startMillis
                  && nextStartMillis >= endMillis) {
            history.put(
                    startMillis, new long[]{startMillis, endMillis, bandwidth}
            );
          }
        } else {
          logger.error("Illegal line '{}' in bandwidth history. Skipping this "
                  + "line.", line);
          continue;
        }
      }
    }
  }

  public void addToWriteHistory(BandwidthHistory bandwidthHistory) {
    this.addToHistory(this.writeHistory, bandwidthHistory);
  }

  public void addToReadHistory(BandwidthHistory bandwidthHistory) {
    this.addToHistory(this.readHistory, bandwidthHistory);
  }

  private void addToHistory(SortedMap<Long, long[]> history,
      BandwidthHistory bandwidthHistory) {
    long intervalMillis = bandwidthHistory.getIntervalLength()
        * DateTimeHelper.ONE_SECOND;
    for (Map.Entry<Long, Long> e :
        bandwidthHistory.getBandwidthValues().entrySet()) {
      long endMillis = e.getKey();
      long startMillis = endMillis - intervalMillis;
      long bandwidthValue = e.getValue();
      /* TODO Should we first check whether an interval is already
       * contained in history? */
      history.put(startMillis, new long[] { startMillis, endMillis,
          bandwidthValue });
      this.isDirty = true;
    }
  }

  public void compressHistory(long lastSeenMillis) {
    this.compressHistory(this.writeHistory, lastSeenMillis);
    this.compressHistory(this.readHistory, lastSeenMillis);
  }

  private void compressHistory(SortedMap<Long, long[]> history,
      long lastSeenMillis) {
    SortedMap<Long, long[]> uncompressedHistory = new TreeMap<>(history);
    history.clear();
    long lastStartMillis = 0L;
    long lastEndMillis = 0L;
    long lastBandwidth = 0L;
    String lastMonthString = "1970-01";
    for (long[] v : uncompressedHistory.values()) {
      long startMillis = v[0];
      long endMillis = v[1];
      long bandwidth = v[2];
      long intervalLengthMillis;
      if (lastSeenMillis - endMillis <= DateTimeHelper.THREE_DAYS) {
        intervalLengthMillis = DateTimeHelper.FIFTEEN_MINUTES;
      } else if (lastSeenMillis - endMillis <= DateTimeHelper.ONE_WEEK) {
        intervalLengthMillis = DateTimeHelper.ONE_HOUR;
      } else if (lastSeenMillis - endMillis
          <= DateTimeHelper.ROUGHLY_ONE_MONTH) {
        intervalLengthMillis = DateTimeHelper.FOUR_HOURS;
      } else if (lastSeenMillis - endMillis
          <= DateTimeHelper.ROUGHLY_SIX_MONTHS) {
        intervalLengthMillis = DateTimeHelper.ONE_DAY;
      } else if (lastSeenMillis - endMillis
          <= DateTimeHelper.ROUGHLY_ONE_YEAR) {
        intervalLengthMillis = DateTimeHelper.TWO_DAYS;
      } else {
        intervalLengthMillis = DateTimeHelper.TEN_DAYS;
      }
      String monthString = DateTimeHelper.format(startMillis,
          DateTimeHelper.ISO_YEARMONTH_FORMAT);
      if (lastEndMillis == startMillis
          && ((lastEndMillis - 1L) / intervalLengthMillis)
          == ((endMillis - 1L) / intervalLengthMillis)
          && lastMonthString.equals(monthString)) {
        lastEndMillis = endMillis;
        lastBandwidth += bandwidth;
      } else {
        if (lastStartMillis > 0L) {
          history.put(lastStartMillis, new long[] { lastStartMillis,
              lastEndMillis, lastBandwidth });
        }
        lastStartMillis = startMillis;
        lastEndMillis = endMillis;
        lastBandwidth = bandwidth;
      }
      lastMonthString = monthString;
    }
    if (lastStartMillis > 0L) {
      history.put(lastStartMillis, new long[] { lastStartMillis,
          lastEndMillis, lastBandwidth });
    }
  }

  @Override
  public String toDocumentString() {
    StringBuilder sb = new StringBuilder();
    for (long[] v : writeHistory.values()) {
      sb.append("w ").append(DateTimeHelper.format(v[0])).append(" ")
          .append(DateTimeHelper.format(v[1])).append(" ")
          .append(v[2]).append("\n");
    }
    for (long[] v : readHistory.values()) {
      sb.append("r ").append(DateTimeHelper.format(v[0])).append(" ")
          .append(DateTimeHelper.format(v[1])).append(" ")
          .append(v[2]).append("\n");
    }
    if (this.overloadRatelimitsTimestamp != -1L) {
      sb.append("rl ").append(
              DateTimeHelper.format(this.overloadRatelimitsTimestamp)
              )
      .append(" ").append(this.overloadRatelimitsRateLimit)
      .append(" ").append(this.overloadRatelimitsBurstLimit)
      .append(" ").append(this.overloadRatelimitsReadCount)
      .append(" ").append(this.overloadRatelimitsWriteCount)
      .append("\n");
    }
    if (this.overloadFdExhaustedTimestamp != -1L) {
      sb.append("fd ").append(
              DateTimeHelper.format(this.overloadFdExhaustedTimestamp)
      ).append("\n");
    }
    return sb.toString();
  }
}
