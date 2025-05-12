package org.example;

import java.math.BigInteger;
import java.util.Arrays;

public class ParallelFactorial {

    public static BigInteger execute(int t, int n) throws InterruptedException {
        int[][] subFactorialSources = new int[t][];

        int subLength = n / t;

        for (int i = 0; i < t; i++) {
            int size = i == subFactorialSources.length - 1 ? n : subLength;
            subFactorialSources[i] = buildSequence(size, subLength * i);
            n -= subLength;
        }

        FactorialValueResolver[] factorialValueResolvers = new FactorialValueResolver[t];

        for (int i = 0; i < t; i++) {
            factorialValueResolvers[i] = new FactorialValueResolver(subFactorialSources[i]);
            factorialValueResolvers[i].start();
        }

        for (FactorialValueResolver valueResolver: factorialValueResolvers) {
            valueResolver.join();
        }

        return Arrays.stream(factorialValueResolvers).map(FactorialValueResolver::getSubValue).reduce(BigInteger::multiply).orElse(BigInteger.ONE);
    }

    private static int[] buildSequence(int size, int startValue) {
        int[] sequence = new int[size];
        for (int i = 0; i < size; i++) {
            sequence[i] = startValue + i + 1;
        }
        return sequence;
    }

    public static class FactorialValueResolver extends Thread {

        private final int[] factorialSlice;

        private BigInteger result = BigInteger.ONE;

        public FactorialValueResolver(int[] factorialSlice) {
            this.factorialSlice = factorialSlice;
        }

        public BigInteger getSubValue() {
            return result;
        }

        @Override
        public void run() {
            for (int i : factorialSlice) {
                result = result.multiply(BigInteger.valueOf(i));
            }
        }
    }

}
