package org.torproject.metrics.onionoo.userstats;

import java.sql.Timestamp;

public interface UserStats {

    String getFingerprint();
    String getNickname();
    String getNode();
    String getMetric();
    String getCountry();
    String getTransport();
    String getVersion();
    long getStatsStart();
    long getStatsEnd();
    double getVal();

}
