package ru.mse.itmo.common;

import ru.mse.itmo.proto.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageUtils {
    private MessageUtils() {
    }

    public static MessageWrapper readMessage(DataInputStream inputStream) throws IOException {
        int msgSize = inputStream.readInt();
        byte[] buffer = new byte[msgSize];
        inputStream.readFully(buffer, 0, msgSize);
        Message message = Message.parseFrom(buffer);
        return new MessageWrapper(msgSize, message);
    }

    public static void writeMessage(DataOutputStream outputStream, Message message) throws IOException {
        int msgSize = message.getSerializedSize();
        outputStream.writeInt(msgSize);
        outputStream.write(message.toByteArray());
        outputStream.flush();
    }
}
