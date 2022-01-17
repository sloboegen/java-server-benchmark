## Java servers benchmark

### Why does it exist?

Dlya dopuska k zachetu.

### Description

This is a simple application for testing different server' architectures:
* Blocking -- one thread for reading and one thread for writing for each client
* NonBlocking -- one selector for reading and one selector for writing for all clients
* Asynchronous -- asynchronous read/write

For all architectures, the common is that client tasks are executed in a thread pool of a fixed size.

For simplicity, all clients work in blocking mode.

### Running

The main class is `ru.mse.itmo.Main`.

After running the `main` function you should choose parameters for benchmark.
When the experiment is over, you will see the message `Benchmark finished`.

Results will be in the folder `reports/ARCH_TYPE/`.
If you need plots you should run from the project root `gnuplot reports/ARCH_TYPE/VAR_PARAMETER.p` where `VAR_PARAMETER` is the name of variable parameter (one of 'array_size', 'client_number', 'request_delta').
Values of the other parameters you can find near with the plot title.

### Prerequisites

* Java (>= 15), gradle.7.1 for building and running
* gnuplot for plot generation
