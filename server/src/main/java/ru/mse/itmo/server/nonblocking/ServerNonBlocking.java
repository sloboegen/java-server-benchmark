package ru.mse.itmo.server.nonblocking;

import ru.mse.itmo.common.ArrayUtils;
import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerNonBlocking extends Server {
    private final ServerSocketChannel serverChannel;
    private final Selector readSelector = Selector.open();
    private final Selector writeSelector = Selector.open();

    private final Queue<ClientContext> registrationReadQueue = new ConcurrentLinkedQueue<>();
    private final Queue<ClientContext> registrationWriteQueue = new ConcurrentLinkedQueue<>();

    private final ExecutorService readSelectorPool = Executors.newSingleThreadExecutor();
    private final ExecutorService writeSelectorPool = Executors.newSingleThreadExecutor();

    public ServerNonBlocking() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(Constants.SERVER_ADDRESS, Constants.SERVER_PORT));
    }

    @Override
    public void launch() {
        readSelectorPool.submit(this::readWorker);
        writeSelectorPool.submit(this::writeWorker);
        try {
            while (serverChannel.isOpen()) {
                SocketChannel socketChannel = serverChannel.accept();
                socketChannel.configureBlocking(false);
                registrationReadQueue.add(new ClientContext(socketChannel));
                readSelector.wakeup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() throws IOException {
        serverChannel.close();
        workerPool.shutdown();
        readSelectorPool.shutdown();
        writeSelectorPool.shutdown();
    }

    private void readWorker() {
        try {
            while (serverChannel.isOpen()) {
                readSelector.select();
                Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        ClientContext context = (ClientContext) selectionKey.attachment();
                        int bytesRead = context.socketChannel.read(context.byteBuffer);

                        // TODO: а точно ли нужна эта проверка?
                        if (bytesRead > 0) {
                            int curBytesRead = bytesRead;
                            context.bytesRead += bytesRead;

                            context.byteBuffer.flip();

                            if (!context.isMsgSizeInitialize()) {
                                if (context.byteBuffer.limit() >= Integer.BYTES) {
                                    context.msgSize = context.byteBuffer.getInt();
                                    context.requestBuffer = ByteBuffer.allocate(context.msgSize);
                                    curBytesRead -= Integer.BYTES;
                                }
                            }

                            byte[] bytes = new byte[curBytesRead];
                            context.byteBuffer.get(bytes, 0, curBytesRead);
                            context.requestBuffer.put(bytes);

                            context.byteBuffer.clear();

                            if (context.bytesRead == context.msgSize + Integer.BYTES) {
                                context.requestBuffer.flip();
                                Message request = Message.parseFrom(context.requestBuffer);
                                List<Integer> array = request.getArrayList();
                                System.out.println("Server: got msg");
//                                array.forEach(System.out::println);
                                doSortTask(context, array);
                                context.toInitState();
                            }
                        }
                    }
                    iterator.remove();
                }

                while (!registrationReadQueue.isEmpty()) {
                    ClientContext context = registrationReadQueue.poll();
                    context.socketChannel.register(readSelector, SelectionKey.OP_READ, context);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWorker() {
        try {
            while (serverChannel.isOpen()) {
                writeSelector.select();
                Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isWritable()) {
                        ClientContext context = (ClientContext) selectionKey.attachment();
                        context.bytesWrite += context.socketChannel.write(context.responseBuffer);
                        if (context.bytesWrite == context.msgSize + Integer.BYTES) {
//                            System.out.println("Server: msg send");
                            selectionKey.cancel();
                        }
                    }
                    iterator.remove();
                }

                while (!registrationWriteQueue.isEmpty()) {
                    ClientContext context = registrationWriteQueue.poll();
                    context.socketChannel.register(writeSelector, SelectionKey.OP_WRITE, context);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doSortTask(ClientContext context, List<Integer> array) {
        workerPool.submit(() -> {
            List<Integer> sortedArray = ArrayUtils.insertionSort(array);
            Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();
            context.responseBuffer = ByteBuffer.allocate(response.getSerializedSize() + Integer.BYTES);
            context.responseBuffer.putInt(response.getSerializedSize());
            context.responseBuffer.put(response.toByteArray());
            context.responseBuffer.flip();
            registrationWriteQueue.add(context);
            writeSelector.wakeup();
        });
    }
}
