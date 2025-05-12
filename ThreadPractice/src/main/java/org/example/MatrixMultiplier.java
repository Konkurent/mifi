package org.example;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixMultiplier {

    public static int[][] multiply(int[][] a, int[][] b) {
        if (a.length == 0 && b.length == 0) return new int[0][0];
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            return Arrays.stream(a).sequential()
                    .map(it -> new PointMultiplier(it, b))
                    .map(executorService::submit)
                    .map(it -> {
                        try {
                            return it.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).toArray(int[][]::new);
        }
    }

    public static void main(String[] args) {
        int[][] a = {};
        int[][] b = {};
        multiply(a, b);
    }

    private static class PointMultiplier implements Callable<int[]> {

        private final int[] line;
        private final int[][] matrix;

        PointMultiplier(int[] a, int[][] matrix) {
            line = a;
            this.matrix = matrix;
        }

        @Override
        public int[] call() {
            int[] result = new int[matrix[0].length];
            for (int i = 0; i < matrix[0].length; i++) {
                for (int j = 0; j < line.length; j++) {
                    result[i] += line[j] * matrix[j][i];
                }
            }
            return result;
        }
    }

}
