package org.example;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author macwozni
 */
public class Checker {

    // machine precision epsilon
    static double epsilon = 0.00001;

    /**
     * @throws FileNotFoundException if the give file does not exist we just frow exception - from main subroutine...
     * @throws IOException if there is some problem with IO we just frow exception - from main subroutine...
     */
    public static void check(Matrix input, Matrix output) throws FileNotFoundException, IOException {
        // solve
        //create data structures for solver
        Jama.Matrix A = new Jama.Matrix(input.getMatrix());
        Jama.Matrix b = new Jama.Matrix(input.getRHS());
        // x=a/b
        Jama.Matrix x = A.solve(b);


        double[][] outputMatrix = output.getMatrix();
        double[][] outputRHS = output.getRHS();
        // in processed/solved matrix it should be 1 on diagonal and 0 elsewhere
        for (int i = 0; i < output.size; i++) {
            for (int j = 0; j < output.size; j++) {
                // if diagonal - should be 1.0
                if (i == j) {
                    // if it is not 1.0 - print it to the output and exit
                    if (!Matrix.compare(1., outputMatrix[i][j], epsilon)) {
                        System.out.println("Error 1 " + i + " " + j);
                        System.exit(0);
                    }
                    //if  not diagonal - should be 0.0
                } else if (!Matrix.compare(0., outputMatrix[i][j], epsilon)) {
                    // if it is not 0.0 - print it to the output and exit
                    System.out.println("Error 2 " + i + " " + j);
                    System.exit(0);
                }
            }
        }

        System.out.print(x.getArray()[0][0]);
        for (int j = 1; j < output.size; j++) {
            System.out.print(" ");
            System.out.print(x.getArray()[j][0]);
        }
        System.out.println();

        System.out.print(outputRHS[0][0]);
        for (int j = 1; j < output.size; j++) {
            System.out.print(" ");
            System.out.print(outputRHS[j][0]);
        }
        System.out.println();

        // check RHS vector - should be equal to the one from solved here initial problem
        for (int j = 0; j < output.size; j++) {
            if (!Matrix.compare(x.getArray()[j][0], outputRHS[j][0], epsilon)) {
                // if it is not equal - print it to the output and exit
                System.out.println("Error 3 " + (output.size + 1) + " " + j);
                System.out.println(x.getArray()[j][0] + " " + outputRHS[j][0]);
                System.exit(0);
            }
        }
    }
}
