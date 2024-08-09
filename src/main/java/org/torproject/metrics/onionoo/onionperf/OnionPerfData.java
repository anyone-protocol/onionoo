package org.torproject.metrics.onionoo.onionperf;

import java.sql.Timestamp;
import java.util.*;

public class OnionPerfData {

    private List<Measurement> measurements;

    public OnionPerfData() {
        measurements = new ArrayList<>();
    }

    public List<OnionperfIncludingPartialsStatistic> getIncludingPartialsList() {
        List<OnionperfIncludingPartialsStatistic> results = new ArrayList<>();

        // Extracting the required fields and processing them
        List<MeasurementIncludingPartials> includingPartialsList = new ArrayList<>();
        for (Measurement m : measurements) {
            includingPartialsList.add(new MeasurementIncludingPartials(
                    m.getStart(), m.getFilesize(), m.getSource(), m.getEndpointRemote(), m.getDataComplete()
            ));
            if (m.getFilesize() > 51200 && m.getPartial51200() != null) {
                includingPartialsList.add(new MeasurementIncludingPartials(
                        m.getStart(), 51200, m.getSource(), m.getEndpointRemote(), m.getPartial51200()
                ));
            }
            if (m.getFilesize() > 1048576 && m.getPartial1048576() != null) {
                includingPartialsList.add(new MeasurementIncludingPartials(
                        m.getStart(), 1048576, m.getSource(), m.getEndpointRemote(), m.getPartial1048576()
                ));
            }
        }

        // Group by date, filesize, source, server
        Map<String, List<MeasurementIncludingPartials>> grouped = new HashMap<>();
        for (MeasurementIncludingPartials mip : includingPartialsList) {
            String key = String.format("%s-%d-%s-%s",
                    new Date(mip.getStart().getTime()).toString(), mip.getFilesize(), mip.getSource(),
                    mip.getEndpointRemote().endsWith(".onion") ? "onion" : "public"
            );
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<MeasurementIncludingPartials>());
            }
            grouped.get(key).add(mip);
        }

        for (String key : grouped.keySet()) {
            List<MeasurementIncludingPartials> group = grouped.get(key);
            String[] parts = key.split("-");
            Date date = new Date(group.get(0).getStart().getTime());
            int filesize = group.get(0).getFilesize();
            String source = group.get(0).getSource();
            String server = parts[3];

            List<Integer> datacompletes = new ArrayList<>();
            for (MeasurementIncludingPartials mip : group) {
                datacompletes.add(mip.getDataComplete());
            }

            Double q1 = null, md = null, q3 = null;
            if (!datacompletes.isEmpty()) {
                q1 = Percentile.percentile(datacompletes, 0.25);
                md = Percentile.percentile(datacompletes, 0.50);
                q3 = Percentile.percentile(datacompletes, 0.75);
            }

            results.add(new OnionperfIncludingPartialsStatistic(date, filesize, source, server, q1, md, q3));
        }

        return results;
    }

    public List<OnionperfFailureStatistic> getFailureList() {
        List<OnionperfFailureStatistic> results = new ArrayList<>();

        // Group by date, source, server
        Map<String, List<Measurement>> grouped = new HashMap<>();
        Date currentDate = new Date();
        for (Measurement m : measurements) {
            // Ensure the measurement date is before yesterday
            if (m.getStart().before(new Timestamp(currentDate.getTime() - 24 * 60 * 60 * 1000))) {
                String serverType = m.getEndpointRemote().endsWith(".onion") ? "onion" : "public";
                String key = String.format("%s-%s-%s",
                        new Date(m.getStart().getTime()).toString(), m.getSource(), serverType);
                if (!grouped.containsKey(key)) {
                    grouped.put(key, new ArrayList<Measurement>());
                }
                grouped.get(key).add(m);
            }
        }

        for (String key : grouped.keySet()) {
            List<Measurement> group = grouped.get(key);
            String[] parts = key.split("-");
            Date date = new Date(group.get(0).getStart().getTime());
            String source = group.get(0).getSource();
            String server = parts[2];

            int timeouts = 0;
            int failures = 0;
            int requests = group.size();

            for (Measurement m : group) {
                if (Boolean.TRUE.equals(m.getDidTimeout()) || (m.getDataComplete() != null && m.getDataComplete() < 1)) {
                    timeouts++;
                } else if (Boolean.FALSE.equals(m.getDidTimeout()) && m.getDataComplete() != null && m.getDataComplete() >= 1 && m.getReadBytes() < m.getFilesize()) {
                    failures++;
                }
            }

            results.add(new OnionperfFailureStatistic(date, source, server, timeouts, failures, requests));
        }

        return results;
    }

    public List<BuildTimeStatistic> getBuildTimeList() {
        List<BuildTimeStatistic> results = new ArrayList<>();

        // Group by date, source, position
        Map<String, List<BuildTime>> grouped = new HashMap<>();
        Date currentDate = new Date();
        for (Measurement m : measurements) {
            if (m.getStart().before(new Timestamp(currentDate.getTime() - 24 * 60 * 60 * 1000))) {
                for (BuildTime bt : m.getBuildTimes()) {
                    if (bt.getPosition() <= 3) {
                        String key = String.format("%s-%s-%d",
                                new Date(m.getStart().getTime()).toString(), m.getSource(), bt.getPosition());
                        if (!grouped.containsKey(key)) {
                            grouped.put(key, new ArrayList<BuildTime>());
                        }
                        grouped.get(key).add(bt);
                    }
                }
            }
        }

        for (String key : grouped.keySet()) {
            List<BuildTime> group = grouped.get(key);
            String[] parts = key.split("-");
            Date date = new Date(group.get(0).getStart().getTime());
            String source = parts[1];
            int position = group.get(0).getPosition();

            List<Integer> deltas = new ArrayList<>();
            for (BuildTime bt : group) {
                deltas.add(bt.getDelta());
            }

            Double q1 = null, md = null, q3 = null;
            if (!deltas.isEmpty()) {
                q1 = Percentile.percentile(deltas, 0.25);
                md = Percentile.percentile(deltas, 0.50);
                q3 = Percentile.percentile(deltas, 0.75);
            }

            results.add(new BuildTimeStatistic(date, source, position, q1 != null ? Math.floor(q1) : null, md != null ? Math.floor(md) : null, q3 != null ? Math.floor(q3) : null));
        }

        return results;
    }

    public List<LatencyStatistic> getLatencyList() {
        List<LatencyStatistic> results = new ArrayList<>();

        // Filter and process data
        List<FilteredMeasurement> filteredMeasurements = new ArrayList<>();
        Date currentDate = new Date();
        for (int i = 0; i < measurements.size(); i++) {
            Measurement m = measurements.get(i);
            if (m.getStart().before(new Timestamp(currentDate.getTime() - 24 * 60 * 60 * 1000)) &&
                    m.getDataRequest() > 0 && m.getDataResponse() > 0) {
                String serverType = m.getEndpointRemote().endsWith(".onion") ? "onion" : "public";
                int latency = m.getDataResponse() - m.getDataRequest();
                filteredMeasurements.add(new FilteredMeasurement(
                        new Date(m.getStart().getTime()), m.getSource(), serverType, latency
                ));
            }
        }

        // Group by date, source, server
        Map<String, List<FilteredMeasurement>> grouped = new HashMap<>();
        for (int i = 0; i < filteredMeasurements.size(); i++) {
            FilteredMeasurement fm = filteredMeasurements.get(i);
            String key = String.format("%s-%s-%s",
                    fm.getDate().toString(), fm.getSource(), fm.getServer());
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<FilteredMeasurement>());
            }
            grouped.get(key).add(fm);
        }

        for (String key : grouped.keySet()) {
            List<FilteredMeasurement> group = grouped.get(key);
            String[] parts = key.split("-");
            Date date = group.get(0).getDate();
            String source = group.get(0).getSource();
            String server = parts[2];

            List<Integer> latencies = new ArrayList<>();
            for (int i = 0; i < group.size(); i++) {
                FilteredMeasurement fm = group.get(i);
                latencies.add(fm.getLatency());
            }

            Double q1 = null, md = null, q3 = null;
            if (!latencies.isEmpty()) {
                q1 = Percentile.percentile(latencies, 0.25);
                md = Percentile.percentile(latencies, 0.50);
                q3 = Percentile.percentile(latencies, 0.75);
            }

            Integer low = null, high = null;
            if (q1 != null && q3 != null) {
                double iqr = q3 - q1;
                low = getMinLatency(latencies, q1 - 1.5 * iqr);
                high = getMaxLatency(latencies, q3 + 1.5 * iqr);
            }

            results.add(new LatencyStatistic(date, source, server,
                    low, q1 != null ? (int) Math.floor(q1) : null, md != null ? (int) Math.floor(md) : null,
                    q3 != null ? (int) Math.floor(q3) : null, high));
        }

        return results;
    }

    public List<ThroughputStatistic> getThroughputList() {
        List<ThroughputStatistic> results = new ArrayList<>();

        // Filter and process data
        List<ThroughputMeasurement> throughputMeasurements = new ArrayList<>();
        Date currentDate = new Date();
        for (int i = 0; i < measurements.size(); i++) {
            Measurement m = measurements.get(i);
            if (m.getStart().before(new Timestamp(currentDate.getTime() - 24 * 60 * 60 * 1000))) {
                String serverType = m.getEndpointRemote().endsWith(".onion") ? "onion" : "public";
                Double kbps = null;
                if (m.getFilesize() == 1048576 && m.getDataPerc100() > m.getDataPerc50()) {
                    kbps = 4194304.0 / (m.getDataPerc100() - m.getDataPerc50());
                } else if (m.getFilesize() == 5242880 && m.getDataPerc100() > m.getDataPerc80()) {
                    kbps = 8388608.0 / (m.getDataPerc100() - m.getDataPerc80());
                }
                if (kbps != null) {
                    throughputMeasurements.add(new ThroughputMeasurement(
                            new Date(m.getStart().getTime()), m.getSource(), serverType, kbps
                    ));
                }
            }
        }

        // Group by date, source, server
        Map<String, List<ThroughputMeasurement>> grouped = new HashMap<>();
        for (int i = 0; i < throughputMeasurements.size(); i++) {
            ThroughputMeasurement tm = throughputMeasurements.get(i);
            String key = String.format("%s-%s-%s",
                    tm.getDate().toString(), tm.getSource(), tm.getServer());
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<ThroughputMeasurement>());
            }
            grouped.get(key).add(tm);
        }

        for (String key : grouped.keySet()) {
            List<ThroughputMeasurement> group = grouped.get(key);
            String[] parts = key.split("-");
            Date date = group.get(0).getDate();
            String source = group.get(0).getSource();
            String server = parts[2];

            List<Double> kbpsValues = new ArrayList<>();
            for (int i = 0; i < group.size(); i++) {
                ThroughputMeasurement tm = group.get(i);
                kbpsValues.add(tm.getKbps());
            }

            Double q1 = null, md = null, q3 = null;
            if (!kbpsValues.isEmpty()) {
                q1 = Percentile.percentile(0.25, kbpsValues);
                md = Percentile.percentile(0.50, kbpsValues);
                q3 = Percentile.percentile(0.75, kbpsValues);
            }

            Double low = null, high = null;
            if (q1 != null && q3 != null) {
                double iqr = q3 - q1;
                low = getMinKbps(kbpsValues, q1 - 1.5 * iqr);
                high = getMaxKbps(kbpsValues, q3 + 1.5 * iqr);
            }

            results.add(new ThroughputStatistic(date, source, server,
                    low, q1 != null ? Math.floor(q1) : null, md != null ? Math.floor(md) : null,
                    q3 != null ? Math.floor(q3) : null, high));
        }

        return results;
    }

    private Integer getMinLatency(List<Integer> latencies, double threshold) {
        Integer min = null;
        for (int i = 0; i < latencies.size(); i++) {
            int lat = latencies.get(i);
            if (lat >= threshold) {
                if (min == null || lat < min) {
                    min = lat;
                }
            }
        }
        return min;
    }

    private Integer getMaxLatency(List<Integer> latencies, double threshold) {
        Integer max = null;
        for (int i = 0; i < latencies.size(); i++) {
            int lat = latencies.get(i);
            if (lat <= threshold) {
                if (max == null || lat > max) {
                    max = lat;
                }
            }
        }
        return max;
    }

    private Double getMinKbps(List<Double> kbpsValues, double threshold) {
        Double min = null;
        for (int i = 0; i < kbpsValues.size(); i++) {
            double kbps = kbpsValues.get(i);
            if (kbps >= threshold) {
                if (min == null || kbps < min) {
                    min = kbps;
                }
            }
        }
        return min;
    }

    private Double getMaxKbps(List<Double> kbpsValues, double threshold) {
        Double max = null;
        for (int i = 0; i < kbpsValues.size(); i++) {
            double kbps = kbpsValues.get(i);
            if (kbps <= threshold) {
                if (max == null || kbps > max) {
                    max = kbps;
                }
            }
        }
        return max;
    }

    class MeasurementIncludingPartials {
        private Timestamp start;
        private int filesize;
        private String source;
        private String endpointRemote;
        private Integer dataComplete;

        public MeasurementIncludingPartials(Timestamp start, int filesize, String source, String endpointRemote, Integer dataComplete) {
            this.start = start;
            this.filesize = filesize;
            this.source = source;
            this.endpointRemote = endpointRemote;
            this.dataComplete = dataComplete;
        }

        public Timestamp getStart() {
            return start;
        }

        public int getFilesize() {
            return filesize;
        }

        public String getSource() {
            return source;
        }

        public String getEndpointRemote() {
            return endpointRemote;
        }

        public Integer getDataComplete() {
            return dataComplete;
        }
    }

    class FilteredMeasurement {
        private Date date;
        private String source;
        private String server;
        private int latency;

        public FilteredMeasurement(Date date, String source, String server, int latency) {
            this.date = date;
            this.source = source;
            this.server = server;
            this.latency = latency;
        }

        public Date getDate() {
            return date;
        }

        public String getSource() {
            return source;
        }

        public String getServer() {
            return server;
        }

        public int getLatency() {
            return latency;
        }
    }

    class ThroughputMeasurement {
        private Date date;
        private String source;
        private String server;
        private double kbps;

        public ThroughputMeasurement(Date date, String source, String server, double kbps) {
            this.date = date;
            this.source = source;
            this.server = server;
            this.kbps = kbps;
        }

        public Date getDate() {
            return date;
        }

        public String getSource() {
            return source;
        }

        public String getServer() {
            return server;
        }

        public double getKbps() {
            return kbps;
        }
    }

}
