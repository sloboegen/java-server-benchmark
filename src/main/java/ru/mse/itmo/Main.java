package ru.mse.itmo;

import ru.mse.itmo.enums.ServerArchitecture;
import ru.mse.itmo.enums.VariableParameterEnum;

public class Main {
    public static void main(String[] args) {
//        Scanner in = new Scanner(System.in);

        System.out.println("Welcome to the application for testing different server architectures");
        System.out.println("Firstly you should choose the architecture.");
        System.out.println("Type `1` to choose blocking architecture");
        System.out.println("Type `2` to choose non-blocking architecture");
        System.out.println("Type `3` to choose asynchronous architecture");

        final int architectureInt = 1;
        ServerArchitecture architecture = ServerArchitecture.ofInt(architectureInt);

        System.out.println("Enter the number of requests for each client");
        final int requestNumber = 10;

        System.out.println("Choose the variable parameter");
        System.out.println("Type `1` to vary request' delta (in millis)");
        System.out.println("Type `2` to vary array size");
        System.out.println("Type `3` to vary client number");

        final int variableParameterInt = 3;
        VariableParameterEnum variableParameterEnum = VariableParameterEnum.ofInt(variableParameterInt);

        System.out.println("Enter the left bound for this variable");
        final int variableBoundLeft = 1;

        System.out.println("Enter the right bound for this variable");
        final int variableBoundRight = 3;

        System.out.println("Enter the step with which this parameter will change");
        final int variableStep = 1;

        int requestDelta = -1;
        int arraySize = -1;
        int clientNumber = -1;

        if (variableParameterEnum != VariableParameterEnum.REQUEST_DELTA) {
            System.out.println("Enter the request' delta (in millis)");
            requestDelta = 100;
        }

        if (variableParameterEnum != VariableParameterEnum.ARRAY_SIZE) {
            System.out.println("Enter the array size");
            arraySize = 100;
        }

        if (variableParameterEnum != VariableParameterEnum.CLIENT_NUMBER) {
            System.out.println("Enter the client number");
            clientNumber = 5;
        }

        System.out.println("================================");
        System.out.println("Benchmark starting...");
        System.out.println("================================");

        ServerBenchmark benchmark = new ServerBenchmark(
                architecture,
                requestNumber,
                requestDelta,
                arraySize,
                clientNumber,
                new VariableParameter(variableParameterEnum, variableBoundLeft, variableBoundRight, variableStep)
        );

        benchmark.runBenchmark();

        System.out.println("================================");
        System.out.println("Benchmark finished");
        System.out.println("================================");

        benchmark.saveResults();
    }
}
