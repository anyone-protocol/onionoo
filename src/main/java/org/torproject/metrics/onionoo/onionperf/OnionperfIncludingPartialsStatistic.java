package org.torproject.metrics.onionoo.onionperf;

import java.util.Date;

public class OnionperfIncludingPartialsStatistic {
    private Date date;
    private int filesize;
    private String source;
    private String server;
    private Double q1;
    private Double md;
    private Double q3;

    // Constructor
    public OnionperfIncludingPartialsStatistic(Date date, int filesize, String source, String server, Double q1, Double md, Double q3) {
        this.date = date;
        this.filesize = filesize;
        this.source = source;
        this.server = server;
        this.q1 = q1;
        this.md = md;
        this.q3 = q3;
    }

    // Getters
    public Date getDate() { return date; }
    public int getFilesize() { return filesize; }
    public String getSource() { return source; }
    public String getServer() { return server; }
    public Double getQ1() { return q1; }
    public Double getMd() { return md; }
    public Double getQ3() { return q3; }
}
