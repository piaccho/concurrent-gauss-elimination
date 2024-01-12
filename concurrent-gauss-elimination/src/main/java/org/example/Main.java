package org.example;
import java.io.IOException;
import java.util.*;

public class Main {
    static String inputFilename = null;
    static String outputFilename = null;
    static Integer N = null;
    static Matrix input = null;

    public static void main(String[] args) throws InterruptedException, IOException {
        parseArgs(args);
        getInput();

        ConcurrentGaussElimination CGE = new ConcurrentGaussElimination(input);

        CGE.createModel();
        CGE.solveWithSchedulers(outputFilename);

        System.out.println("\nCHECKER by macwozni:");
        Checker.check(input, CGE.getMatrix());
    }

    private static void parseArgs(String[] args) {
        if (args.length == 0) {
            inputFilename = "input.txt";
            outputFilename = "output.txt";
        } else if (args.length <= 3) {
            inputFilename = args[0];
            outputFilename = args[1];
            if (args.length == 3) {
                try {
                    N = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Wrong third argument.");
                    System.exit(1);
                }
            }
        } else {
            System.out.println("Wrong number of arguments.");
            System.exit(1);
        }
    }

    public static void getInput() throws IOException {
        if (N != null) {
            System.out.println("\nGENERATOR by macwozni:");
            input = Generator.generateMatrix(N, inputFilename);
        } else {
            Optional<Matrix> optionalInput = InputReader.readInput(inputFilename);
            if (optionalInput.isEmpty()) {
                System.out.println("Reading file failed.");
                System.exit(1);
            }
            input = optionalInput.get();
        }
        System.out.println(input);
    }
}