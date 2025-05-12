package org.example;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class AsyncSumCalculator {
    public static Single<Integer> calculateSum(int n) {
        return Observable.range(1, n)
                .reduce(0, Integer::sum);
    }

    public static void main(String[] args) {
        System.out.println(calculateSum(0).blockingGet());
    }
}
