package ru.mse.itmo;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
//        Scanner in = new Scanner(System.in);

        System.out.println("Hello! Welcome to the best application for testing different server architectures");
        System.out.println("Firstly you should choose the architecture.");
        System.out.println("Type `1` to choose blocking architecture");
//        int archType = in.nextInt();
//
//        System.out.println("Enter the number of requests for each client");
//        int x = in.nextInt();
        // TODO: выбрать параметр, который будет меняться, его диапазон и шаг

//        System.out.println("Enter the delta:");
//        int delta = in.nextInt();
//
//        System.out.println("Enter N:");
//        int n = in.nextInt();
//
//        System.out.println("Enter M:");
//        int m = in.nextInt();

        int archType = 1;
        int x = 1;
        int delta = 100;
        int n = 10;
        int m = 1;

        ServerTester serverTester = new ServerTester(archType, x, delta, n, m);
        serverTester.doExperiment();
    }
}
