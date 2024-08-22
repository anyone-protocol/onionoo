package org.torproject.metrics.onionoo.onionperf;

import java.util.Date;

public class ThroughputStatistic {
    private Date date;
    private String source;
    private String server;
    private Double low;
    private Double q1;
    private Double md;
    private Double q3;
    private Double high;

    public ThroughputStatistic(Date date, String source, String server, Double low, Double q1, Double md, Double q3, Double high) {
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

    public Double getLow() {
        return low;
    }

    public Double getQ1() {
        return q1;
    }

    public Double getMd() {
        return md;
    }

    public Double getQ3() {
        return q3;
    }

    public Double getHigh() {
        return high;
    }
}
