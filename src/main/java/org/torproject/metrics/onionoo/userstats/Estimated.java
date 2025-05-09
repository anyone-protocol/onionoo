package org.torproject.metrics.onionoo.userstats;

public class Estimated {

    private String date;
    private String country;
    private int frac;
    private int users;

    public Estimated(String date, String country, int frac, int users) {
        this.date = date;
        this.country = country;
        this.frac = frac;
        this.users = users;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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
