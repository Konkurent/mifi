
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

class MatrixTransposer {
    public static int[][] transpose(int[][] a) {
        if (a.length == 0) return a;
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            return IntStream.range(0, a[0].length).mapToObj(index -> new LineTransposer(index, a)).map(executorService::submit).map(it -> {
                try {
                    return it.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(int[][]::new);
        }
    }

//    public static void main(String[] args) {
//        int[][] a = {
//                {1, 2, 3, 4}
//        };
//        int[][] expected = {
//                {1},
//                {2},
//                {3},
//                {4}
//        };
//        int[][] result = MatrixTransposer.transpose(a);
//        if (Arrays.deepEquals(result, expected)) {
//            System.out.println("testTransposeSingleColumnMatrix passed");
//        } else {
//            System.out.println("testTransposeSingleColumnMatrix failed");
//        }
//        int[][] b = transpose(a);
//        System.out.println(Arrays.deepToString(b));
//    }


    private static class LineTransposer implements Callable<int[]> {

        private final int[][] matrix;
        private final int index;

        public LineTransposer(int index, int[][] matrix) {
            this.matrix = matrix;
            this.index = index;
        }

        @Override
        public int[] call() {
            int[] result = new int[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                result[i] = matrix[i][index];
            }
            return result;
        }
    }
}
