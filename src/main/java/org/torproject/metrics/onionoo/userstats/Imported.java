package org.torproject.metrics.onionoo.userstats;

import java.util.Objects;

public class Imported implements UserStats {

    private String fingerprint;
    private String nickname;
    private Metric metric;
    private String country;
    private long statsStart;
    private long statsEnd;
    private double val;

    public Imported(String fingerprint,
                    String nickname,
                    Metric metric,
                    String country,
                    long statsStart,
                    long statsEnd,
                    double val) {
        this.fingerprint = fingerprint;
        this.nickname = nickname;
        this.metric = metric;
        this.country = country;
        this.statsStart = statsStart;
        this.statsEnd = statsEnd;
        this.val = val;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getStatsStart() {
        return statsStart;
    }

    public void setStatsStart(long statsStart) {
        this.statsStart = statsStart;
    }

    public long getStatsEnd() {
        return statsEnd;
    }

    public void setStatsEnd(long statsEnd) {
        this.statsEnd = statsEnd;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "Imported{" +
                "fingerprint='" + fingerprint + '\'' +
                ", nickname='" + nickname + '\'' +
                ", metric=" + metric +
                ", country='" + country + '\'' +
                ", statsStart=" + statsStart +
                ", statsEnd=" + statsEnd +
                ", val=" + val +
                '}';
    }

    public String getKey() {
        return String.join("-", fingerprint, nickname, metric.name(), country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Imported imported = (Imported) o;
        return statsStart == imported.statsStart && statsEnd == imported.statsEnd && Double.compare(imported.val, val) == 0 && Objects.equals(fingerprint, imported.fingerprint) && Objects.equals(nickname, imported.nickname) && metric == imported.metric && Objects.equals(country, imported.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fingerprint, nickname, metric, country, statsStart, statsEnd, val);
    }
}
