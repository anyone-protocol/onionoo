package org.torproject.metrics.onionoo.userstats;

public interface UserStats {

    String getFingerprint();
    String getNickname();
    Metric getMetric();
    String getCountry();
    String getTransport();
    String getVersion();
    long getStatsStart();
    long getStatsEnd();
    double getVal();

}
