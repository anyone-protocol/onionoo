package org.torproject.metrics.onionoo.docs;

import org.torproject.descriptor.TorperfResult;
import org.torproject.metrics.onionoo.onionperf.Measurement;

import java.util.List;

public class OnionperfStatus extends Document {


    private List<Measurement> measurements;

    public OnionperfStatus() {
    }

    public OnionperfStatus(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

}
