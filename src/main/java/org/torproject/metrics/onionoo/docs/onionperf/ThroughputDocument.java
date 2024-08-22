package org.torproject.metrics.onionoo.docs.onionperf;

import org.torproject.metrics.onionoo.onionperf.Measurement;
import org.torproject.metrics.onionoo.onionperf.ThroughputStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ThroughputDocument extends OnionperfDocument {

    public ThroughputDocument(List<Measurement> measurements) {
        super(measurements);
    }

    @Override
    public String getDocumentString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<ThroughputStatistic> throughputList = getThroughputList();
        List<String> statistics = new ArrayList<>();
        statistics.add("date,source,server,low,q1,md,q3,high");

        for (ThroughputStatistic throughputStatistic : throughputList) {
            statistics.add(String.format("%s,%s,%s,%d,%d,%d,%d,%d",
                    dateFormat.format(throughputStatistic.getDate()),
                    throughputStatistic.getSource(),
                    throughputStatistic.getServer(),
                    throughputStatistic.getLow() == null ? null : throughputStatistic.getLow().intValue(),
                    throughputStatistic.getQ1() == null ? null : throughputStatistic.getQ1().intValue(),
                    throughputStatistic.getMd() == null ? null : throughputStatistic.getMd().intValue(),
                    throughputStatistic.getQ3() == null ? null : throughputStatistic.getQ3().intValue(),
                    throughputStatistic.getHigh() == null ? null : throughputStatistic.getHigh().intValue()
                    )
            );
        }

        return String.join("\n", statistics);
    }
}
