package org.example;

import Jama.LUDecomposition;
import Jama.Matrix;

import java.io.*;

/**
 * @author macwozni
 */
public class Generator {

    // machine precision epsilon
    static double epsilon = 0.00001;

    public static org.example.Matrix generateMatrix(int n, String inputFilename) throws FileNotFoundException, IOException {

        // generate random system of equations
        // LHS
        Matrix A = Matrix.random(n, n);
        // RHS
        Matrix B = Matrix.random(n, 1);
        // try to solve system of equations
        LUDecomposition lu = A.lu();
        // check if it is non singular
        boolean nonSingular = lu.isNonsingular();
        // if it is non singular - check if it requires pivot during gaussian elimination
        if (nonSingular){
            nonSingular = requiresPivot(A.getArray(), n);
        }
        // if it is non singular or requires pivot try to generate another system
        // until we find something that meets our requirements
        while (!nonSingular) {
            // generate random system of equations
            A = Matrix.random(n, n);
            // try to solve system of equations
            lu = A.lu();
            // check if it is non singular
            nonSingular = lu.isNonsingular();
            // if it is non singular - check if it requires pivot during gaussian elimination
            if (nonSingular){
                nonSingular = requiresPivot(A.getArray(), n);
            }
        }

        // save unsolved system of equation to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFilename))) {
            writer.write(Integer.toString(n));
            writer.newLine();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    writer.write(A.getArray()[i][j] + " ");
                }
                writer.newLine();
            }
            for (int j = 0; j < n; j++) {
                writer.write(B.getArray()[j][0] + " ");
            }
            System.out.println("Generated input saved to file: " + inputFilename);
        } catch (IOException e) {
            System.out.println("An error occurred while saving generated input to file: " + inputFilename);
            e.printStackTrace();
        }

        return new org.example.Matrix(n, A.getArray(), B.getArray());
    }

    /**
     * @param m matrix for gaussian elimination
     * @param size size of the matrix
     * @return true if matrix requires pivoting during gaussian elimination
     * This subroutine checks if matrix requires pivoting during simple gaussian elimination.
     */
    static boolean requiresPivot(double m[][], int size) {
        // make a local copy - just in case
        double[][] matrix = m;

        // for each row
        for (int i = 0; i < size; i++) {
            // check if we have 0.0 on diagonal
            if (org.example.Matrix.compare(0., matrix[i][i], epsilon)) {
                // if yes - return true
                return true;
            }
            // for each row below
            for (int j=i+1; j<size; j++){
                // compute mulitplier
                double n = matrix[j][i]/matrix[i][i];
                // subtract each element of one row from another
                for (int k=0; k<size; k++){
                    matrix[j][k] = matrix[j][k] - matrix[i][k]*n;
                }
            }
        }

        // now we know, that pivoting is not required during elimination
        return false;
    }
}