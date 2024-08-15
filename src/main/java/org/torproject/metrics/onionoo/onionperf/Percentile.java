package org.torproject.metrics.onionoo.onionperf;

import java.util.List;

public class Percentile {

    public static Double percentile(List<Integer> values, double percentile) {
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return Double.valueOf(values.get(Math.min(index, values.size() - 1)));
    }

    public static Double percentile(double percentile, List<Double> values) {
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return values.get(Math.min(index, values.size() - 1));
    }
}