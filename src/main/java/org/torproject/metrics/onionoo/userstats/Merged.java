package org.torproject.metrics.onionoo.userstats;

public class Merged implements UserStats {

    private long id;
    private String fingerprint;
    private String nickname;
    private Metric metric;
    private String country;
    private long statsStart;
    private long statsEnd;
    private double val;

    public Merged() {
    }

    public Merged(long id,
                  String fingerprint,
                  String nickname,
                  Metric metric,
                  String country,
                  long statsStart,
                  long statsEnd,
                  double val) {
        this.id = id;
        this.fingerprint = fingerprint;
        this.nickname = nickname;
        this.metric = metric;
        this.country = country;
        this.statsStart = statsStart;
        this.statsEnd = statsEnd;
        this.val = val;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        return "Merged{" +
                "id=" + id +
                ", fingerprint='" + fingerprint + '\'' +
                ", nickname='" + nickname + '\'' +
                ", metric=" + metric +
                ", country='" + country + '\'' +
                ", statsStart=" + statsStart +
                ", statsEnd=" + statsEnd +
                ", val=" + val +
                '}';
    }
}
