package ru.mse.itmo.enums;

public enum VariableParameterEnum {
    REQUEST_DELTA,
    ARRAY_SIZE,
    CLIENT_NUMBER;

    public static VariableParameterEnum ofInt(int n) {
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

    @Override
    public String toString() {
        switch (this) {
            case REQUEST_DELTA -> {
                return "Request delta";
            }
            case ARRAY_SIZE -> {
                return "Array size";
            }
            case CLIENT_NUMBER -> {
                return "Client number";
            }
        }
        return "";
    }
}
