package ru.mse.itmo;

import ru.mse.itmo.enums.ServerArchitecture;

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

    private final List<AtomRunResults> results = new ArrayList<>();

    public ServerBenchmark(ServerArchitecture architecture,
                           int requestNumber,
                           int requestDelta,
                           int arraySize,
                           int clientNumber,
                           VariableParameter variableParameter) {
        this.architecture = architecture;
        this.requestNumber = requestNumber;
        this.requestDelta = requestDelta;
        this.arraySize = arraySize;
        this.clientNumber = clientNumber;

        this.variableParameter = variableParameter;
    }

    public void runBenchmark() {
        switch (variableParameter.kind) {
            case REQUEST_DELTA -> runVariableRequestDelta();
            case ARRAY_SIZE -> runVariableArraySize();
            case CLIENT_NUMBER -> runVariableClientNumber();
            default -> throw new RuntimeException("Unknown variable parameter");
        }
    }

    public void saveResults() {
        new Reporter(
                architecture,
                variableParameter,
                results,
                requestDelta,
                arraySize,
                clientNumber).generateReport();
    }

    private void runVariableRequestDelta() {
        requestDelta = variableParameter.leftBound;
        while (requestDelta <= variableParameter.rightBound) {
            doIteration();
            requestDelta += variableParameter.step;
        }
    }

    private void runVariableArraySize() {
        arraySize = variableParameter.leftBound;
        while (arraySize <= variableParameter.rightBound) {
            doIteration();
            arraySize += variableParameter.step;
        }
    }

    private void runVariableClientNumber() {
        clientNumber = variableParameter.leftBound;
        while (clientNumber <= variableParameter.rightBound) {
            doIteration();
            clientNumber += variableParameter.step;
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
