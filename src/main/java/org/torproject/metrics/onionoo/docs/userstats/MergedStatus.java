package org.torproject.metrics.onionoo.docs.userstats;

import org.torproject.metrics.onionoo.docs.Document;
import org.torproject.metrics.onionoo.userstats.Merged;
import org.torproject.metrics.onionoo.userstats.Metric;

import java.util.ArrayList;
import java.util.List;

public class MergedStatus extends Document {

    private static final String NULL = "null";

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

    @Override
    public void setFromDocumentString(String documentString) {
        merged = new ArrayList<>();
        String[] lines = documentString.split("\n");
        for (String line : lines) {
            String[] parts = line.split(" ");
            if (parts.length == 8) {
                Merged m = new Merged();
                m.setId(Long.parseLong(parts[0]));
                m.setFingerprint(parts[1]);
                m.setNickname(parts[2]);
                m.setMetric(Metric.valueOf(parts[3]));
                String country = parts[4];
                m.setCountry(NULL.equals(country) ? null : country);
                m.setStatsStart(Long.parseLong(parts[5]));
                m.setStatsEnd(Long.parseLong(parts[6]));
                m.setVal(Double.parseDouble(parts[7]));
                merged.add(m);
            }
        }
    }

    @Override
    public String toDocumentString() {
        StringBuilder sb = new StringBuilder();

        for (Merged m : merged) {
            sb
                .append(m.getId()).append(" ")
                .append(m.getFingerprint()).append(" ")
                .append(m.getNickname()).append(" ")
                .append(m.getMetric().toString()).append(" ")
                .append(m.getCountry()).append(" ")
                .append(m.getStatsStart()).append(" ")
                .append(m.getStatsEnd()).append(" ")
                .append(m.getVal())
                .append("\n");
        }

        return sb.toString();
    }
}
