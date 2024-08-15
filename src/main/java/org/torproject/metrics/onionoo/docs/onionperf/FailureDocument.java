package org.torproject.metrics.onionoo.docs.onionperf;

import org.torproject.metrics.onionoo.onionperf.Measurement;
import org.torproject.metrics.onionoo.onionperf.OnionperfFailureStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FailureDocument extends OnionperfDocument {
    public FailureDocument(List<Measurement> measurements) {
        super(measurements);
    }

    @Override
    public String getDocumentString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<OnionperfFailureStatistic> failureList = getFailureList();
        List<String> statistics = new ArrayList<>();
        statistics.add("date,source,server,timeout,failure,requests");

        for (OnionperfFailureStatistic failureStatistic: failureList) {
            statistics.add(String.format("%s,%s,%s,%d,%d,%d",
                            dateFormat.format(failureStatistic.getDate()),
                            failureStatistic.getSource(),
                            failureStatistic.getServer(),
                            failureStatistic.getTimeouts(),
                            failureStatistic.getFailures(),
                            failureStatistic.getRequests()
                    )
            );
        }

        return String.join("\n", statistics);
    }
}
