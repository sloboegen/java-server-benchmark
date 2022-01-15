package ru.mse.itmo;

public class AtomRunResults {
    public final double timeServer;
    public final double timeTask;
    public final double timeClient;

    public AtomRunResults(double timeServer, double timeTask, double timeClient) {
        this.timeServer = timeServer;
        this.timeTask = timeTask;
        this.timeClient = timeClient;
    }
}
