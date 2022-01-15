package ru.mse.itmo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerBenchmark {
    private final ServerArchitecture architecture;
    private final int requestNumber;
    private int requestDelta;
    private int arraySize;
    private int clientNumber;

    private final VariableParameter variableParameter;
    private final int variableBoundLeft;
    private final int variableBoundRight;
    private final int variableStep;

    private final List<AtomRunResults> results = new ArrayList<>();

    public ServerBenchmark(ServerArchitecture architecture,
                           int requestNumber,
                           int requestDelta,
                           int arraySize,
                           int clientNumber,
                           VariableParameter variableParameter,
                           int variableBoundLeft,
                           int variableBoundRight,
                           int variableStep) {
        this.architecture = architecture;
        this.requestNumber = requestNumber;
        this.requestDelta = requestDelta;
        this.arraySize = arraySize;
        this.clientNumber = clientNumber;

        this.variableParameter = variableParameter;
        this.variableBoundLeft = variableBoundLeft;
        this.variableBoundRight = variableBoundRight;
        this.variableStep = variableStep;
    }

    public void runBenchmark() {
        switch (variableParameter) {
            case REQUEST_DELTA -> runVariableRequestDelta();
            case ARRAY_SIZE -> runVariableArraySize();
            case CLIENT_NUMBER -> runVariableClientNumber();
            default -> throw new RuntimeException("Unknown variable parameter");
        }
    }

    public void showResults() {
        for (AtomRunResults result : results) {
            System.out.println("Client Time: " + result.timeClient);
        }
    }

    private void runVariableRequestDelta() {
        requestDelta = variableBoundLeft;
        while (requestDelta <= variableBoundRight) {
            doIteration();
            requestDelta += variableStep;
        }
    }

    private void runVariableArraySize() {
        arraySize = variableBoundLeft;
        while (arraySize <= variableBoundRight) {
            doIteration();
            arraySize += variableStep;
        }
    }

    private void runVariableClientNumber() {
        clientNumber = variableBoundLeft;
        while (clientNumber <= variableBoundRight) {
            doIteration();
            clientNumber += variableStep;
        }
    }

    private void doIteration() {
        System.out.println(
                "*** Running with " +
                        "requestDelta = " + requestDelta +
                        "; arraySize = " + arraySize +
                        "; clientNumber = " + clientNumber + " ***"
        );
        results.add(atomRun());
    }

    private AtomRunResults atomRun() {
        try {
            AtomRunner atomRunner = new AtomRunner(architecture, requestNumber, requestDelta, arraySize, clientNumber);
            return atomRunner.run();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
