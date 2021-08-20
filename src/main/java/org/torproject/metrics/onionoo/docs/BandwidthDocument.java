/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.docs;

import java.util.Map;

public class BandwidthDocument extends Document {

  @SuppressWarnings("unused")
  private String fingerprint;

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  private Map<String, GraphHistory> writeHistory;

  public void setWriteHistory(Map<String, GraphHistory> writeHistory) {
    this.writeHistory = writeHistory;
  }

  public Map<String, GraphHistory> getWriteHistory() {
    return this.writeHistory;
  }

  private Map<String, GraphHistory> readHistory;

  public void setReadHistory(Map<String, GraphHistory> readHistory) {
    this.readHistory = readHistory;
  }

  public Map<String, GraphHistory> getReadHistory() {
    return this.readHistory;
  }

  private Map<String, Long> overloadRatelimits;

  public void setOverloadRatelimits(Map<String, Long> overloadRatelimits) {
    this.overloadRatelimits = overloadRatelimits;
  }

  public Map<String, Long> getOverloadRatelimits() {
    return this.overloadRatelimits;
  }

  private Map<String, Long> overloadFdExhausted;

  public void setOverloadFdExhausted(
      Map<String, Long> overloadFdExhausted
  ) {
    this.overloadFdExhausted = overloadFdExhausted;
  }

  public Map<String, Long> getOverloadFdExhausted() {
    return this.overloadFdExhausted;
  }

}
