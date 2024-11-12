package org.torproject.metrics.onionoo.userstats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class Aggregator {

    public List<Aggregated> aggregate(List<Merged> merged) {

        Map<String, UpdateTemp> updateTemp = merged.stream().collect(
                Collectors.groupingBy(
                        this::groupKey,
                        Collectors.collectingAndThen(Collectors.toList(), this::aggregateToUpdateTemp)
                )
        );

        List<Aggregated> aggregated = new ArrayList<>();


        /*
         *  -- Insert partly empty results for all existing combinations of date,
         *   -- node ('relay' or 'bridge'), country, transport, and version.  Only
         *   -- the rrx and nrx fields will contain number and seconds of reported
         *   -- responses for the given combination of date, node, etc., while the
         *   -- other fields will be updated below.
         *   INSERT INTO aggregated (date, node, country, transport, version, rrx,
         *       nrx)
         *     SELECT date, node, country, transport, version, SUM(val) AS rrx,
         *     SUM(seconds) AS nrx
         *     FROM update_temp_name WHERE metric = 'responses'
         *     GROUP BY date, node, country, transport, version;
         */
        Collection<Aggregated> responses = updateTemp.values().stream()
                .filter(ut -> ut.getMetric() == Metric.RESPONSES)
                .collect(
                        Collectors.groupingBy(UpdateTemp::key,
                                Collectors.collectingAndThen(Collectors.toList(), this::aggregateToAggregated)
                        )
                ).values();
        aggregated.addAll(responses);

        /*
         *   -- Create another temporary table with only those entries that aren't
         *   -- broken down by any dimension.  This table is much smaller, so the
         *   -- following operations are much faster.
         *   CREATE TEMPORARY TABLE update_no_dimensions_temp_name AS
         *     SELECT fingerprint, nickname, node, metric, date, val, seconds FROM update_temp_name
         *     WHERE country = ''
         *     AND transport = ''
         *     AND version = '';
         */
        List<UpdateTemp> noDimension = updateTemp.values().stream()
                .filter(ut -> ut.getCountry() == null)
                .collect(Collectors.toList());

        /*
         *   -- Update results in the aggregated table by setting aggregates based
         *   -- on reported directory bytes.  These aggregates are only based on
         *   -- date and node, so that the same values are set for all combinations
         *   -- of country, transport, and version.
         *   UPDATE aggregated
         *     SET hh = aggregated_bytes.hh, nh = aggregated_bytes.nh
         *     FROM (
         *       SELECT date, node, SUM(val) AS hh, SUM(seconds) AS nh
         *       FROM update_no_dimensions_temp_name
         *       WHERE metric = 'bytes'
         *       GROUP BY date, node
         *     ) aggregated_bytes
         *     WHERE aggregated.date = aggregated_bytes.date
         *     AND aggregated.node = aggregated_bytes.node;
         */
        Map<LocalDate, Aggregated> bytes = noDimension.stream()
                .filter(nd -> nd.getMetric() == Metric.BYTES)
                .collect(Collectors.groupingBy(
                                UpdateTemp::getDate,
                                Collectors.collectingAndThen(Collectors.toList(), this::aggregateToBytes)
                        )
                );
        aggregated.forEach(a -> {
            Aggregated b = bytes.get(a.getDate());
            if (b != null) {
                a.setHh(b.getHh());
                a.setNh(b.getNh());
            }
        });

        /*
         *   -- Update results based on nodes being contained in the network status.
         *   UPDATE aggregated
         *     SET nn = aggregated_status.nn
         *     FROM (
         *       SELECT date, node, SUM(seconds) AS nn
         *       FROM update_no_dimensions_temp_name
         *       WHERE metric = 'status'
         *       GROUP BY date, node
         *     ) aggregated_status
         *     WHERE aggregated.date = aggregated_status.date
         *     AND aggregated.node = aggregated_status.node;
         */
        Map<LocalDate, Double> status = noDimension.stream()
                .filter(nd -> nd.getMetric() == Metric.STATUS)
                .collect(Collectors.groupingBy(UpdateTemp::getDate, Collectors.summingDouble(UpdateTemp::getSeconds)));
        aggregated.forEach(a -> {
            Double aDouble = status.get(a.getDate());
            if (aDouble != null) {
                a.setNn(aDouble);
            }
        });

        /*
         *
         */
        Map<LocalDate, Double> hrh = noDimension.stream()
                .collect(Collectors.groupingBy(UpdateTemp::anotherKey))
                .entrySet().stream()
                .map(entry -> filterBoth(entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Aggregated::getDate, Collectors.summingDouble(Aggregated::getHrh)));
        aggregated.forEach(a -> {
            Double aDouble = hrh.get(a.getDate());
            if (aDouble != null) {
                a.setHrh(aDouble);
            }
        });

        /**
         *   -- Update results based on nodes reporting responses but no bytes.
         *   UPDATE aggregated
         *     SET nrh = aggregated_responses_bytes.nrh
         *     FROM (
         *       SELECT responses.date, responses.node,
         *              SUM(GREATEST(0, responses.seconds
         *                              - COALESCE(bytes.seconds, 0))) AS nrh
         *       FROM update_no_dimensions_temp_name responses
         *       LEFT JOIN update_no_dimensions_temp_name bytes
         *       ON responses.date = bytes.date
         *       AND responses.fingerprint = bytes.fingerprint
         *       AND responses.nickname = bytes.nickname
         *       AND responses.node = bytes.node
         *       WHERE responses.metric = 'responses'
         *       AND bytes.metric = 'bytes'
         *       GROUP BY responses.date, responses.node
         *     ) aggregated_responses_bytes
         *     WHERE aggregated.date = aggregated_responses_bytes.date
         *     AND aggregated.node = aggregated_responses_bytes.node;
         */
        Map<LocalDate, Double> nrh = noDimension.stream()
                .collect(Collectors.groupingBy(UpdateTemp::anotherKey))
                .entrySet().stream()
                .map(entry -> filterBothLeft(entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Aggregated::getDate, Collectors.summingDouble(Aggregated::getNrh)));

        aggregated.forEach(a -> {
            Double aDouble = nrh.get(a.getDate());
            if (aDouble != null) {
                a.setNrh(aDouble);
            }
        });

        return aggregated;
    }

    private String groupKey(Merged m) {
        return m.getFingerprint() + "-" + m.getNickname() + "-" + m.getMetric() + "-"
                + m.getCountry() + "-" + epochToLocalDate(m.getStatsStart());
    }

    // Helper function to check if dates are the same based on `long` timestamps
    private boolean sameDate(long startTime1, long startTime2) {
        return epochToLocalDate(startTime1).equals(epochToLocalDate(startTime2));
    }

    // Helper function to convert epoch timestamp to LocalDate for date comparisons
    private LocalDate epochToLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    // Helper function to aggregate a list of `Merged` entries into an `UpdateTemp`
    private UpdateTemp aggregateToUpdateTemp(List<Merged> list) {
        Merged first = list.get(0);
        double totalSeconds = list.stream().mapToDouble(m -> (m.getStatsEnd() - m.getStatsStart()) / 1000.0).sum();
        double totalVal = list.stream().mapToDouble(Merged::getVal).sum();

        return new UpdateTemp(first.getFingerprint(), first.getNickname(), first.getMetric(), first.getCountry(),
                epochToLocalDate(first.getStatsStart()), totalVal, totalSeconds);
    }

    private Aggregated aggregateToAggregated(List<UpdateTemp> list) {
        UpdateTemp first = list.get(0);
        //  -- Total number of reported responses, possibly broken down by country,
        //  -- transport, or version if either of them is not ''.  See r(R) in the
        //  -- tech report.
        double rrx = list.stream().mapToDouble(UpdateTemp::getVal).sum();
        //  -- Total number of seconds of nodes reporting responses, possibly broken
        //  -- down by country, transport, or version if either of them is not ''.
        //  -- This would be referred to as n(R) in the tech report, though it's not
        //  -- used there.
        double nrx = list.stream().mapToDouble(UpdateTemp::getSeconds).sum();

        return new Aggregated(first.getDate(), first.getCountry(), rrx, nrx, 0, 0, 0, 0, 0);
    }

    private Aggregated aggregateToBytes(List<UpdateTemp> list) {
        UpdateTemp first = list.get(0);
        // -- Total number of reported bytes.  See h(H) in the tech report.
        double hh = list.stream().mapToDouble(UpdateTemp::getVal).sum();
        // -- Number of seconds of nodes reporting bytes.  See n(H) in the tech report.
        double nh = list.stream().mapToDouble(UpdateTemp::getSeconds).sum();

        return new Aggregated(first.getDate(), first.getCountry(), 0, 0, hh, 0, 0, nh, 0);
    }

    private Aggregated filterBoth(List<UpdateTemp> list) {
        UpdateTemp ut = list.get(0);
        Optional<UpdateTemp> bytes = list.stream().filter(it -> it.getMetric() == Metric.BYTES && it.getSeconds() > 0).findFirst();
        Optional<UpdateTemp> responses = list.stream().filter(it -> it.getMetric() == Metric.RESPONSES).findFirst();

        if (bytes.isPresent() && responses.isPresent()) {
            UpdateTemp b = bytes.get();
            UpdateTemp r = responses.get();

            double hrh = (Math.min(b.getSeconds(), r.getSeconds()) * b.getVal()) / b.getSeconds();

            return new Aggregated(ut.getDate(), ut.getCountry(),
            0, 0, 0, 0, hrh, 0, 0);
        } else {
            return null;
        }

    }

    private Aggregated filterBothLeft(List<UpdateTemp> list) {
        Optional<UpdateTemp> bytes = list.stream().filter(it -> it.getMetric() == Metric.BYTES).findFirst();
        Optional<UpdateTemp> responses = list.stream().filter(it -> it.getMetric() == Metric.RESPONSES).findFirst();

        if (responses.isPresent()) {
            UpdateTemp r = responses.get();
            double respSec = r.getSeconds();
            double bytesSec = bytes.map(UpdateTemp::getSeconds).orElse(0.0);
            double nrh = Math.max(0, respSec - bytesSec);
            return new Aggregated(r.getDate(), r.getCountry(), 0, 0, 0, 0, 0, 0, nrh);
        } else {
            return null;
        }
    }

}
