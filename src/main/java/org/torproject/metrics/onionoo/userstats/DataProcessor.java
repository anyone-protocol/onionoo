package org.torproject.metrics.onionoo.userstats;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataProcessor {

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
