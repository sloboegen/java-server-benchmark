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
        Message message = Message.parseDelimitedFrom(inputStream);
        return new MessageWrapper(msgSize, message);
    }

    public static void writeMessage(DataOutputStream outputStream, Message message) throws IOException {
        int msgSize = message.getSerializedSize();
        outputStream.writeInt(msgSize);
        message.writeDelimitedTo(outputStream);
        outputStream.flush();
    }
}
