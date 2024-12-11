package org.torproject.metrics.onionoo.userstats;

public class CurrentMergeDto {

    private static final long DEFAULT = -1L;

    String lastFingerprint = null;
    String lastNickname = null;
    Metric lastMetric = null;
    String lastCountry = null;
    long lastStart = DEFAULT;
    long lastEnd = DEFAULT;
    long lastId = DEFAULT;
    double lastVal = DEFAULT;

}
