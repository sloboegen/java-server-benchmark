package ru.mse.itmo;

import ru.mse.itmo.enums.ServerArchitecture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Reporter {
    private final ServerArchitecture architecture;
    private final VariableParameter variableParameter;
    private final List<AtomRunResults> results;

    private final String reportsDir;
    private String dataFile;
    private String gnuplotFile;
    private String plotPng;

    private final int requestCount;
    private final int requestDelta;
    private final int arraySize;
    private final int clientNumber;

    public Reporter(ServerArchitecture architecture,
                    VariableParameter variableParameter,
                    List<AtomRunResults> results,
                    int requestCount,
                    int requestDelta,
                    int arraySize,
                    int clientNumber) {
        this.architecture = architecture;
        this.variableParameter = variableParameter;
        this.results = results;

        this.requestCount = requestCount;
        this.requestDelta = requestDelta;
        this.arraySize = arraySize;
        this.clientNumber = clientNumber;

        switch (architecture) {
            case BLOCKING -> reportsDir = "reports" + File.separator + "blocking";
            case NONBLOCKING -> reportsDir = "reports" + File.separator + "nonblocking";
            case ASYNCHRONOUS -> reportsDir = "reports" + File.separator + "asynchronous";
            default -> throw new RuntimeException("Unsupported server architecture type");
        }

        switch (variableParameter.kind) {
            case REQUEST_DELTA -> initializeFilenames("request_delta");
            case ARRAY_SIZE -> initializeFilenames("array_size");
            case CLIENT_NUMBER -> initializeFilenames("client_number");
            default -> throw new RuntimeException("Unknown variable parameter");
        }
    }

    public void generateReport() {
        try {
            dumpToFile(generateContent(), dataFile);
            dumpToFile(generateGnuplotConfig(), gnuplotFile);
        } catch (IOException e) {
            System.err.println("Error while dumping report");
        }
    }

    private void dumpToFile(String content, String path) throws IOException {
        if (!Files.exists(Path.of(reportsDir))) {
            Files.createDirectory(Path.of(reportsDir));
        }
        Path file = Paths.get(path);
        if (!Files.exists(Path.of(path))) {
            Files.createFile(file);
        }
        Files.writeString(file.toAbsolutePath(), content);
    }

    private void initializeFilenames(String filename) {
        String prefix = reportsDir + File.separator + filename;
        dataFile = prefix + ".csv";
        gnuplotFile = prefix + ".p";
        plotPng = prefix + ".png";
    }

    private String generateContent() {
        StringBuilder content = new StringBuilder();
        content.append("# varP sTime tTime cTime\n");
        content.append("# ----------------\n");
        int curVariableParameter = variableParameter.leftBound;
        for (AtomRunResults result : results) {
            content.append(curVariableParameter).append(", ");
            content.append(String.format("%.3f", result.timeServer)).append(", ");
            content.append(String.format("%.3f", result.timeTask)).append(", ");
            content.append(String.format("%.3f", result.timeClient)).append("\n");
            curVariableParameter += variableParameter.step;
        }
        return content.toString();
    }

    private String generateGnuplotConfig() {
        StringBuilder content = new StringBuilder();
        String title = String.format("archType: %s; requestCount = %d; ", architecture.toString(), requestCount);
        switch (variableParameter.kind) {
            case REQUEST_DELTA -> title += String.format("arraySize = %d; clientNumber = %d", arraySize, clientNumber);
            case ARRAY_SIZE -> title += String.format("requestDelta = %d; clientNumber = %d", requestDelta, clientNumber);
            case CLIENT_NUMBER -> title += String.format("requestDelta = %d; arraySize = %d", requestDelta, arraySize);
        }

        content.append("set term png\n");
        content.append(String.format("set output '%s'\n", plotPng));
        content.append("set key\n");
        content.append(String.format("set xlabel '%s'\n", variableParameter.kind));
        content.append("set ylabel 'time (in millis)'\n");
        content.append(String.format("set title '%s'\n", title));
        content.append(String.format("plot '%s' using 1:2 with linespoints title '%s', ", dataFile, "timeServer"));
        content.append(String.format("'%s' using 1:3 with linespoints title '%s', ", dataFile, "timeTask"));
        content.append(String.format("'%s' using 1:4 with linespoints title '%s'\n", dataFile, "timeClient"));
        return content.toString();
    }
}
