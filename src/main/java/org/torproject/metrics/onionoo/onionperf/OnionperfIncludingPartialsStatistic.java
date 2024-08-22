package org.torproject.metrics.onionoo.onionperf;

import java.util.Date;

public class OnionperfIncludingPartialsStatistic {
    private final Date date;
    private final int filesize;
    private final String source;
    private final String serverType;
    private final Double q1;
    private final Double md;
    private final Double q3;

    // Constructor
    public OnionperfIncludingPartialsStatistic(Date date, int filesize, String source, String serverType, Double q1, Double md, Double q3) {
        this.date = date;
        this.filesize = filesize;
        this.source = source;
        this.serverType = serverType;
        this.q1 = q1;
        this.md = md;
        this.q3 = q3;
    }

    // Getters
    public Date getDate() { return date; }
    public int getFilesize() { return filesize; }
    public String getSource() { return source; }
    public String getServerType() { return serverType; }
    public Double getQ1() { return q1; }
    public Double getMd() { return md; }
    public Double getQ3() { return q3; }
}
