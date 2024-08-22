package org.torproject.metrics.onionoo.docs.onionperf;

import org.torproject.metrics.onionoo.onionperf.BuildTimeStatistic;
import org.torproject.metrics.onionoo.onionperf.Measurement;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CircuitDocument extends OnionperfDocument {

    public CircuitDocument(List<Measurement> measurements) {
        super(measurements);
    }

    @Override
    public String getDocumentString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<BuildTimeStatistic> buildTimeList = getBuildTimeList();
        List<String> statistics = new ArrayList<>();
        statistics.add("date,source,position,q1,md,q3");

        for (BuildTimeStatistic buildTime : buildTimeList) {
            statistics.add(String.format("%s,%s,%d,%d,%d,%d",
                    dateFormat.format(buildTime.getDate()),
                    buildTime.getSource(),
                    buildTime.getPosition(),
                    buildTime.getQ1() == null ? null : buildTime.getQ1().intValue(),
                    buildTime.getMd() == null ? null : buildTime.getMd().intValue(),
                    buildTime.getQ3() == null ? null : buildTime.getQ3().intValue()));
        }

        return String.join("\n", statistics);
    }
}
