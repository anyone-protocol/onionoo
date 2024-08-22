package org.torproject.metrics.onionoo.docs.onionperf;

import org.torproject.metrics.onionoo.onionperf.Measurement;
import org.torproject.metrics.onionoo.onionperf.OnionperfIncludingPartialsStatistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DownloadDocument extends OnionperfDocument {
    public DownloadDocument(List<Measurement> measurements) {
        super(measurements);
    }

    @Override
    public String getDocumentString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<OnionperfIncludingPartialsStatistic> includingPartialsList = getIncludingPartialsList();
        List<String> statistics = new ArrayList<>();
        statistics.add("date,filesize,source,server,q1,md,q3");

        for (OnionperfIncludingPartialsStatistic statistic: includingPartialsList) {
            statistics.add(String.format("%s,%d,%s,%s,%.0f,%.0f,%.0f",
                            dateFormat.format(statistic.getDate()),
                            statistic.getFilesize(),
                            statistic.getSource(),
                            statistic.getServerType(),
                            statistic.getQ1(),
                            statistic.getMd(),
                            statistic.getQ3()
                    )
            );
        }

        return String.join("\n", statistics);
    }
}
