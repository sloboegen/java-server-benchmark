package ru.mse.itmo.server.async;

import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;
import ru.mse.itmo.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class ServerAsync extends Server {
    private final AsynchronousServerSocketChannel serverChannel;

    private static class Context {
        static final int MSG_SIZE_NOT_INIT = -1;

        AsynchronousSocketChannel socketChannel;
        ByteBuffer byteBuffer;
        ByteBuffer requestBuffer;
        ByteBuffer responseBuffer;

        boolean isReadMode;
        int getBytes;
        int msgSize;

        Context(AsynchronousSocketChannel socketChannel, ByteBuffer byteBuffer) {
            this.socketChannel = socketChannel;
            this.byteBuffer = byteBuffer;
            toInitState();
        }

        void toInitState() {
            this.isReadMode = true;
            this.getBytes = 0;
            this.msgSize = MSG_SIZE_NOT_INIT;
        }
    }

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
                ByteBuffer clientBuffer = ByteBuffer.allocate(1024);
                Context context = new Context(socketChannel, clientBuffer);
                socketChannel.read(clientBuffer, context, new ReadCompletionHandler());
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

    private class ReadCompletionHandler implements CompletionHandler<Integer, Context> {
        @Override
        public void completed(Integer bytesRead, Context context) {
            try {
                if (bytesRead != -1 && context.isReadMode) {

                    // reading full message size
                    if (context.msgSize == Context.MSG_SIZE_NOT_INIT && context.byteBuffer.position() < Integer.BYTES) {
                        context.socketChannel.read(context.byteBuffer, context, this);
                        return;
                    }

                    int curBytesRead;
                    context.byteBuffer.flip();

                    if (context.msgSize == Context.MSG_SIZE_NOT_INIT) {
                        curBytesRead = context.byteBuffer.limit() - Integer.BYTES;
                        context.msgSize = context.byteBuffer.getInt();
                        context.requestBuffer = ByteBuffer.allocate(context.msgSize);
                    } else {
                        curBytesRead = bytesRead;
                    }

                    context.getBytes += curBytesRead;

                    if (curBytesRead != 0) {
                        byte[] bytes = new byte[curBytesRead];
                        context.byteBuffer.get(bytes, 0, curBytesRead);
                        context.requestBuffer.put(bytes);
                    }

                    context.byteBuffer.clear();

                    if (context.getBytes == context.msgSize) {
                        context.isReadMode = false;
                        context.requestBuffer.flip();
                        Message request = Message.parseFrom(context.requestBuffer);
                        context.requestBuffer.clear();

                        List<Integer> array = request.getArrayList();
                        List<Integer> sortedArray = sortArrayAndRegisterTime(array);

                        Message response = Message.newBuilder().setN(sortedArray.size()).addAllArray(sortedArray).build();
                        int responseSize = response.getSerializedSize();

                        context.responseBuffer = ByteBuffer.allocate(responseSize + Integer.BYTES);
                        context.responseBuffer.putInt(responseSize);
                        context.responseBuffer.put(response.toByteArray());
                        context.responseBuffer.flip();

                        context.socketChannel.write(context.responseBuffer);
                        context.toInitState();
                    }

                    context.socketChannel.read(context.byteBuffer, context, this);
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                stopLatch.countDown();
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable e, Context context) {
            stopLatch.countDown();
            e.printStackTrace();
        }
    }
}
