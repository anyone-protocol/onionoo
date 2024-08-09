package org.torproject.metrics.onionoo.onionperf;

import java.util.Date;

public class LatencyStatistic {
    private Date date;
    private String source;
    private String server;
    private Integer low;
    private Integer q1;
    private Integer md;
    private Integer q3;
    private Integer high;

    public LatencyStatistic(Date date, String source, String server, Integer low, Integer q1, Integer md, Integer q3, Integer high) {
        this.date = date;
        this.source = source;
        this.server = server;
        this.low = low;
        this.q1 = q1;
        this.md = md;
        this.q3 = q3;
        this.high = high;
    }

    public Date getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getServer() {
        return server;
    }

    public Integer getLow() {
        return low;
    }

    public Integer getQ1() {
        return q1;
    }

    public Integer getMd() {
        return md;
    }

    public Integer getQ3() {
        return q3;
    }

    public Integer getHigh() {
        return high;
    }
}
