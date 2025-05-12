package org.example;

import java.util.concurrent.*;

/**
 * Напишите программу, которая использует ForkJoin Framework для подсчета факториала заданного числа.
 * В функцию передается единственное целое число n (0 <= n <= 10^6).
 * Требуется вернуть из данной функции факториал числа n.
 * Гарантируется, что при использовании типа long переполнения не будет.
 */
public class ForkJoinFactorialTask {

    private static final ForkJoinPool executorService = new ForkJoinPool();

    public static long factorial(long n) {
        if (n <= 1) return 1;
        ForkJoinTask<Long> res = executorService.submit(new FactorialResolverTask(n));
        return res.join();
    }


    private static class FactorialResolverTask extends RecursiveTask<Long> {

        private final static long THRESHOLD = 5;

        private final long start;
        private final long end;

        FactorialResolverTask(long start, long end) {
            this.start = start;
            this.end = end;
        }


        FactorialResolverTask(long end) {
            this.start = 1L;
            this.end = end;
        }


        @Override
        protected Long compute() {
            if (start == end) {
                return start;
            } else if (end - start == 1) {
                return start * end;
            } else {
                long mid = (start + end) / 2;
                FactorialResolverTask task1 = new FactorialResolverTask(start, mid);
                FactorialResolverTask task2 = new FactorialResolverTask(mid + 1, end);
                task1.fork();
                return task2.compute() * task1.join();
            }
        }

    }


}
