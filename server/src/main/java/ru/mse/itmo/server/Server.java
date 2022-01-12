package ru.mse.itmo.server;

import ru.mse.itmo.common.TimeMeter;

import java.io.IOException;

public abstract class Server {
    protected final TimeMeter serverTimeMeter = new TimeMeter();
    protected final TimeMeter taskTimeMeter = new TimeMeter();

    public abstract void launch();

    public abstract void shutdown() throws IOException;

    public double getAverageTaskTime() {
        return taskTimeMeter.getAverageTime();
    }

    public double getAverageServerTime() {
        return serverTimeMeter.getAverageTime();
    }
}
