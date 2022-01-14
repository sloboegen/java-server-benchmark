package ru.mse.itmo.client;

import ru.mse.itmo.common.*;
import ru.mse.itmo.proto.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
//            System.out.println("Client" + socket.getLocalPort() + " got answer");
//            sortedArray.forEach(System.out::println);

            Instant after = Instant.now();

            clientTimeMeter.addTimeMeasure(Duration.between(before, after));

            if (!sortedChecker(array, sortedArray)) {
                System.out.println("error: server response isn't correct");
                System.out.println("Generated: => ");
                array.forEach(x -> System.out.println(x + " "));
                System.out.println();

                System.out.println("Response: => ");
                array.forEach(x -> System.out.println(x + " "));
                System.out.println();

                System.exit(1);
            }

            Thread.sleep(requestDelta);
        }

//        System.out.println("Client " + socket.getLocalPort() + ": all done");
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
//        System.out.println("Client: start writing request");
        MessageUtils.writeMessage(outputStream, request);
//        System.out.println("Client: end writing request");
    }

    private List<Integer> handleResponse() throws IOException {
        MessageWrapper messageWrapper = MessageUtils.readMessage(inputStream);
//        System.out.println("Client: start reading response");
        List<Integer> result = messageWrapper.message.getArrayList();
//        System.out.println("Client: end reading response");
        return result;
    }

    private boolean sortedChecker(List<Integer> generated, List<Integer> response) {
        List<Integer> xs = new ArrayList<>(generated);
        xs.sort(Integer::compare);
        return xs.equals(response);
    }
}
