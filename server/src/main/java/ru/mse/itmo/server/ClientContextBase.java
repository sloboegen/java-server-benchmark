package ru.mse.itmo.server;

import ru.mse.itmo.common.Constants;
import ru.mse.itmo.proto.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public abstract class ClientContextBase {
    public final ByteBuffer byteBuffer;
    public ByteBuffer requestBuffer;
    public ByteBuffer responseBuffer;

    public int bytesRead;
    public int bytesWrite;
    public Integer inMsgSize;
    public Integer outMsgSize;

    private Instant requestHandleTime = null;
    private Instant responseSendTime = null;

    public ClientContextBase() {
        byteBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        invalidateRequest();
        invalidateResponse();
    }

    public void invalidateRequest() {
        bytesRead = 0;
        inMsgSize = null;
    }

    public void invalidateResponse() {
        bytesWrite = 0;
        outMsgSize = null;
    }

    public boolean isInMsgSizeInitialize() {
        return inMsgSize != null;
    }

    public boolean isOutMsgSizeInitialize() {
        return outMsgSize != null;
    }

    public boolean isInMsgSizeReading() {
        return bytesRead <= Integer.BYTES;
    }

    public boolean isFullMsgRead() {
        return isInMsgSizeInitialize() && bytesRead == inMsgSize + Integer.BYTES;
    }

    public boolean isFullMsgWrite() {
        boolean result = isOutMsgSizeInitialize() && bytesWrite >= outMsgSize + Integer.BYTES;
        if (result) {
            invalidateResponse();
        }
        return result;
    }

    public void allocateRequestBuffer() {
        requestBuffer = ByteBuffer.allocate(inMsgSize);
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
        responseBuffer.flip();
    }

    public Message buildRequestFromBuffer() throws IOException {
        assert isFullMsgRead();
        requestBuffer.flip();
        return Message.parseFrom(requestBuffer);
    }

    public void setRequestHandleTime(Instant time) {
        requestHandleTime = time;
    }

    public void setResponseSendTime(Instant time) {
        responseSendTime = time;
    }

    public Duration getRequestProcessingTime() {
        assert requestHandleTime != null;
        assert responseSendTime != null;
        return Duration.between(requestHandleTime, responseSendTime);
    }
}
