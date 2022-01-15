package ru.mse.itmo.server;

import ru.mse.itmo.common.ArrayUtils;
import ru.mse.itmo.common.TimeMeter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server {
    protected final TimeMeter serverTimeMeter = new TimeMeter();
    protected final TimeMeter taskTimeMeter = new TimeMeter();

    protected final ExecutorService workerPool = Executors.newFixedThreadPool(5);

    protected final CountDownLatch stopLatch;

    public Server(CountDownLatch stopLatch) {
        this.stopLatch = stopLatch;
    }

    public abstract void launch();

    public abstract void shutdown() throws IOException;

    public double getAverageTaskTime() {
        return taskTimeMeter.getAverageTime();
    }

    public double getAverageServerTime() {
        return serverTimeMeter.getAverageTime();
    }

    protected List<Integer> sortArrayAndRegisterTime(List<Integer> array) throws ExecutionException, InterruptedException {
        Instant before = Instant.now();
        List<Integer> sortedArray = workerPool.submit(() -> ArrayUtils.insertionSort(array)).get();
        Instant after = Instant.now();
        taskTimeMeter.addTimeMeasure(Duration.between(before, after));
        return sortedArray;
    }
}
