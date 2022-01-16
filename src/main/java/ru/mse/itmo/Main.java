package ru.mse.itmo;

import ru.mse.itmo.enums.ServerArchitecture;
import ru.mse.itmo.enums.VariableParameterEnum;

import java.util.Scanner;

public class Main {
    private static final boolean RELEASE = true;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("Welcome to the application for testing different server architectures");
        System.out.println("Firstly you should choose the architecture.");
        System.out.println("Type `1` to choose blocking architecture");
        System.out.println("Type `2` to choose non-blocking architecture");
        System.out.println("Type `3` to choose asynchronous architecture");

        int architectureInt = 3;
        if (RELEASE) {
            architectureInt = in.nextInt();
        }
        ServerArchitecture architecture = ServerArchitecture.ofInt(architectureInt);

        System.out.println("Enter the number of requests for each client");
        int requestNumber = 20;
        if (RELEASE) {
            requestNumber = in.nextInt();
        }

        System.out.println("Choose the variable parameter");
        System.out.println("Type `1` to vary request' delta (in millis)");
        System.out.println("Type `2` to vary array size");
        System.out.println("Type `3` to vary client number");

        int variableParameterInt = 1;
        if (RELEASE) {
            variableParameterInt = in.nextInt();
        }

        VariableParameterEnum variableParameterEnum = VariableParameterEnum.ofInt(variableParameterInt);

        System.out.println("Enter the left bound for this variable");
        int variableBoundLeft = 5;
        if (RELEASE) {
            variableBoundLeft = in.nextInt();
        }

        System.out.println("Enter the right bound for this variable");
        int variableBoundRight = 500;
        if (RELEASE) {
            variableBoundRight = in.nextInt();
        }

        System.out.println("Enter the step with which this parameter will change");
        int variableStep = 50;
        if (RELEASE) {
            variableStep = in.nextInt();
        }

        int requestDelta = -1;
        int arraySize = -1;
        int clientNumber = -1;

        if (variableParameterEnum != VariableParameterEnum.REQUEST_DELTA) {
            System.out.println("Enter the request' delta (in millis)");
            requestDelta = 100;
            if (RELEASE) {
                requestDelta = in.nextInt();
            }
        }

        if (variableParameterEnum != VariableParameterEnum.ARRAY_SIZE) {
            System.out.println("Enter the array size");
            arraySize = 100;
            if (RELEASE) {
                arraySize = in.nextInt();
            }
        }

        if (variableParameterEnum != VariableParameterEnum.CLIENT_NUMBER) {
            System.out.println("Enter the client number");
            clientNumber = 10;
            if (RELEASE) {
                clientNumber = in.nextInt();
            }
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
