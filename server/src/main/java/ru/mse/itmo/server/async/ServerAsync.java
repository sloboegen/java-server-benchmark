package ru.mse.itmo.server.async;

import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

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
                System.out.println("AsyncServer end");
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
                        List<Integer> array = request.getArrayList();
                        List<Integer> sortedArray = sortArrayAndRegisterTime(array);

                        Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();
                        context.putResponseIntoBuffer(response);
                        context.socketChannel.write(context.responseBuffer);
                        context.resetContext();
                    }
                    context.socketChannel.read(context.byteBuffer, context, this);
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                stopLatch.countDown();
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable e, ClientContextAsync context) {
            stopLatch.countDown();
            e.printStackTrace();
        }
    }
}
