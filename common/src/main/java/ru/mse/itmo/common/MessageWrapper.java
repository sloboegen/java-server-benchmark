package ru.mse.itmo.common;

import ru.mse.itmo.proto.Message;

public class MessageWrapper {
    public final int msgSize;
    public final Message message;

    public MessageWrapper(int msgSize, Message message) {
        this.msgSize = msgSize;
        this.message = message;
    }
}
