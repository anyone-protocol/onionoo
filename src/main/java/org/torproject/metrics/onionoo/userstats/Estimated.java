package org.torproject.metrics.onionoo.userstats;

import java.time.LocalDate;

public class Estimated {

    private LocalDate date;
    private String node;
    private String country;
    private String transport;
    private String version;
    private int frac;
    private int users;

    public Estimated(LocalDate date, String node, String country, String transport, String version, int frac, int users) {
        this.date = date;
        this.node = node;
        this.country = country;
        this.transport = transport;
        this.version = version;
        this.frac = frac;
        this.users = users;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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

    public int getFrac() {
        return frac;
    }

    public void setFrac(int frac) {
        this.frac = frac;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }
}
