package ru.mse.itmo.server.async;

import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

public class ServerAsync extends Server {
    private final AsynchronousServerSocketChannel serverChannel;

    public ServerAsync(CountDownLatch stopLatch) throws IOException {
        super(stopLatch);
        serverChannel = AsynchronousServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(Constants.SERVER_ADDRESS, Constants.SERVER_PORT));
    }

    @Override
    public void launch() {
        while (serverChannel.isOpen()) {
            try {
                AsynchronousSocketChannel socketChannel = serverChannel.accept().get();
                ClientContextAsync context = new ClientContextAsync(socketChannel);
                socketChannel.read(context.byteBuffer, context, new ReadCompletionHandler());
            } catch (InterruptedException | ExecutionException e) {
                stopLatch.countDown();
                System.out.println("Asynchronous Server stopped");
            }
        }
    }

    @Override
    public void shutdown() throws IOException {
        serverChannel.close();
        workerPool.shutdown();
    }

    private class ReadCompletionHandler implements CompletionHandler<Integer, ClientContextAsync> {
        @Override
        public void completed(Integer bytesRead, ClientContextAsync context) {
            try {
                if (bytesRead != -1) {
                    if (context.bytesRead == 0) {
                        context.setRequestHandleTime(Instant.now());
                    }

                    context.bytesRead += bytesRead;
                    if (context.isInMsgSizeReading()) {
                        context.socketChannel.read(context.byteBuffer, context, this);
                        return;
                    }

                    context.byteBuffer.flip();
                    if (!context.isInMsgSizeInitialize()) {
                        context.inMsgSize = context.byteBuffer.getInt();
                        context.allocateRequestBuffer();
                        context.putIntoRequestBuffer(context.bytesRead - Integer.BYTES);
                    } else {
                        context.putIntoRequestBuffer(bytesRead);
                    }
                    context.byteBuffer.clear();

                    if (context.isFullMsgRead()) {
                        Message request = context.buildRequestFromBuffer();
                        workerPool.submit(() -> {
                            List<Integer> array = request.getArrayList();
                            List<Integer> sortedArray = sortArrayAndRegisterTime(array);
                            Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();
                            context.outMsgSize = response.getSerializedSize();
                            context.putResponseIntoBuffer(response);
                            context.socketChannel.write(context.responseBuffer, context, new WriteCompletionHandler());
                        });
                        context.invalidateRequest();
                    }
                    // TODO: подумать, мб зависания, если вызвали read, но ничего не прочитали
                    context.socketChannel.read(context.byteBuffer, context, this);
                }
            } catch (IOException | RejectedExecutionException e) {
                stopLatch.countDown();
            }
        }

        @Override
        public void failed(Throwable e, ClientContextAsync context) {
            e.printStackTrace();
            stopLatch.countDown();
        }
    }

    private class WriteCompletionHandler implements CompletionHandler<Integer, ClientContextAsync> {
        @Override
        public void completed(Integer bytesWrite, ClientContextAsync context) {
            context.bytesWrite += bytesWrite;
            if (context.isFullMsgWrite()) {
                context.setResponseSendTime(Instant.now());
                serverTimeMeter.addTimeMeasure(context.getRequestProcessingTime());
            } else {
                context.socketChannel.write(context.responseBuffer, context, this);
            }
        }

        @Override
        public void failed(Throwable e, ClientContextAsync context) {
            e.printStackTrace();
            stopLatch.countDown();
        }
    }
}
