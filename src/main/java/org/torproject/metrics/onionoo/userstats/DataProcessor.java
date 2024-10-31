package org.torproject.metrics.onionoo.userstats;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataProcessor {

    // Method to merge imported data into merged data
    public static List<Merged> merge(List<Imported> importedList) {
        List<Merged> mergedList = new ArrayList<>();
        System.out.println(mergedList);

        // Group Imported entries by unique keys and process each group
        Map<String, List<Imported>> groupedImports = importedList.stream().collect(
                Collectors.groupingBy(imp -> String.join("-",
                        imp.getFingerprint(), imp.getNickname(), imp.getNode().name(),
                        imp.getMetric().name(), imp.getCountry(), imp.getTransport(), imp.getVersion()))
        );

        int idCounter = 1;
        for (List<Imported> group : groupedImports.values()) {
            Merged mergedEntry = new Merged(idCounter++,
                    group.get(0).getFingerprint(), group.get(0).getNickname(), group.get(0).getNode(),
                    group.get(0).getMetric(), group.get(0).getCountry(), group.get(0).getTransport(),
                    group.get(0).getVersion(), group.get(0).getStatsStart(), group.get(0).getStatsEnd(), 0);

            // Merge intervals within each group
            for (Imported imp : group) {
                if (imp.getStatsStart().after(mergedEntry.getStatsEnd())) {
                    // If there’s a gap, create a new merged entry
                    mergedEntry.setStatsEnd(imp.getStatsEnd());
                    mergedEntry.setVal(mergedEntry.getVal() + imp.getVal());
                } else {
                    mergedEntry.setStatsEnd(imp.getStatsEnd().after(mergedEntry.getStatsEnd()) ? imp.getStatsEnd() : mergedEntry.getStatsEnd());
                    mergedEntry.setVal(mergedEntry.getVal() + imp.getVal());
                }
            }
            mergedList.add(mergedEntry);
        }
        return mergedList;
    }

    public static List<Aggregated> aggregate(List<Merged> mergedList) {
        List<Aggregated> aggregatedList = new ArrayList<>();

        // Group merged entries by date and unique key attributes
        Map<String, List<Merged>> groupedMerges = mergedList.stream().collect(
                Collectors.groupingBy(merge -> String.join("-",
                        merge.getStatsStart().toLocalDateTime().toLocalDate().toString(), merge.getNode().name(),
                        merge.getCountry(), merge.getTransport(), merge.getVersion()))
        );

        for (List<Merged> group : groupedMerges.values()) {
            Timestamp date = group.get(0).getStatsStart();
            Node node = group.get(0).getNode();
            String country = group.get(0).getCountry();
            String transport = group.get(0).getTransport();
            String version = group.get(0).getVersion();

            // Initialize aggregates
            double rrx = 0, nrx = 0, hh = 0, nn = 0, hrh = 0, nh = 0, nrh = 0;

            // Sum values for each metric type
            for (Merged merged : group) {
                if (merged.getMetric() == Metric.RESPONSES) {
                    rrx += merged.getVal();
                    nrx += getDurationSeconds(merged.getStatsStart(), merged.getStatsEnd());
                } else if (merged.getMetric() == Metric.BYTES) {
                    hh += merged.getVal();
                    nh += getDurationSeconds(merged.getStatsStart(), merged.getStatsEnd());
                } else if (merged.getMetric() == Metric.STATUS) {
                    nn += getDurationSeconds(merged.getStatsStart(), merged.getStatsEnd());
                }
                if (merged.getMetric() == Metric.BYTES && rrx > 0) {
                    hrh += Math.min(hh, rrx);
                }
                if (merged.getMetric() == Metric.RESPONSES && hh == 0) {
                    nrh += nrx;
                }
            }

            Aggregated aggregatedEntry = new Aggregated(date, node, country, transport, version, rrx, nrx, hh, nn, hrh, nh, nrh);
            aggregatedList.add(aggregatedEntry);
        }
        return aggregatedList;
    }

    public static List<Estimated> estimate(List<Aggregated> aggregatedList) {
        List<Estimated> estimatedList = new ArrayList<>();

        for (Aggregated agg : aggregatedList) {
//            if (agg.getHh() > 0 && agg.getNn() > 0) {
                // Calculate the fraction for estimation
                double frac = (agg.getHrh() * agg.getNh() + agg.getHh() * agg.getNrh()) / (agg.getHh() * agg.getNn());

                // Only include entries where frac is between 0.1 and 1.1
                if (frac >= 0.1 && frac <= 1.1) {
                    // Round fraction to an integer percent
                    int fracPercent = (int) Math.round(frac * 100);

                    // Estimate users based on rrx and fraction
                    int users = (int) Math.round(agg.getRrx() / (frac * 10));

                    // Only include estimates older than yesterday
                    if (agg.getDate().before(Timestamp.from(Instant.now().minusSeconds(3600)))) {
                        Estimated estimated = new Estimated(
                                agg.getDate(),
                                agg.getNode(),
                                agg.getCountry(),
                                agg.getTransport(),
                                agg.getVersion(),
                                fracPercent,
                                users
                        );
                        estimatedList.add(estimated);
                    }
                }
//            }
        }

        // Sort results by date, node, version, transport, and country (similar to SQL ORDER BY clause)
        estimatedList.sort(Comparator.comparing(Estimated::getDate)
                .thenComparing(Estimated::getNode)
                .thenComparing(Estimated::getVersion)
                .thenComparing(Estimated::getTransport)
                .thenComparing(Estimated::getCountry));

        return estimatedList;
    }

    // Helper function to calculate duration in seconds between start and end times
    private static double getDurationSeconds(Timestamp start, Timestamp end) {
        return Duration.between(start.toInstant(), end.toInstant()).getSeconds();
    }

}
