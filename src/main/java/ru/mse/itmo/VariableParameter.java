package ru.mse.itmo;

public enum VariableParameter {
    REQUEST_DELTA,
    ARRAY_SIZE,
    CLIENT_NUMBER;

    public static VariableParameter ofInt(int n) {
        switch (n) {
            case 1 -> {
                return REQUEST_DELTA;
            }
            case 2 -> {
                return ARRAY_SIZE;
            }
            case 3 -> {
                return CLIENT_NUMBER;
            }
            default -> {
                return null;
            }
        }
    }
}
