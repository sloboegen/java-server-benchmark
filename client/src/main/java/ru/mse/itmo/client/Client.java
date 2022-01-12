package ru.mse.itmo.client;

import ru.mse.itmo.common.*;
import ru.mse.itmo.proto.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

public class Client {
    private Socket socket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;

    private final int requestCount;
    private final int requestDelta;
    private final int arraySize;

    private final CountDownLatch clientEndWorkLatch;
    private final CountDownLatch serverMetricsLatch;

    private final TimeMeter clientTimeMeter = new TimeMeter();

    public Client(CountDownLatch clientEndWorkLatch,
                  CountDownLatch serverMetricsLatch,
                  String address, int port, int requestCount, int requestDelta, int arraySize) {
        this.requestCount = requestCount;
        this.requestDelta = requestDelta;
        this.arraySize = arraySize;
        this.clientEndWorkLatch = clientEndWorkLatch;
        this.serverMetricsLatch = serverMetricsLatch;
        try {
            this.socket = new Socket(InetAddress.getByName(address), port);
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws InterruptedException, IOException, ExecutionException {
        serverMetricsLatch.countDown();
        serverMetricsLatch.await();

        for (int i = 0; i < requestCount; i++) {
            List<Integer> array = ArrayUtils.generateRandomArray(arraySize);

            Instant before = Instant.now();
            sendRequest(array);
            List<Integer> sortedArray = handleResponse();
            Instant after = Instant.now();

            clientTimeMeter.addTimeMeasure(Duration.between(before, after));

            Thread.sleep(requestDelta);
        }

        System.out.println("Client " + socket.getLocalPort() + ": all done");
        clientEndWorkLatch.countDown();
    }

    public double getAverageRequestTime() {
        return clientTimeMeter.getAverageTime();
    }

    public void shutdown() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }

    private void sendRequest(List<Integer> array) throws IOException {
        Message request = Message.newBuilder().setN(array.size()).addAllArray(array).build();
        MessageUtils.writeMessage(outputStream, request);
    }

    private List<Integer> handleResponse() throws IOException {
        MessageWrapper messageWrapper = MessageUtils.readMessage(inputStream);
        return messageWrapper.message.getArrayList();
    }
}
