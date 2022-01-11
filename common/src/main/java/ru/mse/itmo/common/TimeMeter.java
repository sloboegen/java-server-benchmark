package ru.mse.itmo.common;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimeMeter {
    private final Queue<Long> times = new ConcurrentLinkedQueue<>();

    public TimeMeter() {}

    public void addTimeMeasure(long time) {
        times.add(time);
    }

    public void addTimeMeasure(Duration duration) {
        times.add(duration.toMillis());
    }

    public double getAverageTime() {
        return times.stream().mapToDouble(x -> x).average().orElse(0.0);
    }
}
