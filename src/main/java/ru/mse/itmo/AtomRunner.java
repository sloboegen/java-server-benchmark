package ru.mse.itmo;

import ru.mse.itmo.client.Client;
import ru.mse.itmo.common.Constants;
import ru.mse.itmo.server.Server;
import ru.mse.itmo.server.async.ServerAsync;
import ru.mse.itmo.server.blocking.ServerBlocking;
import ru.mse.itmo.server.nonblocking.ServerNonBlocking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AtomRunner {
    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService clientsExecutor = Executors.newCachedThreadPool();

    private final CountDownLatch initLatch;
    private final CountDownLatch stopLatch;

    private final Server server;
    private final List<Client> clients = new ArrayList<>();
    private final int requestNumber;
    private final int requestDelta;
    private final int arraySize;
    private final int clientNumber;

    public AtomRunner(ServerArchitecture archType, int requestNumber, int requestDelta, int arraySize, int clientNumber) throws IOException {
        initLatch = new CountDownLatch(clientNumber);
        stopLatch = new CountDownLatch(1);

        switch (archType) {
            case BLOCKING -> server = new ServerBlocking(stopLatch);
            case NONBLOCKING -> server = new ServerNonBlocking(stopLatch);
            case ASYNCHRONOUS -> server = new ServerAsync(stopLatch);
            default -> throw new RuntimeException("Unsupported server architecture type");
        }

        this.requestNumber = requestNumber;
        this.requestDelta = requestDelta;
        this.arraySize = arraySize;
        this.clientNumber = clientNumber;
    }

    public AtomRunResults run() throws InterruptedException {
        serverExecutor.submit(server::launch);

        for (int i = 0; i < clientNumber; i++) {
            Client client = new Client(initLatch, stopLatch,
                    Constants.SERVER_ADDRESS, Constants.SERVER_PORT,
                    requestNumber, requestDelta, arraySize);
            clients.add(client);
        }

        for (Client client : clients) {
            clientsExecutor.submit(() -> {
                try {
                    client.run();
                } catch (InterruptedException | IOException | ExecutionException e) {
                    stopLatch.countDown();
                }
            });
        }

        stopLatch.await();

        shutdown();

        double timeClient = clients.stream()
                .map(Client::getAverageRequestTime)
                .mapToDouble(x -> x)
                .average().orElse(0.0);

        double timeServer = server.getAverageServerTime();
        double timeTask = server.getAverageTaskTime();

        return new AtomRunResults(timeServer, timeTask, timeClient);
    }

    private void shutdown() {
        try {
            server.shutdown();
            clients.forEach(c -> {
                try {
                    c.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverExecutor.shutdown();
            clientsExecutor.shutdown();
        } catch (IOException ignored) {
        }
    }
}
