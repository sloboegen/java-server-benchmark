package ru.mse.itmo.enums;

public enum ServerArchitecture {
    BLOCKING,
    NONBLOCKING,
    ASYNCHRONOUS;

    public static ServerArchitecture ofInt(int n) {
        switch (n) {
            case 1 -> {
                return BLOCKING;
            }
            case 2 -> {
                return NONBLOCKING;
            }
            case 3 -> {
                return ASYNCHRONOUS;
            }
            default -> {
                return null;
            }
        }
    }
}
