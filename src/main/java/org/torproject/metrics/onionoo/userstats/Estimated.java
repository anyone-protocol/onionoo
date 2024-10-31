package org.torproject.metrics.onionoo.userstats;

import java.sql.Timestamp;

public class Estimated {

    private Timestamp date;
    private Node node;
    private String country;
    private String transport;
    private String version;
    private int frac;
    private int users;

    public Estimated(Timestamp date, Node node, String country, String transport, String version, int frac, int users) {
        this.date = date;
        this.node = node;
        this.country = country;
        this.transport = transport;
        this.version = version;
        this.frac = frac;
        this.users = users;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
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
