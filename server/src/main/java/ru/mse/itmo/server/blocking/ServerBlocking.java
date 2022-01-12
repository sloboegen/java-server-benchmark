package ru.mse.itmo.server.blocking;

import ru.mse.itmo.common.*;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;
import ru.mse.itmo.server.WriteTaskServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerBlocking extends Server {
    private final ServerSocket serverSocket;
    private final ExecutorService readPool = Executors.newCachedThreadPool();
    private final ExecutorService workerPool = Executors.newFixedThreadPool(5);

    public ServerBlocking() throws IOException {
        serverSocket = new ServerSocket(Constants.SERVER_PORT);
    }

    @Override
    public void launch() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                readPool.submit(new ClientWorker(socket));
            }
        } catch (IOException ignored) {
            System.out.println("Server end");
        }
    }

    @Override
    public void shutdown() throws IOException {
        readPool.shutdown();
        workerPool.shutdown();
        serverSocket.close();
    }

    private class ClientWorker implements Runnable {
        private final ExecutorService writePool = Executors.newSingleThreadExecutor();

        private final Socket socket;
        private final DataInputStream inputStream;
        private final DataOutputStream outputStream;

        public ClientWorker(Socket socket) throws IOException {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try (Socket socket = this.socket) {
                while (!socket.isClosed()) {
                    MessageWrapper messageWrapper = MessageUtils.readMessage(inputStream);
                    handleRequest(messageWrapper.message);

                }
            } catch (IOException | ExecutionException | InterruptedException ignored) {
            } finally {
                writePool.shutdown();
            }
        }

        private void handleRequest(Message request) throws ExecutionException, InterruptedException {
            final List<Integer> array = request.getArrayList();

            Instant before = Instant.now();
            List<Integer> sortedArray = workerPool.submit(() -> ArrayUtils.insertionSort(array)).get();
            Instant after = Instant.now();

            taskTimeMeter.addTimeMeasure(Duration.between(before, after));

            Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();
            executeWriteTask(() -> MessageUtils.writeMessage(outputStream, response));
        }

        private void executeWriteTask(WriteTaskServer task) {
            writePool.submit(() -> {
                try {
                    task.run();
                } catch (IOException ignored) {
                }
            });
        }
    }
}
