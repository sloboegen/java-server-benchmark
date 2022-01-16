package ru.mse.itmo.server.nonblocking;

import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
                selectorRead.addToRegistrationQueue(new ClientContextNB(socketChannel));
                selectorRead.wakeup();
            }
        } catch (IOException e) {
            stopLatch.countDown();
        }
    }

    @Override
    public void shutdown() throws IOException {
        serverChannel.close();
        selectorRead.close();
        selectorWrite.close();
        workerPool.shutdown();
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
                ClientContextNB context = (ClientContextNB) selectionKey.attachment();
                int bytesRead = context.socketChannel.read(context.byteBuffer);

                if (bytesRead > 0) {
                    if (context.bytesRead == 0) {
                        context.setRequestHandleTime(Instant.now());
                    }

                    context.bytesRead += bytesRead;
                    if (!context.isInMsgSizeInitialize()) {
                        if (context.bytesRead > Integer.BYTES) {
                            context.byteBuffer.flip();
                            context.inMsgSize = context.byteBuffer.getInt();
                            context.allocateRequestBuffer();
                            context.putIntoRequestBuffer(context.bytesRead - Integer.BYTES);
                            context.byteBuffer.clear();
                        }
                    } else {
                        context.byteBuffer.flip();
                        context.putIntoRequestBuffer(bytesRead);
                        context.byteBuffer.clear();
                    }

                    if (context.isInMsgSizeInitialize() && context.bytesRead == context.inMsgSize + Integer.BYTES) {
                        Message request = context.buildRequestFromBuffer();
                        List<Integer> array = request.getArrayList();
                        sendTaskForExecution(context, array);
                    }
                }
            }
        }

        private void sendTaskForExecution(ClientContextNB context, List<Integer> array) {
            workerPool.submit(() -> {
                try {
                    List<Integer> sortedArray = sortArrayAndRegisterTime(array);
                    Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();

                    context.putResponseIntoBuffer(response);
                    context.invalidateRequest();
                    context.outMsgSize = response.getSerializedSize();

                    selectorWrite.addToRegistrationQueue(context);
                    selectorWrite.wakeup();
                } catch (ExecutionException | InterruptedException e) {
                    stopLatch.countDown();
                }
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
                ClientContextNB context = (ClientContextNB) selectionKey.attachment();
                context.bytesWrite += context.socketChannel.write(context.responseBuffer);
                if (context.isFullMsgWrite()) {
                    context.setResponseSendTime(Instant.now());
                    serverTimeMeter.addTimeMeasure(context.getRequestProcessingTime());
                    selectionKey.cancel();
                }
            }
        }
    }
}