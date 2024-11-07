package org.torproject.metrics.onionoo.userstats;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataProcessor {

    // Method to merge imported data into merged data
    public static List<Merged> merge(List<Imported> importedList) {
        int idCounter = 1;
        List<Merged> mergedList = new ArrayList<>();

        // Step 1: Group by unique fields (fingerprint, nickname, node, metric, country, transport, version)
        Map<String, List<Imported>> groupedImported = importedList.stream().collect(Collectors.groupingBy(
                imported -> String.join("-", imported.getFingerprint(), imported.getNickname(),
                        imported.getNode(), imported.getMetric(),
                        imported.getCountry(), imported.getTransport(), imported.getVersion())
        ));

        // Step 2: Process each group independently
        for (List<Imported> group : groupedImported.values()) {
            // Sort each group by startTime to ensure intervals are processed in sequence
            group.sort(Comparator.comparing(Imported::getStatsStart));

            // Initialize variables to track merging within the group
            long lastStartTime = group.get(0).getStatsStart();
            long lastEndTime = group.get(0).getStatsEnd();
            double lastVal = group.get(0).getVal();

            // Use first entry to initialize shared fields for Merged
            String fingerprint = group.get(0).getFingerprint();
            String nickname = group.get(0).getNickname();
            String node = group.get(0).getNode();
            String metric = group.get(0).getMetric();
            String country = group.get(0).getCountry();
            String transport = group.get(0).getTransport();
            String version = group.get(0).getVersion();

            // Merge intervals within the sorted group
            for (int i = 1; i < group.size(); i++) {
                Imported current = group.get(i);

                if (current.getStatsStart() <= lastEndTime) {
                    // Overlapping or adjacent interval, extend the end time and accumulate the value
                    lastEndTime = Math.max(lastEndTime, current.getStatsEnd());
                    lastVal += current.getVal();
                } else {
                    // No overlap, add the previous merged interval to mergedList
                    mergedList.add(new Merged(idCounter++, fingerprint, nickname, node, metric, country,
                            transport, version, lastStartTime, lastEndTime, lastVal));

                    // Start a new interval
                    lastStartTime = current.getStatsStart();
                    lastEndTime = current.getStatsEnd();
                    lastVal = current.getVal();
                }
            }

            // Add the last merged interval of the group to mergedList
            mergedList.add(new Merged(idCounter++, fingerprint, nickname, node, metric, country,
                    transport, version, lastStartTime, lastEndTime, lastVal));
        }

        return mergedList;
    }

    public static List<Aggregated> aggregate(List<Merged> mergedList) {
        List<Aggregated> aggregatedList = new ArrayList<>();

        // Group merged entries by date and unique key attributes
        Map<String, List<Merged>> groupedMerges = mergedList.stream().collect(
                Collectors.groupingBy(merge -> String.join("-",
                        Instant.ofEpochMilli(merge.getStatsStart())
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDateTime()
                                .toLocalDate().toString(), merge.getNode(),
                        merge.getCountry(), merge.getTransport(), merge.getVersion()))
        );

        for (List<Merged> group : groupedMerges.values()) {
            LocalDate date = Instant.ofEpochMilli(group.get(0).getStatsStart()).atZone(ZoneOffset.UTC).toLocalDate();
            String node = group.get(0).getNode();
            String country = group.get(0).getCountry();
            String transport = group.get(0).getTransport();
            String version = group.get(0).getVersion();

            // Initialize aggregates
            double rrx = 0, nrx = 0, hh = 0, nn = 0, hrh = 0, nh = 0, nrh = 0;

            // Sum values for each metric type
            for (Merged merged : group) {
                if (merged.getMetric().equals("responses")) {
                    rrx += merged.getVal();
                    nrx += getDurationSeconds(merged.getStatsStart(), merged.getStatsEnd());
                } else if (merged.getMetric().equals("bytes")) {
                    hh += merged.getVal();
                    nh += getDurationSeconds(merged.getStatsStart(), merged.getStatsEnd());
                } else if (merged.getMetric().equals("status")) {
                    nn += getDurationSeconds(merged.getStatsStart(), merged.getStatsEnd());
                }
                if (merged.getMetric().equals("bytes") && rrx > 0) {
                    hrh += Math.min(hh, rrx);
                }
                if (merged.getMetric().equals("responses") && hh == 0) {
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
//                if (frac >= 0.1 && frac <= 1.1) {
                    // Round fraction to an integer percent
                    int fracPercent = (int) Math.round(frac * 100);

                    // Estimate users based on rrx and fraction
                    int users = (int) Math.round(agg.getRrx() / (frac * 10));

                    // Only include estimates older than yesterday
//                    if (agg.getDate().isBefore(LocalDate.now().minusDays(1))) {
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
//                    }
//                }
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
    private static double getDurationSeconds(long start, long end) {
        return Duration.between(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end)).getSeconds();
    }

}
