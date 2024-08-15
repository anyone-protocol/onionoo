package org.torproject.metrics.onionoo.onionperf;

import java.util.Collections;
import java.util.List;

public class Percentile {

    public static Double percentile(List<Integer> values, double percentile) {
        if (values.contains(null)) {
            return null;
        }
        int index = (int) Math.ceil(percentile * values.size());
        return Double.valueOf(values.get(index - 1));
    }

    public static Double percentile(double percentile, List<Double> values) {
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return values.get(Math.min(index, values.size() - 1));
    }
}