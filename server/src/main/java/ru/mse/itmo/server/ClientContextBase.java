package ru.mse.itmo.server;

import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ClientContextBase {
    public final ByteBuffer byteBuffer;
    public ByteBuffer requestBuffer;
    public ByteBuffer responseBuffer;

    public int bytesRead;
    public int bytesWrite;
    public Integer msgSize;

    public ClientContextBase() {
        byteBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        resetContext();
    }

    public void resetContext() {
        bytesRead = 0;
        bytesWrite = 0;
        msgSize = null;
    }

    public boolean isMsgSizeInitialize() {
        return msgSize != null;
    }

    public boolean isMsgSizeReading() {
        return bytesRead <= Integer.BYTES;
    }

    public boolean isFullMsgRead() {
        return isMsgSizeInitialize() && bytesRead == msgSize + Integer.BYTES;
    }

    public void putIntoRequestBuffer(int bytesRead) {
        byte[] bytes = new byte[bytesRead];
        byteBuffer.get(bytes, 0, bytesRead);
        requestBuffer.put(bytes);
    }

    public void putResponseIntoBuffer(Message response) {
        int responseSize = response.getSerializedSize();
        responseBuffer = ByteBuffer.allocate(responseSize + Integer.BYTES);
        responseBuffer.putInt(responseSize);
        responseBuffer.put(response.toByteArray());
    }

    public Message buildRequestFromBuffer() throws IOException {
        assert isFullMsgRead();
        requestBuffer.flip();
        return Message.parseFrom(requestBuffer);
    }
}
