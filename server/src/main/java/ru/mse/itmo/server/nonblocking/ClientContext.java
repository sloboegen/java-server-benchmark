package ru.mse.itmo.server.nonblocking;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientContext {
    public final SocketChannel socketChannel;

    public ByteBuffer byteBuffer;
    public ByteBuffer requestBuffer;
    public ByteBuffer responseBuffer;

    public int msgSize;
    public int bytesRead;
    public int bytesWrite;


    public ClientContext(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        toInitState();
    }

    public void toInitState() {
        byteBuffer = ByteBuffer.allocate(1024);
        msgSize = -1;
        bytesRead = 0;
        bytesWrite = 0;
    }

    public boolean isMsgSizeInitialize() {
        return msgSize != -1;
    }
}
