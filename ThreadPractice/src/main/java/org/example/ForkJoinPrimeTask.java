package org.example;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

/**
 * На вход программе подается два числа l и r (0 <= l <= r <= 10^6).
 * Требуется написать функцию, которая с помощью ForkJoin посчитает и вернет единственное число — количество простых чисел на отрезке [l;r] (обе границы включены).
 */
public class ForkJoinPrimeTask {
    public static long countPrimes(long l, long r) {
        // Ваше решение
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new PrimeTask(l, r));
    }

    public static void main(String[] args) {
        System.out.println(countPrimes(0, 0));
    }

    private static class PrimeTask extends RecursiveTask<Long> {

        private final static long THRESHOLD = 10;

        private final long start;
        private final long end;

        private PrimeTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            long length = end - start + 1;
            if (length < THRESHOLD) {
                return LongStream.rangeClosed(start, end).filter(PrimeTask::isPrime).count();
            } else {
                long mid = (start + end) / 2;
                PrimeTask left = new PrimeTask(start, mid);
                PrimeTask right = new PrimeTask(mid + 1, end);
                left.fork();
                right.fork();
                return left.join() + right.join();
            }
        }

        public static boolean isPrime(Long numb) {
            if (numb <= 1) {
                return false;
            }
            if (numb <= 3) return true;
            for (int i = 2; i <= Math.sqrt(numb); i++) {
                if (numb % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }

}
