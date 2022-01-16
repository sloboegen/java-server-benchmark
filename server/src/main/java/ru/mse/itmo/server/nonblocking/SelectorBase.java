package ru.mse.itmo.server.nonblocking;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public abstract class SelectorBase implements Runnable {
    private final Selector selector;
    private final int selectionKeyType;
    private final CountDownLatch stopLatch;

    protected final ConcurrentLinkedQueue<ClientContext> registrationQueue = new ConcurrentLinkedQueue<>();

    protected SelectorBase(int selectionKeyType, CountDownLatch stopLatch) throws IOException {
        selector = Selector.open();
        this.stopLatch = stopLatch;
        this.selectionKeyType = selectionKeyType;
    }

    @Override
    public final void run() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    processSelectionKey(selectionKey);
                    iterator.remove();
                }
                registerNew();
            }
        } catch (IOException e) {
            stopLatch.countDown();
            e.printStackTrace();
        }
    }

    public final void addToRegistrationQueue(ClientContext context) {
        registrationQueue.add(context);
    }

    public final void wakeup() {
        selector.wakeup();
    }

    public final void close() throws IOException {
        selector.close();
    }

    protected abstract void processSelectionKey(SelectionKey selectionKey) throws IOException;

    private void registerNew() throws ClosedChannelException {
        while (!registrationQueue.isEmpty()) {
            ClientContext context = registrationQueue.poll();
            context.socketChannel.register(selector, selectionKeyType, context);
        }
    }
}
