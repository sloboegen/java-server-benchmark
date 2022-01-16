package ru.mse.itmo.server.nonblocking;

import ru.mse.itmo.common.ArrayUtils;
import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerNonBlocking extends Server {
    private final ServerSocketChannel serverChannel;

    private final SelectorRead selectorRead = new SelectorRead();
    private final SelectorWrite selectorWrite = new SelectorWrite();

    private final ExecutorService readSelectorPool = Executors.newSingleThreadExecutor();
    private final ExecutorService writeSelectorPool = Executors.newSingleThreadExecutor();

    public ServerNonBlocking(CountDownLatch stopLatch) throws IOException {
        super(stopLatch);
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(Constants.SERVER_ADDRESS, Constants.SERVER_PORT));
    }

    @Override
    public void launch() {
        readSelectorPool.submit(selectorRead);
        writeSelectorPool.submit(selectorWrite);
        try {
            while (serverChannel.isOpen()) {
                SocketChannel socketChannel = serverChannel.accept();
                socketChannel.configureBlocking(false);
                selectorRead.addToRegistrationQueue(new ClientContext(socketChannel));
                selectorRead.wakeup();
            }
        } catch (IOException e) {
            stopLatch.countDown();
        }
    }

    @Override
    public void shutdown() throws IOException {
        serverChannel.close();
        workerPool.shutdown();
        selectorRead.close();
        selectorWrite.close();
        readSelectorPool.shutdown();
        writeSelectorPool.shutdown();
    }

    private class SelectorRead extends SelectorBase {
        public SelectorRead() throws IOException {
            super(SelectionKey.OP_READ, stopLatch);
        }

        @Override
        protected void processSelectionKey(SelectionKey selectionKey) throws IOException {
            if (selectionKey.isReadable()) {
                ClientContext context = (ClientContext) selectionKey.attachment();
                int bytesRead = context.socketChannel.read(context.byteBuffer);

                if (bytesRead > 0) {
                    context.bytesRead += bytesRead;

                    if (!context.isMsgSizeInitialize()) {
                        if (context.bytesRead > Integer.BYTES) {
                            context.byteBuffer.flip();
                            context.msgSize = context.byteBuffer.getInt();
                            context.requestBuffer = ByteBuffer.allocate(context.msgSize);
                            putIntoRequestBuffer(context, context.bytesRead - Integer.BYTES);
                            context.byteBuffer.clear();
                        }
                    } else {
                        context.byteBuffer.flip();
                        putIntoRequestBuffer(context, bytesRead);
                        context.byteBuffer.clear();
                    }

                    if (context.isMsgSizeInitialize() && context.bytesRead == context.msgSize + Integer.BYTES) {
                        context.requestBuffer.flip();
                        Message request = Message.parseFrom(context.requestBuffer);
                        context.requestBuffer.clear();

                        List<Integer> array = request.getArrayList();
                        doSortTask(context, array);
                    }
                }
            }
        }

        private void putIntoRequestBuffer(ClientContext context, int bytesRead) {
            byte[] bytes = new byte[bytesRead];
            context.byteBuffer.get(bytes, 0, bytesRead);
            context.requestBuffer.put(bytes);
        }

        private void doSortTask(ClientContext context, List<Integer> array) {
            workerPool.submit(() -> {
                List<Integer> sortedArray = ArrayUtils.insertionSort(array);
                Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();

                context.responseBuffer = ByteBuffer.allocate(response.getSerializedSize() + Integer.BYTES);
                context.responseBuffer.putInt(response.getSerializedSize());
                context.responseBuffer.put(response.toByteArray());
                context.responseBuffer.flip();
                context.toInitState();

                selectorWrite.addToRegistrationQueue(context);
                selectorWrite.wakeup();
            });
        }
    }

    private class SelectorWrite extends SelectorBase {
        public SelectorWrite() throws IOException {
            super(SelectionKey.OP_WRITE, stopLatch);
        }

        @Override
        protected void processSelectionKey(SelectionKey selectionKey) throws IOException {
            if (selectionKey.isWritable()) {
                ClientContext context = (ClientContext) selectionKey.attachment();
                context.bytesWrite += context.socketChannel.write(context.responseBuffer);
                if (context.bytesWrite >= context.msgSize + Integer.BYTES) {
                    selectionKey.cancel();
                }
            }
        }
    }
}
