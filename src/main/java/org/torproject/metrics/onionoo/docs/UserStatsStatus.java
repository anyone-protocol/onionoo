package org.torproject.metrics.onionoo.docs;

import org.torproject.metrics.onionoo.userstats.Estimated;

import java.util.List;

public class UserStatsStatus extends Document {

    private List<Estimated> estimated;

    public UserStatsStatus(List<Estimated> estimated) {
        this.estimated = estimated;
    }

    public List<Estimated> getEstimated() {
        return estimated;
    }

    public void setEstimated(List<Estimated> estimated) {
        this.estimated = estimated;
    }
}
