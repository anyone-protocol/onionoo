package org.torproject.metrics.onionoo.userstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessor.class);

    private static final long DEFAULT = -1L; // todo - read for merged

    public List<Merged> merge(List<Merged> mergedList, List<Imported> importedList) {
        long undoEnd = DEFAULT;
        double undoVal = DEFAULT;

        mergedList.sort(Comparator.comparing(Merged::getFingerprint)
                .thenComparing(Merged::getNickname)
                .thenComparing(Merged::getMetric)
                .thenComparing(Merged::getCountry, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Merged::getStatsStart)
                .thenComparing(Merged::getStatsEnd));
        importedList.sort(Comparator.comparing(Imported::getFingerprint)
                .thenComparing(Imported::getNickname)
                .thenComparing(Imported::getMetric)
                .thenComparing(Imported::getCountry, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Imported::getStatsStart)
                .thenComparing(Imported::getStatsEnd));

        Map<Long, Merged> mergedMap = mergedList.stream()
                .collect(Collectors.toMap(Merged::getId, m -> m));

        long idCounter = mergedMap.isEmpty() ? 1 : mergedMap.keySet().stream().max(Long::compareTo).get() + 1;

        LocalDate weekAgo = toLocalDate(Instant.now().toEpochMilli()).minusDays(7);

        Set<LocalDate> distinctDates = importedList.stream()
                .map(imported -> toLocalDate(imported.getStatsStart()))
                .filter(date -> date.isAfter(weekAgo))
                .collect(Collectors.toSet());

        importedList = importedList.stream()
                .filter(imported -> toLocalDate(imported.getStatsStart()).isAfter(weekAgo))
                .collect(Collectors.toList());

        Map<String, List<Merged>> mergedGroup = mergedList.stream()
                .filter(merged -> toLocalDate(merged.getStatsStart()).isAfter(weekAgo))
                .filter(merged -> distinctDates.contains(toLocalDate(merged.getStatsStart())))
                .collect(Collectors.groupingBy(m ->
                        String.join("-", m.getFingerprint(), m.getNickname(), m.getMetric().name(), m.getCountry()))
                );

        //    -- Select these columns from all entries in the imported table, plus
        //    -- do an outer join on the merged_part table to find adjacent entries
        //    -- that we might want to merge the new entries with.  It's possible
        //    -- that we handle the same imported entry twice, if it starts directly
        //    -- after one existing entry and ends directly before another existing
        //    -- entry.
        //    -- The table name is now substituted via the variable merged_part_name.
        //    FROM imported LEFT JOIN merged_part_name
        //
        //    -- First two join conditions are to find adjacent intervals.  In fact,
        //    -- we also include overlapping intervals here, so that we can skip the
        //    -- overlapping entry in the imported table.
        for (Imported imported : importedList) {
            CurrentMergeDto cur = new CurrentMergeDto();
            //       imported.fingerprint = merged_part_name.fingerprint AND
            //       imported.nickname = merged_part_name.nickname AND
            //       imported.metric = merged_part_name.metric AND
            //       imported.country = merged_part_name.country AND
            List<Merged> mergeds = mergedGroup.get(imported.getKey());
            Merged merged = findAdjacentOrOverlapping(mergeds, imported);
            //    -- Select id, interval start and end, and value of the existing entry
            //    -- in merged_part; all these fields may be null if the imported entry
            //    -- is not adjacent to an existing one.
            //    merged_part_name.id AS merged_id,
            //    merged_part_name.stats_start AS merged_start,
            //    merged_part_name.stats_end AS merged_end,
            //    merged_part_name.val AS merged_val,
            long mergedId = merged != null ? merged.getId() : DEFAULT;
            long mergedStart = merged != null ? merged.getStatsStart() : DEFAULT;
            long mergedEnd = merged != null ? merged.getStatsEnd() : DEFAULT;
            double mergedVal = merged != null ? merged.getVal() : DEFAULT;
            //    -- Select interval start and end and value of the newly imported entry.
            //    imported.stats_start AS imported_start,
            //    imported.stats_end AS imported_end,
            //    imported.val AS imported_val,
            long importedStart = imported.getStatsStart();
            long importedEnd = imported.getStatsEnd();
            double importedVal = imported.getVal();
            //    -- Select columns that define the group of entries that can be merged
            //    -- in the merged table.
            //    imported.fingerprint AS fingerprint,
            //    imported.nickname AS nickname,
            //    imported.metric AS metric,
            //    imported.country AS country,
            String fingerprint = imported.getFingerprint();
            String nickname = imported.getNickname();
            Metric metric = imported.getMetric();
            String country = imported.getCountry();
            // -- If we're processing the very first entry or if we have reached a
            //    -- new group of entries that belong together, (re-)set last_*
            //    -- variables.
            //    IF last_fingerprint IS NULL OR
            //        DATE(cur.imported_start) <> DATE(last_start) OR
            //        cur.fingerprint <> last_fingerprint OR
            //        cur.nickname <> last_nickname OR
            //        cur.metric <> last_metric OR
            //        cur.country <> last_country OR
            //      last_id := -1;
            //      last_start := '1970-01-01 00:00:00';
            //      last_end := '1970-01-01 00:00:00';
            //      last_val := -1;
            //    END IF;
            if (!sameDate(importedStart, cur.lastStart) ||
                    !fingerprint.equals(cur.lastFingerprint) || !nickname.equals(cur.lastNickname) ||
                    !metric.equals(cur.lastMetric) || (country != null && !country.equals(cur.lastCountry))) {
                cur.lastId = DEFAULT;
                cur.lastStart = DEFAULT;
                cur.lastEnd = DEFAULT;
                cur.lastVal = DEFAULT;
            }
            //    -- Remember all fields that determine the group of which entries
            //    -- belong together.
            //    last_fingerprint := cur.fingerprint;
            //    last_nickname := cur.nickname;
            //    last_metric := cur.metric;
            //    last_country := cur.country;
            cur.lastFingerprint = fingerprint;
            cur.lastNickname = nickname;
            cur.lastMetric = metric;
            cur.lastCountry = country;
            //    -- If the existing entry that we're currently looking at starts before
            //    -- the previous entry ends, we have created two overlapping entries in
            //    -- the last iteration, and that is not allowed.  Undo the previous
            //    -- change.
            //    IF cur.merged_start IS NOT NULL AND
            //        cur.merged_start < last_end AND
            //        undo_end IS NOT NULL AND undo_val IS NOT NULL THEN
            //      UPDATE merged SET stats_end = undo_end, val = undo_val
            //        WHERE id = last_id;
            //      undo_end := NULL;
            //      undo_val := NULL;
            if (mergedStart != DEFAULT && mergedStart < cur.lastEnd && undoEnd != DEFAULT && undoVal != DEFAULT) {
                Merged m = mergedMap.get(cur.lastId);
                m.setStatsEnd(undoEnd);
                m.setVal(undoVal);
                undoEnd = DEFAULT;
                undoVal = DEFAULT;
            //    -- If there is no adjacent entry to the one we're about to merge,
            //    -- insert it as new entry.
            //    ELSIF cur.merged_end IS NULL THEN
            //      IF cur.imported_start > last_end THEN
            //        last_start := cur.imported_start;
            //        last_end := cur.imported_end;
            //        last_val := cur.imported_val;
            //        INSERT INTO merged (fingerprint, nickname, metric, country,
            //                            stats_start, stats_end, val)
            //          VALUES (last_fingerprint, last_nickname, last_metric,
            //                  last_country, last_start, last_end, last_val)
            //          RETURNING id INTO last_id;
            } else if (mergedEnd == DEFAULT) {
                if (importedStart > cur.lastEnd) {
                    cur.lastStart = importedStart;
                    cur.lastEnd = importedEnd;
                    cur.lastVal = importedVal;
                    cur.lastId = idCounter;
                    mergedMap.put(cur.lastId, new Merged(idCounter++, cur.lastFingerprint, cur.lastNickname, cur.lastMetric, cur.lastCountry, cur.lastStart, cur.lastEnd, cur.lastVal));
            //      -- If there was no adjacent entry before starting to merge, but
            //      -- there is now one ending right before the new entry starts, merge
            //      -- the new entry into the existing one.
            //      ELSIF cur.imported_start = last_end THEN
            //        last_val := last_val + cur.imported_val;
            //        last_end := cur.imported_end;
            //        UPDATE merged SET stats_end = last_end, val = last_val
            //          WHERE id = last_id;
            //      END IF;
                } else if (importedStart == cur.lastEnd) {
                    cur.lastVal = cur.lastVal + importedVal;
                    cur.lastEnd = importedEnd;
                    Merged m = mergedMap.get(cur.lastId);
                    m.setStatsEnd(cur.lastEnd);
                    m.setVal(cur.lastVal);
                }
            //      -- There's no risk of this entry overlapping with the next.
            //      undo_end := NULL;
            //      undo_val := NULL;
                undoEnd = DEFAULT;
                undoVal = DEFAULT;
            //    -- If the new entry ends right when an existing entry starts, but
            //    -- there's a gap between when the previously processed entry ends and
            //    -- when the new entry starts, merge the new entry with the existing
            //    -- entry we're currently looking at.
            //    ELSIF cur.imported_end = cur.merged_start THEN
            //      IF cur.imported_start > last_end THEN
            //        last_id := cur.merged_id;
            //        last_start := cur.imported_start;
            //        last_end := cur.merged_end;
            //        last_val := cur.imported_val + cur.merged_val;
            //        UPDATE merged SET stats_start = last_start, val = last_val
            //          WHERE id = last_id;
            } else if (importedEnd == mergedStart) {
                if (importedStart > cur.lastEnd) {
                    cur.lastId = merged.getId();
                    cur.lastStart = importedStart;
                    cur.lastEnd = importedEnd;
                    cur.lastVal = importedVal + mergedVal;
                    Merged m = mergedMap.get(cur.lastId);
                    m.setStatsStart(cur.lastStart);
                    m.setVal(cur.lastVal);
            //      -- If the new entry ends right when an existing entry starts and
            //      -- there's no gap between when the previously processed entry ends
            //      -- and when the new entry starts, merge the new entry with the other
            //      -- two entries.  This happens by deleting the previous entry and
            //      -- expanding the subsequent entry to cover all three entries.
            //      ELSIF cur.imported_start = last_end THEN
            //        DELETE FROM merged WHERE id = last_id;
            //        last_id := cur.merged_id;
            //        last_end := cur.merged_end;
            //        last_val := last_val + cur.merged_val;
            //        UPDATE merged SET stats_start = last_start, val = last_val
            //          WHERE id = last_id;
            //      END IF;
                } else if (importedStart == cur.lastEnd) {
                    if (cur.lastId != mergedId) {
                        mergedMap.remove(cur.lastId);
                    }
                    cur.lastId = mergedId;
                    cur.lastEnd = mergedEnd;
                    cur.lastVal = importedVal + mergedVal;
                    Merged m = mergedMap.get(cur.lastId);
                    m.setStatsStart(cur.lastStart);
                    m.setVal(cur.lastVal);
                }
            //      -- There's no risk of this entry overlapping with the next.
            //      undo_end := NULL;
            //      undo_val := NULL;
                undoEnd = DEFAULT;
                undoVal = DEFAULT;
            //    -- If the new entry starts right when an existing entry ends, but
            //    -- there's a gap between the previously processed entry and the
            //    -- existing one, extend the existing entry.  There's a special case
            //    -- when this operation is false and must be undone, which is when the
            //    -- newly added entry overlaps with the subsequent entry.  That's why
            //    -- we have to store the old interval end and value, so that this
            //    -- operation can be undone in the next loop iteration.
            //    ELSIF cur.imported_start = cur.merged_end THEN
            //      IF last_end < cur.imported_start THEN
            //        undo_end := cur.merged_end;
            //        undo_val := cur.merged_val;
            //        last_id := cur.merged_id;
            //        last_start := cur.merged_start;
            //        last_end := cur.imported_end;
            //        last_val := cur.merged_val + cur.imported_val;
            //        UPDATE merged SET stats_end = last_end, val = last_val
            //          WHERE id = last_id;
            } else if(importedStart == mergedEnd) {
                if (cur.lastEnd < importedStart) {
                    undoEnd = mergedEnd;
                    undoVal = mergedVal;
                    cur.lastId = mergedId;
                    cur.lastStart = mergedStart;
                    cur.lastEnd = importedEnd;
                    cur.lastVal = mergedVal + importedVal;
                    Merged m = mergedMap.get(cur.lastId);
                    m.setStatsEnd(cur.lastEnd);
                    m.setVal(cur.lastVal);
            //      -- If the new entry starts right when an existing entry ends and
            //      -- there's no gap between the previously processed entry and the
            //      -- existing entry, extend the existing entry.  This is very similar
            //      -- to the previous case.  The same reasoning about possibly having
            //      -- to undo this operation applies.
            //      ELSE
            //        undo_end := cur.merged_end;
            //        undo_val := last_val;
            //        last_end := cur.imported_end;
            //        last_val := last_val + cur.imported_val;
            //        UPDATE merged SET stats_end = last_end, val = last_val
            //          WHERE id = last_id;
            //      END IF;
                } else {
                    undoEnd = mergedEnd;
                    undoVal = cur.lastVal;
                    cur.lastEnd = importedEnd;
                    cur.lastVal = cur.lastVal + importedVal;
                    Merged m = mergedMap.get(cur.lastId);
                    m.setStatsEnd(cur.lastEnd);
                    m.setVal(cur.lastVal);
                }
            //    -- If none of the cases above applies, there must have been an overlap
            //    -- between the new entry and an existing one.  Skip the new entry.
            //    ELSE
            //      last_id := cur.merged_id;
            //      last_start := cur.merged_start;
            //      last_end := cur.merged_end;
            //      last_val := cur.merged_val;
            //      undo_end := NULL;
            //      undo_val := NULL;
            //    END IF;
            } else {
                cur.lastId = mergedId;
                cur.lastStart = mergedStart;
                cur.lastEnd = mergedEnd;
                cur.lastVal = mergedVal;
                undoEnd = DEFAULT;
                undoVal = DEFAULT;
            }
        }

        mergedGroup.clear();
        mergedList.clear();

        return new ArrayList<>(mergedMap.values());
    }

    private Merged findAdjacentOrOverlapping(List<Merged> mergedPart, Imported imported) {
        return mergedPart == null
                ? null
                : mergedPart.stream()
                    .filter(merged -> isAdjacentOrOverlapping(imported, merged))
                    .findFirst()
                    .orElse(null);
    }

    //    ON imported.stats_end >= merged_part_name.stats_start AND
    //       imported.stats_start <= merged_part_name.stats_end AND
    //       -- Further join conditions are same date, fingerprint, node, etc.,
    //       -- so that we don't merge entries that don't belong together.
    //       DATE(imported.stats_start) = DATE(merged_part_name.stats_start)
    private boolean isAdjacentOrOverlapping(Imported imported, Merged merged) {
        return imported.getStatsEnd() >= merged.getStatsStart() &&
                imported.getStatsStart() <= merged.getStatsEnd() &&
                sameDate(imported.getStatsStart(), merged.getStatsStart());
    }

    private boolean sameDate(long importedStartMillis, long mergedStartMillis) {
        return toLocalDate(importedStartMillis).equals(toLocalDate(mergedStartMillis));
    }

    private LocalDate toLocalDate(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    // aggregate

    public List<Aggregated> aggregate(List<Merged> merged) {
        //  -- Create a new temporary table containing all relevant information
        //  -- needed to update the aggregated table.  In this table, we sum up all
        //  -- observations of a given type by reporting node.  This query is
        //  -- (temporarily) materialized, because we need to combine its entries
        //  -- multiple times in various ways.  A (non-materialized) view would have
        //  -- meant to re-compute this query multiple times.
        Map<String, UpdateTemp> updateTemp = merged.stream().collect(
                Collectors.groupingBy(
                        this::groupKey,
                        Collectors.collectingAndThen(Collectors.toList(), this::aggregateToUpdateTemp)
                )
        );

        /*
         *  -- Insert partly empty results for all existing combinations of date and country.  Only
         *   -- the rrx and nrx fields will contain number and seconds of reported
         *   -- responses for the given combination of date, etc., while the
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
        List<Aggregated> aggregated = new ArrayList<>(responses);

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
         *  -- Update results based on nodes reporting both bytes and responses.
         *   UPDATE aggregated
         *     SET hrh = aggregated_bytes_responses.hrh
         *     FROM (
         *       SELECT bytes.date, bytes.node,
         *              SUM((LEAST(bytes.seconds, responses.seconds)
         *                  * bytes.val) / bytes.seconds) AS hrh
         *       FROM update_no_dimensions_temp_name bytes
         *       LEFT JOIN update_no_dimensions_temp_name responses
         *       ON bytes.date = responses.date
         *       AND bytes.fingerprint = responses.fingerprint
         *       AND bytes.nickname = responses.nickname
         *       AND bytes.node = responses.node
         *       WHERE bytes.metric = 'bytes'
         *       AND responses.metric = 'responses'
         *       AND bytes.seconds > 0
         *       GROUP BY bytes.date, bytes.node
         *     ) aggregated_bytes_responses
         *     WHERE aggregated.date = aggregated_bytes_responses.date
         *     AND aggregated.node = aggregated_bytes_responses.node;
         */
        Map<LocalDate, Double> hrh = noDimension.stream()
                .collect(Collectors.groupingBy(UpdateTemp::anotherKey))
                .entrySet().stream()
                .map(entry -> aggregateHrh(entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Aggregated::getDate, Collectors.summingDouble(Aggregated::getHrh)));

        aggregated.forEach(a -> {
            Double aDouble = hrh.get(a.getDate());
            if (aDouble != null) {
                a.setHrh(aDouble);
            }
        });

        /*
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
                .map(entry -> aggregateNrh(entry.getValue()))
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
        return String.join("-", m.getFingerprint(), m.getNickname(), m.getMetric().name(), m.getCountry(), toLocalDate(m.getStatsStart()).toString());
    }

    // done
    private UpdateTemp aggregateToUpdateTemp(List<Merged> list) {
        Merged first = list.get(0);
        double totalSeconds = list.stream().mapToDouble(m -> (m.getStatsEnd() - m.getStatsStart()) / 1000.0).sum();
        double totalVal = list.stream().mapToDouble(Merged::getVal).sum();

        return new UpdateTemp(first.getFingerprint(), first.getNickname(), first.getMetric(), first.getCountry(),
                toLocalDate(first.getStatsStart()), totalVal, totalSeconds);
    }

    // done
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

    // done
    private Aggregated aggregateToBytes(List<UpdateTemp> list) {
        UpdateTemp first = list.get(0);
        // -- Total number of reported bytes.  See h(H) in the tech report.
        double hh = list.stream().mapToDouble(UpdateTemp::getVal).sum();
        // -- Number of seconds of nodes reporting bytes.  See n(H) in the tech report.
        double nh = list.stream().mapToDouble(UpdateTemp::getSeconds).sum();

        return new Aggregated(first.getDate(), first.getCountry(), 0, 0, hh, 0, 0, nh, 0);
    }

    // done
    private Aggregated aggregateHrh(List<UpdateTemp> list) {
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

    // done
    private Aggregated aggregateNrh(List<UpdateTemp> list) {
        Optional<UpdateTemp> responses = list.stream().filter(it -> it.getMetric() == Metric.RESPONSES).findFirst();
        Optional<UpdateTemp> bytes = list.stream().filter(it -> it.getMetric() == Metric.BYTES).findFirst();

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

    // estimate

    // -- User-friendly view on the aggregated table that implements the
    // -- algorithm proposed in Tor Tech Report 2012-10-001.  This view returns
    // -- user number estimates for both relay and bridge staistics, possibly
    // -- broken down by country or transport or version.
    public List<Estimated> estimate(List<Aggregated> aggregatedList) {
        List<Estimated> estimatedList = new ArrayList<>();

        Instant now = Instant.now().minus(Duration.ofHours(3)); // shift to allow all the metrics come
        LocalDate target = toLocalDate(now.toEpochMilli());

        for (Aggregated agg : aggregatedList) {
            if (agg.getHh() * agg.getNn() > 0.0) {
                // Calculate the fraction for estimation
                double frac = (agg.getHrh() * agg.getNh() + agg.getHh() * agg.getNrh()) / (agg.getHh() * agg.getNn());

                // Only include entries where frac is between 0.1 and 1.1
//                if (frac >= 0.1 && frac <= 1.1) {
                    // Round fraction to an integer percent
                    int fracPercent = (int) Math.round(frac * 100);

                    // Estimate users based on rrx and fraction
                    int users = (int) Math.round(agg.getRrx() / (frac * 10));

                    // Only include estimates older than today
                    if (agg.getDate().isBefore(target)) {
                        Estimated estimated = new Estimated(
                                agg.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                agg.getCountry(),
                                fracPercent,
                                users
                        );
                        estimatedList.add(estimated);
                    }
//                }
            }
        }

        // Sort results by date, node and country (similar to SQL ORDER BY clause)
        estimatedList.sort(Comparator.comparing(Estimated::getDate)
                .thenComparing(Estimated::getCountry, Comparator.nullsLast(Comparator.naturalOrder())));

        return estimatedList;
    }

}
