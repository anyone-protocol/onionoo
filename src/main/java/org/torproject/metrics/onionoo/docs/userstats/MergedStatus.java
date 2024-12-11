package org.torproject.metrics.onionoo.docs.userstats;

import org.torproject.metrics.onionoo.docs.Document;
import org.torproject.metrics.onionoo.userstats.Merged;

import java.util.List;

public class MergedStatus extends Document {

    private List<Merged> merged;

    public MergedStatus() {
    }

    public MergedStatus(List<Merged> merged) {
        this.merged = merged;
    }

    public List<Merged> getMerged() {
        return merged;
    }

    public void setMerged(List<Merged> merged) {
        this.merged = merged;
    }
}
