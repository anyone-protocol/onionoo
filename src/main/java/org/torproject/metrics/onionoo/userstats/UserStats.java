package org.torproject.metrics.onionoo.userstats;

public interface UserStats {

    String getFingerprint();
    String getNickname();
    Metric getMetric();
    String getCountry();
    long getStatsStart();
    long getStatsEnd();
    double getVal();

}
