package org.torproject.metrics.onionoo.userstats;

import java.time.LocalDate;

public class UpdateTemp {
    private String fingerprint;
    private String nickname;
    private Metric metric;
    private String country;
    private LocalDate date;
    private double val;
    private double seconds;

    public UpdateTemp(String fingerprint, String nickname, Metric metric, String country,
                      LocalDate date, double val, double seconds) {
        this.fingerprint = fingerprint;
        this.nickname = nickname;
        this.metric = metric;
        this.country = country;
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

    public double getSeconds() {
        return seconds;
    }

    public void setSeconds(double seconds) {
        this.seconds = seconds;
    }

    public String getGroupKey() {
        return fingerprint + "-" + nickname + "-" + metric + "-" + country + "-" + date;
    }

    public String key() {
        return date + "-" + country;
    }

    public String anotherKey() {
        return date + "-" + fingerprint + "-" + nickname;
    }
}