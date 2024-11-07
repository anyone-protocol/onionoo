package org.torproject.metrics.onionoo.userstats;

public class Merged implements UserStats {

    private int id;
    private String fingerprint;
    private String nickname;
    private String node;
    private String metric;
    private String country;
    private String transport;
    private String version;
    private long statsStart;
    private long statsEnd;
    private double val;

    public Merged() {
    }

    public Merged(int id, String fingerprint, String nickname, String node, String metric, String country, String transport, String version, long statsStart, long statsEnd, double val) {
        this.id = id;
        this.fingerprint = fingerprint;
        this.nickname = nickname;
        this.node = node;
        this.metric = metric;
        this.country = country;
        this.transport = transport;
        this.version = version;
        this.statsStart = statsStart;
        this.statsEnd = statsEnd;
        this.val = val;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
                ", node=" + node +
                ", metric=" + metric +
                ", country='" + country + '\'' +
                ", transport='" + transport + '\'' +
                ", version='" + version + '\'' +
                ", statsStart=" + statsStart +
                ", statsEnd=" + statsEnd +
                ", val=" + val +
                '}';
    }
}
