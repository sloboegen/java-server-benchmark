package ru.mse.itmo.server.nonblocking;

import ru.mse.itmo.server.ClientContextBase;

import java.nio.channels.SocketChannel;

public class ClientContextNB extends ClientContextBase {
    public final SocketChannel socketChannel;

    public ClientContextNB(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
