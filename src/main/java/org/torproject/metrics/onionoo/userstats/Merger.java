package org.torproject.metrics.onionoo.userstats;

import java.util.*;
import java.util.stream.Collectors;

public class Merger {
    private static int idCounter = 1;

    public static List<Merged> mergeFirstTime(List<Imported> importedList) {
        List<Merged> mergedList = new ArrayList<>();

        // Step 1: Group by unique fields (fingerprint, nickname, node, metric, country)
        Map<String, List<Imported>> groupedImported = importedList.stream().collect(Collectors.groupingBy(
                imported -> String.join("-", imported.getFingerprint(), imported.getNickname(),
                        imported.getMetric().name(), imported.getCountry())
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
            Metric metric = group.get(0).getMetric();
            String country = group.get(0).getCountry();

            // Merge intervals within the sorted group
            for (int i = 1; i < group.size(); i++) {
                Imported current = group.get(i);

                if (current.getStatsStart() <= lastEndTime) {
                    // Overlapping or adjacent interval, extend the end time and accumulate the value
                    lastEndTime = Math.max(lastEndTime, current.getStatsEnd());
                    lastVal += current.getVal();
                } else {
                    // No overlap, add the previous merged interval to mergedList
                    mergedList.add(new Merged(idCounter++, fingerprint, nickname, metric, country,
                            lastStartTime, lastEndTime, lastVal));

                    // Start a new interval
                    lastStartTime = current.getStatsStart();
                    lastEndTime = current.getStatsEnd();
                    lastVal = current.getVal();
                }
            }

            // Add the last merged interval of the group to mergedList
            mergedList.add(new Merged(idCounter++, fingerprint, nickname, metric, country, lastStartTime, lastEndTime, lastVal));
        }

        return mergedList;
    }
}