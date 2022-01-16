package ru.mse.itmo.server.async;

import ru.mse.itmo.server.ClientContextBase;

import java.nio.channels.AsynchronousSocketChannel;

public class ClientContextAsync extends ClientContextBase {
    public final AsynchronousSocketChannel socketChannel;

    public ClientContextAsync(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
