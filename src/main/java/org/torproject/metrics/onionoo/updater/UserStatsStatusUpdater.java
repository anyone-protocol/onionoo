package org.torproject.metrics.onionoo.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torproject.descriptor.*;
import org.torproject.metrics.onionoo.docs.DocumentStore;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.UserStatsStatus;
import org.torproject.metrics.onionoo.docs.userstats.MergedStatus;
import org.torproject.metrics.onionoo.userstats.*;

import java.util.*;

public class UserStatsStatusUpdater implements DescriptorListener, StatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(UserStatsStatusUpdater.class);

    private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L;
    private static final long ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS;
    private static final long ONE_WEEK_MILLIS = 7L * ONE_DAY_MILLIS;

    private final DescriptorSource descriptorSource;
    private final DocumentStore documentStore;

    private final DataProcessor dataProcessor = new DataProcessor();

    private final Set<Imported> imported = new HashSet<>();

    public UserStatsStatusUpdater() {
        this.descriptorSource = DescriptorSourceFactory.getDescriptorSource();
        this.documentStore = DocumentStoreFactory.getDocumentStore();
        this.registerDescriptorListeners();
    }

    private void registerDescriptorListeners() {
        this.descriptorSource.registerDescriptorListener(this, DescriptorType.RELAY_CONSENSUSES);
        this.descriptorSource.registerDescriptorListener(this, DescriptorType.RELAY_EXTRA_INFOS);
    }

    @Override
    public void processDescriptor(Descriptor descriptor, boolean relay) {
        if (descriptor instanceof ExtraInfoDescriptor) {
            parseRelayExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
        } else if (descriptor instanceof RelayNetworkStatusConsensus) {
            parseRelayNetworkStatusConsensus((RelayNetworkStatusConsensus) descriptor);
        }
    }

    private void parseRelayExtraInfoDescriptor(ExtraInfoDescriptor descriptor) {
        long publishedMillis = descriptor.getPublishedMillis();
        String fingerprint = descriptor.getFingerprint().toUpperCase();
        String nickname = descriptor.getNickname();
        long dirreqStatsEndMillis = descriptor.getDirreqStatsEndMillis();
        long dirreqStatsIntervalLengthMillis = descriptor.getDirreqStatsIntervalLength() * 1000L;
        SortedMap<String, Integer> responses = descriptor.getDirreqV3Resp();
        SortedMap<String, Integer> requests = descriptor.getDirreqV3Reqs();
        BandwidthHistory dirreqWriteHistory = descriptor.getDirreqWriteHistory();
        parseRelayDirreqV3Resp(fingerprint, nickname, publishedMillis, dirreqStatsEndMillis, dirreqStatsIntervalLengthMillis, responses, requests);
        parseRelayDirreqWriteHistory(fingerprint, nickname, publishedMillis, dirreqWriteHistory);
    }

    private void parseRelayDirreqWriteHistory(String fingerprint,
                                              String nickname,
                                              long publishedMillis,
                                              BandwidthHistory dirreqWriteHistory) {
        if (dirreqWriteHistory == null
                || publishedMillis - dirreqWriteHistory.getHistoryEndMillis()
                > ONE_WEEK_MILLIS) {
            return;
            /* Cut off all observations that are one week older than
             * the descriptor publication time, or we'll have to update
             * weeks of aggregate values every hour. */
        }
        long intervalLengthMillis =
                dirreqWriteHistory.getIntervalLength() * 1000L;
        for (Map.Entry<Long, Long> e
                : dirreqWriteHistory.getBandwidthValues().entrySet()) {
            long intervalEndMillis = e.getKey();
            long intervalStartMillis =
                    intervalEndMillis - intervalLengthMillis;
            for (int i = 0; i < 2; i++) {
                long fromMillis = intervalStartMillis;
                long toMillis = intervalEndMillis;
                double writtenBytes = (double) e.getValue();
                if (intervalStartMillis / ONE_DAY_MILLIS
                        < intervalEndMillis / ONE_DAY_MILLIS) {
                    long utcBreakMillis = (intervalEndMillis
                            / ONE_DAY_MILLIS) * ONE_DAY_MILLIS;
                    if (i == 0) {
                        toMillis = utcBreakMillis;
                    } else {
                        fromMillis = utcBreakMillis;
                    }
                    double intervalFraction = ((double) (toMillis - fromMillis))
                            / ((double) intervalLengthMillis);
                    writtenBytes *= intervalFraction;
                } else if (i == 1) {
                    break;
                }
                insertIntoImported(fingerprint, nickname,  Metric.BYTES, null, fromMillis, toMillis, writtenBytes);
            }
        }
    }

    // todo - reviewed
    private void parseRelayDirreqV3Resp(String fingerprint,
                                        String nickname,
                                        long publishedMillis,
                                        long dirreqStatsEndMillis,
                                        long dirreqStatsIntervalLengthMillis,
                                        SortedMap<String, Integer> responses,
                                        SortedMap<String, Integer> requests) {
        if (responses == null
                || publishedMillis - dirreqStatsEndMillis > ONE_WEEK_MILLIS
                || dirreqStatsIntervalLengthMillis != ONE_DAY_MILLIS) {
            /* Cut off all observations that are one week older than
             * the descriptor publication time, or we'll have to update
             * weeks of aggregate values every hour. */
            return;
        }
        long statsStartMillis = dirreqStatsEndMillis
                - dirreqStatsIntervalLengthMillis;
        long utcBreakMillis = (dirreqStatsEndMillis / ONE_DAY_MILLIS)
                * ONE_DAY_MILLIS;
        double resp = ((double) responses.get("ok")) - 4.0;
        if (resp > 0.0) {
            for (int i = 0; i < 2; i++) {
                long fromMillis = i == 0 ? statsStartMillis : utcBreakMillis;
                long toMillis = i == 0 ? utcBreakMillis : dirreqStatsEndMillis;
                if (fromMillis >= toMillis) {
                    continue;
                }
                double intervalFraction = ((double) (toMillis - fromMillis))
                        / ((double) dirreqStatsIntervalLengthMillis);
                double total = 0L;
                SortedMap<String, Double> requestsCopy = new TreeMap<>();
                if (null != requests) {
                    for (Map.Entry<String, Integer> e : requests.entrySet()) {
                        if (e.getValue() < 4.0) {
                            continue;
                        }
                        double frequency = ((double) e.getValue()) - 4.0;
                        requestsCopy.put(e.getKey(), frequency);
                        total += frequency;
                    }
                }
                /* If we're not told any requests, or at least none of them are greater
                 * than 4, put in a default that we'll attribute all responses to. */
                if (requestsCopy.isEmpty()) {
                    requestsCopy.put("??", 4.0);
                    total = 4.0;
                }
                for (Map.Entry<String, Double> e : requestsCopy.entrySet()) {
                    String country = e.getKey();
                    double val = resp * intervalFraction * e.getValue() / total;
                    insertIntoImported(fingerprint, nickname, Metric.RESPONSES, country, fromMillis, toMillis, val);
                }
                insertIntoImported(fingerprint, nickname, Metric.RESPONSES, null, fromMillis, toMillis, resp * intervalFraction);
            }
        }
    }

    private void parseRelayNetworkStatusConsensus(RelayNetworkStatusConsensus consensus) {
        long fromMillis = consensus.getValidAfterMillis();
        long toMillis = consensus.getFreshUntilMillis();
        for (NetworkStatusEntry statusEntry : consensus.getStatusEntries().values()) {
            String fingerprint = statusEntry.getFingerprint().toUpperCase();
            String nickname = statusEntry.getNickname();
            if (statusEntry.getFlags().contains("Running")) {
                insertIntoImported(fingerprint, nickname, Metric.STATUS, null, fromMillis, toMillis, 0.0);
            }
        }
    }

    void insertIntoImported(String fingerprint,
                            String nickname,
                            Metric metric,
                            String country,
                            long fromMillis,
                            long toMillis,
                            double val) {
        if (fromMillis > toMillis) {
            return;
        }
        imported.add(
                new Imported(fingerprint, nickname, metric, country, fromMillis, toMillis, Math.round(val * 10.0) / 10.0)
        );
    }

    @Override
    public void updateStatuses() {
        logger.info("Imported size: {}", imported.size());
        MergedStatus statusOld = documentStore.retrieve(MergedStatus.class, true);

        if (statusOld != null) {
            logger.info("Old merged size: {}", statusOld.getMerged().size());
        } else {
            logger.info("Old merged size: 0");
        }

        List<Merged> merged = statusOld == null ? new ArrayList<>() : statusOld.getMerged();


        List<Merged> merge = dataProcessor.merge(merged, new ArrayList<>(imported));

        logger.info("New merged size: {}", merge.size());

        if (!merge.isEmpty()) {
            logger.info("New merged is not empty, persist to disk");
            MergedStatus newMerge = new MergedStatus(merge);
            documentStore.store(newMerge);
        }

        logger.info("Start aggregation based on merged data");
        List<Aggregated> aggregate = dataProcessor.aggregate(merge);
        logger.info("Aggregated size: {}", aggregate.size());
        List<Estimated> estimated = dataProcessor.estimate(aggregate);
        logger.info("Estimated size: {}", estimated.size());
        this.documentStore.store(new UserStatsStatus(estimated));
        imported.clear();
    }

    @Override
    public String getStatsString() {
        return null;
    }
}
