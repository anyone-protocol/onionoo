package org.torproject.metrics.onionoo.onionperf;

import org.torproject.descriptor.TorperfResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TorperfResultConverter {

    public Measurement toMeasurement(TorperfResult tr) {
        String truncatedSource = truncateString(tr.getSource(), 32);
        Measurement m = new Measurement();
        m.setMeasurementId(UUID.randomUUID().toString());
        m.setSource(truncatedSource);
        m.setFilesize(tr.getFileSize());
        m.setStart(new Timestamp(tr.getStartMillis()));
        m.setSocket(toNullableNumber(tr.getSocketMillis(), tr.getStartMillis()));
        m.setConnect(toNullableNumber(tr.getConnectMillis(), tr.getStartMillis()));
        m.setNegotiate(toNullableNumber(tr.getNegotiateMillis(), tr.getStartMillis()));
        m.setRequest(toNullableNumber(tr.getRequestMillis(), tr.getStartMillis()));
        m.setResponse(toNullableNumber(tr.getResponseMillis(), tr.getStartMillis()));
        m.setDataRequest(toNullableNumber(tr.getDataRequestMillis(), tr.getStartMillis()));
        m.setDataResponse(toNullableNumber(tr.getDataResponseMillis(), tr.getStartMillis()));
        m.setDataComplete(toNullableNumber(tr.getDataCompleteMillis(), tr.getStartMillis()));
        m.setWriteBytes(tr.getWriteBytes());
        m.setReadBytes(tr.getReadBytes());
        if (null == tr.didTimeout()) {
            m.setDidTimeout(null);
        } else {
            m.setDidTimeout(tr.didTimeout());
        }
        if (tr.getDataPercentiles() != null) {
            m.setDataPerc0(toNullableNumber(tr.getDataPercentiles().get(0), tr.getStartMillis()));
            m.setDataPerc10(toNullableNumber(tr.getDataPercentiles().get(10), tr.getStartMillis()));
            m.setDataPerc20(toNullableNumber(tr.getDataPercentiles().get(20), tr.getStartMillis()));
            m.setDataPerc30(toNullableNumber(tr.getDataPercentiles().get(30), tr.getStartMillis()));
            m.setDataPerc40(toNullableNumber(tr.getDataPercentiles().get(40), tr.getStartMillis()));
            m.setDataPerc50(toNullableNumber(tr.getDataPercentiles().get(50), tr.getStartMillis()));
            m.setDataPerc60(toNullableNumber(tr.getDataPercentiles().get(60), tr.getStartMillis()));
            m.setDataPerc70(toNullableNumber(tr.getDataPercentiles().get(70), tr.getStartMillis()));
            m.setDataPerc80(toNullableNumber(tr.getDataPercentiles().get(80), tr.getStartMillis()));
            m.setDataPerc90(toNullableNumber(tr.getDataPercentiles().get(90), tr.getStartMillis()));
            m.setDataPerc100(toNullableNumber(tr.getDataPercentiles().get(100), tr.getStartMillis()));
        }
        if (tr.getLaunchMillis() < 0L) {
            m.setLaunch(null);
        } else {
            m.setLaunch(new Timestamp(tr.getLaunchMillis()));
        }
        if (tr.getUsedAtMillis() < 0L) {
            m.setUsedAt(null);
        } else {
            m.setUsedAt(new Timestamp(tr.getUsedAtMillis()));
        }
        if (tr.getTimeout() < 0L) {
            m.setTimeout(null);
        } else {
            m.setTimeout((int) tr.getTimeout());
        }
        if (tr.getQuantile() < 0.0) {
            m.setQuantile(null);
        } else {
            m.setQuantile(tr.getQuantile());
        }
        if (tr.getCircId() < 0L) {
            m.setCircId(null);
        } else {
            m.setCircId(tr.getCircId());
        }
        if (tr.getUsedBy() < 0L) {
            m.setUsedBy(null);
        } else {
            m.setUsedBy(tr.getUsedBy());
        }
        m.setEndpointLocal(truncateString(tr.getEndpointLocal(), 64));
        m.setEndpointProxy(truncateString(tr.getEndpointProxy(), 64));

        if (m.getEndpointRemote() != null && !m.getEndpointRemote().contains("onion")) {
            m.setEndpointRemote(truncateString(m.getEndpointRemote(), 64));
        }

        m.setEndpointRemote(truncateString(tr.getEndpointRemote(), 64));
        m.setHostnameLocal(truncateString(tr.getHostnameLocal(), 64));
        m.setHostnameRemote(truncateString(tr.getHostnameRemote(), 64));
        m.setSourceAddress(truncateString(tr.getSourceAddress(), 64));
        if (tr.getPartials() != null) {
            m.setPartial10240(toNullableNumber(tr.getPartials().get(10240), tr.getStartMillis()));
            m.setPartial20480(toNullableNumber(tr.getPartials().get(20480), tr.getStartMillis()));
            m.setPartial51200(toNullableNumber(tr.getPartials().get(51200), tr.getStartMillis()));
            m.setPartial102400(toNullableNumber(tr.getPartials().get(102400), tr.getStartMillis()));
            m.setPartial204800(toNullableNumber(tr.getPartials().get(204800), tr.getStartMillis()));
            m.setPartial512000(toNullableNumber(tr.getPartials().get(512000), tr.getStartMillis()));
            m.setPartial1048576(toNullableNumber(tr.getPartials().get(1048576), tr.getStartMillis()));
            m.setPartial2097152(toNullableNumber(tr.getPartials().get(2097152), tr.getStartMillis()));
            m.setPartial5242880(toNullableNumber(tr.getPartials().get(5242880), tr.getStartMillis()));
        }
        if (null != tr.getBuildTimes()) {
            List<BuildTime> buildTimes = new ArrayList<>();
            int position = 1;
            long previousBuildTime = 0L;
            for (long buildtime : tr.getBuildTimes()) {
                BuildTime buildTime = new BuildTime();
                buildTime.setStart(new Timestamp(tr.getStartMillis()));
                buildTime.setSource(truncatedSource);
                buildTime.setPosition(position);
                buildTime.setBuildtime((int) buildtime);
                buildTime.setDelta((int) (buildtime - previousBuildTime));
                position++;
                previousBuildTime = buildtime;
                buildTimes.add(buildTime);
            }
            m.setBuildTimes(buildTimes);
        }
        return m;
    }

    private static Integer toNullableNumber(long millis, long start) {
        return millis == 0L ? null : (int) (millis - start);
    }

    private static String truncateString(String originalString, int truncateAfter) {
        if (originalString == null) {
            return null;
        }
        if (originalString.length() > truncateAfter) {
            originalString = originalString.substring(0, truncateAfter);
        }
        return originalString;
    }

}
