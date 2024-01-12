package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.Scanner;

public class InputReader {
    static Optional<Matrix> readInput(String inputFilename) {
        try {
            File file = new File(inputFilename);
            Scanner scanner = new Scanner(file);

            int size = scanner.nextInt();
            double[][] matrix = new double[size][size];
            double[][] RHS = new double[size][1];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = scanner.nextDouble();
                }
            }

            for (int i = 0; i < size; i++) {
                RHS[i][0] = scanner.nextDouble();
            }

            scanner.close();

            if(size == 0) {
                System.out.println("Matrix is empty");
                System.exit(1);
            }

            return Optional.of(new Matrix(size, matrix, RHS));

        } catch (FileNotFoundException e) {
            System.out.println(inputFilename + " file not found.");
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
