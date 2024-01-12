package org.example;

import java.util.Arrays;

class Matrix {
    int size;
    double[][] matrix;
    double[][] RHS;

    Matrix(int size, double[][] matrix, double[][] RHS) {
        this.size = size;
        this.matrix = matrix;
        this.RHS = RHS;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(double[][] matrix) {
        this.matrix = matrix;
    }

    public double[][] getRHS() {
        return RHS;
    }

    public void setRHS(double[][] RHS) {
        this.RHS = RHS;
    }

    @Override
    public String toString() {
        StringBuilder sb2DMatrix = new StringBuilder();
        sb2DMatrix.append("[\n\t\t");
        for (double[] row : matrix) {
            sb2DMatrix.append(Arrays.toString(row));
            sb2DMatrix.append("\n\t\t");
        }
        sb2DMatrix.append("]");

        StringBuilder sb2DRHS = new StringBuilder();
        sb2DRHS.append("[");
        for (double[] rh : RHS) {
            sb2DRHS.append(rh[0]);
            sb2DRHS.append(", ");
        }
        sb2DRHS.append("]");

        return "Matrix:" +
                "\n\tsize=" + size +
                ",\n\tmatrix=" + sb2DMatrix +
                ",\n\tRHS=" + sb2DRHS;
    }

    /**
     * @param a first variable for comparisson
     * @param b second variable for comparisson
     * @param epsilon machine precission for floating point
     * @return true if equals or within bounds of epsilon precission
     */
    static boolean compare(double a, double b, double epsilon) {
        double c = Math.abs(a - b);
        return c < epsilon;
    }
}