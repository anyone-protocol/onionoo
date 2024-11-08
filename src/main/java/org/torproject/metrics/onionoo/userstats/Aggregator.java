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

        List<Aggregated> aggregatedList = new ArrayList<>();

        Collection<Aggregated> first = updateTemp.values().stream()
                .filter(ut -> ut.getMetric() == Metric.RESPONSES)
                .collect(
                        Collectors.groupingBy(UpdateTemp::key,
                                Collectors.collectingAndThen(Collectors.toList(), this::aggregateToAggregated)
                        )
                ).values();

        aggregatedList.addAll(first);

        List<UpdateTemp> noDimension = updateTemp.values().stream()
                .filter(ut -> ut.getCountry() == null && ut.getTransport() == null && ut.getVersion() == null)
                .collect(Collectors.toList());

        Map<LocalDate, Aggregated> collect = noDimension.stream()
                .filter(nd -> nd.getMetric() == Metric.BYTES)
                .collect(Collectors.groupingBy(
                                UpdateTemp::getDate,
                                Collectors.collectingAndThen(Collectors.toList(), this::aggregateToBytes)
                        )
                );

        aggregatedList.forEach(a -> {
            Aggregated bytes = collect.get(a.getDate());
            if (bytes != null) {
                a.setHh(bytes.getHh());
                a.setNh(bytes.getNh());
            }
        });

        Map<LocalDate, Double> status = noDimension.stream()
                .filter(nd -> nd.getMetric() == Metric.STATUS)
                .collect(Collectors.groupingBy(UpdateTemp::getDate, Collectors.summingDouble(UpdateTemp::getSeconds)));

        aggregatedList.forEach(a -> {
            Double aDouble = status.get(a.getDate());
            if (aDouble != null) {
                a.setNn(aDouble);
            }
        });

        Map<LocalDate, Double> hrh = noDimension.stream()
                .collect(Collectors.groupingBy(UpdateTemp::anotherKey))
                .entrySet().stream()
                .map(entry -> filterBoth(entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Aggregated::getDate, Collectors.summingDouble(Aggregated::getHrh)));

        aggregatedList.forEach(a -> {
            Double aDouble = hrh.get(a.getDate());
            if (aDouble != null) {
                a.setHrh(aDouble);
            }
        });

        Map<LocalDate, Double> nrh = noDimension.stream()
                .collect(Collectors.groupingBy(UpdateTemp::anotherKey))
                .entrySet().stream()
                .map(entry -> filterBothLeft(entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Aggregated::getDate, Collectors.summingDouble(Aggregated::getNrh)));

        aggregatedList.forEach(a -> {
            Double aDouble = nrh.get(a.getDate());
            if (aDouble != null) {
                a.setNrh(aDouble);
            }
        });

        return aggregatedList;
    }

    private String groupKey(Merged m) {
        return m.getFingerprint() + "-" + m.getNickname() + "-" + m.getMetric() + "-"
                + m.getCountry() + "-" + m.getTransport() + "-" + m.getVersion() + "-" + epochToLocalDate(m.getStatsStart());
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
        long totalSeconds = list.stream().mapToLong(m -> (m.getStatsEnd() - m.getStatsStart()) / 1000).sum();
        double totalVal = list.stream().mapToDouble(Merged::getVal).sum();

        return new UpdateTemp(first.getFingerprint(), first.getNickname(), first.getMetric(),
                first.getCountry(), first.getTransport(), first.getVersion(),
                epochToLocalDate(first.getStatsStart()), totalVal, totalSeconds);
    }

    private Aggregated aggregateToAggregated(List<UpdateTemp> list) {
        UpdateTemp first = list.get(0);
        double rrx = list.stream().mapToDouble(UpdateTemp::getVal).sum();
        long nrx = list.stream().mapToLong(UpdateTemp::getSeconds).sum();

        return new Aggregated(first.getDate(), first.getCountry(), first.getTransport(),
                first.getVersion(), rrx, nrx, 0, 0, 0, 0, 0);
    }

    private Aggregated aggregateToBytes(List<UpdateTemp> list) {
        UpdateTemp first = list.get(0);
        double hh = list.stream().mapToDouble(UpdateTemp::getVal).sum();
        long nh = list.stream().mapToLong(UpdateTemp::getSeconds).sum();

        return new Aggregated(first.getDate(), first.getCountry(), first.getTransport(),
                first.getVersion(), 0, 0, hh, 0, 0, nh, 0);
    }

    private Aggregated filterBoth(List<UpdateTemp> list) {
        UpdateTemp ut = list.get(0);
        Optional<UpdateTemp> bytes = list.stream().filter(it -> it.getMetric() == Metric.BYTES && it.getSeconds() > 0).findFirst();
        Optional<UpdateTemp> responses = list.stream().filter(it -> it.getMetric() == Metric.RESPONSES).findFirst();

        if (bytes.isPresent() && responses.isPresent()) {
            UpdateTemp b = bytes.get();
            UpdateTemp r = responses.get();

            double hrh = (Math.min(b.getSeconds(), r.getSeconds()) * b.getVal()) / b.getSeconds();

            return new Aggregated(ut.getDate(), ut.getCountry(), ut.getTransport(), ut.getVersion(),
            0, 0, 0, 0, hrh, 0, 0);
        } else {
            return null;
        }

    }

    private Aggregated filterBothLeft(List<UpdateTemp> list) {
        Optional<UpdateTemp> bytes = list.stream().filter(it -> it.getMetric() == Metric.BYTES).findFirst();
        Optional<UpdateTemp> responses = list.stream().filter(it -> it.getMetric() == Metric.RESPONSES).findFirst();

        if (responses.isPresent() && bytes.isPresent() && bytes.get().getSeconds() > 0) {
            return null;
        } else {
            if (responses.isPresent()) {
                UpdateTemp r = responses.get();
                long bytesSeconds = 0;
                double nrh = Math.max(0, r.getSeconds() - bytesSeconds);
                return new Aggregated(r.getDate(), r.getCountry(), r.getTransport(), r.getVersion(),
                        0, 0, 0, 0, 0, 0, nrh);
            } else {
                return null;
            }
        }

    }


}
