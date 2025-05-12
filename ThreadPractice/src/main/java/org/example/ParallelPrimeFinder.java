package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParallelPrimeFinder {

    @SuppressWarnings("unchecked")
    public static List<Long> execute(int t, long l, long r) throws InterruptedException {
        if (r - l <= 0) return new ArrayList<>();
        long[] source = new long[(int) (r - l)];
        for (int i = 0; i < r - l; i++) {
            source[i] = l + i;
        }
        int arrCount = (int) ((r - l) / t);
        long[][] slices = resolveArraySlice(arrCount, source);
        PrimeFinder[] finders = new PrimeFinder[slices.length];
        for (int i = 0; i < slices.length; i++) {
            finders[i] = new PrimeFinder(slices[i]);
            finders[i].start();
        }
        for (PrimeFinder finder : finders) {
            finder.join();
        }
        return Arrays.stream(finders).flatMap(it -> it.primeValues.stream()).sorted(Long::compareTo).toList();
    }

    private static int resolveSubSize(int totalLength, int partNum, int expectedSubLength) {
        int remainder = totalLength - partNum * expectedSubLength;
        if (remainder > expectedSubLength) {
            return expectedSubLength;
        } else {
            return remainder;
        }
    }

    private static long[][] resolveArraySlice(int sliceArrLength, long[] array) {
        long[][] result;
        if (sliceArrLength == 0) {
            result = new long[1][];
            result[0] = array;
            return result;
        }
        result = new long[sliceArrLength][];
        int expectedLength = array.length / sliceArrLength;
        for (int i = 0; i < sliceArrLength; i++) {
            int remainder = i == sliceArrLength - 1 ? array.length - (i + 1) * expectedLength : 0;
            int actualLength = resolveSubSize(array.length, i, expectedLength) + remainder;
            result[i] = Arrays.copyOfRange(array, i * expectedLength, i * expectedLength + actualLength);
        }
        return result;
    }

    private static class PrimeFinder extends Thread {

        private final long[] subSource;

        public final List<Long> primeValues = new ArrayList<>();

        public PrimeFinder(long[] subSource) {
            this.subSource = subSource;
        }

        @Override
        public void run() {
            for (long value : subSource) {
                if (isPrime(value)) {
                    primeValues.add(value);
                }
            }
        }

        private boolean isPrime(Long numb) {
            for (int i = 2; i < Math.sqrt(numb); i++) {
                if (numb % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }

}
