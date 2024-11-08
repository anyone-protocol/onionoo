package org.torproject.metrics.onionoo.userstats;

import java.time.LocalDate;

public class UpdateTemp {
    private String fingerprint;
    private String nickname;
    private Metric metric;
    private String country;
    private String transport;
    private String version;
    private LocalDate date;
    private double val;
    private long seconds;

    public UpdateTemp(String fingerprint, String nickname, Metric metric, String country,
                      String transport, String version, LocalDate date, double val, long seconds) {
        this.fingerprint = fingerprint;
        this.nickname = nickname;
        this.metric = metric;
        this.country = country;
        this.transport = transport;
        this.version = version;
        this.date = date;
        this.val = val;
        this.seconds = seconds;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public String getGroupKey() {
        return fingerprint + "-" + nickname + "-" + metric + "-" + country + "-" + transport + "-" + version + "-" + date;
    }

    public String key() {
        return date + "-" + country + "-" + transport + "-" + version;
    }

    public String anotherKey() {
        return date + "-" + fingerprint + "-" + nickname;
    }
}