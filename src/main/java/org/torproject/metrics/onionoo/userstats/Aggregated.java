package org.torproject.metrics.onionoo.userstats;

import java.time.LocalDate;

public class Aggregated {
    private LocalDate date;
    private String node;
    private String country;
    private String transport;
    private String version;
    private double rrx;
    private double nrx;
    private double hh;
    private double nn;
    private double hrh;
    private double nh;
    private double nrh;

    public Aggregated(LocalDate date,
                      String node,
                      String country,
                      String transport,
                      String version,
                      double rrx,
                      double nrx,
                      double hh,
                      double nn,
                      double hrh,
                      double nh,
                      double nrh) {
        this.date = date;
        this.node = node;
        this.country = country;
        this.transport = transport;
        this.version = version;
        this.rrx = rrx;
        this.nrx = nrx;
        this.hh = hh;
        this.nn = nn;
        this.hrh = hrh;
        this.nh = nh;
        this.nrh = nrh;
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

    public double getRrx() {
        return rrx;
    }

    public void setRrx(double rrx) {
        this.rrx = rrx;
    }

    public double getNrx() {
        return nrx;
    }

    public void setNrx(double nrx) {
        this.nrx = nrx;
    }

    public double getHh() {
        return hh;
    }

    public void setHh(double hh) {
        this.hh = hh;
    }

    public double getNn() {
        return nn;
    }

    public void setNn(double nn) {
        this.nn = nn;
    }

    public double getHrh() {
        return hrh;
    }

    public void setHrh(double hrh) {
        this.hrh = hrh;
    }

    public double getNh() {
        return nh;
    }

    public void setNh(double nh) {
        this.nh = nh;
    }

    public double getNrh() {
        return nrh;
    }

    public void setNrh(double nrh) {
        this.nrh = nrh;
    }

    @Override
    public String toString() {
        return "Aggregated{" +
                "date=" + date +
                ", node=" + node +
                ", country='" + country + '\'' +
                ", transport='" + transport + '\'' +
                ", version='" + version + '\'' +
                ", rrx=" + rrx +
                ", nrx=" + nrx +
                ", hh=" + hh +
                ", nn=" + nn +
                ", hrh=" + hrh +
                ", nh=" + nh +
                ", nrh=" + nrh +
                '}';
    }
}
