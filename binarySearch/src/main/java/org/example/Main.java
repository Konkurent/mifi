package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.example.Main.MatrixHash.calcHash;

public class Main {
    public static void main(String[] args) {
        List<List<Integer>> matrix = new ArrayList<>();
        matrix.add(List.of(6, 8, 5, 4));
        matrix.add(List.of(7, 12, 5, 7));
        matrix.add(List.of(12, 8, 9, 1));

        System.out.println(calcHash(matrix, 1000, 6, 7));
    }

    public class MatrixHash {
        // Метод для вычисления степени
        public static long power(int x, int n) {
            long res = 1;
            for (int i = 0; i < n; i++) {
                res *= x;
            }
            return res;
        }

        // Метод для вычисления хеша
        public static int calcHash(List<List<Integer>> matrix, int mod, int p, int q) {
            long hash = 0;
            int n = matrix.size();
            for (int i = 0; i < n; i++) {
                int m = matrix.get(i).size(); // все m, по всем итерациям, должны быть равны
                for (int j = 0; j < m; j++) {
                    hash += power(p, i) * power(q, j) * matrix.get(i).get(j);
                    System.out.println(p + " " + i + " " + q + " " + j + " " + matrix.get(i).get(j));
                    System.out.println("+   " + power(p, i) * power(q, j) * matrix.get(i).get(j));
                    hash %= mod;
                }
            }
            return (int) hash;
        }
    }

}