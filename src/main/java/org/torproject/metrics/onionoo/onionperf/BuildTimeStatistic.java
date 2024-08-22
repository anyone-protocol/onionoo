package org.torproject.metrics.onionoo.onionperf;

import java.util.Date;

public class BuildTimeStatistic {
    private Date date;
    private String source;
    private int position;
    private Double q1;
    private Double md;
    private Double q3;

    public BuildTimeStatistic(Date date, String source, int position, Double q1, Double md, Double q3) {
        this.date = date;
        this.source = source;
        this.position = position;
        this.q1 = q1;
        this.md = md;
        this.q3 = q3;
    }

    public Date getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public int getPosition() {
        return position;
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
}
