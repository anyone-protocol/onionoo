package org.torproject.metrics.onionoo.onionperf;

import java.util.Collections;
import java.util.List;

public class Percentile {

    public static double percentile(List<Integer> values, double percentile) {
        Collections.sort(values);
        int index = (int) Math.ceil(percentile * values.size());
        return values.get(index - 1);
    }

    public static double percentile(double percentile, List<Double> values) {
        Collections.sort(values);
        int index = (int) Math.ceil(percentile * values.size());
        return values.get(index - 1);
    }
}