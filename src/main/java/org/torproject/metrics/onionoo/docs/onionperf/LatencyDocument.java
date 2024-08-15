package org.torproject.metrics.onionoo.docs.onionperf;

import org.torproject.metrics.onionoo.onionperf.LatencyStatistic;
import org.torproject.metrics.onionoo.onionperf.Measurement;
import org.torproject.metrics.onionoo.onionperf.ThroughputStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LatencyDocument extends OnionperfDocument {
    public LatencyDocument(List<Measurement> measurements) {
        super(measurements);
    }

    @Override
    public String getDocumentString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<LatencyStatistic> latencyList = getLatencyList();
        List<String> statistics = new ArrayList<>();
        statistics.add("date,source,server,low,q1,md,q3,high");

        for (LatencyStatistic latencyStatistic : latencyList) {
            statistics.add(String.format("%s,%s,%s,%d,%d,%d,%d,%d",
                            dateFormat.format(latencyStatistic.getDate()),
                            latencyStatistic.getSource(),
                            latencyStatistic.getServer(),
                            latencyStatistic.getLow(),
                            latencyStatistic.getQ1(),
                            latencyStatistic.getMd(),
                            latencyStatistic.getQ3(),
                            latencyStatistic.getHigh()
                    )
            );
        }

        return String.join("\n", statistics);
    }
}
