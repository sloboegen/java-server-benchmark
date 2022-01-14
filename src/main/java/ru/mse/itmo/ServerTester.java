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

public class ServerTester {
    private final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService clientsExecutor = Executors.newCachedThreadPool();

    private final Server server;
    private final List<Client> clients = new ArrayList<>();
    private final int x;
    private final int delta;
    private final int n;
    private final int m;

    public ServerTester(int archType, int x, int delta, int n, int m) throws IOException {
        switch (archType) {
            case 1 -> server = new ServerBlocking();
            case 2 -> server = new ServerNonBlocking();
            case 3 -> server = new ServerAsync();
            default -> throw new RuntimeException("Unsupported server architecture type");
        }

        this.x = x;
        this.delta = delta;
        this.n = n;
        this.m = m;
    }

    public void doExperiment() throws InterruptedException {
        serverExecutor.submit(server::launch);

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        CountDownLatch clientEndWorkLatch = new CountDownLatch(m);
        CountDownLatch serverMetricsLatch = new CountDownLatch(m);

        for (int i = 0; i < m; i++) {
            Client client = new Client(clientEndWorkLatch, serverMetricsLatch,
                    Constants.SERVER_ADDRESS,
                    Constants.SERVER_PORT, x, delta, n);
            clients.add(client);
        }

        System.out.println("All clients connected");

        for (Client client : clients) {
            clientsExecutor.submit(() -> {
                try {
                    client.run();
                } catch (InterruptedException | IOException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }

        clientEndWorkLatch.await();

        double timeClient = clients.stream()
                .map(Client::getAverageRequestTime)
                .mapToDouble(x -> x)
                .average().orElse(0.0);

        double timeServer = server.getAverageServerTime();
        double timeTask = server.getAverageTaskTime();

        System.out.println("END ALL");
        System.out.println("Client time: " + timeClient);
        System.out.println("Server time: " + timeServer);
        System.out.println("Task time: " + timeTask);

        shutdown();
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
