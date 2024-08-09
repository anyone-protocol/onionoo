package org.torproject.metrics.onionoo.onionperf;

import java.util.Date;

public class OnionperfFailureStatistic {
    private Date date;
    private String source;
    private String server;
    private int timeouts;
    private int failures;
    private int requests;

    // Constructor
    public OnionperfFailureStatistic(Date date, String source, String server, int timeouts, int failures, int requests) {
        this.date = date;
        this.source = source;
        this.server = server;
        this.timeouts = timeouts;
        this.failures = failures;
        this.requests = requests;
    }

    // Getters
    public Date getDate() { return date; }
    public String getSource() { return source; }
    public String getServer() { return server; }
    public int getTimeouts() { return timeouts; }
    public int getFailures() { return failures; }
    public int getRequests() { return requests; }
}
