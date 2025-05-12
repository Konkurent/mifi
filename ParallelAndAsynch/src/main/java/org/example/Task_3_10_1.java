package org.example;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Условие
 * Вам необходимо реализовать асинхронное вычисление формулы с помощью CompletableFuture и его функционала по комбинированию и асинхронному выполнению задач:
 */
public class Task_3_10_1 {


    public static void main(String[] args) {
        CompletableFuture.supplyAsync(Task_3_10_1::scanInput)
                .thenCompose(Task_3_10_1::calculate)
                .exceptionally(error -> Double.NaN)
                .thenApply(String::valueOf)
                .thenApply("Final result of the formula: "::concat)
                .thenAccept(System.out::println)
                .join();
    }

    private static CompletableFuture<Double> calculate(Double[] input) {
        CompletableFuture<Double> sumOfSquare = CompletableFuture.supplyAsync(() -> sumSquare(input[0], input[1]));
        CompletableFuture<Double> log = CompletableFuture.supplyAsync(() -> log(input[2]));
        CompletableFuture<Double> sqrt = CompletableFuture.supplyAsync(() -> sqrt(input[3]));
        return sumOfSquare.thenCombine(log, (s1, s2) -> s1 * s2)
                .thenCombine(sqrt, (s1, s2) -> s1 / s2);
    }


    private static Double[] scanInput() {
        try {
            System.out.println("Введите a, b, c и d в одну строку. Допускается наличие дробной части (_.__) " + Thread.currentThread().getName());
            String[] input = new Scanner(System.in).nextLine().trim().split("\\s+");
            if (input.length != 4) throw new IllegalArgumentException("Неверные входные данные");
            return Arrays.stream(input).map(Double::parseDouble).toArray(Double[]::new);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.out.println("Попробуем еще разок");
            return scanInput();
//            throw e;
        }
    }

    private static CompletableFuture<Double> sumSquare(Double[] input) {
        return CompletableFuture.supplyAsync(() -> sumSquare(input[0], input[1]));
    }

    private static Double sumSquare(Double a, Double b) {
        System.out.println(Thread.currentThread().getName() + " sumSquare");
        double result = Double.NaN;
        try {
            Thread.sleep(Duration.of(5, ChronoUnit.SECONDS));
            result = Math.pow(a, 2) + Math.pow(b, 2);
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Calculating sum of squares: " + result);
        }
    }

    private static Double log(Double c) {
        System.out.println(Thread.currentThread().getName() + " log");
        double result = Double.NaN;
        try {
            Thread.sleep(Duration.of(15, ChronoUnit.SECONDS));
            result = Math.log(c);
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Calculating log(c): " + result);
        }
    }

    private static Double sqrt(Double d) {
        System.out.println(Thread.currentThread().getName() + " sqrt");
        double res = Double.NaN;
        try {
            Thread.sleep(Duration.of(10, ChronoUnit.SECONDS));
            res = Math.sqrt(d);
            return res;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Calculating sqrt(d): " + res);
        }
    }

}
