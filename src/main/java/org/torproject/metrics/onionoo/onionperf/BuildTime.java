package org.torproject.metrics.onionoo.onionperf;

import java.sql.Timestamp;

public class BuildTime {
    private Timestamp start;
    private String source;
    private int position;
    private int buildtime;
    private int delta;

    public BuildTime() {
    }

    public BuildTime(Timestamp start, String source, int position, int buildtime, int delta) {
        this.start = start;
        this.source = source;
        this.position = position;
        this.buildtime = buildtime;
        this.delta = delta;
    }

    public Timestamp getStart() {
        return start;
    }

    public String getSource() {
        return source;
    }

    public int getPosition() {
        return position;
    }

    public int getBuildtime() {
        return buildtime;
    }

    public int getDelta() {
        return delta;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setBuildtime(int buildtime) {
        this.buildtime = buildtime;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }
}